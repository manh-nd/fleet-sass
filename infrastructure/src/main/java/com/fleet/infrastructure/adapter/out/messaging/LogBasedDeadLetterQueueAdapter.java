package com.fleet.infrastructure.adapter.out.messaging;

import com.fleet.domain.notification.port.out.DeadLetterQueuePort;
import com.fleet.domain.notification.vo.NotificationId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogBasedDeadLetterQueueAdapter implements DeadLetterQueuePort {
    @Override
    public void enqueue(NotificationId id, String reason) {
        log.error("[DLQ] Notification {} permanently failed. Reason: {}", id.value(), reason);
    }
}
