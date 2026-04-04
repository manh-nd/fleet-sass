package com.fleet.application.rule.usecase;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;

public interface ManageNotificationRuleUseCase {
    void createRule(TenantId tenantId, ServiceId serviceId, String eventType, RuleNode conditionRoot, int cooldownMinutes, boolean isActive);
}
