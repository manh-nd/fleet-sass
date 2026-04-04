package com.fleet.application.notification;

import com.fleet.application.notification.usecase.DispatchAlertUseCase;
import com.fleet.domain.notification.model.EmailSubscription;
import com.fleet.domain.notification.model.NotificationAction;
import com.fleet.domain.notification.port.out.NotificationActionRepositoryPort;
import com.fleet.domain.notification.port.out.NotificationDispatcherPort;
import com.fleet.domain.notification.port.out.SubscriptionCheckPort;
import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.EventPayload;
import com.fleet.domain.rule.vo.RuleId;

import java.util.List;
import java.util.Map;

public class DispatchAlertService implements DispatchAlertUseCase {

    private final NotificationActionRepositoryPort actionRepo;
    private final SubscriptionCheckPort subscriptionCheckPort;
    private final NotificationDispatcherPort dispatcher;

    public DispatchAlertService(NotificationActionRepositoryPort actionRepo,
            SubscriptionCheckPort subscriptionCheckPort,
            NotificationDispatcherPort dispatcher) {
        this.actionRepo = actionRepo;
        this.subscriptionCheckPort = subscriptionCheckPort;
        this.dispatcher = dispatcher;
    }

    @Override
    public void dispatch(RuleId ruleId, EventPayload payload) {
        // 1. Lấy danh sách kênh nhận thông báo của Rule này từ DB
        List<NotificationAction> actions = actionRepo.getActionsForRule(ruleId);

        for (NotificationAction action : actions) {
            String recipient = action.getRecipient();

            // 2. Kẻ gác cổng GDPR: Kiểm tra Unsubscribe cho Email
            if ("EMAIL".equalsIgnoreCase(action.getChannelType())) {
                EmailSubscription sub = subscriptionCheckPort.getSubscriptionStatus(new EmailAddress(recipient),
                        ruleId);

                // Nếu DB báo đã Unsubscribe hoặc đang Pending -> Drop!
                if (sub != null && !sub.canSend()) {
                    continue;
                }
            }

            // 3. Render nội dung tin nhắn (Thay thế các biến {{variable}})
            String message = renderTemplate(action.getMessageTemplate(), payload.data());

            // 4. Dispatch (Gửi thực tế)
            switch (action.getChannelType().toUpperCase()) {
                case "EMAIL" -> dispatcher.sendEmail(recipient, "Fleet Alert", message);
                case "SMS" -> dispatcher.sendSms(recipient, message);
                case "WEBHOOK" -> dispatcher.sendWebhook(recipient, message);
            }
        }
    }

    // Logic thay thế biến siêu đơn giản. Nếu Template có {{speed}}, sẽ được thay
    // bằng 85
    private String renderTemplate(String template, Map<String, Object> data) {
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return result;
    }
}