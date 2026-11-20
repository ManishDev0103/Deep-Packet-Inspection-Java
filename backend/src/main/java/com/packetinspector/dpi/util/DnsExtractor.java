package com.packetinspector.dpi.util;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class DnsExtractor {
    public Optional<String> extractQuery(byte[] payload, int offset, int length) {
        if (!isQuery(payload, offset, length)) return Optional.empty();
        int end = offset + length;
        int cursor = offset + 12;
        StringBuilder domain = new StringBuilder();
        while (cursor < end) {
            int labelLength = Byte.toUnsignedInt(payload[cursor++]);
            if (labelLength == 0) break;
            if (labelLength > 63 || cursor + labelLength > end) break;
            if (!domain.isEmpty()) domain.append('.');
            domain.append(new String(payload, cursor, labelLength, StandardCharsets.US_ASCII));
            cursor += labelLength;
        }
        return domain.isEmpty() ? Optional.empty() : Optional.of(domain.toString());
    }

    public boolean isQuery(byte[] payload, int offset, int length) {
        return length >= 12
                && (Byte.toUnsignedInt(payload[offset + 2]) & 0x80) == 0
                && (((Byte.toUnsignedInt(payload[offset + 4]) << 8)
                | Byte.toUnsignedInt(payload[offset + 5])) > 0);
    }
}
