package com.clinic.modules.ecommerce.exception;

import com.clinic.api.ApiError;
import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for e-commerce error handling functionality.
 * Verifies that exceptions are properly handled and converted to structured API responses.
 */
class EcommerceErrorHandlingTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServletWebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/public/products/123");
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void testProductNotFoundException() {
        // Given
        Long productId = 123L;
        Long tenantId = 456L;
        ProductNotFoundException exception = new ProductNotFoundException(productId, tenantId);

        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleProductNotFoundException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PRODUCT_NOT_FOUND", response.getBody().getErrorCode());
        assertFalse(response.getBody().isSuccess());
        
        List<ApiError> errors = response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        
        // Verify error details contain product and tenant IDs
        assertTrue(errors.stream().anyMatch(error -> "productId".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "tenantId".equals(error.getField())));
    }

    @Test
    void testInsufficientStockException() {
        // Given
        Long productId = 123L;
        Long variantId = 789L;
        Integer requestedQuantity = 10;
        Integer availableQuantity = 5;
        InsufficientStockException exception = new InsufficientStockException(
                productId, variantId, requestedQuantity, availableQuantity, 
                "Insufficient stock available");

        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleInsufficientStockException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INSUFFICIENT_STOCK", response.getBody().getErrorCode());
        assertFalse(response.getBody().isSuccess());
        
        List<ApiError> errors = response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(4, errors.size());
        
        // Verify error details contain all stock information
        assertTrue(errors.stream().anyMatch(error -> "productId".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "variantId".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "requestedQuantity".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "availableQuantity".equals(error.getField())));
    }

    @Test
    void testInvalidCartStateException() {
        // Given
        String cartId = "cart-123";
        String operation = "ADD_ITEM";
        InvalidCartStateException exception = new InvalidCartStateException(cartId, operation, "Invalid cart state");

        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleInvalidCartStateException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_CART_STATE", response.getBody().getErrorCode());
        assertFalse(response.getBody().isSuccess());
        
        List<ApiError> errors = response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        
        // Verify error details contain cart information
        assertTrue(errors.stream().anyMatch(error -> "cartId".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "operation".equals(error.getField())));
    }

    @Test
    void testPaymentProcessingException() {
        // Given
        String paymentProvider = "PAYPAL";
        String orderId = "order-123";
        String providerErrorCode = "PAYMENT_DECLINED";
        PaymentProcessingException exception = new PaymentProcessingException(
                paymentProvider, orderId, "Payment was declined", providerErrorCode);

        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handlePaymentProcessingException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PAYMENT_PROCESSING_ERROR", response.getBody().getErrorCode());
        assertFalse(response.getBody().isSuccess());
        
        List<ApiError> errors = response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        
        // Verify error details contain payment information
        assertTrue(errors.stream().anyMatch(error -> "paymentProvider".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "orderId".equals(error.getField())));
    }

    @Test
    void testEcommerceFeatureDisabledException() {
        // Given
        Long tenantId = 456L;
        EcommerceFeatureDisabledException exception = new EcommerceFeatureDisabledException(tenantId);

        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleEcommerceFeatureDisabledException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ECOMMERCE_FEATURE_DISABLED", response.getBody().getErrorCode());
        assertFalse(response.getBody().isSuccess());
        
        List<ApiError> errors = response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        
        // Verify error details contain tenant ID
        assertTrue(errors.stream().anyMatch(error -> "tenantId".equals(error.getField())));
    }

    @Test
    void testRateLimitExceededException() {
        // Given
        String operation = "cart";
        int limit = 60;
        int windowSeconds = 60;
        java.time.Instant retryAfter = java.time.Instant.now().plusSeconds(30);
        RateLimitExceededException exception = new RateLimitExceededException(operation, limit, windowSeconds, retryAfter);

        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleRateLimitExceededException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RATE_LIMIT_EXCEEDED", response.getBody().getErrorCode());
        assertFalse(response.getBody().isSuccess());
        
        // Verify rate limit headers are set
        assertTrue(response.getHeaders().containsKey("Retry-After"));
        assertTrue(response.getHeaders().containsKey("X-RateLimit-Limit"));
        assertTrue(response.getHeaders().containsKey("X-RateLimit-Window"));
        assertTrue(response.getHeaders().containsKey("X-RateLimit-Reset"));
        
        List<ApiError> errors = response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(4, errors.size());
        
        // Verify error details contain rate limit information
        assertTrue(errors.stream().anyMatch(error -> "operation".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "limit".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "windowSeconds".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "retryAfterSeconds".equals(error.getField())));
    }

    @Test
    void testAuthenticationException() {
        // Given
        String authenticationType = "JWT";
        String reason = "TOKEN_EXPIRED";
        AuthenticationException exception = new AuthenticationException(authenticationType, reason, "JWT token has expired");

        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAuthenticationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AUTHENTICATION_FAILED", response.getBody().getErrorCode());
        assertFalse(response.getBody().isSuccess());
        
        List<ApiError> errors = response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        
        // Verify error details contain authentication information
        assertTrue(errors.stream().anyMatch(error -> "authenticationType".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "reason".equals(error.getField())));
    }

    @Test
    void testAuthorizationException() {
        // Given
        String resource = "products";
        String action = "CREATE";
        String userId = "user-123";
        String tenantId = "tenant-456";
        AuthorizationException exception = new AuthorizationException(resource, action, userId, tenantId, "Access denied");

        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAuthorizationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AUTHORIZATION_FAILED", response.getBody().getErrorCode());
        assertFalse(response.getBody().isSuccess());
        
        List<ApiError> errors = response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(4, errors.size());
        
        // Verify error details contain authorization information
        assertTrue(errors.stream().anyMatch(error -> "resource".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "action".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "userId".equals(error.getField())));
        assertTrue(errors.stream().anyMatch(error -> "tenantId".equals(error.getField())));
    }

    @Test
    void testApiResponseFactoryErrorMethods() {
        // Test basic error response creation
        ApiResponse<Void> errorResponse = ApiResponseFactory.error("TEST_ERROR", "Test error message");
        assertNotNull(errorResponse);
        assertEquals("TEST_ERROR", errorResponse.getErrorCode());
        assertEquals("Test error message", errorResponse.getMessage());
        assertFalse(errorResponse.isSuccess());

        // Test error response with details
        List<ApiError> errors = List.of(
                ApiError.of("field1", "Error in field 1"),
                ApiError.of("field2", "Error in field 2")
        );
        ApiResponse<Void> detailedErrorResponse = ApiResponseFactory.error("VALIDATION_ERROR", "Validation failed", errors);
        assertNotNull(detailedErrorResponse);
        assertEquals("VALIDATION_ERROR", detailedErrorResponse.getErrorCode());
        assertEquals("Validation failed", detailedErrorResponse.getMessage());
        assertEquals(2, detailedErrorResponse.getErrors().size());
        assertFalse(detailedErrorResponse.isSuccess());
    }

    @Test
    void testApiResponseFactorySuccessMethods() {
        // Test basic success response
        String data = "test data";
        ApiResponse<String> successResponse = ApiResponseFactory.success(data);
        assertNotNull(successResponse);
        assertEquals(data, successResponse.getData());
        assertTrue(successResponse.isSuccess());

        // Test success response with message
        ApiResponse<String> successWithMessage = ApiResponse.success(data, "Operation successful");
        assertNotNull(successWithMessage);
        assertEquals(data, successWithMessage.getData());
        assertEquals("Operation successful", successWithMessage.getMessage());
        assertTrue(successWithMessage.isSuccess());
    }
}