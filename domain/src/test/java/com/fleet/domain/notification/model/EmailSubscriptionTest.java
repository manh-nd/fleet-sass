package com.fleet.domain.notification.model;

import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmailSubscriptionTest {

    @Test
    void shouldCanSendWhenSubscribed() {
        EmailAddress email = new EmailAddress("test@example.com");
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EmailSubscription subscription = new EmailSubscription(email, ruleId, EmailSubscription.SubscriptionStatus.SUBSCRIBED);
        
        assertTrue(subscription.canSend());
    }

    @Test
    void shouldNotCanSendWhenPending() {
        EmailAddress email = new EmailAddress("test@example.com");
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EmailSubscription subscription = new EmailSubscription(email, ruleId, EmailSubscription.SubscriptionStatus.PENDING);
        
        assertFalse(subscription.canSend());
    }

    @Test
    void shouldNotCanSendWhenUnsubscribed() {
        EmailAddress email = new EmailAddress("test@example.com");
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EmailSubscription subscription = new EmailSubscription(email, ruleId, EmailSubscription.SubscriptionStatus.UNSUBSCRIBED);
        
        assertFalse(subscription.canSend());
    }

    @Test
    void shouldGettersWorkCorrectly() {
        EmailAddress email = new EmailAddress("test@example.com");
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EmailSubscription subscription = new EmailSubscription(email, ruleId, EmailSubscription.SubscriptionStatus.SUBSCRIBED);
        
        assertEquals(email, subscription.getEmail());
        assertEquals(ruleId, subscription.getRuleId());
    }
}
