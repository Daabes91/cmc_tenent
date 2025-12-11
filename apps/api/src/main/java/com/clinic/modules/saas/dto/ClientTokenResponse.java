package com.clinic.modules.saas.dto;

import java.time.Instant;

/**
 * Response DTO for PayPal client token generation.
 * Contains the client token used to initialize hosted fields and its expiration time.
 */
public class ClientTokenResponse {
    private String clientToken;
    private Instant expiresAt;
    private String planId;

    public ClientTokenResponse() {
    }

    public ClientTokenResponse(String clientToken, Instant expiresAt, String planId) {
        this.clientToken = clientToken;
        this.expiresAt = expiresAt;
        this.planId = planId;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }
}
