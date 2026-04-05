package com.fleet.domain;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectTest {

    @Test
    void testEmailAddressValidation() {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(null));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(""));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("  "));

        EmailAddress email = new EmailAddress("test@example.com");
        assertEquals("test@example.com", email.value());
    }

    @Test
    void testTenantIdValidation() {
        assertThrows(NullPointerException.class, () -> new TenantId(null));
        UUID uuid = UUID.randomUUID();
        TenantId id = new TenantId(uuid);
        assertEquals(uuid, id.value());
    }

    @Test
    void testRuleIdValidation() {
        // RuleId should now guard against null (parity with TenantId/ServiceId)
        assertThrows(NullPointerException.class, () -> new RuleId(null));
        UUID uuid = UUID.randomUUID();
        RuleId id = new RuleId(uuid);
        assertEquals(uuid, id.value());
    }

    @Test
    void testServiceId() {
        assertThrows(NullPointerException.class, () -> new ServiceId(null));
        ServiceId id = new ServiceId("SERVICE-1");
        assertEquals("SERVICE-1", id.value());
    }
}
