package com.fleet.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.fleet.application.rule.port.out.RuleConditionSerializer;
import com.fleet.application.rule.usecase.ManageNotificationRuleUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.Operator;
import com.fleet.domain.rule.vo.RuleId;
import com.fleet.domain.shared.exception.RuleParsingException;
import com.fleet.domain.shared.pagination.CursorPage;
import com.fleet.infrastructure.adapter.in.web.dto.CreateRuleRequest;
import com.fleet.infrastructure.adapter.in.web.dto.UpdateRuleRequest;
import com.fleet.infrastructure.adapter.out.db.RuleAstParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RuleControllerTest {

    private MockMvc mockMvc;

    @Mock private ManageNotificationRuleUseCase manageRulesUseCase;
    @Mock private RuleConditionSerializer conditionSerializer;
    @Mock private RuleAstParser ruleAstParser;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RuleController controller = new RuleController(
                manageRulesUseCase, conditionSerializer, ruleAstParser, objectMapper);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListRulesWithPagination() throws Exception {
        UUID tenantId = UUID.randomUUID();
        when(manageRulesUseCase.listRules(eq(new TenantId(tenantId)), eq(null), eq(20)))
                .thenReturn(CursorPage.lastPage(List.of()));

        mockMvc.perform(get("/api/v1/rules").param("tenantId", tenantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void shouldCreateRuleSuccessfully() throws Exception {
        UUID tenantId = UUID.randomUUID();
        CreateRuleRequest request = new CreateRuleRequest(
                tenantId, "S1", "SPEEDING",
                Map.of("type", "CONDITION", "field", "speed", "operator", "gt", "value", 80),
                5, true);

        ConditionNode mockNode = new ConditionNode("speed", Operator.GT, 80);
        when(conditionSerializer.deserialize(any())).thenReturn(mockNode);

        mockMvc.perform(post("/api/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(manageRulesUseCase).createRule(
                eq(new TenantId(tenantId)),
                eq(new ServiceId("S1")),
                eq("SPEEDING"),
                eq(mockNode),
                eq(5),
                eq(true));
    }

    @Test
    void shouldReturnBadRequestWhenParsingFails() throws Exception {
        UUID tenantId = UUID.randomUUID();
        CreateRuleRequest request = new CreateRuleRequest(
                tenantId, "S1", "SPEEDING", Map.of("type", "UNKNOWN"), 5, true);

        when(conditionSerializer.deserialize(any())).thenThrow(new RuleParsingException("Unknown node type"));

        mockMvc.perform(post("/api/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CONDITION_JSON"));
    }

    @Test
    void shouldUpdateRuleSuccessfully() throws Exception {
        UUID ruleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UpdateRuleRequest request = new UpdateRuleRequest(
                tenantId, "S1", "SPEEDING",
                Map.of("type", "CONDITION", "field", "speed", "operator", "gt", "value", 90),
                10, false);

        ConditionNode mockNode = new ConditionNode("speed", Operator.GT, 90);
        when(conditionSerializer.deserialize(any())).thenReturn(mockNode);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/rules/{ruleId}", ruleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(manageRulesUseCase).updateRule(
                eq(new RuleId(ruleId)),
                eq(new TenantId(tenantId)),
                eq(new ServiceId("S1")),
                eq("SPEEDING"),
                eq(mockNode),
                eq(10),
                eq(false));
    }

    @Test
    void shouldDeleteRuleSuccessfully() throws Exception {
        UUID ruleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rules/{ruleId}", ruleId)
                .param("tenantId", tenantId.toString()))
                .andExpect(status().isOk());

        verify(manageRulesUseCase).deleteRule(eq(new RuleId(ruleId)), eq(new TenantId(tenantId)));
    }
}
