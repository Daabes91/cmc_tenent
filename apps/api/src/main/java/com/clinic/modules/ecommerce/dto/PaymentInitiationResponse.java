package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.PaymentStatus;

/**
 * Response DTO for payment initiation containing PayPal approval URL.
 */
public class PaymentInitiationResponse {

    private String paymentId;
    private String providerOrderId;
    private String approvalUrl;
    private PaymentStatus status;
    private boolean success;
    private String message;

    // Constructors
    public PaymentInitiationResponse() {}

    public PaymentInitiationResponse(String paymentId, String providerOrderId, String approvalUrl, PaymentStatus status) {
        this.paymentId = paymentId;
        this.providerOrderId = providerOrderId;
        this.approvalUrl = approvalUrl;
        this.status = status;
        this.success = true;
        this.message = "Payment initiated successfully";
    }

    public static PaymentInitiationResponse success(String paymentId, String providerOrderId, String approvalUrl, PaymentStatus status) {
        return new PaymentInitiationResponse(paymentId, providerOrderId, approvalUrl, status);
    }

    public static PaymentInitiationResponse error(String message) {
        PaymentInitiationResponse response = new PaymentInitiationResponse();
        response.success = false;
        response.message = message;
        return response;
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getProviderOrderId() {
        return providerOrderId;
    }

    public void setProviderOrderId(String providerOrderId) {
        this.providerOrderId = providerOrderId;
    }

    public String getApprovalUrl() {
        return approvalUrl;
    }

    public void setApprovalUrl(String approvalUrl) {
        this.approvalUrl = approvalUrl;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PaymentInitiationResponse{" +
                "paymentId='" + paymentId + '\'' +
                ", providerOrderId='" + providerOrderId + '\'' +
                ", approvalUrl='" + approvalUrl + '\'' +
                ", status=" + status +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}