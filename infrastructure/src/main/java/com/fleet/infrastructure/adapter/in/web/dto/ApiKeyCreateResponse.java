package com.fleet.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * Response returned when a new API key is issued.
 * Includes the plaintext key which is only shown ONCE.
 */
@Schema(description = "Response returned when a new API key is issued")
public record ApiKeyCreateResponse(
        @Schema(description = "Unique ID of the API key", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
        @Schema(description = "The plaintext API key. STORE THIS SECURELY. It will not be shown again.", example = "ak_7f8e...)") String key,
        @Schema(description = "Service identifier that owns this key", example = "billing-service") String serviceId,
        @Schema(description = "Human-readable description", example = "Production billing key") String description,
        @Schema(description = "Expiration timestamp, or null if it never expires") Instant expiresAt,
        @Schema(description = "Security warning about the plaintext key", example = "Store this key securely. It will not be shown again.") String warning) {
}
