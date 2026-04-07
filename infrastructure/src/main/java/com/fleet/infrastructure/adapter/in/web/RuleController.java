package com.fleet.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.fleet.application.rule.usecase.ManageNotificationRuleUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.vo.RuleId;
import com.fleet.infrastructure.adapter.in.web.dto.CreateRuleRequest;
import com.fleet.infrastructure.adapter.in.web.dto.NotificationRuleResponse;
import com.fleet.infrastructure.adapter.in.web.dto.UpdateRuleRequest;
import com.fleet.infrastructure.adapter.out.db.RuleAstParser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Inbound adapter for REST operations on notification rules.
 * All exceptions bubble up to {@link GlobalExceptionHandler} for consistent error responses.
 */
@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final ManageNotificationRuleUseCase manageRulesUseCase;
    private final RuleAstParser ruleAstParser;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<NotificationRuleResponse>> listRules(@RequestParam UUID tenantId) {
        List<NotificationRuleResponse> rules = manageRulesUseCase.listRules(new TenantId(tenantId))
                .stream()
                .map(rule -> NotificationRuleResponse.from(rule, ruleAstParser, objectMapper))
                .toList();
        return ResponseEntity.ok(rules);
    }

    @PostMapping
    public ResponseEntity<Void> createRule(@Valid @RequestBody CreateRuleRequest request) {
        String conditionsJson = objectMapper.writeValueAsString(request.conditions());
        RuleNode root = ruleAstParser.parse(conditionsJson);
        manageRulesUseCase.createRule(
                new TenantId(request.tenantId()),
                new ServiceId(request.serviceId()),
                request.eventType(),
                root,
                request.cooldownMinutes(),
                request.active());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<Void> updateRule(
            @PathVariable UUID ruleId,
            @Valid @RequestBody UpdateRuleRequest request) {
        String conditionsJson = objectMapper.writeValueAsString(request.conditions());
        RuleNode root = ruleAstParser.parse(conditionsJson);
        manageRulesUseCase.updateRule(
                new RuleId(ruleId),
                new TenantId(request.tenantId()),
                new ServiceId(request.serviceId()),
                request.eventType(),
                root,
                request.cooldownMinutes(),
                request.active());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID ruleId, @RequestParam UUID tenantId) {
        manageRulesUseCase.deleteRule(new RuleId(ruleId), new TenantId(tenantId));
        return ResponseEntity.ok().build();
    }
}
