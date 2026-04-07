package com.fleet.infrastructure.adapter.out.notification;

import com.fleet.domain.notification.port.out.NotificationDispatcherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub adapter for Firebase Cloud Messaging (FCM) push notifications.
 *
 * <p>Currently logs the intent and simulates a successful dispatch.
 * In production, replace this with the official Firebase Admin SDK:</p>
 * <pre>
 *   implementation 'com.google.firebase:firebase-admin:9.2.0'
 * </pre>
 *
 * <p>This bean is only active when {@code fleet.push.fcm.enabled=true} is set.
 * When disabled, the {@code ConsoleNotificationAdapter} handles PUSH channel for dev/testing.</p>
 */
@Component
@ConditionalOnProperty(name = "fleet.push.fcm.enabled", havingValue = "true")
@Slf4j
public class FcmPushNotificationAdapter implements NotificationDispatcherPort {

    @Override
    public void sendEmail(String recipient, String subject, String body) {
        // FCM adapter only handles PUSH — delegate email to another adapter via composition
        throw new UnsupportedOperationException("FcmPushNotificationAdapter only handles PUSH channel");
    }

    @Override
    public void sendSms(String recipient, String message) {
        throw new UnsupportedOperationException("FcmPushNotificationAdapter only handles PUSH channel");
    }

    @Override
    public void sendWebhook(String url, String payload) {
        throw new UnsupportedOperationException("FcmPushNotificationAdapter only handles PUSH channel");
    }

    @Override
    public void sendPush(String deviceToken, String title, String body) {
        // TODO: replace with Firebase Admin SDK call
        // Message message = Message.builder()
        //     .setToken(deviceToken)
        //     .setNotification(Notification.builder().setTitle(title).setBody(body).build())
        //     .build();
        // FirebaseMessaging.getInstance().send(message);

        log.info("[FCM STUB] Sending push to token={}, title='{}', body='{}'", deviceToken, title, body);
        // Simulate successful FCM dispatch
    }
}
