package com.fleet.domain.rule.ast;

/**
 * Supported logical operators for {@link LogicalNode}.
 * Using an enum eliminates stringly-typed operators and makes
 * invalid construction impossible at the domain level.
 */
public enum LogicalOperator {
    AND,
    OR;

    /**
     * Resolves a {@link LogicalOperator} from its name string (case-insensitive).
     *
     * @param name the operator string as stored in JSON / DB
     * @return matching {@link LogicalOperator}
     * @throws IllegalArgumentException if the name is not supported
     */
    public static LogicalOperator fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("LogicalOperator name must not be null");
        }
        try {
            return LogicalOperator.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported logical operator: " + name);
        }
    }
}
