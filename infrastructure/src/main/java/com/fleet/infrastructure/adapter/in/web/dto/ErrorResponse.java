package com.fleet.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Standard error response body returned by
 * {@link com.fleet.infrastructure.adapter.in.web.GlobalExceptionHandler}.
 *
 * @param code      machine-readable error code (e.g. "RULE_NOT_FOUND",
 *                  "INVALID_CONDITION")
 * @param message   human-readable description of the error
 * @param timestamp when the error occurred (UTC)
 */
@Schema(description = "Standard error response structure")
public record ErrorResponse(
        @Schema(description = "Machine-readable error code", example = "RULE_NOT_FOUND") String code,
        @Schema(description = "Human-readable error description", example = "The requested rule with ID 123 was not found.") String message,
        @Schema(description = "UTC timestamp of the error") Instant timestamp) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, Instant.now());
    }
}
