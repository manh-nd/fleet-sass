package com.fleet.domain.notification.port.out;

/**
 * Outbound port for sending email notifications.
 *
 * <p>Implementations include AWS SES, SMTP, or a console stub for local dev.</p>
 */
public interface EmailSenderPort {

    /**
     * Sends an email message.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    email body (HTML or plain text)
     */
    void send(String to, String subject, String body);
}
