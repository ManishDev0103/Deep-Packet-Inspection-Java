package com.packetinspector.dpi.util;

import com.packetinspector.dpi.model.dto.PcapGlobalHeader;
import com.packetinspector.dpi.model.dto.PcapPacketHeader;
import com.packetinspector.dpi.model.dto.RawPacket;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PcapWriter implements Closeable {
    private final OutputStream output;
    private final ByteOrder byteOrder;

    public PcapWriter(Path path, PcapGlobalHeader header) throws IOException {
        output = new BufferedOutputStream(Files.newOutputStream(path));
        byteOrder = header.byteOrder();
        ByteBuffer bytes = ByteBuffer.allocate(24).order(byteOrder);
        bytes.putInt(0xa1b2c3d4);
        bytes.putShort((short) header.versionMajor());
        bytes.putShort((short) header.versionMinor());
        bytes.putInt(header.thisZone());
        bytes.putInt((int) header.significantFigures());
        bytes.putInt((int) header.snapshotLength());
        bytes.putInt((int) header.linkType());
        output.write(bytes.array());
    }

    public void write(RawPacket packet) throws IOException {
        PcapPacketHeader header = packet.header();
        ByteBuffer bytes = ByteBuffer.allocate(16).order(byteOrder);
        bytes.putInt((int) header.timestampSeconds());
        bytes.putInt((int) header.timestampFraction());
        bytes.putInt(packet.data().length);
        bytes.putInt(packet.data().length);
        output.write(bytes.array());
        output.write(packet.data());
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
