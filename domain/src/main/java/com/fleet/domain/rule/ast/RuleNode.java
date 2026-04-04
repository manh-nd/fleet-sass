package com.fleet.domain.rule.ast;

import com.fleet.domain.rule.vo.EventPayload;

public interface RuleNode {
    boolean evaluate(EventPayload payload);
}
