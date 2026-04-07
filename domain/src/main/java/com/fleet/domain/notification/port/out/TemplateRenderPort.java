package com.fleet.domain.notification.port.out;

import java.util.Locale;
import java.util.Map;

/**
 * Outbound port for rendering notification templates with i18n support.
 *
 * <p>The concrete implementation handles template lookup (by ID + locale),
 * falls back to a default locale (typically {@link java.util.Locale#ENGLISH}),
 * and substitutes {@code {{variable}}} placeholders in the template body.</p>
 */
public interface TemplateRenderPort {

    /**
     * Renders a template by ID for a given locale with variable substitution.
     *
     * @param templateId the template identifier (e.g. "SPEED_ALERT", "GEOFENCE_BREACH")
     * @param locale     the target locale for i18n translation
     * @param variables  key-value pairs for placeholder substitution (e.g. "speed" → 120)
     * @return the fully rendered message string
     * @throws com.fleet.domain.shared.exception.RuleNotFoundException if the template is not found
     */
    String render(String templateId, Locale locale, Map<String, Object> variables);

    /**
     * Returns {@code true} if a template with the given ID exists in any locale.
     *
     * @param templateId the template identifier
     */
    boolean exists(String templateId);
}
