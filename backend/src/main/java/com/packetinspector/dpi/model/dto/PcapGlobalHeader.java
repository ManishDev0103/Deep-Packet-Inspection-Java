package com.packetinspector.dpi.model.dto;

import java.nio.ByteOrder;

public record PcapGlobalHeader(
        int versionMajor,
        int versionMinor,
        int thisZone,
        long significantFigures,
        long snapshotLength,
        long linkType,
        ByteOrder byteOrder,
        boolean nanoseconds
) {}
