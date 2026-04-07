package com.fleet.domain.notification.model;

import com.fleet.domain.notification.vo.NotificationId;

import java.time.Instant;

/**
 * The result of a notification dispatch attempt.
 *
 * <p>Returned immediately by {@code SendNotificationUseCase.send()} so that
 * the calling service can correlate the request with delivery status queries.</p>
 *
 * @param notificationId unique ID assigned to this notification event
 * @param status         current delivery status at time of response
 * @param channel        the channel used for dispatch
 * @param recipient      the delivery recipient
 * @param failReason     human-readable reason for failure, or {@code null} if not failed
 * @param timestamp      when the notification was processed
 */
public record NotificationResult(
        NotificationId notificationId,
        DeliveryStatus status,
        NotificationAction.ChannelType channel,
        String recipient,
        String failReason,
        Instant timestamp
) {
    /** Creates a successful result. */
    public static NotificationResult sent(
            NotificationId id,
            NotificationAction.ChannelType channel,
            String recipient) {
        return new NotificationResult(id, DeliveryStatus.SENT, channel, recipient, null, Instant.now());
    }

    /** Creates a failed result with a reason. */
    public static NotificationResult failed(
            NotificationId id,
            NotificationAction.ChannelType channel,
            String recipient,
            String reason) {
        return new NotificationResult(id, DeliveryStatus.FAILED, channel, recipient, reason, Instant.now());
    }
}
