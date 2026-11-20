package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.dto.Connection;
import com.packetinspector.dpi.model.dto.PacketJob;
import com.packetinspector.dpi.model.entity.AppType;
import com.packetinspector.dpi.model.entity.ConnectionState;
import com.packetinspector.dpi.model.entity.PacketAction;
import com.packetinspector.dpi.model.entity.Protocol;
import com.packetinspector.dpi.model.entity.TcpFlags;
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
        boolean outbound = job.tuple().srcPort() >= job.tuple().dstPort();
        Connection connection = connectionTracker.getOrCreate(job.tuple());
        connection = connectionTracker.updateTraffic(connection, job.data().length, outbound);

        if (job.tuple().protocol() == Protocol.TCP) {
            updateTcpState(connection, job.tcpFlags());
        }

        if (connection.action() == PacketAction.DROP) return PacketAction.DROP;

        if (connection.state() != ConnectionState.CLASSIFIED && job.payloadLength() > 0) {
            inspectPayload(job, connection);
            connection = connectionTracker.get(job.tuple());
        }

        var reason = rules.shouldBlock(job.tuple().srcIp(), job.tuple().dstPort(),
                connection.appType(), connection.sni());
        if (reason.isPresent()) {
            connectionTracker.block(connection);
            return PacketAction.DROP;
        }
        return PacketAction.FORWARD;
    }

    private void inspectPayload(PacketJob job, Connection connection) {
        byte[] payload = job.data();
        int offset = job.payloadOffset();
        int length = job.payloadLength();
        if (length <= 0) return;

        if (job.tuple().protocol() == Protocol.TCP) {
            var sni = sniExtractor.extract(payload, offset, length);
            if (sni.isPresent()) {
                sniExtractions++;
                classificationHits++;
                AppType app = classifier.fromDomain(sni.get());
                connectionTracker.classify(connection, app, sni.get());
                return;
            }

            var host = httpHostExtractor.extract(payload, offset, length);
            if (host.isPresent()) {
                classificationHits++;
                AppType app = classifier.fromDomain(host.get());
                connectionTracker.classify(connection, app, host.get());
                return;
            }
        } else if (job.tuple().protocol() == Protocol.UDP) {
            if (job.tuple().dstPort() == 53 || job.tuple().srcPort() == 53) {
                var query = dnsExtractor.extractQuery(payload, offset, length);
                if (query.isPresent()) {
                    classificationHits++;
                    AppType app = classifier.fromDomain(query.get());
                    connectionTracker.classify(connection, app, query.get());
                    return;
                }
            }
        }
    }

    private void updateTcpState(Connection connection, int tcpFlags) {
        boolean syn = (tcpFlags & TcpFlags.SYN) != 0;
        boolean ack = (tcpFlags & TcpFlags.ACK) != 0;
        boolean fin = (tcpFlags & TcpFlags.FIN) != 0;
        boolean rst = (tcpFlags & TcpFlags.RST) != 0;

        if (rst) {
            connectionTracker.close(connection.tuple());
            return;
        }

        boolean synSeen = connection.synSeen() || syn;
        boolean synAckSeen = connection.synAckSeen() || (syn && ack);
        boolean finSeen = connection.finSeen() || fin;

        ConnectionState state = connection.state();
        if (state == ConnectionState.NEW && syn) state = ConnectionState.ESTABLISHED;
        if (fin) state = ConnectionState.CLOSED;

        connectionTracker.update(connection.withTcpState(state, synSeen, synAckSeen, finSeen));
    }
}
