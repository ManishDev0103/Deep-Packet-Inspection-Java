package com.packetinspector.dpi.model.entity;

public final class Protocol {
    public static final int ICMP = 1;
    public static final int TCP = 6;
    public static final int UDP = 17;

    private Protocol() {}

    public static String toString(int protocol) {
        return switch (protocol) {
            case TCP -> "TCP";
            case UDP -> "UDP";
            case ICMP -> "ICMP";
            default -> "Unknown(" + protocol + ")";
        };
    }
}
