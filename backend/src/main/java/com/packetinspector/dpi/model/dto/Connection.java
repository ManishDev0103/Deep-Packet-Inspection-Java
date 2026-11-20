package com.packetinspector.dpi.model.dto;

import com.packetinspector.dpi.model.entity.AppType;
import com.packetinspector.dpi.model.entity.ConnectionState;
import com.packetinspector.dpi.model.entity.PacketAction;

public record Connection(
        FiveTuple tuple,
        ConnectionState state,
        AppType appType,
        String sni,
        long packetsIn,
        long packetsOut,
        long bytesIn,
        long bytesOut,
        long firstSeenNanos,
        long lastSeenNanos,
        PacketAction action,
        boolean synSeen,
        boolean synAckSeen,
        boolean finSeen) {

    public Connection {
        sni = sni == null ? "" : sni;
    }

    public static Connection create(FiveTuple tuple, long nowNanos) {
        return new Connection(tuple, ConnectionState.NEW, AppType.UNKNOWN, "",
                0, 0, 0, 0, nowNanos, nowNanos, PacketAction.FORWARD,
                false, false, false);
    }

    public Connection withTraffic(long bytes, boolean outbound, long nowNanos) {
        return new Connection(tuple, state, appType, sni,
                packetsIn + (outbound ? 0 : 1),
                packetsOut + (outbound ? 1 : 0),
                bytesIn + (outbound ? 0 : bytes),
                bytesOut + (outbound ? bytes : 0),
                firstSeenNanos, nowNanos, action, synSeen, synAckSeen, finSeen);
    }

    public Connection classify(AppType type, String domain) {
        return new Connection(tuple, ConnectionState.CLASSIFIED, type, domain,
                packetsIn, packetsOut, bytesIn, bytesOut, firstSeenNanos, lastSeenNanos,
                action, synSeen, synAckSeen, finSeen);
    }

    public Connection withTcpState(ConnectionState newState, boolean newSynSeen,
                                   boolean newSynAckSeen, boolean newFinSeen) {
        return new Connection(tuple, newState, appType, sni, packetsIn, packetsOut,
                bytesIn, bytesOut, firstSeenNanos, lastSeenNanos, action,
                newSynSeen, newSynAckSeen, newFinSeen);
    }

    public Connection blocked() {
        return new Connection(tuple, ConnectionState.BLOCKED, appType, sni,
                packetsIn, packetsOut, bytesIn, bytesOut, firstSeenNanos, lastSeenNanos,
                PacketAction.DROP, synSeen, synAckSeen, finSeen);
    }
}
