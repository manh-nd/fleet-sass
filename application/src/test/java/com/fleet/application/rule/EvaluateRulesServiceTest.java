package com.fleet.application.rule;

import com.fleet.application.rule.port.out.RuleEventPublisherPort;
import com.fleet.application.shared.event.RuleTriggeredEvent;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.port.out.CooldownPort;
import com.fleet.domain.rule.port.out.RuleRepositoryPort;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluateRulesServiceTest {

    @Mock private RuleRepositoryPort ruleRepository;
    @Mock private CooldownPort cooldownPort;
    @Mock private RuleEventPublisherPort eventPublisher;

    @InjectMocks
    private EvaluateRulesService service;

    @Test
    void shouldSkipWhenOnCooldown() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EventPayload payload = new EventPayload("ref-1", Map.of());

        NotificationRule rule = NotificationRule.reconstitute(
                ruleId, tenantId, new ServiceId("S1"), "SPEEDING", mock(RuleNode.class), true, 5);

        when(ruleRepository.findActiveRules(tenantId, "SPEEDING")).thenReturn(List.of(rule));
        when(cooldownPort.isOnCooldown(ruleId, "ref-1")).thenReturn(true);

        List<NotificationRule> result = service.evaluate(tenantId, "SPEEDING", payload);

        assertTrue(result.isEmpty());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldTriggerEventWhenRuleSatisfiedAndNotInCooldown() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EventPayload payload = new EventPayload("ref-1", Map.of("speed", 100));

        RuleNode node = mock(RuleNode.class);
        NotificationRule rule = NotificationRule.reconstitute(
                ruleId, tenantId, new ServiceId("S1"), "SPEEDING", node, true, 5);

        when(ruleRepository.findActiveRules(tenantId, "SPEEDING")).thenReturn(List.of(rule));
        when(cooldownPort.isOnCooldown(ruleId, "ref-1")).thenReturn(false);
        when(node.evaluate(payload)).thenReturn(true);
        when(cooldownPort.tryAcquireCooldown(ruleId, "ref-1", 5)).thenReturn(true);

        List<NotificationRule> result = service.evaluate(tenantId, "SPEEDING", payload);

        assertEquals(1, result.size());
        verify(eventPublisher).publish(any(RuleTriggeredEvent.class));
    }

    @Test
    void shouldNotTriggerEventWhenSatisfiedButCooldownAcquisitionFails() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        RuleId ruleId = new RuleId(UUID.randomUUID());
        EventPayload payload = new EventPayload("ref-1", Map.of("speed", 100));

        RuleNode node = mock(RuleNode.class);
        NotificationRule rule = NotificationRule.reconstitute(
                ruleId, tenantId, new ServiceId("S1"), "SPEEDING", node, true, 5);

        when(ruleRepository.findActiveRules(tenantId, "SPEEDING")).thenReturn(List.of(rule));
        when(cooldownPort.isOnCooldown(ruleId, "ref-1")).thenReturn(false);
        when(node.evaluate(payload)).thenReturn(true);
        // Atomic acquisition fails — another instance already claimed this cooldown
        when(cooldownPort.tryAcquireCooldown(ruleId, "ref-1", 5)).thenReturn(false);

        List<NotificationRule> result = service.evaluate(tenantId, "SPEEDING", payload);

        assertTrue(result.isEmpty());
        verify(eventPublisher, never()).publish(any());
    }
}
