package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.dto.BlockReason;
import com.packetinspector.dpi.model.dto.RuleStats;
import com.packetinspector.dpi.model.entity.AppType;
import com.packetinspector.dpi.model.entity.BlockingRuleEntity;
import com.packetinspector.dpi.repository.BlockingRuleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class RuleManager {

    private final BlockingRuleRepository ruleRepository;

    private final Set<Long> blockedIps = new HashSet<>();
    private final Set<AppType> blockedApps = new HashSet<>();
    private final Set<String> blockedDomains = new HashSet<>();
    private final List<String> domainPatterns = new ArrayList<>();
    private final Set<Integer> blockedPorts = new HashSet<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RuleManager(BlockingRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @PostConstruct
    public void loadFromDatabase() {
        if (ruleRepository == null) return;
        lock.writeLock().lock();
        try {
            for (BlockingRuleEntity rule : ruleRepository.findAll()) {
                switch (rule.getRuleType().toUpperCase(Locale.ROOT)) {
                    case "IP" -> blockedIps.add(parseIpv4(rule.getRuleValue()));
                    case "APP" -> blockedApps.add(AppType.fromDisplayName(rule.getRuleValue()));
                    case "DOMAIN" -> {
                        String dom = rule.getRuleValue().toLowerCase(Locale.ROOT);
                        if (dom.contains("*")) domainPatterns.add(dom);
                        else blockedDomains.add(dom);
                    }
                    case "PORT" -> blockedPorts.add(Integer.parseInt(rule.getRuleValue()));
                }
            }
        } catch (Exception ignored) {
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Transactional
    public void blockIp(String value) {
        long ip = parseIpv4(value);
        lock.writeLock().lock();
        try {
            blockedIps.add(ip);
            if (ruleRepository != null && !ruleRepository.existsByRuleTypeAndRuleValue("IP", value)) {
                ruleRepository.save(new BlockingRuleEntity("IP", value));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Transactional
    public void unblockIp(String value) {
        long ip = parseIpv4(value);
        lock.writeLock().lock();
        try {
            blockedIps.remove(ip);
            if (ruleRepository != null) {
                ruleRepository.deleteByRuleTypeAndRuleValue("IP", value);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Transactional
    public void blockApp(AppType app) {
        lock.writeLock().lock();
        try {
            blockedApps.add(app);
            if (ruleRepository != null && !ruleRepository.existsByRuleTypeAndRuleValue("APP", app.name())) {
                ruleRepository.save(new BlockingRuleEntity("APP", app.name()));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Transactional
    public void unblockApp(AppType app) {
        lock.writeLock().lock();
        try {
            blockedApps.remove(app);
            if (ruleRepository != null) {
                ruleRepository.deleteByRuleTypeAndRuleValue("APP", app.name());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Transactional
    public void blockDomain(String domain) {
        String value = domain.trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) throw new IllegalArgumentException("Domain rule cannot be empty");
        lock.writeLock().lock();
        try {
            if (value.contains("*")) domainPatterns.add(value);
            else blockedDomains.add(value);
            if (ruleRepository != null && !ruleRepository.existsByRuleTypeAndRuleValue("DOMAIN", value)) {
                ruleRepository.save(new BlockingRuleEntity("DOMAIN", value));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Transactional
    public void unblockDomain(String domain) {
        String value = domain.trim().toLowerCase(Locale.ROOT);
        lock.writeLock().lock();
        try {
            if (value.contains("*")) domainPatterns.remove(value);
            else blockedDomains.remove(value);
            if (ruleRepository != null) {
                ruleRepository.deleteByRuleTypeAndRuleValue("DOMAIN", value);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Transactional
    public void blockPort(int port) {
        if (port < 0 || port > 0xffff) throw new IllegalArgumentException("Port must be from 0 to 65535");
        lock.writeLock().lock();
        try {
            blockedPorts.add(port);
            if (ruleRepository != null && !ruleRepository.existsByRuleTypeAndRuleValue("PORT", String.valueOf(port))) {
                ruleRepository.save(new BlockingRuleEntity("PORT", String.valueOf(port)));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Transactional
    public void unblockPort(int port) {
        lock.writeLock().lock();
        try {
            blockedPorts.remove(port);
            if (ruleRepository != null) {
                ruleRepository.deleteByRuleTypeAndRuleValue("PORT", String.valueOf(port));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isIpBlocked(long ip) {
        lock.readLock().lock();
        try {
            return blockedIps.contains(ip);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isAppBlocked(AppType app) {
        lock.readLock().lock();
        try {
            return blockedApps.contains(app);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isDomainBlocked(String domain) {
        if (domain == null || domain.isEmpty()) return false;
        String normalized = domain.toLowerCase(Locale.ROOT);
        lock.readLock().lock();
        try {
            if (blockedDomains.contains(normalized)) return true;
            for (String pattern : domainPatterns) {
                if (domainMatchesPattern(normalized, pattern)) return true;
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isPortBlocked(int port) {
        lock.readLock().lock();
        try {
            return blockedPorts.contains(port);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<BlockReason> shouldBlock(long srcIp, int dstPort, AppType app, String domain) {
        if (isIpBlocked(srcIp)) return Optional.of(new BlockReason("IP", ipToString(srcIp)));
        if (isPortBlocked(dstPort)) return Optional.of(new BlockReason("PORT", String.valueOf(dstPort)));
        if (isAppBlocked(app)) return Optional.of(new BlockReason("APP", app.getDisplayName()));
        if (isDomainBlocked(domain)) return Optional.of(new BlockReason("DOMAIN", domain));
        return Optional.empty();
    }

    public RuleStats getStats() {
        lock.readLock().lock();
        try {
            return new RuleStats(
                    blockedIps.size(),
                    blockedApps.size(),
                    blockedDomains.size() + domainPatterns.size(),
                    blockedPorts.size());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Transactional
    public void clearAll() {
        lock.writeLock().lock();
        try {
            blockedIps.clear();
            blockedApps.clear();
            blockedDomains.clear();
            domainPatterns.clear();
            blockedPorts.clear();
            if (ruleRepository != null) {
                ruleRepository.deleteAll();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static long parseIpv4(String ip) {
        String[] parts = ip.trim().split("\\.");
        if (parts.length != 4) throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
        long result = 0;
        for (int i = 0; i < 4; i++) {
            long octet = Long.parseLong(parts[i]);
            if (octet < 0 || octet > 255) throw new IllegalArgumentException("Invalid octet: " + octet);
            result |= (octet << (i * 8));
        }
        return result;
    }

    public static String ipToString(long ip) {
        return "%d.%d.%d.%d".formatted(
                ip & 0xff, (ip >> 8) & 0xff, (ip >> 16) & 0xff, (ip >> 24) & 0xff);
    }

    private static boolean domainMatchesPattern(String domain, String pattern) {
        if (pattern.startsWith("*.")) {
            String suffix = pattern.substring(2);
            return domain.endsWith(suffix) || domain.equals(suffix);
        }
        return domain.contains(pattern);
    }
}
