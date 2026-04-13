package com.fleet.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * Metadata for an API key. Plaintext key is NEVER included here.
 */
@Schema(description = "Metadata for an API key")
public record ApiKeyResponse(
        @Schema(description = "Unique ID of the API key", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
        @Schema(description = "Service identifier that owns this key", example = "billing-service") String serviceId,
        @Schema(description = "Human-readable description", example = "Production billing key") String description,
        @Schema(description = "Whether the key is active", example = "true") boolean active,
        @Schema(description = "Whether the key has not expired", example = "true") boolean valid,
        @Schema(description = "Creation timestamp") Instant createdAt,
        @Schema(description = "Expiration timestamp, or null if it never expires") Instant expiresAt) {
}
