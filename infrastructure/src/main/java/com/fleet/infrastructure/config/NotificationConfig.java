package com.fleet.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fleet.application.notification.DispatchAlertService;
import com.fleet.application.notification.usecase.DispatchAlertUseCase;
import com.fleet.domain.notification.port.out.NotificationActionRepositoryPort;
import com.fleet.domain.notification.port.out.NotificationDispatcherPort;
import com.fleet.domain.notification.port.out.SubscriptionCheckPort;

/**
 * Spring configuration for notification-related beans.
 */
@Configuration
public class NotificationConfig {
    @Bean
    public DispatchAlertUseCase dispatchAlertUseCase(
            NotificationActionRepositoryPort actionRepositoryPort,
            SubscriptionCheckPort subscriptionCheckPort,
            NotificationDispatcherPort dispatcherPort) {
        return new DispatchAlertService(actionRepositoryPort, subscriptionCheckPort, dispatcherPort);
    }
}
