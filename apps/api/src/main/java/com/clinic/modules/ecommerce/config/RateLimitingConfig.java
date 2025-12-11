package com.clinic.modules.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for e-commerce rate limiting.
 * Defines rate limits for different types of operations.
 */
@Configuration
@ConfigurationProperties(prefix = "ecommerce.rate-limiting")
public class RateLimitingConfig {

    /**
     * Rate limit for cart operations (requests per minute per IP).
     */
    private int cartOperationsPerMinute = 60;

    /**
     * Rate limit for order creation (requests per minute per IP).
     */
    private int orderCreationPerMinute = 10;

    /**
     * Rate limit for payment operations (requests per minute per IP).
     */
    private int paymentOperationsPerMinute = 5;

    /**
     * Rate limit for product browsing (requests per minute per IP).
     */
    private int productBrowsingPerMinute = 300;

    /**
     * Rate limit for search operations (requests per minute per IP).
     */
    private int searchOperationsPerMinute = 100;

    /**
     * Rate limit for admin operations (requests per minute per user).
     */
    private int adminOperationsPerMinute = 120;

    /**
     * Whether rate limiting is enabled.
     */
    private boolean enabled = true;

    /**
     * Time window for rate limiting in seconds.
     */
    private int windowSizeSeconds = 60;

    // Getters and setters
    public int getCartOperationsPerMinute() {
        return cartOperationsPerMinute;
    }

    public void setCartOperationsPerMinute(int cartOperationsPerMinute) {
        this.cartOperationsPerMinute = cartOperationsPerMinute;
    }

    public int getOrderCreationPerMinute() {
        return orderCreationPerMinute;
    }

    public void setOrderCreationPerMinute(int orderCreationPerMinute) {
        this.orderCreationPerMinute = orderCreationPerMinute;
    }

    public int getPaymentOperationsPerMinute() {
        return paymentOperationsPerMinute;
    }

    public void setPaymentOperationsPerMinute(int paymentOperationsPerMinute) {
        this.paymentOperationsPerMinute = paymentOperationsPerMinute;
    }

    public int getProductBrowsingPerMinute() {
        return productBrowsingPerMinute;
    }

    public void setProductBrowsingPerMinute(int productBrowsingPerMinute) {
        this.productBrowsingPerMinute = productBrowsingPerMinute;
    }

    public int getSearchOperationsPerMinute() {
        return searchOperationsPerMinute;
    }

    public void setSearchOperationsPerMinute(int searchOperationsPerMinute) {
        this.searchOperationsPerMinute = searchOperationsPerMinute;
    }

    public int getAdminOperationsPerMinute() {
        return adminOperationsPerMinute;
    }

    public void setAdminOperationsPerMinute(int adminOperationsPerMinute) {
        this.adminOperationsPerMinute = adminOperationsPerMinute;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getWindowSizeSeconds() {
        return windowSizeSeconds;
    }

    public void setWindowSizeSeconds(int windowSizeSeconds) {
        this.windowSizeSeconds = windowSizeSeconds;
    }
}