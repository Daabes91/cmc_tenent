package com.clinic.modules.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for manual billing status override by SaaS managers.
 */
public class BillingStatusOverrideRequest {

    // Path variable is the source of truth; keep optional in payload to avoid validation failures
    private Long tenantId;

    @NotBlank(message = "Billing status is required")
    private String billingStatus;

    @NotBlank(message = "Reason is required")
    private String reason;

    public BillingStatusOverrideRequest() {
    }

    public BillingStatusOverrideRequest(Long tenantId, String billingStatus, String reason) {
        this.tenantId = tenantId;
        this.billingStatus = billingStatus;
        this.reason = reason;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(String billingStatus) {
        this.billingStatus = billingStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
