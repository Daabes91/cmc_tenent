package com.clinic.modules.saas.config;

import com.clinic.modules.saas.model.PlanTier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.*;

/**
 * Configuration class for subscription plan tiers.
 * Manages plan details, PayPal mappings, pricing, and features for each tier.
 */
@Configuration
@ConfigurationProperties(prefix = "subscription.plans")
public class PlanTierConfig {

    private Map<String, PlanTierDetails> tiers = new HashMap<>();

    public PlanTierConfig() {
        initializeDefaultPlans();
    }

    /**
     * Initialize default plan configurations.
     * These can be overridden by application.yml or environment variables.
     */
    private void initializeDefaultPlans() {
        // Basic Plan
        PlanTierDetails basic = new PlanTierDetails();
        basic.setTierName("Basic");
        basic.setDescription("Essential features for small clinics");
        basic.setPaypalProductId("PROD_BASIC");
        basic.setPaypalMonthlyPlanId("PLAN_BASIC_MONTHLY");
        basic.setPaypalAnnualPlanId("PLAN_BASIC_ANNUAL");
        basic.setPricing(createPricing("29.99", "299.99"));
        basic.setFeatures(Arrays.asList(
            "Up to 100 patients",
            "Basic appointment scheduling",
            "Patient records management",
            "Email support"
        ));
        basic.setMaxPatients(100);
        basic.setMaxStaff(2);
        basic.setMaxDoctors(2);
        tiers.put(PlanTier.BASIC.name(), basic);

        // Professional Plan
        PlanTierDetails professional = new PlanTierDetails();
        professional.setTierName("Professional");
        professional.setDescription("Advanced features for growing practices");
        professional.setPaypalProductId("PROD_PROFESSIONAL");
        professional.setPaypalMonthlyPlanId("PLAN_PROFESSIONAL_MONTHLY");
        professional.setPaypalAnnualPlanId("PLAN_PROFESSIONAL_ANNUAL");
        professional.setPricing(createPricing("79.99", "799.99"));
        professional.setFeatures(Arrays.asList(
            "Up to 500 patients",
            "Advanced scheduling with reminders",
            "Treatment plans and billing",
            "Multi-currency support",
            "Priority email support",
            "Custom branding"
        ));
        professional.setMaxPatients(500);
        professional.setMaxStaff(10);
        professional.setMaxDoctors(10);
        tiers.put(PlanTier.PROFESSIONAL.name(), professional);

        // Enterprise Plan
        PlanTierDetails enterprise = new PlanTierDetails();
        enterprise.setTierName("Enterprise");
        enterprise.setDescription("Full-featured solution for large organizations");
        enterprise.setPaypalProductId("PROD_ENTERPRISE");
        enterprise.setPaypalMonthlyPlanId("PLAN_ENTERPRISE_MONTHLY");
        enterprise.setPaypalAnnualPlanId("PLAN_ENTERPRISE_ANNUAL");
        enterprise.setPricing(createPricing("199.99", "1999.99"));
        enterprise.setFeatures(Arrays.asList(
            "Unlimited patients",
            "Advanced analytics and reporting",
            "Multi-location support",
            "API access",
            "Dedicated account manager",
            "24/7 phone support",
            "Custom integrations"
        ));
        enterprise.setMaxPatients(-1); // Unlimited
        enterprise.setMaxStaff(-1); // Unlimited
        enterprise.setMaxDoctors(-1); // Unlimited
        tiers.put(PlanTier.ENTERPRISE.name(), enterprise);

        // Custom Plan
        PlanTierDetails custom = new PlanTierDetails();
        custom.setTierName("Custom");
        custom.setDescription("Tailored solution with negotiated terms");
        custom.setPaypalProductId("PROD_CUSTOM");
        custom.setPaypalMonthlyPlanId("PLAN_CUSTOM_MONTHLY");
        custom.setPaypalAnnualPlanId("PLAN_CUSTOM_ANNUAL");
        custom.setPricing(createPricing("0.00", "0.00")); // Negotiated pricing
        custom.setFeatures(Arrays.asList(
            "Custom feature set",
            "Negotiated pricing",
            "Dedicated support",
            "Custom SLA"
        ));
        custom.setMaxPatients(-1);
        custom.setMaxStaff(-1);
        custom.setMaxDoctors(-1);
        tiers.put(PlanTier.CUSTOM.name(), custom);
    }

    private Map<String, Map<String, BigDecimal>> createPricing(String monthlyUsd, String annualUsd) {
        Map<String, Map<String, BigDecimal>> pricing = new HashMap<>();
        
        Map<String, BigDecimal> usdPricing = new HashMap<>();
        usdPricing.put("monthly", new BigDecimal(monthlyUsd));
        usdPricing.put("annual", new BigDecimal(annualUsd));
        pricing.put("USD", usdPricing);

        // Add other currencies with approximate conversions
        Map<String, BigDecimal> eurPricing = new HashMap<>();
        eurPricing.put("monthly", new BigDecimal(monthlyUsd).multiply(new BigDecimal("0.92")));
        eurPricing.put("annual", new BigDecimal(annualUsd).multiply(new BigDecimal("0.92")));
        pricing.put("EUR", eurPricing);

        Map<String, BigDecimal> gbpPricing = new HashMap<>();
        gbpPricing.put("monthly", new BigDecimal(monthlyUsd).multiply(new BigDecimal("0.79")));
        gbpPricing.put("annual", new BigDecimal(annualUsd).multiply(new BigDecimal("0.79")));
        pricing.put("GBP", gbpPricing);

        return pricing;
    }

    /**
     * Get plan details for a specific tier.
     * Results are cached for 1 hour to improve performance.
     */
    @org.springframework.cache.annotation.Cacheable(value = "planTierConfig", key = "'details_' + #tier.name()", cacheManager = "billingSaasCacheManager")
    public PlanTierDetails getPlanDetails(PlanTier tier) {
        if (tier == null) {
            return null;
        }
        return tiers.get(tier.name());
    }

    /**
     * Get PayPal product ID for a tier.
     * Results are cached for 1 hour to improve performance.
     */
    @org.springframework.cache.annotation.Cacheable(value = "planTierConfig", key = "'productId_' + #tier.name()", cacheManager = "billingSaasCacheManager")
    public String getPayPalProductId(PlanTier tier) {
        PlanTierDetails details = getPlanDetails(tier);
        return details != null ? details.getPaypalProductId() : null;
    }

    /**
     * Get PayPal plan ID for a tier and billing cycle.
     * Results are cached for 1 hour to improve performance.
     */
    @org.springframework.cache.annotation.Cacheable(value = "planTierConfig", key = "'planId_' + #tier.name() + '_' + #billingCycle", cacheManager = "billingSaasCacheManager")
    public String getPayPalPlanId(PlanTier tier, String billingCycle) {
        PlanTierDetails details = getPlanDetails(tier);
        if (details == null) {
            return null;
        }
        
        if ("ANNUAL".equalsIgnoreCase(billingCycle)) {
            return details.getPaypalAnnualPlanId();
        }
        return details.getPaypalMonthlyPlanId();
    }

    /**
     * Get price for a tier, currency, and billing cycle.
     * Results are cached for 1 hour to improve performance.
     */
    @org.springframework.cache.annotation.Cacheable(value = "planTierConfig", key = "'price_' + #tier.name() + '_' + #currency + '_' + #billingCycle", cacheManager = "billingSaasCacheManager")
    public BigDecimal getPrice(PlanTier tier, String currency, String billingCycle) {
        PlanTierDetails details = getPlanDetails(tier);
        if (details == null || details.getPricing() == null) {
            return BigDecimal.ZERO;
        }

        Map<String, BigDecimal> currencyPricing = details.getPricing().get(currency.toUpperCase());
        if (currencyPricing == null) {
            // Default to USD if currency not found
            currencyPricing = details.getPricing().get("USD");
        }

        if (currencyPricing == null) {
            return BigDecimal.ZERO;
        }

        String cycle = "ANNUAL".equalsIgnoreCase(billingCycle) ? "annual" : "monthly";
        return currencyPricing.getOrDefault(cycle, BigDecimal.ZERO);
    }

    /**
     * Get features list for a tier.
     * Results are cached for 1 hour to improve performance.
     */
    @org.springframework.cache.annotation.Cacheable(value = "planTierConfig", key = "'features_' + #tier.name()", cacheManager = "billingSaasCacheManager")
    public List<String> getFeatures(PlanTier tier) {
        PlanTierDetails details = getPlanDetails(tier);
        return details != null ? details.getFeatures() : Collections.emptyList();
    }

    /**
     * Attempt to resolve a PlanTier by matching the provided PayPal plan ID
     * against the configured monthly/annual IDs for each tier.
     */
    public PlanTier findTierByPayPalPlanId(String planId) {
        if (planId == null || planId.isBlank()) {
            return null;
        }

        String normalized = planId.trim();
        for (Map.Entry<String, PlanTierDetails> entry : tiers.entrySet()) {
            PlanTierDetails details = entry.getValue();
            if (details == null) {
                continue;
            }

            if (normalized.equalsIgnoreCase(details.getPaypalMonthlyPlanId())
                    || normalized.equalsIgnoreCase(details.getPaypalAnnualPlanId())) {
                try {
                    return PlanTier.valueOf(entry.getKey());
                } catch (IllegalArgumentException ignored) {
                    // If configuration uses unknown tier name, skip
                }
            }
        }
        return null;
    }

    /**
     * Get all available plan tiers.
     */
    public Map<String, PlanTierDetails> getAllTiers() {
        return new HashMap<>(tiers);
    }

    /**
     * Check if a tier exists.
     */
    public boolean tierExists(PlanTier tier) {
        return tier != null && tiers.containsKey(tier.name());
    }

    // Getters and Setters for Spring Boot configuration binding
    public Map<String, PlanTierDetails> getTiers() {
        return tiers;
    }

    public void setTiers(Map<String, PlanTierDetails> tiers) {
        this.tiers = tiers;
    }

    /**
     * Inner class representing details for a specific plan tier.
     */
    public static class PlanTierDetails {
        private String tierName;
        private String description;
        private String paypalProductId;
        private String paypalMonthlyPlanId;
        private String paypalAnnualPlanId;
        private Map<String, Map<String, BigDecimal>> pricing; // Currency -> (cycle -> price)
        private List<String> features;
        private int maxPatients;
        private int maxStaff;
        private int maxDoctors;

        // Getters and Setters
        public String getTierName() {
            return tierName;
        }

        public void setTierName(String tierName) {
            this.tierName = tierName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPaypalProductId() {
            return paypalProductId;
        }

        public void setPaypalProductId(String paypalProductId) {
            this.paypalProductId = paypalProductId;
        }

        public String getPaypalMonthlyPlanId() {
            return paypalMonthlyPlanId;
        }

        public void setPaypalMonthlyPlanId(String paypalMonthlyPlanId) {
            this.paypalMonthlyPlanId = paypalMonthlyPlanId;
        }

        public String getPaypalAnnualPlanId() {
            return paypalAnnualPlanId;
        }

        public void setPaypalAnnualPlanId(String paypalAnnualPlanId) {
            this.paypalAnnualPlanId = paypalAnnualPlanId;
        }

        public Map<String, Map<String, BigDecimal>> getPricing() {
            return pricing;
        }

        public void setPricing(Map<String, Map<String, BigDecimal>> pricing) {
            this.pricing = pricing;
        }

        public List<String> getFeatures() {
            return features;
        }

        public void setFeatures(List<String> features) {
            this.features = features;
        }

        public int getMaxPatients() {
            return maxPatients;
        }

        public void setMaxPatients(int maxPatients) {
            this.maxPatients = maxPatients;
        }

        public int getMaxStaff() {
            return maxStaff;
        }

        public void setMaxStaff(int maxStaff) {
            this.maxStaff = maxStaff;
        }

        public int getMaxDoctors() {
            return maxDoctors;
        }

        public void setMaxDoctors(int maxDoctors) {
            this.maxDoctors = maxDoctors;
        }
    }
}
