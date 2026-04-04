package com.fleet.domain.rule.port.out;

import com.fleet.domain.rule.vo.RuleId;

public interface CooldownPort {

    boolean isOnCooldown(RuleId ruleId, String vehicleId);

    void setCooldown(RuleId ruleId, String vehicleId, int cooldownMinutes);
}
