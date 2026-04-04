package com.fleet.application.entitlement.usecase;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

public interface CheckEntitlementUseCase {
    boolean check(TenantId tenantId, ServiceId serviceId);
}
