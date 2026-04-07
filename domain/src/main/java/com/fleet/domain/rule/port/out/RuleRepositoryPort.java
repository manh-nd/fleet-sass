package com.fleet.domain.rule.port.out;

import java.util.List;

import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.vo.RuleId;

/**
 * Outbound port for managing Notification Rule persistence.
 */
public interface RuleRepositoryPort {
    List<NotificationRule> findActiveRules(TenantId tenantId, String eventType);
    /** Returns ALL rules (active and inactive) belonging to a tenant. Used for management UIs. */
    List<NotificationRule> findAllByTenant(TenantId tenantId);
    void save(NotificationRule rule);
    void update(NotificationRule rule);
    void delete(RuleId ruleId, TenantId tenantId);
}
