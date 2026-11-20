package com.packetinspector.dpi.model.dto;

import com.packetinspector.dpi.model.entity.Protocol;

public record FiveTuple(
        long srcIp,
        long dstIp,
        int srcPort,
        int dstPort,
        int protocol
) {
    public FiveTuple reverse() {
        return new FiveTuple(dstIp, srcIp, dstPort, srcPort, protocol);
    }

    public static String ipToString(long ip) {
        return ((ip >> 0) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 24) & 0xFF);
    }

    public static long parseIp(String ipStr) {
        if (ipStr == null || ipStr.isBlank()) return 0L;
        String[] parts = ipStr.trim().split("\\.");
        if (parts.length != 4) return 0L;
        long result = 0L;
        for (int i = 0; i < 4; i++) {
            long octet = Long.parseLong(parts[i]);
            result |= (octet & 0xFFL) << (i * 8);
        }
        return result;
    }

    public String toFormattedString() {
        return ipToString(srcIp) + ":" + srcPort + " -> " +
               ipToString(dstIp) + ":" + dstPort + " (" +
               Protocol.toString(protocol) + ")";
    }
}
