package com.fleet.domain.rule.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;

public class NotificationRule {
    private final RuleId id;
    private final TenantId tenantId;
    private final ServiceId serviceId;
    private final String eventType;
    private final RuleNode conditionRoot;
    private final boolean isActive;
    private final int cooldownMinutes;

    public NotificationRule(RuleId id, TenantId tenantId, ServiceId serviceId,
            String eventType, RuleNode conditionRoot, boolean isActive, int cooldownMinutes) {
        this.id = id;
        this.tenantId = tenantId;
        this.serviceId = serviceId;
        this.eventType = eventType;
        this.conditionRoot = conditionRoot;
        this.isActive = isActive;
        this.cooldownMinutes = cooldownMinutes;
    }

    // Business Behavior
    public boolean isSatisfiedBy(EventPayload payload) {
        if (!isActive || conditionRoot == null)
            return false;
        return conditionRoot.evaluate(payload);
    }

    // Getters...
    public RuleId getId() {
        return id;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getCooldownMinutes() {
        return cooldownMinutes;
    }
}