package com.packetinspector.dpi.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dpi_blocking_rules")
public class BlockingRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String ruleType; // IP, APP, DOMAIN, PORT

    @Column(nullable = false, length = 255)
    private String ruleValue;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public BlockingRuleEntity() {}

    public BlockingRuleEntity(String ruleType, String ruleValue) {
        this.ruleType = ruleType;
        this.ruleValue = ruleValue;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getRuleValue() { return ruleValue; }
    public void setRuleValue(String ruleValue) { this.ruleValue = ruleValue; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
