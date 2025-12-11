package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for initiating a payment for an order.
 */
public class PaymentInitiationRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Return URL is required")
    @Size(max = 500, message = "Return URL must not exceed 500 characters")
    private String returnUrl;

    @NotBlank(message = "Cancel URL is required")
    @Size(max = 500, message = "Cancel URL must not exceed 500 characters")
    private String cancelUrl;

    // Constructors
    public PaymentInitiationRequest() {}

    public PaymentInitiationRequest(Long orderId, String returnUrl, String cancelUrl) {
        this.orderId = orderId;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    @Override
    public String toString() {
        return "PaymentInitiationRequest{" +
                "orderId=" + orderId +
                ", returnUrl='" + returnUrl + '\'' +
                ", cancelUrl='" + cancelUrl + '\'' +
                '}';
    }
}