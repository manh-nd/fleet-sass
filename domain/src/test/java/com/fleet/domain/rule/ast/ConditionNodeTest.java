package com.fleet.domain.rule.ast;

import com.fleet.domain.rule.vo.EventPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConditionNodeTest {

    @Test
    void shouldEvaluateGreaterThan() {
        ConditionNode node = new ConditionNode("speed", Operator.GT, 80);
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 100))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("speed", 60))));
    }

    @Test
    void shouldEvaluateLessThan() {
        ConditionNode node = new ConditionNode("speed", Operator.LT, 80);
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 60))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("speed", 100))));
    }

    @Test
    void shouldEvaluateEquals() {
        ConditionNode node = new ConditionNode("status", Operator.EQ, "ONLINE");
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "ONLINE"))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "OFFLINE"))));
    }

    @Test
    void shouldEvaluateNotEquals() {
        ConditionNode node = new ConditionNode("status", Operator.NEQ, "ONLINE");
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "OFFLINE"))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "ONLINE"))));
    }

    @Test
    void shouldEvaluateGreaterThanOrEqual() {
        ConditionNode node = new ConditionNode("speed", Operator.GTE, 80);
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 100))));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 80))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("speed", 60))));
    }

    @Test
    void shouldEvaluateLessThanOrEqual() {
        ConditionNode node = new ConditionNode("speed", Operator.LTE, 80);
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 60))));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 80))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("speed", 100))));
    }

    @Test
    void shouldReturnFalseWhenFieldIsMissing() {
        ConditionNode node = new ConditionNode("speed", Operator.GT, 80);
        assertFalse(node.evaluate(new EventPayload("v1", Map.of())));
    }

    @Test
    void shouldEvaluateIn() {
        ConditionNode node = new ConditionNode("status", Operator.IN, List.of("ONLINE", "IDLE"));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "ONLINE"))));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "IDLE"))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "OFFLINE"))));
    }

    @Test
    void shouldEvaluateNotIn() {
        ConditionNode node = new ConditionNode("status", Operator.NOT_IN, List.of("OFFLINE", "ERROR"));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "ONLINE"))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "OFFLINE"))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "ERROR"))));
    }

    @Test
    void shouldReturnFalseForInWhenValueIsNotCollection() {
        // value is a plain String, not a Collection → IN should return false
        ConditionNode node = new ConditionNode("status", Operator.IN, "ONLINE");
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "ONLINE"))));
    }

    @Test
    void shouldReturnTrueForNotInWhenValueIsNotCollection() {
        // value is a plain String, not a Collection → NOT_IN should return true (vacuous)
        ConditionNode node = new ConditionNode("status", Operator.NOT_IN, "OFFLINE");
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "ONLINE"))));
    }

    @Test
    void shouldThrowIllegalArgumentWhenComparingNonNumericValues() {
        ConditionNode node = new ConditionNode("status", Operator.GT, 80);
        EventPayload payload = new EventPayload("v1", Map.of("status", "not-a-number"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> node.evaluate(payload));
        assertTrue(ex.getMessage().contains("Cannot compare non-numeric values"));
    }

    @Test
    void shouldThrowWhenFieldIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConditionNode("", Operator.GT, 80));
        assertThrows(IllegalArgumentException.class,
                () -> new ConditionNode("  ", Operator.GT, 80));
    }

    @Test
    void shouldThrowWhenOperatorIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConditionNode("speed", null, 80));
    }
}
