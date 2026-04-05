package com.fleet.infrastructure.adapter.out.inmemory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fleet.domain.entitlement.model.TenantSubscription;
import com.fleet.domain.entitlement.port.out.SubscriptionRepositoryPort;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

/**
 * In-memory stub implementation of {@link SubscriptionRepositoryPort}.
 * All tenants are treated as having an active subscription — used for local dev / testing.
 */
@Repository
public class InMemorySubscriptionAdapter implements SubscriptionRepositoryPort {

    @Override
    public Optional<TenantSubscription> findSubscription(TenantId tenantId, ServiceId serviceId) {
        return Optional.of(new TenantSubscription(
                tenantId,
                serviceId,
                TenantSubscription.SubscriptionStatus.ACTIVE,
                Instant.now().plus(Duration.ofHours(10))));
    }
}
