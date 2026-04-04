package com.fleet.domain.entitlement.vo;

import java.util.Objects;

public record ServiceId(String value) {
    public ServiceId {
        Objects.requireNonNull(value, "ServiceId cannot be null");
    }
}
