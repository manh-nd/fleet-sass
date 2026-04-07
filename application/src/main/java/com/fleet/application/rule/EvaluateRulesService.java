package com.fleet.application.rule;

import java.util.ArrayList;
import java.util.List;

import com.fleet.application.rule.port.out.RuleEventPublisherPort;
import com.fleet.application.rule.usecase.EvaluateRulesUseCase;
import com.fleet.application.shared.event.RuleTriggeredEvent;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.port.out.CooldownPort;
import com.fleet.domain.rule.port.out.RuleRepositoryPort;
import com.fleet.domain.rule.vo.EventPayload;

import lombok.RequiredArgsConstructor;

/**
 * Service implementation for rule evaluation.
 * Orchestrates rule selection, AST matching, cooldown checks, and event publishing.
 */
@RequiredArgsConstructor
public class EvaluateRulesService implements EvaluateRulesUseCase {

    private final RuleRepositoryPort ruleRepository;
    private final CooldownPort cooldownPort;
    private final RuleEventPublisherPort eventPublisher;

    @Override
    public List<NotificationRule> evaluate(TenantId tenantId, String eventType, EventPayload payload) {
        List<NotificationRule> activeRules = ruleRepository.findActiveRules(tenantId, eventType);
        List<NotificationRule> triggeredRules = new ArrayList<>();
        for (NotificationRule rule : activeRules) {
            if (cooldownPort.isOnCooldown(rule.getId(), payload.referenceId())) {
                continue;
            }
            if (rule.isSatisfiedBy(payload)) {
                // Atomically acquire the cooldown lock to prevent concurrent duplicate triggers
                if (cooldownPort.tryAcquireCooldown(rule.getId(), payload.referenceId(), rule.getCooldownMinutes())) {
                    triggeredRules.add(rule);
                    eventPublisher.publish(new RuleTriggeredEvent(rule.getId(), payload));
                }
            }
        }
        return triggeredRules;
    }
}
