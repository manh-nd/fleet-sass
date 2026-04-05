package com.fleet.infrastructure.adapter.out.db;

import tools.jackson.databind.ObjectMapper;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.LogicalNode;
import com.fleet.domain.rule.ast.LogicalOperator;
import com.fleet.domain.rule.ast.Operator;
import com.fleet.domain.rule.ast.RuleNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleAstParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RuleAstParser parser = new RuleAstParser(objectMapper);

    @Test
    void shouldParseConditionNode() {
        String json = "{\"type\":\"CONDITION\",\"field\":\"speed\",\"operator\":\">\",\"value\":80}";
        RuleNode node = parser.parse(json);

        assertInstanceOf(ConditionNode.class, node);
        ConditionNode conditionNode = (ConditionNode) node;
        assertEquals("speed", conditionNode.getField());
        assertEquals(Operator.GT, conditionNode.getOperator());
        assertEquals(80, ((Number) conditionNode.getValue()).intValue());
    }

    @Test
    void shouldParseConditionNodeWithInOperator() {
        String json = "{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"IN\",\"value\":[\"ONLINE\", \"IDLE\"]}";
        RuleNode node = parser.parse(json);

        assertInstanceOf(ConditionNode.class, node);
        ConditionNode conditionNode = (ConditionNode) node;
        assertEquals("status", conditionNode.getField());
        assertEquals(Operator.IN, conditionNode.getOperator());
        assertInstanceOf(List.class, conditionNode.getValue());
        List<?> values = (List<?>) conditionNode.getValue();
        assertEquals(2, values.size());
        assertTrue(values.contains("ONLINE"));
        assertTrue(values.contains("IDLE"));
    }

    @Test
    void shouldParseLogicalNode() {
        String json = """
                {
                  "type": "LOGICAL",
                  "operator": "AND",
                  "children": [
                    {"type":"CONDITION","field":"speed","operator":">","value":80},
                    {"type":"CONDITION","field":"fuel","operator":"<","value":10}
                  ]
                }
                """;
        RuleNode node = parser.parse(json);

        assertInstanceOf(LogicalNode.class, node);
        LogicalNode logicalNode = (LogicalNode) node;
        assertEquals(LogicalOperator.AND, logicalNode.getOperator());
        assertEquals(2, logicalNode.getChildren().size());
    }

    @Test
    void shouldThrowForUnknownNodeType() {
        String json = "{\"type\":\"UNKNOWN\",\"field\":\"speed\",\"operator\":\">\",\"value\":80}";
        assertThrows(RuntimeException.class, () -> parser.parse(json));
    }

    @Test
    void shouldReturnNullForBlankInput() {
        assertNull(parser.parse(null));
        assertNull(parser.parse(""));
        assertNull(parser.parse("   "));
    }

    @Test
    void shouldSerializeConditionNode() {
        ConditionNode node = new ConditionNode("speed", Operator.GT, 80);
        String json = parser.serialize(node);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"CONDITION\""), "type discriminator must be present");
        assertTrue(json.contains("\"field\":\"speed\""));
        assertTrue(json.contains("\"operator\":\">\""), "operator should be serialized as symbol, not enum name");
        assertTrue(json.contains("\"value\":80"));
    }

    @Test
    void shouldRoundTripConditionNodeThroughSerializeAndParse() {
        ConditionNode original = new ConditionNode("speed", Operator.GTE, 60);
        String json = parser.serialize(original);
        RuleNode reparsed = parser.parse(json);

        assertInstanceOf(ConditionNode.class, reparsed);
        ConditionNode reparsedCondition = (ConditionNode) reparsed;
        assertEquals(Operator.GTE, reparsedCondition.getOperator());
        assertEquals("speed", reparsedCondition.getField());
        assertEquals(60.0, ((Number) reparsedCondition.getValue()).doubleValue());
    }

    @Test
    void shouldReturnNullForNullNode() {
        assertNull(parser.serialize(null));
    }
}
