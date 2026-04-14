package com.fleet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fleet.application.rule.ManageNotificationRuleService;
import com.fleet.application.rule.EvaluateRulesService;
import com.fleet.application.entitlement.usecase.CheckEntitlementUseCase;
import com.fleet.application.rule.port.out.RuleEventPublisherPort;
import com.fleet.application.rule.usecase.ManageNotificationRuleUseCase;
import com.fleet.domain.rule.port.out.CooldownPort;
import com.fleet.domain.rule.port.out.RuleRepositoryPort;

/**
 * Spring configuration for rule-related beans.
 * Defines the mapping between application use cases and their dependencies.
 */
@Configuration
public class RuleConfig {

    @Bean
    public EvaluateRulesService evaluateRulesService(
            RuleRepositoryPort ruleRepositoryPort,
            CooldownPort cooldownPort,
            RuleEventPublisherPort ruleEventPublisherPort,
            CheckEntitlementUseCase checkEntitlementUseCase) {
        return new EvaluateRulesService(ruleRepositoryPort, cooldownPort, ruleEventPublisherPort,
                checkEntitlementUseCase);
    }

    @Bean
    public ManageNotificationRuleUseCase manageNotificationRuleUseCase(RuleRepositoryPort ruleRepositoryPort) {
        return new ManageNotificationRuleService(ruleRepositoryPort);
    }
}
