package com.clinic.modules.saas.exception;

/**
 * Exception thrown when PayPal configuration is missing or invalid.
 * Maps to HTTP 503 Service Unavailable.
 */
public class PayPalConfigurationException extends RuntimeException {
    private final String missingConfig;

    public PayPalConfigurationException(String missingConfig) {
        super("PayPal configuration error: " + missingConfig);
        this.missingConfig = missingConfig;
    }

    public PayPalConfigurationException(String missingConfig, Throwable cause) {
        super("PayPal configuration error: " + missingConfig, cause);
        this.missingConfig = missingConfig;
    }

    public String getMissingConfig() {
        return missingConfig;
    }
}
