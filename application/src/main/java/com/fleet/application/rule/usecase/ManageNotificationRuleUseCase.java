package com.fleet.application.rule.usecase;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.vo.RuleId;
import com.fleet.domain.shared.pagination.CursorPage;

/**
 * Inbound port for managing notification rule lifecycle (CRUD).
 */
public interface ManageNotificationRuleUseCase {

    void createRule(TenantId tenantId, ServiceId serviceId, String eventType,
            RuleNode conditionRoot, int cooldownMinutes, boolean isActive);

    void updateRule(RuleId ruleId, TenantId tenantId, ServiceId serviceId, String eventType,
            RuleNode conditionRoot, int cooldownMinutes, boolean isActive);

    void deleteRule(RuleId ruleId, TenantId tenantId);

    /**
     * Lists rules for a tenant with cursor-based pagination.
     *
     * @param tenantId the owning tenant
     * @param cursor   opaque cursor from the previous response, or {@code null} for the first page
     * @param limit    maximum number of rules per page (recommended: 20–100)
     */
    CursorPage<NotificationRule> listRules(TenantId tenantId, String cursor, int limit);
}
