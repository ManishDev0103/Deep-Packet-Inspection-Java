package com.packetinspector.dpi.util;

import com.packetinspector.dpi.model.dto.ParsedPacket;
import com.packetinspector.dpi.model.dto.RawPacket;
import com.packetinspector.dpi.model.entity.EtherType;
import com.packetinspector.dpi.model.entity.Protocol;

import java.util.Optional;

public final class PacketParser {
    private static final int ETHERNET_HEADER_SIZE = 14;
    private static final int MIN_IPV4_HEADER_SIZE = 20;
    private static final int MIN_TCP_HEADER_SIZE = 20;
    private static final int UDP_HEADER_SIZE = 8;

    public Optional<ParsedPacket> parse(RawPacket raw) {
        byte[] data = raw.data();
        if (data.length < ETHERNET_HEADER_SIZE) return Optional.empty();

        String destinationMac = macToString(data, 0);
        String sourceMac = macToString(data, 6);
        int etherType = readU16(data, 12);
        int offset = ETHERNET_HEADER_SIZE;
        int ipVersion = 0;
        int protocol = 0;
        int ttl = 0;
        String sourceIp = "";
        String destinationIp = "";
        boolean hasIp = false;
        boolean hasTcp = false;
        boolean hasUdp = false;
        int sourcePort = 0;
        int destinationPort = 0;
        int tcpFlags = 0;
        long sequenceNumber = 0;
        long acknowledgementNumber = 0;

        if (etherType == EtherType.IPV4) {
            if (data.length < offset + MIN_IPV4_HEADER_SIZE) return Optional.empty();
            int versionAndLength = Byte.toUnsignedInt(data[offset]);
            ipVersion = versionAndLength >>> 4;
            int ipHeaderLength = (versionAndLength & 0x0f) * 4;
            if (ipVersion != 4 || ipHeaderLength < MIN_IPV4_HEADER_SIZE
                    || data.length < offset + ipHeaderLength) return Optional.empty();

            ttl = Byte.toUnsignedInt(data[offset + 8]);
            protocol = Byte.toUnsignedInt(data[offset + 9]);
            sourceIp = ipToString(readU32(data, offset + 12));
            destinationIp = ipToString(readU32(data, offset + 16));
            hasIp = true;
            offset += ipHeaderLength;

            if (protocol == Protocol.TCP) {
                if (data.length < offset + MIN_TCP_HEADER_SIZE) return Optional.empty();
                sourcePort = readU16(data, offset);
                destinationPort = readU16(data, offset + 2);
                sequenceNumber = readU32(data, offset + 4);
                acknowledgementNumber = readU32(data, offset + 8);
                int tcpHeaderLength = (Byte.toUnsignedInt(data[offset + 12]) >>> 4) * 4;
                tcpFlags = Byte.toUnsignedInt(data[offset + 13]);
                if (tcpHeaderLength < MIN_TCP_HEADER_SIZE
                        || data.length < offset + tcpHeaderLength) return Optional.empty();
                hasTcp = true;
                offset += tcpHeaderLength;
            } else if (protocol == Protocol.UDP) {
                if (data.length < offset + UDP_HEADER_SIZE) return Optional.empty();
                sourcePort = readU16(data, offset);
                destinationPort = readU16(data, offset + 2);
                hasUdp = true;
                offset += UDP_HEADER_SIZE;
            }
        }

        return Optional.of(new ParsedPacket(
                raw.header().timestampSeconds(), raw.header().timestampFraction(),
                sourceMac, destinationMac, etherType, hasIp, ipVersion,
                sourceIp, destinationIp, protocol, ttl, hasTcp, hasUdp,
                sourcePort, destinationPort, tcpFlags, sequenceNumber,
                acknowledgementNumber, offset, Math.max(0, data.length - offset)));
    }

    public static String macToString(byte[] data, int offset) {
        return "%02x:%02x:%02x:%02x:%02x:%02x".formatted(
                Byte.toUnsignedInt(data[offset]), Byte.toUnsignedInt(data[offset + 1]),
                Byte.toUnsignedInt(data[offset + 2]), Byte.toUnsignedInt(data[offset + 3]),
                Byte.toUnsignedInt(data[offset + 4]), Byte.toUnsignedInt(data[offset + 5]));
    }

    public static String ipToString(long ip) {
        return "%d.%d.%d.%d".formatted(
                (ip >>> 24) & 0xff, (ip >>> 16) & 0xff, (ip >>> 8) & 0xff, ip & 0xff);
    }

    public static String protocolToString(int protocol) {
        return Protocol.toString(protocol);
    }

    private static int readU16(byte[] data, int offset) {
        return (Byte.toUnsignedInt(data[offset]) << 8) | Byte.toUnsignedInt(data[offset + 1]);
    }

    private static long readU32(byte[] data, int offset) {
        return (((long) Byte.toUnsignedInt(data[offset])) << 24)
                | (((long) Byte.toUnsignedInt(data[offset + 1])) << 16)
                | (((long) Byte.toUnsignedInt(data[offset + 2])) << 8)
                | ((long) Byte.toUnsignedInt(data[offset + 3]));
    }
}
