package com.fleet.infrastructure.adapter.in.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fleet.application.entitlement.usecase.CheckEntitlementUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/entitlements")
@RequiredArgsConstructor
public class EntitlementController {

    private final CheckEntitlementUseCase checkEntitlementUseCase;

    @GetMapping("/check")
    public boolean check(@RequestParam UUID tenantId, @RequestParam String serviceId) {
        return checkEntitlementUseCase.check(new TenantId(tenantId), new ServiceId(serviceId));
    }

}
