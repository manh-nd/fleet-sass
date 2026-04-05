package com.fleet.infrastructure.adapter.out.db;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.LogicalNode;
import com.fleet.domain.rule.ast.LogicalOperator;
import com.fleet.domain.rule.ast.Operator;
import com.fleet.domain.rule.ast.RuleNode;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Parses a JSON representation of a rule AST into domain {@link RuleNode} objects,
 * and serializes domain nodes back to JSON for persistence.
 *
 * <p>The {@code "type"} field in JSON ("CONDITION" / "LOGICAL") and the operator
 * symbol string are infrastructure concepts used for polymorphic (de)serialization.
 * They do not belong on domain objects — this class is the mapping boundary.</p>
 */
@Component
public class RuleAstParser {

    private final ObjectMapper objectMapper;

    public RuleAstParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ---- Deserialization: JSON → Domain ----

    public RuleNode parse(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return null;
        }
        try {
            JsonNode rootJson = objectMapper.readTree(jsonString);
            return buildNode(rootJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse conditions_json to AST", e);
        }
    }

    private RuleNode buildNode(JsonNode jsonNode) {
        String type = jsonNode.get("type").asString();

        if ("LOGICAL".equals(type)) {
            LogicalOperator operator = LogicalOperator.fromString(jsonNode.get("operator").asString());
            List<RuleNode> children = new ArrayList<>();

            for (JsonNode childJson : jsonNode.get("children")) {
                children.add(buildNode(childJson));
            }
            return new LogicalNode(operator, children);

        } else if ("CONDITION".equals(type)) {
            String field = jsonNode.get("field").asString();
            Operator operator = Operator.fromSymbol(jsonNode.get("operator").asString());

            JsonNode valueNode = jsonNode.get("value");
            Object value = extractValue(valueNode);

            return new ConditionNode(field, operator, value);
        }

        throw new IllegalArgumentException("Unknown node type in JSON: " + type);
    }

    private Object extractValue(JsonNode valueNode) {
        if (valueNode.isNumber()) {
            return valueNode.numberValue();
        }
        if (valueNode.isBoolean()) {
            return valueNode.booleanValue();
        }
        if (valueNode.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode element : valueNode) {
                list.add(extractValue(element));
            }
            return list;
        }
        return valueNode.asString();
    }

    // ---- Serialization: Domain → JSON ----

    /**
     * Serializes a {@link RuleNode} to the canonical JSON format used for DB storage.
     * This method handles the {@code type} discriminator and operator symbol mapping
     * explicitly, keeping the domain objects clean of persistence concerns.
     */
    public String serialize(RuleNode node) {
        if (node == null) {
            return null;
        }
        try {
            ObjectNode json = buildJson(node);
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize AST to JSON", e);
        }
    }

    private ObjectNode buildJson(RuleNode node) {
        ObjectNode json = objectMapper.createObjectNode();

        if (node instanceof ConditionNode c) {
            json.put("type", "CONDITION");
            json.put("field", c.getField());
            json.put("operator", c.getOperator().getSymbol()); // serialize symbol, not enum name
            Object val = c.getValue();
            if (val instanceof Number n) {
                json.put("value", n.doubleValue());
            } else if (val instanceof Boolean b) {
                json.put("value", b);
            } else if (val instanceof List<?> list) {
                ArrayNode arr = json.putArray("value");
                for (Object item : list) {
                    arr.add(String.valueOf(item));
                }
            } else {
                json.put("value", String.valueOf(val));
            }

        } else if (node instanceof LogicalNode l) {
            json.put("type", "LOGICAL");
            json.put("operator", l.getOperator().name()); // AND / OR
            ArrayNode childrenNode = json.putArray("children");
            for (RuleNode child : l.getChildren()) {
                childrenNode.add(buildJson(child));
            }
        } else {
            throw new IllegalArgumentException("Unknown RuleNode type: " + node.getClass().getName());
        }

        return json;
    }
}
