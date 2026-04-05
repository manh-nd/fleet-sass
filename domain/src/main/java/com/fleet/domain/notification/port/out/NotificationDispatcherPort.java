package com.fleet.domain.notification.port.out;

/**
 * Outbound port for dispatching notifications through multiple channels.
 */
public interface NotificationDispatcherPort {

    void sendEmail(String to, String subject, String body);

    void sendSms(String phone, String message);

    void sendWebhook(String url, String jsonPayload);

}
