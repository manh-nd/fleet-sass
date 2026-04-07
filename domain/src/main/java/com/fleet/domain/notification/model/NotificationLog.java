package com.fleet.domain.notification.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.vo.NotificationId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * Audit record for every notification dispatch attempt.
 *
 * <p>{@code NotificationLog} provides full observability into notification delivery:
 * which service sent it, through which channel, to whom, and with what result.
 * This enables delivery status queries, retry logic, and analytics.</p>
 *
 * <p>Instances are created via {@link #create} and updated via {@link #markSent},
 * {@link #markFailed} — immutability via returning new instances.</p>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationLog {

    private final NotificationId id;
    private final TenantId tenantId;
    private final ServiceId serviceId;
    private final NotificationAction.ChannelType channel;
    private final String recipient;
    private final String renderedContent;
    private final DeliveryStatus status;
    private final String failReason;
    private final int attempts;
    private final Instant createdAt;

    // ---- Factory ----

    /**
     * Creates a new log entry in {@link DeliveryStatus#QUEUED} state.
     */
    public static NotificationLog create(
            TenantId tenantId,
            ServiceId serviceId,
            NotificationAction.ChannelType channel,
            String recipient,
            String renderedContent) {
        return new NotificationLog(
                NotificationId.generate(),
                tenantId,
                serviceId,
                channel,
                recipient,
                renderedContent,
                DeliveryStatus.QUEUED,
                null,
                0,
                Instant.now());
    }

    /**
     * Reconstitutes a persisted log entry from the database, preserving its original state.
     * Used exclusively by repository adapters — not for business logic.
     */
    public static NotificationLog reconstitute(
            NotificationId id,
            TenantId tenantId,
            ServiceId serviceId,
            NotificationAction.ChannelType channel,
            String recipient,
            String renderedContent,
            DeliveryStatus status,
            String failReason,
            int attempts,
            Instant createdAt) {
        return new NotificationLog(
                id, tenantId, serviceId, channel, recipient,
                renderedContent, status, failReason, attempts, createdAt);
    }

    /** Returns a new log with status SENT and incremented attempt count. */
    public NotificationLog markSent() {
        return new NotificationLog(
                id, tenantId, serviceId, channel, recipient, renderedContent,
                DeliveryStatus.SENT, null, attempts + 1, createdAt);
    }

    /** Returns a new log with status FAILED, a reason, and incremented attempt count. */
    public NotificationLog markFailed(String reason) {
        return new NotificationLog(
                id, tenantId, serviceId, channel, recipient, renderedContent,
                DeliveryStatus.FAILED, reason, attempts + 1, createdAt);
    }
}
