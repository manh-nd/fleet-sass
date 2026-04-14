package com.fleet.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body for creating a new notification rule.
 */
@Schema(description = "Request body for creating a new notification rule")
public record CreateRuleRequest(
        @Schema(description = "The owning tenant's UUID", example = "550e8400-e29b-41d4-a716-446655440000") @NotNull UUID tenantId,
        @Schema(description = "The service identifier that owns this rule", example = "billing-service") @NotBlank String serviceId,
        @Schema(description = "The event type this rule reacts to", example = "INVOICE_GENERATED") @NotBlank String eventType,
        @Schema(description = "The condition tree in JSON format", 
                example = "{\"type\": \"LOGICAL\", \"operator\": \"AND\", \"children\": [{\"type\": \"CONDITION\", \"field\": \"total_amount\", \"operator\": \"gte\", \"value\": 1000}, {\"type\": \"CONDITION\", \"field\": \"currency\", \"operator\": \"eq\", \"value\": \"USD\"}]}") @NotNull Object conditions,
        @Schema(description = "Suppression window after triggering (minutes)", example = "60") @Min(0) int cooldownMinutes,
        @Schema(description = "Whether the rule is initially active", example = "true") boolean active) {
}
