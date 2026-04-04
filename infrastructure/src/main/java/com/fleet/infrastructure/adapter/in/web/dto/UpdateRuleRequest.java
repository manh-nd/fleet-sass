package com.fleet.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public record UpdateRuleRequest(
    UUID tenantId,
    String serviceId,
    String eventType,
    Object conditions, 
    int cooldownMinutes,
    boolean active
) {
}
