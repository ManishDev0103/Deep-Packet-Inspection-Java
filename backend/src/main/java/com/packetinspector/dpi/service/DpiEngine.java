package com.packetinspector.dpi.service;

import com.packetinspector.dpi.exception.InvalidPcapException;
import com.packetinspector.dpi.model.dto.*;
import com.packetinspector.dpi.model.entity.*;
import com.packetinspector.dpi.repository.ConnectionRepository;
import com.packetinspector.dpi.util.PacketParser;
import com.packetinspector.dpi.util.PcapReader;
import com.packetinspector.dpi.util.PcapWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

@Service
public class DpiEngine {
    private static final int LOAD_BALANCER_COUNT = 2;
    private static final int FAST_PATHS_PER_BALANCER = 2;
    private static final int QUEUE_CAPACITY = 10_000;

    private final RuleManager ruleManager;
    private final ConnectionRepository connectionRepository;
    private final PacketParser parser = new PacketParser();

    public DpiEngine(RuleManager ruleManager, ConnectionRepository connectionRepository) {
        this.ruleManager = ruleManager;
        this.connectionRepository = connectionRepository;
    }

    public ProcessingReport process(Path inputPath, Path outputPath,
                                    List<String> blockedIps,
                                    List<String> blockedApps,
                                    List<String> blockedDomains,
                                    List<Integer> blockedPorts) throws IOException {
        if (blockedIps != null) blockedIps.forEach(ruleManager::blockIp);
        if (blockedApps != null) blockedApps.stream().map(AppType::fromDisplayName).forEach(ruleManager::blockApp);
        if (blockedDomains != null) blockedDomains.forEach(ruleManager::blockDomain);
        if (blockedPorts != null) blockedPorts.forEach(ruleManager::blockPort);

        LongAdder totalPackets = new LongAdder();
        LongAdder totalBytes = new LongAdder();
        LongAdder tcpPackets = new LongAdder();
        LongAdder udpPackets = new LongAdder();
        BlockingQueue<PacketJob> outputQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        List<FastPathProcessor> processors = new ArrayList<>();
        List<LoadBalancer> balancers = new ArrayList<>();

        for (int i = 0; i < LOAD_BALANCER_COUNT * FAST_PATHS_PER_BALANCER; i++) {
            processors.add(new FastPathProcessor(i, QUEUE_CAPACITY, outputQueue, ruleManager));
        }
        for (int i = 0; i < LOAD_BALANCER_COUNT; i++) {
            List<BlockingQueue<PacketJob>> queues = processors.subList(
                    i * FAST_PATHS_PER_BALANCER, (i + 1) * FAST_PATHS_PER_BALANCER)
                    .stream().map(FastPathProcessor::input).toList();
            balancers.add(new LoadBalancer(i, QUEUE_CAPACITY, queues));
        }
        List<ConnectionTracker> trackers = processors.stream()
                .map(FastPathProcessor::connectionTracker).toList();
        GlobalConnectionTable connectionTable = new GlobalConnectionTable(trackers);
        AtomicReference<IOException> outputFailure = new AtomicReference<>();

        PcapReader reader;
        try {
            reader = new PcapReader(inputPath);
        } catch (IOException exception) {
            throw new InvalidPcapException("Unable to read the uploaded PCAP", exception);
        }

        try (reader; PcapWriter writer = new PcapWriter(outputPath, reader.globalHeader())) {
            Thread outputThread = new Thread(
                    () -> writeOutput(outputQueue, writer, outputFailure), "dpi-output");
            outputThread.start();
            processors.forEach(FastPathProcessor::start);
            balancers.forEach(LoadBalancer::start);

            IOException readFailure = null;
            try {
                long packetId = 0;
                var next = reader.readNextPacket();
                while (next.isPresent()) {
                    RawPacket raw = next.get();
                    var parsed = parser.parse(raw);
                    if (parsed.isPresent() && parsed.get().hasIp()
                            && (parsed.get().hasTcp() || parsed.get().hasUdp())) {
                        ParsedPacket packet = parsed.get();
                        FiveTuple tuple = new FiveTuple(
                                RuleManager.parseIpv4(packet.sourceIp()),
                                RuleManager.parseIpv4(packet.destinationIp()),
                                packet.sourcePort(), packet.destinationPort(), packet.protocol());
                        PacketJob job = createJob(packetId++, raw, packet, tuple);
                        totalPackets.increment();
                        totalBytes.add(raw.data().length);
                        if (packet.hasTcp()) tcpPackets.increment();
                        else if (packet.hasUdp()) udpPackets.increment();
                        int balancerIndex = Math.floorMod(tuple.hashCode(), balancers.size());
                        balancers.get(balancerIndex).input().put(job);
                    }
                    next = reader.readNextPacket();
                }
            } catch (IOException exception) {
                readFailure = new IOException("Unable to read the uploaded PCAP", exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                readFailure = new IOException("Packet ingestion was interrupted", exception);
            }

            for (LoadBalancer balancer : balancers) {
                try {
                    balancer.finish();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            for (LoadBalancer balancer : balancers) {
                try {
                    balancer.join();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            for (FastPathProcessor processor : processors) {
                try {
                    processor.finish();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            for (FastPathProcessor processor : processors) {
                try {
                    processor.join();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }

            try {
                outputQueue.put(PacketJob.stopSignal());
                outputThread.join();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            if (readFailure != null) throw readFailure;
            if (outputFailure.get() != null) throw outputFailure.get();

            // Persist flows to MySQL
            persistConnections(trackers);

            long totalCount = totalPackets.sum();
            long lbReceived = balancers.stream().mapToLong(b -> b.stats().packetsReceived()).sum();
            long lbDispatched = balancers.stream().mapToLong(b -> b.stats().packetsDispatched()).sum();
            long fpProcessed = processors.stream().mapToLong(p -> p.stats().processed()).sum();
            long fpForwarded = processors.stream().mapToLong(p -> p.stats().forwarded()).sum();
            long fpDropped = processors.stream().mapToLong(p -> p.stats().dropped()).sum();
            long sniHits = processors.stream().mapToLong(p -> p.stats().sniExtractions()).sum();
            long appHits = processors.stream().mapToLong(p -> p.stats().classificationHits()).sum();

            GlobalConnectionTable.GlobalStats globalStats = connectionTable.stats();
            RuleStats ruleStats = ruleManager.getStats();
            ProcessingReport.RuleCounts counts = new ProcessingReport.RuleCounts(
                    ruleStats.blockedIps(), ruleStats.blockedApps(),
                    ruleStats.blockedDomains(), ruleStats.blockedPorts());

            return new ProcessingReport(
                    totalCount, totalBytes.sum(), fpForwarded, fpDropped,
                    tcpPackets.sum(), udpPackets.sum(), 0,
                    globalStats.activeConnections(), globalStats.connectionsSeen(),
                    lbReceived, lbDispatched, fpProcessed, fpForwarded, fpDropped,
                    sniHits, appHits, counts,
                    globalStats.applications(), globalStats.topDomains());
        }
    }

    private void persistConnections(List<ConnectionTracker> trackers) {
        if (connectionRepository == null) return;
        try {
            List<ConnectionEntity> entities = new ArrayList<>();
            for (ConnectionTracker tracker : trackers) {
                for (Connection c : tracker.all()) {
                    ConnectionEntity entity = new ConnectionEntity();
                    entity.setFlowKey(c.tuple().toFormattedString());
                    entity.setSrcIp(FiveTuple.ipToString(c.tuple().srcIp()));
                    entity.setSrcPort(c.tuple().srcPort());
                    entity.setDstIp(FiveTuple.ipToString(c.tuple().dstIp()));
                    entity.setDstPort(c.tuple().dstPort());
                    entity.setProtocol(c.tuple().protocol());
                    entity.setState(c.state());
                    entity.setAppType(c.appType());
                    entity.setSni(c.sni());
                    entity.setPacketsIn(c.packetsIn());
                    entity.setPacketsOut(c.packetsOut());
                    entity.setBytesIn(c.bytesIn());
                    entity.setBytesOut(c.bytesOut());
                    entity.setAction(c.action());
                    entity.setFirstSeen(Instant.now());
                    entity.setLastSeen(Instant.now());
                    entities.add(entity);
                    if (entities.size() >= 100) break;
                }
                if (entities.size() >= 100) break;
            }
            if (!entities.isEmpty()) {
                connectionRepository.saveAll(entities);
            }
        } catch (Exception ignored) {
        }
    }

    private static PacketJob createJob(long packetId, RawPacket raw, ParsedPacket packet, FiveTuple tuple) {
        int transportOffset = packet.hasTcp()
                ? packet.payloadOffset() - 20
                : (packet.hasUdp() ? packet.payloadOffset() - 8 : packet.payloadOffset());
        return new PacketJob(
                packetId, tuple, raw.data(), 0, 14, transportOffset,
                packet.payloadOffset(), packet.payloadLength(),
                packet.tcpFlags(), raw.header().timestampSeconds(), raw.header().timestampFraction());
    }

    private static void writeOutput(BlockingQueue<PacketJob> queue, PcapWriter writer,
                                    AtomicReference<IOException> failure) {
        try {
            PacketJob job;
            while (!(job = queue.take()).endOfStream()) {
                writer.write(new RawPacket(
                        new PcapPacketHeader(job.timestampSeconds(), job.timestampFraction(),
                                job.data().length, job.data().length),
                        job.data()));
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            failure.set(new IOException("Output writer was interrupted", exception));
        } catch (IOException exception) {
            failure.set(exception);
        }
    }
}
