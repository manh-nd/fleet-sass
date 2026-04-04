package com.fleet.domain.rule.ast;

import java.util.List;

import com.fleet.domain.rule.vo.EventPayload;

public class LogicalNode implements RuleNode {
    private final String operator; // "AND" hoặc "OR"
    private final List<RuleNode> children;

    public LogicalNode(String operator, List<RuleNode> children) {
        this.operator = operator;
        this.children = children;
    }

    @Override
    public boolean evaluate(EventPayload payload) {
        if (children == null || children.isEmpty())
            return true;

        if ("AND".equals(operator)) {
            for (RuleNode child : children) {
                if (!child.evaluate(payload))
                    return false;
            }
            return true;
        } else if ("OR".equals(operator)) {
            for (RuleNode child : children) {
                if (child.evaluate(payload))
                    return true;
            }
            return false;
        }

        throw new IllegalArgumentException("Unknown logical operator: " + operator);
    }
}
