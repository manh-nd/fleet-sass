package com.fleet.domain.notification.model;

import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.RuleId;

import lombok.Getter;

/**
 * Tracks an email subscription status used for anti-spam / GDPR checks.
 *
 * <p>If {@code ruleId} is {@code null}, this represents a global (system-wide)
 * unsubscribe setting for the given email address.</p>
 */
@Getter
public class EmailSubscription {

    private final EmailAddress email;
    // null means global unsubscribe from the entire system
    private final RuleId ruleId;
    private SubscriptionStatus status;

    public enum SubscriptionStatus {
        PENDING, SUBSCRIBED, UNSUBSCRIBED
    }

    public EmailSubscription(EmailAddress email, RuleId ruleId, SubscriptionStatus status) {
        this.email = email;
        this.ruleId = ruleId;
        this.status = status;
    }

    /**
     * Returns {@code true} if an email may be sent to this address.
     * Anti-spam / GDPR gate: only SUBSCRIBED status allows sending.
     */
    public boolean canSend() {
        return this.status == SubscriptionStatus.SUBSCRIBED;
    }
}
