package com.fleet.domain.rule.port.out;

import java.util.List;

import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.model.NotificationRule;

public interface RuleRepositoryPort {
    List<NotificationRule> findActiveRules(TenantId tenantId, String eventType);
    void save(NotificationRule rule);
}
