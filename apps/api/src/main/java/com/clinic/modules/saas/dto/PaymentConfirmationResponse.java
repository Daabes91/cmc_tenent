package com.clinic.modules.saas.dto;

/**
 * DTO for payment confirmation response.
 * Contains session token and redirect URL after successful payment verification.
 */
public class PaymentConfirmationResponse {

    private boolean success;
    private String sessionToken;
    private String redirectUrl;
    private String error;

    public PaymentConfirmationResponse() {
    }

    public PaymentConfirmationResponse(boolean success, String sessionToken, String redirectUrl) {
        this.success = success;
        this.sessionToken = sessionToken;
        this.redirectUrl = redirectUrl;
    }

    public PaymentConfirmationResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Static factory methods for convenience

    public static PaymentConfirmationResponse success(String sessionToken, String redirectUrl) {
        return new PaymentConfirmationResponse(true, sessionToken, redirectUrl);
    }

    public static PaymentConfirmationResponse error(String error) {
        return new PaymentConfirmationResponse(false, error);
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
