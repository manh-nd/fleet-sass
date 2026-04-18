package com.fleet.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Request body for the direct-send notification API.
 *
 * <p>Either {@code templateId} or {@code body} must be provided (validated in the
 * domain model). The {@code subject} field is used for EMAIL notifications;
 * it is optional and defaults to an empty string for other channels.</p>
 */
@Schema(description = "Request body for sending a notification immediately")
public record SendNotificationRequest(
        @Schema(description = "The owning tenant's UUID",
                example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull UUID tenantId,

        @Schema(description = "The calling service identifier", example = "billing-service")
        @NotBlank String serviceId,

        @Schema(description = "Delivery channel: EMAIL | SMS | WEBHOOK | PUSH",
                example = "EMAIL")
        @NotBlank String channel,

        @Schema(description = "Recipient: email address, phone number, device token, or webhook URL",
                example = "driver@fleet.com")
        @NotBlank String recipient,

        @Schema(description = "Subject line (EMAIL only; ignored for other channels)",
                example = "Fleet Speed Alert")
        String subject,

        @Schema(description = "Optional template ID for i18n rendering",
                example = "SPEED_ALERT")
        String templateId,

        @Schema(description = "Raw message body (used when templateId is not provided)",
                example = "Vehicle {{plate}} exceeded {{speed}} km/h.")
        String body,

        @Schema(description = "BCP 47 locale tag for i18n (e.g. 'en', 'vi')", example = "en")
        String locale,

        @Schema(description = "Template placeholder values",
                example = "{\"plate\": \"51A-123.45\", \"speed\": 120}")
        Map<String, Object> variables) {
}
