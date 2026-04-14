package com.fleet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Fleet SaaS — Notification Hub entry point.
 *
 * <p>
 * {@code @EnableAsync} activates asynchronous processing so that
 * {@code @Async} on {@code NotificationEventListener} dispatches
 * alerts off the rule-evaluation thread.
 * </p>
 *
 * <p>
 * {@code @EnableCaching} activates Redis-backed Spring Cache for
 * API key lookups, reducing DB hits on every authenticated request.
 * </p>
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableCaching
public class FleetApplication {
    public static void main(String[] args) {
        SpringApplication.run(FleetApplication.class, args);
    }
}
