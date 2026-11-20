package com.packetinspector.dpi.model.dto;

public record PcapPacketHeader(
        long timestampSeconds,
        long timestampFraction,
        long capturedLength,
        long originalLength
) {}
