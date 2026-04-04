package com.fleet.domain.rule.vo;

import java.util.Map;

public record EventPayload(String vehicleId, Map<String, Object> data) {
}
