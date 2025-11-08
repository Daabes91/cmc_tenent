package com.clinic.modules.saas.exception;

/**
 * Exception thrown when a resource conflict occurs (e.g., duplicate slug).
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
