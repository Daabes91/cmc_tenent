package com.clinic.config;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.publicapi.exception.DoctorNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

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

}
