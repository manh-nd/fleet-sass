package com.fleet.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.application.notification.usecase.SendNotificationUseCase;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.notification.model.DeliveryStatus;
import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.notification.model.NotificationLog;
import com.fleet.domain.notification.model.NotificationResult;
import com.fleet.domain.notification.vo.NotificationId;
import com.fleet.domain.shared.pagination.CursorPage;
import com.fleet.infrastructure.adapter.in.web.dto.SendNotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock private SendNotificationUseCase sendNotificationUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        NotificationController controller = new NotificationController(sendNotificationUseCase);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldSendNotificationAndReturnSentResult() throws Exception {
        UUID tenantId = UUID.randomUUID();
        NotificationId notifId = NotificationId.generate();

        SendNotificationRequest request = new SendNotificationRequest(
                tenantId, "FLEET-CORE", "EMAIL", "driver@fleet.com",
                null, "Speed alert: {{speed}} km/h",
                "en", Map.of("speed", 120));

        NotificationResult result = NotificationResult.sent(notifId, ChannelType.EMAIL, "driver@fleet.com");
        when(sendNotificationUseCase.send(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.channel").value("EMAIL"))
                .andExpect(jsonPath("$.recipient").value("driver@fleet.com"))
                .andExpect(jsonPath("$.notificationId").isNotEmpty());
    }

    @Test
    void shouldReturnFailedResultWhenDispatchFails() throws Exception {
        UUID tenantId = UUID.randomUUID();
        NotificationId notifId = NotificationId.generate();

        SendNotificationRequest request = new SendNotificationRequest(
                tenantId, "FLEET-CORE", "EMAIL", "bad@fleet.com",
                null, "Alert body", "en", Map.of());

        NotificationResult result = NotificationResult.failed(
                notifId, ChannelType.EMAIL, "bad@fleet.com", "SMTP timeout");
        when(sendNotificationUseCase.send(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failReason").value("SMTP timeout"));
    }

    @Test
    void shouldReturnDeliveryHistory() throws Exception {
        UUID tenantId = UUID.randomUUID();
        when(sendNotificationUseCase.getDeliveryHistory(eq(new TenantId(tenantId)), isNull(), eq(20)))
                .thenReturn(CursorPage.lastPage(List.of()));

        mockMvc.perform(get("/api/v1/notifications")
                .param("tenantId", tenantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void shouldReturn404WhenNotificationNotFound() throws Exception {
        UUID notifId = UUID.randomUUID();
        when(sendNotificationUseCase.getDeliveryStatus(new NotificationId(notifId)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/notifications/{id}", notifId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotificationStatus() throws Exception {
        UUID tenantId = UUID.randomUUID();
        NotificationId notifId = NotificationId.generate();

        NotificationLog log = NotificationLog.reconstitute(
                notifId,
                new TenantId(tenantId),
                new com.fleet.domain.entitlement.vo.ServiceId("FLEET-CORE"),
                ChannelType.EMAIL,
                "driver@fleet.com",
                "Speed alert",
                DeliveryStatus.SENT,
                null, 1,
                java.time.Instant.now());

        when(sendNotificationUseCase.getDeliveryStatus(new NotificationId(notifId.value())))
                .thenReturn(Optional.of(log));

        mockMvc.perform(get("/api/v1/notifications/{id}", notifId.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.channel").value("EMAIL"));
    }

    @Test
    void shouldReturn400WhenChannelInvalid() throws Exception {
        UUID tenantId = UUID.randomUUID();
        SendNotificationRequest request = new SendNotificationRequest(
                tenantId, "FLEET-CORE", "INVALID_CHANNEL", "driver@fleet.com",
                null, "Alert body", "en", Map.of());

        // ChannelType.fromString("INVALID_CHANNEL") throws IllegalArgumentException
        // inside the controller before send() is ever called — no stubbing needed.
        mockMvc.perform(post("/api/v1/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
