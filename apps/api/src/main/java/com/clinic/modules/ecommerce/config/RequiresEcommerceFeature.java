package com.clinic.modules.ecommerce.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods or classes that require e-commerce feature to be enabled.
 * 
 * This annotation can be used on controller methods or classes to automatically
 * validate that the e-commerce feature is enabled for the tenant before processing
 * the request.
 * 
 * Usage:
 * <pre>
 * {@code
 * @RequiresEcommerceFeature
 * @GetMapping("/products")
 * public ResponseEntity<List<Product>> getProducts(@PathVariable Long tenantId) {
 *     // Method implementation
 * }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresEcommerceFeature {
}