package com.packetinspector.dpi.model.entity;

public final class TcpFlags {
    public static final int FIN = 0x01;
    public static final int SYN = 0x02;
    public static final int RST = 0x04;
    public static final int PSH = 0x08;
    public static final int ACK = 0x10;
    public static final int URG = 0x20;

    private TcpFlags() {}

    public static String toString(int flags) {
        var sb = new StringBuilder();
        if ((flags & SYN) != 0) sb.append("SYN ");
        if ((flags & ACK) != 0) sb.append("ACK ");
        if ((flags & FIN) != 0) sb.append("FIN ");
        if ((flags & RST) != 0) sb.append("RST ");
        if ((flags & PSH) != 0) sb.append("PSH ");
        if ((flags & URG) != 0) sb.append("URG ");
        return sb.toString().trim();
    }
}
