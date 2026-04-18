package com.fleet.domain.notification.port.out;

/**
 * Outbound port for delivering webhook notifications.
 *
 * <p>Implementations should sign the payload with HMAC-SHA256 and enforce
 * a connect/read timeout to avoid blocking the caller on unresponsive endpoints.</p>
 */
public interface WebhookSenderPort {

    /**
     * POSTs a JSON payload to a remote webhook URL.
     *
     * @param url         target webhook endpoint
     * @param jsonPayload raw JSON body
     */
    void send(String url, String jsonPayload);
}
