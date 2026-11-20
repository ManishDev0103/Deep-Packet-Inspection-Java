package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.AppType;
import com.packetinspector.dpi.model.Connection;
import com.packetinspector.dpi.model.ConnectionState;
import com.packetinspector.dpi.model.FiveTuple;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConnectionTracker {
    public record TrackerStats(int activeConnections, long totalConnectionsSeen,
                               long classifiedConnections, long blockedConnections) {}

    private final int maximumConnections;
    private final Map<FiveTuple, Connection> connections = new HashMap<>();
    private long totalSeen;
    private long classifiedCount;
    private long blockedCount;

    public ConnectionTracker(int maximumConnections) {
        if (maximumConnections < 1) throw new IllegalArgumentException("Connection limit must be positive");
        this.maximumConnections = maximumConnections;
    }

    public Connection getOrCreate(FiveTuple tuple) {
        Connection connection = connections.get(tuple);
        if (connection != null) return connection;
        if (connections.size() >= maximumConnections) evictOldest();
        totalSeen++;
        connection = Connection.create(tuple, System.nanoTime());
        connections.put(tuple, connection);
        return connection;
    }

    public Connection get(FiveTuple tuple) {
        Connection connection = connections.get(tuple);
        return connection != null ? connection : connections.get(tuple.reverse());
    }

    public Connection updateTraffic(Connection connection, long bytes, boolean outbound) {
        Connection updated = connection.withTraffic(bytes, outbound, System.nanoTime());
        connections.put(connection.tuple(), updated);
        return updated;
    }

    public Connection classify(Connection connection, AppType app, String sni) {
        if (connection.state() == ConnectionState.CLASSIFIED) return connection;
        Connection classified = connection.classify(app, sni);
        connections.put(connection.tuple(), classified);
        classifiedCount++;
        return classified;
    }

    public Connection block(Connection connection) {
        Connection blocked = connection.blocked();
        connections.put(connection.tuple(), blocked);
        blockedCount++;
        return blocked;
    }

    public Connection update(Connection connection) {
        connections.put(connection.tuple(), connection);
        return connection;
    }

    public void close(FiveTuple tuple) {
        Connection connection = connections.get(tuple);
        if (connection != null) {
            connections.put(tuple, connection.withTcpState(ConnectionState.CLOSED,
                    connection.synSeen(), connection.synAckSeen(), connection.finSeen()));
        }
    }

    public int cleanupStale(Duration timeout) {
        long cutoff = System.nanoTime() - timeout.toNanos();
        int previousSize = connections.size();
        connections.values().removeIf(connection -> connection.lastSeenNanos() < cutoff
                || connection.state() == ConnectionState.CLOSED);
        return previousSize - connections.size();
    }

    public List<Connection> all() {
        return new ArrayList<>(connections.values());
    }

    public int activeCount() {
        return connections.size();
    }

    public TrackerStats stats() {
        return new TrackerStats(connections.size(), totalSeen, classifiedCount, blockedCount);
    }

    public void clear() {
        connections.clear();
    }

    private void evictOldest() {
        connections.values().stream()
                .min(Comparator.comparingLong(Connection::lastSeenNanos))
                .map(Connection::tuple)
                .ifPresent(connections::remove);
    }
}
