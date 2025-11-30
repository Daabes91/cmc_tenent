package com.clinic.modules.saas.dto;

/**
 * Response DTO for payment confirmation endpoint.
 * Contains session token and redirect URL after successful payment.
 */
public class PaymentConfirmationResponse {

    private boolean success;
    private String sessionToken;
    private String redirectUrl;
    private String error;

    public PaymentConfirmationResponse() {
    }

    public static PaymentConfirmationResponse success(String sessionToken, String redirectUrl) {
        PaymentConfirmationResponse response = new PaymentConfirmationResponse();
        response.success = true;
        response.sessionToken = sessionToken;
        response.redirectUrl = redirectUrl;
        return response;
    }

    public static PaymentConfirmationResponse error(String error) {
        PaymentConfirmationResponse response = new PaymentConfirmationResponse();
        response.success = false;
        response.error = error;
        return response;
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
