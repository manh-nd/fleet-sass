package com.fleet.application.rule.usecase;

import java.util.List;

import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.vo.EventPayload;

/**
 * Inbound port for evaluating active rules for a given event.
 */
public interface EvaluateRulesUseCase {
    List<NotificationRule> evaluate(TenantId tenantId, String eventType, EventPayload payload);
}
