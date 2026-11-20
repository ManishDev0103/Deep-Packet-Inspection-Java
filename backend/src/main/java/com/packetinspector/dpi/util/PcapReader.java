package com.packetinspector.dpi.util;

import com.packetinspector.dpi.model.dto.PcapGlobalHeader;
import com.packetinspector.dpi.model.dto.PcapPacketHeader;
import com.packetinspector.dpi.model.dto.RawPacket;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class PcapReader implements AutoCloseable {
    private static final int GLOBAL_HEADER_SIZE = 24;
    private static final int PACKET_HEADER_SIZE = 16;
    private static final long MAX_PACKET_SIZE = 65_535;

    private final InputStream input;
    private final PcapGlobalHeader globalHeader;

    public PcapReader(Path path) throws IOException {
        input = new BufferedInputStream(Files.newInputStream(path));
        byte[] bytes = input.readNBytes(GLOBAL_HEADER_SIZE);
        if (bytes.length != GLOBAL_HEADER_SIZE) {
            input.close();
            throw new IOException("PCAP global header is truncated");
        }

        ByteOrder order;
        if (matches(bytes, 0xa1, 0xb2, 0xc3, 0xd4)) order = ByteOrder.BIG_ENDIAN;
        else if (matches(bytes, 0xd4, 0xc3, 0xb2, 0xa1)) order = ByteOrder.LITTLE_ENDIAN;
        else {
            input.close();
            throw new IOException("Invalid PCAP magic number");
        }

        ByteBuffer fields = ByteBuffer.wrap(bytes).order(order);
        globalHeader = new PcapGlobalHeader(
                Short.toUnsignedInt(fields.getShort(4)),
                Short.toUnsignedInt(fields.getShort(6)),
                fields.getInt(8),
                Integer.toUnsignedLong(fields.getInt(12)),
                Integer.toUnsignedLong(fields.getInt(16)),
                Integer.toUnsignedLong(fields.getInt(20)),
                order,
                false);
    }

    public PcapGlobalHeader globalHeader() {
        return globalHeader;
    }

    public Optional<RawPacket> readNextPacket() throws IOException {
        int first = input.read();
        if (first < 0) return Optional.empty();

        byte[] header = new byte[PACKET_HEADER_SIZE];
        header[0] = (byte) first;
        readFully(header, 1, PACKET_HEADER_SIZE - 1, "PCAP packet header is truncated");
        ByteBuffer fields = ByteBuffer.wrap(header).order(globalHeader.byteOrder());
        long seconds = Integer.toUnsignedLong(fields.getInt());
        long fraction = Integer.toUnsignedLong(fields.getInt());
        long capturedLength = Integer.toUnsignedLong(fields.getInt());
        long originalLength = Integer.toUnsignedLong(fields.getInt());

        if (capturedLength > globalHeader.snapshotLength() || capturedLength > MAX_PACKET_SIZE) {
            throw new IOException("Invalid PCAP packet length: " + capturedLength);
        }
        byte[] data = new byte[(int) capturedLength];
        readFully(data, 0, data.length, "PCAP packet data is truncated");
        return Optional.of(new RawPacket(
                new PcapPacketHeader(seconds, fraction, capturedLength, originalLength), data));
    }

    private void readFully(byte[] data, int offset, int length, String message) throws IOException {
        int read = 0;
        while (read < length) {
            int count = input.read(data, offset + read, length - read);
            if (count < 0) throw new EOFException(message);
            read += count;
        }
    }

    private static boolean matches(byte[] bytes, int b0, int b1, int b2, int b3) {
        return Byte.toUnsignedInt(bytes[0]) == b0
                && Byte.toUnsignedInt(bytes[1]) == b1
                && Byte.toUnsignedInt(bytes[2]) == b2
                && Byte.toUnsignedInt(bytes[3]) == b3;
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
