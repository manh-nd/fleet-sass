package com.fleet.application.rule.usecase;

import java.util.List;

import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.vo.EventPayload;

public interface EvaluateRulesUseCase {
    List<NotificationRule> evaluate(TenantId tenantId, String eventType, EventPayload payload);
}
