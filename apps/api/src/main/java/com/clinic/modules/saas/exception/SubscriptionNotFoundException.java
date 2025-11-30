package com.clinic.modules.saas.exception;

/**
 * Exception thrown when a subscription is not found.
 * Maps to HTTP 404 Not Found.
 */
public class SubscriptionNotFoundException extends RuntimeException {
    private final String subscriptionId;

    public SubscriptionNotFoundException(String subscriptionId) {
        super("Subscription not found: " + subscriptionId);
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
