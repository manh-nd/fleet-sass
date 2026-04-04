package com.fleet.application.rule;

import com.fleet.application.rule.usecase.ManageNotificationRuleUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.port.out.RuleRepositoryPort;
import com.fleet.domain.rule.vo.RuleId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ManageNotificationRuleService implements ManageNotificationRuleUseCase {

    private final RuleRepositoryPort ruleRepository;

    @Override
    public void createRule(TenantId tenantId, ServiceId serviceId, String eventType, RuleNode conditionRoot, int cooldownMinutes, boolean isActive) {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        NotificationRule rule = new NotificationRule(
            ruleId, 
            tenantId, 
            serviceId, 
            eventType, 
            conditionRoot, 
            isActive, 
            cooldownMinutes
        );
        ruleRepository.save(rule);
    }

    @Override
    public void updateRule(RuleId ruleId, TenantId tenantId, ServiceId serviceId, String eventType, RuleNode conditionRoot, int cooldownMinutes, boolean isActive) {
        NotificationRule rule = new NotificationRule(
            ruleId, 
            tenantId, 
            serviceId, 
            eventType, 
            conditionRoot, 
            isActive, 
            cooldownMinutes
        );
        ruleRepository.update(rule);
    }

    @Override
    public void deleteRule(RuleId ruleId, TenantId tenantId) {
        ruleRepository.delete(ruleId, tenantId);
    }
}
