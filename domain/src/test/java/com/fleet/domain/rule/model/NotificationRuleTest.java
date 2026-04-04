package com.fleet.domain.rule.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRuleTest {

    @Test
    void shouldBeSatisfiedByPayload() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("s1");
        ConditionNode speedHigh = new ConditionNode("speed", ">", 80);
        
        NotificationRule rule = new NotificationRule(ruleId, tenantId, serviceId, "SPEEDING", speedHigh, true, 5);
        
        EventPayload payload = new EventPayload("v1", Map.of("speed", 100));
        assertTrue(rule.isSatisfiedBy(payload));
    }

    @Test
    void shouldNotBeSatisfiedWhenInactive() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("s1");
        ConditionNode speedHigh = new ConditionNode("speed", ">", 80);
        
        NotificationRule rule = new NotificationRule(ruleId, tenantId, serviceId, "SPEEDING", speedHigh, false, 5);
        
        EventPayload payload = new EventPayload("v1", Map.of("speed", 100));
        assertFalse(rule.isSatisfiedBy(payload));
    }

    @Test
    void shouldNotBeSatisfiedWhenConditionRootIsNull() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("s1");
        
        NotificationRule rule = new NotificationRule(ruleId, tenantId, serviceId, "SPEEDING", null, true, 5);
        
        EventPayload payload = new EventPayload("v1", Map.of("speed", 100));
        assertFalse(rule.isSatisfiedBy(payload));
    }
}
