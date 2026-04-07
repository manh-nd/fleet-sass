package com.fleet.application.notification.usecase;

import com.fleet.domain.notification.model.NotificationRequest;
import com.fleet.domain.notification.model.NotificationResult;
import com.fleet.domain.notification.vo.NotificationId;
import com.fleet.domain.shared.pagination.CursorPage;
import com.fleet.domain.notification.model.NotificationLog;
import com.fleet.domain.entitlement.vo.TenantId;

/**
 * Inbound port for the direct-send notification API.
 *
 * <p>Consuming services use this use case to send notifications without
 * defining rules — for ad-hoc or system-generated notifications.</p>
 */
public interface SendNotificationUseCase {

    /**
     * Sends a notification immediately.
     *
     * <p>Orchestrates template rendering (if a templateId is provided),
     * dispatches to the delivery channel, and persists a delivery log entry.</p>
     *
     * @param request the notification to send
     * @return the result with a unique notification ID and delivery status
     */
    NotificationResult send(NotificationRequest request);

    /**
     * Returns the delivery history for a tenant with cursor-based pagination.
     *
     * @param tenantId the owning tenant
     * @param cursor   opaque cursor for the next page, or {@code null} for the first page
     * @param limit    maximum items per page
     */
    CursorPage<NotificationLog> getDeliveryHistory(TenantId tenantId, String cursor, int limit);

    /**
     * Returns the delivery status of a specific notification.
     *
     * @param id the notification's unique ID
     * @return the log entry, or empty if not found
     */
    java.util.Optional<NotificationLog> getDeliveryStatus(NotificationId id);
}
