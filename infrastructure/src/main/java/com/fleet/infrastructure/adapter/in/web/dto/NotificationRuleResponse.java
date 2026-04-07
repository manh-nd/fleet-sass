package com.fleet.infrastructure.adapter.in.web.dto;

import com.fleet.domain.rule.model.NotificationRule;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * REST response DTO for a {@link NotificationRule}.
 * Serializes the AST condition tree as a raw JSON node for the Angular client.
 */
public record NotificationRuleResponse(
    UUID id,
    UUID tenantId,
    String serviceId,
    String eventType,
    Object conditions,
    int cooldownMinutes,
    boolean active
) {
    /**
     * Maps a domain {@link NotificationRule} to this response DTO.
     * The condition AST is serialized back to a JSON structure that mirrors
     * the {@code CreateRuleRequest#conditions} format.
     */
    public static NotificationRuleResponse from(NotificationRule rule, com.fleet.infrastructure.adapter.out.db.RuleAstParser parser, ObjectMapper objectMapper) {
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
                rule.isActive()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize rule conditions to JSON", e);
        }
    }
}
