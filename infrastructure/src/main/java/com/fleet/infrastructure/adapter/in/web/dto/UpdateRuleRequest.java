package com.fleet.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body for updating an existing notification rule.
 *
 * @param tenantId        the owning tenant's UUID (used for authorization scope)
 * @param serviceId       the service identifier
 * @param eventType       the event type this rule reacts to
 * @param conditions      the updated condition tree in JSON format
 * @param cooldownMinutes suppression window after triggering (must be >= 0)
 * @param active          whether the rule should be active after the update
 */
public record UpdateRuleRequest(
        @NotNull UUID tenantId,
        @NotBlank String serviceId,
        @NotBlank String eventType,
        @NotNull Object conditions,
        @Min(0) int cooldownMinutes,
        boolean active
) {}
