package com.fleet.infrastructure.adapter.out.db;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.Template;
import com.fleet.domain.notification.port.out.TemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PostgreSQL implementation of {@link TemplateRepositoryPort}.
 *
 * <p>Template content is persisted as a JSONB column where keys are IETF BCP 47 language
 * tags (e.g. {@code "en"}, {@code "vi"}) and values are template body strings.</p>
 */
@Repository
@RequiredArgsConstructor
public class PostgresTemplateAdapter implements TemplateRepositoryPort {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void save(Template template) {
        jdbcClient.sql("""
                INSERT INTO notification_templates
                    (id, tenant_id, service_id, template_key, content, version, created_at, updated_at)
                VALUES
                    (:id, :tenantId, :serviceId, :templateKey, CAST(:content AS jsonb), :version, :createdAt, :updatedAt)
                """)
                .param("id",          template.getId())
                .param("tenantId",    template.getTenantId().value())
                .param("serviceId",   template.getServiceId().value())
                .param("templateKey", template.getTemplateKey())
                .param("content",     toJson(template.getContent()))
                .param("version",     template.getVersion())
                .param("createdAt",   Timestamp.from(template.getCreatedAt()))
                .param("updatedAt",   Timestamp.from(template.getUpdatedAt()))
                .update();
    }

    @Override
    @SneakyThrows
    public void update(Template template) {
        jdbcClient.sql("""
                UPDATE notification_templates
                SET content = CAST(:content AS jsonb), version = :version, updated_at = :updatedAt
                WHERE id = :id
                """)
                .param("id",        template.getId())
                .param("content",   toJson(template.getContent()))
                .param("version",   template.getVersion())
                .param("updatedAt", Timestamp.from(template.getUpdatedAt()))
                .update();
    }

    @Override
    public void delete(UUID id) {
        jdbcClient.sql("DELETE FROM notification_templates WHERE id = :id")
                .param("id", id)
                .update();
    }

    @Override
    public Optional<Template> findByKey(TenantId tenantId, ServiceId serviceId, String templateKey) {
        return jdbcClient.sql("""
                SELECT id, tenant_id, service_id, template_key, content, version, created_at, updated_at
                FROM notification_templates
                WHERE tenant_id = :tenantId AND service_id = :serviceId AND template_key = :templateKey
                """)
                .param("tenantId",    tenantId.value())
                .param("serviceId",   serviceId.value())
                .param("templateKey", templateKey)
                .query((rs, n) -> mapRow(rs))
                .optional();
    }

    @Override
    public Optional<Template> findById(UUID id) {
        return jdbcClient.sql("""
                SELECT id, tenant_id, service_id, template_key, content, version, created_at, updated_at
                FROM notification_templates WHERE id = :id
                """)
                .param("id", id)
                .query((rs, n) -> mapRow(rs))
                .optional();
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    @SneakyThrows
    private Template mapRow(ResultSet rs) throws SQLException {
        String contentJson = rs.getString("content");
        Map<String, String> rawMap = objectMapper.readValue(contentJson, new TypeReference<>() {});
        // Convert string keys ("en", "vi") back to Locale objects
        Map<Locale, String> content = rawMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Locale.forLanguageTag(e.getKey()),
                        Map.Entry::getValue));

        var createdAtTs = rs.getTimestamp("created_at");
        var updatedAtTs = rs.getTimestamp("updated_at");
        return Template.reconstitute(
                rs.getObject("id", UUID.class),
                new TenantId(rs.getObject("tenant_id", UUID.class)),
                new ServiceId(rs.getString("service_id")),
                rs.getString("template_key"),
                content,
                rs.getInt("version"),
                createdAtTs != null ? createdAtTs.toInstant() : null,
                updatedAtTs != null ? updatedAtTs.toInstant() : null);
    }

    @SneakyThrows
    private String toJson(Map<Locale, String> content) {
        // Serialize Locale → its BCP 47 language tag string (e.g. Locale.ENGLISH → "en")
        Map<String, String> stringMap = content.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toLanguageTag(),
                        Map.Entry::getValue));
        return objectMapper.writeValueAsString(stringMap);
    }
}
