package com.fleet.infrastructure.adapter.out.db;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.LogicalNode;
import com.fleet.domain.rule.ast.RuleNode;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class RuleAstParser {
    private final ObjectMapper objectMapper;

    public RuleAstParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
            String operator = jsonNode.get("operator").asString();
            List<RuleNode> children = new ArrayList<>();

            for (JsonNode childJson : jsonNode.get("children")) {
                children.add(buildNode(childJson));
            }
            return new LogicalNode(operator, children);

        } else if ("CONDITION".equals(type)) {
            String field = jsonNode.get("field").asString();
            String operator = jsonNode.get("operator").asString();

            JsonNode valueNode = jsonNode.get("value");
            Object value = extractValue(valueNode);

            return new ConditionNode(field, operator, value);
        }

        throw new IllegalArgumentException("Unknown node type in JSON: " + type);
    }

    private Object extractValue(JsonNode valueNode) {
        if (valueNode.isNumber())
            return valueNode.numberValue();
        if (valueNode.isBoolean())
            return valueNode.booleanValue();
        return valueNode.asString();
    }
}
