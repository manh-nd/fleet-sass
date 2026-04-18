package com.fleet.config;

import com.fleet.application.rule.usecase.ManageNotificationRuleUseCase;
import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.ConditionNode;
import com.fleet.domain.rule.ast.Operator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Populates the database with synthetic demo data on startup.
 *
 * <p>Active only when {@code spring.profiles.active=demo}. Useful for stakeholder
 * demonstrations and exploratory testing via Swagger UI without requiring live data.</p>
 *
 * <p>Start with:
 * <pre>SPRING_PROFILES_ACTIVE=demo ./gradlew :bootstrap:bootRun</pre>
 * then open <a href="http://localhost:8080/swagger-ui.html">Swagger UI</a>
 * and fire {@code POST /api/v1/rules/evaluate} with a sample vehicle event.</p>
 *
 * <h3>Seeded tenants</h3>
 * <ul>
 *   <li>{@code acme-corp}  — SPEED_ALERT rule: speed &gt; 80</li>
 *   <li>{@code globex}     — GEOFENCE_BREACH rule: zone_id == "RESTRICTED"</li>
 *   <li>{@code initech}    — LOW_FUEL rule: fuel_pct &lt;= 15</li>
 * </ul>
 */
@Component
@Profile("demo")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    // Fixed UUIDs so Swagger examples always work after restarts
    static final UUID TENANT_ACME   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID TENANT_GLOBEX = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static final UUID TENANT_INITECH = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private final ManageNotificationRuleUseCase ruleUseCase;

    @Override
    public void run(String... args) {
        log.info("=== [DEMO] Seeding demo data ===");

        // ── Tenant 1: ACME Corp — speed alert ─────────────────────────────
        createRule(TENANT_ACME, "fleet-tracker", "SPEED_ALERT",
                new ConditionNode("speed", Operator.GT, 80));

        // ── Tenant 2: Globex — geofence breach ────────────────────────────
        createRule(TENANT_GLOBEX, "fleet-tracker", "GEOFENCE_BREACH",
                new ConditionNode("zone_id", Operator.EQ, "RESTRICTED"));

        // ── Tenant 3: Initech — low fuel ──────────────────────────────────
        createRule(TENANT_INITECH, "fleet-tracker", "LOW_FUEL",
                new ConditionNode("fuel_pct", Operator.LTE, 15));

        log.info("=== [DEMO] Seeded 3 tenants and 3 rules. Open http://localhost:8080/swagger-ui.html ===");
    }

    private void createRule(UUID tenantId, String serviceId, String eventType,
                            ConditionNode condition) {
        try {
            ruleUseCase.createRule(
                    new TenantId(tenantId),
                    new ServiceId(serviceId),
                    eventType,
                    condition,
                    5,    // 5-minute cooldown
                    true  // active
            );
            log.info("[DEMO] Created rule: tenantId={} eventType={}", tenantId, eventType);
        } catch (Exception e) {
            // Rule may already exist from a previous run — safe to ignore
            log.debug("[DEMO] Rule already exists or failed to create ({}): {}", eventType, e.getMessage());
        }
    }
}
