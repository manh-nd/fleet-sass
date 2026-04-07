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
        NotificationAction action = NotificationAction.create(ruleId, ChannelType.EMAIL, "test@example.com", "Hello Speeding!");

        assertEquals(ChannelType.EMAIL, action.getChannelType());
        assertEquals("test@example.com", action.getRecipient());
        assertEquals("Hello Speeding!", action.getMessageTemplate());
        assertEquals(ruleId, action.getRuleId());
    }

    @Test
    void shouldSupportAllChannelTypes() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        NotificationAction email   = NotificationAction.create(ruleId, ChannelType.EMAIL,   "a@b.com",          "msg");
        NotificationAction sms     = NotificationAction.create(ruleId, ChannelType.SMS,     "0901234567",       "msg");
        NotificationAction webhook = NotificationAction.create(ruleId, ChannelType.WEBHOOK, "http://x.com",     "msg");
        NotificationAction push    = NotificationAction.create(ruleId, ChannelType.PUSH,    "device-token-abc", "msg");

        assertEquals(ChannelType.EMAIL,   email.getChannelType());
        assertEquals(ChannelType.SMS,     sms.getChannelType());
        assertEquals(ChannelType.WEBHOOK, webhook.getChannelType());
        assertEquals(ChannelType.PUSH,    push.getChannelType());
    }

    @Test
    void channelTypeShouldResolveFromString() {
        assertEquals(ChannelType.EMAIL,   ChannelType.fromString("EMAIL"));
        assertEquals(ChannelType.EMAIL,   ChannelType.fromString("email"));
        assertEquals(ChannelType.SMS,     ChannelType.fromString("SMS"));
        assertEquals(ChannelType.WEBHOOK, ChannelType.fromString("WEBHOOK"));
        assertEquals(ChannelType.PUSH,    ChannelType.fromString("PUSH"));
    }

    @Test
    void shouldThrowWhenRecipientIsBlank() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class,
                () -> NotificationAction.create(ruleId, ChannelType.EMAIL, "", "msg"));
    }

    @Test
    void channelTypeShouldThrowForUnsupportedValue() {
        assertThrows(IllegalArgumentException.class, () -> ChannelType.fromString("FAX"));
    }

    @Test
    void channelTypeShouldThrowForNull() {
        assertThrows(IllegalArgumentException.class, () -> ChannelType.fromString(null));
    }
}
