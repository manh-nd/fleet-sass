package com.fleet.infrastructure.adapter.in.web;

import com.fleet.application.notification.usecase.ManageTemplateUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.Template;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing notification templates.
 *
 * <p>Templates are locale-keyed content bodies used by the notification hub
 * to render messages in the recipient's language. All endpoints are scoped
 * to a (tenantId, serviceId) pair supplied via query parameters.</p>
 *
 * <p>Base path: {@code /api/v1/templates}</p>
 */
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Tag(name = "Templates", description = "Manage i18n notification templates")
public class TemplateController {

    private final ManageTemplateUseCase manageTemplateUseCase;

    /**
     * Creates a new template for a (tenant, service) pair.
     * The request body is a locale-keyed map of content strings.
     * English ({@code "en"}) is required as the fallback locale.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a notification template",
               description = "Registers a new template with locale-specific content. English fallback is mandatory.")
    public Template create(
            @RequestParam UUID tenantId,
            @RequestParam String serviceId,
            @RequestParam String templateKey,
            @RequestBody Map<String, String> localeContent) {
        Map<Locale, String> content = toLocaleMap(localeContent);
        return manageTemplateUseCase.create(
                new TenantId(tenantId), new ServiceId(serviceId), templateKey, content);
    }

    /**
     * Replaces the content of an existing template, incrementing its version.
     */
    @PutMapping("/{id}/content")
    @Operation(summary = "Update template content",
               description = "Replaces all locale variants and increments the version number.")
    public Template updateContent(
            @PathVariable UUID id,
            @RequestBody Map<String, String> localeContent) {
        return manageTemplateUseCase.updateContent(id, toLocaleMap(localeContent));
    }

    /**
     * Removes a template by id.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a template")
    public void delete(@PathVariable UUID id) {
        manageTemplateUseCase.delete(id);
    }

    /**
     * Looks up a template by its natural key: (tenant, service, templateKey).
     */
    @GetMapping("/lookup")
    @Operation(summary = "Find template by natural key",
               description = "Returns the template registered for the given (tenantId, serviceId, templateKey) combination.")
    public ResponseEntity<Template> findByKey(
            @RequestParam UUID tenantId,
            @RequestParam String serviceId,
            @RequestParam String templateKey) {
        return manageTemplateUseCase
                .findByKey(new TenantId(tenantId), new ServiceId(serviceId), templateKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a template by its surrogate UUID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get template by id")
    public ResponseEntity<Template> findById(@PathVariable UUID id) {
        return manageTemplateUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Converts string locale keys ("en", "vi") from the JSON body to {@link Locale} objects. */
    private Map<Locale, String> toLocaleMap(Map<String, String> raw) {
        return raw.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        e -> Locale.forLanguageTag(e.getKey()),
                        Map.Entry::getValue));
    }
}
