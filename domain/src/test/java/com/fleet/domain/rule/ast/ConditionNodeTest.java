package com.fleet.domain.rule.ast;

import com.fleet.domain.rule.vo.EventPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConditionNodeTest {

    @Test
    void shouldEvaluateGreaterThan() {
        ConditionNode node = new ConditionNode("speed", ">", 80);
        EventPayload payload = new EventPayload("v1", Map.of("speed", 100));
        assertTrue(node.evaluate(payload));

        payload = new EventPayload("v1", Map.of("speed", 60));
        assertFalse(node.evaluate(payload));
    }

    @Test
    void shouldEvaluateLessThan() {
        ConditionNode node = new ConditionNode("speed", "<", 80);
        EventPayload payload = new EventPayload("v1", Map.of("speed", 60));
        assertTrue(node.evaluate(payload));

        payload = new EventPayload("v1", Map.of("speed", 100));
        assertFalse(node.evaluate(payload));
    }

    @Test
    void shouldEvaluateEquals() {
        ConditionNode node = new ConditionNode("status", "==", "ONLINE");
        EventPayload payload = new EventPayload("v1", Map.of("status", "ONLINE"));
        assertTrue(node.evaluate(payload));

        payload = new EventPayload("v1", Map.of("status", "OFFLINE"));
        assertFalse(node.evaluate(payload));
    }

    @Test
    void shouldReturnFalseWhenFieldIsMissing() {
        ConditionNode node = new ConditionNode("speed", ">", 80);
        EventPayload payload = new EventPayload("v1", Map.of());
        assertFalse(node.evaluate(payload));
    }

    @Test
    void shouldEvaluateGreaterThanOrEqual() {
        ConditionNode node = new ConditionNode("speed", ">=", 80);
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 100))));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 80))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("speed", 60))));
    }

    @Test
    void shouldEvaluateLessThanOrEqual() {
        ConditionNode node = new ConditionNode("speed", "<=", 80);
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 60))));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("speed", 80))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("speed", 100))));
    }

    @Test
    void shouldEvaluateNotEquals() {
        ConditionNode node = new ConditionNode("status", "!=", "ONLINE");
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "OFFLINE"))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "ONLINE"))));
    }

    @Test
    void shouldEvaluateIn() {
        ConditionNode node = new ConditionNode("status", "IN", List.of("ONLINE", "IDLE"));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "ONLINE"))));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "IDLE"))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "OFFLINE"))));
    }

    @Test
    void shouldEvaluateNotIn() {
        ConditionNode node = new ConditionNode("status", "NOT_IN", List.of("OFFLINE", "ERROR"));
        assertTrue(node.evaluate(new EventPayload("v1", Map.of("status", "ONLINE"))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "OFFLINE"))));
        assertFalse(node.evaluate(new EventPayload("v1", Map.of("status", "ERROR"))));
    }

    @Test
    void shouldThrowExceptionForUnsupportedOperator() {
        ConditionNode node = new ConditionNode("speed", "!", 80);
        EventPayload payload = new EventPayload("v1", Map.of("speed", 100));
        assertThrows(IllegalArgumentException.class, () -> node.evaluate(payload));
    }
}
