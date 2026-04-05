package com.fleet.domain.rule.ast;

import com.fleet.domain.rule.vo.EventPayload;

import java.util.List;

/**
 * A composite node in the rule AST that combines child nodes
 * with a logical operator (AND / OR).
 *
 * <p>An empty or null children list is <strong>not</strong> allowed —
 * a {@link LogicalNode} with no children is a configuration error and
 * will fail fast at construction time.</p>
 */
public class LogicalNode implements RuleNode {

    private final LogicalOperator operator;
    private final List<RuleNode> children;

    public LogicalNode(LogicalOperator operator, List<RuleNode> children) {
        if (operator == null) {
            throw new IllegalArgumentException("LogicalNode operator must not be null");
        }
        if (children == null || children.isEmpty()) {
            throw new IllegalStateException(
                    "LogicalNode must have at least one child — an empty condition tree is a configuration error");
        }
        this.operator = operator;
        this.children = List.copyOf(children); // defensive copy — immutable
    }

    public LogicalOperator getOperator() {
        return operator;
    }

    public List<RuleNode> getChildren() {
        return children;
    }

    @Override
    public boolean evaluate(EventPayload payload) {
        return switch (operator) {
            case AND -> children.stream().allMatch(child -> child.evaluate(payload));
            case OR  -> children.stream().anyMatch(child -> child.evaluate(payload));
        };
    }
}
