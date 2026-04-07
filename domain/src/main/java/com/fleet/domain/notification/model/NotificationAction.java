package com.fleet.domain.notification.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.fleet.domain.rule.vo.RuleId;

/**
 * Describes a single notification dispatch action associated with a rule.
 * Each action specifies the channel, recipient, and message template.
 *
 * <p>Instances must be created via {@link #create} to ensure validation
 * is applied per channel type.</p>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationAction {

    /**
     * Supported notification delivery channels.
     */
    public enum ChannelType {
        EMAIL,
        SMS,
        WEBHOOK,
        PUSH;

        /**
         * Resolves a {@link ChannelType} from its name string (case-insensitive).
         *
         * @throws IllegalArgumentException if the string does not match any channel
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
    /** Email address, phone number, device token, or webhook URL — depends on channelType. */
    private final String recipient;
    private final String messageTemplate;

    /**
     * Creates a validated {@link NotificationAction}.
     *
     * @param ruleId          the rule this action belongs to
     * @param channelType     the delivery channel
     * @param recipient       email address / phone number / device token / webhook URL
     * @param messageTemplate the message template (supports {{variable}} placeholders)
     */
    public static NotificationAction create(
            RuleId ruleId,
            ChannelType channelType,
            String recipient,
            String messageTemplate) {
        if (ruleId == null) throw new IllegalArgumentException("ruleId must not be null");
        if (channelType == null) throw new IllegalArgumentException("channelType must not be null");
        if (recipient == null || recipient.isBlank())
            throw new IllegalArgumentException("recipient must not be blank");
        if (messageTemplate == null || messageTemplate.isBlank())
            throw new IllegalArgumentException("messageTemplate must not be blank");
        return new NotificationAction(ruleId, channelType, recipient, messageTemplate);
    }
}