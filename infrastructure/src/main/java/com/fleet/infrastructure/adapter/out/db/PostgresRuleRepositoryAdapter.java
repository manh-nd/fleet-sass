package com.fleet.infrastructure.adapter.out.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
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
import com.fleet.domain.shared.pagination.CursorPage;

import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL implementation of {@link RuleRepositoryPort}.
 *
 * <p>
 * Uses {@link JdbcClient} for persistence and handles rule conditions as JSONB.
 * Cursor-based pagination uses a UUID cursor encoded as Base64 for opacity.
 * </p>
 *
 * <p>
 * The cursor encodes the last returned row's UUID. Subsequent queries use
 * {@code WHERE id > :cursor ORDER BY id} to fetch the next page
 * deterministically.
 * </p>
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
                ORDER BY id
                """;
        return jdbcClient.sql(sql)
                .param("tenantId", tenantId.value())
                .param("eventType", eventType)
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public CursorPage<NotificationRule> findAllByTenant(TenantId tenantId, String cursor, int limit) {
        // Fetch limit+1 rows; if we get limit+1 back there are more pages
        int fetchSize = limit + 1;
        List<NotificationRule> rows;

        if (cursor == null || cursor.isBlank()) {
            // First page — no cursor filter
            String sql = """
                    SELECT id, tenant_id, service_id, event_type, conditions_json, cooldown_minutes, is_active
                    FROM notification_rules
                    WHERE tenant_id = :tenantId
                    ORDER BY id
                    LIMIT :limit
                    """;
            rows = jdbcClient.sql(sql)
                    .param("tenantId", tenantId.value())
                    .param("limit", fetchSize)
                    .query((rs, rowNum) -> mapRow(rs))
                    .list();
        } else {
            // Subsequent pages — decode cursor UUID and use keyset pagination
            UUID cursorId = decodeCursor(cursor);
            String sql = """
                    SELECT id, tenant_id, service_id, event_type, conditions_json, cooldown_minutes, is_active
                    FROM notification_rules
                    WHERE tenant_id = :tenantId
                      AND id > :cursorId
                    ORDER BY id
                    LIMIT :limit
                    """;
            rows = jdbcClient.sql(sql)
                    .param("tenantId", tenantId.value())
                    .param("cursorId", cursorId)
                    .param("limit", fetchSize)
                    .query((rs, rowNum) -> mapRow(rs))
                    .list();
        }

        boolean hasMore = rows.size() == fetchSize;
        List<NotificationRule> page = hasMore ? rows.subList(0, limit) : rows;
        String nextCursor = hasMore ? encodeCursor(page.get(page.size() - 1).getId().value()) : null;
        return new CursorPage<>(page, nextCursor, hasMore);
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
                SET service_id = :serviceId,
                    event_type = :eventType,
                    conditions_json = CAST(:conditions AS jsonb),
                    cooldown_minutes = :cooldownMinutes,
                    is_active = :isActive,
                    updated_at = CURRENT_TIMESTAMP
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

    // ---- Private helpers ----

    private NotificationRule mapRow(ResultSet rs) throws SQLException {
        RuleId id = new RuleId(rs.getObject("id", UUID.class));
        TenantId tId = new TenantId(rs.getObject("tenant_id", UUID.class));
        ServiceId sId = new ServiceId(rs.getString("service_id"));
        String eType = rs.getString("event_type");
        boolean isActive = rs.getBoolean("is_active");
        String jsonbString = rs.getString("conditions_json");
        int cooldownMinutes = rs.getInt("cooldown_minutes");
        RuleNode rootCondition = astParser.parse(jsonbString);
        return NotificationRule.reconstitute(id, tId, sId, eType, rootCondition, isActive, cooldownMinutes);
    }

    /** Encodes a UUID as a URL-safe Base64 string for use as an opaque cursor. */
    private String encodeCursor(UUID id) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(id.toString().getBytes());
    }

    /** Decodes a cursor string back to a UUID. */
    private UUID decodeCursor(String cursor) {
        try {
            return UUID.fromString(new String(Base64.getUrlDecoder().decode(cursor)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid pagination cursor: " + cursor, e);
        }
    }
}
