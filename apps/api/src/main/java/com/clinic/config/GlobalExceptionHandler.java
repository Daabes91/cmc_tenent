package com.clinic.config;

import com.clinic.api.ApiError;
import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.publicapi.exception.DoctorNotFoundException;
import com.clinic.modules.saas.exception.*;
import com.clinic.modules.ecommerce.exception.*;
import org.springframework.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Global exception handler for REST controllers.
 * Handles security exceptions and converts them to proper HTTP responses.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle DoctorNotFoundException when a doctor is not found.
     * Returns HTTP 404 Not Found.
     */
    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDoctorNotFoundException(
            DoctorNotFoundException ex,
            WebRequest request) {

        log.warn("Doctor not found for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseFactory.error(
                        "DOCTOR_NOT_FOUND",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle AccessDeniedException thrown by @PreAuthorize when permission is denied.
     * Returns HTTP 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        log.warn("Access denied for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponseFactory.error(
                        "ACCESS_DENIED",
                        "You do not have permission to access this resource.",
                        null
                ));
    }

    /**
     * Handle ConflictException when a resource conflict occurs (e.g., duplicate slug).
     * Returns HTTP 409 Conflict.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflictException(
            ConflictException ex,
            WebRequest request) {

        log.warn("Conflict for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponseFactory.error(
                        "CONFLICT",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle NotFoundException when a requested resource is not found.
     * Returns HTTP 404 Not Found.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
            NotFoundException ex,
            WebRequest request) {

        log.warn("Not found for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseFactory.error(
                        "NOT_FOUND",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle UnauthorizedException when authentication fails.
     * Returns HTTP 401 Unauthorized.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
            UnauthorizedException ex,
            WebRequest request) {

        log.warn("Unauthorized access for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseFactory.error(
                        "UNAUTHORIZED",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle ResponseStatusException from Spring framework.
     * Returns the appropriate HTTP status with error details.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(
            ResponseStatusException ex,
            WebRequest request) {

        log.warn("Response status exception for request {}: {} - {}",
                request.getDescription(false),
                ex.getStatusCode(),
                ex.getReason());

        String errorCode = ex.getStatusCode().toString().replace(" ", "_").toUpperCase();
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponseFactory.error(
                        errorCode,
                        message,
                        null
                ));
    }

    /**
     * Handle validation errors from @Valid annotations.
     * Returns HTTP 400 Bad Request with field-specific error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Validation failed for request {}", request.getDescription(false));

        List<ApiError> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return ApiError.of(fieldName, errorMessage);
                })
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseFactory.error(
                        "VALIDATION_ERROR",
                        "Validation failed",
                        errors
                ));
    }

    /**
     * Handle SubdomainExistsException when a subdomain is already taken.
     * Returns HTTP 409 Conflict.
     */
    @ExceptionHandler(SubdomainExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleSubdomainExistsException(
            SubdomainExistsException ex,
            WebRequest request) {

        log.warn("Subdomain conflict for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponseFactory.error(
                        "SUBDOMAIN_EXISTS",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle PayPalApiException when PayPal API calls fail.
     * Returns HTTP 502 Bad Gateway.
     */
    @ExceptionHandler(PayPalApiException.class)
    public ResponseEntity<ApiResponse<Void>> handlePayPalApiException(
            PayPalApiException ex,
            WebRequest request) {

        log.error("PayPal API error for request {}: {}", request.getDescription(false), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponseFactory.error(
                        "PAYPAL_API_ERROR",
                        "Payment service temporarily unavailable. Please try again later.",
                        null
                ));
    }

    /**
     * Handle SubscriptionNotFoundException when a subscription is not found.
     * Returns HTTP 404 Not Found.
     */
    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSubscriptionNotFoundException(
            SubscriptionNotFoundException ex,
            WebRequest request) {

        log.warn("Subscription not found for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseFactory.error(
                        "SUBSCRIPTION_NOT_FOUND",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle InvalidSubscriptionStatusException when subscription status is invalid.
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(InvalidSubscriptionStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSubscriptionStatusException(
            InvalidSubscriptionStatusException ex,
            WebRequest request) {

        log.warn("Invalid subscription status for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseFactory.error(
                        "INVALID_SUBSCRIPTION_STATUS",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle WebhookVerificationException when webhook signature verification fails.
     * Returns HTTP 401 Unauthorized.
     * This is a security-critical exception that should trigger alerts.
     */
    @ExceptionHandler(WebhookVerificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleWebhookVerificationException(
            WebhookVerificationException ex,
            WebRequest request) {

        log.error("SECURITY ALERT: Webhook verification failed for request {}: {}", 
                request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseFactory.error(
                        "WEBHOOK_VERIFICATION_FAILED",
                        "Webhook signature verification failed",
                        null
                ));
    }

    /**
     * Handle BillingAccessDeniedException when access is denied due to billing status.
     * Returns HTTP 403 Forbidden.
     */
    @ExceptionHandler(BillingAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleBillingAccessDeniedException(
            BillingAccessDeniedException ex,
            WebRequest request) {

        log.warn("Billing access denied for request {}: Tenant {} has billing status {}", 
                request.getDescription(false), ex.getTenantId(), ex.getBillingStatus());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponseFactory.error(
                        "BILLING_ACCESS_DENIED",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle InvalidPlanTierException when an invalid plan tier is specified.
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(InvalidPlanTierException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPlanTierException(
            InvalidPlanTierException ex,
            WebRequest request) {

        log.warn("Invalid plan tier for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseFactory.error(
                        "INVALID_PLAN_TIER",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle PlanChangeConflictException when a plan change conflicts with existing pending change.
     * Returns HTTP 409 Conflict.
     */
    @ExceptionHandler(PlanChangeConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlanChangeConflictException(
            PlanChangeConflictException ex,
            WebRequest request) {

        log.warn("Plan change conflict for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponseFactory.error(
                        "PLAN_CHANGE_CONFLICT",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle PayPalConfigurationException when PayPal configuration is missing or invalid.
     * Returns HTTP 503 Service Unavailable.
     */
    @ExceptionHandler(PayPalConfigurationException.class)
    public ResponseEntity<ApiResponse<Void>> handlePayPalConfigurationException(
            PayPalConfigurationException ex,
            WebRequest request) {

        log.error("PayPal configuration error for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponseFactory.error(
                        "PAYPAL_CONFIGURATION_ERROR",
                        "Payment service is not properly configured. Please contact support.",
                        null
                ));
    }

    /**
     * Handle IllegalArgumentException for general validation errors.
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        log.warn("Illegal argument for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseFactory.error(
                        "INVALID_ARGUMENT",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle IllegalStateException for state-related errors.
     * Returns HTTP 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request) {

        log.warn("Illegal state for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponseFactory.error(
                        "INVALID_STATE",
                        ex.getMessage(),
                        null
                ));
    }

    /**
     * Handle PlanLimitExceededException when a tenant exceeds their subscription plan limits.
     * Returns HTTP 402 Payment Required.
     */
    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlanLimitExceededException(
            PlanLimitExceededException ex,
            WebRequest request) {

        log.warn("Plan limit exceeded for request {}: {} (current: {}, max: {})", 
                request.getDescription(false), ex.getLimitType(), ex.getCurrentCount(), ex.getMaxAllowed());

        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponseFactory.error(
                        "PLAN_LIMIT_EXCEEDED",
                        ex.getMessage(),
                        List.of(
                                ApiError.of("limitType", ex.getLimitType()),
                                ApiError.of("currentCount", String.valueOf(ex.getCurrentCount())),
                                ApiError.of("maxAllowed", String.valueOf(ex.getMaxAllowed()))
                        )
                ));
    }

    /**
     * Handle EcommerceFeatureDisabledException when e-commerce feature is not enabled.
     * Returns HTTP 403 Forbidden.
     */
    @ExceptionHandler(EcommerceFeatureDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleEcommerceFeatureDisabledException(
            EcommerceFeatureDisabledException ex,
            WebRequest request) {

        log.warn("E-commerce feature disabled for request {}: tenant {}", 
                request.getDescription(false), ex.getTenantId());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponseFactory.error(
                        "ECOMMERCE_FEATURE_DISABLED",
                        ex.getMessage(),
                        List.of(ApiError.of("tenantId", String.valueOf(ex.getTenantId())))
                ));
    }

    /**
     * Handle ProductNotFoundException when a product is not found.
     * Returns HTTP 404 Not Found.
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFoundException(
            ProductNotFoundException ex,
            WebRequest request) {

        log.warn("Product not found for request {}: product {} for tenant {}", 
                request.getDescription(false), ex.getProductId(), ex.getTenantId());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseFactory.error(
                        "PRODUCT_NOT_FOUND",
                        ex.getMessage(),
                        List.of(
                                ApiError.of("productId", String.valueOf(ex.getProductId())),
                                ApiError.of("tenantId", String.valueOf(ex.getTenantId()))
                        )
                ));
    }

    /**
     * Handle InsufficientStockException when requested quantity exceeds available stock.
     * Returns HTTP 409 Conflict.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStockException(
            InsufficientStockException ex,
            WebRequest request) {

        log.warn("Insufficient stock for request {}: product {} variant {} (requested: {}, available: {})", 
                request.getDescription(false), ex.getProductId(), ex.getVariantId(), 
                ex.getRequestedQuantity(), ex.getAvailableQuantity());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponseFactory.error(
                        "INSUFFICIENT_STOCK",
                        ex.getMessage(),
                        List.of(
                                ApiError.of("productId", String.valueOf(ex.getProductId())),
                                ApiError.of("variantId", String.valueOf(ex.getVariantId())),
                                ApiError.of("requestedQuantity", String.valueOf(ex.getRequestedQuantity())),
                                ApiError.of("availableQuantity", String.valueOf(ex.getAvailableQuantity()))
                        )
                ));
    }

    /**
     * Handle InvalidCartStateException when cart operations fail validation.
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(InvalidCartStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCartStateException(
            InvalidCartStateException ex,
            WebRequest request) {

        log.warn("Invalid cart state for request {}: cart {} operation {}", 
                request.getDescription(false), ex.getCartId(), ex.getOperation());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseFactory.error(
                        "INVALID_CART_STATE",
                        ex.getMessage(),
                        List.of(
                                ApiError.of("cartId", ex.getCartId()),
                                ApiError.of("operation", ex.getOperation())
                        )
                ));
    }

    /**
     * Handle PaymentProcessingException when payment operations fail.
     * Returns HTTP 502 Bad Gateway.
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentProcessingException(
            PaymentProcessingException ex,
            WebRequest request) {

        log.error("Payment processing error for request {}: provider {} order {} error {}", 
                request.getDescription(false), ex.getPaymentProvider(), ex.getOrderId(), 
                ex.getProviderErrorCode(), ex);

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponseFactory.error(
                        "PAYMENT_PROCESSING_ERROR",
                        "Payment processing failed. Please try again later.",
                        List.of(
                                ApiError.of("paymentProvider", ex.getPaymentProvider()),
                                ApiError.of("orderId", ex.getOrderId())
                        )
                ));
    }

    /**
     * Handle RateLimitExceededException when rate limits are exceeded.
     * Returns HTTP 429 Too Many Requests.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceededException(
            RateLimitExceededException ex,
            WebRequest request) {

        log.warn("Rate limit exceeded for request {}: operation {} limit {} window {}s", 
                request.getDescription(false), ex.getOperation(), ex.getLimit(), ex.getWindowSeconds());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        headers.add("X-RateLimit-Limit", String.valueOf(ex.getLimit()));
        headers.add("X-RateLimit-Window", String.valueOf(ex.getWindowSeconds()));
        headers.add("X-RateLimit-Reset", String.valueOf(ex.getRetryAfter().getEpochSecond()));

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(ApiResponseFactory.error(
                        "RATE_LIMIT_EXCEEDED",
                        ex.getMessage(),
                        List.of(
                                ApiError.of("operation", ex.getOperation()),
                                ApiError.of("limit", String.valueOf(ex.getLimit())),
                                ApiError.of("windowSeconds", String.valueOf(ex.getWindowSeconds())),
                                ApiError.of("retryAfterSeconds", String.valueOf(ex.getRetryAfterSeconds()))
                        )
                ));
    }

    /**
     * Handle AuthenticationException when e-commerce authentication fails.
     * Returns HTTP 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request) {

        log.warn("E-commerce authentication failed for request {}: type {} reason {}", 
                request.getDescription(false), ex.getAuthenticationType(), ex.getReason());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseFactory.error(
                        "AUTHENTICATION_FAILED",
                        ex.getMessage(),
                        List.of(
                                ApiError.of("authenticationType", ex.getAuthenticationType()),
                                ApiError.of("reason", ex.getReason())
                        )
                ));
    }

    /**
     * Handle AuthorizationException when e-commerce authorization fails.
     * Returns HTTP 403 Forbidden.
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationException(
            AuthorizationException ex,
            WebRequest request) {

        log.warn("E-commerce authorization failed for request {}: resource {} action {} user {} tenant {}", 
                request.getDescription(false), ex.getResource(), ex.getAction(), ex.getUserId(), ex.getTenantId());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponseFactory.error(
                        "AUTHORIZATION_FAILED",
                        ex.getMessage(),
                        List.of(
                                ApiError.of("resource", ex.getResource()),
                                ApiError.of("action", ex.getAction()),
                                ApiError.of("userId", ex.getUserId()),
                                ApiError.of("tenantId", ex.getTenantId())
                        )
                ));
    }

    /**
     * Handle general EcommerceException for other e-commerce related errors.
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(EcommerceException.class)
    public ResponseEntity<ApiResponse<Void>> handleEcommerceException(
            EcommerceException ex,
            WebRequest request) {

        log.warn("E-commerce error for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseFactory.error(
                        "ECOMMERCE_ERROR",
                        ex.getMessage(),
                        null
                ));
    }

}
