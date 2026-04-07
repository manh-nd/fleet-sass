package com.fleet.domain.shared.exception;

/**
 * Thrown when a notification template cannot be found by its ID.
 */
public class TemplateNotFoundException extends RuntimeException {

    private final String templateId;

    public TemplateNotFoundException(String templateId) {
        super("Notification template not found: " + templateId);
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }
}
