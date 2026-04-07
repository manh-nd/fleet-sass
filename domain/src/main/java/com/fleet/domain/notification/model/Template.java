package com.fleet.domain.notification.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A reusable, versioned notification template with locale-specific content.
 *
 * <p>Templates are owned by a (tenant, service) pair and identified by a {@code templateKey}
 * (e.g. {@code "SPEED_ALERT"}, {@code "GEOFENCE_BREACH"}). Each template stores content
 * variants keyed by {@link Locale}, with {@link Locale#ENGLISH} as the required fallback.</p>
 *
 * <p>Variable substitution placeholders follow {@code {{variableName}}} syntax.</p>
 *
 * <h3>Example content map</h3>
 * <pre>{@code
 * {
 *   Locale.ENGLISH: "Vehicle {{referenceId}} exceeded speed limit: {{speed}} km/h",
 *   Locale.forLanguageTag("vi"): "Xe {{referenceId}} vượt tốc độ: {{speed}} km/h"
 * }
 * }</pre>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Template {

    private final UUID id;
    private final TenantId tenantId;
    private final ServiceId serviceId;

    /** Human-readable key for this template, e.g. {@code "SPEED_ALERT"}. Unique per (tenant, service). */
    private final String templateKey;

    /** Locale → rendered body map. Must contain at least {@link Locale#ENGLISH}. */
    private final Map<Locale, String> content;

    /** Monotonically increasing version number, incremented on each update. */
    private final int version;

    private final Instant createdAt;
    private final Instant updatedAt;

    // ── Factory ──────────────────────────────────────────────────────────────

    /**
     * Creates a new template at version 1.
     *
     * @throws IllegalArgumentException if {@code content} does not contain an English fallback
     */
    public static Template create(
            TenantId tenantId,
            ServiceId serviceId,
            String templateKey,
            Map<Locale, String> content) {
        Objects.requireNonNull(tenantId,    "tenantId must not be null");
        Objects.requireNonNull(serviceId,   "serviceId must not be null");
        Objects.requireNonNull(templateKey, "templateKey must not be null");
        Objects.requireNonNull(content,     "content must not be null");
        if (!content.containsKey(Locale.ENGLISH)) {
            throw new IllegalArgumentException(
                    "Template must contain English (Locale.ENGLISH) as the mandatory fallback locale");
        }
        Instant now = Instant.now();
        return new Template(UUID.randomUUID(), tenantId, serviceId, templateKey, content, 1, now, now);
    }

    /** Reconstitutes a persisted template from the database. */
    public static Template reconstitute(
            UUID id,
            TenantId tenantId,
            ServiceId serviceId,
            String templateKey,
            Map<Locale, String> content,
            int version,
            Instant createdAt,
            Instant updatedAt) {
        return new Template(id, tenantId, serviceId, templateKey, content, version, createdAt, updatedAt);
    }

    // ── Business logic ────────────────────────────────────────────────────────

    /**
     * Returns a new template with updated content and an incremented version number.
     * The original instance is not modified (immutable pattern).
     */
    public Template updateContent(Map<Locale, String> newContent) {
        if (!newContent.containsKey(Locale.ENGLISH)) {
            throw new IllegalArgumentException("Updated content must contain English fallback");
        }
        return new Template(id, tenantId, serviceId, templateKey, newContent, version + 1, createdAt, Instant.now());
    }

    /**
     * Returns the content for the requested locale, falling back to English if unavailable.
     */
    public String getContentForLocale(Locale locale) {
        return content.getOrDefault(locale, content.get(Locale.ENGLISH));
    }
}
