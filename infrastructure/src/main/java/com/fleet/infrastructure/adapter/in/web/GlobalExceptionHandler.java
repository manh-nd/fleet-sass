package com.fleet.infrastructure.adapter.in.web;

import com.fleet.domain.shared.exception.InvalidRuleConditionException;
import com.fleet.domain.shared.exception.RuleNotFoundException;
import com.fleet.domain.shared.exception.RuleParsingException;
import com.fleet.infrastructure.adapter.in.web.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centralized exception handler for all REST controllers.
 * Translates domain exceptions into structured {@link ErrorResponse} bodies with appropriate HTTP status codes.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 404 — rule not found in the repository.
     */
    @ExceptionHandler(RuleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRuleNotFound(RuleNotFoundException ex) {
        log.warn("Rule not found: {}", ex.getRuleId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("RULE_NOT_FOUND", ex.getMessage()));
    }

    /**
     * 400 — condition JSON could not be parsed into a valid AST.
     */
    @ExceptionHandler(RuleParsingException.class)
    public ResponseEntity<ErrorResponse> handleRuleParsing(RuleParsingException ex) {
        log.warn("Rule condition parsing failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_CONDITION_JSON", ex.getMessage()));
    }

    /**
     * 400 — condition tree is structurally invalid (e.g. empty logical node).
     */
    @ExceptionHandler(InvalidRuleConditionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCondition(InvalidRuleConditionException ex) {
        log.warn("Invalid rule condition: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_CONDITION", ex.getMessage()));
    }

    /**
     * 400 — Bean Validation constraint violations on request DTOs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", details));
    }

    /**
     * 400 — domain invariant violations (e.g. negative cooldown, blank eventType).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_ARGUMENT", ex.getMessage()));
    }

    /**
     * 500 — catch-all for unexpected errors. Logs the full stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred. Please try again later."));
    }
}
