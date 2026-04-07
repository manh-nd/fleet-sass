package com.fleet.infrastructure.integration;

import com.fleet.application.rule.usecase.EvaluateRulesUseCase;
import com.fleet.application.shared.event.RuleTriggeredEvent;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.vo.EventPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.context.annotation.Import;

@SpringBootTest
@Testcontainers
@Import(EvaluateRulesIntegrationTest.TestConfig.class)
public class EvaluateRulesIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private EvaluateRulesUseCase evaluateRulesUseCase;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private TestEventListener testEventListener;

    @BeforeEach
    void setUp() {
        testEventListener.clear();
        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS notification_rules (
                id UUID PRIMARY KEY,
                tenant_id UUID NOT NULL,
                service_id VARCHAR(50) NOT NULL,
                event_type VARCHAR(100) NOT NULL,
                conditions_json JSONB NOT NULL,
                cooldown_minutes INT DEFAULT 5,
                is_active BOOLEAN DEFAULT TRUE
            )
        """).update();
        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS notification_actions (
                rule_id UUID NOT NULL,
                channel_type VARCHAR(20) NOT NULL,
                recipient VARCHAR(255) NOT NULL,
                message_template TEXT NOT NULL
            )
        """).update();
        jdbcClient.sql("TRUNCATE TABLE notification_rules").update();
        jdbcClient.sql("TRUNCATE TABLE notification_actions").update();
    }

    @Test
    void shouldEvaluateRuleAndTriggerEvent() {
        UUID ruleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String eventType = "SPEEDING";

        // Insert a rule into Postgres
        jdbcClient.sql("""
            INSERT INTO notification_rules (id, tenant_id, service_id, event_type, conditions_json, cooldown_minutes, is_active)
            VALUES (:id, :tenantId, 'S1', :eventType, CAST(:conditions AS jsonb), 5, true)
        """)
        .param("id", ruleId)
        .param("tenantId", tenantId)
        .param("eventType", eventType)
        .param("conditions", "{\"type\":\"CONDITION\", \"field\":\"speed\", \"operator\":\">\", \"value\":80}")
        .update();

        // Prepare EventPayload
        EventPayload payload = new EventPayload("ref-V1", Map.of("speed", 100));

        // Execute UseCase
        List<NotificationRule> result = evaluateRulesUseCase.evaluate(new TenantId(tenantId), eventType, payload);

        // Assertions
        assertEquals(1, result.size());
        assertEquals(ruleId, result.get(0).getId().value());

        // Verify Event was published via Spring ApplicationEventPublisher
        assertEquals(1, testEventListener.getEvents().size());
        assertEquals(ruleId, testEventListener.getEvents().get(0).ruleId().value());
    }

    @Test
    void shouldRespectCooldown() {
        UUID ruleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String eventType = "SPEEDING";

        jdbcClient.sql("""
            INSERT INTO notification_rules (id, tenant_id, service_id, event_type, conditions_json, cooldown_minutes, is_active)
            VALUES (:id, :tenantId, 'S1', :eventType, CAST(:conditions AS jsonb), 5, true)
        """)
        .param("id", ruleId)
        .param("tenantId", tenantId)
        .param("eventType", eventType)
        .param("conditions", "{\"type\":\"CONDITION\", \"field\":\"speed\", \"operator\":\">\", \"value\":80}")
        .update();

        EventPayload payload = new EventPayload("ref-V1", Map.of("speed", 100));

        // 1st time - should trigger
        evaluateRulesUseCase.evaluate(new TenantId(tenantId), eventType, payload);
        assertEquals(1, testEventListener.getEvents().size());

        // 2nd time immediately - should NOT trigger due to cooldown in Redis
        evaluateRulesUseCase.evaluate(new TenantId(tenantId), eventType, payload);
        assertEquals(1, testEventListener.getEvents().size());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestEventListener testEventListener() {
            return new TestEventListener();
        }
    }

    static class TestEventListener {
        private final List<RuleTriggeredEvent> events = new ArrayList<>();

        @org.springframework.context.event.EventListener
        public void handleRuleTriggered(RuleTriggeredEvent event) {
            events.add(event);
        }

        public List<RuleTriggeredEvent> getEvents() {
            return events;
        }

        public void clear() {
            events.clear();
        }
    }
}
