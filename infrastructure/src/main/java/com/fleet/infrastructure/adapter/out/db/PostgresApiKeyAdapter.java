package com.fleet.infrastructure.adapter.out.db;

import com.fleet.domain.entitlement.model.ApiKey;
import com.fleet.domain.entitlement.port.out.ApiKeyRepositoryPort;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL + Redis-cached implementation of {@link ApiKeyRepositoryPort}.
 *
 * <p>{@link Cacheable} caches the {@code findByHash} result in Redis under
 * the {@code "apiKeys"} cache. The TTL is configured in {@code application.yml}
 * under {@code spring.cache.redis.time-to-live}.</p>
 */
@Repository
@RequiredArgsConstructor
public class PostgresApiKeyAdapter implements ApiKeyRepositoryPort {

    private final JdbcClient jdbcClient;

    @Override
    @Cacheable(value = "apiKeys", key = "#keyHash", unless = "#result == null || !#result.isPresent()")
    public Optional<ApiKey> findByHash(String keyHash) {
        return jdbcClient.sql("""
                SELECT id, tenant_id, service_id, key_hash, description, active, created_at, expires_at
                FROM api_keys
                WHERE key_hash = :keyHash AND active = true
                  AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
                """)
                .param("keyHash", keyHash)
                .query((rs, n) -> mapRow(rs))
                .optional();
    }

    @Override
    public void save(ApiKey apiKey) {
        jdbcClient.sql("""
                INSERT INTO api_keys (id, tenant_id, service_id, key_hash, description, active, created_at, expires_at)
                VALUES (:id, :tenantId, :serviceId, :keyHash, :description, :active, :createdAt, :expiresAt)
                """)
                .param("id",          apiKey.getId())
                .param("tenantId",    apiKey.getTenantId().value())
                .param("serviceId",   apiKey.getServiceId().value())
                .param("keyHash",     apiKey.getKeyHash())
                .param("description", apiKey.getDescription())
                .param("active",      apiKey.isActive())
                .param("createdAt",   Timestamp.from(apiKey.getCreatedAt()))
                .param("expiresAt",   apiKey.getExpiresAt() != null ? Timestamp.from(apiKey.getExpiresAt()) : null)
                .update();
    }

    @Override
    @CacheEvict(value = "apiKeys", key = "#apiKey.keyHash")
    public void revoke(ApiKey apiKey) {
        jdbcClient.sql("UPDATE api_keys SET active = false WHERE id = :id")
                .param("id", apiKey.getId())
                .update();
    }

    @Override
    public List<ApiKey> findByTenant(TenantId tenantId) {
        return jdbcClient.sql("""
                SELECT id, tenant_id, service_id, key_hash, description, active, created_at, expires_at
                FROM api_keys WHERE tenant_id = :tenantId ORDER BY created_at DESC
                """)
                .param("tenantId", tenantId.value())
                .query((rs, n) -> mapRow(rs))
                .list();
    }

    private ApiKey mapRow(ResultSet rs) throws SQLException {
        var createdAtTs = rs.getTimestamp("created_at");
        var expiresAtTs = rs.getTimestamp("expires_at");
        return ApiKey.reconstitute(
                rs.getObject("id", UUID.class),
                new TenantId(rs.getObject("tenant_id", UUID.class)),
                new ServiceId(rs.getString("service_id")),
                rs.getString("key_hash"),
                rs.getString("description"),
                rs.getBoolean("active"),
                createdAtTs != null ? createdAtTs.toInstant() : null,
                expiresAtTs != null ? expiresAtTs.toInstant() : null);
    }
}
