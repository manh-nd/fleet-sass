package com.fleet.domain.notification.model;

import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationActionTest {

    @Test
    void shouldExposeFieldsCorrectly() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        NotificationAction action = new NotificationAction(ruleId, ChannelType.EMAIL, "test@example.com", "Hello Speeding!");

        assertEquals(ChannelType.EMAIL, action.getChannelType());
        assertEquals("test@example.com", action.getRecipient());
        assertEquals("Hello Speeding!", action.getMessageTemplate());
        assertEquals(ruleId, action.getRuleId());
    }

    @Test
    void shouldSupportAllChannelTypes() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        NotificationAction email = new NotificationAction(ruleId, ChannelType.EMAIL, "a@b.com", "msg");
        NotificationAction sms = new NotificationAction(ruleId, ChannelType.SMS, "0901234567", "msg");
        NotificationAction webhook = new NotificationAction(ruleId, ChannelType.WEBHOOK, "http://x.com", "msg");

        assertEquals(ChannelType.EMAIL, email.getChannelType());
        assertEquals(ChannelType.SMS, sms.getChannelType());
        assertEquals(ChannelType.WEBHOOK, webhook.getChannelType());
    }

    @Test
    void channelTypeShouldResolveFromString() {
        assertEquals(ChannelType.EMAIL, ChannelType.fromString("EMAIL"));
        assertEquals(ChannelType.EMAIL, ChannelType.fromString("email"));
        assertEquals(ChannelType.SMS, ChannelType.fromString("SMS"));
        assertEquals(ChannelType.WEBHOOK, ChannelType.fromString("WEBHOOK"));
    }

    @Test
    void channelTypeShouldThrowForUnsupportedValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ChannelType.fromString("PUSH"));
        assertTrue(ex.getMessage().contains("Unsupported channel type"));
    }

    @Test
    void channelTypeShouldThrowForNull() {
        assertThrows(IllegalArgumentException.class, () -> ChannelType.fromString(null));
    }
}
