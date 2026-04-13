package com.fleet.infrastructure.adapter.out.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.DeliveryStatus;
import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.notification.model.NotificationLog;
import com.fleet.domain.notification.port.out.NotificationLogRepositoryPort;
import com.fleet.domain.notification.vo.NotificationId;
import com.fleet.domain.shared.pagination.CursorPage;

import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL implementation of {@link NotificationLogRepositoryPort}.
 *
 * <p>
 * Uses keyset cursor pagination on {@code id} (UUID), consistent with
 * {@link PostgresRuleRepositoryAdapter}.
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class PostgresNotificationLogAdapter implements NotificationLogRepositoryPort {

    private final JdbcClient jdbcClient;

    @Override
    public void save(NotificationLog log) {
        jdbcClient.sql("""
                INSERT INTO notification_log
                    (id, tenant_id, service_id, channel, recipient, rendered_content,
                     status, fail_reason, attempts, created_at)
                VALUES
                    (:id, :tenantId, :serviceId, :channel, :recipient, :content,
                     :status, :failReason, :attempts, :createdAt)
                """)
                .param("id", log.getId().value())
                .param("tenantId", log.getTenantId().value())
                .param("serviceId", log.getServiceId().value())
                .param("channel", log.getChannel().name())
                .param("recipient", log.getRecipient())
                .param("content", log.getRenderedContent())
                .param("status", log.getStatus().name())
                .param("failReason", log.getFailReason())
                .param("attempts", log.getAttempts())
                .param("createdAt", Timestamp.from(log.getCreatedAt()))
                .update();
    }

    @Override
    public void update(NotificationLog log) {
        jdbcClient.sql("""
                UPDATE notification_log
                SET status = :status, fail_reason = :failReason, attempts = :attempts
                WHERE id = :id
                """)
                .param("id", log.getId().value())
                .param("status", log.getStatus().name())
                .param("failReason", log.getFailReason())
                .param("attempts", log.getAttempts())
                .update();
    }

    @Override
    public CursorPage<NotificationLog> findByTenant(TenantId tenantId, String cursor, int limit) {
        int fetchSize = limit + 1;
        List<NotificationLog> rows;

        if (cursor == null || cursor.isBlank()) {
            rows = jdbcClient.sql("""
                    SELECT id, tenant_id, service_id, channel, recipient, rendered_content,
                           status, fail_reason, attempts, created_at
                    FROM notification_log
                    WHERE tenant_id = :tenantId
                    ORDER BY id
                    LIMIT :limit
                    """)
                    .param("tenantId", tenantId.value())
                    .param("limit", fetchSize)
                    .query((rs, n) -> mapRow(rs))
                    .list();
        } else {
            UUID cursorId = decodeCursor(cursor);
            rows = jdbcClient.sql("""
                    SELECT id, tenant_id, service_id, channel, recipient, rendered_content,
                           status, fail_reason, attempts, created_at
                    FROM notification_log
                    WHERE tenant_id = :tenantId AND id > :cursorId
                    ORDER BY id
                    LIMIT :limit
                    """)
                    .param("tenantId", tenantId.value())
                    .param("cursorId", cursorId)
                    .param("limit", fetchSize)
                    .query((rs, n) -> mapRow(rs))
                    .list();
        }

        boolean hasMore = rows.size() == fetchSize;
        List<NotificationLog> page = hasMore ? rows.subList(0, limit) : rows;
        String nextCursor = hasMore ? encodeCursor(page.get(page.size() - 1).getId().value()) : null;
        return new CursorPage<>(page, nextCursor, hasMore);
    }

    @Override
    public Optional<NotificationLog> findById(NotificationId id) {
        return jdbcClient.sql("""
                SELECT id, tenant_id, service_id, channel, recipient, rendered_content,
                       status, fail_reason, attempts, created_at
                FROM notification_log WHERE id = :id
                """)
                .param("id", id.value())
                .query((rs, n) -> mapRow(rs))
                .optional();
    }

    // ---- Private helpers ----

    private NotificationLog mapRow(ResultSet rs) throws SQLException {
        var status = DeliveryStatus.valueOf(rs.getString("status"));
        var createdAtTimestamp = rs.getTimestamp("created_at");
        var createdAt = createdAtTimestamp != null ? createdAtTimestamp.toInstant() : null;
        return NotificationLog.reconstitute(
                new NotificationId(rs.getObject("id", UUID.class)),
                new TenantId(rs.getObject("tenant_id", UUID.class)),
                new ServiceId(rs.getString("service_id")),
                ChannelType.fromString(rs.getString("channel")),
                rs.getString("recipient"),
                rs.getString("rendered_content"),
                status,
                rs.getString("fail_reason"),
                rs.getInt("attempts"),
                createdAt);
    }

    private String encodeCursor(UUID id) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(id.toString().getBytes());
    }

    private UUID decodeCursor(String cursor) {
        try {
            return UUID.fromString(new String(Base64.getUrlDecoder().decode(cursor)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid pagination cursor: " + cursor, e);
        }
    }
}
