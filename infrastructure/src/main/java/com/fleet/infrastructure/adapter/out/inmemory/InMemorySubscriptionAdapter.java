package com.fleet.infrastructure.adapter.out.inmemory;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Repository;

import com.fleet.domain.entitlement.model.TenantSubscription;
import com.fleet.domain.entitlement.port.out.SubscriptionRepositoryPort;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

@Repository
public class InMemorySubscriptionAdapter implements SubscriptionRepositoryPort {

    @Override
    public TenantSubscription findSubscription(TenantId tenantId, ServiceId serviceId) {
        return new TenantSubscription(
                tenantId,
                serviceId,
                TenantSubscription.SubscriptionStatus.ACTIVE,
                Instant.now().plus(Duration.ofHours(10)));
    }

}
