package com.fleet.application.entitlement;

import java.time.Instant;

import com.fleet.application.entitlement.usecase.CheckEntitlementUseCase;
import com.fleet.domain.entitlement.port.out.SubscriptionRepositoryPort;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

import lombok.RequiredArgsConstructor;

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
