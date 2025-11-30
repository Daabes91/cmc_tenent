package com.clinic.modules.saas.dto;

/**
 * DTO for clinic signup response.
 * Contains the PayPal approval URL and tenant ID for the signup flow.
 */
public class SignupResponse {

    private boolean success;
    private String approvalUrl;
    private Long tenantId;
    private String error;

    public SignupResponse() {
    }

    public SignupResponse(boolean success, String approvalUrl, Long tenantId) {
        this.success = success;
        this.approvalUrl = approvalUrl;
        this.tenantId = tenantId;
    }

    public SignupResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Static factory methods for convenience

    public static SignupResponse success(String approvalUrl, Long tenantId) {
        return new SignupResponse(true, approvalUrl, tenantId);
    }

    public static SignupResponse error(String error) {
        return new SignupResponse(false, error);
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getApprovalUrl() {
        return approvalUrl;
    }

    public void setApprovalUrl(String approvalUrl) {
        this.approvalUrl = approvalUrl;
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
