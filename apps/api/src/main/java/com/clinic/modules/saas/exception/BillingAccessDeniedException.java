package com.clinic.modules.saas.exception;

import com.clinic.modules.core.tenant.BillingStatus;

/**
 * Exception thrown when access is denied due to billing status.
 * Maps to HTTP 403 Forbidden.
 */
public class BillingAccessDeniedException extends RuntimeException {
    private final Long tenantId;
    private final BillingStatus billingStatus;

    public BillingAccessDeniedException(Long tenantId, BillingStatus billingStatus) {
        super("Access denied for tenant " + tenantId + " due to billing status: " + billingStatus);
        this.tenantId = tenantId;
        this.billingStatus = billingStatus;
    }

    public BillingAccessDeniedException(Long tenantId, BillingStatus billingStatus, String customMessage) {
        super(customMessage);
        this.tenantId = tenantId;
        this.billingStatus = billingStatus;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public BillingStatus getBillingStatus() {
        return billingStatus;
    }
}
