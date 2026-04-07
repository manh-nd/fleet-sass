package com.fleet.domain.rule.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.Operator;
import com.fleet.domain.rule.vo.EventPayload;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRuleTest {

    private final TenantId tenantId = new TenantId(UUID.randomUUID());
    private final ServiceId serviceId = new ServiceId("s1");
    private final ConditionNode speedCondition = new ConditionNode("speed", Operator.GT, 80);

    @Test
    void shouldBeSatisfiedByPayload() {
        NotificationRule rule = activeRule(speedCondition);
        assertTrue(rule.isSatisfiedBy(new EventPayload("ref-1", Map.of("speed", 100))));
    }

    @Test
    void shouldNotBeSatisfiedWhenConditionDoesNotMatch() {
        NotificationRule rule = activeRule(speedCondition);
        assertFalse(rule.isSatisfiedBy(new EventPayload("ref-1", Map.of("speed", 60))));
    }

    @Test
    void shouldNotBeSatisfiedWhenInactive() {
        NotificationRule rule = NotificationRule.create(tenantId, serviceId, "SPEEDING", speedCondition, 5, false);
        assertFalse(rule.isSatisfiedBy(new EventPayload("ref-1", Map.of("speed", 100))));
    }

    @Test
    void shouldNotBeSatisfiedWhenConditionRootIsNull() {
        // reconstitute allows null conditionRoot (for rules loaded from DB mid-migration)
        NotificationRule rule = NotificationRule.reconstitute(
                null, tenantId, serviceId, "SPEEDING", null, true, 5);
        assertFalse(rule.isSatisfiedBy(new EventPayload("ref-1", Map.of("speed", 100))));
    }

    @Test
    void createShouldThrowOnBlankEventType() {
        assertThrows(IllegalArgumentException.class,
                () -> NotificationRule.create(tenantId, serviceId, " ", speedCondition, 5, true));
    }

    @Test
    void createShouldThrowOnNegativeCooldown() {
        assertThrows(IllegalArgumentException.class,
                () -> NotificationRule.create(tenantId, serviceId, "SPEEDING", speedCondition, -1, true));
    }

    @Test
    void activateShouldReturnActiveRule() {
        NotificationRule inactive = NotificationRule.create(tenantId, serviceId, "SPEEDING", speedCondition, 5, false);
        NotificationRule active = inactive.activate();
        assertTrue(active.isActive());
        assertFalse(inactive.isActive()); // original unchanged
    }

    @Test
    void deactivateShouldReturnInactiveRule() {
        NotificationRule active = activeRule(speedCondition);
        NotificationRule inactive = active.deactivate();
        assertFalse(inactive.isActive());
        assertTrue(active.isActive()); // original unchanged
    }

    // ---- Helpers ----

    private NotificationRule activeRule(ConditionNode condition) {
        return NotificationRule.create(tenantId, serviceId, "SPEEDING", condition, 5, true);
    }
}
