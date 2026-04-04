package com.fleet.domain.rule.ast;

import com.fleet.domain.rule.vo.EventPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogicalNodeTest {

    @Test
    void shouldEvaluateAndLogic() {
        ConditionNode speedHigh = new ConditionNode("speed", ">", 80);
        ConditionNode engineHot = new ConditionNode("temp", ">", 100);
        LogicalNode andNode = new LogicalNode("AND", List.of(speedHigh, engineHot));

        EventPayload payload = new EventPayload("v1", Map.of("speed", 100, "temp", 120));
        assertTrue(andNode.evaluate(payload));

        payload = new EventPayload("v1", Map.of("speed", 100, "temp", 80));
        assertFalse(andNode.evaluate(payload));
    }

    @Test
    void shouldEvaluateOrLogic() {
        ConditionNode speedHigh = new ConditionNode("speed", ">", 80);
        ConditionNode engineHot = new ConditionNode("temp", ">", 100);
        LogicalNode orNode = new LogicalNode("OR", List.of(speedHigh, engineHot));

        EventPayload payload = new EventPayload("v1", Map.of("speed", 100, "temp", 80));
        assertTrue(orNode.evaluate(payload));

        payload = new EventPayload("v1", Map.of("speed", 60, "temp", 80));
        assertFalse(orNode.evaluate(payload));
    }

    @Test
    void shouldReturnTrueForEmptyChildren() {
        LogicalNode andNode = new LogicalNode("AND", List.of());
        EventPayload payload = new EventPayload("v1", Map.of());
        assertTrue(andNode.evaluate(payload));
    }

    @Test
    void shouldThrowExceptionForUnsupportedOperator() {
        ConditionNode speedHigh = new ConditionNode("speed", ">", 80);
        LogicalNode xorNode = new LogicalNode("XOR", List.of(speedHigh));
        EventPayload payload = new EventPayload("v1", Map.of("speed", 100));
        assertThrows(IllegalArgumentException.class, () -> xorNode.evaluate(payload));
    }
}
