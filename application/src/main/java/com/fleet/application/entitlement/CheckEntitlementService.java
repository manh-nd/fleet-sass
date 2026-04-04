package com.fleet.application.entitlement;

import java.time.Instant;

import com.fleet.application.entitlement.usecase.CheckEntitlementUseCase;
import com.fleet.domain.entitlement.model.TenantSubscription;
import com.fleet.domain.entitlement.port.out.SubscriptionRepositoryPort;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckEntitlementService implements CheckEntitlementUseCase {

    private final SubscriptionRepositoryPort repository;

    @Override
    public boolean check(TenantId tenantId, ServiceId serviceId) {
        TenantSubscription sub = repository.findSubscription(tenantId, serviceId);
        if (sub == null) {
            return false;
        }
        return sub.isValidAt(Instant.now());
    }
}
