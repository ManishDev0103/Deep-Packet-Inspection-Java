package com.packetinspector.dpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "dpi.auth.username=test-user",
        "dpi.auth.password=test-password"
})
class DpiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requiresBasicAuthentication() throws Exception {
        mockMvc.perform(multipart("/api/process").file(capture()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsFilteredCaptureAndProcessingReport() throws Exception {
        var response = mockMvc.perform(multipart("/api/process")
                        .file(capture())
                        .param("blockPort", "53")
                        .with(httpBasic("test-user", "test-password")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"filtered.pcap\""))
                .andReturn().getResponse();

        var reportBytes = Base64.getDecoder()
                .decode(response.getHeader("X-Processing-Report"));
        var report = objectMapper.readTree(reportBytes);
        assertEquals(1, report.path("totalPackets").asInt());
        assertEquals(0, report.path("forwardedPackets").asInt());
        assertEquals(1, report.path("droppedPackets").asInt());
    }

    private static MockMultipartFile capture() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ByteBuffer globalHeader = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
        globalHeader.putInt(0xa1b2c3d4).putShort((short) 2).putShort((short) 4)
                .putInt(0).putInt(0).putInt(65_535).putInt(1);
        bytes.write(globalHeader.array());
        byte[] packet = new byte[42];
        packet[12] = 8;
        packet[13] = 0;
        packet[14] = 0x45;
        packet[17] = 28;
        packet[22] = 64;
        packet[23] = 17;
        packet[26] = (byte) 192;
        packet[28] = 2;
        packet[29] = 1;
        packet[30] = (byte) 198;
        packet[31] = 51;
        packet[32] = 100;
        packet[33] = 2;
        packet[34] = 0x30;
        packet[35] = 0x39;
        packet[37] = 53;
        packet[39] = 8;
        ByteBuffer packetHeader = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        packetHeader.putInt(1).putInt(0).putInt(packet.length).putInt(packet.length);
        bytes.write(packetHeader.array());
        bytes.write(packet);
        return new MockMultipartFile("file", "capture.pcap",
                "application/vnd.tcpdump.pcap", bytes.toByteArray());
    }
}
