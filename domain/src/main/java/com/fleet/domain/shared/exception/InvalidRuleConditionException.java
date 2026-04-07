package com.fleet.domain.shared.exception;

/**
 * Thrown when a rule condition tree is structurally invalid.
 */
public class InvalidRuleConditionException extends RuntimeException {

    public InvalidRuleConditionException(String message) {
        super(message);
    }

    public InvalidRuleConditionException(String message, Throwable cause) {
        super(message, cause);
    }
}
