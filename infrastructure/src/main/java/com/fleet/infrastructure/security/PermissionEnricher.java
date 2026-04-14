package com.fleet.infrastructure.security;

import com.fleet.infrastructure.adapter.out.keycloak.KeycloakUserInfoClient;
import com.fleet.infrastructure.adapter.out.redis.RedisPermissionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the cache-aside logic for permission enrichment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionEnricher {

    private final KeycloakUserInfoClient userInfoClient;
    private final RedisPermissionCache permissionCache;

    // Cache TTL matched to accessTokenLifespan in fleet-realm.json (30m)
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * Retrieves granular permissions for a user, using Redis as a cache.
     * Hits Keycloak UserInfo endpoint on cache miss.
     */
    public List<String> getEnrichedPermissions(String userId, String tenantId, String accessToken) {
        Optional<List<String>> cachedPerms = permissionCache.getPermissions(userId, tenantId);
        
        if (cachedPerms.isPresent()) {
            log.trace("Cache hit for user {} permissions", userId);
            return cachedPerms.get();
        }

        log.debug("Cache miss for user {}. Fetching from Keycloak UserInfo...", userId);
        List<String> keycloakRoles = userInfoClient.fetchUserRoles(accessToken);
        
        permissionCache.putPermissions(userId, tenantId, keycloakRoles, CACHE_TTL);
        return keycloakRoles;
    }
}
