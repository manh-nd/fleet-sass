package com.fleet.domain.entitlement.port.out;

import java.util.Optional;

import com.fleet.domain.entitlement.model.TenantSubscription;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

public interface SubscriptionRepositoryPort {
    Optional<TenantSubscription> findSubscription(TenantId tenantId, ServiceId serviceId);
}
