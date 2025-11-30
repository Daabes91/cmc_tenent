package com.clinic.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception,
                                                                       HttpServletRequest request) {
        List<ApiError> errors = new ArrayList<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.add(ApiError.of(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        ApiResponse<Void> response = ApiResponseFactory.error(
                "VALIDATION_ERROR",
                "Validation failed.",
                errors,
                metaWithPath(request, HttpStatus.BAD_REQUEST)
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception,
                                                                       HttpServletRequest request) {
        List<ApiError> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            errors.add(ApiError.of(violation.getPropertyPath().toString(), violation.getMessage()));
        }

        ApiResponse<Void> response = ApiResponseFactory.error(
                "CONSTRAINT_VIOLATION",
                "Validation failed.",
                errors,
                metaWithPath(request, HttpStatus.BAD_REQUEST)
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException exception,
                                                                  HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        ApiResponse<Void> response = ApiResponseFactory.error(
                resolveStatusCode(status),
                exception.getReason() != null ? exception.getReason() : status.getReasonPhrase(),
                null,
                metaWithPath(request, status)
        );
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException exception,
                                                               HttpServletRequest request) {
        ApiResponse<Void> response = ApiResponseFactory.error(
                "MALFORMED_JSON",
                "Request body is invalid or malformed.",
                null,
                metaWithPath(request, HttpStatus.BAD_REQUEST)
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception exception,
                                                           HttpServletRequest request) {
        log.error("Unhandled exception for request {} {}", request.getMethod(), request.getRequestURI(), exception);
        ApiResponse<Void> response = ApiResponseFactory.error(
                "INTERNAL_ERROR",
                "An unexpected error occurred.",
                null,
                metaWithPath(request, HttpStatus.INTERNAL_SERVER_ERROR)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private Map<String, Object> metaWithPath(HttpServletRequest request, HttpStatus status) {
        return Map.of(
                "status", status.value(),
                "path", request.getRequestURI()
        );
    }

    private String resolveStatusCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "BAD_REQUEST";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case NOT_FOUND -> "NOT_FOUND";
            case CONFLICT -> "CONFLICT";
            case TOO_MANY_REQUESTS -> "RATE_LIMITED";
            default -> "ERROR_" + status.value();
        };
    }
}
