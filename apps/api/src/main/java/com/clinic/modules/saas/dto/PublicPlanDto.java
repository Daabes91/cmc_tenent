package com.clinic.modules.saas.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public-facing DTO for marketing site pricing.
 */
public class PublicPlanDto {
    private String tier;
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal annualPrice;
    private String currency;
    private List<String> features;
    private String paypalProductId;
    private String monthlyPlanId;
    private String annualPlanId;
    private boolean popular;

    public PublicPlanDto() {
    }

    public PublicPlanDto(String tier,
                         String name,
                         String description,
                         BigDecimal monthlyPrice,
                         BigDecimal annualPrice,
                         String currency,
                         List<String> features,
                         String paypalProductId,
                         String monthlyPlanId,
                         String annualPlanId,
                         boolean popular) {
        this.tier = tier;
        this.name = name;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.annualPrice = annualPrice;
        this.currency = currency;
        this.features = features;
        this.paypalProductId = paypalProductId;
        this.monthlyPlanId = monthlyPlanId;
        this.annualPlanId = annualPlanId;
        this.popular = popular;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public BigDecimal getAnnualPrice() {
        return annualPrice;
    }

    public void setAnnualPrice(BigDecimal annualPrice) {
        this.annualPrice = annualPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getPaypalProductId() {
        return paypalProductId;
    }

    public void setPaypalProductId(String paypalProductId) {
        this.paypalProductId = paypalProductId;
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

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }
}
