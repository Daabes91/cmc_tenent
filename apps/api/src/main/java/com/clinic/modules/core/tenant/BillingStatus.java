package com.clinic.modules.core.tenant;

/**
 * Enum representing the billing status of a tenant.
 */
public enum BillingStatus {
    /**
     * Tenant has been created but payment has not been completed yet.
     */
    PENDING_PAYMENT,

    /**
     * Tenant has an active subscription and full access to the platform.
     */
    ACTIVE,

    /**
     * Tenant's payment is overdue but still has limited access.
     */
    PAST_DUE,

    /**
     * Tenant subscription is suspended but can be reactivated.
     */
    SUSPENDED,

    /**
     * Tenant's subscription has been canceled and access is restricted.
     */
    CANCELED
}
