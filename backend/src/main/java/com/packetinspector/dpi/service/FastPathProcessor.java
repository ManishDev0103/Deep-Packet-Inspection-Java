package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.AppType;
import com.packetinspector.dpi.model.Connection;
import com.packetinspector.dpi.model.ConnectionState;
import com.packetinspector.dpi.model.PacketAction;
import com.packetinspector.dpi.model.PacketJob;
import com.packetinspector.dpi.model.Protocol;
import com.packetinspector.dpi.model.TcpFlags;
import com.packetinspector.dpi.util.ApplicationClassifier;
import com.packetinspector.dpi.util.DnsExtractor;
import com.packetinspector.dpi.util.HttpHostExtractor;
import com.packetinspector.dpi.util.SniExtractor;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.LongAdder;

final class FastPathProcessor implements Runnable {
    record Stats(long processed, long forwarded, long dropped, long connections,
                 long sniExtractions, long classificationHits) {}

    private final int id;
    private final BlockingQueue<PacketJob> input;
    private final BlockingQueue<PacketJob> output;
    private final ConnectionTracker connectionTracker = new ConnectionTracker(100_000);
    private final RuleManager rules;
    private final SniExtractor sniExtractor = new SniExtractor();
    private final HttpHostExtractor httpHostExtractor = new HttpHostExtractor();
    private final DnsExtractor dnsExtractor = new DnsExtractor();
    private final ApplicationClassifier classifier = new ApplicationClassifier();
    private final LongAdder processed = new LongAdder();
    private final LongAdder forwarded = new LongAdder();
    private final LongAdder dropped = new LongAdder();
    private long sniExtractions;
    private long classificationHits;
    private volatile IOException failure;
    private final Thread thread;

    FastPathProcessor(int id, int queueSize, BlockingQueue<PacketJob> output, RuleManager rules) {
        this.id = id;
        input = new ArrayBlockingQueue<>(queueSize);
        this.output = output;
        this.rules = rules;
        thread = new Thread(this, "dpi-fp-" + id);
    }

    BlockingQueue<PacketJob> input() {
        return input;
    }

    ConnectionTracker connectionTracker() {
        return connectionTracker;
    }

    void start() {
        thread.start();
    }

    void finish() throws InterruptedException {
        input.put(PacketJob.stopSignal());
    }

    void join() throws InterruptedException {
        thread.join();
    }

    Stats stats() {
        return new Stats(processed.sum(), forwarded.sum(), dropped.sum(),
                connectionTracker.activeCount(), sniExtractions, classificationHits);
    }

    IOException failure() {
        return failure;
    }

    @Override
    public void run() {
        try {
            PacketJob job;
            while (!(job = input.take()).endOfStream()) {
                processed.increment();
                PacketAction action;
                try {
                    action = processPacket(job);
                } catch (RuntimeException exception) {
                    failure = new IOException("Fast-path packet inspection failed", exception);
                    action = PacketAction.DROP;
                }
                if (action == PacketAction.DROP) dropped.increment();
                else {
                    forwarded.increment();
                    output.put(job);
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            failure = new IOException("Fast-path processor was interrupted", exception);
        }
    }

    private PacketAction processPacket(PacketJob job) {
        Connection connection = connectionTracker.getOrCreate(job.tuple());
        connection = connectionTracker.updateTraffic(connection, job.data().length, true);
        if (job.tuple().protocol() == Protocol.TCP) connection = updateTcpState(connection, job.tcpFlags());
        if (connection.state() == ConnectionState.BLOCKED) return PacketAction.DROP;

        if (connection.state() != ConnectionState.CLASSIFIED && job.payloadLength() > 0) {
            connection = inspectPayload(job, connection);
        }
        var reason = rules.shouldBlock(job.tuple().sourceIp(), job.tuple().destinationPort(),
                connection.appType(), connection.sni());
        if (reason.isPresent()) {
            connectionTracker.block(connection);
            return PacketAction.DROP;
        }
        return PacketAction.FORWARD;
    }

    private Connection inspectPayload(PacketJob job, Connection connection) {
        byte[] data = job.data();
        int offset = job.payloadOffset();
        int length = job.payloadLength();
        var sni = (job.tuple().destinationPort() == 443 || length >= 50)
                ? sniExtractor.extract(data, offset, length) : java.util.Optional.<String>empty();
        if (sni.isPresent()) {
            String serverName = sni.get();
            sniExtractions++;
            AppType app = classifier.fromDomain(serverName);
            if (app != AppType.UNKNOWN && app != AppType.HTTPS) classificationHits++;
            return connectionTracker.classify(connection, app, serverName);
        }
        if (job.tuple().destinationPort() == 80) {
            var host = httpHostExtractor.extract(data, offset, length);
            if (host.isPresent()) {
                AppType app = classifier.fromDomain(host.get());
                if (app != AppType.UNKNOWN && app != AppType.HTTP) classificationHits++;
                return connectionTracker.classify(connection, app, host.get());
            }
        }
        if (job.tuple().destinationPort() == 53 || job.tuple().sourcePort() == 53) {
            var domain = dnsExtractor.extractQuery(data, offset, length);
            if (domain.isPresent()) return connectionTracker.classify(connection, AppType.DNS, domain.get());
        }
        if (job.tuple().destinationPort() == 80) {
            return connectionTracker.classify(connection, AppType.HTTP, "");
        }
        if (job.tuple().destinationPort() == 443) {
            return connectionTracker.classify(connection, AppType.HTTPS, "");
        }
        return connection;
    }

    private Connection updateTcpState(Connection connection, int flags) {
        boolean synSeen = connection.synSeen();
        boolean synAckSeen = connection.synAckSeen();
        boolean finSeen = connection.finSeen();
        ConnectionState state = connection.state();
        if ((flags & TcpFlags.SYN) != 0) {
            if ((flags & TcpFlags.ACK) != 0) synAckSeen = true;
            else synSeen = true;
        }
        if (synSeen && synAckSeen && (flags & TcpFlags.ACK) != 0
                && state == ConnectionState.NEW) state = ConnectionState.ESTABLISHED;
        if ((flags & TcpFlags.FIN) != 0) finSeen = true;
        if ((flags & TcpFlags.RST) != 0 || (finSeen && (flags & TcpFlags.ACK) != 0)) {
            state = ConnectionState.CLOSED;
        }
        Connection updated = connection.withTcpState(state, synSeen, synAckSeen, finSeen);
        return connectionTracker.update(updated);
    }
}
