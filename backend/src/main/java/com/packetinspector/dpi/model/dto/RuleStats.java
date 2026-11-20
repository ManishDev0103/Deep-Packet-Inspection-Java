package com.packetinspector.dpi.model.dto;

public record RuleStats(
        long blockedIps,
        long blockedApps,
        long blockedDomains,
        long blockedPorts
) {}
