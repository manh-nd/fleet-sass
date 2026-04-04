package com.fleet.application.entitlement;

import com.fleet.domain.entitlement.model.TenantSubscription;
import com.fleet.domain.entitlement.port.out.SubscriptionRepositoryPort;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckEntitlementServiceTest {

    @Mock
    private SubscriptionRepositoryPort repository;

    @InjectMocks
    private CheckEntitlementService service;

    @Test
    void shouldReturnTrueWhenSubscriptionIsValid() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("S1");
        TenantSubscription sub = new TenantSubscription(tenantId, serviceId, 
            TenantSubscription.SubscriptionStatus.ACTIVE, Instant.now().plus(1, ChronoUnit.DAYS));
        
        when(repository.findSubscription(tenantId, serviceId)).thenReturn(sub);
        
        assertTrue(service.check(tenantId, serviceId));
    }

    @Test
    void shouldReturnFalseWhenSubscriptionIsMissing() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("S1");
        
        when(repository.findSubscription(tenantId, serviceId)).thenReturn(null);
        
        assertFalse(service.check(tenantId, serviceId));
    }

    @Test
    void shouldReturnFalseWhenSubscriptionIsExpired() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        ServiceId serviceId = new ServiceId("S1");
        TenantSubscription sub = new TenantSubscription(tenantId, serviceId, 
            TenantSubscription.SubscriptionStatus.ACTIVE, Instant.now().minus(1, ChronoUnit.DAYS));
        
        when(repository.findSubscription(tenantId, serviceId)).thenReturn(sub);
        
        assertFalse(service.check(tenantId, serviceId));
    }
}
