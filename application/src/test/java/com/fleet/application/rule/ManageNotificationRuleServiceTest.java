package com.fleet.application.rule;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.port.out.RuleRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

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
        ConditionNode condition = new ConditionNode("speed", ">", 80);

        service.createRule(tenantId, serviceId, "SPEEDING", condition, 10, true);

        ArgumentCaptor<NotificationRule> captor = ArgumentCaptor.forClass(NotificationRule.class);
        verify(ruleRepository).save(captor.capture());

        NotificationRule savedRule = captor.getValue();
        assertNotNull(savedRule.getId());
        assertEquals(tenantId, savedRule.getTenantId());
        assertEquals(serviceId, savedRule.getServiceId());
        assertEquals("SPEEDING", savedRule.getEventType());
        assertEquals(condition, savedRule.getConditionRoot());
        assertEquals(10, savedRule.getCooldownMinutes());
        assertTrue(savedRule.isActive());
    }
}
