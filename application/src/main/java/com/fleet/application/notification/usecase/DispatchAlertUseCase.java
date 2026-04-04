package com.fleet.application.notification.usecase;

import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;

public interface DispatchAlertUseCase {
    void dispatch(RuleId ruleId, EventPayload payload);
}