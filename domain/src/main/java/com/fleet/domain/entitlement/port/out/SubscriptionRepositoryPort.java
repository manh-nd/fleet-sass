package com.fleet.domain.entitlement.port.out;

import com.fleet.domain.entitlement.model.TenantSubscription;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

public interface SubscriptionRepositoryPort {
    TenantSubscription findSubscription(TenantId tenantId, ServiceId serviceId);
}
