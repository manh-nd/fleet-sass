package com.fleet.domain.notification.port.out;

/**
 * Outbound port for dispatching notifications through multiple channels.
 *
 * @deprecated Replaced by focused single-responsibility ports:
 *             {@link EmailSenderPort}, {@link SmsSenderPort},
 *             {@link WebhookSenderPort}, {@link PushSenderPort}.
 *             This interface will be removed once all adapters are migrated.
 */
@Deprecated(since = "3.1.0", forRemoval = true)
public interface NotificationDispatcherPort {

    void sendEmail(String to, String subject, String body);

    void sendSms(String phone, String message);

    void sendWebhook(String url, String jsonPayload);

    /**
     * Sends a push notification to a device via FCM/APNs.
     *
     * @param deviceToken the FCM registration token or APNs device token
     * @param title       the notification title
     * @param body        the notification body
     */
    void sendPush(String deviceToken, String title, String body);

}

