package com.fleet.infrastructure.adapter.out.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Redis-backed cache for user granular permissions.
 * Keys are structured as "perm_cache:{userId}:{tenantId}".
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisPermissionCache {

    private static final String KEY_PREFIX = "perm_cache:";
    private final StringRedisTemplate redisTemplate;

    /**
     * Retrieves cached permissions for a user-tenant context.
     */
    public Optional<List<String>> getPermissions(String userId, String tenantId) {
        String key = buildKey(userId, tenantId);
        String val = redisTemplate.opsForValue().get(key);
        
        if (val == null) return Optional.empty();
        
        if (val.isEmpty()) return Optional.of(Collections.emptyList());
        
        return Optional.of(Arrays.asList(val.split(",")));
    }

    /**
     * Caches permissions for a user-tenant context.
     */
    public void putPermissions(String userId, String tenantId, List<String> permissions, Duration ttl) {
        String key = buildKey(userId, tenantId);
        String val = String.join(",", permissions);
        redisTemplate.opsForValue().set(key, val, ttl);
    }

    private String buildKey(String userId, String tenantId) {
        return KEY_PREFIX + userId + ":" + tenantId;
    }
}
