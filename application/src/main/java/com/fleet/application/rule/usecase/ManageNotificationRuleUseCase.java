package com.fleet.application.rule.usecase;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;

import com.fleet.domain.rule.vo.RuleId;

/**
 * Inbound port for managing notification rules.
 */
public interface ManageNotificationRuleUseCase {
    void createRule(TenantId tenantId, ServiceId serviceId, String eventType, RuleNode conditionRoot, int cooldownMinutes, boolean isActive);
    void updateRule(RuleId ruleId, TenantId tenantId, ServiceId serviceId, String eventType, RuleNode conditionRoot, int cooldownMinutes, boolean isActive);
    void deleteRule(RuleId ruleId, TenantId tenantId);
    java.util.List<com.fleet.domain.rule.model.NotificationRule> listRules(TenantId tenantId);
}
