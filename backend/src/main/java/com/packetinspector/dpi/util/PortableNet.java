package com.packetinspector.dpi.util;

import java.nio.ByteOrder;

public final class PortableNet {
    private PortableNet() {}

    public static int swapBytes16(int value) {
        return ((value & 0xFF00) >> 8) | ((value & 0x00FF) << 8);
    }

    public static long swapBytes32(long value) {
        return ((value & 0xFF000000L) >>> 24)
                | ((value & 0x00FF0000L) >>> 8)
                | ((value & 0x0000FF00L) << 8)
                | ((value & 0x000000FFL) << 24);
    }

    public static boolean isLittleEndian() {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    }

    public static int netToHost16(int netValue) {
        int value = netValue & 0xFFFF;
        return isLittleEndian() ? swapBytes16(value) : value;
    }

    public static long netToHost32(long netValue) {
        long value = netValue & 0xFFFFFFFFL;
        return isLittleEndian() ? swapBytes32(value) : value;
    }

    public static int hostToNet16(int hostValue) {
        return netToHost16(hostValue);
    }

    public static long hostToNet32(long hostValue) {
        return netToHost32(hostValue);
    }
}
