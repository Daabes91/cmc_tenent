package com.clinic.modules.saas.service;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.dto.PayPalWebhookEvent;
import com.clinic.modules.saas.dto.PayPalWebhookResource;
import com.clinic.modules.saas.exception.PayPalApiException;
import com.clinic.modules.saas.exception.WebhookVerificationException;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.monitoring.BillingAlertService;
import com.clinic.modules.saas.monitoring.BillingMetricsService;
import com.clinic.modules.saas.repository.PaymentTransactionRepository;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for processing PayPal webhook events.
 * Handles signature verification and event processing.
 */
@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    private final PayPalConfigService payPalConfigService;
    private final RestTemplate restTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final TenantRepository tenantRepository;
    private final BillingAuditLogger auditLogger;
    private final BillingMetricsService metricsService;
    private final BillingAlertService alertService;

    public WebhookService(
            PayPalConfigService payPalConfigService,
            RestTemplate restTemplate,
            SubscriptionRepository subscriptionRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            TenantRepository tenantRepository,
            BillingAuditLogger auditLogger,
            BillingMetricsService metricsService,
            BillingAlertService alertService) {
        this.payPalConfigService = payPalConfigService;
        this.restTemplate = restTemplate;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.tenantRepository = tenantRepository;
        this.auditLogger = auditLogger;
        this.metricsService = metricsService;
        this.alertService = alertService;
    }

    /**
     * Verify webhook signature using PayPal API.
     *
     * @param transmissionId PayPal transmission ID header
     * @param transmissionTime PayPal transmission time header
     * @param transmissionSig PayPal transmission signature header
     * @param certUrl PayPal certificate URL header
     * @param authAlgo PayPal auth algorithm header
     * @param webhookId PayPal webhook ID from configuration
     * @param webhookBody Raw webhook body as string
     * @return true if signature is valid
     * @throws WebhookVerificationException if signature verification fails
     * @throws PayPalApiException if PayPal API call fails
     */
    public boolean verifyWebhookSignature(
            String transmissionId,
            String transmissionTime,
            String transmissionSig,
            String certUrl,
            String authAlgo,
            String webhookId,
            String webhookBody) {

        logger.info("Verifying webhook signature for transmission ID: {}", transmissionId);

        try {
            String accessToken = payPalConfigService.getAccessToken();
            String baseUrl = payPalConfigService.getBaseUrl();

            String url = baseUrl + "/v1/notifications/verify-webhook-signature";

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("transmission_id", transmissionId);
            requestBody.put("transmission_time", transmissionTime);
            requestBody.put("transmission_sig", transmissionSig);
            requestBody.put("cert_url", certUrl);
            requestBody.put("auth_algo", authAlgo);
            requestBody.put("webhook_id", webhookId);
            requestBody.put("webhook_event", webhookBody);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make API call
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                String verificationStatus = (String) responseBody.get("verification_status");

                boolean isValid = "SUCCESS".equals(verificationStatus);
                
                if (isValid) {
                    logger.info("Webhook signature verified successfully for transmission ID: {}", transmissionId);
                    return true;
                } else {
                    logger.error("SECURITY ALERT: Webhook signature verification failed for transmission ID: {}. Status: {}", 
                        transmissionId, verificationStatus);
                    auditLogger.logWebhookVerificationFailure(transmissionId, "Verification status: " + verificationStatus);
                    metricsService.recordWebhookVerificationFailure();
                    alertService.alertWebhookVerificationFailure(transmissionId, "Verification status: " + verificationStatus);
                    throw new WebhookVerificationException(transmissionId, "Verification status: " + verificationStatus);
                }
            }

            logger.error("SECURITY ALERT: Failed to verify webhook signature. Response status: {}", response.getStatusCode());
            auditLogger.logWebhookVerificationFailure(transmissionId, "Unexpected response status: " + response.getStatusCode());
            metricsService.recordWebhookVerificationFailure();
            alertService.alertWebhookVerificationFailure(transmissionId, "Unexpected response status: " + response.getStatusCode());
            throw new WebhookVerificationException(transmissionId, "Unexpected response status: " + response.getStatusCode());

        } catch (WebhookVerificationException e) {
            // Re-throw webhook verification exceptions
            throw e;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("SECURITY ALERT: PayPal API error during webhook verification for transmission ID: {}. Status: {}, Body: {}", 
                transmissionId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            auditLogger.logWebhookVerificationFailure(transmissionId, 
                "PayPal API error: " + e.getStatusCode() + " - " + e.getMessage());
            metricsService.recordWebhookVerificationFailure();
            alertService.alertWebhookVerificationFailure(transmissionId, "PayPal API error: " + e.getStatusCode());
            throw new PayPalApiException("webhook verification", e.getStatusCode().value(), 
                "PayPal API error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("SECURITY ALERT: Unexpected error during webhook verification for transmission ID: {}", transmissionId, e);
            auditLogger.logWebhookVerificationFailure(transmissionId, "Unexpected error: " + e.getMessage());
            metricsService.recordWebhookVerificationFailure();
            alertService.alertWebhookVerificationFailure(transmissionId, "Unexpected error: " + e.getMessage());
            throw new PayPalApiException("webhook verification", "Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Process a PayPal webhook event.
     * Routes the event to the appropriate handler based on event type.
     *
     * @param event the webhook event
     */
    @Transactional
    public void processWebhookEvent(PayPalWebhookEvent event) {
        logger.info("Processing webhook event: {} (ID: {})", event.getEventType(), event.getId());
        String subscriptionId = event.getResource() != null ? event.getResource().getId() : null;
        auditLogger.logWebhookReceived(event.getEventType(), event.getId(), subscriptionId, null);
        
        // Record webhook received metric
        metricsService.recordWebhookReceived(event.getEventType());
        
        // Start timer for processing duration
        Timer.Sample sample = metricsService.startWebhookProcessingTimer();

        try {
            String eventType = event.getEventType();

            switch (eventType) {
                case "BILLING.SUBSCRIPTION.ACTIVATED":
                    handleSubscriptionActivated(event);
                    break;
                case "BILLING.SUBSCRIPTION.CANCELLED":
                    handleSubscriptionCancelled(event);
                    break;
                case "BILLING.SUBSCRIPTION.SUSPENDED":
                    handleSubscriptionSuspended(event);
                    break;
                case "BILLING.SUBSCRIPTION.UPDATED":
                    handlePlanChangeWebhook(event);
                    break;
                case "PAYMENT.SALE.COMPLETED":
                    handlePaymentMethodUpdateWebhook(event);
                    break;
                default:
                    logger.info("Unhandled webhook event type: {}", eventType);
                    auditLogger.logWebhookProcessed(eventType, event.getId(), null, true, 
                        "Event type not handled");
            }
            
            // Record successful processing
            metricsService.recordWebhookProcessedSuccess(event.getEventType());
            metricsService.stopWebhookProcessingTimer(sample);

        } catch (Exception e) {
            logger.error("Error processing webhook event: {} (ID: {})", event.getEventType(), event.getId(), e);
            auditLogger.logWebhookProcessed(event.getEventType(), event.getId(), null, false, 
                "Error: " + e.getMessage());
            
            // Record failed processing
            metricsService.recordWebhookProcessedFailure(event.getEventType(), e.getClass().getSimpleName());
            metricsService.stopWebhookProcessingTimer(sample);
            
            throw e;
        }
    }

    /**
     * Handle subscription activated event.
     * Updates tenant billing status to active.
     *
     * @param event the webhook event
     */
    private void handleSubscriptionActivated(PayPalWebhookEvent event) {
        PayPalWebhookResource resource = event.getResource();
        String subscriptionId = resource.getId();
        String customId = resource.getCustomId();

        logger.info("Handling subscription activated: {}", subscriptionId);

        // Find subscription
        SubscriptionEntity subscription = subscriptionRepository
                .findByPaypalSubscriptionId(subscriptionId)
                .orElse(null);

        if (subscription == null) {
            logger.warn("Subscription not found for PayPal subscription ID: {}. Skipping activation.", subscriptionId);
            return;
        }

        // Update subscription status
        subscription.setStatus("ACTIVE");
        
        // Update billing period if available
        if (resource.getBillingInfo() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> billingInfo = resource.getBillingInfo();
            String nextBillingTime = (String) billingInfo.get("next_billing_time");
            if (nextBillingTime != null) {
                subscription.setCurrentPeriodEnd(parsePayPalDateTime(nextBillingTime));
            }
        }
        
        subscriptionRepository.save(subscription);

        // Update tenant billing status
        TenantEntity tenant = subscription.getTenant();
        String oldStatus = tenant.getBillingStatus().name();
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        tenantRepository.save(tenant);

        logger.info("Subscription activated successfully for tenant: {}", tenant.getId());
        auditLogger.logBillingStatusChange(tenant.getId(), oldStatus, "ACTIVE", 
            "Webhook event: BILLING.SUBSCRIPTION.ACTIVATED");
        auditLogger.logWebhookProcessed("BILLING.SUBSCRIPTION.ACTIVATED", resource.getId(), 
            tenant.getId(), true, "Subscription and tenant activated");
        
        // Record metrics
        metricsService.recordSubscriptionActivated();
        metricsService.recordBillingStatusChange(oldStatus, "ACTIVE");
    }

    /**
     * Handle subscription cancelled event.
     * Updates tenant billing status to canceled.
     *
     * @param event the webhook event
     */
    private void handleSubscriptionCancelled(PayPalWebhookEvent event) {
        PayPalWebhookResource resource = event.getResource();
        String subscriptionId = resource.getId();

        logger.info("Handling subscription cancelled: {}", subscriptionId);

        // Find subscription
        SubscriptionEntity subscription = subscriptionRepository
                .findByPaypalSubscriptionId(subscriptionId)
                .orElse(null);

        if (subscription == null) {
            logger.warn("Subscription not found for PayPal subscription ID: {}. Skipping cancellation.", subscriptionId);
            return;
        }

        // Update subscription status
        subscription.setStatus("CANCELLED");
        subscriptionRepository.save(subscription);

        // Update tenant billing status
        TenantEntity tenant = subscription.getTenant();
        String oldStatus = tenant.getBillingStatus().name();
        tenant.setBillingStatus(BillingStatus.CANCELED);
        tenantRepository.save(tenant);

        logger.info("Subscription cancelled successfully for tenant: {}", tenant.getId());
        auditLogger.logBillingStatusChange(tenant.getId(), oldStatus, "CANCELED", 
            "Webhook event: BILLING.SUBSCRIPTION.CANCELLED");
        auditLogger.logWebhookProcessed("BILLING.SUBSCRIPTION.CANCELLED", resource.getId(), 
            tenant.getId(), true, "Subscription cancelled, tenant access revoked");
        
        // Record metrics
        metricsService.recordSubscriptionCancelled();
        metricsService.recordBillingStatusChange(oldStatus, "CANCELED");
    }

    /**
     * Handle subscription suspended event.
     * Updates tenant billing status to past_due.
     *
     * @param event the webhook event
     */
    private void handleSubscriptionSuspended(PayPalWebhookEvent event) {
        PayPalWebhookResource resource = event.getResource();
        String subscriptionId = resource.getId();

        logger.info("Handling subscription suspended: {}", subscriptionId);

        // Find subscription
        SubscriptionEntity subscription = subscriptionRepository
                .findByPaypalSubscriptionId(subscriptionId)
                .orElse(null);

        if (subscription == null) {
            logger.warn("Subscription not found for PayPal subscription ID: {}. Skipping suspension.", subscriptionId);
            return;
        }

        // Update subscription status
        subscription.setStatus("SUSPENDED");
        subscriptionRepository.save(subscription);

        // Update tenant billing status
        TenantEntity tenant = subscription.getTenant();
        String oldStatus = tenant.getBillingStatus().name();
        tenant.setBillingStatus(BillingStatus.PAST_DUE);
        tenantRepository.save(tenant);

        logger.info("Subscription suspended successfully for tenant: {}", tenant.getId());
        auditLogger.logBillingStatusChange(tenant.getId(), oldStatus, "PAST_DUE", 
            "Webhook event: BILLING.SUBSCRIPTION.SUSPENDED");
        auditLogger.logWebhookProcessed("BILLING.SUBSCRIPTION.SUSPENDED", resource.getId(), 
            tenant.getId(), true, "Subscription suspended, tenant access restricted");
        
        // Record metrics
        metricsService.recordSubscriptionSuspended();
        metricsService.recordBillingStatusChange(oldStatus, "PAST_DUE");
    }

    /**
     * Handle plan change webhook event (BILLING.SUBSCRIPTION.UPDATED).
     * Updates subscription entity when plan tier changes are detected.
     *
     * @param event the webhook event
     */
    private void handlePlanChangeWebhook(PayPalWebhookEvent event) {
        PayPalWebhookResource resource = event.getResource();
        String subscriptionId = resource.getId();

        logger.info("Handling plan change webhook: {}", subscriptionId);

        // Find subscription
        SubscriptionEntity subscription = subscriptionRepository
                .findByPaypalSubscriptionId(subscriptionId)
                .orElse(null);

        if (subscription == null) {
            logger.warn("Subscription not found for PayPal subscription ID: {}. Skipping plan change.", subscriptionId);
            return;
        }

        TenantEntity tenant = subscription.getTenant();
        PlanTier oldPlanTier = subscription.getPlanTier();
        String oldPlanTierName = oldPlanTier != null ? oldPlanTier.name() : "UNKNOWN";

        // Extract plan information from billing_info
        if (resource.getBillingInfo() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> billingInfo = resource.getBillingInfo();
            
            // Update next billing time if available
            String nextBillingTime = (String) billingInfo.get("next_billing_time");
            if (nextBillingTime != null) {
                LocalDateTime renewalDate = parsePayPalDateTime(nextBillingTime);
                subscription.setRenewalDate(renewalDate);
                subscription.setCurrentPeriodEnd(renewalDate);
                logger.info("Updated renewal date to: {}", renewalDate);
            }

            // Check for last payment information
            @SuppressWarnings("unchecked")
            Map<String, Object> lastPayment = (Map<String, Object>) billingInfo.get("last_payment");
            if (lastPayment != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> amountData = (Map<String, Object>) lastPayment.get("amount");
                if (amountData != null) {
                    String amountValue = (String) amountData.get("value");
                    String currency = (String) amountData.get("currency_code");
                    logger.info("Last payment: {} {}", amountValue, currency);
                }
            }
        }

        // If there's a pending plan change, apply it now
        if (subscription.getPendingPlanTier() != null) {
            PlanTier newPlanTier = subscription.getPendingPlanTier();
            LocalDateTime effectiveDate = subscription.getPendingPlanEffectiveDate();
            subscription.setPlanTier(newPlanTier);
            subscription.setPendingPlanTier(null);
            subscription.setPendingPlanEffectiveDate(null);
            
            logger.info("Applied pending plan change from {} to {} for tenant: {}", 
                oldPlanTierName, newPlanTier.name(), tenant.getId());
            
            // Log the plan change with detailed audit trail
            auditLogger.logPlanUpgrade(tenant.getId(), null, oldPlanTierName, newPlanTier.name(), effectiveDate);
            auditLogger.logPlanChange(tenant.getId(), oldPlanTierName, newPlanTier.name(), 
                "Webhook event: BILLING.SUBSCRIPTION.UPDATED");
        }

        // Update subscription status if changed
        if (resource.getStatus() != null && !resource.getStatus().equals(subscription.getStatus())) {
            String oldStatus = subscription.getStatus();
            subscription.setStatus(resource.getStatus());
            logger.info("Updated subscription status from {} to {}", oldStatus, resource.getStatus());
        }

        subscriptionRepository.save(subscription);

        logger.info("Plan change processed successfully for tenant: {}", tenant.getId());
        auditLogger.logWebhookProcessed("BILLING.SUBSCRIPTION.UPDATED", subscriptionId, 
            tenant.getId(), true, "Plan change applied");
    }

    /**
     * Handle payment method update webhook event (PAYMENT.SALE.COMPLETED).
     * Updates payment method information and records the payment transaction.
     *
     * @param event the webhook event
     */
    private void handlePaymentMethodUpdateWebhook(PayPalWebhookEvent event) {
        PayPalWebhookResource resource = event.getResource();
        String transactionId = resource.getId();

        logger.info("Handling payment method update: {}", transactionId);

        // Check if transaction already exists
        if (paymentTransactionRepository.findByPaypalTransactionId(transactionId).isPresent()) {
            logger.info("Payment transaction already recorded: {}", transactionId);
            return;
        }

        // Extract payment details
        Map<String, Object> amountData = resource.getAmount();
        if (amountData == null) {
            logger.warn("No amount data in payment event: {}", transactionId);
            return;
        }

        String amountValue = (String) amountData.get("value");
        String currency = (String) amountData.get("currency_code");

        if (amountValue == null || currency == null) {
            logger.warn("Missing amount or currency in payment event: {}", transactionId);
            return;
        }

        // Try to extract billing agreement ID or subscription ID from custom_id
        String customId = resource.getCustomId();
        SubscriptionEntity subscription = null;
        
        if (customId != null) {
            // Try to find subscription by custom_id (which might be tenant_id)
            try {
                Long tenantId = Long.parseLong(customId);
                subscription = subscriptionRepository.findByTenantId(tenantId).orElse(null);
            } catch (NumberFormatException e) {
                logger.debug("Custom ID is not a tenant ID: {}", customId);
            }
        }

        if (subscription != null) {
            TenantEntity tenant = subscription.getTenant();
            
            // Update payment method information if available in the webhook
            // Note: PayPal doesn't always send full payment method details in webhooks
            // We might need to call PayPal API to get full details
            String oldPaymentMethod = subscription.getPaymentMethodMask();
            
            // For now, just log that a payment was made
            // In a full implementation, you would call PayPal API to get payment method details
            logger.info("Payment completed for tenant: {}, Amount: {} {}", 
                tenant.getId(), amountValue, currency);
            
            // Log the payment transaction
            auditLogger.logPaymentTransaction(transactionId, subscription.getPaypalSubscriptionId(), 
                tenant.getId(), amountValue, currency, "COMPLETED");
            
            // If payment method changed, log it
            if (oldPaymentMethod != null) {
                auditLogger.logPaymentMethodUpdate(tenant.getId(), oldPaymentMethod, 
                    "Payment method used for transaction", "Webhook event: PAYMENT.SALE.COMPLETED");
            }
            
            auditLogger.logWebhookProcessed("PAYMENT.SALE.COMPLETED", transactionId, 
                tenant.getId(), true, "Payment recorded: " + amountValue + " " + currency);
        } else {
            // Subscription not found - log for manual review
            logger.warn("Could not find subscription for payment transaction: {}. Custom ID: {}", 
                transactionId, customId);
            
            auditLogger.logPaymentTransaction(transactionId, "unknown", null, amountValue, currency, "COMPLETED");
            auditLogger.logWebhookProcessed("PAYMENT.SALE.COMPLETED", transactionId, null, true, 
                "Payment recorded (subscription not found): " + amountValue + " " + currency);
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
}
