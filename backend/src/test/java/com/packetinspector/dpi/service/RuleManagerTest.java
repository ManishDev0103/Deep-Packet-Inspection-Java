package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.AppType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleManagerTest {
    @Test
    void reportsBlockingPrecedenceAndMatchesWildcardDomains() {
        var rules = new RuleManager();
        rules.blockIp("192.0.2.1");
        rules.blockPort(443);
        rules.blockApp(AppType.GOOGLE);
        rules.blockDomain("*.example.com");

        assertEquals(RuleManager.RuleType.IP,
                rules.shouldBlock(0xc0000201L, 443, AppType.GOOGLE, "www.example.com")
                        .orElseThrow().type());
        assertEquals(RuleManager.RuleType.PORT,
                rules.shouldBlock(0, 443, AppType.GOOGLE, "www.example.com")
                        .orElseThrow().type());
        assertEquals(RuleManager.RuleType.APP,
                rules.shouldBlock(0, 80, AppType.GOOGLE, "www.example.com")
                        .orElseThrow().type());
        assertEquals(RuleManager.RuleType.DOMAIN,
                rules.shouldBlock(0, 80, AppType.UNKNOWN, "www.example.com")
                        .orElseThrow().type());
        assertTrue(rules.shouldBlock(0, 80, AppType.UNKNOWN, "unrelated.test").isEmpty());
    }

    @Test
    void evictsOldestConnectionWhenAtCapacity() {
        var tracker = new ConnectionTracker(1);
        var first = new com.packetinspector.dpi.model.FiveTuple(1, 2, 1000, 80, 6);
        var second = new com.packetinspector.dpi.model.FiveTuple(1, 3, 1001, 443, 6);

        tracker.getOrCreate(first);
        tracker.getOrCreate(second);

        assertEquals(1, tracker.activeCount());
        assertEquals(2, tracker.stats().totalConnectionsSeen());
        assertTrue(tracker.get(first) == null);
        assertTrue(tracker.get(second) != null);
    }
}
