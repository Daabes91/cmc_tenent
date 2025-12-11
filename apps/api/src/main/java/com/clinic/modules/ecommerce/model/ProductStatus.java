package com.clinic.modules.ecommerce.model;

/**
 * Enumeration of product status values.
 * 
 * Defines the lifecycle states of products in the e-commerce system.
 */
public enum ProductStatus {
    /**
     * Product is in draft state and not visible to customers.
     */
    DRAFT,
    
    /**
     * Product is active and visible to customers.
     */
    ACTIVE,
    
    /**
     * Product is archived and not visible to customers.
     */
    ARCHIVED
}