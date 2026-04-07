package com.fleet.application.notification.usecase;

import com.fleet.domain.notification.model.Template;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port for notification template management.
 *
 * <p>Templates are keyed by a human-readable {@code templateKey} (e.g. {@code "SPEED_ALERT"})
 * scoped to a (tenant, service) pair. Each template stores locale-specific content variants
 * for i18n, with English as the mandatory fallback.</p>
 *
 * <h3>Lifecycle</h3>
 * <pre>
 *   create() → template registered at version 1
 *   updateContent() → version incremented, content replaced
 *   delete() → template removed
 * </pre>
 */
public interface ManageTemplateUseCase {

    /**
     * Registers a new template for a given service.
     *
     * @param tenantId    owning tenant
     * @param serviceId   owning service
     * @param templateKey human-readable key, e.g. {@code "SPEED_ALERT"}
     * @param content     locale → template body map; must include {@link Locale#ENGLISH}
     * @return the newly created template
     * @throws IllegalArgumentException  if English fallback is missing
     * @throws IllegalStateException     if the key already exists for this (tenant, service)
     */
    Template create(TenantId tenantId, ServiceId serviceId, String templateKey, Map<Locale, String> content);

    /**
     * Replaces the content of an existing template, incrementing its version.
     *
     * @param id     the template's surrogate id
     * @param content new locale → body map; must include {@link Locale#ENGLISH}
     * @return the updated template
     * @throws com.fleet.domain.shared.exception.TemplateNotFoundException if no template with that id exists
     */
    Template updateContent(UUID id, Map<Locale, String> content);

    /**
     * Removes a template by id.
     *
     * @throws com.fleet.domain.shared.exception.TemplateNotFoundException if no template with that id exists
     */
    void delete(UUID id);

    /**
     * Retrieves a template by its natural key.
     *
     * @return empty if no template is registered for this (tenant, service, key) combination
     */
    Optional<Template> findByKey(TenantId tenantId, ServiceId serviceId, String templateKey);

    /**
     * Retrieves a template by its surrogate id.
     *
     * @return empty if no template exists with that id
     */
    Optional<Template> findById(UUID id);
}
