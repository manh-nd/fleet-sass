package com.fleet.application.notification;

import com.fleet.domain.notification.model.NotificationAction.ChannelType;
import com.fleet.domain.notification.port.out.EmailSenderPort;
import com.fleet.domain.notification.port.out.PushSenderPort;
import com.fleet.domain.notification.port.out.SmsSenderPort;
import com.fleet.domain.notification.port.out.WebhookSenderPort;

import lombok.RequiredArgsConstructor;

/**
 * Routes a notification to the correct channel-specific sender port.
 *
 * <p>This replaces the fat {@code NotificationDispatcherPort} interface.
 * Each channel has its own focused port injected here, so adapters only
 * implement the interface they actually support — no more
 * {@code UnsupportedOperationException} stubs.</p>
 *
 * <p>Adding a new channel (e.g. WHATSAPP) requires only:</p>
 * <ol>
 *   <li>Adding the new {@code ChannelType} enum value</li>
 *   <li>Injecting the new sender port here</li>
 *   <li>Adding a {@code case} in {@link #dispatch}</li>
 * </ol>
 */
@RequiredArgsConstructor
public class ChannelDispatchRouter {

    private final EmailSenderPort   emailSender;
    private final SmsSenderPort     smsSender;
    private final WebhookSenderPort webhookSender;
    private final PushSenderPort    pushSender;

    /**
     * Dispatches {@code content} to the correct channel adapter.
     *
     * @param channel   target delivery channel
     * @param recipient email / phone / device token / URL
     * @param subject   subject line (meaningful for EMAIL and PUSH; ignored by SMS/WEBHOOK)
     * @param content   rendered message body
     */
    public void dispatch(ChannelType channel, String recipient, String subject, String content) {
        switch (channel) {
            case EMAIL   -> emailSender.send(recipient, subject, content);
            case SMS     -> smsSender.send(recipient, content);
            case WEBHOOK -> webhookSender.send(recipient, content);
            case PUSH    -> pushSender.send(recipient, subject, content);
        }
    }
}
