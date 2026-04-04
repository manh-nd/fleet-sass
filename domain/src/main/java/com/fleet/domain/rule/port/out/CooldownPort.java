package com.fleet.domain.rule.port.out;

import com.fleet.domain.rule.vo.RuleId;

public interface CooldownPort {

    boolean isOnCooldown(RuleId ruleId, String vehicleId);

    void setCooldown(RuleId ruleId, String vehicleId, int cooldownMinutes);

    /**
     * Atomically attempts to acquire a cooldown.
     * Returns true if the cooldown was successfully acquired (i.e. it was not on cooldown before).
     * Returns false if it is currently on cooldown.
     */
    boolean tryAcquireCooldown(RuleId ruleId, String vehicleId, int cooldownMinutes);
}
