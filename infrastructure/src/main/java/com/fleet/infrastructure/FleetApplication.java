package com.fleet.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Fleet SaaS — Notification Hub entry point.
 *
 * <p>{@code @EnableAsync} activates asynchronous processing so that
 * {@code @Async} on {@code NotificationEventListener} dispatches
 * alerts off the rule-evaluation thread.</p>
 */
@SpringBootApplication
@EnableAsync
public class FleetApplication {
    public static void main(String[] args) {
        SpringApplication.run(FleetApplication.class, args);
    }
}
