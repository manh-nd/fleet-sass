package com.fleet.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.fleet.application.rule.port.out.RuleConditionSerializer;
import com.fleet.application.rule.usecase.ManageNotificationRuleUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.vo.RuleId;
import com.fleet.domain.shared.pagination.CursorPage;
import com.fleet.infrastructure.adapter.in.web.dto.*;
import com.fleet.infrastructure.adapter.out.db.RuleAstParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Inbound adapter for REST operations on notification rules.
 * All exceptions bubble up to {@link GlobalExceptionHandler} for consistent
 * error responses.
 */
@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Tag(name = "Rules", description = "Operations for managing notification rules and conditions")
public class RuleController {

    private final ManageNotificationRuleUseCase manageRulesUseCase;
    private final RuleConditionSerializer conditionSerializer;
    private final RuleAstParser ruleAstParser;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "List notification rules", description = "Returns a paginated list of notification rules for a specific tenant.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved rules")
    public ResponseEntity<CursorPage<NotificationRuleResponse>> listRules(
            @Parameter(description = "Tenant ID to list rules for", required = true) @RequestParam UUID tenantId,
            @Parameter(description = "Pagination cursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "Maximum number of items to return") @RequestParam(defaultValue = "20") int limit) {
        CursorPage<NotificationRuleResponse> page = manageRulesUseCase
                .listRules(new TenantId(tenantId), cursor, limit)
                .map(rule -> NotificationRuleResponse.from(rule, ruleAstParser, objectMapper));
        return ResponseEntity.ok(page);
    }

    @PostMapping
    @Operation(summary = "Create a notification rule", description = "Registers a new rule with conditions. Rules are evaluated against incoming events.")
    @ApiResponse(responseCode = "200", description = "Rule created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid condition JSON or validation failed", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> createRule(@Valid @RequestBody CreateRuleRequest request) {
        String conditionsJson = objectMapper.writeValueAsString(request.conditions());
        RuleNode root = conditionSerializer.deserialize(conditionsJson);
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
    @Operation(summary = "Update a notification rule", description = "Replaces the configuration of an existing rule.")
    @ApiResponse(responseCode = "200", description = "Rule updated successfully")
    @ApiResponse(responseCode = "404", description = "Rule not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> updateRule(
            @Parameter(description = "ID of the rule to update", required = true) @PathVariable UUID ruleId,
            @Valid @RequestBody UpdateRuleRequest request) {
        String conditionsJson = objectMapper.writeValueAsString(request.conditions());
        RuleNode root = conditionSerializer.deserialize(conditionsJson);
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
    @Operation(summary = "Delete a notification rule", description = "Permanently removes a rule.")
    @ApiResponse(responseCode = "200", description = "Rule deleted successfully")
    @ApiResponse(responseCode = "404", description = "Rule not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "ID of the rule to delete", required = true) @PathVariable UUID ruleId,
            @Parameter(description = "Owner tenant ID", required = true) @RequestParam UUID tenantId) {
        manageRulesUseCase.deleteRule(new RuleId(ruleId), new TenantId(tenantId));
        return ResponseEntity.ok().build();
    }
}
