package com.clinic.modules.saas.dto;

/**
 * Response DTO for PayPal configuration.
 * Client secret is never returned for security reasons.
 */
public class PayPalConfigResponse {

    private Long id;
    private String clientId;
    private String planId;
    private String webhookId;
    private Boolean sandboxMode;
    private String maskedClientSecret;
    private java.util.List<PayPalPlanConfigDto> planConfigs = new java.util.ArrayList<>();

    public PayPalConfigResponse() {
    }

    public PayPalConfigResponse(Long id, String clientId, String planId, String webhookId, Boolean sandboxMode) {
        this.id = id;
        this.clientId = clientId;
        this.planId = planId;
        this.webhookId = webhookId;
        this.sandboxMode = sandboxMode;
        this.maskedClientSecret = "****";
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public Boolean getSandboxMode() {
        return sandboxMode;
    }

    public void setSandboxMode(Boolean sandboxMode) {
        this.sandboxMode = sandboxMode;
    }

    public String getMaskedClientSecret() {
        return maskedClientSecret;
    }

    public void setMaskedClientSecret(String maskedClientSecret) {
        this.maskedClientSecret = maskedClientSecret;
    }

    public java.util.List<PayPalPlanConfigDto> getPlanConfigs() {
        return planConfigs;
    }

    public void setPlanConfigs(java.util.List<PayPalPlanConfigDto> planConfigs) {
        this.planConfigs = planConfigs;
    }
}
