package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.PcapGlobalHeader;
import com.packetinspector.dpi.model.PcapPacketHeader;
import com.packetinspector.dpi.model.RawPacket;
import com.packetinspector.dpi.util.PcapReader;
import com.packetinspector.dpi.util.PcapWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DpiEngineTest {
    @TempDir
    Path directory;

    private final DpiEngine engine = new DpiEngine();

    @Test
    void forwardsPacketAndWritesValidPcap() throws Exception {
        Path input = writeUdpCapture("input.pcap");
        Path output = directory.resolve("forwarded.pcap");

        var report = engine.process(input, output, List.of(), List.of(), List.of(), List.of());

        assertEquals(1, report.totalPackets());
        assertEquals(1, report.forwardedPackets());
        assertEquals(0, report.droppedPackets());
        assertEquals(1, report.udpPackets());
        try (PcapReader reader = new PcapReader(output)) {
            assertArrayEquals(packetBytes(), reader.readNextPacket().orElseThrow().data());
            assertTrue(reader.readNextPacket().isEmpty());
        }
    }

    @Test
    void dropsPacketsMatchingDestinationPortRule() throws Exception {
        Path input = writeUdpCapture("blocked-input.pcap");
        Path output = directory.resolve("blocked-output.pcap");

        var report = engine.process(input, output, List.of(), List.of(), List.of(), List.of(53));

        assertEquals(1, report.totalPackets());
        assertEquals(0, report.forwardedPackets());
        assertEquals(1, report.droppedPackets());
        try (PcapReader reader = new PcapReader(output)) {
            assertTrue(reader.readNextPacket().isEmpty());
        }
    }

    private Path writeUdpCapture(String name) throws Exception {
        Path path = directory.resolve(name);
        byte[] packet = packetBytes();
        var globalHeader = new PcapGlobalHeader(
                2, 4, 0, 0, 65_535, 1, ByteOrder.LITTLE_ENDIAN, false);
        try (PcapWriter writer = new PcapWriter(path, globalHeader)) {
            writer.write(new RawPacket(new PcapPacketHeader(1, 0, packet.length, packet.length), packet));
        }
        return path;
    }

    private static byte[] packetBytes() {
        byte[] packet = new byte[42];
        packet[12] = 8;
        packet[13] = 0;
        packet[14] = 0x45;
        packet[16] = 0;
        packet[17] = 28;
        packet[22] = 64;
        packet[23] = 17;
        packet[26] = (byte) 192;
        packet[27] = 0;
        packet[28] = 2;
        packet[29] = 1;
        packet[30] = (byte) 198;
        packet[31] = 51;
        packet[32] = 100;
        packet[33] = 2;
        packet[34] = 0x30;
        packet[35] = 0x39;
        packet[36] = 0;
        packet[37] = 53;
        packet[38] = 0;
        packet[39] = 8;
        return packet;
    }
}
