package com.fleet.domain.notification.port.out;

import com.fleet.domain.notification.model.NotificationLog;
import com.fleet.domain.notification.vo.NotificationId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.shared.pagination.CursorPage;

/**
 * Outbound port for persisting and querying notification delivery logs.
 */
public interface NotificationLogRepositoryPort {

    /** Persists a new log entry. */
    void save(NotificationLog log);

    /** Updates an existing log entry (after status change). */
    void update(NotificationLog log);

    /**
     * Returns paginated notification logs for a tenant.
     *
     * @param tenantId owning tenant
     * @param cursor   opaque cursor from the previous page, or {@code null} for first page
     * @param limit    maximum items per page
     */
    CursorPage<NotificationLog> findByTenant(TenantId tenantId, String cursor, int limit);

    /** Finds a single log entry by its unique ID. */
    java.util.Optional<NotificationLog> findById(NotificationId id);
}
