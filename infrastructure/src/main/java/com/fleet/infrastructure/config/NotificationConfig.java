package com.fleet.infrastructure.config;

import com.fleet.application.notification.DispatchAlertService;
import com.fleet.application.notification.ManageTemplateService;
import com.fleet.application.notification.SendNotificationService;
import com.fleet.application.notification.usecase.DispatchAlertUseCase;
import com.fleet.application.notification.usecase.ManageTemplateUseCase;
import com.fleet.application.notification.usecase.SendNotificationUseCase;
import com.fleet.domain.notification.port.out.DeadLetterQueuePort;
import com.fleet.domain.notification.port.out.NotificationActionRepositoryPort;
import com.fleet.domain.notification.port.out.NotificationDispatcherPort;
import com.fleet.domain.notification.port.out.NotificationLogRepositoryPort;
import com.fleet.domain.notification.port.out.SubscriptionCheckPort;
import com.fleet.domain.notification.port.out.TemplateRenderPort;
import com.fleet.domain.notification.port.out.TemplateRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for all notification-related application service beans.
 */
@Configuration
public class NotificationConfig {

    /**
     * Alert dispatch service — triggered by rule evaluation events.
     * Handles email subscription checking per the GDPR anti-spam contract.
     */
    @Bean
    public DispatchAlertUseCase dispatchAlertUseCase(
            NotificationActionRepositoryPort actionRepositoryPort,
            SubscriptionCheckPort subscriptionCheckPort,
            NotificationDispatcherPort dispatcherPort) {
        return new DispatchAlertService(actionRepositoryPort, subscriptionCheckPort, dispatcherPort);
    }

    /**
     * Direct-send notification service — consumed by the REST API and external services.
     * Handles template rendering, dispatch, and delivery log persistence.
     */
    @Bean
    public SendNotificationUseCase sendNotificationUseCase(
            NotificationDispatcherPort dispatcherPort,
            TemplateRenderPort templateRenderer,
            NotificationLogRepositoryPort logRepository,
            DeadLetterQueuePort deadLetterQueue) {
        return new SendNotificationService(dispatcherPort, templateRenderer, logRepository, deadLetterQueue);
    }

    @Bean
    public ManageTemplateUseCase manageTemplateUseCase(TemplateRepositoryPort templateRepositoryPort) {
        return new ManageTemplateService(templateRepositoryPort);
    }
}
