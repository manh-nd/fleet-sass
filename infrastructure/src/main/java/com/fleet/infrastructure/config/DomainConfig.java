package com.fleet.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fleet.application.entitlement.CheckEntitlementService;
import com.fleet.application.entitlement.usecase.CheckEntitlementUseCase;
import com.fleet.domain.entitlement.port.out.SubscriptionRepositoryPort;

@Configuration
public class DomainConfig {
    @Bean
    public CheckEntitlementUseCase checkEntitlementUseCase(SubscriptionRepositoryPort repositoryPort) {
        return new CheckEntitlementService(repositoryPort);
    }
}
