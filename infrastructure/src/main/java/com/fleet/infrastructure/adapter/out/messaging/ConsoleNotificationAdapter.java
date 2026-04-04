package com.fleet.infrastructure.adapter.out.messaging;

import org.springframework.stereotype.Service;

import com.fleet.domain.notification.port.out.NotificationDispatcherPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConsoleNotificationAdapter implements NotificationDispatcherPort {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("📧 [MOCK EMAIL] Gửi tới {}: Tiêu đề: '{}' | Nội dung: {}", to, subject, body);
    }

    @Override
    public void sendSms(String phone, String message) {
        log.info("📱 [MOCK SMS] Gửi tới {}: Nội dung: {}", phone, message);
    }

    @Override
    public void sendWebhook(String url, String jsonPayload) {
        log.info("🌐 [MOCK WEBHOOK] Bắn tới {}: Payload: {}", url, jsonPayload);
    }
}