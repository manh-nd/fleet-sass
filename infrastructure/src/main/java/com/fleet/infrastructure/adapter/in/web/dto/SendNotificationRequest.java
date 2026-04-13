package com.fleet.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Request body for the direct-send notification API.
 *
 * <p>
 * Either {@code templateId} or {@code body} must be provided (validated in the
 * domain model).
 * </p>
 */
@Schema(description = "Request body for sending a notification immediately")
public record SendNotificationRequest(
        @Schema(description = "The owning tenant's UUID", example = "550e8400-e29b-41d4-a716-446655440000") @NotNull UUID tenantId,
        @Schema(description = "The calling service identifier", example = "billing-service") @NotBlank String serviceId,
        @Schema(description = "Delivery channel: EMAIL | SMS | WEBHOOK | PUSH", example = "EMAIL") @NotBlank String channel,
        @Schema(description = "Recipient: email, phone, device token, or URL", example = "user@example.com") @NotBlank String recipient,
        @Schema(description = "Optional template ID for i18n rendering", example = "invoice-ready") String templateId,
        @Schema(description = "Raw message body (used when templateId is not provided)", example = "Your invoice is ready.") String body,
        @Schema(description = "BCP 47 locale tag for i18n (e.g. 'en', 'vi')", example = "en") String locale,
        @Schema(description = "Template placeholder values (dynamic key-value pairs)", 
                example = "{\"user_name\": \"John Doe\", \"order_id\": \"ABC-123\"}") Map<String, Object> variables) {
}
