package com.packetinspector.dpi.model;

import com.packetinspector.dpi.model.dto.FiveTuple;
import com.packetinspector.dpi.model.entity.AppType;
import com.packetinspector.dpi.util.PortableNet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreTypesTest {
    @Test
    void formatsAndReversesFiveTupleLikeCpp() {
        var tuple = new FiveTuple(
                FiveTuple.parseIp("192.0.2.1"),
                FiveTuple.parseIp("198.51.100.2"),
                12345,
                443,
                6);

        assertEquals("192.0.2.1:12345 -> 198.51.100.2:443 (TCP)", tuple.toString());
        assertEquals(
                new FiveTuple(tuple.dstIp(), tuple.srcIp(), tuple.dstPort(), tuple.srcPort(), 6),
                tuple.reverse());
    }

    @Test
    void matchesCppSniClassificationFallback() {
        assertEquals(AppType.YOUTUBE, AppType.fromSni("www.youtube.com"));
        assertEquals(AppType.GITHUB, AppType.fromSni("github.com"));
        assertEquals(AppType.HTTPS, AppType.fromSni("unrecognized.example"));
        assertEquals(AppType.UNKNOWN, AppType.fromSni(""));
    }

    @Test
    void swaps16And32BitNetworkValues() {
        assertEquals(0x3412, PortableNet.swapBytes16(0x1234));
        assertEquals(0x78563412L, PortableNet.swapBytes32(0x12345678L));
    }
}
