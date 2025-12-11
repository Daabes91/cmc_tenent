package com.clinic.modules.ecommerce.model;

/**
 * Enumeration of carousel types.
 * 
 * Defines the different types of carousels supported in the e-commerce system.
 */
public enum CarouselType {
    /**
     * Hero banner carousel for main page displays.
     */
    HERO,
    
    /**
     * Product showcase carousel.
     */
    PRODUCT,
    
    /**
     * Category showcase carousel.
     */
    CATEGORY,
    
    /**
     * Brand showcase carousel.
     */
    BRAND,
    
    /**
     * Special offers carousel.
     */
    OFFER,
    
    /**
     * Customer testimonials carousel.
     */
    TESTIMONIAL,
    
    /**
     * Blog posts carousel.
     */
    BLOG,
    
    /**
     * Mixed content carousel.
     */
    MIXED,

    /**
     * Dynamic carousel that auto-populates all visible products (no manual items needed).
     */
    VIEW_ALL_PRODUCTS
}
