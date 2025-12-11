package com.clinic.modules.saas.dto;

/**
 * DTO for subscription creation response.
 * Contains the PayPal subscription ID and approval URL for redirect-based flow.
 */
public class SubscriptionCreationResponse {

    private String subscriptionId;
    private String approvalUrl;
    private String status;

    public SubscriptionCreationResponse() {
    }

    public SubscriptionCreationResponse(String subscriptionId, String approvalUrl, String status) {
        this.subscriptionId = subscriptionId;
        this.approvalUrl = approvalUrl;
        this.status = status;
    }

    // Getters and Setters

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getApprovalUrl() {
        return approvalUrl;
    }

    public void setApprovalUrl(String approvalUrl) {
        this.approvalUrl = approvalUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
