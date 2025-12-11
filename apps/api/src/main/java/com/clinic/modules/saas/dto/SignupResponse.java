package com.clinic.modules.saas.dto;

/**
 * DTO for clinic signup response.
 * Contains the tenant ID and PayPal approval URL for the redirect-based flow.
 */
public class SignupResponse {

    private boolean success;
    private Long tenantId;
    private String approvalUrl;
    private String error;

    public SignupResponse() {
    }

    public SignupResponse(boolean success, Long tenantId, String approvalUrl) {
        this.success = success;
        this.tenantId = tenantId;
        this.approvalUrl = approvalUrl;
    }

    public SignupResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Static factory methods for convenience

    public static SignupResponse success(Long tenantId, String approvalUrl) {
        return new SignupResponse(true, tenantId, approvalUrl);
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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getApprovalUrl() {
        return approvalUrl;
    }

    public void setApprovalUrl(String approvalUrl) {
        this.approvalUrl = approvalUrl;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
