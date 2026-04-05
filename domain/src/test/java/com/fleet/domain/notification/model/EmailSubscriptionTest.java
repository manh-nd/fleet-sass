package com.fleet.domain.notification.model;

import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmailSubscriptionTest {

    @Test
    void shouldCanSendWhenSubscribed() {
        EmailSubscription sub = build(EmailSubscription.SubscriptionStatus.SUBSCRIBED);
        assertTrue(sub.canSend());
    }

    @Test
    void shouldNotCanSendWhenPending() {
        EmailSubscription sub = build(EmailSubscription.SubscriptionStatus.PENDING);
        assertFalse(sub.canSend());
    }

    @Test
    void shouldNotCanSendWhenUnsubscribed() {
        EmailSubscription sub = build(EmailSubscription.SubscriptionStatus.UNSUBSCRIBED);
        assertFalse(sub.canSend());
    }

    @Test
    void shouldExposeEmailAndRuleId() {
        EmailAddress email = new EmailAddress("test@example.com");
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EmailSubscription sub = new EmailSubscription(email, ruleId, EmailSubscription.SubscriptionStatus.SUBSCRIBED);

        assertEquals(email, sub.getEmail());
        assertEquals(ruleId, sub.getRuleId());
    }

    @Test
    void shouldAllowNullRuleIdForGlobalSubscription() {
        // null ruleId = global unsubscribe
        EmailAddress email = new EmailAddress("global@example.com");
        EmailSubscription sub = new EmailSubscription(email, null, EmailSubscription.SubscriptionStatus.UNSUBSCRIBED);
        assertNull(sub.getRuleId());
        assertFalse(sub.canSend());
    }

    // ---- Helper ----

    private EmailSubscription build(EmailSubscription.SubscriptionStatus status) {
        return new EmailSubscription(
                new EmailAddress("test@example.com"),
                new RuleId(UUID.randomUUID()),
                status);
    }
}
