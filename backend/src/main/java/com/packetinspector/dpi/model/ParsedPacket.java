package com.packetinspector.dpi.model;

public record ParsedPacket(
        long timestampSeconds,
        long timestampFraction,
        String sourceMac,
        String destinationMac,
        int etherType,
        boolean hasIp,
        int ipVersion,
        String sourceIp,
        String destinationIp,
        int protocol,
        int ttl,
        boolean hasTcp,
        boolean hasUdp,
        int sourcePort,
        int destinationPort,
        int tcpFlags,
        long sequenceNumber,
        long acknowledgementNumber,
        int payloadOffset,
        int payloadLength) {}
