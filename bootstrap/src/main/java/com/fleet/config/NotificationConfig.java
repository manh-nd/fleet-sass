package com.fleet.config;

import com.fleet.application.notification.ChannelDispatchRouter;
import com.fleet.application.notification.DispatchAlertService;
import com.fleet.application.notification.ManageTemplateService;
import com.fleet.application.notification.SendNotificationService;
import com.fleet.application.notification.usecase.DispatchAlertUseCase;
import com.fleet.application.notification.usecase.ManageTemplateUseCase;
import com.fleet.application.notification.usecase.SendNotificationUseCase;
import com.fleet.domain.notification.port.out.DeadLetterQueuePort;
import com.fleet.domain.notification.port.out.EmailSenderPort;
import com.fleet.domain.notification.port.out.NotificationActionRepositoryPort;
import com.fleet.domain.notification.port.out.NotificationLogRepositoryPort;
import com.fleet.domain.notification.port.out.PushSenderPort;
import com.fleet.domain.notification.port.out.SmsSenderPort;
import com.fleet.domain.notification.port.out.SubscriptionCheckPort;
import com.fleet.domain.notification.port.out.TemplateRenderPort;
import com.fleet.domain.notification.port.out.TemplateRepositoryPort;
import com.fleet.domain.notification.port.out.WebhookSenderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for all notification-related application service beans.
 *
 * <p>Each channel uses a dedicated focused port ({@link EmailSenderPort},
 * {@link SmsSenderPort}, {@link WebhookSenderPort}, {@link PushSenderPort})
 * wired into a {@link ChannelDispatchRouter}. Adding a new channel requires
 * only a new port + adapter + a case in the router — zero changes here.</p>
 */
@Configuration
public class NotificationConfig {

    /**
     * Routes notifications to the correct per-channel sender port.
     * Console stub adapters are active by default via {@code @ConditionalOnMissingBean}.
     */
    @Bean
    public ChannelDispatchRouter channelDispatchRouter(
            EmailSenderPort   emailSender,
            SmsSenderPort     smsSender,
            WebhookSenderPort webhookSender,
            PushSenderPort    pushSender) {
        return new ChannelDispatchRouter(emailSender, smsSender, webhookSender, pushSender);
    }

    /**
     * Alert dispatch service — triggered by rule evaluation events.
     * Handles email subscription checking per the GDPR anti-spam contract.
     */
    @Bean
    public DispatchAlertUseCase dispatchAlertUseCase(
            NotificationActionRepositoryPort actionRepositoryPort,
            SubscriptionCheckPort            subscriptionCheckPort,
            ChannelDispatchRouter            channelRouter) {
        return new DispatchAlertService(actionRepositoryPort, subscriptionCheckPort, channelRouter);
    }

    /**
     * Direct-send notification service — consumed by the REST API and external services.
     * Handles template rendering, dispatch, and delivery log persistence.
     *
     * <p>Retry logic is intentionally absent here: a single dispatch attempt is made,
     * and failures are forwarded to the Dead Letter Queue. Infrastructure-level retries
     * (SQS visibility timeout, Spring Retry on adapters) handle transient failures.</p>
     */
    @Bean
    public SendNotificationUseCase sendNotificationUseCase(
            ChannelDispatchRouter            channelRouter,
            TemplateRenderPort               templateRenderer,
            NotificationLogRepositoryPort    logRepository,
            DeadLetterQueuePort              deadLetterQueue) {
        return new SendNotificationService(channelRouter, templateRenderer, logRepository, deadLetterQueue);
    }

    @Bean
    public ManageTemplateUseCase manageTemplateUseCase(TemplateRepositoryPort templateRepositoryPort) {
        return new ManageTemplateService(templateRepositoryPort);
    }
}
