package com.clinic.modules.ecommerce.dto.validation;

/**
 * Validation groups for e-commerce operations.
 * Used to apply different validation rules based on the operation context.
 */
public class EcommerceValidationGroups {

    /**
     * Validation group for product creation operations.
     */
    public interface ProductCreate {}

    /**
     * Validation group for product update operations.
     */
    public interface ProductUpdate {}

    /**
     * Validation group for cart operations.
     */
    public interface CartOperation {}

    /**
     * Validation group for order creation operations.
     */
    public interface OrderCreate {}

    /**
     * Validation group for payment operations.
     */
    public interface PaymentOperation {}

    /**
     * Validation group for carousel operations.
     */
    public interface CarouselOperation {}

    /**
     * Validation group for admin operations.
     */
    public interface AdminOperation {}

    /**
     * Validation group for public API operations.
     */
    public interface PublicOperation {}
}