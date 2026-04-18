package com.fleet.domain.notification.port.out;

/**
 * Outbound port for sending SMS notifications.
 *
 * <p>Implementations include AWS SNS, Twilio, or a console stub for local dev.</p>
 */
public interface SmsSenderPort {

    /**
     * Sends an SMS message.
     *
     * @param phone   recipient phone number (E.164 format recommended)
     * @param message message body
     */
    void send(String phone, String message);
}
