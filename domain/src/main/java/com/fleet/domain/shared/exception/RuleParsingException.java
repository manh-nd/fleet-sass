package com.fleet.domain.shared.exception;

/**
 * Thrown when a rule condition tree cannot be parsed from its serialized form.
 */
public class RuleParsingException extends RuntimeException {

    public RuleParsingException(String message) {
        super(message);
    }

    public RuleParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
