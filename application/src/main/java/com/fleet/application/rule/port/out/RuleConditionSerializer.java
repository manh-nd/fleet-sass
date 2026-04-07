package com.fleet.application.rule.port.out;

import com.fleet.domain.rule.ast.RuleNode;

/**
 * Application-level port for serializing and deserializing the rule condition AST.
 *
 * <p>This port sits in the application layer so that the application services can
 * depend on it without coupling to any infrastructure concern (Jackson, persistence format, etc.).
 * The concrete implementation ({@code RuleAstParser}) lives in the infrastructure layer.</p>
 *
 * <p>The raw string format is an infrastructure decision — currently JSON/JSONB for PostgreSQL,
 * but could be replaced with another format (e.g. Protocol Buffers, YAML) without touching
 * the domain or application layers.</p>
 */
public interface RuleConditionSerializer {

    /**
     * Deserializes a raw string representation into a domain {@link RuleNode} tree.
     *
     * @param raw the serialized condition string (e.g. JSON)
     * @return the root {@link RuleNode}, or {@code null} if input is blank
     * @throws com.fleet.domain.shared.exception.RuleParsingException on parse failure
     */
    RuleNode deserialize(String raw);

    /**
     * Serializes a domain {@link RuleNode} tree to its raw string representation.
     *
     * @param node the root node to serialize
     * @return the serialized string, or {@code null} if node is null
     * @throws com.fleet.domain.shared.exception.RuleParsingException on serialization failure
     */
    String serialize(RuleNode node);
}
