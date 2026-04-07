package com.fleet.application.rule;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.Operator;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.port.out.RuleRepositoryPort;
import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageNotificationRuleServiceTest {

    @Mock
    private RuleRepositoryPort ruleRepository;

    @InjectMocks
    private ManageNotificationRuleService service;

    @Test
    void shouldCreateAndSaveRule() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("S1");
        ConditionNode condition = new ConditionNode("speed", Operator.GT, 80);

        service.createRule(tenantId, serviceId, "SPEEDING", condition, 10, true);

        ArgumentCaptor<NotificationRule> captor = ArgumentCaptor.forClass(NotificationRule.class);
        verify(ruleRepository).save(captor.capture());

        NotificationRule saved = captor.getValue();
        assertNotNull(saved.getId());
        assertEquals(tenantId, saved.getTenantId());
        assertEquals(serviceId, saved.getServiceId());
        assertEquals("SPEEDING", saved.getEventType());
        assertEquals(condition, saved.getConditionRoot());
        assertEquals(10, saved.getCooldownMinutes());
        assertTrue(saved.isActive());
    }

    @Test
    void shouldUpdateRule() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("S2");
        ConditionNode condition = new ConditionNode("speed", Operator.GT, 90);

        service.updateRule(ruleId, tenantId, serviceId, "SPEEDING_NEW", condition, 15, false);

        ArgumentCaptor<NotificationRule> captor = ArgumentCaptor.forClass(NotificationRule.class);
        verify(ruleRepository).update(captor.capture());

        NotificationRule updated = captor.getValue();
        assertEquals(ruleId, updated.getId());
        assertEquals(tenantId, updated.getTenantId());
        assertEquals(serviceId, updated.getServiceId());
        assertEquals("SPEEDING_NEW", updated.getEventType());
        assertEquals(condition, updated.getConditionRoot());
        assertEquals(15, updated.getCooldownMinutes());
        assertFalse(updated.isActive());
    }

    @Test
    void shouldDeleteRule() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        TenantId tenantId = new TenantId(UUID.randomUUID());

        service.deleteRule(ruleId, tenantId);

        verify(ruleRepository).delete(ruleId, tenantId);
    }

    @Test
    void shouldListRulesWithCursorPagination() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        var emptyPage = com.fleet.domain.shared.pagination.CursorPage.<com.fleet.domain.rule.model.NotificationRule>lastPage(java.util.List.of());
        doReturn(emptyPage).when(ruleRepository).findAllByTenant(tenantId, null, 20);

        var page = service.listRules(tenantId, null, 20);

        assertNotNull(page);
        assertFalse(page.hasMore());
        verify(ruleRepository).findAllByTenant(tenantId, null, 20);
    }
}
