package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for capturing a PayPal payment after user approval.
 */
public class PaymentCaptureRequest {

    @NotBlank(message = "PayPal order ID is required")
    @Size(max = 255, message = "PayPal order ID must not exceed 255 characters")
    private String paypalOrderId;

    // Constructors
    public PaymentCaptureRequest() {}

    public PaymentCaptureRequest(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
    }

    // Getters and Setters
    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public void setPaypalOrderId(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
    }

    @Override
    public String toString() {
        return "PaymentCaptureRequest{" +
                "paypalOrderId='" + paypalOrderId + '\'' +
                '}';
    }
}