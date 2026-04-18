package com.fleet.application.notification;

import com.fleet.application.notification.usecase.DispatchAlertUseCase;
import com.fleet.application.shared.TemplateVariableSubstitutor;
import com.fleet.domain.notification.model.EmailSubscription;
import com.fleet.domain.notification.model.NotificationAction;
import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.notification.port.out.NotificationActionRepositoryPort;
import com.fleet.domain.notification.port.out.SubscriptionCheckPort;
import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Service implementation for dispatching rule-triggered alerts.
 *
 * <p>Coordinates multi-channel delivery (Email, SMS, Webhook, Push)
 * while enforcing GDPR opt-in policies for email recipients.</p>
 *
 * <p>Channels are routed through {@link ChannelDispatchRouter}, which holds
 * a distinct focused sender port per channel type — eliminating the need for
 * a fat {@code NotificationDispatcherPort} that throws on unsupported methods.</p>
 */
@RequiredArgsConstructor
public class DispatchAlertService implements DispatchAlertUseCase {

    private final NotificationActionRepositoryPort actionRepo;
    private final SubscriptionCheckPort            subscriptionCheckPort;
    private final ChannelDispatchRouter            channelRouter;

    @Override
    public void dispatch(RuleId ruleId, EventPayload payload) {
        List<NotificationAction> actions = actionRepo.getActionsForRule(ruleId);

        for (NotificationAction action : actions) {
            String recipient = action.getRecipient();

            // GDPR gate: check email opt-in status before sending
            if (action.getChannelType() == ChannelType.EMAIL) {
                EmailSubscription sub = subscriptionCheckPort.getSubscriptionStatus(
                        new EmailAddress(recipient), ruleId);
                if (sub != null && !sub.canSend()) {
                    continue;
                }
            }

            String message = TemplateVariableSubstitutor.apply(
                    action.getMessageTemplate(), payload.data());

            // Subject is "Fleet Alert" for email/push; empty string is ignored by SMS/webhook
            channelRouter.dispatch(action.getChannelType(), recipient, "Fleet Alert", message);
        }
    }
}