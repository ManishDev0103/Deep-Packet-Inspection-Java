package com.packetinspector.dpi.util;

import java.util.Optional;

public final class QuicSniExtractor {
    private final SniExtractor tlsExtractor;

    public QuicSniExtractor(SniExtractor tlsExtractor) {
        this.tlsExtractor = tlsExtractor;
    }

    public Optional<String> extract(byte[] payload, int offset, int length) {
        if (!isInitial(payload, offset, length)) return Optional.empty();
        for (int i = offset + 5; i + 50 < offset + length; i++) {
            if (Byte.toUnsignedInt(payload[i]) == 1) {
                Optional<String> serverName = tlsExtractor.extract(payload, i - 5, offset + length - i + 5);
                if (serverName.isPresent()) return serverName;
            }
        }
        return Optional.empty();
    }

    public boolean isInitial(byte[] payload, int offset, int length) {
        return length >= 5 && (Byte.toUnsignedInt(payload[offset]) & 0x80) != 0;
    }
}
