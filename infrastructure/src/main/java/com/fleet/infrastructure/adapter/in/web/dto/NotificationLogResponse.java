package com.fleet.infrastructure.adapter.in.web.dto;

import com.fleet.domain.notification.model.NotificationLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response body for a single notification delivery log entry.
 */
@Schema(description = "Response body for a single notification delivery log entry")
public record NotificationLogResponse(
        @Schema(description = "Unique ID of the notification event", example = "550e8400-e29b-41d4-a716-446655440000") UUID notificationId,
        @Schema(description = "Owning tenant UUID", example = "550e8400-e29b-41d4-a716-446655440000") UUID tenantId,
        @Schema(description = "Calling service identifier", example = "billing-service") String serviceId,
        @Schema(description = "Delivery channel used", example = "EMAIL") String channel,
        @Schema(description = "Delivery recipient", example = "user@example.com") String recipient,
        @Schema(description = "Current delivery status", example = "SENT", allowableValues = {"QUEUED", "SENT", "DELIVERED", "FAILED"}) String status,
        @Schema(description = "Failure reason, or null if not failed", example = "Invalid email format") String failReason,
        @Schema(description = "Number of dispatch attempts", example = "1") int attempts,
        @Schema(description = "When the notification was first submitted") Instant createdAt) {
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
