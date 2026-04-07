package com.fleet.infrastructure.adapter.out.db;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.DeliveryStatus;
import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.notification.model.NotificationLog;
import com.fleet.domain.notification.vo.NotificationId;
import com.fleet.domain.shared.pagination.CursorPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class PostgresNotificationLogAdapterTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private JdbcClient jdbcClient;
    private PostgresNotificationLogAdapter adapter;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(postgres.getDriverClassName());
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());

        jdbcClient = JdbcClient.create(dataSource);
        adapter = new PostgresNotificationLogAdapter(jdbcClient);

        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS notification_log (
                id UUID PRIMARY KEY,
                tenant_id UUID NOT NULL,
                service_id VARCHAR(50) NOT NULL,
                channel VARCHAR(20) NOT NULL,
                recipient VARCHAR(255) NOT NULL,
                rendered_content TEXT,
                status VARCHAR(20) NOT NULL,
                fail_reason TEXT,
                attempts INT NOT NULL DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """).update();
        jdbcClient.sql("TRUNCATE TABLE notification_log").update();
    }

    @Test
    void shouldSaveAndFindById() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        NotificationLog log = NotificationLog.create(
                tenantId, new ServiceId("FLEET"), ChannelType.EMAIL, "a@b.c", "Hello");

        adapter.save(log);

        Optional<NotificationLog> found = adapter.findById(log.getId());
        assertTrue(found.isPresent());
        assertEquals("a@b.c", found.get().getRecipient());
        assertEquals(DeliveryStatus.QUEUED, found.get().getStatus());
    }

    @Test
    void shouldUpdateStatus() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        NotificationLog log = NotificationLog.create(
                tenantId, new ServiceId("FLEET"), ChannelType.EMAIL, "a@b.c", "Hello");
        adapter.save(log);

        NotificationLog sentLog = log.markSent();
        adapter.update(sentLog);

        Optional<NotificationLog> found = adapter.findById(log.getId());
        assertTrue(found.isPresent());
        assertEquals(DeliveryStatus.SENT, found.get().getStatus());
    }

    @Test
    void shouldFindPaginatedByTenant() {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        
        // Save 3 logs
        for (int i = 0; i < 3; i++) {
            adapter.save(NotificationLog.create(
                    tenantId, new ServiceId("FLEET"), ChannelType.EMAIL, "a@b.c", "Hello " + i));
            try { Thread.sleep(10); } catch (InterruptedException ignored) {} // Ensure ordering by time
        }

        CursorPage<NotificationLog> page1 = adapter.findByTenant(tenantId, null, 2);
        assertEquals(2, page1.items().size());
        assertTrue(page1.hasMore());

        CursorPage<NotificationLog> page2 = adapter.findByTenant(tenantId, page1.nextCursor(), 2);
        assertEquals(1, page2.items().size());
        assertFalse(page2.hasMore());
    }
}
