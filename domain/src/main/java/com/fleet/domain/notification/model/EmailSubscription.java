package com.fleet.domain.notification.model;

import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.RuleId;

public class EmailSubscription {
    private final EmailAddress email;
    private final RuleId ruleId; // Nếu null tức là Unsubscribe toàn bộ hệ thống
    private SubscriptionStatus status;

    public enum SubscriptionStatus {
        PENDING, SUBSCRIBED, UNSUBSCRIBED
    }

    public EmailSubscription(EmailAddress email, RuleId ruleId, SubscriptionStatus status) {
        this.email = email;
        this.ruleId = ruleId;
        this.status = status;
    }

    // Business Logic: Có được phép gửi không? (Anti-spam/GDPR check)
    public boolean canSend() {
        return this.status == SubscriptionStatus.SUBSCRIBED;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public RuleId getRuleId() {
        return ruleId;
    }
}
