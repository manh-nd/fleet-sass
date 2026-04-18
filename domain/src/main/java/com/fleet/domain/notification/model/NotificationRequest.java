package com.fleet.domain.notification.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.NotificationAction.ChannelType;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a request to send a notification through a specific channel.
 *
 * <p>This is the primary input for the {@code SendNotificationUseCase} — consuming
 * services submit a {@code NotificationRequest} to trigger a notification directly
 * without needing to define a rule.</p>
 *
 * @param tenantId    the owning tenant
 * @param serviceId   the calling service identity
 * @param channel     the delivery channel (EMAIL, SMS, WEBHOOK, PUSH)
 * @param recipient   email address / phone / device token / webhook URL
 * @param subject     subject line (used for EMAIL; empty default for other channels)
 * @param templateId  optional template ID; if null, {@code body} is used as raw content
 * @param body        raw message body (used when no template is specified)
 * @param locale      locale for i18n template rendering (defaults to English if null)
 * @param variables   key-value pairs for template variable substitution
 */
public record NotificationRequest(
        TenantId tenantId,
        ServiceId serviceId,
        ChannelType channel,
        String recipient,
        String subject,
        String templateId,
        String body,
        Locale locale,
        Map<String, Object> variables
) {
    public NotificationRequest {
        Objects.requireNonNull(tenantId,  "tenantId must not be null");
        Objects.requireNonNull(serviceId, "serviceId must not be null");
        Objects.requireNonNull(channel,   "channel must not be null");

        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("recipient must not be blank");
        }
        if (templateId == null && (body == null || body.isBlank())) {
            throw new IllegalArgumentException("Either templateId or body must be provided");
        }

        // Defaults
        if (subject == null)   subject   = "";
        if (locale == null)    locale    = Locale.ENGLISH;
        if (variables == null) variables = Map.of();
    }
}
