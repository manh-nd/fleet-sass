package com.fleet.domain.notification.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.vo.NotificationId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NotificationLog} domain aggregate.
 * Verifies the immutable state transition pattern.
 */
class NotificationLogTest {

    private final TenantId tenantId   = new TenantId(UUID.randomUUID());
    private final ServiceId serviceId = new ServiceId("FLEET-CORE");

    @Test
    void shouldCreateLogInQueuedState() {
        NotificationLog log = NotificationLog.create(
                tenantId, serviceId,
                NotificationAction.ChannelType.EMAIL,
                "driver@fleet.com",
                "Speed alert content");

        assertEquals(DeliveryStatus.QUEUED, log.getStatus());
        assertEquals(0, log.getAttempts());
        assertNull(log.getFailReason());
        assertNotNull(log.getId());
        assertNotNull(log.getCreatedAt());
    }

    @Test
    void shouldTransitionToSentAndIncrementAttempts() {
        NotificationLog original = NotificationLog.create(
                tenantId, serviceId,
                NotificationAction.ChannelType.SMS,
                "0901234567", "Message");

        NotificationLog sent = original.markSent();

        // Original is unchanged (immutable)
        assertEquals(DeliveryStatus.QUEUED, original.getStatus());
        assertEquals(0, original.getAttempts());

        // New instance has updated state
        assertEquals(DeliveryStatus.SENT, sent.getStatus());
        assertEquals(1, sent.getAttempts());
        assertNull(sent.getFailReason());
        // Same identity
        assertEquals(original.getId(), sent.getId());
    }

    @Test
    void shouldTransitionToFailedWithReason() {
        NotificationLog original = NotificationLog.create(
                tenantId, serviceId,
                NotificationAction.ChannelType.WEBHOOK,
                "https://hook.io", "Payload");

        NotificationLog failed = original.markFailed("Connection timeout");

        assertEquals(DeliveryStatus.FAILED, failed.getStatus());
        assertEquals("Connection timeout", failed.getFailReason());
        assertEquals(1, failed.getAttempts());
        assertEquals(original.getId(), failed.getId());
    }

    @Test
    void shouldReconstituteWithAllFields() {
        NotificationId notifId = NotificationId.generate();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        NotificationLog log = NotificationLog.reconstitute(
                notifId, tenantId, serviceId,
                NotificationAction.ChannelType.PUSH,
                "device-token-123",
                "Push body",
                DeliveryStatus.SENT,
                null, 2, createdAt);

        assertEquals(notifId, log.getId());
        assertEquals(DeliveryStatus.SENT, log.getStatus());
        assertEquals(2, log.getAttempts());
        assertEquals(createdAt, log.getCreatedAt());
        assertEquals("device-token-123", log.getRecipient());
    }
}
