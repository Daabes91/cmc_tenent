package com.clinic.config;

import com.clinic.api.ApiError;
import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.publicapi.exception.DoctorNotFoundException;
import com.clinic.modules.saas.exception.ConflictException;
import com.clinic.modules.saas.exception.NotFoundException;
import com.clinic.modules.saas.exception.UnauthorizedException;
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

}
