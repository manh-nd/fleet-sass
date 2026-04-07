package com.fleet.domain.rule.ast;

import com.fleet.domain.rule.vo.EventPayload;

/**
 * A leaf node in the rule AST that evaluates a single field condition
 * against an {@link EventPayload}.
 *
 * <p>The {@code value} field is now a typed {@link ConditionValue}, enabling
 * exhaustive pattern matching and eliminating unchecked {@code Object} casts.</p>
 */
public class ConditionNode implements RuleNode {

    private final String field;
    private final Operator operator;
    private final ConditionValue value;

    public ConditionNode(String field, Operator operator, ConditionValue value) {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("ConditionNode field must not be null or blank");
        }
        if (operator == null) {
            throw new IllegalArgumentException("ConditionNode operator must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("ConditionNode value must not be null");
        }
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Convenience constructor that wraps a raw {@link Object} into the appropriate
     * {@link ConditionValue} subtype. Used by infrastructure parsers and tests.
     */
    public ConditionNode(String field, Operator operator, Object rawValue) {
        this(field, operator, toConditionValue(rawValue));
    }

    public String getField() { return field; }
    public Operator getOperator() { return operator; }
    public ConditionValue getValue() { return value; }

    @Override
    public boolean evaluate(EventPayload payload) {
        Object actual = payload.data().get(field);
        if (actual == null) {
            return false;
        }

        return switch (operator) {
            case GT     -> compareNumeric(actual, value) > 0;
            case LT     -> compareNumeric(actual, value) < 0;
            case GTE    -> compareNumeric(actual, value) >= 0;
            case LTE    -> compareNumeric(actual, value) <= 0;
            case EQ     -> equalsValue(actual, value);
            case NEQ    -> !equalsValue(actual, value);
            case IN     -> value instanceof ConditionValue.ListValue lv && lv.contains(actual);
            case NOT_IN -> !(value instanceof ConditionValue.ListValue lv && lv.contains(actual));
        };
    }

    // ---- Private Helpers ----

    private int compareNumeric(Object actual, ConditionValue expected) {
        double a = parseDouble(actual, "actual");
        double b = switch (expected) {
            case ConditionValue.NumericValue n -> n.number();
            case ConditionValue.StringValue s  -> parseDouble(s.text(), "expected");
            default -> throw new IllegalArgumentException(
                    "Operator '%s' requires a numeric value, got: %s".formatted(operator, expected));
        };
        return Double.compare(a, b);
    }

    private boolean equalsValue(Object actual, ConditionValue expected) {
        return switch (expected) {
            case ConditionValue.NumericValue n -> {
                try { yield Double.compare(Double.parseDouble(actual.toString()), n.number()) == 0; }
                catch (NumberFormatException e) { yield false; }
            }
            case ConditionValue.StringValue s  -> s.text().equals(actual);
            case ConditionValue.BooleanValue b -> actual instanceof Boolean ab && ab == b.flag();
            case ConditionValue.ListValue lv   -> lv.contains(actual);
        };
    }

    private double parseDouble(Object val, String label) {
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Cannot compare non-numeric %s value for operator '%s': %s".formatted(label, operator, val), e);
        }
    }

    /**
     * Wraps a raw value (from JSON parsing or tests) into the appropriate {@link ConditionValue}.
     */
    public static ConditionValue toConditionValue(Object raw) {
        if (raw instanceof ConditionValue cv) return cv;
        if (raw instanceof Number n)          return ConditionValue.NumericValue.of(n);
        if (raw instanceof Boolean b)         return new ConditionValue.BooleanValue(b);
        if (raw instanceof java.util.List<?> list) {
            var elements = list.stream()
                    .map(ConditionNode::toConditionValue)
                    .toList();
            return new ConditionValue.ListValue(elements);
        }
        return new ConditionValue.StringValue(String.valueOf(raw));
    }
}
