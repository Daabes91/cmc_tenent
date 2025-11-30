package com.clinic.modules.saas.exception;

/**
 * Exception thrown when webhook signature verification fails.
 * Maps to HTTP 401 Unauthorized.
 * This is a security-critical exception that should trigger alerts.
 */
public class WebhookVerificationException extends RuntimeException {
    private final String transmissionId;

    public WebhookVerificationException(String transmissionId) {
        super("Webhook signature verification failed for transmission ID: " + transmissionId);
        this.transmissionId = transmissionId;
    }

    public WebhookVerificationException(String transmissionId, String reason) {
        super("Webhook signature verification failed for transmission ID: " + transmissionId + ". Reason: " + reason);
        this.transmissionId = transmissionId;
    }

    public String getTransmissionId() {
        return transmissionId;
    }
}
