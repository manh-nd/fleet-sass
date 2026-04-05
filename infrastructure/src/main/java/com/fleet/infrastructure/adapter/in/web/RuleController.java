package com.fleet.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.fleet.application.rule.usecase.ManageNotificationRuleUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.vo.RuleId;
import com.fleet.infrastructure.adapter.in.web.dto.CreateRuleRequest;
import com.fleet.infrastructure.adapter.in.web.dto.UpdateRuleRequest;
import com.fleet.infrastructure.adapter.out.db.RuleAstParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final ManageNotificationRuleUseCase manageRulesUseCase;
    private final RuleAstParser ruleAstParser;
    private final ObjectMapper objectMapper; // Spring Boot autoconfigures a standard Jackson ObjectMapper

    @PostMapping
    public ResponseEntity<Void> createRule(@RequestBody CreateRuleRequest request) {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<Void> updateRule(@PathVariable UUID ruleId,
            @RequestBody UpdateRuleRequest request) {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID ruleId, @RequestParam UUID tenantId) {
        manageRulesUseCase.deleteRule(new RuleId(ruleId), new TenantId(tenantId));
        return ResponseEntity.ok().build();
    }
}
