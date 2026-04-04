package com.fleet.domain.notification.port.out;

import java.util.List;

import com.fleet.domain.notification.model.NotificationAction;
import com.fleet.domain.rule.vo.RuleId;

public interface NotificationActionRepositoryPort {
    List<NotificationAction> getActionsForRule(RuleId ruleId);
}
