package com.fleet.infrastructure.adapter.out.notification;

import com.fleet.domain.notification.port.out.SmsSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Console/stub implementation of {@link SmsSenderPort} for local development.
 * Active whenever no production SMS adapter (e.g. AWS SNS, Twilio) is registered.
 */
@Component
@ConditionalOnMissingBean(SmsSenderPort.class)
@Slf4j
public class ConsoleSmsAdapter implements SmsSenderPort {

    @Override
    public void send(String phone, String message) {
        log.info("[MOCK SMS] To: {} | Message: {}", phone, message);
    }
}
