package com.fleet.application.shared.event;

import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;

public record RuleTriggeredEvent(RuleId ruleId, EventPayload payload) {
}