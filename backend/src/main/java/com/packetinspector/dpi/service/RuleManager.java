package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.AppType;
import com.packetinspector.dpi.util.ApplicationClassifier;
import com.packetinspector.dpi.util.PacketParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class RuleManager {
    public enum RuleType { IP, PORT, APP, DOMAIN }

    public record BlockReason(RuleType type, String detail) {}

    public record RuleStats(int blockedIps, int blockedApps, int blockedDomains, int blockedPorts) {}

    private final Set<Long> blockedIps = new HashSet<>();
    private final Set<AppType> blockedApps = new HashSet<>();
    private final Set<String> blockedDomains = new HashSet<>();
    private final List<String> domainPatterns = new ArrayList<>();
    private final Set<Integer> blockedPorts = new HashSet<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void blockIp(String value) {
        lock.writeLock().lock();
        try {
            blockedIps.add(parseIpv4(value));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void unblockIp(String value) {
        lock.writeLock().lock();
        try {
            blockedIps.remove(parseIpv4(value));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void blockApp(AppType app) {
        lock.writeLock().lock();
        try {
            blockedApps.add(app);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void unblockApp(AppType app) {
        lock.writeLock().lock();
        try {
            blockedApps.remove(app);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void blockDomain(String domain) {
        String value = domain.trim();
        if (value.isEmpty()) throw new IllegalArgumentException("Domain rule cannot be empty");
        lock.writeLock().lock();
        try {
            if (value.contains("*")) domainPatterns.add(value);
            else blockedDomains.add(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void unblockDomain(String domain) {
        lock.writeLock().lock();
        try {
            if (domain.contains("*")) domainPatterns.remove(domain);
            else blockedDomains.remove(domain);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void blockPort(int port) {
        if (port < 0 || port > 0xffff) throw new IllegalArgumentException("Port must be from 0 to 65535");
        lock.writeLock().lock();
        try {
            blockedPorts.add(port);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void unblockPort(int port) {
        lock.writeLock().lock();
        try {
            blockedPorts.remove(port);
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
        lock.readLock().lock();
        try {
            if (blockedDomains.contains(domain)) return true;
            String normalizedDomain = domain.toLowerCase(Locale.ROOT);
            return domainPatterns.stream().map(value -> value.toLowerCase(Locale.ROOT))
                    .anyMatch(pattern -> matchesPattern(normalizedDomain, pattern));
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

    public Optional<BlockReason> shouldBlock(long sourceIp, int destinationPort,
                                              AppType app, String domain) {
        lock.readLock().lock();
        try {
            if (blockedIps.contains(sourceIp)) {
                return Optional.of(new BlockReason(RuleType.IP, PacketParser.ipToString(sourceIp)));
            }
            if (blockedPorts.contains(destinationPort)) {
                return Optional.of(new BlockReason(RuleType.PORT, Integer.toString(destinationPort)));
            }
            if (blockedApps.contains(app)) {
                return Optional.of(new BlockReason(RuleType.APP, app.label()));
            }
            if (!domain.isEmpty() && (blockedDomains.contains(domain)
                    || domainPatterns.stream().map(value -> value.toLowerCase(Locale.ROOT))
                    .anyMatch(pattern -> matchesPattern(domain.toLowerCase(Locale.ROOT), pattern)))) {
                return Optional.of(new BlockReason(RuleType.DOMAIN, domain));
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void loadRules(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            loadRules(reader);
        }
    }

    public void loadRules(BufferedReader reader) throws IOException {
        String section = "";
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line;
                continue;
            }
            switch (section) {
                case "[BLOCKED_IPS]" -> blockIp(line);
                case "[BLOCKED_APPS]" -> blockApp(AppType.fromName(line));
                case "[BLOCKED_DOMAINS]" -> blockDomain(line);
                case "[BLOCKED_PORTS]" -> blockPort(parsePort(line));
                default -> throw new IOException("Rule outside a recognized section: " + line);
            }
        }
    }

    public void saveRules(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        lock.readLock().lock();
        try {
            lines.add("[BLOCKED_IPS]");
            blockedIps.stream().map(PacketParser::ipToString).forEach(lines::add);
            lines.add("");
            lines.add("[BLOCKED_APPS]");
            blockedApps.stream().map(AppType::label).forEach(lines::add);
            lines.add("");
            lines.add("[BLOCKED_DOMAINS]");
            lines.addAll(blockedDomains);
            lines.addAll(domainPatterns);
            lines.add("");
            lines.add("[BLOCKED_PORTS]");
            blockedPorts.stream().map(String::valueOf).forEach(lines::add);
        } finally {
            lock.readLock().unlock();
        }
        Files.write(path, lines);
    }

    public void clearAll() {
        lock.writeLock().lock();
        try {
            blockedIps.clear();
            blockedApps.clear();
            blockedDomains.clear();
            domainPatterns.clear();
            blockedPorts.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public RuleStats stats() {
        lock.readLock().lock();
        try {
            return new RuleStats(blockedIps.size(), blockedApps.size(),
                    blockedDomains.size() + domainPatterns.size(), blockedPorts.size());
        } finally {
            lock.readLock().unlock();
        }
    }

    private static boolean matchesPattern(String domain, String pattern) {
        if (!pattern.startsWith("*.")) return false;
        String bareDomain = pattern.substring(2);
        return domain.equals(bareDomain) || domain.endsWith("." + bareDomain);
    }

    private static long parseIpv4(String value) {
        String[] parts = value.split("\\.", -1);
        if (parts.length != 4) throw new IllegalArgumentException("Invalid IPv4 address: " + value);
        long result = 0;
        for (String part : parts) {
            int octet;
            try {
                if (part.isEmpty() || !part.chars().allMatch(Character::isDigit)) {
                    throw new NumberFormatException();
                }
                octet = Integer.parseInt(part);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid IPv4 address: " + value, exception);
            }
            if (octet > 255) throw new IllegalArgumentException("Invalid IPv4 address: " + value);
            result = (result << 8) | octet;
        }
        return result;
    }

    private static int parsePort(String value) throws IOException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid blocked port: " + value, exception);
        }
    }
}
