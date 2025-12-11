package com.clinic.modules.ecommerce.model;

/**
 * Enumeration of call-to-action types for carousel items.
 * 
 * Defines the different types of actions that can be triggered from carousel items.
 */
public enum CallToActionType {
    /**
     * No call-to-action button.
     */
    NONE,
    
    /**
     * Add to cart action.
     */
    ADD_TO_CART,
    
    /**
     * Link to external or internal URL.
     */
    LINK
}