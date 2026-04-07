package com.fleet.domain.rule.port.out;

import com.fleet.domain.rule.vo.RuleId;

/**
 * Outbound port for managing per-rule, per-entity cooldown state.
 * Prevents alert fatigue by suppressing repeated notifications within a time window.
 */
public interface CooldownPort {

    boolean isOnCooldown(RuleId ruleId, String referenceId);

    void setCooldown(RuleId ruleId, String referenceId, int cooldownMinutes);

    /**
     * Atomically attempts to acquire a cooldown lock.
     * Returns {@code true} if the cooldown was successfully acquired (first trigger within window).
     * Returns {@code false} if a cooldown is already active for this rule + entity combination.
     */
    boolean tryAcquireCooldown(RuleId ruleId, String referenceId, int cooldownMinutes);
}
