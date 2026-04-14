package com.fleet.infrastructure.adapter.out.keycloak;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.stereotype.Repository;

import com.fleet.domain.entitlement.model.TenantSubscription;
import com.fleet.domain.entitlement.port.out.SubscriptionRepositoryPort;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.infrastructure.config.FleetProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Keycloak implementation of {@link SubscriptionRepositoryPort}.
 * Maps Tenants to Keycloak Groups and Subscriptions to Group Attributes.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class KeycloakSubscriptionAdapter implements SubscriptionRepositoryPort {

    private final Keycloak keycloak;
    private final FleetProperties properties;

    @Override
    public Optional<TenantSubscription> findSubscription(TenantId tenantId, ServiceId serviceId) {
        log.debug("Finding subscription for tenant {} and service {}", tenantId.value(), serviceId.value());
        String realm = properties.getKeycloak().getRealm();

        return findGroupForTenant(tenantId, realm)
                .flatMap(group -> mapToSubscription(group, tenantId, serviceId));
    }

    private Optional<GroupRepresentation> findGroupForTenant(TenantId tenantId, String realm) {
        String tenantIdStr = tenantId.value().toString();
        
        // Fetch all groups and look for the one with the matching tenant_id attribute.
        // In a production environment with many tenants, this should be optimized
        // (e.g., via specialized search or by naming groups 'tenant:<uuid>').
        return keycloak.realm(realm).groups().groups().stream()
                .filter(g -> hasTenantId(g, tenantIdStr))
                .findFirst();
    }

    private boolean hasTenantId(GroupRepresentation group, String tenantId) {
        Map<String, List<String>> attributes = group.getAttributes();
        if (attributes == null) return false;
        
        List<String> ids = attributes.get("tenant_id");
        return ids != null && ids.contains(tenantId);
    }

    private Optional<TenantSubscription> mapToSubscription(GroupRepresentation group, TenantId tenantId, ServiceId serviceId) {
        Map<String, List<String>> attributes = group.getAttributes();
        if (attributes == null) return Optional.empty();

        String serviceName = serviceId.value();
        String statusKey = "sub:" + serviceName + ":status";
        String expiryKey = "sub:" + serviceName + ":expiry";

        List<String> statusValues = attributes.get(statusKey);
        List<String> expiryValues = attributes.get(expiryKey);

        if (statusValues == null || statusValues.isEmpty() || expiryValues == null || expiryValues.isEmpty()) {
            log.warn("Subscription attributes missing for group {} and service {}", group.getName(), serviceName);
            return Optional.empty();
        }

        try {
            TenantSubscription.SubscriptionStatus status = TenantSubscription.SubscriptionStatus.valueOf(statusValues.get(0).toUpperCase());
            Instant validUntil = Instant.parse(expiryValues.get(0));

            return Optional.of(new TenantSubscription(tenantId, serviceId, status, validUntil));
        } catch (Exception e) {
            log.error("Failed to parse subscription attributes for tenant {}: {}", tenantId.value(), e.getMessage());
            return Optional.empty();
        }
    }
}
