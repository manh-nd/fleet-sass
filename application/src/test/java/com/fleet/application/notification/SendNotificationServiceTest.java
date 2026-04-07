package com.fleet.application.notification;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.DeliveryStatus;
import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.notification.model.NotificationLog;
import com.fleet.domain.notification.model.NotificationRequest;
import com.fleet.domain.notification.model.NotificationResult;
import com.fleet.domain.notification.port.out.DeadLetterQueuePort;
import com.fleet.domain.notification.port.out.NotificationDispatcherPort;
import com.fleet.domain.notification.port.out.NotificationLogRepositoryPort;
import com.fleet.domain.notification.port.out.TemplateRenderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {

    @Mock private NotificationDispatcherPort dispatcher;
    @Mock private TemplateRenderPort templateRenderer;
    @Mock private NotificationLogRepositoryPort logRepository;
    @Mock private DeadLetterQueuePort deadLetterQueue;

    @InjectMocks
    private SendNotificationService service;

    private final TenantId tenantId   = new TenantId(UUID.randomUUID());
    private final ServiceId serviceId = new ServiceId("REMODUL");

    @Test
    void shouldDispatchEmailViaSendAndReturnSentResult() {
        NotificationRequest request = new NotificationRequest(
                tenantId, serviceId,
                ChannelType.EMAIL, "driver@fleet.com",
                null, "Speed: {{speed}} km/h",
                Locale.ENGLISH, Map.of("speed", 100));

        NotificationResult result = service.send(request);

        assertEquals(DeliveryStatus.SENT, result.status());
        assertEquals(ChannelType.EMAIL, result.channel());
        assertEquals("driver@fleet.com", result.recipient());
        assertNotNull(result.notificationId());
        assertNull(result.failReason());

        // Verify email was dispatched
        verify(dispatcher).sendEmail(eq("driver@fleet.com"), eq("Fleet Notification"), eq("Speed: 100 km/h"));

        // Verify log was saved (QUEUED) then updated (SENT)
        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository).save(captor.capture());
        assertEquals(DeliveryStatus.QUEUED, captor.getValue().getStatus());

        verify(logRepository).update(argThat(log -> log.getStatus() == DeliveryStatus.SENT));
    }

    @Test
    void shouldUseTemplateWhenTemplateIdProvided() {
        when(templateRenderer.render("SPEED_ALERT", Locale.of("vi"), Map.of("speed", 120)))
                .thenReturn("Cảnh báo tốc độ: 120 km/h");

        NotificationRequest request = new NotificationRequest(
                tenantId, serviceId,
                ChannelType.SMS, "0901234567",
                "SPEED_ALERT", null,
                Locale.of("vi"), Map.of("speed", 120));

        NotificationResult result = service.send(request);

        assertEquals(DeliveryStatus.SENT, result.status());
        verify(dispatcher).sendSms("0901234567", "Cảnh báo tốc độ: 120 km/h");
    }

    @Test
    void shouldReturnFailedResultWhenDispatcherThrows() {
        doThrow(new RuntimeException("Network timeout"))
                .when(dispatcher).sendEmail(any(), any(), any());

        NotificationRequest request = new NotificationRequest(
                tenantId, serviceId,
                ChannelType.EMAIL, "user@example.com",
                null, "Hello Bob",
                Locale.ENGLISH, Map.of());

        NotificationResult failedResult = service.send(request);

        // Should attempt dispatch 3 times (due to retry loop) but catch exception
        verify(dispatcher, times(3)).sendEmail(eq("user@example.com"), eq("Fleet Notification"), eq("Hello Bob"));

        // Status should be marked FAILED
        assertEquals(DeliveryStatus.FAILED, failedResult.status());
        assertEquals("Network timeout", failedResult.failReason());

        // Should push to Dead Letter Queue
        verify(deadLetterQueue).enqueue(any(), eq("Network timeout"));
        
        // Log should be updated to FAILED
        verify(logRepository).update(argThat(log -> log.getStatus() == DeliveryStatus.FAILED));
    }

    @Test
    void shouldDispatchPushChannel() {
        NotificationRequest request = new NotificationRequest(
                tenantId, serviceId,
                ChannelType.PUSH, "device-token-abc",
                null, "Geofence breach detected",
                Locale.ENGLISH, Map.of());

        service.send(request);

        verify(dispatcher).sendPush("device-token-abc", "Fleet Notification", "Geofence breach detected");
    }

    @Test
    void shouldApplyVariablesOnRawBody() {
        NotificationRequest request = new NotificationRequest(
                tenantId, serviceId,
                ChannelType.WEBHOOK, "https://hook.example.com",
                null, "Unit {{id}} entered zone {{zone}}",
                Locale.ENGLISH, Map.of("id", "V001", "zone", "A1"));

        service.send(request);

        verify(dispatcher).sendWebhook("https://hook.example.com", "Unit V001 entered zone A1");
    }
}
