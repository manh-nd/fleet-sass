package com.fleet.application.notification;

import com.fleet.application.notification.usecase.SendNotificationUseCase;
import com.fleet.application.shared.TemplateVariableSubstitutor;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.NotificationLog;
import com.fleet.domain.notification.model.NotificationRequest;
import com.fleet.domain.notification.model.NotificationResult;
import com.fleet.domain.notification.port.out.DeadLetterQueuePort;
import com.fleet.domain.notification.port.out.NotificationLogRepositoryPort;
import com.fleet.domain.notification.port.out.TemplateRenderPort;
import com.fleet.domain.notification.vo.NotificationId;
import com.fleet.domain.shared.pagination.CursorPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Implementation of {@link SendNotificationUseCase}.
 *
 * <p>Orchestrates the direct-send flow:</p>
 * <ol>
 *   <li>Render the message content (via template or raw body)</li>
 *   <li>Create a QUEUED log entry to track the attempt</li>
 *   <li>Dispatch to the appropriate channel via {@link ChannelDispatchRouter}</li>
 *   <li>Update the log to SENT or FAILED</li>
 *   <li>Return a {@link NotificationResult} to the caller</li>
 * </ol>
 *
 * <p><strong>Retry policy:</strong> this service performs a single dispatch attempt.
 * Retry logic is the responsibility of the infrastructure layer (e.g. SQS visibility
 * timeout, Spring Retry on the adapter, or an outbox poller). Blocking retry loops
 * inside an application service block virtual threads and cannot be monitored.</p>
 */
@RequiredArgsConstructor
@Slf4j
public class SendNotificationService implements SendNotificationUseCase {

    private final ChannelDispatchRouter  channelRouter;
    private final TemplateRenderPort     templateRenderer;
    private final NotificationLogRepositoryPort logRepository;
    private final DeadLetterQueuePort    deadLetterQueue;

    @Override
    public NotificationResult send(NotificationRequest request) {
        // Step 1: Render content
        String content = resolveContent(request);

        // Step 2: Persist QUEUED log entry
        NotificationLog notifLog = NotificationLog.create(
                request.tenantId(),
                request.serviceId(),
                request.channel(),
                request.recipient(),
                content);
        logRepository.save(notifLog);

        // Step 3: Dispatch (single attempt — retries owned by infrastructure)
        try {
            channelRouter.dispatch(
                    request.channel(),
                    request.recipient(),
                    request.subject(),
                    content);

            NotificationLog sentLog = notifLog.markSent();
            logRepository.update(sentLog);
            return NotificationResult.sent(notifLog.getId(), request.channel(), request.recipient());

        } catch (Exception ex) {
            log.error("Failed to dispatch notification {} via {} to {}: {}",
                    notifLog.getId(), request.channel(), request.recipient(), ex.getMessage());

            NotificationLog failedLog = notifLog.markFailed(ex.getMessage());
            logRepository.update(failedLog);
            deadLetterQueue.enqueue(notifLog.getId(), ex.getMessage());

            return NotificationResult.failed(
                    notifLog.getId(), request.channel(), request.recipient(), ex.getMessage());
        }
    }

    @Override
    public CursorPage<NotificationLog> getDeliveryHistory(TenantId tenantId, String cursor, int limit) {
        return logRepository.findByTenant(tenantId, cursor, limit);
    }

    @Override
    public Optional<NotificationLog> getDeliveryStatus(NotificationId id) {
        return logRepository.findById(id);
    }

    // ---- Private helpers ----

    private String resolveContent(NotificationRequest request) {
        if (request.templateId() != null) {
            return templateRenderer.render(
                    request.templateId(),
                    request.locale(),
                    request.variables());
        }
        return TemplateVariableSubstitutor.apply(request.body(), request.variables());
    }
}
