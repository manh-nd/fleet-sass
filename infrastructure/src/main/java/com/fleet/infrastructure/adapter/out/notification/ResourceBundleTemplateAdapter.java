package com.fleet.infrastructure.adapter.out.notification;

import com.fleet.domain.notification.port.out.TemplateRenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Infrastructure implementation of {@link TemplateRenderPort} using Java {@link ResourceBundle}.
 *
 * <p>Templates are stored in {@code src/main/resources/templates/{templateId}_{locale}.properties}
 * with a required {@code message} key. Falls back to {@link Locale#ENGLISH} if the requested
 * locale is unavailable.</p>
 *
 * <p>Variable substitution uses {@code {{key}}} syntax in the template message value.</p>
 *
 * <p>Example file: {@code templates/SPEED_ALERT_vi.properties}</p>
 * <pre>
 *   message=C\u1ea3nh b\u00e1o t\u1ed1c \u0111\u1ed9: xe \u0111ang ch\u1ea1y {{speed}} km/h, v\u01b0\u1ee3t qu\u00e1 gi\u1edbi h\u1ea1n!
 * </pre>
 */
@Component
@Slf4j
public class ResourceBundleTemplateAdapter implements TemplateRenderPort {

    private static final String TEMPLATE_BASE = "templates.";
    private static final String MESSAGE_KEY   = "message";

    @Override
    public String render(String templateId, Locale locale, Map<String, Object> variables) {
        ResourceBundle bundle = loadBundle(templateId, locale);
        String template = bundle.getString(MESSAGE_KEY);
        return applyVariables(template, variables);
    }

    @Override
    public boolean exists(String templateId) {
        try {
            ResourceBundle.getBundle(TEMPLATE_BASE + templateId, Locale.ENGLISH);
            return true;
        } catch (MissingResourceException e) {
            return false;
        }
    }

    // ---- Private helpers ----

    private ResourceBundle loadBundle(String templateId, Locale locale) {
        String baseName = TEMPLATE_BASE + templateId;
        try {
            // Try exact locale first
            return ResourceBundle.getBundle(baseName, locale);
        } catch (MissingResourceException e) {
            log.warn("Template '{}' not found for locale '{}', falling back to English", templateId, locale);
            try {
                return ResourceBundle.getBundle(baseName, Locale.ENGLISH);
            } catch (MissingResourceException fallbackEx) {
                throw new com.fleet.domain.shared.exception.TemplateNotFoundException(templateId);
            }
        }
    }

    private String applyVariables(String template, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) return template;
        String result = template;
        for (var entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}
