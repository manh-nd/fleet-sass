package com.fleet.domain.notification.port.out;

import com.fleet.domain.notification.model.EmailSubscription;
import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.RuleId;

/**
 * Outbound port to check the subscription (opt-in/opt-out) status of a recipient.
 */
public interface SubscriptionCheckPort {
    EmailSubscription getSubscriptionStatus(EmailAddress email, RuleId ruleId);
}
