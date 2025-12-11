package com.clinic.modules.saas.dto;

import java.time.Instant;

/**
 * Response DTO for card-based subscription creation.
 * Contains subscription details after successful creation.
 */
public class CardSubscriptionResponse {

    private String subscriptionId;
    private String status;
    private String tenantId;
    private Instant activatedAt;

    public CardSubscriptionResponse() {
    }

    public CardSubscriptionResponse(String subscriptionId, String status, String tenantId, Instant activatedAt) {
        this.subscriptionId = subscriptionId;
        this.status = status;
        this.tenantId = tenantId;
        this.activatedAt = activatedAt;
    }

    // Getters and Setters

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Instant activatedAt) {
        this.activatedAt = activatedAt;
    }
}
