package com.fleet.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body for updating an existing notification rule.
 */
@Schema(description = "Request body for updating an existing notification rule")
public record UpdateRuleRequest(
        @Schema(description = "The owning tenant's UUID (used for authorization scope)", example = "550e8400-e29b-41d4-a716-446655440000") @NotNull UUID tenantId,
        @Schema(description = "The calling service identifier", example = "billing-service") @NotBlank String serviceId,
        @Schema(description = "The event type this rule reacts to", example = "INVOICE_GENERATED") @NotBlank String eventType,
        @Schema(description = "The updated condition tree in JSON format", 
                example = "{\"type\": \"LOGICAL\", \"operator\": \"AND\", \"children\": [{\"type\": \"CONDITION\", \"field\": \"total_amount\", \"operator\": \">=\", \"value\": 1000}, {\"type\": \"CONDITION\", \"field\": \"currency\", \"operator\": \"==\", \"value\": \"USD\"}]}") @NotNull Object conditions,
        @Schema(description = "Suppression window after triggering (minutes)", example = "60") @Min(0) int cooldownMinutes,
        @Schema(description = "Whether the rule should be active after the update", example = "true") boolean active) {
}
