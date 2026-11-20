package com.packetinspector.dpi.model.dto;

public record RawPacket(
        PcapPacketHeader header,
        byte[] data
) {}
