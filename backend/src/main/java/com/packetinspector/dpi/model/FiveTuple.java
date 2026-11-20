package com.packetinspector.dpi.model;

public record FiveTuple(long sourceIp, long destinationIp, int sourcePort,
                        int destinationPort, int protocol) {
    public FiveTuple {
        if (sourceIp < 0 || sourceIp > 0xffff_ffffL
                || destinationIp < 0 || destinationIp > 0xffff_ffffL) {
            throw new IllegalArgumentException("IPv4 addresses must be unsigned 32-bit values");
        }
        if (sourcePort < 0 || sourcePort > 0xffff
                || destinationPort < 0 || destinationPort > 0xffff) {
            throw new IllegalArgumentException("Ports must be unsigned 16-bit values");
        }
        if (protocol < 0 || protocol > 0xff) {
            throw new IllegalArgumentException("Protocol must be an unsigned 8-bit value");
        }
    }

    public FiveTuple reverse() {
        return new FiveTuple(destinationIp, sourceIp, destinationPort, sourcePort, protocol);
    }
}
