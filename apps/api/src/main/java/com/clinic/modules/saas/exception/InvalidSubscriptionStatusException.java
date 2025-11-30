package com.clinic.modules.saas.exception;

/**
 * Exception thrown when a subscription has an invalid status for the requested operation.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidSubscriptionStatusException extends RuntimeException {
    private final String subscriptionId;
    private final String currentStatus;
    private final String expectedStatus;

    public InvalidSubscriptionStatusException(String subscriptionId, String currentStatus, String expectedStatus) {
        super("Subscription " + subscriptionId + " has invalid status '" + currentStatus + 
              "'. Expected: " + expectedStatus);
        this.subscriptionId = subscriptionId;
        this.currentStatus = currentStatus;
        this.expectedStatus = expectedStatus;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getExpectedStatus() {
        return expectedStatus;
    }
}
