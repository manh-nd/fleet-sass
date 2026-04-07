package com.fleet.infrastructure.adapter.out.messaging;

import org.springframework.stereotype.Component;

import com.fleet.domain.notification.port.out.NotificationDispatcherPort;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock implementation of {@link NotificationDispatcherPort} for local development.
 * Logs all notification dispatches to the console instead of sending real messages.
 */
@Component
@Slf4j
public class ConsoleNotificationAdapter implements NotificationDispatcherPort {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("[MOCK EMAIL] To: {} | Subject: '{}' | Body: {}", to, subject, body);
    }

    @Override
    public void sendSms(String phone, String message) {
        log.info("[MOCK SMS] To: {} | Message: {}", phone, message);
    }

    @Override
    public void sendWebhook(String url, String jsonPayload) {
        log.info("[MOCK WEBHOOK] URL: {} | Payload: {}", url, jsonPayload);
    }

    @Override
    public void sendPush(String deviceToken, String title, String body) {
        log.info("[MOCK PUSH] Token: {} | Title: '{}' | Body: {}", deviceToken, title, body);
    }
}