package com.fleet.infrastructure.adapter.out.notification;

import com.fleet.domain.notification.port.out.PushSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Console/stub implementation of {@link PushSenderPort} for local development.
 * Active whenever no production push adapter (e.g. FCM) is registered.
 *
 * <p>Replaced by {@link FcmPushNotificationAdapter} when
 * {@code fleet.push.fcm.enabled=true}.</p>
 */
@Component
@ConditionalOnMissingBean(PushSenderPort.class)
@Slf4j
public class ConsolePushAdapter implements PushSenderPort {

    @Override
    public void send(String deviceToken, String title, String body) {
        log.info("[MOCK PUSH] Token: {} | Title: '{}' | Body: {}", deviceToken, title, body);
    }
}
