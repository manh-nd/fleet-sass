package com.fleet.application.notification;

import com.fleet.application.notification.usecase.ManageTemplateUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.Template;
import com.fleet.domain.notification.port.out.TemplateRepositoryPort;
import com.fleet.domain.shared.exception.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing {@link ManageTemplateUseCase}.
 *
 * <p>Orchestrates template lifecycle operations: create, update content,
 * delete, and lookup. All mutations are delegated to the
 * {@link TemplateRepositoryPort} outbound port.</p>
 */
@RequiredArgsConstructor
@Slf4j
public class ManageTemplateService implements ManageTemplateUseCase {

    private final TemplateRepositoryPort templateRepository;

    @Override
    public Template create(
            TenantId tenantId,
            ServiceId serviceId,
            String templateKey,
            Map<Locale, String> content) {
        // Template.create() enforces English fallback and null checks
        Template template = Template.create(tenantId, serviceId, templateKey, content);
        templateRepository.save(template);
        log.info("Template created: key={}, tenant={}, service={}, locales={}",
                templateKey, tenantId.value(), serviceId.value(), content.keySet());
        return template;
    }

    @Override
    public Template updateContent(UUID id, Map<Locale, String> content) {
        Template existing = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException(id.toString()));
        Template updated = existing.updateContent(content);
        templateRepository.update(updated);
        log.info("Template updated: id={}, newVersion={}, locales={}",
                id, updated.getVersion(), content.keySet());
        return updated;
    }

    @Override
    public void delete(UUID id) {
        templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException(id.toString()));
        templateRepository.delete(id);
        log.info("Template deleted: id={}", id);
    }

    @Override
    public Optional<Template> findByKey(TenantId tenantId, ServiceId serviceId, String templateKey) {
        return templateRepository.findByKey(tenantId, serviceId, templateKey);
    }

    @Override
    public Optional<Template> findById(UUID id) {
        return templateRepository.findById(id);
    }
}
