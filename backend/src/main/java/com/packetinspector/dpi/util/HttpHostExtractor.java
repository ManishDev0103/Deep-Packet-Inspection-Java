package com.packetinspector.dpi.util;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class HttpHostExtractor {
    private static final String[] METHODS = {"GET ", "POST", "PUT ", "HEAD", "DELE", "PATC", "OPTI"};

    public Optional<String> extract(byte[] payload, int offset, int length) {
        if (!isHttpRequest(payload, offset, length)) return Optional.empty();
        int end = offset + length;
        for (int lineStart = offset; lineStart < end; ) {
            int lineEnd = lineStart;
            while (lineEnd < end && payload[lineEnd] != '\n') lineEnd++;
            int contentEnd = lineEnd > lineStart && payload[lineEnd - 1] == '\r' ? lineEnd - 1 : lineEnd;
            int colon = lineStart;
            while (colon < contentEnd && payload[colon] != ':') colon++;
            if (colon < contentEnd && asciiEqualsIgnoreCase(payload, lineStart, colon - lineStart, "host")) {
                int valueStart = colon + 1;
                while (valueStart < contentEnd
                        && (payload[valueStart] == ' ' || payload[valueStart] == '\t')) valueStart++;
                String host = new String(payload, valueStart, contentEnd - valueStart,
                        StandardCharsets.ISO_8859_1).trim();
                int separator = host.lastIndexOf(':');
                if (separator > 0 && host.indexOf(':') == separator) host = host.substring(0, separator);
                return host.isEmpty() ? Optional.empty() : Optional.of(host);
            }
            lineStart = lineEnd + 1;
        }
        return Optional.empty();
    }

    public boolean isHttpRequest(byte[] payload, int offset, int length) {
        for (String method : METHODS) {
            if (length >= method.length() && asciiEquals(payload, offset, method)) return true;
        }
        return false;
    }

    private static boolean asciiEquals(byte[] data, int offset, String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Byte.toUnsignedInt(data[offset + i]) != value.charAt(i)) return false;
        }
        return true;
    }

    private static boolean asciiEqualsIgnoreCase(byte[] data, int offset, int length, String value) {
        if (length != value.length()) return false;
        for (int i = 0; i < length; i++) {
            if (Character.toLowerCase((char) Byte.toUnsignedInt(data[offset + i])) != value.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
