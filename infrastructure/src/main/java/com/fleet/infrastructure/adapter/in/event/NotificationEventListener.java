package com.fleet.infrastructure.adapter.in.event;

import com.fleet.application.notification.usecase.DispatchAlertUseCase;
import com.fleet.application.shared.event.RuleTriggeredEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final DispatchAlertUseCase dispatchAlertUseCase;

    @Async
    @EventListener
    public void handleRuleTriggered(RuleTriggeredEvent event) {
        dispatchAlertUseCase.dispatch(event.ruleId(), event.payload());
    }
}