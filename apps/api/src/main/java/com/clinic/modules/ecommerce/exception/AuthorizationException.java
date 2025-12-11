package com.clinic.modules.ecommerce.exception;

/**
 * Exception thrown when authorization fails in e-commerce operations.
 * Used for permission checks, role validation, and access control failures.
 */
public class AuthorizationException extends EcommerceException {

    private final String resource;
    private final String action;
    private final String userId;
    private final String tenantId;

    public AuthorizationException(String message) {
        super(message);
        this.resource = null;
        this.action = null;
        this.userId = null;
        this.tenantId = null;
    }

    public AuthorizationException(String resource, String action, String message) {
        super(message);
        this.resource = resource;
        this.action = action;
        this.userId = null;
        this.tenantId = null;
    }

    public AuthorizationException(String resource, String action, String userId, String tenantId, String message) {
        super(message);
        this.resource = resource;
        this.action = action;
        this.userId = userId;
        this.tenantId = tenantId;
    }

    public AuthorizationException(String resource, String action, String userId, String tenantId, String message, Throwable cause) {
        super(message, cause);
        this.resource = resource;
        this.action = action;
        this.userId = userId;
        this.tenantId = tenantId;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }

    public String getUserId() {
        return userId;
    }

    public String getTenantId() {
        return tenantId;
    }
}