package com.packetinspector.dpi.model;

public record PcapPacketHeader(
        long timestampSeconds,
        long timestampFraction,
        long capturedLength,
        long originalLength) {}
