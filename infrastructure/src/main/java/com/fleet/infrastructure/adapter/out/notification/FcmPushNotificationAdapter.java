package com.fleet.infrastructure.adapter.out.notification;

import com.fleet.domain.notification.port.out.PushSenderPort;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Firebase Cloud Messaging (FCM) adapter for push notifications.
 *
 * <p>Only active when {@code fleet.push.fcm.enabled=true}. When disabled,
 * {@link ConsolePushAdapter} handles the PUSH channel for dev and testing.</p>
 *
 * <p>This adapter implements only {@link PushSenderPort} — it has no knowledge
 * of email, SMS, or webhooks, in line with the Interface Segregation Principle.</p>
 */
@Component
@ConditionalOnProperty(name = "fleet.push.fcm.enabled", havingValue = "true")
@Slf4j
public class FcmPushNotificationAdapter implements PushSenderPort {

    @Override
    public void send(String deviceToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to token: {}", deviceToken, e);
            throw new RuntimeException("FCM dispatch failed for token: " + deviceToken, e);
        } catch (Exception e) {
            log.error("Unexpected error sending FCM message", e);
            throw new RuntimeException("Unexpected FCM error", e);
        }
    }
}
