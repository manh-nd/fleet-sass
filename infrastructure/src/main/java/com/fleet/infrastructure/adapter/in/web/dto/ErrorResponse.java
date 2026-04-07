package com.fleet.infrastructure.adapter.in.web.dto;

import java.time.Instant;

/**
 * Standard error response body returned by {@link com.fleet.infrastructure.adapter.in.web.GlobalExceptionHandler}.
 *
 * @param code      machine-readable error code (e.g. "RULE_NOT_FOUND", "INVALID_CONDITION")
 * @param message   human-readable description of the error
 * @param timestamp when the error occurred (UTC)
 */
public record ErrorResponse(String code, String message, Instant timestamp) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, Instant.now());
    }
}
