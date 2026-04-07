package com.fleet.domain.rule.ast;

import java.util.List;

/**
 * Sealed type hierarchy for condition values in the rule AST.
 *
 * <p>Using a sealed interface makes the value type explicit and enables
 * exhaustive pattern matching within {@link ConditionNode} and infrastructure
 * serializers, eliminating unchecked {@code Object} casts.</p>
 *
 * <p>Permitted subtypes:</p>
 * <ul>
 *   <li>{@link NumericValue} — for numeric comparisons (GT, LT, GTE, LTE, EQ, NEQ)</li>
 *   <li>{@link StringValue}  — for string equality / set membership checks</li>
 *   <li>{@link BooleanValue} — for flag conditions (EQ, NEQ)</li>
 *   <li>{@link ListValue}    — for IN / NOT_IN set membership operators</li>
 * </ul>
 */
public sealed interface ConditionValue
        permits ConditionValue.NumericValue,
                ConditionValue.StringValue,
                ConditionValue.BooleanValue,
                ConditionValue.ListValue {

    // ---- Subtypes (records for conciseness) ----

    /**
     * A numeric condition value backed by a {@code double}.
     */
    record NumericValue(double number) implements ConditionValue {
        /** Convenience factory for integer literals (e.g. from JSON). */
        public static NumericValue of(Number n) {
            return new NumericValue(n.doubleValue());
        }
    }

    /**
     * A string condition value.
     */
    record StringValue(String text) implements ConditionValue {
        public StringValue {
            if (text == null) throw new IllegalArgumentException("StringValue text must not be null");
        }
    }

    /**
     * A boolean condition value.
     */
    record BooleanValue(boolean flag) implements ConditionValue {}

    /**
     * A list condition value for IN / NOT_IN membership checks.
     *
     * <p>Each element is itself a {@link ConditionValue} to remain type-safe.</p>
     */
    record ListValue(List<ConditionValue> elements) implements ConditionValue {
        public ListValue {
            if (elements == null || elements.isEmpty())
                throw new IllegalArgumentException("ListValue must contain at least one element");
            elements = List.copyOf(elements); // defensive copy + immutability
        }

        /** Returns true if any element's raw value equals the given actual value. */
        public boolean contains(Object actual) {
            return elements.stream().anyMatch(cv -> rawEquals(cv, actual));
        }

        private boolean rawEquals(ConditionValue cv, Object actual) {
            return switch (cv) {
                case NumericValue n -> {
                    try { yield Double.compare(n.number(), Double.parseDouble(actual.toString())) == 0; }
                    catch (NumberFormatException e) { yield false; }
                }
                case StringValue s  -> s.text().equals(actual);
                case BooleanValue b -> actual instanceof Boolean ab && ab == b.flag();
                case ListValue ignored -> false; // nested lists not supported
            };
        }
    }
}
