package com.clinic.modules.saas.dto;

/**
 * Response for inline (on-site) signup flow using PayPal JS SDK.
 */
public class InlineSignupResponse {
    private boolean success;
    private String subscriptionId;
    private Long tenantId;
    private String error;

    public InlineSignupResponse() {}

    public InlineSignupResponse(boolean success, String subscriptionId, Long tenantId, String error) {
        this.success = success;
        this.subscriptionId = subscriptionId;
        this.tenantId = tenantId;
        this.error = error;
    }

    public static InlineSignupResponse success(String subscriptionId, Long tenantId) {
        return new InlineSignupResponse(true, subscriptionId, tenantId, null);
    }

    public static InlineSignupResponse error(String message) {
        return new InlineSignupResponse(false, null, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
