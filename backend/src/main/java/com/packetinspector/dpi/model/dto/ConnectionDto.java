package com.packetinspector.dpi.model.dto;

public record ConnectionDto(
        String flowKey,
        String srcIp,
        int srcPort,
        String dstIp,
        int dstPort,
        String protocol,
        String state,
        String appType,
        String sni,
        long packetsIn,
        long packetsOut,
        long bytesIn,
        long bytesOut,
        String action,
        String firstSeen,
        String lastSeen
) {}
