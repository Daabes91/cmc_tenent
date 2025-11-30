package com.clinic.modules.saas.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO describing a PayPal plan mapping for a specific tier.
 */
public class PayPalPlanConfigDto {

    @NotBlank(message = "Plan tier is required")
    private String tier;

    private String monthlyPlanId;
    private String annualPlanId;
    private String displayName;
    private String description;
    private String currency;
    private java.math.BigDecimal monthlyPrice;
    private java.math.BigDecimal annualPrice;
    private java.util.List<String> features;

    public PayPalPlanConfigDto() {
    }

    public PayPalPlanConfigDto(String tier, String monthlyPlanId, String annualPlanId) {
        this.tier = tier;
        this.monthlyPlanId = monthlyPlanId;
        this.annualPlanId = annualPlanId;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getMonthlyPlanId() {
        return monthlyPlanId;
    }

    public void setMonthlyPlanId(String monthlyPlanId) {
        this.monthlyPlanId = monthlyPlanId;
    }

    public String getAnnualPlanId() {
        return annualPlanId;
    }

    public void setAnnualPlanId(String annualPlanId) {
        this.annualPlanId = annualPlanId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public java.math.BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(java.math.BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public java.math.BigDecimal getAnnualPrice() {
        return annualPrice;
    }

    public void setAnnualPrice(java.math.BigDecimal annualPrice) {
        this.annualPrice = annualPrice;
    }

    public java.util.List<String> getFeatures() {
        return features;
    }

    public void setFeatures(java.util.List<String> features) {
        this.features = features;
    }

    public String getPlanIdForCycle(String billingCycle) {
        if ("ANNUAL".equalsIgnoreCase(billingCycle)) {
            return annualPlanId;
        }
        return monthlyPlanId;
    }
}
