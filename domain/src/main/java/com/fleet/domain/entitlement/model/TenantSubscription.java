package com.fleet.domain.entitlement.model;

import java.time.Instant;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

/**
 * Represents a tenant's subscription to a specific service.
 *
 * <p>This class is intentionally immutable. Domain state transitions
 * (suspend, renew) return new instances rather than mutating in place.</p>
 */
public class TenantSubscription {

    private final TenantId tenantId;
    private final ServiceId serviceId;
    private final SubscriptionStatus status;
    private final Instant validUntil;

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

    // --- Getters ---

    public TenantId getTenantId() {
        return tenantId;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    // --- Business Behavior ---

    /**
     * Returns {@code true} if this subscription is active at the given instant.
     */
    public boolean isValidAt(Instant time) {
        return status == SubscriptionStatus.ACTIVE && time.isBefore(validUntil);
    }

    /**
     * Returns a new {@link TenantSubscription} with status SUSPENDED.
     * Does not mutate the current instance.
     */
    public TenantSubscription suspend() {
        return new TenantSubscription(tenantId, serviceId, SubscriptionStatus.SUSPENDED, validUntil);
    }

    /**
     * Returns a new {@link TenantSubscription} with status ACTIVE and the given expiry.
     * Does not mutate the current instance.
     *
     * @param newValidUntil the new expiry instant
     */
    public TenantSubscription renewUntil(Instant newValidUntil) {
        if (newValidUntil == null) {
            throw new IllegalArgumentException("renewUntil instant must not be null");
        }
        return new TenantSubscription(tenantId, serviceId, SubscriptionStatus.ACTIVE, newValidUntil);
    }
}
