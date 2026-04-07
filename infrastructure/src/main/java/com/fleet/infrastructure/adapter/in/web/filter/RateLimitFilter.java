package com.fleet.infrastructure.adapter.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Token-bucket rate limiter using Redis as the distributed counter.
 *
 * <p>Limits are applied per API key (or IP address as a fallback).
 * The default policy is <b>100 requests per minute per key</b>.</p>
 *
 * <p>Uses a simple Redis sliding window increment with TTL — suitable for
 * moderate traffic. For high-throughput scenarios, replace with Bucket4j's
 * full Redis integration ({@code bucket4j-redis} Caffeine-backed Redis proxy).</p>
 *
 * <p>On Redis unavailability, the filter fails open (allows the request through)
 * to prevent service disruption.</p>
 */
@Component
@Order(1)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int    LIMIT_PER_MINUTE  = 100;
    private static final String RATE_LIMIT_PREFIX = "rate:";

    private final StringRedisTemplate redis;

    public RateLimitFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String key = resolveKey(request);

        try {
            String redisKey = RATE_LIMIT_PREFIX + key;
            Long count = redis.opsForValue().increment(redisKey);
            if (count == 1) {
                // First request in this window — set TTL of 1 minute
                redis.expire(redisKey, Duration.ofMinutes(1));
            }

            // Set standard rate limit headers
            response.setHeader("X-RateLimit-Limit",     String.valueOf(LIMIT_PER_MINUTE));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, LIMIT_PER_MINUTE - count)));

            if (count > LIMIT_PER_MINUTE) {
                log.warn("Rate limit exceeded for key={}", key);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("""
                        {"code":"RATE_LIMIT_EXCEEDED","message":"Too many requests. Please retry after 1 minute."}
                        """);
                return;
            }
        } catch (Exception e) {
            // Fail open — Redis outage should not block valid traffic
            log.error("Rate limit check failed (Redis unavailable?), allowing request through: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Identifies the rate limit key: prefer the API key header, fall back to remote IP.
     */
    private String resolveKey(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isBlank()) {
            // Use first 16 chars as a non-sensitive partial key for Redis key naming
            return "apikey:" + apiKey.substring(0, Math.min(16, apiKey.length()));
        }
        return "ip:" + request.getRemoteAddr();
    }
}
