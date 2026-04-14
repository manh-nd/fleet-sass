package com.fleet.infrastructure.adapter.out.db;

import tools.jackson.databind.ObjectMapper;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.ConditionValue;
import com.fleet.domain.rule.ast.LogicalNode;
import com.fleet.domain.rule.ast.LogicalOperator;
import com.fleet.domain.rule.ast.Operator;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.shared.exception.RuleParsingException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleAstParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RuleAstParser parser = new RuleAstParser(objectMapper);

    @Test
    void shouldParseNumericConditionNode() {
        String json = "{\"type\":\"CONDITION\",\"field\":\"speed\",\"operator\":\"gt\",\"value\":80}";
        RuleNode node = parser.parse(json);

        assertInstanceOf(ConditionNode.class, node);
        ConditionNode cond = (ConditionNode) node;
        assertEquals("speed", cond.getField());
        assertEquals(Operator.GT, cond.getOperator());
        assertInstanceOf(ConditionValue.NumericValue.class, cond.getValue());
        assertEquals(80.0, ((ConditionValue.NumericValue) cond.getValue()).number());
    }

    @Test
    void shouldParseBooleanConditionNode() {
        String json = "{\"type\":\"CONDITION\",\"field\":\"cruise_control\",\"operator\":\"eq\",\"value\":false}";
        RuleNode node = parser.parse(json);

        assertInstanceOf(ConditionNode.class, node);
        ConditionNode cond = (ConditionNode) node;
        assertInstanceOf(ConditionValue.BooleanValue.class, cond.getValue());
        assertFalse(((ConditionValue.BooleanValue) cond.getValue()).flag());
    }

    @Test
    void shouldParseConditionNodeWithInOperator() {
        String json = "{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"in\",\"value\":[\"ONLINE\",\"IDLE\"]}";
        RuleNode node = parser.parse(json);

        assertInstanceOf(ConditionNode.class, node);
        ConditionNode cond = (ConditionNode) node;
        assertEquals(Operator.IN, cond.getOperator());
        assertInstanceOf(ConditionValue.ListValue.class, cond.getValue());

        ConditionValue.ListValue listVal = (ConditionValue.ListValue) cond.getValue();
        assertEquals(2, listVal.elements().size());
        assertTrue(listVal.contains("ONLINE"));
        assertTrue(listVal.contains("IDLE"));
    }

    @Test
    void shouldParseLogicalNode() {
        String json = """
                {
                  "type": "LOGICAL",
                  "operator": "AND",
                  "children": [
                    {"type":"CONDITION","field":"speed","operator":"gt","value":80},
                    {"type":"CONDITION","field":"fuel","operator":"lt","value":10}
                  ]
                }
                """;
        RuleNode node = parser.parse(json);

        assertInstanceOf(LogicalNode.class, node);
        LogicalNode logical = (LogicalNode) node;
        assertEquals(LogicalOperator.AND, logical.getOperator());
        assertEquals(2, logical.getChildren().size());
    }

    @Test
    void shouldThrowRuleParsingExceptionForUnknownNodeType() {
        String json = "{\"type\":\"UNKNOWN\",\"field\":\"speed\",\"operator\":\"gt\",\"value\":80}";
        assertThrows(RuleParsingException.class, () -> parser.parse(json));
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
        assertTrue(json.contains("\"operator\":\"gt\""), "operator should be serialized as symbol");
        assertTrue(json.contains("\"value\":80"));
    }

    @Test
    void shouldRoundTripNumericConditionNode() {
        ConditionNode original = new ConditionNode("speed", Operator.GTE, 60);
        String json = parser.serialize(original);
        RuleNode reparsed = parser.parse(json);

        assertInstanceOf(ConditionNode.class, reparsed);
        ConditionNode reparsedCond = (ConditionNode) reparsed;
        assertEquals(Operator.GTE, reparsedCond.getOperator());
        assertEquals("speed", reparsedCond.getField());
        assertInstanceOf(ConditionValue.NumericValue.class, reparsedCond.getValue());
        assertEquals(60.0, ((ConditionValue.NumericValue) reparsedCond.getValue()).number());
    }

    @Test
    void shouldRoundTripListConditionNode() {
        ConditionNode original = new ConditionNode("status", Operator.IN, List.of("A", "B", "C"));
        String json = parser.serialize(original);
        RuleNode reparsed = parser.parse(json);

        assertInstanceOf(ConditionNode.class, reparsed);
        ConditionNode reparsedCond = (ConditionNode) reparsed;
        assertInstanceOf(ConditionValue.ListValue.class, reparsedCond.getValue());
        ConditionValue.ListValue lv = (ConditionValue.ListValue) reparsedCond.getValue();
        assertEquals(3, lv.elements().size());
        assertTrue(lv.contains("A"));
        assertTrue(lv.contains("C"));
    }

    @Test
    void shouldReturnNullForNullNode() {
        assertNull(parser.serialize(null));
    }

    @Test
    void shouldImplementRuleConditionSerializerViaDeserialize() {
        // Verify the port interface is correctly implemented
        String json = "{\"type\":\"CONDITION\",\"field\":\"temperature\",\"operator\":\"gt\",\"value\":37.5}";
        RuleNode node = parser.deserialize(json);
        assertInstanceOf(ConditionNode.class, node);
    }
}
