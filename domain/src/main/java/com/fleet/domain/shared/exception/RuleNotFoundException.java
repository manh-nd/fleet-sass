package com.fleet.domain.shared.exception;

import java.util.UUID;

/**
 * Thrown when a notification rule cannot be found by its identifier.
 */
public class RuleNotFoundException extends RuntimeException {

    private final UUID ruleId;

    public RuleNotFoundException(UUID ruleId) {
        super("Notification rule not found: " + ruleId);
        this.ruleId = ruleId;
    }

    public UUID getRuleId() {
        return ruleId;
    }
}
