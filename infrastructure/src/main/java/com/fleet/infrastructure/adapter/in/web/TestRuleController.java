package com.fleet.infrastructure.adapter.in.web;

import com.fleet.application.rule.usecase.EvaluateRulesUseCase;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.vo.EventPayload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test/rules")
public class TestRuleController {

    private final EvaluateRulesUseCase evaluateRulesUseCase;

    public TestRuleController(EvaluateRulesUseCase evaluateRulesUseCase) {
        this.evaluateRulesUseCase = evaluateRulesUseCase;
    }

    @PostMapping("/evaluate")
    public List<String> evaluate(@RequestBody Map<String, Object> requestPayload) {
        // Hardcode Tenant ID của Avocado Transport để test nhanh
        TenantId tenantId = new TenantId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        String eventType = "SPEED_NO_CRUISE";

        String vehicleId = requestPayload.getOrDefault("vehicle_plate", "UNKNOWN").toString();
        EventPayload payload = new EventPayload(vehicleId, requestPayload);

        // Gọi luồng Use Case -> Kéo DB -> Build AST -> Evaluate -> Trả Rule hợp lệ
        List<NotificationRule> triggeredRules = evaluateRulesUseCase.evaluate(tenantId, eventType, payload);

        // Trả về danh sách Rule ID đã bị vi phạm để xem kết quả
        return triggeredRules.stream()
                .map(rule -> "Đã vi phạm Rule ID: " + rule.getId().value().toString())
                .toList();
    }
}