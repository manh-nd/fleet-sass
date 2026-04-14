package com.fleet.application.notification;

import com.fleet.application.notification.usecase.DispatchAlertUseCase;
import com.fleet.domain.notification.model.EmailSubscription;
import com.fleet.domain.notification.model.NotificationAction;
import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.notification.port.out.NotificationActionRepositoryPort;
import com.fleet.domain.notification.port.out.NotificationDispatcherPort;
import com.fleet.domain.notification.port.out.SubscriptionCheckPort;
import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Service implementation for dispatching alerts.
 * Coordinates multi-channel delivery (Email, SMS, Webhook, Push)
 * while enforcing GDPR opt-in policies for email recipients.
 */
@RequiredArgsConstructor
public class DispatchAlertService implements DispatchAlertUseCase {

    private final NotificationActionRepositoryPort actionRepo;
    private final SubscriptionCheckPort subscriptionCheckPort;
    private final NotificationDispatcherPort dispatcher;

    @Override
    public void dispatch(RuleId ruleId, EventPayload payload) {
        List<NotificationAction> actions = actionRepo.getActionsForRule(ruleId);

        for (NotificationAction action : actions) {
            String recipient = action.getRecipient();

            // GDPR gate: check email unsubscribe status before sending
            if (action.getChannelType() == ChannelType.EMAIL) {
                EmailSubscription sub = subscriptionCheckPort.getSubscriptionStatus(
                        new EmailAddress(recipient), ruleId);
                if (sub != null && !sub.canSend()) {
                    continue;
                }
            }

            String message = renderTemplate(action.getMessageTemplate(), payload.data());

            switch (action.getChannelType()) {
                case EMAIL -> dispatcher.sendEmail(recipient, "Fleet Alert", message);
                case SMS -> dispatcher.sendSms(recipient, message);
                case WEBHOOK -> dispatcher.sendWebhook(recipient, message);
                case PUSH -> dispatcher.sendPush(recipient, "Fleet Alert", message);
            }
        }
    }

    /**
     * Replaces {@code {{variable}}} placeholders in the template with values from
     * the payload.
     */
    private String renderTemplate(String template, Map<String, Object> data) {
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}