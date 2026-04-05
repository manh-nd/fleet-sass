package com.fleet.domain.notification.model;

import com.fleet.domain.rule.vo.RuleId;

import lombok.Getter;

@Getter
public class NotificationAction {
    private final RuleId ruleId;
    private final String channelType; // "EMAIL", "SMS", "WEBHOOK"
    private final String recipient; // "manager@example.com" hoặc "0901234567"
    private final String messageTemplate;

    public NotificationAction(RuleId ruleId, String channelType, String recipient, String messageTemplate) {
        this.ruleId = ruleId;
        this.channelType = channelType;
        this.recipient = recipient;
        this.messageTemplate = messageTemplate;
    }
}