package com.fleet.application.rule.port.out;

import com.fleet.application.shared.event.RuleTriggeredEvent;

public interface RuleEventPublisherPort {
    void publish(RuleTriggeredEvent event);
}
