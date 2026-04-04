package com.fleet.domain.entitlement.vo;

import java.util.Objects;
import java.util.UUID;

public record TenantId(UUID value) {
    public TenantId {
        Objects.requireNonNull(value, "TenantId cannot be null");
    }
}
