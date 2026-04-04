package com.fleet.infrastructure.adapter.out.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.vo.RuleId;

@Testcontainers
class PostgresRuleRepositoryAdapterTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private PostgresRuleRepositoryAdapter adapter;
    private JdbcClient jdbcClient;
    private RuleAstParser astParser;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(postgres.getDriverClassName());
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());

        jdbcClient = JdbcClient.create(dataSource);
        astParser = mock(RuleAstParser.class);
        adapter = new PostgresRuleRepositoryAdapter(jdbcClient, astParser);

        // Ensure table exists
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

        jdbcClient.sql("TRUNCATE TABLE notification_rules").update();
    }

    @Test
    void shouldFindActiveRules() {
        UUID ruleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String eventType = "SPEEDING";

        jdbcClient
                .sql("""
                            INSERT INTO notification_rules (id, tenant_id, service_id, event_type, conditions_json, cooldown_minutes, is_active)
                            VALUES (?, ?, 'S1', ?, '{"type":"CONDITION"}'::jsonb, 5, true)
                        """)
                .params(ruleId, tenantId, eventType)
                .update();

        RuleNode mockNode = mock(RuleNode.class);
        when(astParser.parse(anyString())).thenReturn(mockNode);

        List<NotificationRule> result = adapter.findActiveRules(new TenantId(tenantId), eventType);

        assertEquals(1, result.size());
        assertEquals(new RuleId(ruleId), result.get(0).getId());
        assertEquals(mockNode, result.get(0).getConditionRoot());
    }

    @Test
    void shouldNotFindInactiveRules() {
        UUID ruleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String eventType = "SPEEDING";

        jdbcClient.sql("""
                    INSERT INTO notification_rules (id, tenant_id, service_id, event_type, conditions_json, is_active)
                    VALUES (?, ?, 'S1', ?, '{"type":"CONDITION"}'::jsonb, false)
                """)
                .params(ruleId, tenantId, eventType)
                .update();

        List<NotificationRule> result = adapter.findActiveRules(new TenantId(tenantId), eventType);

        assertTrue(result.isEmpty());
    }
}
