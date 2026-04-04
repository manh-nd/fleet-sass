package com.fleet.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fleet.application.rule.EvaluateRulesService;
import com.fleet.application.rule.port.out.RuleEventPublisherPort;
import com.fleet.application.rule.usecase.EvaluateRulesUseCase;
import com.fleet.domain.rule.port.out.CooldownPort;
import com.fleet.domain.rule.port.out.RuleRepositoryPort;

@Configuration
public class RuleConfig {

    @Bean
    public EvaluateRulesUseCase evaluateRulesUseCase(RuleRepositoryPort ruleRepositoryPort, CooldownPort cooldownPort,
            RuleEventPublisherPort ruleEventPublisherPort) {
        return new EvaluateRulesService(ruleRepositoryPort, cooldownPort, ruleEventPublisherPort);
    }

    @Bean
    public com.fleet.application.rule.usecase.ManageNotificationRuleUseCase manageNotificationRuleUseCase(RuleRepositoryPort ruleRepositoryPort) {
        return new com.fleet.application.rule.ManageNotificationRuleService(ruleRepositoryPort);
    }
}
