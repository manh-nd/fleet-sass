package com.fleet.application.notification;

import com.fleet.application.notification.usecase.SendNotificationUseCase;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.NotificationLog;
import com.fleet.domain.notification.model.NotificationRequest;
import com.fleet.domain.notification.model.NotificationResult;
import com.fleet.domain.notification.port.out.NotificationDispatcherPort;
import com.fleet.domain.notification.port.out.NotificationLogRepositoryPort;
import com.fleet.domain.notification.port.out.TemplateRenderPort;
import com.fleet.domain.notification.vo.NotificationId;
import com.fleet.domain.shared.pagination.CursorPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link SendNotificationUseCase}.
 *
 * <p>Orchestrates the direct-send flow:</p>
 * <ol>
 *   <li>Render the message content (via template or raw body)</li>
 *   <li>Create a QUEUED log entry to track the attempt</li>
 *   <li>Dispatch to the appropriate channel adapter</li>
 *   <li>Update the log to SENT or FAILED</li>
 *   <li>Return a {@link NotificationResult} to the caller</li>
 * </ol>
 */
@RequiredArgsConstructor
@Slf4j
public class SendNotificationService implements SendNotificationUseCase {

    private final NotificationDispatcherPort dispatcher;
    private final TemplateRenderPort templateRenderer;
    private final NotificationLogRepositoryPort logRepository;

    @Override
    public NotificationResult send(NotificationRequest request) {
        // Step 1: Render content
        String content = resolveContent(request);

        // Step 2: Create QUEUED log entry
        NotificationLog notifLog = NotificationLog.create(
                request.tenantId(),
                request.serviceId(),
                request.channel(),
                request.recipient(),
                content);
        logRepository.save(notifLog);

        // Step 3 + 4: Dispatch and update log
        try {
            dispatch(request.channel(), request.recipient(), content);
            NotificationLog sentLog = notifLog.markSent();
            logRepository.update(sentLog);

            log.info("Notification {} dispatched via {} to {}",
                    notifLog.getId(), request.channel(), request.recipient());
            return NotificationResult.sent(notifLog.getId(), request.channel(), request.recipient());

        } catch (Exception ex) {
            log.error("Failed to dispatch notification {} via {} to {}: {}",
                    notifLog.getId(), request.channel(), request.recipient(), ex.getMessage());
            NotificationLog failedLog = notifLog.markFailed(ex.getMessage());
            logRepository.update(failedLog);
            return NotificationResult.failed(notifLog.getId(), request.channel(), request.recipient(), ex.getMessage());
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
        return applyVariables(request.body(), request.variables());
    }

    private void dispatch(
            com.fleet.domain.notification.model.NotificationAction.ChannelType channel,
            String recipient,
            String content) {
        switch (channel) {
            case EMAIL   -> dispatcher.sendEmail(recipient, "Fleet Notification", content);
            case SMS     -> dispatcher.sendSms(recipient, content);
            case WEBHOOK -> dispatcher.sendWebhook(recipient, content);
            case PUSH    -> dispatcher.sendPush(recipient, "Fleet Notification", content);
        }
    }

    private String applyVariables(String body, Map<String, Object> variables) {
        if (body == null || variables == null || variables.isEmpty()) return body;
        String result = body;
        for (var entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}
