package com.fleet.infrastructure.adapter.out.event;

import com.fleet.application.rule.port.out.RuleEventPublisherPort;
import com.fleet.application.shared.event.RuleTriggeredEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringRuleEventPublisherAdapter implements RuleEventPublisherPort {

    private final ApplicationEventPublisher springPublisher;

    @Override
    public void publish(RuleTriggeredEvent event) {
        springPublisher.publishEvent(event);
    }
}