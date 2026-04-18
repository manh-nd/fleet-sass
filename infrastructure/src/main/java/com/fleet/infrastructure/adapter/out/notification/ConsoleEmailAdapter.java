package com.fleet.infrastructure.adapter.out.notification;

import com.fleet.domain.notification.port.out.EmailSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Console/stub implementation of {@link EmailSenderPort} for local development.
 * Active whenever no production email adapter (e.g. SES) is registered.
 *
 * <p>Replace with {@code SesEmailAdapter} by setting
 * {@code fleet.email.provider=ses} (future).</p>
 */
@Component
@ConditionalOnMissingBean(EmailSenderPort.class)
@Slf4j
public class ConsoleEmailAdapter implements EmailSenderPort {

    @Override
    public void send(String to, String subject, String body) {
        log.info("[MOCK EMAIL] To: {} | Subject: '{}' | Body: {}", to, subject, body);
    }
}
