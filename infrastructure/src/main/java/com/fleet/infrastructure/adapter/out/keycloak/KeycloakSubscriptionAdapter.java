package com.fleet.infrastructure.adapter.out.keycloak;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.data.redis.core.StringRedisTemplate;
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
 *
 * <p>Maps Tenants → Keycloak Groups and Subscriptions → Group Attributes.</p>
 *
 * <p><strong>Performance:</strong> tenant-to-group-ID mappings are cached in Redis
 * under the key {@code kc:group:{tenantId}} with a 10-minute TTL. This avoids
 * fetching the full group list on every entitlement check — which becomes O(n)
 * with many tenants.</p>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class KeycloakSubscriptionAdapter implements SubscriptionRepositoryPort {

    private static final String GROUP_CACHE_PREFIX = "kc:group:";
    private static final Duration GROUP_CACHE_TTL  = Duration.ofMinutes(10);

    private final Keycloak              keycloak;
    private final FleetProperties       properties;
    private final StringRedisTemplate   redisTemplate;

    @Override
    public Optional<TenantSubscription> findSubscription(TenantId tenantId, ServiceId serviceId) {
        log.debug("Finding subscription for tenant {} and service {}", tenantId.value(), serviceId.value());
        String realm = properties.getKeycloak().getRealm();

        return findGroupForTenant(tenantId, realm)
                .flatMap(group -> mapToSubscription(group, tenantId, serviceId));
    }

    // ---- Private helpers ----

    /**
     * Resolves the Keycloak group for a tenant.
     *
     * <p>Strategy:
     * <ol>
     *   <li>Check Redis: if the group ID is cached, fetch by ID directly (O(1)).</li>
     *   <li>Cache miss: scan all groups once, find by {@code tenant_id} attribute,
     *       then cache the discovered group ID for future calls.</li>
     * </ol>
     * </p>
     */
    private Optional<GroupRepresentation> findGroupForTenant(TenantId tenantId, String realm) {
        String cacheKey     = GROUP_CACHE_PREFIX + tenantId.value();
        String cachedGroupId = redisTemplate.opsForValue().get(cacheKey);

        if (cachedGroupId != null) {
            log.debug("Cache hit for tenant {} → group {}", tenantId.value(), cachedGroupId);
            try {
                GroupRepresentation group = keycloak.realm(realm).groups().group(cachedGroupId).toRepresentation();
                return Optional.ofNullable(group);
            } catch (Exception e) {
                // Stale cache entry — invalidate and fall through to full scan
                log.warn("Cached group ID {} no longer exists in Keycloak, invalidating cache", cachedGroupId);
                redisTemplate.delete(cacheKey);
            }
        }

        // Full scan (O(n)) — only runs on first access or after cache invalidation
        log.debug("Cache miss for tenant {} — scanning all Keycloak groups", tenantId.value());
        String tenantIdStr = tenantId.value().toString();

        Optional<GroupRepresentation> group = keycloak.realm(realm).groups().groups().stream()
                .filter(g -> hasTenantId(g, tenantIdStr))
                .findFirst();

        group.ifPresent(g -> {
            redisTemplate.opsForValue().set(cacheKey, g.getId(), GROUP_CACHE_TTL);
            log.debug("Cached tenant {} → group {} (TTL {})", tenantId.value(), g.getId(), GROUP_CACHE_TTL);
        });

        return group;
    }

    private boolean hasTenantId(GroupRepresentation group, String tenantId) {
        Map<String, List<String>> attributes = group.getAttributes();
        if (attributes == null) return false;
        List<String> ids = attributes.get("tenant_id");
        return ids != null && ids.contains(tenantId);
    }

    private Optional<TenantSubscription> mapToSubscription(
            GroupRepresentation group, TenantId tenantId, ServiceId serviceId) {

        Map<String, List<String>> attributes = group.getAttributes();
        if (attributes == null) return Optional.empty();

        String serviceName = serviceId.value();
        String statusKey   = "sub:" + serviceName + ":status";
        String expiryKey   = "sub:" + serviceName + ":expiry";

        List<String> statusValues = attributes.get(statusKey);
        List<String> expiryValues = attributes.get(expiryKey);

        if (statusValues == null || statusValues.isEmpty()
                || expiryValues == null || expiryValues.isEmpty()) {
            log.warn("Subscription attributes missing for group {} and service {}",
                    group.getName(), serviceName);
            return Optional.empty();
        }

        try {
            TenantSubscription.SubscriptionStatus status =
                    TenantSubscription.SubscriptionStatus.valueOf(statusValues.get(0).toUpperCase());
            Instant validUntil = Instant.parse(expiryValues.get(0));
            return Optional.of(new TenantSubscription(tenantId, serviceId, status, validUntil));
        } catch (Exception e) {
            log.error("Failed to parse subscription attributes for tenant {}: {}",
                    tenantId.value(), e.getMessage());
            return Optional.empty();
        }
    }
}
