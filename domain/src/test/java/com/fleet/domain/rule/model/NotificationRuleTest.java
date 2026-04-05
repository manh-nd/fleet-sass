package com.fleet.domain.rule.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.Operator;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRuleTest {

    @Test
    void shouldBeSatisfiedByPayload() {
        ConditionNode speedHigh = new ConditionNode("speed", Operator.GT, 80);
        NotificationRule rule = buildRule(speedHigh, true);

        assertTrue(rule.isSatisfiedBy(new EventPayload("v1", Map.of("speed", 100))));
    }

    @Test
    void shouldNotBeSatisfiedWhenConditionDoesNotMatch() {
        ConditionNode speedHigh = new ConditionNode("speed", Operator.GT, 80);
        NotificationRule rule = buildRule(speedHigh, true);

        // speed 60 does NOT exceed threshold of 80
        assertFalse(rule.isSatisfiedBy(new EventPayload("v1", Map.of("speed", 60))));
    }

    @Test
    void shouldNotBeSatisfiedWhenInactive() {
        ConditionNode speedHigh = new ConditionNode("speed", Operator.GT, 80);
        NotificationRule rule = buildRule(speedHigh, false);

        assertFalse(rule.isSatisfiedBy(new EventPayload("v1", Map.of("speed", 100))));
    }

    @Test
    void shouldNotBeSatisfiedWhenConditionRootIsNull() {
        NotificationRule rule = buildRule(null, true);

        assertFalse(rule.isSatisfiedBy(new EventPayload("v1", Map.of("speed", 100))));
    }

    private NotificationRule buildRule(ConditionNode condition, boolean isActive) {
        return new NotificationRule(
                new RuleId(UUID.randomUUID()),
                new TenantId(UUID.randomUUID()),
                new ServiceId("s1"),
                "SPEEDING",
                condition,
                isActive,
                5);
    }
}
