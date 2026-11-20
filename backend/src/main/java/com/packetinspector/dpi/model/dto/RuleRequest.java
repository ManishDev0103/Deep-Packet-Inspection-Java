package com.packetinspector.dpi.model.dto;

import jakarta.validation.constraints.NotBlank;

public record RuleRequest(
        @NotBlank(message = "Rule type is required (IP, APP, DOMAIN, PORT)")
        String type,

        @NotBlank(message = "Rule value is required")
        String value
) {}
