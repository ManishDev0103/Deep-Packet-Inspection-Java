package com.packetinspector.dpi.controller.api;

import com.packetinspector.dpi.exception.InvalidPcapException;
import com.packetinspector.dpi.model.dto.ProcessingReport;
import com.packetinspector.dpi.model.dto.RuleRequest;
import com.packetinspector.dpi.model.dto.RuleStats;
import com.packetinspector.dpi.model.entity.AppType;
import com.packetinspector.dpi.model.entity.ConnectionEntity;
import com.packetinspector.dpi.repository.ConnectionRepository;
import com.packetinspector.dpi.service.DpiEngine;
import com.packetinspector.dpi.service.RuleManager;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DpiApiController {

    private final DpiEngine dpiEngine;
    private final RuleManager ruleManager;
    private final ConnectionRepository connectionRepository;

    public DpiApiController(DpiEngine dpiEngine, RuleManager ruleManager,
                            ConnectionRepository connectionRepository) {
        this.dpiEngine = dpiEngine;
        this.ruleManager = ruleManager;
        this.connectionRepository = connectionRepository;
    }

    @PostMapping(value = "/dpi/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProcessingReport> processPcap(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "blockedIps", required = false) List<String> blockedIps,
            @RequestParam(value = "blockedApps", required = false) List<String> blockedApps,
            @RequestParam(value = "blockedDomains", required = false) List<String> blockedDomains,
            @RequestParam(value = "blockedPorts", required = false) List<Integer> blockedPorts
    ) throws IOException {
        if (file == null || file.isEmpty()) throw new InvalidPcapException("Uploaded file cannot be empty");

        Path inputTemp = Files.createTempFile("dpi-input-", ".pcap");
        Path outputTemp = Files.createTempFile("dpi-output-", ".pcap");
        try {
            Files.write(inputTemp, file.getBytes());
            ProcessingReport report = dpiEngine.process(
                    inputTemp, outputTemp, blockedIps, blockedApps, blockedDomains, blockedPorts);
            return ResponseEntity.ok(report);
        } finally {
            Files.deleteIfExists(inputTemp);
            Files.deleteIfExists(outputTemp);
        }
    }

    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> getRules() {
        RuleStats stats = ruleManager.getStats();
        return ResponseEntity.ok(Map.of("stats", stats));
    }

    @PostMapping("/rules")
    public ResponseEntity<RuleStats> addRule(@Valid @RequestBody RuleRequest request) {
        String type = request.type().trim().toUpperCase(Locale.ROOT);
        String value = request.value().trim();
        switch (type) {
            case "IP" -> ruleManager.blockIp(value);
            case "APP" -> ruleManager.blockApp(AppType.fromDisplayName(value));
            case "DOMAIN" -> ruleManager.blockDomain(value);
            case "PORT" -> ruleManager.blockPort(Integer.parseInt(value));
            default -> throw new IllegalArgumentException("Unknown rule type: " + type);
        }
        return ResponseEntity.ok(ruleManager.getStats());
    }

    @DeleteMapping("/rules")
    public ResponseEntity<RuleStats> deleteRule(@Valid @RequestBody RuleRequest request) {
        String type = request.type().trim().toUpperCase(Locale.ROOT);
        String value = request.value().trim();
        switch (type) {
            case "IP" -> ruleManager.unblockIp(value);
            case "APP" -> ruleManager.unblockApp(AppType.fromDisplayName(value));
            case "DOMAIN" -> ruleManager.unblockDomain(value);
            case "PORT" -> ruleManager.unblockPort(Integer.parseInt(value));
            default -> throw new IllegalArgumentException("Unknown rule type: " + type);
        }
        return ResponseEntity.ok(ruleManager.getStats());
    }

    @GetMapping("/connections")
    public ResponseEntity<List<ConnectionEntity>> getConnections() {
        return ResponseEntity.ok(connectionRepository.findTop100ByOrderByIdDesc());
    }
}
