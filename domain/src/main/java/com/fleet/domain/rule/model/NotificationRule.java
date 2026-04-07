package com.fleet.domain.rule.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.fleet.domain.entitlement.vo.ServiceId;
import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.ast.RuleNode;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;

import java.util.UUID;

/**
 * The {@code NotificationRule} aggregate is the core of the rule engine.
 * It encapsulates the condition tree (AST) evaluated against incoming events,
 * and enforces all invariants through factory methods.
 *
 * <p>Instances must be created via {@link #create} (new rules) or
 * {@link #reconstitute} (rehydrating from persistence). The constructor is private
 * to prevent bypassing validation.</p>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationRule {

    private final RuleId id;
    private final TenantId tenantId;
    private final ServiceId serviceId;
    private final String eventType;
    private final RuleNode conditionRoot;
    private final boolean isActive;
    private final int cooldownMinutes;

    // ---- Factory Methods ----

    /**
     * Creates a new {@link NotificationRule} with a generated ID.
     * Validates all mandatory fields and enforces invariants.
     *
     * @param tenantId        the owning tenant
     * @param serviceId       the service that owns this rule
     * @param eventType       the event type this rule reacts to (must not be blank)
     * @param conditionRoot   the root node of the condition AST (must not be null)
     * @param cooldownMinutes how long to suppress repeat triggers (must be &gt;= 0)
     * @param isActive        whether this rule is currently active
     */
    public static NotificationRule create(
            TenantId tenantId,
            ServiceId serviceId,
            String eventType,
            RuleNode conditionRoot,
            int cooldownMinutes,
            boolean isActive) {
        validate(tenantId, serviceId, eventType, conditionRoot, cooldownMinutes);
        return new NotificationRule(
                new RuleId(UUID.randomUUID()),
                tenantId,
                serviceId,
                eventType,
                conditionRoot,
                isActive,
                cooldownMinutes);
    }

    /**
     * Reconstitutes a {@link NotificationRule} from a persisted state.
     * Skips ID generation; intended for repository adapters only.
     */
    public static NotificationRule reconstitute(
            RuleId id,
            TenantId tenantId,
            ServiceId serviceId,
            String eventType,
            RuleNode conditionRoot,
            boolean isActive,
            int cooldownMinutes) {
        return new NotificationRule(id, tenantId, serviceId, eventType, conditionRoot, isActive, cooldownMinutes);
    }

    // ---- Mutation Methods (return new instances — immutability) ----

    /**
     * Returns a new rule with {@code isActive = true}.
     */
    public NotificationRule activate() {
        return new NotificationRule(id, tenantId, serviceId, eventType, conditionRoot, true, cooldownMinutes);
    }

    /**
     * Returns a new rule with {@code isActive = false}.
     */
    public NotificationRule deactivate() {
        return new NotificationRule(id, tenantId, serviceId, eventType, conditionRoot, false, cooldownMinutes);
    }

    /**
     * Returns a new rule with an updated condition tree.
     *
     * @param newConditionRoot the new AST root (must not be null)
     */
    public NotificationRule updateConditions(RuleNode newConditionRoot) {
        if (newConditionRoot == null) {
            throw new IllegalArgumentException("conditionRoot must not be null");
        }
        return new NotificationRule(id, tenantId, serviceId, eventType, newConditionRoot, isActive, cooldownMinutes);
    }

    // ---- Business Behavior ----

    /**
     * Returns {@code true} if this rule is active and its condition tree
     * evaluates to {@code true} for the given event payload.
     */
    public boolean isSatisfiedBy(EventPayload payload) {
        if (!isActive || conditionRoot == null) {
            return false;
        }
        return conditionRoot.evaluate(payload);
    }

    // ---- Private Helpers ----

    private static void validate(
            TenantId tenantId,
            ServiceId serviceId,
            String eventType,
            RuleNode conditionRoot,
            int cooldownMinutes) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        if (serviceId == null) throw new IllegalArgumentException("serviceId must not be null");
        if (eventType == null || eventType.isBlank())
            throw new IllegalArgumentException("eventType must not be blank");
        if (conditionRoot == null) throw new IllegalArgumentException("conditionRoot must not be null");
        if (cooldownMinutes < 0)
            throw new IllegalArgumentException("cooldownMinutes must be >= 0, got: " + cooldownMinutes);
    }
}