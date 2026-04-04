package com.fleet.domain.rule.ast;

import com.fleet.domain.rule.vo.EventPayload;
import lombok.Getter;

@Getter
public class ConditionNode implements RuleNode {
    private final String type = "CONDITION";
    private final String field;
    private final String operator;
    private final Object value;

    public ConditionNode(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public boolean evaluate(EventPayload payload) {
        Object actualValue = payload.data().get(field);
        if (actualValue == null)
            return false;

        return switch (operator) {
            case ">" -> compareNumeric(actualValue, value) > 0;
            case "<" -> compareNumeric(actualValue, value) < 0;
            case "==" -> actualValue.equals(value);
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

    private int compareNumeric(Object actual, Object expected) {
        return Double.valueOf(actual.toString()).compareTo(Double.valueOf(expected.toString()));
    }
}
