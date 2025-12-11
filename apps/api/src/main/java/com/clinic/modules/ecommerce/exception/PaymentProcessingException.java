package com.clinic.modules.ecommerce.exception;

/**
 * Exception thrown when payment operations fail.
 * 
 * This can occur during:
 * - Payment provider communication failures
 * - Payment validation errors
 * - Payment capture or refund failures
 */
public class PaymentProcessingException extends EcommerceException {

    private final String paymentProvider;
    private final String orderId;
    private final String providerErrorCode;

    public PaymentProcessingException(String message) {
        super(message);
        this.paymentProvider = "UNKNOWN";
        this.orderId = null;
        this.providerErrorCode = null;
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.paymentProvider = "UNKNOWN";
        this.orderId = null;
        this.providerErrorCode = null;
    }

    public PaymentProcessingException(String paymentProvider, String orderId, String message) {
        super("Payment processing failed for order " + orderId + " via " + paymentProvider + ": " + message);
        this.paymentProvider = paymentProvider;
        this.orderId = orderId;
        this.providerErrorCode = null;
    }

    public PaymentProcessingException(String paymentProvider, String orderId, 
                                   String message, String providerErrorCode) {
        super("Payment processing failed for order " + orderId + " via " + paymentProvider + ": " + message);
        this.paymentProvider = paymentProvider;
        this.orderId = orderId;
        this.providerErrorCode = providerErrorCode;
    }

    public PaymentProcessingException(String paymentProvider, String orderId, 
                                   String message, Throwable cause) {
        super("Payment processing failed for order " + orderId + " via " + paymentProvider + ": " + message, cause);
        this.paymentProvider = paymentProvider;
        this.orderId = orderId;
        this.providerErrorCode = null;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProviderErrorCode() {
        return providerErrorCode;
    }
}