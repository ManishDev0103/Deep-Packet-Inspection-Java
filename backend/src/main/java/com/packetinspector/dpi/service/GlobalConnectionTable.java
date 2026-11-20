package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.dto.Connection;
import com.packetinspector.dpi.model.entity.AppType;

import java.util.*;

public final class GlobalConnectionTable {
    public record GlobalStats(int activeConnections, long connectionsSeen,
                              Map<AppType, Long> applications,
                              Map<String, Long> topDomains) {}

    private final List<ConnectionTracker> trackers;

    public GlobalConnectionTable(List<ConnectionTracker> trackers) {
        this.trackers = List.copyOf(trackers);
    }

    public GlobalStats stats() {
        Map<AppType, Long> applications = new HashMap<>();
        Map<String, Long> domains = new HashMap<>();
        int activeConnections = 0;
        long connectionsSeen = 0;
        for (ConnectionTracker tracker : trackers) {
            ConnectionTracker.TrackerStats trackerStats = tracker.stats();
            activeConnections += trackerStats.activeConnections();
            connectionsSeen += trackerStats.totalConnectionsSeen();
            for (Connection connection : tracker.all()) {
                applications.merge(connection.appType(), 1L, Long::sum);
                if (!connection.sni().isEmpty()) domains.merge(connection.sni(), 1L, Long::sum);
            }
        }
        Map<String, Long> topDomains = domains.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(20)
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        HashMap::putAll);
        return new GlobalStats(activeConnections, connectionsSeen,
                Map.copyOf(applications), Map.copyOf(topDomains));
    }
}
