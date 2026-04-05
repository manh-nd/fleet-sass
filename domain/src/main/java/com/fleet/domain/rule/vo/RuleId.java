package com.fleet.domain.rule.vo;

import java.util.Objects;
import java.util.UUID;

public record RuleId(UUID value) {
    public RuleId {
        Objects.requireNonNull(value, "RuleId cannot be null");
    }
}
