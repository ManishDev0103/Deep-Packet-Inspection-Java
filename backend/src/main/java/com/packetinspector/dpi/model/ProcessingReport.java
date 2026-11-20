package com.packetinspector.dpi.model;

import java.util.Map;

public record ProcessingReport(
        long totalPackets,
        long totalBytes,
        long forwardedPackets,
        long droppedPackets,
        long tcpPackets,
        long udpPackets,
        long otherPackets,
        long activeConnections,
        long connectionsSeen,
        long loadBalancerReceived,
        long loadBalancerDispatched,
        long fastPathProcessed,
        long fastPathForwarded,
        long fastPathDropped,
        long sniExtractions,
        long classificationHits,
        RuleCounts rules,
        Map<AppType, Long> applications,
        Map<String, Long> topDomains) {

    public record RuleCounts(long blockedIps, long blockedApps, long blockedDomains, long blockedPorts) {}

    public ProcessingReport {
        applications = Map.copyOf(applications);
        topDomains = Map.copyOf(topDomains);
    }
}
