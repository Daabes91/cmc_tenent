package com.clinic.modules.ecommerce.exception;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for e-commerce exception classes.
 * Tests the exception constructors and getter methods.
 */
class EcommerceExceptionTest {

    @Test
    void testProductNotFoundException() {
        // Test basic constructor
        ProductNotFoundException basicException = new ProductNotFoundException("Product not found");
        assertEquals("Product not found", basicException.getMessage());
        assertNull(basicException.getProductId());
        assertNull(basicException.getTenantId());

        // Test constructor with IDs
        Long productId = 123L;
        Long tenantId = 456L;
        ProductNotFoundException detailedException = new ProductNotFoundException(productId, tenantId);
        assertEquals(productId, detailedException.getProductId());
        assertEquals(tenantId, detailedException.getTenantId());
        assertTrue(detailedException.getMessage().contains(productId.toString()));
        assertTrue(detailedException.getMessage().contains(tenantId.toString()));

        // Test constructor with custom message
        String customMessage = "Custom error message";
        ProductNotFoundException customException = new ProductNotFoundException(productId, tenantId, customMessage);
        assertEquals(customMessage, customException.getMessage());
        assertEquals(productId, customException.getProductId());
        assertEquals(tenantId, customException.getTenantId());
    }

    @Test
    void testInsufficientStockException() {
        // Test basic constructor
        InsufficientStockException basicException = new InsufficientStockException("Insufficient stock");
        assertEquals("Insufficient stock", basicException.getMessage());
        assertNull(basicException.getProductId());
        assertNull(basicException.getVariantId());
        assertNull(basicException.getRequestedQuantity());
        assertNull(basicException.getAvailableQuantity());

        // Test constructor with details
        Long productId = 123L;
        Long variantId = 789L;
        Integer requestedQuantity = 10;
        Integer availableQuantity = 5;
        String message = "Not enough stock available";
        
        InsufficientStockException detailedException = new InsufficientStockException(
                productId, variantId, requestedQuantity, availableQuantity, message);
        
        assertEquals(message, detailedException.getMessage());
        assertEquals(productId, detailedException.getProductId());
        assertEquals(variantId, detailedException.getVariantId());
        assertEquals(requestedQuantity, detailedException.getRequestedQuantity());
        assertEquals(availableQuantity, detailedException.getAvailableQuantity());
    }

    @Test
    void testInvalidCartStateException() {
        // Test basic constructor
        InvalidCartStateException basicException = new InvalidCartStateException("Invalid cart state");
        assertEquals("Invalid cart state", basicException.getMessage());
        assertNull(basicException.getCartId());
        assertNull(basicException.getOperation());

        // Test constructor with details
        String cartId = "cart-123";
        String operation = "ADD_ITEM";
        String message = "Cannot add item to cart";
        
        InvalidCartStateException detailedException = new InvalidCartStateException(cartId, operation, message);
        assertEquals(message, detailedException.getMessage());
        assertEquals(cartId, detailedException.getCartId());
        assertEquals(operation, detailedException.getOperation());
    }

    @Test
    void testPaymentProcessingException() {
        // Test basic constructor
        PaymentProcessingException basicException = new PaymentProcessingException("Payment failed");
        assertEquals("Payment failed", basicException.getMessage());
        assertEquals("UNKNOWN", basicException.getPaymentProvider());
        assertNull(basicException.getOrderId());
        assertNull(basicException.getProviderErrorCode());

        // Test constructor with provider and order
        String paymentProvider = "PAYPAL";
        String orderId = "order-123";
        String message = "Payment was declined";
        
        PaymentProcessingException providerException = new PaymentProcessingException(paymentProvider, orderId, message);
        assertTrue(providerException.getMessage().contains(paymentProvider));
        assertTrue(providerException.getMessage().contains(orderId));
        assertTrue(providerException.getMessage().contains(message));
        assertEquals(paymentProvider, providerException.getPaymentProvider());
        assertEquals(orderId, providerException.getOrderId());
        assertNull(providerException.getProviderErrorCode());

        // Test constructor with error code
        String providerErrorCode = "PAYMENT_DECLINED";
        PaymentProcessingException fullException = new PaymentProcessingException(
                paymentProvider, orderId, message, providerErrorCode);
        
        assertEquals(paymentProvider, fullException.getPaymentProvider());
        assertEquals(orderId, fullException.getOrderId());
        assertEquals(providerErrorCode, fullException.getProviderErrorCode());
    }

    @Test
    void testEcommerceFeatureDisabledException() {
        Long tenantId = 456L;
        EcommerceFeatureDisabledException exception = new EcommerceFeatureDisabledException(tenantId);
        
        assertEquals(tenantId, exception.getTenantId());
        assertTrue(exception.getMessage().contains(tenantId.toString()));
        assertTrue(exception.getMessage().toLowerCase().contains("not enabled"));

        // Test with custom message
        String customMessage = "Custom feature disabled message";
        EcommerceFeatureDisabledException customException = new EcommerceFeatureDisabledException(tenantId, customMessage);
        assertEquals(customMessage, customException.getMessage());
        assertEquals(tenantId, customException.getTenantId());
    }

    @Test
    void testRateLimitExceededException() {
        String operation = "cart";
        int limit = 60;
        int windowSeconds = 60;
        Instant retryAfter = Instant.now().plusSeconds(30);
        
        RateLimitExceededException exception = new RateLimitExceededException(operation, limit, windowSeconds, retryAfter);
        
        assertEquals(operation, exception.getOperation());
        assertEquals(limit, exception.getLimit());
        assertEquals(windowSeconds, exception.getWindowSeconds());
        assertEquals(retryAfter, exception.getRetryAfter());
        
        // Test retry after seconds calculation
        long retryAfterSeconds = exception.getRetryAfterSeconds();
        assertTrue(retryAfterSeconds >= 0);
        assertTrue(retryAfterSeconds <= 30);
        
        assertTrue(exception.getMessage().contains(operation));
        assertTrue(exception.getMessage().contains(String.valueOf(limit)));
        assertTrue(exception.getMessage().contains(String.valueOf(windowSeconds)));
    }

    @Test
    void testAuthenticationException() {
        // Test basic constructor
        AuthenticationException basicException = new AuthenticationException("Authentication failed");
        assertEquals("Authentication failed", basicException.getMessage());
        assertEquals("UNKNOWN", basicException.getAuthenticationType());
        assertNull(basicException.getReason());

        // Test constructor with details
        String authenticationType = "JWT";
        String reason = "TOKEN_EXPIRED";
        String message = "JWT token has expired";
        
        AuthenticationException detailedException = new AuthenticationException(authenticationType, reason, message);
        assertEquals(message, detailedException.getMessage());
        assertEquals(authenticationType, detailedException.getAuthenticationType());
        assertEquals(reason, detailedException.getReason());
    }

    @Test
    void testAuthorizationException() {
        // Test basic constructor
        AuthorizationException basicException = new AuthorizationException("Access denied");
        assertEquals("Access denied", basicException.getMessage());
        assertNull(basicException.getResource());
        assertNull(basicException.getAction());
        assertNull(basicException.getUserId());
        assertNull(basicException.getTenantId());

        // Test constructor with resource and action
        String resource = "products";
        String action = "CREATE";
        String message = "Cannot create products";
        
        AuthorizationException resourceException = new AuthorizationException(resource, action, message);
        assertEquals(message, resourceException.getMessage());
        assertEquals(resource, resourceException.getResource());
        assertEquals(action, resourceException.getAction());
        assertNull(resourceException.getUserId());
        assertNull(resourceException.getTenantId());

        // Test constructor with full details
        String userId = "user-123";
        String tenantId = "tenant-456";
        
        AuthorizationException fullException = new AuthorizationException(resource, action, userId, tenantId, message);
        assertEquals(message, fullException.getMessage());
        assertEquals(resource, fullException.getResource());
        assertEquals(action, fullException.getAction());
        assertEquals(userId, fullException.getUserId());
        assertEquals(tenantId, fullException.getTenantId());
    }

    @Test
    void testEcommerceExceptionHierarchy() {
        // Verify all e-commerce exceptions extend EcommerceException
        assertTrue(new ProductNotFoundException("test") instanceof EcommerceException);
        assertTrue(new InsufficientStockException("test") instanceof EcommerceException);
        assertTrue(new InvalidCartStateException("test") instanceof EcommerceException);
        assertTrue(new PaymentProcessingException("test") instanceof EcommerceException);
        assertTrue(new EcommerceFeatureDisabledException(1L) instanceof EcommerceException);
        assertTrue(new RateLimitExceededException("test", 1, 1, Instant.now()) instanceof EcommerceException);
        assertTrue(new AuthenticationException("test") instanceof EcommerceException);
        assertTrue(new AuthorizationException("test") instanceof EcommerceException);
        
        // Verify EcommerceException extends RuntimeException
        assertTrue(new EcommerceException("test") instanceof RuntimeException);
    }
}