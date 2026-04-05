package com.fleet.domain.rule.ast;

import com.fleet.domain.rule.vo.EventPayload;

import java.util.Collection;

/**
 * A leaf node in the rule AST that evaluates a single field condition
 * against an {@link EventPayload}.
 */
public class ConditionNode implements RuleNode {

    private final String field;
    private final Operator operator;
    private final Object value;

    public ConditionNode(String field, Operator operator, Object value) {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("ConditionNode field must not be null or blank");
        }
        if (operator == null) {
            throw new IllegalArgumentException("ConditionNode operator must not be null");
        }
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean evaluate(EventPayload payload) {
        Object actualValue = payload.data().get(field);
        if (actualValue == null) {
            return false;
        }

        return switch (operator) {
            case GT      -> compareNumeric(actualValue, value) > 0;
            case LT      -> compareNumeric(actualValue, value) < 0;
            case GTE     -> compareNumeric(actualValue, value) >= 0;
            case LTE     -> compareNumeric(actualValue, value) <= 0;
            case EQ      -> actualValue.equals(value);
            case NEQ     -> !actualValue.equals(value);
            case IN      -> {
                if (value instanceof Collection<?> coll) {
                    yield coll.contains(actualValue);
                }
                yield false;
            }
            case NOT_IN  -> {
                if (value instanceof Collection<?> coll) {
                    yield !coll.contains(actualValue);
                }
                // If no collection is provided the contract is undefined;
                // treat as "not in empty set" → true
                yield true;
            }
        };
    }

    private int compareNumeric(Object actual, Object expected) {
        try {
            double a = Double.parseDouble(actual.toString());
            double b = Double.parseDouble(expected.toString());
            return Double.compare(a, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Cannot compare non-numeric values for operator '%s': actual=%s, expected=%s"
                            .formatted(operator, actual, expected), e);
        }
    }
}
