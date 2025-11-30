package com.clinic.modules.saas.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for updating PayPal configuration.
 */
public class PayPalConfigRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    private String clientSecret;

    private String planId;

    @NotNull(message = "Sandbox mode flag is required")
    private Boolean sandboxMode;

    private String webhookId;

    @Valid
    @NotEmpty(message = "At least one plan configuration is required")
    private List<PayPalPlanConfigDto> planConfigs = new ArrayList<>();

    // Getters and Setters

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public Boolean getSandboxMode() {
        return sandboxMode;
    }

    public void setSandboxMode(Boolean sandboxMode) {
        this.sandboxMode = sandboxMode;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public List<PayPalPlanConfigDto> getPlanConfigs() {
        return planConfigs;
    }

    @JsonSetter("planConfigs")
    public void setPlanConfigs(Object raw) {
        this.planConfigs = normalizePlanConfigs(raw);
    }

    /**
     * Accept both arrays and map-like objects (e.g., {"0": {...}}) for planConfigs to be lenient with client payloads.
     */
    private List<PayPalPlanConfigDto> normalizePlanConfigs(Object raw) {
        List<PayPalPlanConfigDto> configs = new ArrayList<>();
        if (raw == null) return configs;

        if (raw instanceof List<?>) {
            for (Object item : (List<?>) raw) {
                PayPalPlanConfigDto dto = toPlanConfig(item);
                if (dto != null) configs.add(dto);
            }
            return configs;
        }

        if (raw instanceof Map<?, ?> map) {
            Collection<?> values = map.values();
            for (Object item : values) {
                PayPalPlanConfigDto dto = toPlanConfig(item);
                if (dto != null) configs.add(dto);
            }
        }

        return configs;
    }

    private PayPalPlanConfigDto toPlanConfig(Object value) {
        if (value instanceof PayPalPlanConfigDto dto) {
            return dto;
        }
        if (value instanceof Map<?, ?> map) {
            PayPalPlanConfigDto dto = new PayPalPlanConfigDto();
            dto.setTier(string(map.get("tier")));
            dto.setDisplayName(string(map.get("displayName")));
            dto.setDescription(string(map.get("description")));
            dto.setCurrency(string(map.get("currency")));
            dto.setMonthlyPlanId(string(map.get("monthlyPlanId")));
            dto.setAnnualPlanId(string(map.get("annualPlanId")));
            dto.setMonthlyPrice(number(map.get("monthlyPrice")));
            dto.setAnnualPrice(number(map.get("annualPrice")));

            Object features = map.get("features");
            if (features instanceof List<?>) {
                List<String> featureList = new ArrayList<>();
                for (Object f : (List<?>) features) {
                    String s = string(f);
                    if (s != null) featureList.add(s);
                }
                dto.setFeatures(featureList);
            }
            return dto;
        }
        return null;
    }

    private String string(Object value) {
        if (value == null) return null;
        String s = value.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private BigDecimal number(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
