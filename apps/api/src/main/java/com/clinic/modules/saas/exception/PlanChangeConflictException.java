package com.clinic.modules.saas.exception;

/**
 * Exception thrown when a plan change conflicts with an existing pending change.
 * Maps to HTTP 409 Conflict.
 */
public class PlanChangeConflictException extends RuntimeException {
    private final Long tenantId;
    private final String pendingChange;

    public PlanChangeConflictException(Long tenantId, String pendingChange) {
        super("Cannot process plan change for tenant " + tenantId + 
              ". A pending change is already scheduled: " + pendingChange);
        this.tenantId = tenantId;
        this.pendingChange = pendingChange;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getPendingChange() {
        return pendingChange;
    }
}
