package com.packetinspector.dpi.model;

public record PacketJob(
        long packetId,
        FiveTuple tuple,
        byte[] data,
        int ethernetOffset,
        int ipOffset,
        int transportOffset,
        int payloadOffset,
        int payloadLength,
        int tcpFlags,
        long timestampSeconds,
        long timestampFraction) {

    private static final long END_OF_STREAM_ID = -1;

    public static PacketJob stopSignal() {
        return new PacketJob(END_OF_STREAM_ID, null, null, 0, 0, 0,
                0, 0, 0, 0, 0);
    }

    public boolean endOfStream() {
        return packetId == END_OF_STREAM_ID;
    }
}
