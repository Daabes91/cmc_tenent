package com.clinic.modules.ecommerce.model;

/**
 * Enumeration of payment status values.
 * 
 * Defines the states of payment transactions in the e-commerce system.
 */
public enum PaymentStatus {
    /**
     * Payment record created but not yet processed.
     */
    CREATED,
    
    /**
     * Payment is being processed.
     */
    PENDING,
    
    /**
     * Payment has been approved by the provider.
     */
    APPROVED,
    
    /**
     * Payment has been captured by the provider.
     */
    CAPTURED,
    
    /**
     * Payment has been captured/completed.
     */
    COMPLETED,
    
    /**
     * Payment failed or was declined.
     */
    FAILED,
    
    /**
     * Payment was cancelled.
     */
    CANCELLED,
    
    /**
     * Payment was refunded.
     */
    REFUNDED
}