package com.fleet.domain.notification.vo;

public record EmailAddress(String value) {
    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }
    }
}
