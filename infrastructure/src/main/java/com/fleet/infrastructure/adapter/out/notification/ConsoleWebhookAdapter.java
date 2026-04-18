package com.fleet.infrastructure.adapter.out.notification;

import com.fleet.domain.notification.port.out.WebhookSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Console/stub implementation of {@link WebhookSenderPort} for local development.
 * Active whenever no production webhook adapter is registered.
 *
 * <p>A production implementation should sign payloads with HMAC-SHA256
 * and enforce connect/read timeouts.</p>
 */
@Component
@ConditionalOnMissingBean(WebhookSenderPort.class)
@Slf4j
public class ConsoleWebhookAdapter implements WebhookSenderPort {

    @Override
    public void send(String url, String jsonPayload) {
        log.info("[MOCK WEBHOOK] URL: {} | Payload: {}", url, jsonPayload);
    }
}
