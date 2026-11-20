package com.packetinspector.dpi.util;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class SniExtractor {
    private static final int TLS_HANDSHAKE = 0x16;
    private static final int CLIENT_HELLO = 0x01;

    public Optional<String> extract(byte[] payload, int offset, int length) {
        if (!isClientHello(payload, offset, length)) return Optional.empty();

        int end = offset + length;
        int cursor = offset + 9;
        if (cursor + 34 > end) return Optional.empty();
        cursor += 34;

        if (cursor >= end) return Optional.empty();
        int sessionLength = u8(payload, cursor++);
        if (cursor + sessionLength + 2 > end) return Optional.empty();
        cursor += sessionLength;

        int cipherLength = u16(payload, cursor);
        cursor += 2;
        if (cursor + cipherLength + 1 > end) return Optional.empty();
        cursor += cipherLength;

        int compressionLength = u8(payload, cursor++);
        if (cursor + compressionLength + 2 > end) return Optional.empty();
        cursor += compressionLength;

        int extensionsLength = u16(payload, cursor);
        cursor += 2;
        int extensionsEnd = Math.min(end, cursor + extensionsLength);
        while (cursor + 4 <= extensionsEnd) {
            int type = u16(payload, cursor);
            int extensionLength = u16(payload, cursor + 2);
            cursor += 4;
            if (cursor + extensionLength > extensionsEnd) return Optional.empty();
            if (type == 0) return readServerName(payload, cursor, extensionLength);
            cursor += extensionLength;
        }
        return Optional.empty();
    }

    public boolean isClientHello(byte[] payload, int offset, int length) {
        if (length < 9 || u8(payload, offset) != TLS_HANDSHAKE) return false;
        int version = u16(payload, offset + 1);
        return version >= 0x0300 && version <= 0x0304
                && u16(payload, offset + 3) <= length - 5
                && u8(payload, offset + 5) == CLIENT_HELLO;
    }

    private Optional<String> readServerName(byte[] payload, int offset, int length) {
        if (length < 5) return Optional.empty();
        int listLength = u16(payload, offset);
        int nameLength = u16(payload, offset + 3);
        if (listLength < 3 || u8(payload, offset + 2) != 0 || nameLength > length - 5) {
            return Optional.empty();
        }
        return Optional.of(new String(payload, offset + 5, nameLength, StandardCharsets.US_ASCII));
    }

    private static int u8(byte[] data, int offset) {
        return Byte.toUnsignedInt(data[offset]);
    }

    private static int u16(byte[] data, int offset) {
        return (u8(data, offset) << 8) | u8(data, offset + 1);
    }
}
