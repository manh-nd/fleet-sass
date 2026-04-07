package com.fleet.infrastructure.adapter.out.db;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fleet.application.rule.port.out.RuleConditionSerializer;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.ConditionValue;
import com.fleet.domain.rule.ast.LogicalNode;
import com.fleet.domain.rule.ast.LogicalOperator;
import com.fleet.domain.rule.ast.Operator;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.shared.exception.RuleParsingException;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Infrastructure implementation of {@link RuleConditionSerializer}.
 *
 * <p>Handles bidirectional mapping between the domain AST ({@link RuleNode}) and the
 * JSON format stored in PostgreSQL JSONB columns.</p>
 *
 * <p>The {@code "type"} discriminator ("CONDITION" / "LOGICAL") and operator symbol strings
 * are infrastructure concerns — they live here, not in the domain model.</p>
 */
@Component
@RequiredArgsConstructor
public class RuleAstParser implements RuleConditionSerializer {

    private final ObjectMapper objectMapper;

    // ---- RuleConditionSerializer implementation ----

    @Override
    public RuleNode deserialize(String raw) {
        return parse(raw);
    }

    @Override
    public String serialize(RuleNode node) {
        if (node == null) return null;
        try {
            return objectMapper.writeValueAsString(buildJson(node));
        } catch (Exception e) {
            throw new RuleParsingException("Failed to serialize AST to JSON: " + e.getMessage(), e);
        }
    }

    // ---- Public parse alias (used by RuleController and tests) ----

    /**
     * Parses a JSON string into a domain {@link RuleNode} tree.
     *
     * @throws RuleParsingException if the JSON is malformed or contains an unknown node type
     */
    public RuleNode parse(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) return null;
        try {
            JsonNode rootJson = objectMapper.readTree(jsonString);
            return buildNode(rootJson);
        } catch (RuleParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new RuleParsingException("Failed to parse conditions JSON into AST: " + e.getMessage(), e);
        }
    }

    // ---- Deserialization: JSON → Domain ----

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
            String field    = jsonNode.get("field").asString();
            Operator operator = Operator.fromSymbol(jsonNode.get("operator").asString());
            ConditionValue conditionValue = extractConditionValue(jsonNode.get("value"));
            return new ConditionNode(field, operator, conditionValue);
        }

        throw new RuleParsingException("Unknown node type in conditions JSON: " + type);
    }

    private ConditionValue extractConditionValue(JsonNode valueNode) {
        if (valueNode.isNumber())  return ConditionValue.NumericValue.of(valueNode.numberValue());
        if (valueNode.isBoolean()) return new ConditionValue.BooleanValue(valueNode.booleanValue());
        if (valueNode.isArray()) {
            List<ConditionValue> elements = new ArrayList<>();
            for (JsonNode element : valueNode) {
                elements.add(extractConditionValue(element));
            }
            return new ConditionValue.ListValue(elements);
        }
        return new ConditionValue.StringValue(valueNode.asString());
    }

    // ---- Serialization: Domain → JSON ----

    private ObjectNode buildJson(RuleNode node) {
        ObjectNode json = objectMapper.createObjectNode();

        if (node instanceof ConditionNode c) {
            json.put("type", "CONDITION");
            json.put("field", c.getField());
            json.put("operator", c.getOperator().getSymbol());
            writeConditionValue(json, c.getValue());

        } else if (node instanceof LogicalNode l) {
            json.put("type", "LOGICAL");
            json.put("operator", l.getOperator().name());
            ArrayNode childrenNode = json.putArray("children");
            for (RuleNode child : l.getChildren()) {
                childrenNode.add(buildJson(child));
            }
        } else {
            throw new RuleParsingException("Unknown RuleNode type: " + node.getClass().getName());
        }

        return json;
    }

    private void writeConditionValue(ObjectNode json, ConditionValue value) {
        switch (value) {
            case ConditionValue.NumericValue n  -> json.put("value", n.number());
            case ConditionValue.BooleanValue b  -> json.put("value", b.flag());
            case ConditionValue.StringValue s   -> json.put("value", s.text());
            case ConditionValue.ListValue lv -> {
                ArrayNode arr = json.putArray("value");
                for (ConditionValue element : lv.elements()) {
                    switch (element) {
                        case ConditionValue.NumericValue n -> arr.add(n.number());
                        case ConditionValue.BooleanValue b -> arr.add(b.flag());
                        case ConditionValue.StringValue s  -> arr.add(s.text());
                        case ConditionValue.ListValue ignored ->
                                throw new RuleParsingException("Nested lists are not supported in condition values");
                    }
                }
            }
        }
    }
}
