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
        Instant now = Instant.now();
        TenantSubscription sub = buildActive(now.plus(1, ChronoUnit.DAYS));

        assertTrue(sub.isValidAt(now));
    }

    @Test
    void shouldBeInvalidWhenExpired() {
        Instant now = Instant.now();
        TenantSubscription sub = buildActive(now.minus(1, ChronoUnit.DAYS));

        assertFalse(sub.isValidAt(now));
    }

    @Test
    void shouldBeInvalidWhenStatusIsSuspended() {
        Instant now = Instant.now();
        TenantSubscription sub = new TenantSubscription(
                new TenantId(UUID.randomUUID()), new ServiceId("S1"),
                TenantSubscription.SubscriptionStatus.SUSPENDED,
                now.plus(1, ChronoUnit.DAYS));

        assertFalse(sub.isValidAt(now));
    }

    @Test
    void shouldBeInvalidWhenStatusIsExpiredEnum() {
        Instant now = Instant.now();
        TenantSubscription sub = new TenantSubscription(
                new TenantId(UUID.randomUUID()), new ServiceId("S1"),
                TenantSubscription.SubscriptionStatus.EXPIRED,
                now.plus(1, ChronoUnit.DAYS));

        assertFalse(sub.isValidAt(now));
    }

    @Test
    void suspendShouldReturnNewInstanceWithSuspendedStatus() {
        Instant now = Instant.now();
        TenantSubscription original = buildActive(now.plus(1, ChronoUnit.DAYS));

        TenantSubscription suspended = original.suspend();

        // Original must not be mutated
        assertEquals(TenantSubscription.SubscriptionStatus.ACTIVE, original.getStatus());
        // New instance must be SUSPENDED
        assertEquals(TenantSubscription.SubscriptionStatus.SUSPENDED, suspended.getStatus());
        assertEquals(original.getTenantId(), suspended.getTenantId());
        assertEquals(original.getServiceId(), suspended.getServiceId());
        assertEquals(original.getValidUntil(), suspended.getValidUntil());
        assertFalse(suspended.isValidAt(now));
    }

    @Test
    void renewUntilShouldReturnNewInstanceWithActiveStatus() {
        Instant now = Instant.now();
        TenantSubscription expired = new TenantSubscription(
                new TenantId(UUID.randomUUID()), new ServiceId("S1"),
                TenantSubscription.SubscriptionStatus.EXPIRED,
                now.minus(1, ChronoUnit.DAYS));

        Instant newExpiry = now.plus(30, ChronoUnit.DAYS);
        TenantSubscription renewed = expired.renewUntil(newExpiry);

        // Original must not be mutated
        assertEquals(TenantSubscription.SubscriptionStatus.EXPIRED, expired.getStatus());
        // New instance must be active with new expiry
        assertEquals(TenantSubscription.SubscriptionStatus.ACTIVE, renewed.getStatus());
        assertEquals(newExpiry, renewed.getValidUntil());
        assertTrue(renewed.isValidAt(now));
    }

    @Test
    void renewUntilShouldThrowWhenExpiryIsNull() {
        TenantSubscription sub = buildActive(Instant.now().plus(1, ChronoUnit.DAYS));
        assertThrows(IllegalArgumentException.class, () -> sub.renewUntil(null));
    }

    // ---- Helper ----

    private TenantSubscription buildActive(Instant validUntil) {
        return new TenantSubscription(
                new TenantId(UUID.randomUUID()),
                new ServiceId("SERVICE-1"),
                TenantSubscription.SubscriptionStatus.ACTIVE,
                validUntil);
    }
}
