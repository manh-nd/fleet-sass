package com.fleet.domain.rule.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The NotificationRule aggregate is the core of the rule engine.
 * It contains the condition tree (AST) to be evaluated against incoming events.
 */
@Getter
@AllArgsConstructor
public class NotificationRule {
    private final RuleId id;
    private final TenantId tenantId;
    private final ServiceId serviceId;
    private final String eventType;
    private final RuleNode conditionRoot;
    private final boolean isActive;
    private final int cooldownMinutes;

    // Business Behavior
    public boolean isSatisfiedBy(EventPayload payload) {
        if (!isActive || conditionRoot == null)
            return false;
        return conditionRoot.evaluate(payload);
    }
}