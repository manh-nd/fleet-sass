package com.fleet.domain.entitlement.port.out;

import com.fleet.domain.entitlement.model.ApiKey;
import com.fleet.domain.entitlement.vo.TenantId;

import java.util.Optional;

/**
 * Outbound port for API key lookup.
 *
 * <p>The hash lookup method is performance-critical — it is called on every inbound
 * request that uses API key authentication. Implementations should cache results
 * (e.g. Redis with a short TTL) to avoid repeated DB hits.</p>
 */
public interface ApiKeyRepositoryPort {

    /**
     * Finds an active API key by its SHA-256 hash.
     * Returns empty if the key does not exist, is inactive, or is expired.
     *
     * @param keyHash SHA-256 hex digest of the plaintext API key
     */
    Optional<ApiKey> findByHash(String keyHash);

    /** Persists a new API key. */
    void save(ApiKey apiKey);

    /** Revokes an existing key (marks it inactive). */
    void revoke(ApiKey apiKey);

    /** Lists all keys for a tenant (for management UI). */
    java.util.List<ApiKey> findByTenant(TenantId tenantId);
}
