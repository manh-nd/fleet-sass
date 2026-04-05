package com.fleet.application.notification.usecase;

import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;

/**
 * Inbound port for dispatching rule-triggered alerts.
 */
public interface DispatchAlertUseCase {
    void dispatch(RuleId ruleId, EventPayload payload);
}