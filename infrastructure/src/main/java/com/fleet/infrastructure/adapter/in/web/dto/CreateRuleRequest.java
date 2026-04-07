package com.fleet.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body for creating a new notification rule.
 *
 * @param tenantId        the owning tenant's UUID
 * @param serviceId       the service identifier that owns this rule
 * @param eventType       the event type this rule reacts to
 * @param conditions      the condition tree in JSON format
 * @param cooldownMinutes suppression window after triggering (must be >= 0)
 * @param active          whether the rule is initially active
 */
public record CreateRuleRequest(
        @NotNull UUID tenantId,
        @NotBlank String serviceId,
        @NotBlank String eventType,
        @NotNull Object conditions,
        @Min(0) int cooldownMinutes,
        boolean active
) {}
