package com.fleet.infrastructure.adapter.out.db;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.port.out.RuleRepositoryPort;
import com.fleet.domain.rule.vo.RuleId;

import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL implementation of {@link RuleRepositoryPort}.
 * Uses {@link JdbcClient} for persistence and handles rule conditions as JSONB.
 */
@Repository
@RequiredArgsConstructor
public class PostgresRuleRepositoryAdapter implements RuleRepositoryPort {

    private final JdbcClient jdbcClient;
    private final RuleAstParser astParser;

    @Override
    public List<NotificationRule> findActiveRules(TenantId tenantId, String eventType) {
        String sql = """
                SELECT id, tenant_id, service_id, event_type, conditions_json, cooldown_minutes, is_active
                FROM notification_rules
                WHERE tenant_id = :tenantId
                AND event_type = :eventType
                AND is_active = true
                """;

        return jdbcClient.sql(sql)
                .param("tenantId", tenantId.value())
                .param("eventType", eventType)
                .query((rs, rowNum) -> {
                    RuleId id = new RuleId(rs.getObject("id", UUID.class));
                    TenantId tId = new TenantId(rs.getObject("tenant_id", UUID.class));
                    ServiceId sId = new ServiceId(rs.getString("service_id"));
                    String eType = rs.getString("event_type");
                    boolean isActive = rs.getBoolean("is_active");
                    String jsonbString = rs.getString("conditions_json");
                    int cooldownMinutes = rs.getInt("cooldown_minutes");

                    RuleNode rootCondition = astParser.parse(jsonbString);

                    return new NotificationRule(id, tId, sId, eType, rootCondition, isActive, cooldownMinutes);
                })
                .list();
    }

    @Override
    public void save(NotificationRule rule) {
        String jsonbString = astParser.serialize(rule.getConditionRoot());
        String sql = """
            INSERT INTO notification_rules (id, tenant_id, service_id, event_type, conditions_json, cooldown_minutes, is_active)
            VALUES (:id, :tenantId, :serviceId, :eventType, CAST(:conditions AS jsonb), :cooldownMinutes, :isActive)
        """;
        jdbcClient.sql(sql)
            .param("id", rule.getId().value())
            .param("tenantId", rule.getTenantId().value())
            .param("serviceId", rule.getServiceId().value())
            .param("eventType", rule.getEventType())
            .param("conditions", jsonbString)
            .param("cooldownMinutes", rule.getCooldownMinutes())
            .param("isActive", rule.isActive())
            .update();
    }

    @Override
    public void update(NotificationRule rule) {
        String jsonbString = astParser.serialize(rule.getConditionRoot());
        String sql = """
            UPDATE notification_rules
            SET service_id = :serviceId, event_type = :eventType, conditions_json = CAST(:conditions AS jsonb),
                cooldown_minutes = :cooldownMinutes, is_active = :isActive
            WHERE id = :id AND tenant_id = :tenantId
        """;
        jdbcClient.sql(sql)
            .param("id", rule.getId().value())
            .param("tenantId", rule.getTenantId().value())
            .param("serviceId", rule.getServiceId().value())
            .param("eventType", rule.getEventType())
            .param("conditions", jsonbString)
            .param("cooldownMinutes", rule.getCooldownMinutes())
            .param("isActive", rule.isActive())
            .update();
    }

    @Override
    public void delete(RuleId ruleId, TenantId tenantId) {
        String sql = "DELETE FROM notification_rules WHERE id = :id AND tenant_id = :tenantId";
        jdbcClient.sql(sql)
            .param("id", ruleId.value())
            .param("tenantId", tenantId.value())
            .update();
    }
}
