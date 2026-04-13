package com.fleet.infrastructure.adapter.in.web;

import com.fleet.application.notification.usecase.ManageTemplateUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.Template;
import com.fleet.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
 * <p>
 * Templates are locale-keyed content bodies used by the notification hub
 * to render messages in the recipient's language. All endpoints are scoped
 * to a (tenantId, serviceId) pair supplied via query parameters.
 * </p>
 *
 * <p>
 * Base path: {@code /api/v1/templates}
 * </p>
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
    @Operation(summary = "Create a notification template", description = "Registers a new template with locale-specific content. English fallback is mandatory.")
    @ApiResponse(responseCode = "201", description = "Template created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or missing mandatory English locale", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Template create(
            @Parameter(description = "Tenant ID", required = true) @RequestParam UUID tenantId,
            @Parameter(description = "Service identifier", required = true) @RequestParam String serviceId,
            @Parameter(description = "Unique key for the template", required = true) @RequestParam String templateKey,
            @Parameter(description = "Map of Locale tags to content strings", required = true, schema = @Schema(example = "{\"en\": \"Your invoice for {{amount}} is ready.\", \"vi\": \"Hóa đơn {{amount}} của bạn đã sẵn sàng.\"}")) @RequestBody Map<String, String> localeContent) {
        Map<Locale, String> content = toLocaleMap(localeContent);
        return manageTemplateUseCase.create(
                new TenantId(tenantId), new ServiceId(serviceId), templateKey, content);
    }

    /**
     * Replaces the content of an existing template, incrementing its version.
     */
    @PutMapping("/{id}/content")
    @Operation(summary = "Update template content", description = "Replaces all locale variants and increments the version number.")
    @ApiResponse(responseCode = "200", description = "Template content updated successfully")
    @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Template updateContent(
            @Parameter(description = "ID of the template to update", required = true) @PathVariable UUID id,
            @Parameter(description = "Map of Locale tags to new content" , required = true) @RequestBody Map<String, String> localeContent) {
        return manageTemplateUseCase.updateContent(id, toLocaleMap(localeContent));
    }

    /**
     * Removes a template by id.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a template")
    @ApiResponse(responseCode = "204", description = "Template deleted successfully")
    @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public void delete(@Parameter(description = "ID of the template to delete", required = true) @PathVariable UUID id) {
        manageTemplateUseCase.delete(id);
    }

    /**
     * Looks up a template by its natural key: (tenant, service, templateKey).
     */
    @GetMapping("/lookup")
    @Operation(summary = "Find template by natural key", description = "Returns the template registered for the given (tenantId, serviceId, templateKey) combination.")
    @ApiResponse(responseCode = "200", description = "Template found")
    @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Template> findByKey(
            @Parameter(description = "Tenant ID", required = true) @RequestParam UUID tenantId,
            @Parameter(description = "Service identifier", required = true) @RequestParam String serviceId,
            @Parameter(description = "Template key", required = true) @RequestParam String templateKey) {
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
    @ApiResponse(responseCode = "200", description = "Template found")
    @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Template> findById(@Parameter(description = "ID of the template", required = true) @PathVariable UUID id) {
        return manageTemplateUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Converts string locale keys ("en", "vi") from the JSON body to {@link Locale}
     * objects.
     */
    private Map<Locale, String> toLocaleMap(Map<String, String> raw) {
        return raw.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        e -> Locale.forLanguageTag(e.getKey()),
                        Map.Entry::getValue));
    }
}
