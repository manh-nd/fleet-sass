package com.fleet.infrastructure.adapter.in.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fleet.application.entitlement.usecase.CheckEntitlementUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/entitlements")
@RequiredArgsConstructor
@Tag(name = "Entitlements", description = "Operations for checking service entitlements")
public class EntitlementController {

    private final CheckEntitlementUseCase checkEntitlementUseCase;

    @GetMapping("/check")
    @Operation(summary = "Check service entitlement", description = "Verifies if a specific tenant is entitled to use a specific service.")
    @ApiResponse(responseCode = "200", description = "Successfully checked entitlement")
    public boolean check(
            @Parameter(description = "Tenant ID to check", required = true) @RequestParam UUID tenantId,
            @Parameter(description = "Service identifier to check", required = true) @RequestParam String serviceId) {
        return checkEntitlementUseCase.check(new TenantId(tenantId), new ServiceId(serviceId));
    }

}
