package com.clinic.modules.saas.exception;

/**
 * Exception thrown when attempting to create a tenant with a subdomain that already exists.
 * Maps to HTTP 409 Conflict.
 */
public class SubdomainExistsException extends RuntimeException {
    private final String subdomain;

    public SubdomainExistsException(String subdomain) {
        super("Subdomain '" + subdomain + "' is already taken");
        this.subdomain = subdomain;
    }

    public String getSubdomain() {
        return subdomain;
    }
}
