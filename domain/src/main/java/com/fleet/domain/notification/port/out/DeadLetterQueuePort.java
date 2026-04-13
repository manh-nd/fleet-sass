package com.fleet.domain.notification.port.out;

import com.fleet.domain.notification.vo.NotificationId;

/**
 * Port for dispatching permanently failed notifications.
 *
 * <p>Messages that exhaust all delivery retries are placed in this queue for
 * administrative review, alerting, or manual replay.</p>
 */
public interface DeadLetterQueuePort {
    void enqueue(NotificationId id, String reason);
}
