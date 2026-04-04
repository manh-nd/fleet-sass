package com.fleet.domain.notification.port.out;

import com.fleet.domain.notification.model.EmailSubscription;
import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.RuleId;

public interface SubscriptionCheckPort {
    EmailSubscription getSubscriptionStatus(EmailAddress email, RuleId ruleId);
}
