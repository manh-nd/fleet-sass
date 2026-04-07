package com.fleet.infrastructure.adapter.in.web;

import com.fleet.domain.entitlement.model.ApiKey;
import com.fleet.domain.entitlement.port.out.ApiKeyRepositoryPort;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing service API keys.
 *
 * <p>API keys provide machine-to-machine authentication without requiring Keycloak tokens.
 * The plaintext key is only returned at creation time — subsequent lookups show only the
 * first 8 characters (masked) and metadata.</p>
 *
 * <p>Base path: {@code /api/v1/api-keys}</p>
 */
@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "Manage service-to-service API keys")
public class ApiKeyController {

    private final ApiKeyRepositoryPort apiKeyRepository;

    /**
     * Issues a new API key for a service.
     *
     * <p><b>The plaintext key is returned only once in this response — store it securely.</b></p>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Issue a new API key",
               description = "Creates and stores a new API key. The plaintext key is returned only once — it cannot be retrieved later.")
    public Map<String, Object> create(
            @RequestParam UUID tenantId,
            @RequestParam String serviceId,
            @RequestParam String description,
            @RequestParam(required = false) Instant expiresAt) {

        String plaintext = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String keyHash = hash(plaintext);

        ApiKey apiKey = ApiKey.create(
                new TenantId(tenantId),
                new ServiceId(serviceId),
                keyHash,
                description,
                expiresAt);
        apiKeyRepository.save(apiKey);

        return Map.of(
                "id",          apiKey.getId(),
                "key",         plaintext,   // Shown once only
                "serviceId",   serviceId,
                "description", description,
                "expiresAt",   expiresAt != null ? expiresAt.toString() : "never",
                "warning",     "Store this key securely. It will not be shown again.");
    }

    /**
     * Lists all API keys for a tenant (metadata only — never the plaintext or full hash).
     */
    @GetMapping
    @Operation(summary = "List API keys for a tenant")
    public List<Map<String, Object>> listByTenant(@RequestParam UUID tenantId) {
        return apiKeyRepository.findByTenant(new TenantId(tenantId))
                .stream()
                .map(k -> Map.<String, Object>of(
                        "id",          k.getId(),
                        "serviceId",   k.getServiceId().value(),
                        "description", k.getDescription() != null ? k.getDescription() : "",
                        "active",      k.isActive(),
                        "valid",       k.isValid(),
                        "createdAt",   k.getCreatedAt().toString(),
                        "expiresAt",   k.getExpiresAt() != null ? k.getExpiresAt().toString() : "never"))
                .toList();
    }

    /**
     * Revokes an API key, immediately rendering it invalid.
     * The cached entry (Redis) is also evicted via {@code @CacheEvict} in the adapter.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke an API key",
               description = "Marks the key inactive. Active requests using this key will be rejected after the Redis cache TTL expires.")
    public ResponseEntity<Void> revoke(
            @PathVariable UUID id,
            @RequestParam UUID tenantId) {
        Optional<ApiKey> apiKey = apiKeyRepository.findByTenant(new TenantId(tenantId)).stream()
                .filter(k -> k.getId().equals(id))
                .findFirst();
        if (apiKey.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ApiKey k = apiKey.get();
        apiKeyRepository.revoke(k.revoke());
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String hash(String plaintext) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(plaintext.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
