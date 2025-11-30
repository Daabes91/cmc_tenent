package com.clinic.modules.saas.service;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.config.PlanTierConfig;
import com.clinic.modules.saas.dto.*;
import com.clinic.modules.saas.exception.*;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.monitoring.BillingAlertService;
import com.clinic.modules.saas.monitoring.BillingMetricsService;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing PayPal subscriptions.
 * Handles subscription creation, verification, and status updates.
 */
@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private static final List<String> DEFAULT_PLAN_FEATURES = List.of(
            "Unlimited doctors and staff accounts",
            "Priority chat & email support",
            "Custom domains and branding",
            "Audit-ready activity logs"
    );

    private final PayPalConfigService payPalConfigService;
    private final RestTemplate restTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    private final BillingAuditLogger auditLogger;
    private final BillingMetricsService metricsService;
    private final BillingAlertService alertService;
    private final PlanTierConfig planTierConfig;
    private final PayPalCircuitBreaker circuitBreaker;

    @Value("${app.marketing-url:${app.base-url:http://localhost:3000}}")
    private String marketingAppUrl;

    @Value("${app.admin-url:http://localhost:3001}")
    private String adminAppUrl;

    public SubscriptionService(
            PayPalConfigService payPalConfigService,
            RestTemplate restTemplate,
            SubscriptionRepository subscriptionRepository,
            TenantRepository tenantRepository,
            BillingAuditLogger auditLogger,
            BillingMetricsService metricsService,
            BillingAlertService alertService,
            PlanTierConfig planTierConfig,
            PayPalCircuitBreaker circuitBreaker) {
        this.payPalConfigService = payPalConfigService;
        this.restTemplate = restTemplate;
        this.subscriptionRepository = subscriptionRepository;
        this.tenantRepository = tenantRepository;
        this.auditLogger = auditLogger;
        this.metricsService = metricsService;
        this.alertService = alertService;
        this.planTierConfig = planTierConfig;
        this.circuitBreaker = circuitBreaker;
    }

    private static final Set<String> ACTIVATION_STATUSES = Set.of(
            "ACTIVE",
            "APPROVAL_PENDING",
            "APPROVED"
    );

    /**
     * Create a PayPal subscription for a tenant.
     *
     * @param tenantId the tenant ID
     * @return PayPal approval URL for the user to approve the subscription
     * @throws PayPalApiException if subscription creation fails
     */
    public String createSubscription(Long tenantId) {
        return createSubscription(tenantId, PlanTier.BASIC, "MONTHLY", null);
    }

    public String createSubscription(Long tenantId, String landingBaseUrlOverride) {
        return createSubscription(tenantId, PlanTier.BASIC, "MONTHLY", landingBaseUrlOverride);
    }

    /**
     * Create a PayPal subscription for a tenant with optional landing base URL override.
     *
     * @param tenantId the tenant ID
     * @param planTier requested plan tier
     * @param billingCycle requested billing cycle (MONTHLY/ANNUAL)
     * @param landingBaseUrlOverride optional base URL provided by the caller
     * @return PayPal approval URL
     */
    public String createSubscription(Long tenantId,
                                     PlanTier planTier,
                                     String billingCycle,
                                     String landingBaseUrlOverride) {
        logger.info("Creating PayPal subscription for tenant: {}", tenantId);
        
        // Check circuit breaker state
        circuitBreaker.checkState();
        
        // Start timer for API call
        Timer.Sample sample = metricsService.startPayPalApiTimer();
        metricsService.recordPayPalApiCall("CREATE_SUBSCRIPTION");

        try {
            String accessToken = payPalConfigService.getAccessToken();
            String baseUrl = payPalConfigService.getBaseUrl();
            PlanTier targetTier = planTier != null ? planTier : PlanTier.BASIC;
            String cycle = (billingCycle == null || billingCycle.isBlank()) ? "MONTHLY" : billingCycle.toUpperCase();
            if (!planTierConfig.tierExists(targetTier)) {
                throw new InvalidPlanTierException(targetTier.name(), PlanTier.values());
            }
            String planId = payPalConfigService.getPlanIdForTier(targetTier, cycle);
            if (planId == null || planId.isBlank()) {
                planId = planTierConfig.getPayPalPlanId(targetTier, cycle);
            }
            
            // Validate configuration
            if (accessToken == null || accessToken.isBlank()) {
                throw new PayPalConfigurationException("PayPal access token is not available");
            }
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new PayPalConfigurationException("PayPal base URL is not configured");
            }
            if (planId == null || planId.isBlank()) {
                throw new PayPalConfigurationException("PayPal plan ID is not configured for tier " + targetTier.name());
            }

            String url = baseUrl + "/v1/billing/subscriptions";

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("plan_id", planId);

            // Add custom_id to track tenant
            requestBody.put("custom_id", "tenant_" + tenantId);

            // Add application context with return and cancel URLs
            String landingBaseUrlSource = resolveBaseUrlOverride(landingBaseUrlOverride, marketingAppUrl);
            String landingBaseUrl = normalizeBaseUrl(landingBaseUrlSource);
            Map<String, Object> applicationContext = new HashMap<>();
            applicationContext.put("brand_name", "Clinic SaaS Platform");
            applicationContext.put("locale", "en-US");
            applicationContext.put("shipping_preference", "NO_SHIPPING");
            applicationContext.put("user_action", "SUBSCRIBE_NOW");
            applicationContext.put("return_url", landingBaseUrl + "/payment-confirmation");
            applicationContext.put("cancel_url", landingBaseUrl + "/signup?canceled=true");
            requestBody.put("application_context", applicationContext);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Prefer", "return=representation");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make API call
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                String subscriptionId = (String) responseBody.get("id");

                // Extract approval URL from links
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, String>> links = 
                    (java.util.List<Map<String, String>>) responseBody.get("links");

                if (links != null) {
                    for (Map<String, String> link : links) {
                        if ("approve".equals(link.get("rel"))) {
                            String approvalUrl = link.get("href");
                            logger.info("PayPal subscription created successfully for tenant: {}", tenantId);
                            auditLogger.logPayPalApiCall("CREATE_SUBSCRIPTION", tenantId, subscriptionId, 201, true);
                            auditLogger.logSubscriptionEvent("CREATED", subscriptionId, tenantId, 
                                "Subscription created, awaiting approval");
                            
                            // Record metrics
                            metricsService.recordPayPalApiSuccess("CREATE_SUBSCRIPTION", 201);
                            metricsService.stopPayPalApiTimer(sample, "CREATE_SUBSCRIPTION");
                            metricsService.recordSubscriptionCreated();
                            
                            // Record success in circuit breaker
                            circuitBreaker.recordSuccess();
                            
                            return approvalUrl;
                        }
                    }
                }

                logger.error("Approval URL not found in PayPal response for tenant: {}", tenantId);
                auditLogger.logPayPalApiError("CREATE_SUBSCRIPTION", tenantId, subscriptionId, 201, 
                    "Approval URL not found in response", responseBody.toString());
                metricsService.recordPayPalApiError("CREATE_SUBSCRIPTION", 201);
                metricsService.recordSubscriptionCreationFailure("approval_url_not_found");
                alertService.alertSubscriptionCreationFailure(tenantId, "Approval URL not found in response");
                circuitBreaker.recordFailure();
                throw new PayPalApiException("subscription creation", "Approval URL not found in response");
            }

            logger.error("Failed to create PayPal subscription for tenant: {}. Status: {}", 
                tenantId, response.getStatusCode());
            auditLogger.logPayPalApiCall("CREATE_SUBSCRIPTION", tenantId, null, 
                response.getStatusCode().value(), false);
            metricsService.recordPayPalApiError("CREATE_SUBSCRIPTION", response.getStatusCode().value());
            metricsService.recordSubscriptionCreationFailure("unexpected_status");
            alertService.alertSubscriptionCreationFailure(tenantId, "Unexpected response status: " + response.getStatusCode());
            circuitBreaker.recordFailure();
            throw new PayPalApiException("subscription creation", 
                response.getStatusCode().value(), "Unexpected response status");

        } catch (HttpClientErrorException e) {
            logger.error("PayPal API client error during subscription creation for tenant: {}. Status: {}, Body: {}", 
                tenantId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            auditLogger.logPayPalApiError("CREATE_SUBSCRIPTION", tenantId, null, 
                e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
            metricsService.recordPayPalApiError("CREATE_SUBSCRIPTION", e.getStatusCode().value());
            metricsService.recordSubscriptionCreationFailure("api_client_error");
            alertService.alertSubscriptionCreationFailure(tenantId, "PayPal API client error: " + e.getMessage());
            circuitBreaker.recordFailure();
            throw new PayPalApiException("subscription creation", e.getStatusCode().value(), 
                "PayPal API client error: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            logger.error("PayPal API server error during subscription creation for tenant: {}. Status: {}, Body: {}", 
                tenantId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            auditLogger.logPayPalApiError("CREATE_SUBSCRIPTION", tenantId, null, 
                e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
            metricsService.recordPayPalApiError("CREATE_SUBSCRIPTION", e.getStatusCode().value());
            metricsService.recordSubscriptionCreationFailure("api_server_error");
            alertService.alertSubscriptionCreationFailure(tenantId, "PayPal API server error: " + e.getMessage());
            circuitBreaker.recordFailure();
            throw new PayPalApiException("subscription creation", e.getStatusCode().value(), 
                "PayPal API server error: " + e.getMessage());
        } catch (PayPalApiException | PayPalConfigurationException e) {
            // Re-throw PayPalApiException and PayPalConfigurationException
            circuitBreaker.recordFailure();
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during PayPal subscription creation for tenant: {}", tenantId, e);
            auditLogger.logFailure("CREATE_SUBSCRIPTION", tenantId, null, 
                "Unexpected error during subscription creation", e);
            metricsService.recordSubscriptionCreationFailure("unexpected_error");
            alertService.alertSubscriptionCreationFailure(tenantId, "Unexpected error: " + e.getMessage());
            circuitBreaker.recordFailure();
            throw new PayPalApiException("subscription creation", "Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Verify a PayPal subscription status.
     *
     * @param subscriptionId the PayPal subscription ID
     * @return Map containing subscription details including status
     * @throws PayPalApiException if verification fails
     */
    public Map<String, Object> verifySubscription(String subscriptionId) {
        logger.info("Verifying PayPal subscription: {}", subscriptionId);
        
        // Start timer for API call
        Timer.Sample sample = metricsService.startPayPalApiTimer();
        metricsService.recordPayPalApiCall("VERIFY_SUBSCRIPTION");

        try {
            String accessToken = payPalConfigService.getAccessToken();
            String baseUrl = payPalConfigService.getBaseUrl();

            String url = baseUrl + "/v1/billing/subscriptions/" + subscriptionId;

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            // Make API call
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subscriptionData = (Map<String, Object>) response.getBody();
                String status = (String) subscriptionData.get("status");
                
                logger.info("PayPal subscription verified: {}, status: {}", subscriptionId, status);
                auditLogger.logPayPalApiCall("VERIFY_SUBSCRIPTION", null, subscriptionId, 200, true);
                
                // Record metrics
                metricsService.recordPayPalApiSuccess("VERIFY_SUBSCRIPTION", 200);
                metricsService.stopPayPalApiTimer(sample, "VERIFY_SUBSCRIPTION");
                
                return subscriptionData;
            }

            logger.error("Failed to verify PayPal subscription: {}. Status: {}", 
                subscriptionId, response.getStatusCode());
            auditLogger.logPayPalApiCall("VERIFY_SUBSCRIPTION", null, subscriptionId, 
                response.getStatusCode().value(), false);
            metricsService.recordPayPalApiError("VERIFY_SUBSCRIPTION", response.getStatusCode().value());
            throw new PayPalApiException("subscription verification", 
                response.getStatusCode().value(), "Unexpected response status");

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.error("PayPal subscription not found: {}", subscriptionId);
                auditLogger.logPayPalApiError("VERIFY_SUBSCRIPTION", null, subscriptionId, 404, 
                    "Subscription not found", e.getResponseBodyAsString());
                metricsService.recordPayPalApiError("VERIFY_SUBSCRIPTION", 404);
                throw new SubscriptionNotFoundException(subscriptionId);
            }
            logger.error("PayPal API client error during subscription verification: {}. Status: {}, Body: {}", 
                subscriptionId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            auditLogger.logPayPalApiError("VERIFY_SUBSCRIPTION", null, subscriptionId, 
                e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
            metricsService.recordPayPalApiError("VERIFY_SUBSCRIPTION", e.getStatusCode().value());
            throw new PayPalApiException("subscription verification", e.getStatusCode().value(), 
                "PayPal API client error: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            logger.error("PayPal API server error during subscription verification: {}. Status: {}, Body: {}", 
                subscriptionId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            auditLogger.logPayPalApiError("VERIFY_SUBSCRIPTION", null, subscriptionId, 
                e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
            metricsService.recordPayPalApiError("VERIFY_SUBSCRIPTION", e.getStatusCode().value());
            throw new PayPalApiException("subscription verification", e.getStatusCode().value(), 
                "PayPal API server error: " + e.getMessage());
        } catch (PayPalApiException | SubscriptionNotFoundException e) {
            // Re-throw custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during PayPal subscription verification: {}", subscriptionId, e);
            auditLogger.logFailure("VERIFY_SUBSCRIPTION", null, null, 
                "Unexpected error verifying subscription: " + subscriptionId, e);
            throw new PayPalApiException("subscription verification", "Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Activate a tenant after successful payment confirmation.
     * Creates subscription record and updates tenant billing status.
     *
     * @param subscriptionId the PayPal subscription ID
     * @throws PayPalApiException if verification fails
     * @throws InvalidSubscriptionStatusException if subscription is not active
     * @throws SubscriptionNotFoundException if subscription not found
     */
    @Transactional
    public void activateTenant(String subscriptionId) {
        logger.info("Activating tenant for subscription: {}", subscriptionId);

        try {
            // Verify subscription with PayPal
            Map<String, Object> subscriptionData = verifySubscription(subscriptionId);
            
            String status = (String) subscriptionData.get("status");
            String normalizedStatus = status != null ? status.toUpperCase() : "";
            String customId = (String) subscriptionData.get("custom_id");

            // Validate subscription is active
            // PayPal may briefly report APPROVAL_PENDING/APPROVED right after checkout,
            // so treat these statuses as valid confirmations as well.
            if (!ACTIVATION_STATUSES.contains(normalizedStatus)) {
                logger.error("Cannot activate tenant for subscription: {}. Status is: {}", subscriptionId, status);
                throw new InvalidSubscriptionStatusException(
                        subscriptionId,
                        status,
                        "ACTIVE, APPROVAL_PENDING, or APPROVED"
                );
            }

            // Extract tenant ID from custom_id (format: "tenant_123")
            if (customId == null || !customId.startsWith("tenant_")) {
                logger.error("Invalid custom_id in subscription: {}. Value: {}", subscriptionId, customId);
                throw new PayPalApiException("tenant activation", "Invalid custom_id in subscription: " + customId);
            }
            
            Long tenantId;
            try {
                tenantId = Long.parseLong(customId.substring(7));
            } catch (NumberFormatException e) {
                logger.error("Failed to parse tenant ID from custom_id: {} for subscription: {}", customId, subscriptionId, e);
                throw new PayPalApiException("tenant activation", "Invalid tenant ID in custom_id: " + customId);
            }

            // If we already stored this PayPal subscription, treat as idempotent
            if (subscriptionRepository.findByPaypalSubscriptionId(subscriptionId).isPresent()) {
                logger.warn("Subscription already exists for PayPal subscription ID: {}", subscriptionId);
                return;
            }

            // Get tenant
            TenantEntity tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> {
                        logger.error("Tenant not found: {} for subscription: {}", tenantId, subscriptionId);
                        return new SubscriptionNotFoundException(subscriptionId);
                    });

            // Create or update subscription record for this tenant
            SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                    .orElseGet(SubscriptionEntity::new);

            subscription.setTenant(tenant);
            subscription.setPaypalSubscriptionId(subscriptionId);
            subscription.setStatus(normalizedStatus);
            subscription.setProvider("paypal");
            subscription.setPlanTier(resolvePlanTier((String) subscriptionData.get("plan_id")));

            // Parse billing info if available
            @SuppressWarnings("unchecked")
            Map<String, Object> billingInfo = (Map<String, Object>) subscriptionData.get("billing_info");
            if (billingInfo != null) {
                String nextBillingTime = (String) billingInfo.get("next_billing_time");
                @SuppressWarnings("unchecked")
                Map<String, Object> lastPayment = (Map<String, Object>) billingInfo.get("last_payment");

                if (nextBillingTime != null) {
                    LocalDateTime nextBilling = parsePayPalDateTime(nextBillingTime);
                    subscription.setCurrentPeriodEnd(nextBilling);
                    subscription.setRenewalDate(nextBilling);
                }
                
                if (lastPayment != null) {
                    String time = (String) lastPayment.get("time");
                    if (time != null) {
                        subscription.setCurrentPeriodStart(parsePayPalDateTime(time));
                    }
                }
            }

            subscriptionRepository.save(subscription);
            logger.info("Created subscription record for tenant: {}", tenantId);
            auditLogger.logSubscriptionEvent("ACTIVATED", subscriptionId, tenantId, 
                "Subscription record created in database");

            // Update tenant billing status to active
            String oldStatus = tenant.getBillingStatus().name();
            tenant.setBillingStatus(BillingStatus.ACTIVE);
            tenantRepository.save(tenant);
            logger.info("Updated tenant billing status to ACTIVE for tenant: {}", tenantId);
            auditLogger.logBillingStatusChange(tenantId, oldStatus, "ACTIVE", 
                "Payment confirmed via subscription: " + subscriptionId);
            auditLogger.logSuccess("TENANT_ACTIVATED", tenantId, null, 
                "Tenant activated successfully after payment confirmation");
            
            // Record metrics
            metricsService.recordBillingStatusChange(oldStatus, "ACTIVE");

        } catch (PayPalApiException | InvalidSubscriptionStatusException | SubscriptionNotFoundException e) {
            auditLogger.logFailure("TENANT_ACTIVATION", null, null, 
                "Failed to activate tenant for subscription: " + subscriptionId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during tenant activation for subscription: {}", subscriptionId, e);
            auditLogger.logFailure("TENANT_ACTIVATION", null, null, 
                "Unexpected error activating tenant for subscription: " + subscriptionId, e);
            throw new PayPalApiException("tenant activation", "Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Parse PayPal datetime string to LocalDateTime.
     *
     * @param dateTimeStr PayPal datetime string (ISO 8601 format)
     * @return LocalDateTime
     */
    private LocalDateTime parsePayPalDateTime(String dateTimeStr) {
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
            return zonedDateTime.toLocalDateTime();
        } catch (Exception e) {
            logger.warn("Failed to parse PayPal datetime: {}", dateTimeStr, e);
            return LocalDateTime.now();
        }
    }

    /**
     * Retrieve the current plan information for a tenant by consulting PayPal.
     */
    @Transactional(readOnly = true)
    public TenantPlanResponse getTenantPlan(Long tenantId) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant with ID " + tenantId + " not found"));

        SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for tenant ID " + tenantId));

        Map<String, Object> subscriptionData = verifySubscription(subscription.getPaypalSubscriptionId());
        return buildPlanResponse(tenant, subscription, subscriptionData);
    }

    /**
     * Cancel an active PayPal subscription for the provided tenant.
     */
    @Transactional
    public void cancelTenantPlan(Long tenantId, String reason) {
        SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for tenant ID " + tenantId));

        String normalizedReason = (reason == null || reason.isBlank())
                ? "Cancellation requested by clinic admin"
                : reason.trim();

        callPayPalCancelSubscription(subscription.getPaypalSubscriptionId(), normalizedReason);

        subscription.setStatus("CANCELLED");
        subscriptionRepository.save(subscription);

        TenantEntity tenant = subscription.getTenant();
        BillingStatus previous = tenant.getBillingStatus();
        tenant.setBillingStatus(BillingStatus.CANCELED);
        tenantRepository.save(tenant);

        auditLogger.logBillingStatusChange(
                tenant.getId(),
                previous.name(),
                "CANCELED",
                "Tenant cancelled plan via admin portal"
        );
    }

    /**
     * Normalize a base URL by removing trailing slashes for consistent concatenation.
     */
    private String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Resolve a landing base URL override, falling back to the configured marketing URL when invalid.
     */
    private String resolveBaseUrlOverride(String override, String fallback) {
        if (override == null || override.isBlank()) {
            return fallback;
        }
        String trimmed = override.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        logger.warn("Ignoring invalid client base URL override: {}", override);
        return fallback;
    }

    private PlanTier resolvePlanTier(String paypalPlanId) {
        PlanTier tier = planTierConfig.findTierByPayPalPlanId(paypalPlanId);
        if (tier != null) {
            return tier;
        }
        // Default to BASIC tier for backwards compatibility / legacy plans
        return PlanTier.BASIC;
    }

    private TenantPlanResponse buildPlanResponse(TenantEntity tenant,
                                                 SubscriptionEntity subscription,
                                                 Map<String, Object> subscriptionData) {
        String planName = extractPlanName(subscriptionData);
        String status = Optional.ofNullable(subscriptionData.get("status"))
                .map(Object::toString)
                .orElse(subscription.getStatus());

        LocalDateTime renewalDate = Optional.ofNullable(getNextBillingTime(subscriptionData))
                .map(this::parsePayPalDateTime)
                .orElse(subscription.getRenewalDate() != null ? subscription.getRenewalDate() : subscription.getCurrentPeriodEnd());

        Double amount = extractLastPaymentAmount(subscriptionData);
        String currency = extractLastPaymentCurrency(subscriptionData);

        String billingCycle = determineBillingCycle(subscriptionData);

        Map<String, String> paymentDetails = extractPaymentDetails(subscriptionData);
        
        // Build payment method mask
        String paymentMethodMask = buildPaymentMethodMask(paymentDetails);

        // Get plan tier from subscription or default to BASIC
        PlanTier planTier = subscription.getPlanTier() != null ? subscription.getPlanTier() : PlanTier.BASIC;
        
        // Get features for the plan tier
        List<String> features = planTierConfig.getFeatures(planTier);
        if (features.isEmpty()) {
            features = DEFAULT_PLAN_FEATURES;
        }

        return new TenantPlanResponse(
                tenant.getId(),
                planTier,
                planName,
                amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO,
                currency != null ? currency : "USD",
                billingCycle,
                renewalDate,
                paymentMethodMask,
                paymentDetails.get("brand"),
                status,
                subscription.getCancellationDate(),
                subscription.getCancellationEffectiveDate(),
                subscription.getPendingPlanTier(),
                subscription.getPendingPlanEffectiveDate(),
                features,
                subscription.getPaypalSubscriptionId()
        );
    }
    
    private String buildPaymentMethodMask(Map<String, String> paymentDetails) {
        String brand = paymentDetails.get("brand");
        String last4 = paymentDetails.get("last4");
        
        if (brand != null && last4 != null) {
            if ("PayPal".equals(brand)) {
                return "PayPal (" + last4 + ")";
            }
            return brand + " ****" + last4;
        }
        
        return "Not available";
    }

    private String extractPlanName(Map<String, Object> subscriptionData) {
        Object planOverviewObject = subscriptionData.get("plan_overview");
        if (planOverviewObject instanceof Map<?, ?> planOverview) {
            Object name = planOverview.get("plan_name");
            if (name == null) {
                name = planOverview.get("name");
            }
            if (name != null) {
                return name.toString();
            }
        }
        return "Clinic SaaS Plan";
    }

    private String determineBillingCycle(Map<String, Object> subscriptionData) {
        Object billingInfo = subscriptionData.get("billing_info");
        if (billingInfo instanceof Map<?, ?> info) {
            Object cycleExecutions = info.get("cycle_executions");
            if (cycleExecutions instanceof List<?> executions && !executions.isEmpty()) {
                Object first = executions.get(0);
                if (first instanceof Map<?, ?> cycle) {
                    Object tenure = cycle.get("tenure_type");
                    if (tenure != null) {
                        return tenure.toString().toLowerCase();
                    }
                }
            }
        }
        return "monthly";
    }

    private String getNextBillingTime(Map<String, Object> subscriptionData) {
        Object billingInfo = subscriptionData.get("billing_info");
        if (billingInfo instanceof Map<?, ?> info) {
            Object next = info.get("next_billing_time");
            if (next != null) {
                return next.toString();
            }
        }
        return null;
    }

    private Double extractLastPaymentAmount(Map<String, Object> subscriptionData) {
        Map<String, Object> amount = extractLastPaymentAmountNode(subscriptionData);
        if (amount == null) {
            return null;
        }
        Object value = amount.get("value");
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            logger.debug("Unable to parse payment amount: {}", value, e);
            return null;
        }
    }

    private String extractLastPaymentCurrency(Map<String, Object> subscriptionData) {
        Map<String, Object> amount = extractLastPaymentAmountNode(subscriptionData);
        if (amount == null) {
            return null;
        }
        Object currency = amount.get("currency_code");
        return currency != null ? currency.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractLastPaymentAmountNode(Map<String, Object> subscriptionData) {
        Object billingInfo = subscriptionData.get("billing_info");
        if (billingInfo instanceof Map<?, ?> info) {
            Object lastPaymentObj = info.get("last_payment");
            if (lastPaymentObj instanceof Map<?, ?> lastPayment) {
                Object amountObj = lastPayment.get("amount");
                if (amountObj instanceof Map<?, ?>) {
                    return (Map<String, Object>) amountObj;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractPaymentDetails(Map<String, Object> subscriptionData) {
        Map<String, String> result = new HashMap<>();
        result.put("brand", null);
        result.put("last4", null);
        result.put("expiry", null);

        Object paymentSourceObj = subscriptionData.get("payment_source");
        if (!(paymentSourceObj instanceof Map<?, ?> paymentSource)) {
            return result;
        }

        Object cardObj = paymentSource.get("card");
        if (cardObj instanceof Map<?, ?> card) {
            Object brand = card.get("brand");
            if (brand == null) {
                brand = card.get("name");
            }
            Object lastDigits = card.get("last_digits");
            if (lastDigits == null) {
                lastDigits = card.get("last4");
            }
            Object expiry = card.get("expiry");
            if (brand != null) {
                result.put("brand", brand.toString());
            }
            if (lastDigits != null) {
                result.put("last4", lastDigits.toString());
            }
            if (expiry != null) {
                result.put("expiry", expiry.toString());
            }
            return result;
        }

    Object paypalObj = paymentSource.get("paypal");
        if (paypalObj instanceof Map<?, ?> paypal) {
            Object email = paypal.get("email_address");
            result.put("brand", "PayPal");
            if (email != null) {
                result.put("last4", email.toString());
            }
        }

        return result;
    }

    private void callPayPalCancelSubscription(String subscriptionId, String reason) {
        logger.info("Cancelling PayPal subscription: {}", subscriptionId);
        try {
            String accessToken = payPalConfigService.getAccessToken();
            String baseUrl = payPalConfigService.getBaseUrl();

            String url = baseUrl + "/v1/billing/subscriptions/" + subscriptionId + "/cancel";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, String> body = Map.of("reason", reason);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, request, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new PayPalApiException("subscription cancellation",
                        response.getStatusCode().value(),
                        "Unexpected response when cancelling subscription");
            }

            auditLogger.logSubscriptionEvent("CANCELLED", subscriptionId, null,
                    "Subscription cancelled via admin portal");
        } catch (HttpClientErrorException e) {
            auditLogger.logPayPalApiError("CANCEL_SUBSCRIPTION", null, subscriptionId,
                    e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
            throw new PayPalApiException("subscription cancellation", e.getStatusCode().value(),
                    "PayPal API client error: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            auditLogger.logPayPalApiError("CANCEL_SUBSCRIPTION", null, subscriptionId,
                    e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
            throw new PayPalApiException("subscription cancellation", e.getStatusCode().value(),
                    "PayPal API server error: " + e.getMessage());
        } catch (Exception e) {
            auditLogger.logFailure("CANCEL_SUBSCRIPTION", null, null,
                    "Unexpected error cancelling subscription: " + subscriptionId, e);
            throw new PayPalApiException("subscription cancellation", "Unexpected error: " + e.getMessage(), e);
        }
    }

    private void callPayPalSuspendSubscription(String subscriptionId, String reason) {
        logger.info("Suspending PayPal subscription: {}", subscriptionId);
        try {
            String accessToken = payPalConfigService.getAccessToken();
            String baseUrl = payPalConfigService.getBaseUrl();

            String url = baseUrl + "/v1/billing/subscriptions/" + subscriptionId + "/suspend";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, String> body = Map.of("reason", reason);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, request, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new PayPalApiException("subscription cancellation",
                        response.getStatusCode().value(),
                        "Unexpected response when cancelling subscription");
            }

            auditLogger.logSubscriptionEvent("SUSPENDED", subscriptionId, null,
                    "Subscription suspended via admin portal");
        } catch (HttpClientErrorException e) {
            auditLogger.logPayPalApiError("SUSPEND_SUBSCRIPTION", null, subscriptionId,
                    e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
            throw new PayPalApiException("subscription suspension", e.getStatusCode().value(),
                    "PayPal API client error: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            auditLogger.logPayPalApiError("SUSPEND_SUBSCRIPTION", null, subscriptionId,
                    e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
            throw new PayPalApiException("subscription suspension", e.getStatusCode().value(),
                    "PayPal API server error: " + e.getMessage());
        } catch (Exception e) {
            auditLogger.logFailure("SUSPEND_SUBSCRIPTION", null, null,
                    "Unexpected error suspending subscription: " + subscriptionId, e);
            throw new PayPalApiException("subscription suspension", "Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed plan information for a tenant.
     * Fetches current subscription with plan tier, pricing, renewal date, and payment method.
     * Results are cached for 5 minutes to improve performance.
     *
     * @param tenantId the tenant ID
     * @return TenantPlanResponse with complete plan details
     * @throws NotFoundException if tenant or subscription not found
     */
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "tenantPlan", key = "#tenantId", cacheManager = "billingSaasCacheManager")
    public TenantPlanResponse getPlanDetails(Long tenantId) {
        logger.info("Fetching plan details for tenant: {}", tenantId);
        
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant with ID " + tenantId + " not found"));

        SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for tenant ID " + tenantId));

        // Verify subscription with PayPal to get latest data
        Map<String, Object> subscriptionData = verifySubscription(subscription.getPaypalSubscriptionId());
        
        TenantPlanResponse response = buildPlanResponse(tenant, subscription, subscriptionData);
        
        logger.info("Successfully fetched plan details for tenant: {}", tenantId);
        return response;
    }

    /**
     * Upgrade a tenant's subscription plan to a higher tier.
     * Validates tier, generates PayPal upgrade link, and returns approval URL.
     * Evicts the tenant plan cache to ensure fresh data is fetched.
     *
     * @param tenantId the tenant ID
     * @param targetTier the target plan tier
     * @param billingCycle the billing cycle (MONTHLY or ANNUAL), defaults to current if null
     * @return PlanChangeResponse with PayPal approval URL
     * @throws NotFoundException if tenant or subscription not found
     * @throws IllegalArgumentException if target tier is invalid
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "tenantPlan", key = "#tenantId", cacheManager = "billingSaasCacheManager")
    public PlanChangeResponse upgradePlan(Long tenantId, PlanTier targetTier, String billingCycle) {
        logger.info("Upgrading plan for tenant: {} to tier: {}", tenantId, targetTier);
        
        // Validate input parameters
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (targetTier == null) {
            throw new IllegalArgumentException("Target tier cannot be null");
        }
        
        // Validate tenant and subscription exist
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant with ID " + tenantId + " not found"));

        SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for tenant ID " + tenantId));

        // Validate target tier exists
        if (!planTierConfig.tierExists(targetTier)) {
            throw new InvalidPlanTierException(targetTier.name(), PlanTier.values());
        }

        // Validate subscription is active
        if (!"ACTIVE".equals(subscription.getStatus())) {
            throw new InvalidSubscriptionStatusException(
                subscription.getPaypalSubscriptionId(), 
                subscription.getStatus(), 
                "ACTIVE"
            );
        }
        
        // Check for pending plan changes
        if (subscription.getPendingPlanTier() != null) {
            throw new PlanChangeConflictException(
                tenantId,
                "Pending change to " + subscription.getPendingPlanTier() + 
                " scheduled for " + subscription.getPendingPlanEffectiveDate()
            );
        }
        
        // Check for pending cancellation
        if (subscription.getCancellationEffectiveDate() != null) {
            throw new PlanChangeConflictException(
                tenantId,
                "Subscription is scheduled for cancellation on " + subscription.getCancellationEffectiveDate()
            );
        }
        
        // Validate this is actually an upgrade (not same tier or downgrade)
        PlanTier currentTier = subscription.getPlanTier() != null ? subscription.getPlanTier() : PlanTier.BASIC;
        if (currentTier == targetTier) {
            throw new IllegalArgumentException("Already subscribed to " + targetTier + " plan");
        }

        // Use current billing cycle if not specified
        String cycle = billingCycle != null ? billingCycle : "MONTHLY";
        
        // Get PayPal plan ID for target tier
        String paypalPlanId = planTierConfig.getPayPalPlanId(targetTier, cycle);
        if (paypalPlanId == null) {
            throw new IllegalArgumentException("PayPal plan ID not configured for tier: " + targetTier);
        }

        // Store old tier for audit logging
        String oldTier = subscription.getPlanTier() != null ? subscription.getPlanTier().name() : "BASIC";

        // Call PayPal API to revise subscription
        String approvalUrl = callPayPalReviseSubscription(
            subscription.getPaypalSubscriptionId(), 
            paypalPlanId,
            tenantId
        );

        // Get pricing for the new tier
        BigDecimal newPrice = planTierConfig.getPrice(targetTier, "USD", cycle);
        
        // Update subscription with pending plan change
        subscription.setPendingPlanTier(targetTier);
        subscription.setPendingPlanEffectiveDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        // Log the upgrade request with detailed audit trail
        auditLogger.logPlanUpgrade(tenantId, null, oldTier, targetTier.name(), LocalDateTime.now());
        auditLogger.logSubscriptionEvent("UPGRADE_INITIATED", subscription.getPaypalSubscriptionId(), tenantId,
                "Plan upgrade initiated to tier: " + targetTier);

        logger.info("Successfully initiated plan upgrade for tenant: {}", tenantId);
        
        return new PlanChangeResponse(
                approvalUrl,
                targetTier,
                LocalDateTime.now(),
                newPrice,
                "USD",
                "Plan upgrade initiated. Please complete the approval process with PayPal."
        );
    }

    /**
     * Downgrade a tenant's subscription plan to a lower tier.
     * Schedules tier change for next billing cycle.
     * Evicts the tenant plan cache to ensure fresh data is fetched.
     *
     * @param tenantId the tenant ID
     * @param targetTier the target plan tier
     * @param billingCycle the billing cycle (MONTHLY or ANNUAL), defaults to current if null
     * @return PlanChangeResponse with effective date
     * @throws NotFoundException if tenant or subscription not found
     * @throws IllegalArgumentException if target tier is invalid
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "tenantPlan", key = "#tenantId", cacheManager = "billingSaasCacheManager")
    public PlanChangeResponse downgradePlan(Long tenantId, PlanTier targetTier, String billingCycle) {
        logger.info("Downgrading plan for tenant: {} to tier: {}", tenantId, targetTier);
        
        // Validate input parameters
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (targetTier == null) {
            throw new IllegalArgumentException("Target tier cannot be null");
        }
        
        // Validate tenant and subscription exist
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant with ID " + tenantId + " not found"));

        SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for tenant ID " + tenantId));

        // Validate target tier exists
        if (!planTierConfig.tierExists(targetTier)) {
            throw new InvalidPlanTierException(targetTier.name(), PlanTier.values());
        }

        // Validate subscription is active
        if (!"ACTIVE".equals(subscription.getStatus())) {
            throw new InvalidSubscriptionStatusException(
                subscription.getPaypalSubscriptionId(), 
                subscription.getStatus(), 
                "ACTIVE"
            );
        }
        
        // Check for pending plan changes
        if (subscription.getPendingPlanTier() != null) {
            throw new PlanChangeConflictException(
                tenantId,
                "Pending change to " + subscription.getPendingPlanTier() + 
                " scheduled for " + subscription.getPendingPlanEffectiveDate()
            );
        }
        
        // Check for pending cancellation
        if (subscription.getCancellationEffectiveDate() != null) {
            throw new PlanChangeConflictException(
                tenantId,
                "Subscription is scheduled for cancellation on " + subscription.getCancellationEffectiveDate()
            );
        }
        
        // Validate this is actually a downgrade (not same tier or upgrade)
        PlanTier currentTier = subscription.getPlanTier() != null ? subscription.getPlanTier() : PlanTier.BASIC;
        if (currentTier == targetTier) {
            throw new IllegalArgumentException("Already subscribed to " + targetTier + " plan");
        }

        // Use current billing cycle if not specified
        String cycle = billingCycle != null ? billingCycle : "MONTHLY";
        
        // Get PayPal plan ID for target tier
        String paypalPlanId = planTierConfig.getPayPalPlanId(targetTier, cycle);
        if (paypalPlanId == null) {
            throw new IllegalArgumentException("PayPal plan ID not configured for tier: " + targetTier);
        }

        // Store old tier for audit logging
        String oldTier = subscription.getPlanTier() != null ? subscription.getPlanTier().name() : "BASIC";

        // Schedule downgrade for next billing cycle
        LocalDateTime effectiveDate = subscription.getRenewalDate() != null 
            ? subscription.getRenewalDate() 
            : subscription.getCurrentPeriodEnd();
        
        subscription.setPendingPlanTier(targetTier);
        subscription.setPendingPlanEffectiveDate(effectiveDate);
        subscriptionRepository.save(subscription);

        // Get pricing for the new tier
        BigDecimal newPrice = planTierConfig.getPrice(targetTier, "USD", cycle);

        // Log the downgrade request with detailed audit trail
        auditLogger.logPlanDowngrade(tenantId, null, oldTier, targetTier.name(), effectiveDate);
        auditLogger.logSubscriptionEvent("DOWNGRADE_SCHEDULED", subscription.getPaypalSubscriptionId(), tenantId,
                "Plan downgrade scheduled to tier: " + targetTier + " effective: " + effectiveDate);

        logger.info("Successfully scheduled plan downgrade for tenant: {} effective: {}", tenantId, effectiveDate);
        
        return new PlanChangeResponse(
                null, // No approval URL needed for downgrades
                targetTier,
                effectiveDate,
                newPrice,
                "USD",
                "Plan downgrade scheduled for " + effectiveDate + ". Your current plan will remain active until then."
        );
    }

    /**
     * Cancel a tenant's subscription.
     * Calls PayPal cancel API and updates subscription entity.
     * Evicts the tenant plan cache to ensure fresh data is fetched.
     *
     * @param tenantId the tenant ID
     * @param immediate whether to cancel immediately or at end of billing period
     * @param reason the cancellation reason
     * @return CancellationResponse with effective date
     * @throws NotFoundException if tenant or subscription not found
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "tenantPlan", key = "#tenantId", cacheManager = "billingSaasCacheManager")
    public CancellationResponse cancelSubscription(Long tenantId, boolean immediate, String reason) {
        logger.info("Cancelling subscription for tenant: {}, immediate: {}", tenantId, immediate);
        
        // Validate input parameters
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant with ID " + tenantId + " not found"));

        SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for tenant ID " + tenantId));

        // Validate subscription is not already cancelled/suspended
        if ("CANCELLED".equals(subscription.getStatus()) || "SUSPENDED".equals(subscription.getStatus())) {
            throw new IllegalStateException("Subscription is already cancelled or suspended");
        }
        
        // Check if cancellation is already scheduled
        if (subscription.getCancellationEffectiveDate() != null) {
            throw new IllegalStateException(
                "Subscription cancellation is already scheduled for " + 
                subscription.getCancellationEffectiveDate()
            );
        }
        
        // Validate subscription is in a cancellable state
        if (!"ACTIVE".equals(subscription.getStatus()) && !"PAST_DUE".equals(subscription.getStatus())) {
            throw new InvalidSubscriptionStatusException(
                subscription.getPaypalSubscriptionId(),
                subscription.getStatus(),
                "ACTIVE or PAST_DUE"
            );
        }

        String normalizedReason = (reason == null || reason.isBlank())
                ? "Cancellation requested by tenant administrator"
                : reason.trim();

        // Call PayPal to suspend subscription (reactivable)
        callPayPalSuspendSubscription(subscription.getPaypalSubscriptionId(), normalizedReason);

        LocalDateTime cancellationDate = LocalDateTime.now();
        LocalDateTime effectiveDate = LocalDateTime.now();

        // Mark as suspended immediately to allow reactivation later
        subscription.setStatus("SUSPENDED");
        tenant.setBillingStatus(BillingStatus.SUSPENDED);

        subscription.setCancellationDate(cancellationDate);
        subscription.setCancellationEffectiveDate(effectiveDate);
        subscriptionRepository.save(subscription);
        tenantRepository.save(tenant);

        // Log the cancellation with detailed audit trail
        auditLogger.logCancellation(tenantId, null, cancellationDate, effectiveDate, normalizedReason);
        auditLogger.logBillingStatusChange(
                tenantId,
                tenant.getBillingStatus().name(),
                immediate ? "CANCELED" : "PENDING_CANCELLATION",
                "Subscription cancelled: " + normalizedReason
        );

        logger.info("Successfully cancelled subscription for tenant: {}, effective: {}", tenantId, effectiveDate);
        
        return new CancellationResponse(
                effectiveDate,
                immediate 
                    ? "Subscription suspended immediately" 
                    : "Subscription suspended immediately",
                immediate
        );
    }

    /**
     * Resume a cancelled subscription.
     * Reactivates a subscription that was previously cancelled.
     * Evicts the tenant plan cache to ensure fresh data is fetched.
     *
     * @param tenantId the tenant ID
     * @throws NotFoundException if tenant or subscription not found
     * @throws IllegalStateException if subscription is not cancelled
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "tenantPlan", key = "#tenantId", cacheManager = "billingSaasCacheManager")
    public void resumeSubscription(Long tenantId) {
        logger.info("Resuming subscription for tenant: {}", tenantId);
        
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant with ID " + tenantId + " not found"));

        SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for tenant ID " + tenantId));

        // PayPal only allows reactivation from SUSPENDED; CANCELLED cannot be resumed
        if ("CANCELLED".equals(subscription.getStatus())) {
            throw new InvalidSubscriptionStatusException(
                    subscription.getPaypalSubscriptionId(),
                    subscription.getStatus(),
                    "SUSPENDED"
            );
        }
        // If there is no pending cancellation/suspension flag, disallow resume
        if (!"SUSPENDED".equals(subscription.getStatus()) && subscription.getCancellationDate() == null) {
            throw new InvalidSubscriptionStatusException(
                    subscription.getPaypalSubscriptionId(),
                    subscription.getStatus(),
                    "SUSPENDED"
            );
        }

        // Call PayPal to reactivate subscription
        callPayPalReactivateSubscription(subscription.getPaypalSubscriptionId());

        // Re-sync billing dates from PayPal after activation
        Map<String, Object> subscriptionData = verifySubscription(subscription.getPaypalSubscriptionId());
        String nextBillingRaw = getNextBillingTime(subscriptionData);
        LocalDateTime nextBilling = nextBillingRaw != null ? parsePayPalDateTime(nextBillingRaw) : null;

        // Update subscription status and billing windows
        subscription.setStatus("ACTIVE");
        subscription.setCancellationDate(null);
        subscription.setCancellationEffectiveDate(null);
        if (nextBilling != null) {
            subscription.setCurrentPeriodEnd(nextBilling);
            subscription.setRenewalDate(nextBilling);
        }
        subscriptionRepository.save(subscription);

        // Update tenant billing status
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        tenantRepository.save(tenant);

        // Log the resumption
        auditLogger.logBillingStatusChange(
                tenantId,
                "CANCELED",
                "ACTIVE",
                "Subscription resumed by tenant administrator"
        );

        logger.info("Successfully resumed subscription for tenant: {}", tenantId);
    }

    /**
     * Create a payment method update session.
     * Generates PayPal billing portal URL for updating payment method.
     *
     * @param tenantId the tenant ID
     * @return PaymentMethodUpdateResponse with portal URL
     * @throws NotFoundException if tenant or subscription not found
     */
    @Transactional(readOnly = true)
    public PaymentMethodUpdateResponse createPaymentUpdateSession(Long tenantId) {
        logger.info("Creating payment update session for tenant: {}", tenantId);
        
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant with ID " + tenantId + " not found"));

        SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for tenant ID " + tenantId));

        // Generate PayPal billing portal URL
        String portalUrl = generatePayPalBillingPortalUrl(subscription.getPaypalSubscriptionId());

        logger.info("Successfully created payment update session for tenant: {}", tenantId);
        
        return new PaymentMethodUpdateResponse(
                portalUrl,
                "Please complete the payment method update in the PayPal portal"
        );
    }

    /**
     * Call PayPal API to revise a subscription (for upgrades).
     */
    private String callPayPalReviseSubscription(String subscriptionId, String newPlanId, Long tenantId) {
        logger.info("Revising PayPal subscription: {} to plan: {}", subscriptionId, newPlanId);
        
        try {
            String accessToken = payPalConfigService.getAccessToken();
            String baseUrl = payPalConfigService.getBaseUrl();

            String url = baseUrl + "/v1/billing/subscriptions/" + subscriptionId + "/revise";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("plan_id", newPlanId);
            
            // Add application context for approval
            Map<String, Object> applicationContext = new HashMap<>();
            String adminBaseUrl = normalizeBaseUrl(adminAppUrl);
            applicationContext.put("return_url", adminBaseUrl + "/payment-confirmation");
            applicationContext.put("cancel_url", adminBaseUrl + "/clinic-settings?upgrade_canceled=true");
            requestBody.put("application_context", applicationContext);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                
                // Extract approval URL from links
                @SuppressWarnings("unchecked")
                List<Map<String, String>> links = (List<Map<String, String>>) responseBody.get("links");
                
                if (links != null) {
                    for (Map<String, String> link : links) {
                        if ("approve".equals(link.get("rel"))) {
                            String approvalUrl = link.get("href");
                            auditLogger.logPayPalApiCall("REVISE_SUBSCRIPTION", tenantId, subscriptionId, 200, true);
                            return approvalUrl;
                        }
                    }
                }
                
                throw new PayPalApiException("subscription revision", "Approval URL not found in response");
            }

            throw new PayPalApiException("subscription revision", 
                response.getStatusCode().value(), 
                "Unexpected response status");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            auditLogger.logPayPalApiError("REVISE_SUBSCRIPTION", tenantId, subscriptionId,
                    e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
            throw new PayPalApiException("subscription revision", e.getStatusCode().value(),
                    "PayPal API error: " + e.getMessage());
        } catch (Exception e) {
            auditLogger.logFailure("REVISE_SUBSCRIPTION", tenantId, null,
                    "Unexpected error revising subscription: " + subscriptionId, e);
            throw new PayPalApiException("subscription revision", "Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Call PayPal API to reactivate a cancelled subscription.
     */
    private void callPayPalReactivateSubscription(String subscriptionId) {
        logger.info("Reactivating PayPal subscription: {}", subscriptionId);
        
        try {
            String accessToken = payPalConfigService.getAccessToken();
            String baseUrl = payPalConfigService.getBaseUrl();

            String url = baseUrl + "/v1/billing/subscriptions/" + subscriptionId + "/activate";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, String> body = Map.of("reason", "Subscription resumed by tenant administrator");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, request, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new PayPalApiException("subscription reactivation",
                        response.getStatusCode().value(),
                        "Unexpected response when reactivating subscription");
            }

            auditLogger.logSubscriptionEvent("REACTIVATED", subscriptionId, null,
                    "Subscription reactivated via admin portal");
                    
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            auditLogger.logPayPalApiError("REACTIVATE_SUBSCRIPTION", null, subscriptionId,
                    e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 422 &&
                    e.getResponseBodyAsString() != null &&
                    e.getResponseBodyAsString().contains("SUBSCRIPTION_STATUS_INVALID")) {
                throw new InvalidSubscriptionStatusException(subscriptionId, "CANCELLED", "SUSPENDED");
            }

            throw new PayPalApiException("subscription reactivation", e.getStatusCode().value(),
                    "PayPal API error: " + e.getMessage());
        } catch (Exception e) {
            auditLogger.logFailure("REACTIVATE_SUBSCRIPTION", null, null,
                    "Unexpected error reactivating subscription: " + subscriptionId, e);
            throw new PayPalApiException("subscription reactivation", "Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Generate PayPal billing portal URL for payment method updates.
     */
    private String generatePayPalBillingPortalUrl(String subscriptionId) {
        logger.info("Generating billing portal URL for subscription: {}", subscriptionId);
        
        try {
            String accessToken = payPalConfigService.getAccessToken();
            String baseUrl = payPalConfigService.getBaseUrl();

            // PayPal doesn't have a direct billing portal API, so we construct a URL
            // that redirects to PayPal's subscription management page
            String portalUrl = baseUrl.replace("/v1", "") + "/myaccount/autopay/connect/" + subscriptionId;
            
            auditLogger.logSubscriptionEvent("BILLING_PORTAL_URL_GENERATED", subscriptionId, null,
                    "Billing portal URL generated");
            
            return portalUrl;
            
        } catch (Exception e) {
            logger.error("Error generating billing portal URL for subscription: {}", subscriptionId, e);
            auditLogger.logFailure("BILLING_PORTAL_URL_GENERATION", null, null,
                    "Error generating billing portal URL for subscription: " + subscriptionId, e);
            throw new PayPalApiException("billing portal URL generation", "Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Manually override a tenant's plan tier.
     * This is a privileged operation for SaaS managers to handle special cases.
     * All overrides are logged with manager identity and reason.
     * Evicts the tenant plan cache to ensure fresh data is fetched.
     *
     * @param tenantId the tenant ID
     * @param targetTier the target plan tier
     * @param managerId the SaaS manager ID performing the override
     * @param reason the reason for the override
     * @throws NotFoundException if tenant or subscription not found
     * @throws IllegalArgumentException if target tier is invalid
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "tenantPlan", key = "#tenantId", cacheManager = "billingSaasCacheManager")
    public void manualPlanOverride(Long tenantId, PlanTier targetTier, Long managerId, String reason) {
        logger.warn("Manual plan override requested for tenant: {} to tier: {} by manager: {}", 
            tenantId, targetTier, managerId);
        
        // Validate tenant and subscription exist
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant with ID " + tenantId + " not found"));

        SubscriptionEntity subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for tenant ID " + tenantId));

        // Validate target tier exists
        if (!planTierConfig.tierExists(targetTier)) {
            throw new IllegalArgumentException("Invalid plan tier: " + targetTier);
        }

        // Store old tier for audit logging
        String oldTier = subscription.getPlanTier() != null ? subscription.getPlanTier().name() : "BASIC";
        
        // Check if already at target tier
        if (subscription.getPlanTier() == targetTier) {
            logger.info("Plan tier already set to {} for tenant: {}, no change needed", targetTier, tenantId);
            return;
        }

        // Apply the plan change immediately
        subscription.setPlanTier(targetTier);
        subscription.setPendingPlanTier(null);
        subscription.setPendingPlanEffectiveDate(null);
        subscriptionRepository.save(subscription);

        // Log the manual override with detailed audit trail
        auditLogger.logManualPlanOverride(tenantId, managerId, oldTier, targetTier.name(), reason);
        auditLogger.logSubscriptionEvent("MANUAL_PLAN_OVERRIDE", subscription.getPaypalSubscriptionId(), tenantId,
                "Plan manually overridden from " + oldTier + " to " + targetTier + " by manager " + managerId);

        logger.warn("Plan tier manually overridden for tenant: {} from {} to {} by manager: {}. Reason: {}", 
            tenantId, oldTier, targetTier, managerId, reason);
    }
}
