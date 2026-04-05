package com.fleet.application.entitlement;

import java.time.Instant;

import com.fleet.application.entitlement.usecase.CheckEntitlementUseCase;
import com.fleet.domain.entitlement.port.out.SubscriptionRepositoryPort;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

import lombok.RequiredArgsConstructor;

/**
 * Service implementation for checking service entitlements.
 * Verifies if a tenant has a valid subscription for a given service at the current time.
 */
@RequiredArgsConstructor
public class CheckEntitlementService implements CheckEntitlementUseCase {

    private final SubscriptionRepositoryPort repository;

    @Override
    public boolean check(TenantId tenantId, ServiceId serviceId) {
        return repository.findSubscription(tenantId, serviceId)
                .map(sub -> sub.isValidAt(Instant.now()))
                .orElse(false);
    }
}
