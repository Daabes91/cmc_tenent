package com.clinic.config;

import com.clinic.api.ApiError;
import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.publicapi.exception.DoctorNotFoundException;
import com.clinic.modules.saas.exception.*;
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

}
