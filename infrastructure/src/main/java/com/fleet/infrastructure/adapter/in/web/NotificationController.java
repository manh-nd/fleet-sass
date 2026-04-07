package com.fleet.infrastructure.adapter.in.web;

import com.fleet.application.notification.usecase.SendNotificationUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.notification.model.NotificationLog;
import com.fleet.domain.notification.model.NotificationRequest;
import com.fleet.domain.notification.model.NotificationResult;
import com.fleet.domain.notification.vo.NotificationId;
import com.fleet.domain.shared.pagination.CursorPage;
import com.fleet.infrastructure.adapter.in.web.dto.NotificationLogResponse;
import com.fleet.infrastructure.adapter.in.web.dto.SendNotificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

/**
 * Inbound REST adapter for the direct-send notification API.
 *
 * <p>Consuming services POST to this endpoint to deliver a notification immediately,
 * without needing a pre-configured rule. All exceptions are handled by
 * {@link GlobalExceptionHandler}.</p>
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;

    /**
     * {@code POST /api/v1/notifications/send}
     *
     * <p>Triggers an immediate notification dispatch and returns a delivery result.</p>
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResult> send(@Valid @RequestBody SendNotificationRequest req) {
        Locale locale = req.locale() != null ? Locale.forLanguageTag(req.locale()) : Locale.ENGLISH;

        NotificationRequest request = new NotificationRequest(
                new TenantId(req.tenantId()),
                new ServiceId(req.serviceId()),
                ChannelType.fromString(req.channel()),
                req.recipient(),
                req.templateId(),
                req.body(),
                locale,
                req.variables());

        NotificationResult result = sendNotificationUseCase.send(request);
        return ResponseEntity.ok(result);
    }

    /**
     * {@code GET /api/v1/notifications?tenantId=...&cursor=...&limit=20}
     *
     * <p>Returns paginated notification delivery history for a tenant.</p>
     */
    @GetMapping
    public ResponseEntity<CursorPage<NotificationLogResponse>> listHistory(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        CursorPage<NotificationLogResponse> page = sendNotificationUseCase
                .getDeliveryHistory(new TenantId(tenantId), cursor, limit)
                .map(NotificationLogResponse::from);
        return ResponseEntity.ok(page);
    }

    /**
     * {@code GET /api/v1/notifications/{notificationId}}
     *
     * <p>Returns the current delivery status of a specific notification.</p>
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationLogResponse> getStatus(@PathVariable UUID notificationId) {
        return sendNotificationUseCase.getDeliveryStatus(new NotificationId(notificationId))
                .map(NotificationLogResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
