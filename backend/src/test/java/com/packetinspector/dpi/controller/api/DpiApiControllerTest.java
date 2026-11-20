package com.packetinspector.dpi.controller.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DpiApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void processPcapReturnsReport() throws Exception {
        mockMvc.perform(multipart("/api/dpi/process")
                        .file(capture())
                        .param("blockedPorts", "53"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPackets").value(1))
                .andExpect(jsonPath("$.droppedPackets").value(1));
    }

    @Test
    void managesRules() throws Exception {
        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"IP\",\"value\":\"10.0.0.1\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"IP\",\"value\":\"10.0.0.1\"}"))
                .andExpect(status().isOk());
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
