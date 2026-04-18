package com.fleet.application.shared;

import java.util.Map;

/**
 * Utility for applying {@code {{variable}}} placeholder substitution to a template string.
 *
 * <p>Extracted from {@code SendNotificationService} and {@code DispatchAlertService}
 * to eliminate the duplicated inline substitution logic.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * String result = TemplateVariableSubstitutor.apply(
 *     "Vehicle {{plate}} exceeded {{speed}} km/h",
 *     Map.of("plate", "51A-123.45", "speed", 120));
 * // → "Vehicle 51A-123.45 exceeded 120 km/h"
 * }</pre>
 */
public final class TemplateVariableSubstitutor {

    private TemplateVariableSubstitutor() {}

    /**
     * Replaces every {@code {{key}}} occurrence in {@code template} with the
     * corresponding value from {@code variables}. Missing keys are left as-is.
     *
     * @param template  the template string (may be {@code null})
     * @param variables placeholder values (may be {@code null} or empty)
     * @return the substituted string, or the original template if nothing to replace
     */
    public static String apply(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}
