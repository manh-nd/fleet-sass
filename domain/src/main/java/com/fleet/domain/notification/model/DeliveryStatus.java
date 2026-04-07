package com.fleet.domain.notification.model;

/**
 * Represents the current delivery state of a notification.
 *
 * <p>State transitions:</p>
 * <pre>
 *   QUEUED → SENT      (successfully dispatched to channel)
 *   QUEUED → FAILED    (dispatch failed after max retries)
 *   SENT   → DELIVERED (channel confirmed delivery — future extension)
 * </pre>
 */
public enum DeliveryStatus {

    /** Notification has been accepted and is pending dispatch. */
    QUEUED,

    /** Notification was successfully handed off to the delivery channel. */
    SENT,

    /** Notification delivery was confirmed by the channel (e.g. webhook 200, FCM success). */
    DELIVERED,

    /** Notification dispatch failed permanently after exhausting retries. */
    FAILED
}
