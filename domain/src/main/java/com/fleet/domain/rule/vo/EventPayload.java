package com.fleet.domain.rule.vo;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the raw event data submitted for rule evaluation.
 * {@code referenceId} identifies the subject entity (vehicle, user, device, order, etc.)
 * that produced the event.
 */
public record EventPayload(String referenceId, Map<String, Object> data) {
    public EventPayload {
        Objects.requireNonNull(referenceId, "referenceId must not be null");
        Objects.requireNonNull(data, "data must not be null");
    }
}
