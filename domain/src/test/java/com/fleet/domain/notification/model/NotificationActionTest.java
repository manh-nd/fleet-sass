package com.fleet.domain.notification.model;

import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationActionTest {

    @Test
    void shouldGettersWorkCorrectly() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        NotificationAction action = new NotificationAction(ruleId, "EMAIL", "test@example.com", "Hello Speeding!");
        
        assertEquals("EMAIL", action.getChannelType());
        assertEquals("test@example.com", action.getRecipient());
        assertEquals("Hello Speeding!", action.getMessageTemplate());
    }
}
