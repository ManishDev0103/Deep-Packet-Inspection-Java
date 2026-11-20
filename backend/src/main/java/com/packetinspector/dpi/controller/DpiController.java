package com.packetinspector.dpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packetinspector.dpi.model.ProcessingReport;
import com.packetinspector.dpi.service.DpiEngine;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

@RestController
public class DpiController {
    private final DpiEngine engine;
    private final ObjectMapper objectMapper;

    public DpiController(DpiEngine engine, ObjectMapper objectMapper) {
        this.engine = engine;
        this.objectMapper = objectMapper;
    }

    @PostMapping(path = "/api/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> process(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "blockIp", required = false) List<String> blockIps,
            @RequestParam(name = "blockApp", required = false) List<String> blockApps,
            @RequestParam(name = "blockDomain", required = false) List<String> blockDomains,
            @RequestParam(name = "blockPort", required = false) List<Integer> blockPorts)
            throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Select a non-empty PCAP file.");
        Path input = Files.createTempFile("dpi-upload-", ".pcap");
        Path output = null;
        boolean streamed = false;
        try {
            file.transferTo(input.toFile());
            output = Files.createTempFile("dpi-filtered-", ".pcap");
            ProcessingReport report = engine.process(input, output, orEmpty(blockIps),
                    orEmpty(blockApps), orEmpty(blockDomains), orEmpty(blockPorts));
            String encodedReport = Base64.getEncoder()
                    .encodeToString(objectMapper.writeValueAsBytes(report));
            Path result = output;
            StreamingResponseBody body = response -> {
                try (var stream = Files.newInputStream(result)) {
                    stream.transferTo(response);
                } finally {
                    Files.deleteIfExists(result);
                }
            };
            streamed = true;
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"filtered.pcap\"")
                    .header("X-Processing-Report", encodedReport)
                    .body(body);
        } finally {
            Files.deleteIfExists(input);
            if (!streamed && output != null) Files.deleteIfExists(output);
        }
    }

    private static <T> List<T> orEmpty(List<T> values) {
        return values == null ? List.of() : values;
    }
}
