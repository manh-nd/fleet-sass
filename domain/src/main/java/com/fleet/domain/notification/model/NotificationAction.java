package com.fleet.domain.notification.model;

import com.fleet.domain.rule.vo.RuleId;

import lombok.Getter;

/**
 * Describes a single notification dispatch action associated with a rule.
 * Each action specifies the channel, recipient, and message template.
 */
@Getter
public class NotificationAction {

    /**
     * Supported notification channels.
     */
    public enum ChannelType {
        EMAIL,
        SMS,
        WEBHOOK;

        /**
         * Resolves a {@link ChannelType} from its name string (case-insensitive).
         *
         * @throws IllegalArgumentException if the string doesn't match any channel
         */
        public static ChannelType fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("ChannelType value must not be null");
            }
            try {
                return ChannelType.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unsupported channel type: " + value);
            }
        }
    }

    private final RuleId ruleId;
    private final ChannelType channelType;
    // email address, phone number, or webhook URL depending on channelType
    private final String recipient;
    private final String messageTemplate;

    public NotificationAction(RuleId ruleId, ChannelType channelType, String recipient, String messageTemplate) {
        this.ruleId = ruleId;
        this.channelType = channelType;
        this.recipient = recipient;
        this.messageTemplate = messageTemplate;
    }
}