package com.fleet.infrastructure.adapter.out.redis;

import com.fleet.domain.rule.port.out.CooldownPort;
import com.fleet.domain.rule.vo.RuleId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based implementation of {@link CooldownPort}.
 * Uses Redis TTL and set-if-absent (SETNX) to manage per-entity, per-rule cooldowns
 * safely across multiple service instances.
 */
@Repository
@RequiredArgsConstructor
public class RedisCooldownAdapter implements CooldownPort {

    private final StringRedisTemplate redisTemplate;

    /**
     * Builds the Redis key for a rule + entity combination.
     * Format: {@code cooldown:rule:{ruleId}:ref:{referenceId}}
     */
    private String buildKey(RuleId ruleId, String referenceId) {
        return "cooldown:rule:" + ruleId.value() + ":ref:" + referenceId;
    }

    @Override
    public boolean isOnCooldown(RuleId ruleId, String referenceId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(ruleId, referenceId)));
    }

    @Override
    public void setCooldown(RuleId ruleId, String referenceId, int cooldownMinutes) {
        if (cooldownMinutes <= 0) {
            return; // Zero-minute cooldown means no suppression
        }
        redisTemplate.opsForValue().set(buildKey(ruleId, referenceId), "LOCKED", cooldownMinutes, TimeUnit.MINUTES);
    }

    @Override
    public boolean tryAcquireCooldown(RuleId ruleId, String referenceId, int cooldownMinutes) {
        if (cooldownMinutes <= 0) {
            return true; // No cooldown configured — always allow
        }
        String key = buildKey(ruleId, referenceId);
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", cooldownMinutes, TimeUnit.MINUTES));
    }
}