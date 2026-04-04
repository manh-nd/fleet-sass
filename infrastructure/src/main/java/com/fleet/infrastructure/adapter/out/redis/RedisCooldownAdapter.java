package com.fleet.infrastructure.adapter.out.redis;

import com.fleet.domain.rule.port.out.CooldownPort;
import com.fleet.domain.rule.vo.RuleId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RedisCooldownAdapter implements CooldownPort {

    private final StringRedisTemplate redisTemplate;

    public RedisCooldownAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(RuleId ruleId, String vehicleId) {
        // Cấu trúc Key: cooldown:rule:{id}:vehicle:{id}
        return "cooldown:rule:" + ruleId.value() + ":vehicle:" + vehicleId;
    }

    @Override
    public boolean isOnCooldown(RuleId ruleId, String vehicleId) {
        String key = buildKey(ruleId, vehicleId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void setCooldown(RuleId ruleId, String vehicleId, int cooldownMinutes) {
        if (cooldownMinutes <= 0)
            return; // Nếu khách setup 0 phút thì bỏ qua

        String key = buildKey(ruleId, vehicleId);
        // Lưu giá trị giả "LOCKED" và quan trọng nhất là set TTL tự động hết hạn
        redisTemplate.opsForValue().set(key, "LOCKED", cooldownMinutes, TimeUnit.MINUTES);
    }

    @Override
    public boolean tryAcquireCooldown(RuleId ruleId, String vehicleId, int cooldownMinutes) {
        if (cooldownMinutes <= 0) {
            return true; // No cooldown required, always acquired
        }
        
        String key = buildKey(ruleId, vehicleId);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", cooldownMinutes, TimeUnit.MINUTES));
    }
}