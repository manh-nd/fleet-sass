package com.fleet.infrastructure.adapter.in.web.dto;

import com.fleet.domain.rule.model.NotificationRule;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * REST response DTO for a {@link NotificationRule}.
 * Serializes the AST condition tree as a raw JSON node for the Angular client.
 */
@Schema(description = "Response body for a notification rule")
public record NotificationRuleResponse(
        @Schema(description = "Unique ID of the rule", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
        @Schema(description = "Owning tenant UUID", example = "550e8400-e29b-41d4-a716-446655440000") UUID tenantId,
        @Schema(description = "The service identifier that owns this rule", example = "billing-service") String serviceId,
        @Schema(description = "The event type this rule reacts to", example = "INVOICE_GENERATED") String eventType,
        @Schema(description = "The condition tree in JSON format", 
                example = "{\"type\": \"AND\", \"children\": [{\"type\": \"equals\", \"field\": \"total_amount\", \"value\": 1000}, {\"type\": \"equals\", \"field\": \"currency\", \"value\": \"USD\"}]}") Object conditions,
        @Schema(description = "Suppression window after triggering (minutes)", example = "60") int cooldownMinutes,
        @Schema(description = "Whether the rule is currently active", example = "true") boolean active) {
    /**
     * Maps a domain {@link NotificationRule} to this response DTO.
     * The condition AST is serialized back to a JSON structure that mirrors
     * the {@code CreateRuleRequest#conditions} format.
     */
    public static NotificationRuleResponse from(NotificationRule rule,
            com.fleet.infrastructure.adapter.out.db.RuleAstParser parser, ObjectMapper objectMapper) {
        try {
            String conditionsJson = parser.serialize(rule.getConditionRoot());
            Object conditions = objectMapper.readValue(conditionsJson, Object.class);
            return new NotificationRuleResponse(
                    rule.getId().value(),
                    rule.getTenantId().value(),
                    rule.getServiceId().value(),
                    rule.getEventType(),
                    conditions,
                    rule.getCooldownMinutes(),
                    rule.isActive());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize rule conditions to JSON", e);
        }
    }
}
