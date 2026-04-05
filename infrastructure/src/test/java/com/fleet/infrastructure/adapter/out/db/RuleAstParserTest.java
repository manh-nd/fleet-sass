package com.fleet.infrastructure.adapter.out.db;

import tools.jackson.databind.ObjectMapper;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.LogicalNode;
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

        assertTrue(node instanceof ConditionNode);
        ConditionNode conditionNode = (ConditionNode) node;
        assertEquals("speed", conditionNode.getField());
        assertEquals(">", conditionNode.getOperator());
        assertEquals(80, ((Number) conditionNode.getValue()).intValue());
    }

    @Test
    void shouldParseConditionNodeWithInOperator() {
        String json = "{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"IN\",\"value\":[\"ONLINE\", \"IDLE\"]}";
        RuleNode node = parser.parse(json);

        assertTrue(node instanceof ConditionNode);
        ConditionNode conditionNode = (ConditionNode) node;
        assertEquals("status", conditionNode.getField());
        assertEquals("IN", conditionNode.getOperator());
        assertTrue(conditionNode.getValue() instanceof List);
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

        assertTrue(node instanceof LogicalNode);
        LogicalNode logicalNode = (LogicalNode) node;
        assertEquals("AND", logicalNode.getOperator());
        assertEquals(2, logicalNode.getChildren().size());
    }

    @Test
    void shouldSerializeNode() {
        ConditionNode node = new ConditionNode("speed", ">", 80);
        String json = parser.serialize(node);

        assertTrue(json.contains("\"type\":\"CONDITION\""));
        assertTrue(json.contains("\"field\":\"speed\""));
        assertTrue(json.contains("\"operator\":\">\""));
        assertTrue(json.contains("\"value\":80"));
    }
}
