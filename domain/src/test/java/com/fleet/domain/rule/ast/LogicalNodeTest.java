package com.fleet.domain.rule.ast;

import com.fleet.domain.rule.vo.EventPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogicalNodeTest {

    @Test
    void shouldEvaluateAndLogic() {
        ConditionNode speedHigh = new ConditionNode("speed", Operator.GT, 80);
        ConditionNode engineHot = new ConditionNode("temp", Operator.GT, 100);
        LogicalNode andNode = new LogicalNode(LogicalOperator.AND, List.of(speedHigh, engineHot));

        assertTrue(andNode.evaluate(new EventPayload("v1", Map.of("speed", 100, "temp", 120))));
        assertFalse(andNode.evaluate(new EventPayload("v1", Map.of("speed", 100, "temp", 80))));
    }

    @Test
    void shouldEvaluateOrLogic() {
        ConditionNode speedHigh = new ConditionNode("speed", Operator.GT, 80);
        ConditionNode engineHot = new ConditionNode("temp", Operator.GT, 100);
        LogicalNode orNode = new LogicalNode(LogicalOperator.OR, List.of(speedHigh, engineHot));

        assertTrue(orNode.evaluate(new EventPayload("v1", Map.of("speed", 100, "temp", 80))));
        assertFalse(orNode.evaluate(new EventPayload("v1", Map.of("speed", 60, "temp", 80))));
    }

    @Test
    void shouldThrowWhenChildrenIsEmpty() {
        // Empty children list is a configuration error — must fail fast
        assertThrows(IllegalStateException.class,
                () -> new LogicalNode(LogicalOperator.AND, List.of()));
    }

    @Test
    void shouldThrowWhenChildrenIsNull() {
        assertThrows(IllegalStateException.class,
                () -> new LogicalNode(LogicalOperator.AND, null));
    }

    @Test
    void shouldThrowWhenOperatorIsNull() {
        ConditionNode node = new ConditionNode("speed", Operator.GT, 80);
        assertThrows(IllegalArgumentException.class,
                () -> new LogicalNode(null, List.of(node)));
    }

    @Test
    void shouldEvaluateNestedAndInsideOr() {
        // (speed > 80 AND temp > 100) OR (fuel < 10)
        ConditionNode speedHigh = new ConditionNode("speed", Operator.GT, 80);
        ConditionNode engineHot = new ConditionNode("temp", Operator.GT, 100);
        LogicalNode innerAnd = new LogicalNode(LogicalOperator.AND, List.of(speedHigh, engineHot));

        ConditionNode lowFuel = new ConditionNode("fuel", Operator.LT, 10);
        LogicalNode outerOr = new LogicalNode(LogicalOperator.OR, List.of(innerAnd, lowFuel));

        // Triggered by compound AND condition
        assertTrue(outerOr.evaluate(new EventPayload("v1", Map.of("speed", 100, "temp", 120, "fuel", 50))));
        // Triggered by fuel alone
        assertTrue(outerOr.evaluate(new EventPayload("v1", Map.of("speed", 60, "temp", 80, "fuel", 5))));
        // Neither condition met
        assertFalse(outerOr.evaluate(new EventPayload("v1", Map.of("speed", 60, "temp", 80, "fuel", 50))));
    }
}
