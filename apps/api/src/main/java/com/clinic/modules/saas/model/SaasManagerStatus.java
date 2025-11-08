package com.clinic.modules.saas.model;

/**
 * Status enum for SAAS Manager accounts.
 * Defines the operational state of a SAAS Manager in the system.
 */
public enum SaasManagerStatus {
    /**
     * Manager account is active and can perform operations
     */
    ACTIVE,
    
    /**
     * Manager account is suspended and cannot perform operations
     */
    SUSPENDED
}
