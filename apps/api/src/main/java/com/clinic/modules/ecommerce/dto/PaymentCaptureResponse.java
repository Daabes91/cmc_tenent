package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.PaymentStatus;

/**
 * Response DTO for payment capture containing capture details.
 */
public class PaymentCaptureResponse {

    private String paymentId;
    private String captureId;
    private PaymentStatus status;
    private boolean success;
    private String message;
    private Long orderId;

    // Constructors
    public PaymentCaptureResponse() {}

    public PaymentCaptureResponse(String paymentId, String captureId, PaymentStatus status, Long orderId) {
        this.paymentId = paymentId;
        this.captureId = captureId;
        this.status = status;
        this.orderId = orderId;
        this.success = true;
        this.message = "Payment captured successfully";
    }

    public static PaymentCaptureResponse success(String paymentId, String captureId, PaymentStatus status, Long orderId) {
        return new PaymentCaptureResponse(paymentId, captureId, status, orderId);
    }

    public static PaymentCaptureResponse error(String message) {
        PaymentCaptureResponse response = new PaymentCaptureResponse();
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

    public String getCaptureId() {
        return captureId;
    }

    public void setCaptureId(String captureId) {
        this.captureId = captureId;
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

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "PaymentCaptureResponse{" +
                "paymentId='" + paymentId + '\'' +
                ", captureId='" + captureId + '\'' +
                ", status=" + status +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", orderId=" + orderId +
                '}';
    }
}