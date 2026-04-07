package com.fleet.infrastructure.adapter.in.web.dto;

import com.fleet.domain.notification.model.NotificationLog;

import java.time.Instant;
import java.util.UUID;

/**
 * Response body for a single notification delivery log entry.
 *
 * @param notificationId unique ID of the notification event
 * @param tenantId       owning tenant
 * @param serviceId      calling service
 * @param channel        delivery channel used
 * @param recipient      delivery recipient
 * @param status         current delivery status (QUEUED, SENT, DELIVERED, FAILED)
 * @param failReason     failure reason, or {@code null} if not failed
 * @param attempts       number of dispatch attempts
 * @param createdAt      when the notification was first submitted
 */
public record NotificationLogResponse(
        UUID notificationId,
        UUID tenantId,
        String serviceId,
        String channel,
        String recipient,
        String status,
        String failReason,
        int attempts,
        Instant createdAt
) {
    public static NotificationLogResponse from(NotificationLog log) {
        return new NotificationLogResponse(
                log.getId().value(),
                log.getTenantId().value(),
                log.getServiceId().value(),
                log.getChannel().name(),
                log.getRecipient(),
                log.getStatus().name(),
                log.getFailReason(),
                log.getAttempts(),
                log.getCreatedAt());
    }
}
