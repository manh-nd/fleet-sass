package com.fleet.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.fleet.application.rule.usecase.ManageNotificationRuleUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.Operator;
import com.fleet.domain.rule.vo.RuleId;
import com.fleet.infrastructure.adapter.in.web.dto.CreateRuleRequest;
import com.fleet.infrastructure.adapter.in.web.dto.UpdateRuleRequest;
import com.fleet.infrastructure.adapter.out.db.RuleAstParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RuleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageNotificationRuleUseCase manageRulesUseCase;

    @Mock
    private RuleAstParser ruleAstParser;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RuleController ruleController;

    @BeforeEach
    void setUp() {
        // Need to provide an ObjectMapper to the controller, the @InjectMocks might leave it null or we can pass it manually.
        ruleController = new RuleController(manageRulesUseCase, ruleAstParser, objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(ruleController).build();
    }

    @Test
    void shouldCreateRuleSuccessfully() throws Exception {
        UUID tenantId = UUID.randomUUID();
        CreateRuleRequest request = new CreateRuleRequest(
                tenantId,
                "S1",
                "SPEEDING",
                Map.of("type", "CONDITION", "field", "speed", "operator", ">", "value", 80),
                5,
                true
        );

        ConditionNode mockNode = new ConditionNode("speed", Operator.GT, 80);
        when(ruleAstParser.parse(any())).thenReturn(mockNode);

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
                eq(true)
        );
    }

    @Test
    void shouldReturnBadRequestWhenExceptionIsThrown() throws Exception {
        UUID tenantId = UUID.randomUUID();
        CreateRuleRequest request = new CreateRuleRequest(
                tenantId,
                "S1",
                "SPEEDING",
                null,
                5,
                true
        );

        when(ruleAstParser.parse(any())).thenThrow(new RuntimeException("Invalid JSON"));

        mockMvc.perform(post("/api/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateRuleSuccessfully() throws Exception {
        UUID ruleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UpdateRuleRequest request = new UpdateRuleRequest(
                tenantId,
                "S1",
                "SPEEDING",
                Map.of("type", "CONDITION", "field", "speed", "operator", ">", "value", 90),
                10,
                false
        );

        ConditionNode mockNode = new ConditionNode("speed", Operator.GT, 90);
        when(ruleAstParser.parse(any())).thenReturn(mockNode);

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
                eq(false)
        );
    }

    @Test
    void shouldDeleteRuleSuccessfully() throws Exception {
        UUID ruleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rules/{ruleId}", ruleId)
                .param("tenantId", tenantId.toString()))
                .andExpect(status().isOk());

        verify(manageRulesUseCase).deleteRule(
                eq(new RuleId(ruleId)),
                eq(new TenantId(tenantId))
        );
    }
}
