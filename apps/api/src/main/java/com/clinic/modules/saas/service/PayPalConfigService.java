package com.clinic.modules.saas.service;

import com.clinic.modules.saas.dto.PayPalConfigRequest;
import com.clinic.modules.saas.dto.PayPalConfigResponse;
import com.clinic.modules.saas.dto.PayPalPlanConfigDto;
import com.clinic.modules.saas.model.PayPalConfigEntity;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.repository.PayPalConfigRepository;
import com.clinic.util.EncryptionUtil;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing PayPal configuration.
 * Handles encryption/decryption of client secrets and validation of credentials.
 */
@Service
public class PayPalConfigService {

    private static final Logger logger = LoggerFactory.getLogger(PayPalConfigService.class);

    private static final String SANDBOX_BASE_URL = "https://api-m.sandbox.paypal.com";
    private static final String PRODUCTION_BASE_URL = "https://api-m.paypal.com";
    private static final String TOKEN_ENDPOINT = "/v1/oauth2/token";
    private static final long ACCESS_TOKEN_CACHE_DURATION = 9 * 60 * 60 * 1000; // 9 hours in milliseconds

    private final PayPalConfigRepository payPalConfigRepository;
    private final EncryptionUtil encryptionUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ClinicSettingsRepository clinicSettingsRepository;

    // In-memory cache for access tokens per tenant
    private final java.util.concurrent.ConcurrentHashMap<Long, TokenCache> accessTokenCache = new java.util.concurrent.ConcurrentHashMap<>();
    private TokenCache globalAccessTokenCache;

    public PayPalConfigService(
            PayPalConfigRepository payPalConfigRepository,
            EncryptionUtil encryptionUtil,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            ClinicSettingsRepository clinicSettingsRepository) {
        this.payPalConfigRepository = payPalConfigRepository;
        this.encryptionUtil = encryptionUtil;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.clinicSettingsRepository = clinicSettingsRepository;
    }

    /**
     * Get the current PayPal configuration.
     *
     * @return PayPalConfigResponse with masked client secret
     */
    @Cacheable(value = "paypalConfig", unless = "#result == null")
    public PayPalConfigResponse getConfig() {
        return payPalConfigRepository.findFirstByOrderByIdAsc()
                .map(config -> {
                    PayPalConfigResponse response = new PayPalConfigResponse(
                            config.getId(),
                            config.getClientId(),
                            config.getPlanId(),
                            config.getWebhookId(),
                            config.getSandboxMode()
                    );
                    response.setPlanConfigs(deserializePlanConfigs(config));
                    return response;
                })
                .orElseGet(() -> {
                    logger.warn("PayPal configuration not found. Returning empty defaults.");
                    PayPalConfigResponse response = new PayPalConfigResponse(
                            null,
                            "",
                            "",
                            "",
                            Boolean.TRUE
                    );
                    response.setPlanConfigs(Collections.emptyList());
                    return response;
                });
    }

    /**
     * Update PayPal configuration with validation.
     *
     * @param request PayPalConfigRequest with new configuration
     * @return Updated PayPalConfigResponse
     * @throws IllegalArgumentException if credentials are invalid
     */
    @Transactional
    @CacheEvict(value = {"paypalConfig", "paypalAccessToken"}, allEntries = true)
    public PayPalConfigResponse updateConfig(PayPalConfigRequest request) {
        logger.info("Updating PayPal configuration");

        // Find existing config or create new one
        PayPalConfigEntity config = payPalConfigRepository.findFirstByOrderByIdAsc()
                .orElse(new PayPalConfigEntity());

        boolean hasExistingSecret = config.getId() != null && config.getClientSecretEncrypted() != null;
        String providedSecret = request.getClientSecret() != null ? request.getClientSecret().trim() : null;
        String effectiveSecret;

        if (providedSecret != null && !providedSecret.isEmpty()) {
            effectiveSecret = providedSecret;
        } else if (hasExistingSecret) {
            effectiveSecret = encryptionUtil.decrypt(config.getClientSecretEncrypted());
        } else {
            throw new IllegalArgumentException("Client Secret is required for initial PayPal configuration.");
        }

        // Validate credentials before saving
        if (!validateCredentials(request.getClientId(), effectiveSecret, request.getSandboxMode())) {
            throw new IllegalArgumentException("Invalid PayPal credentials. Please check your Client ID and Secret.");
        }

        config.setClientId(request.getClientId());
        if (providedSecret != null && !providedSecret.isEmpty()) {
            config.setClientSecretEncrypted(encryptionUtil.encrypt(providedSecret));
        } else if (!hasExistingSecret) {
            // Should not happen because we enforce secret for initial config,
            // but keep safeguard to avoid null persistence.
            config.setClientSecretEncrypted(encryptionUtil.encrypt(effectiveSecret));
        }
        List<PayPalPlanConfigDto> sanitizedPlanConfigs = sanitizePlanConfigs(
                request.getPlanConfigs(),
                request.getPlanId()
        );
        if (sanitizedPlanConfigs.isEmpty()) {
            throw new IllegalArgumentException("At least one PayPal plan mapping is required.");
        }

        String fallbackPlanId = normalize(request.getPlanId());
        if (fallbackPlanId == null) {
            fallbackPlanId = sanitizedPlanConfigs.get(0).getMonthlyPlanId();
        }

        config.setPlanId(fallbackPlanId);
        config.setPlanConfig(serializePlanConfigs(sanitizedPlanConfigs));
        config.setWebhookId(request.getWebhookId());
        config.setSandboxMode(request.getSandboxMode());

        PayPalConfigEntity savedConfig = payPalConfigRepository.save(config);

        // Clear cached access tokens (all tenants) since credentials changed
        accessTokenCache.clear();
        globalAccessTokenCache = null;

        logger.info("PayPal configuration updated successfully");

        PayPalConfigResponse response = new PayPalConfigResponse(
                savedConfig.getId(),
                savedConfig.getClientId(),
                savedConfig.getPlanId(),
                savedConfig.getWebhookId(),
                savedConfig.getSandboxMode()
        );
        response.setPlanConfigs(sanitizedPlanConfigs);
        return response;
    }

    /**
     * Validate PayPal credentials by making a test API call.
     *
     * @param clientId PayPal Client ID
     * @param clientSecret PayPal Client Secret
     * @param sandboxMode Whether to use sandbox environment
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateCredentials(String clientId, String clientSecret, Boolean sandboxMode) {
        try {
            String baseUrl = Boolean.TRUE.equals(sandboxMode) ? SANDBOX_BASE_URL : PRODUCTION_BASE_URL;
            String url = baseUrl + TOKEN_ENDPOINT;

            // Create Basic Auth header
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);

            String body = "grant_type=client_credentials";

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            return response.getStatusCode() == HttpStatus.OK && response.getBody() != null;

        } catch (Exception e) {
            logger.error("PayPal credential validation failed", e);
            return false;
        }
    }

    /**
     * Get PayPal access token with caching.
     * Token is cached for 9 hours (PayPal tokens are valid for 9 hours).
     *
     * @return PayPal access token
     * @throws IllegalStateException if configuration is missing or token retrieval fails
     */
    @Cacheable(value = "paypalAccessToken", unless = "#result == null")
    public String getAccessToken() {
        long now = System.currentTimeMillis();
        if (globalAccessTokenCache != null && globalAccessTokenCache.expiresAt > now
                && globalAccessTokenCache.token != null && !globalAccessTokenCache.token.isBlank()) {
            return globalAccessTokenCache.token;
        }

        PayPalConfigEntity config = payPalConfigRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("PayPal configuration not found"));

        String clientId = config.getClientId();
        String clientSecret = encryptionUtil.decrypt(config.getClientSecretEncrypted());
        if (clientId == null || clientSecret == null || clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException("PayPal client ID/secret not configured");
        }

        try {
            String baseUrl = Boolean.TRUE.equals(config.getSandboxMode()) ? SANDBOX_BASE_URL : PRODUCTION_BASE_URL;
            String url = baseUrl + TOKEN_ENDPOINT;

            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);

            String body = "grant_type=client_credentials";

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String token = (String) response.getBody().get("access_token");
                globalAccessTokenCache = new TokenCache(token, now + ACCESS_TOKEN_CACHE_DURATION);
                return token;
            }

            throw new IllegalStateException("Failed to retrieve PayPal access token");

        } catch (Exception e) {
            logger.error("Failed to get PayPal access token (global config)", e);
            throw new IllegalStateException("Failed to retrieve PayPal access token", e);
        }
    }

    /**
     * Get the PayPal base URL based on current configuration.
     *
     * @return PayPal API base URL
     */
    public String getBaseUrl() {
        PayPalConfigEntity config = payPalConfigRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("PayPal configuration not found"));

        return Boolean.TRUE.equals(config.getSandboxMode()) ? SANDBOX_BASE_URL : PRODUCTION_BASE_URL;
    }

    /**
     * Get the PayPal plan ID from configuration.
     *
     * @return PayPal plan ID
     */
    public String getPlanId() {
        PayPalConfigEntity config = payPalConfigRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("PayPal configuration not found"));

        List<PayPalPlanConfigDto> planConfigs = deserializePlanConfigs(config);
        if (!planConfigs.isEmpty()) {
            String planId = planConfigs.get(0).getMonthlyPlanId();
            if (planId != null && !planId.isBlank()) {
                return planId;
            }
        }
        return config.getPlanId();
    }

    /**
     * Retrieve configured PayPal plan mappings.
     */
    public List<PayPalPlanConfigDto> getPlanConfigsList() {
        return payPalConfigRepository.findFirstByOrderByIdAsc()
                .map(this::deserializePlanConfigs)
                .orElseGet(ArrayList::new);
    }

    public String getAccessTokenForTenant(Long tenantId) {
        TenantPayPalConfig config = resolveTenantConfig(tenantId);
        long now = System.currentTimeMillis();
        TokenCache cached = accessTokenCache.get(tenantId);
        if (cached != null && cached.expiresAt > now && cached.token != null && !cached.token.isBlank()) {
            return cached.token;
        }

        try {
            String baseUrl = config.sandboxMode ? SANDBOX_BASE_URL : PRODUCTION_BASE_URL;
            String url = baseUrl + TOKEN_ENDPOINT;

            String auth = config.clientId + ":" + config.clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);

            String body = "grant_type=client_credentials";

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String token = (String) response.getBody().get("access_token");
                // Cache token for 9 hours from now
                accessTokenCache.put(tenantId, new TokenCache(token, now + ACCESS_TOKEN_CACHE_DURATION));
                return token;
            }

            throw new IllegalStateException("Failed to retrieve PayPal access token for tenant " + tenantId);

        } catch (Exception e) {
            logger.error("Failed to get PayPal access token for tenant {}", tenantId, e);
            throw new IllegalStateException("Failed to retrieve PayPal access token for tenant " + tenantId, e);
        }
    }

    public String getBaseUrlForTenant(Long tenantId) {
        TenantPayPalConfig config = resolveTenantConfig(tenantId);
        return config.sandboxMode ? SANDBOX_BASE_URL : PRODUCTION_BASE_URL;
    }

    private TenantPayPalConfig resolveTenantConfig(Long tenantId) {
        ClinicSettingsEntity settings = clinicSettingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Clinic settings not found for tenant " + tenantId));

        String clientId = settings.getPaypalClientId();
        String clientSecret = settings.getPaypalClientSecret();
        String env = settings.getPaypalEnvironment();

        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("PayPal client ID/secret not configured for tenant " + tenantId);
        }

        boolean sandbox = env == null || env.equalsIgnoreCase("sandbox");
        return new TenantPayPalConfig(clientId.trim(), clientSecret.trim(), sandbox);
    }

    private record TenantPayPalConfig(String clientId, String clientSecret, boolean sandboxMode) {}

    private record TokenCache(String token, long expiresAt) {}

    /**
     * Resolve PayPal plan ID for provided tier/cycle, or null if not configured.
     */
    public String getPlanIdForTier(PlanTier tier, String billingCycle) {
        if (tier == null) {
            return null;
        }
        String desiredTier = tier.name();
        return getPlanConfigsList().stream()
                .filter(cfg -> desiredTier.equalsIgnoreCase(cfg.getTier()))
                .map(cfg -> cfg.getPlanIdForCycle(billingCycle))
                .filter(id -> id != null && !id.isBlank())
                .findFirst()
                .orElse(null);
    }

    private List<PayPalPlanConfigDto> deserializePlanConfigs(PayPalConfigEntity config) {
        List<PayPalPlanConfigDto> configs = new ArrayList<>();
        String json = config.getPlanConfig();
        if (json != null && !json.isBlank()) {
            try {
                configs = objectMapper.readValue(json, new TypeReference<List<PayPalPlanConfigDto>>() {});
            } catch (Exception e) {
                logger.warn("Failed to parse PayPal plan configs from database, falling back to legacy planId", e);
            }
        }
        if (configs.isEmpty() && config.getPlanId() != null && !config.getPlanId().isBlank()) {
            configs.add(new PayPalPlanConfigDto("BASIC", config.getPlanId(), null));
        }
        return configs;
    }

    private List<PayPalPlanConfigDto> sanitizePlanConfigs(List<PayPalPlanConfigDto> incoming,
                                                          String legacyPlanId) {
        Map<String, PayPalPlanConfigDto> deduped = new LinkedHashMap<>();
        if (incoming != null) {
            for (PayPalPlanConfigDto config : incoming) {
                if (config == null) continue;
                String tier = normalize(config.getTier());
                if (tier == null) continue;
                PayPalPlanConfigDto cleaned = new PayPalPlanConfigDto(
                        tier.toUpperCase(),
                        normalize(config.getMonthlyPlanId()),
                        normalize(config.getAnnualPlanId())
                );
                cleaned.setDisplayName(normalize(config.getDisplayName()));
                cleaned.setDescription(normalize(config.getDescription()));
                cleaned.setCurrency(normalizeCurrency(config.getCurrency()));
                cleaned.setMonthlyPrice(normalizePrice(config.getMonthlyPrice()));
                cleaned.setAnnualPrice(normalizePrice(config.getAnnualPrice()));
                cleaned.setFeatures(normalizeFeatures(config.getFeatures()));
                deduped.put(cleaned.getTier(), cleaned);
            }
        }

        if (deduped.isEmpty() && legacyPlanId != null && !legacyPlanId.isBlank()) {
            deduped.put("BASIC", new PayPalPlanConfigDto("BASIC", legacyPlanId.trim(), null));
        }

        // Remove entries without any plan IDs
        deduped.values().removeIf(cfg ->
                (cfg.getMonthlyPlanId() == null || cfg.getMonthlyPlanId().isBlank()) &&
                        (cfg.getAnnualPlanId() == null || cfg.getAnnualPlanId().isBlank()));

        return new ArrayList<>(deduped.values());
    }

    private String serializePlanConfigs(List<PayPalPlanConfigDto> configs) {
        try {
            return objectMapper.writeValueAsString(configs);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize PayPal plan configs", e);
        }
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String normalizeCurrency(String value) {
        String normalized = normalize(value);
        return normalized != null ? normalized.toUpperCase() : null;
    }

    private java.math.BigDecimal normalizePrice(java.math.BigDecimal value) {
        if (value == null) return null;
        try {
            return value.compareTo(java.math.BigDecimal.ZERO) < 0 ? null : value;
        } catch (Exception e) {
            return null;
        }
    }

    private java.util.List<String> normalizeFeatures(java.util.List<String> features) {
        if (features == null) return null;
        java.util.List<String> cleaned = new java.util.ArrayList<>();
        for (String f : features) {
            String n = normalize(f);
            if (n != null) cleaned.add(n);
        }
        return cleaned.isEmpty() ? null : cleaned;
    }
}
