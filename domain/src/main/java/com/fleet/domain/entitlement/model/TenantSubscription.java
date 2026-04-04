package com.fleet.domain.entitlement.model;

import java.time.Instant;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

import lombok.Getter;

@Getter
public class TenantSubscription {
    private final TenantId tenantId;
    private final ServiceId serviceId;
    private SubscriptionStatus status;
    private Instant validUntil;

    public enum SubscriptionStatus {
        ACTIVE, EXPIRED, SUSPENDED
    }

    public TenantSubscription(
            TenantId tenantId,
            ServiceId serviceId,
            SubscriptionStatus status,
            Instant validUntil) {
        this.tenantId = tenantId;
        this.serviceId = serviceId;
        this.status = status;
        this.validUntil = validUntil;
    }

    public boolean isValidAt(Instant time) {
        return status == SubscriptionStatus.ACTIVE && time.isBefore(validUntil);
    }
}
