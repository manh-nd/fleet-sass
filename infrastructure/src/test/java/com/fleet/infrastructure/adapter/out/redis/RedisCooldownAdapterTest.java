package com.fleet.infrastructure.adapter.out.redis;

import com.fleet.domain.rule.vo.RuleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class RedisCooldownAdapterTest {

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    private RedisCooldownAdapter adapter;

    @BeforeEach
    void setUp() {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379))
        );
        connectionFactory.afterPropertiesSet();
        
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        template.afterPropertiesSet();
        
        adapter = new RedisCooldownAdapter(template);
    }

    @Test
    void shouldAcquireCooldown() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        String vehicleId = "VEHICLE-1";
        
        // First acquisition should succeed
        assertTrue(adapter.tryAcquireCooldown(ruleId, vehicleId, 5));
        
        // Second acquisition for same rule+vehicle should fail
        assertFalse(adapter.tryAcquireCooldown(ruleId, vehicleId, 5));
        
        // Check isOnCooldown
        assertTrue(adapter.isOnCooldown(ruleId, vehicleId));
    }

    @Test
    void shouldBeFreeWhenNotSet() {
        RuleId ruleId = new RuleId(UUID.randomUUID());
        String vehicleId = "VEHICLE-2";
        
        assertFalse(adapter.isOnCooldown(ruleId, vehicleId));
    }
}
