package com.fleet.application.notification;

import com.fleet.domain.notification.model.EmailSubscription;
import com.fleet.domain.notification.model.NotificationAction;
import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.notification.port.out.EmailSenderPort;
import com.fleet.domain.notification.port.out.NotificationActionRepositoryPort;
import com.fleet.domain.notification.port.out.PushSenderPort;
import com.fleet.domain.notification.port.out.SmsSenderPort;
import com.fleet.domain.notification.port.out.SubscriptionCheckPort;
import com.fleet.domain.notification.port.out.WebhookSenderPort;
import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchAlertServiceTest {

    @Mock private NotificationActionRepositoryPort actionRepo;
    @Mock private SubscriptionCheckPort            subscriptionCheckPort;
    @Mock private EmailSenderPort                  emailSender;
    @Mock private SmsSenderPort                    smsSender;
    @Mock private WebhookSenderPort                webhookSender;
    @Mock private PushSenderPort                   pushSender;

    private DispatchAlertService service;

    @BeforeEach
    void setUp() {
        ChannelDispatchRouter router = new ChannelDispatchRouter(
                emailSender, smsSender, webhookSender, pushSender);
        service = new DispatchAlertService(actionRepo, subscriptionCheckPort, router);
    }

    @Test
    void shouldDispatchEmailWhenSubscribed() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EventPayload payload = new EventPayload("ref-1", Map.of("speed", 100));
        NotificationAction action = NotificationAction.create(ruleId, ChannelType.EMAIL, "test@test.com", "Speed {{speed}} reached!");

        when(actionRepo.getActionsForRule(ruleId)).thenReturn(List.of(action));
        when(subscriptionCheckPort.getSubscriptionStatus(new EmailAddress("test@test.com"), ruleId))
                .thenReturn(new EmailSubscription(new EmailAddress("test@test.com"), ruleId,
                        EmailSubscription.SubscriptionStatus.SUBSCRIBED));

        service.dispatch(ruleId, payload);

        verify(emailSender).send("test@test.com", "Fleet Alert", "Speed 100 reached!");
    }

    @Test
    void shouldSkipEmailWhenUnsubscribed() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EventPayload payload = new EventPayload("ref-1", Map.of("speed", 100));
        NotificationAction action = NotificationAction.create(ruleId, ChannelType.EMAIL, "unsub@test.com", "Speed reached!");

        when(actionRepo.getActionsForRule(ruleId)).thenReturn(List.of(action));
        when(subscriptionCheckPort.getSubscriptionStatus(new EmailAddress("unsub@test.com"), ruleId))
                .thenReturn(new EmailSubscription(new EmailAddress("unsub@test.com"), ruleId,
                        EmailSubscription.SubscriptionStatus.UNSUBSCRIBED));

        service.dispatch(ruleId, payload);

        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldSkipEmailWhenPending() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EventPayload payload = new EventPayload("ref-1", Map.of());
        NotificationAction action = NotificationAction.create(ruleId, ChannelType.EMAIL, "pending@test.com", "Alert!");

        when(actionRepo.getActionsForRule(ruleId)).thenReturn(List.of(action));
        when(subscriptionCheckPort.getSubscriptionStatus(new EmailAddress("pending@test.com"), ruleId))
                .thenReturn(new EmailSubscription(new EmailAddress("pending@test.com"), ruleId,
                        EmailSubscription.SubscriptionStatus.PENDING));

        service.dispatch(ruleId, payload);

        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldDispatchSms() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EventPayload payload = new EventPayload("ref-1", Map.of("driver", "John"));
        NotificationAction action = NotificationAction.create(ruleId, ChannelType.SMS, "0901234567", "Hi {{driver}}!");

        when(actionRepo.getActionsForRule(ruleId)).thenReturn(List.of(action));

        service.dispatch(ruleId, payload);

        verify(smsSender).send("0901234567", "Hi John!");
    }

    @Test
    void shouldDispatchWebhook() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EventPayload payload = new EventPayload("ref-1", Map.of("id", "123"));
        NotificationAction action = NotificationAction.create(ruleId, ChannelType.WEBHOOK, "http://endpoint.com", "Event: {{id}}");

        when(actionRepo.getActionsForRule(ruleId)).thenReturn(List.of(action));

        service.dispatch(ruleId, payload);

        verify(webhookSender).send("http://endpoint.com", "Event: 123");
    }

    @Test
    void shouldDispatchPush() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EventPayload payload = new EventPayload("ref-1", Map.of("zone", "A1"));
        NotificationAction action = NotificationAction.create(ruleId, ChannelType.PUSH, "device-token-xyz", "Entered zone {{zone}}");

        when(actionRepo.getActionsForRule(ruleId)).thenReturn(List.of(action));

        service.dispatch(ruleId, payload);

        verify(pushSender).send("device-token-xyz", "Fleet Alert", "Entered zone A1");
    }
}
