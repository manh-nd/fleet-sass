package com.fleet.domain.notification.port.out;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.Template;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for template persistence.
 *
 * <p>Template content is store as a JSONB locale→content map in the
 * {@code notification_templates} table (changeset 005).</p>
 */
public interface TemplateRepositoryPort {

    /** Persists a new template. Throws if the (tenantId, serviceId, templateKey) combination already exists. */
    void save(Template template);

    /** Replaces the content of an existing template (increments version). */
    void update(Template template);

    /** Soft-deletes (or hard-deletes) a template by id. */
    void delete(UUID id);

    /**
     * Finds a template by its natural key: (tenant, service, templateKey).
     * Returns empty when no template is registered for that key.
     */
    Optional<Template> findByKey(TenantId tenantId, ServiceId serviceId, String templateKey);

    /** Finds a template by its surrogate id. */
    Optional<Template> findById(UUID id);
}
