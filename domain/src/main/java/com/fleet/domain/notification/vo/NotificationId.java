package com.fleet.domain.notification.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for a notification dispatch event.
 */
public record NotificationId(UUID value) {

    public NotificationId {
        Objects.requireNonNull(value, "NotificationId value must not be null");
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
