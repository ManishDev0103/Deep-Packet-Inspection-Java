package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.AppType;
import com.packetinspector.dpi.model.FiveTuple;
import com.packetinspector.dpi.model.PacketJob;
import com.packetinspector.dpi.model.ParsedPacket;
import com.packetinspector.dpi.model.ProcessingReport;
import com.packetinspector.dpi.model.RawPacket;
import com.packetinspector.dpi.exception.InvalidPcapException;
import com.packetinspector.dpi.util.PacketParser;
import com.packetinspector.dpi.util.PcapReader;
import com.packetinspector.dpi.util.PcapWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

@Service
public final class DpiEngine {
    private static final int LOAD_BALANCER_COUNT = 2;
    private static final int FAST_PATHS_PER_BALANCER = 2;
    private static final int QUEUE_CAPACITY = 10_000;

    private final PacketParser parser = new PacketParser();

    public ProcessingReport process(Path inputPath, Path outputPath, List<String> blockedIps,
                                    List<String> blockedApps, List<String> blockedDomains,
                                    List<Integer> blockedPorts) throws IOException {
        RuleManager rules = new RuleManager();
        blockedIps.forEach(rules::blockIp);
        blockedApps.stream().map(AppType::fromName).forEach(rules::blockApp);
        blockedDomains.forEach(rules::blockDomain);
        blockedPorts.forEach(rules::blockPort);

        LongAdder totalPackets = new LongAdder();
        LongAdder totalBytes = new LongAdder();
        LongAdder tcpPackets = new LongAdder();
        LongAdder udpPackets = new LongAdder();
        BlockingQueue<PacketJob> outputQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        List<FastPathProcessor> processors = new ArrayList<>();
        List<LoadBalancer> balancers = new ArrayList<>();
        for (int i = 0; i < LOAD_BALANCER_COUNT * FAST_PATHS_PER_BALANCER; i++) {
            processors.add(new FastPathProcessor(i, QUEUE_CAPACITY, outputQueue, rules));
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
                                parseIpv4(packet.sourceIp()), parseIpv4(packet.destinationIp()),
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
                readFailure = new IOException("PCAP processing was interrupted", exception);
            } finally {
                finishPipeline(balancers, processors, outputQueue, outputThread);
            }

            if (readFailure != null) {
                throw new InvalidPcapException("Unable to read the uploaded PCAP", readFailure);
            }
            if (outputFailure.get() != null) throw outputFailure.get();
            for (LoadBalancer balancer : balancers) {
                if (balancer.failure() != null) throw balancer.failure();
            }
            for (FastPathProcessor processor : processors) {
                if (processor.failure() != null) throw processor.failure();
            }
            return createReport(totalPackets.sum(), totalBytes.sum(), tcpPackets.sum(),
                    udpPackets.sum(), balancers, processors, connectionTable, rules);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("PCAP processing was interrupted", exception);
        }
    }

    private static PacketJob createJob(long packetId, RawPacket raw, ParsedPacket packet,
                                       FiveTuple tuple) {
        byte[] data = raw.data();
        int ipOffset = 14;
        int transportOffset = ipOffset + (Byte.toUnsignedInt(data[ipOffset]) & 0x0f) * 4;
        return new PacketJob(packetId, tuple, data, 0, ipOffset, transportOffset,
                packet.payloadOffset(), packet.payloadLength(), packet.tcpFlags(),
                packet.timestampSeconds(), packet.timestampFraction());
    }

    private static long parseIpv4(String value) {
        long result = 0;
        for (String part : value.split("\\.")) result = (result << 8) | Integer.parseInt(part);
        return result;
    }

    private static void writeOutput(BlockingQueue<PacketJob> queue, PcapWriter writer,
                                    AtomicReference<IOException> failure) {
        IOException writeFailure = null;
        try {
            PacketJob job;
            while (!(job = queue.take()).endOfStream()) {
                if (writeFailure == null) {
                    try {
                        writer.write(new RawPacket(
                                new com.packetinspector.dpi.model.PcapPacketHeader(
                                        job.timestampSeconds(), job.timestampFraction(),
                                        job.data().length, job.data().length), job.data()));
                    } catch (IOException exception) {
                        writeFailure = exception;
                    }
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        failure.set(writeFailure);
    }

    private static void finishPipeline(List<LoadBalancer> balancers,
                                       List<FastPathProcessor> processors,
                                       BlockingQueue<PacketJob> outputQueue,
                                       Thread outputThread) throws InterruptedException {
        for (LoadBalancer balancer : balancers) balancer.finish();
        for (LoadBalancer balancer : balancers) balancer.join();
        for (FastPathProcessor processor : processors) processor.finish();
        for (FastPathProcessor processor : processors) processor.join();
        outputQueue.put(PacketJob.stopSignal());
        outputThread.join();
    }

    private static ProcessingReport createReport(long totalPackets, long totalBytes,
                                                 long tcpPackets, long udpPackets,
                                                 List<LoadBalancer> balancers,
                                                 List<FastPathProcessor> processors,
                                                 GlobalConnectionTable connectionTable,
                                                 RuleManager rules) {
        long lbReceived = 0, lbDispatched = 0, fpProcessed = 0, fpForwarded = 0;
        long fpDropped = 0, sniExtractions = 0, classificationHits = 0;
        for (LoadBalancer balancer : balancers) {
            lbReceived += balancer.stats().packetsReceived();
            lbDispatched += balancer.stats().packetsDispatched();
        }
        for (FastPathProcessor processor : processors) {
            FastPathProcessor.Stats stats = processor.stats();
            fpProcessed += stats.processed();
            fpForwarded += stats.forwarded();
            fpDropped += stats.dropped();
            sniExtractions += stats.sniExtractions();
            classificationHits += stats.classificationHits();
        }
        GlobalConnectionTable.GlobalStats connections = connectionTable.stats();
        RuleManager.RuleStats ruleStats = rules.stats();
        return new ProcessingReport(totalPackets, totalBytes, fpForwarded, fpDropped,
                tcpPackets, udpPackets, 0, connections.activeConnections(),
                connections.connectionsSeen(), lbReceived, lbDispatched, fpProcessed,
                fpForwarded, fpDropped, sniExtractions, classificationHits,
                new ProcessingReport.RuleCounts(ruleStats.blockedIps(), ruleStats.blockedApps(),
                        ruleStats.blockedDomains(), ruleStats.blockedPorts()),
                connections.applications(), connections.topDomains());
    }
}
