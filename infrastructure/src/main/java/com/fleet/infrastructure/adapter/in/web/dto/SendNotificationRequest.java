package com.fleet.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Request body for the direct-send notification API.
 *
 * <p>Either {@code templateId} or {@code body} must be provided (validated in the domain model).</p>
 *
 * @param tenantId   the owning tenant's UUID
 * @param serviceId  the calling service identifier
 * @param channel    delivery channel: EMAIL | SMS | WEBHOOK | PUSH
 * @param recipient  email address, phone number, device token, or webhook URL
 * @param templateId optional template ID for i18n rendering
 * @param body       raw message body (used when templateId is not provided)
 * @param locale     BCP 47 locale tag for i18n (e.g. "en", "vi") — defaults to "en" if null
 * @param variables  template placeholder values
 */
public record SendNotificationRequest(
        @NotNull UUID tenantId,
        @NotBlank String serviceId,
        @NotBlank String channel,
        @NotBlank String recipient,
        String templateId,
        String body,
        String locale,
        Map<String, Object> variables
) {}
