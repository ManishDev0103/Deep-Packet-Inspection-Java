package com.packetinspector.dpi.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dpi_connections")
public class ConnectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String flowKey;

    @Column(nullable = false, length = 45)
    private String srcIp;

    @Column(nullable = false)
    private int srcPort;

    @Column(nullable = false, length = 45)
    private String dstIp;

    @Column(nullable = false)
    private int dstPort;

    @Column(nullable = false)
    private int protocol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectionState state = ConnectionState.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppType appType = AppType.UNKNOWN;

    @Column(length = 255)
    private String sni;

    private long packetsIn;
    private long packetsOut;
    private long bytesIn;
    private long bytesOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PacketAction action = PacketAction.FORWARD;

    private Instant firstSeen;
    private Instant lastSeen;

    public ConnectionEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFlowKey() { return flowKey; }
    public void setFlowKey(String flowKey) { this.flowKey = flowKey; }
    public String getSrcIp() { return srcIp; }
    public void setSrcIp(String srcIp) { this.srcIp = srcIp; }
    public int getSrcPort() { return srcPort; }
    public void setSrcPort(int srcPort) { this.srcPort = srcPort; }
    public String getDstIp() { return dstIp; }
    public void setDstIp(String dstIp) { this.dstIp = dstIp; }
    public int getDstPort() { return dstPort; }
    public void setDstPort(int dstPort) { this.dstPort = dstPort; }
    public int getProtocol() { return protocol; }
    public void setProtocol(int protocol) { this.protocol = protocol; }
    public ConnectionState getState() { return state; }
    public void setState(ConnectionState state) { this.state = state; }
    public AppType getAppType() { return appType; }
    public void setAppType(AppType appType) { this.appType = appType; }
    public String getSni() { return sni; }
    public void setSni(String sni) { this.sni = sni; }
    public long getPacketsIn() { return packetsIn; }
    public void setPacketsIn(long packetsIn) { this.packetsIn = packetsIn; }
    public long getPacketsOut() { return packetsOut; }
    public void setPacketsOut(long packetsOut) { this.packetsOut = packetsOut; }
    public long getBytesIn() { return bytesIn; }
    public void setBytesIn(long bytesIn) { this.bytesIn = bytesIn; }
    public long getBytesOut() { return bytesOut; }
    public void setBytesOut(long bytesOut) { this.bytesOut = bytesOut; }
    public PacketAction getAction() { return action; }
    public void setAction(PacketAction action) { this.action = action; }
    public Instant getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Instant firstSeen) { this.firstSeen = firstSeen; }
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
}
