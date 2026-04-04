package com.fleet.domain.entitlement.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantSubscriptionTest {

    @Test
    void shouldBeValidWhenActiveAndBeforeExpiration() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("SERVICE-1");
        Instant now = Instant.now();
        Instant validUntil = now.plus(1, ChronoUnit.DAYS);
        
        TenantSubscription subscription = new TenantSubscription(tenantId, serviceId, TenantSubscription.SubscriptionStatus.ACTIVE, validUntil);
        
        assertTrue(subscription.isValidAt(now));
    }

    @Test
    void shouldBeInvalidWhenExpired() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("SERVICE-1");
        Instant now = Instant.now();
        Instant validUntil = now.minus(1, ChronoUnit.DAYS);
        
        TenantSubscription subscription = new TenantSubscription(tenantId, serviceId, TenantSubscription.SubscriptionStatus.ACTIVE, validUntil);
        
        assertFalse(subscription.isValidAt(now));
    }

    @Test
    void shouldBeInvalidWhenStatusIsSuspended() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("SERVICE-1");
        Instant now = Instant.now();
        Instant validUntil = now.plus(1, ChronoUnit.DAYS);
        
        TenantSubscription subscription = new TenantSubscription(tenantId, serviceId, TenantSubscription.SubscriptionStatus.SUSPENDED, validUntil);
        
        assertFalse(subscription.isValidAt(now));
    }

    @Test
    void shouldBeInvalidWhenStatusIsExpired() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("SERVICE-1");
        Instant now = Instant.now();
        Instant validUntil = now.plus(1, ChronoUnit.DAYS);
        
        TenantSubscription subscription = new TenantSubscription(tenantId, serviceId, TenantSubscription.SubscriptionStatus.EXPIRED, validUntil);
        
        assertFalse(subscription.isValidAt(now));
    }
}
