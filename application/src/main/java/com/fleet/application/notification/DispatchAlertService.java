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

import java.util.List;
import java.util.Map;

/**
 * Service implementation for dispatching alerts.
 * Coordinates multi-channel delivery (Email, SMS, Webhook) while enforcing GDPR opt-in policies.
 */
public class DispatchAlertService implements DispatchAlertUseCase {

    private final NotificationActionRepositoryPort actionRepo;
    private final SubscriptionCheckPort subscriptionCheckPort;
    private final NotificationDispatcherPort dispatcher;

    public DispatchAlertService(NotificationActionRepositoryPort actionRepo,
            SubscriptionCheckPort subscriptionCheckPort,
            NotificationDispatcherPort dispatcher) {
        this.actionRepo = actionRepo;
        this.subscriptionCheckPort = subscriptionCheckPort;
        this.dispatcher = dispatcher;
    }

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
                case EMAIL   -> dispatcher.sendEmail(recipient, "Fleet Alert", message);
                case SMS     -> dispatcher.sendSms(recipient, message);
                case WEBHOOK -> dispatcher.sendWebhook(recipient, message);
            }
        }
    }

    /**
     * Replaces {@code {{variable}}} placeholders in the template with values from the payload.
     */
    private String renderTemplate(String template, Map<String, Object> data) {
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return result;
    }
}