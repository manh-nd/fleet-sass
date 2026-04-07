package com.fleet.domain.entitlement.model;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * An API key issued to a consuming service for machine-to-machine authentication.
 *
 * <p>API keys are an alternative to Keycloak JWT tokens for internal service
 * communication where OAuth2 flows are impractical (e.g. IoT devices, legacy systems).</p>
 *
 * <p>Keys are stored as SHA-256 hashes — plaintext is only shown once at creation time.</p>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiKey {

    private final UUID id;
    private final TenantId tenantId;
    private final ServiceId serviceId;

    /** SHA-256 hex digest of the plaintext key. Never store or return the plaintext. */
    private final String keyHash;

    /** Human-readable description of what this key is for. */
    private final String description;

    private final boolean active;
    private final Instant createdAt;

    /** Nullable — null means the key does not expire. */
    private final Instant expiresAt;

    // ── Factory ──────────────────────────────────────────────────────────────

    public static ApiKey create(
            TenantId tenantId,
            ServiceId serviceId,
            String keyHash,
            String description,
            Instant expiresAt) {
        return new ApiKey(
                UUID.randomUUID(),
                tenantId,
                serviceId,
                keyHash,
                description,
                true,
                Instant.now(),
                expiresAt);
    }

    public static ApiKey reconstitute(
            UUID id,
            TenantId tenantId,
            ServiceId serviceId,
            String keyHash,
            String description,
            boolean active,
            Instant createdAt,
            Instant expiresAt) {
        return new ApiKey(id, tenantId, serviceId, keyHash, description, active, createdAt, expiresAt);
    }

    // ── Business logic ────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if this key is active and not yet expired.
     */
    public boolean isValid() {
        if (!active) return false;
        if (expiresAt == null) return true;
        return Instant.now().isBefore(expiresAt);
    }

    /** Returns a revoked copy of this key. */
    public ApiKey revoke() {
        return new ApiKey(id, tenantId, serviceId, keyHash, description, false, createdAt, expiresAt);
    }
}
