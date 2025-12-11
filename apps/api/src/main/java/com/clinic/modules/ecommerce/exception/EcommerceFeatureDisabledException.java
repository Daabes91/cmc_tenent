package com.clinic.modules.ecommerce.exception;

/**
 * Exception thrown when e-commerce functionality is accessed but not enabled for the tenant.
 * 
 * This exception is thrown by the feature flag validation system when a tenant
 * attempts to access e-commerce endpoints but the feature is disabled for their account.
 */
public class EcommerceFeatureDisabledException extends EcommerceException {

    private final Long tenantId;

    public EcommerceFeatureDisabledException(Long tenantId) {
        super("E-commerce feature is not enabled for tenant: " + tenantId);
        this.tenantId = tenantId;
    }

    public EcommerceFeatureDisabledException(Long tenantId, String message) {
        super(message);
        this.tenantId = tenantId;
    }

    public Long getTenantId() {
        return tenantId;
    }
}