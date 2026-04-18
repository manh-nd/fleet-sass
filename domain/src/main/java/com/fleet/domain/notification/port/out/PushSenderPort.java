package com.fleet.domain.notification.port.out;

/**
 * Outbound port for sending push notifications.
 *
 * <p>Implementations include Firebase Cloud Messaging (FCM), Apple Push Notification
 * service (APNs), or a console stub for local dev.</p>
 */
public interface PushSenderPort {

    /**
     * Sends a push notification to a device.
     *
     * @param deviceToken FCM registration token or APNs device token
     * @param title       notification title
     * @param body        notification body
     */
    void send(String deviceToken, String title, String body);
}
