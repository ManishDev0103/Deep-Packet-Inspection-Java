package com.packetinspector.dpi.model;

public record RawPacket(PcapPacketHeader header, byte[] data) {}
