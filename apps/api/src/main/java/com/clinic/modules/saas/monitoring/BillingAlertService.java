package com.clinic.modules.saas.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for monitoring billing metrics and triggering alerts.
 * Checks metrics periodically and logs alerts when thresholds are exceeded.
 */
@Service
public class BillingAlertService {

    private static final Logger logger = LoggerFactory.getLogger(BillingAlertService.class);
    private static final Logger alertLogger = LoggerFactory.getLogger("BILLING_ALERTS");

    // Alert thresholds
    private static final double WEBHOOK_SUCCESS_RATE_THRESHOLD = 95.0; // Alert if below 95%
    private static final double PAYPAL_API_SUCCESS_RATE_THRESHOLD = 90.0; // Alert if below 90%
    private static final double SUBSCRIPTION_CREATION_SUCCESS_RATE_THRESHOLD = 85.0; // Alert if below 85%

    private final BillingMetricsService metricsService;

    public BillingAlertService(BillingMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Check webhook processing success rate.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkWebhookSuccessRate() {
        try {
            double successRate = metricsService.getWebhookSuccessRate();
            
            if (successRate < WEBHOOK_SUCCESS_RATE_THRESHOLD) {
                triggerAlert(
                        "WEBHOOK_SUCCESS_RATE_LOW",
                        "HIGH",
                        String.format("Webhook processing success rate is %.2f%% (threshold: %.2f%%)", 
                                successRate, WEBHOOK_SUCCESS_RATE_THRESHOLD),
                        "Check webhook processing logs and PayPal webhook configuration"
                );
            }
        } catch (Exception e) {
            logger.error("Error checking webhook success rate", e);
        }
    }

    /**
     * Check PayPal API success rate.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkPayPalApiSuccessRate() {
        try {
            double successRate = metricsService.getPayPalApiSuccessRate();
            
            if (successRate < PAYPAL_API_SUCCESS_RATE_THRESHOLD) {
                triggerAlert(
                        "PAYPAL_API_ERROR_RATE_HIGH",
                        "CRITICAL",
                        String.format("PayPal API success rate is %.2f%% (threshold: %.2f%%)", 
                                successRate, PAYPAL_API_SUCCESS_RATE_THRESHOLD),
                        "Check PayPal API status and credentials. Review error logs for details."
                );
            }
        } catch (Exception e) {
            logger.error("Error checking PayPal API success rate", e);
        }
    }

    /**
     * Check subscription creation success rate.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void checkSubscriptionCreationSuccessRate() {
        try {
            double successRate = metricsService.getSubscriptionCreationSuccessRate();
            
            if (successRate < SUBSCRIPTION_CREATION_SUCCESS_RATE_THRESHOLD) {
                triggerAlert(
                        "SUBSCRIPTION_CREATION_FAILURE_RATE_HIGH",
                        "HIGH",
                        String.format("Subscription creation success rate is %.2f%% (threshold: %.2f%%)", 
                                successRate, SUBSCRIPTION_CREATION_SUCCESS_RATE_THRESHOLD),
                        "Check PayPal plan configuration and API credentials. Review signup flow logs."
                );
            }
        } catch (Exception e) {
            logger.error("Error checking subscription creation success rate", e);
        }
    }

    /**
     * Trigger an alert.
     * Logs to dedicated alert logger and can be extended to send notifications.
     *
     * @param alertType Type of alert
     * @param severity Severity level (LOW, MEDIUM, HIGH, CRITICAL)
     * @param message Alert message
     * @param recommendation Recommended action
     */
    private void triggerAlert(String alertType, String severity, String message, String recommendation) {
        String alertMessage = String.format(
                "[%s] [%s] %s | Recommendation: %s",
                severity,
                alertType,
                message,
                recommendation
        );

        // Log to dedicated alert logger
        alertLogger.error(alertMessage);

        // Also log to standard logger for visibility
        logger.error("BILLING ALERT: {}", alertMessage);

        // TODO: Extend this to send notifications via:
        // - Email (using SendGrid)
        // - Slack webhook
        // - PagerDuty
        // - Custom monitoring system
    }

    /**
     * Manual alert trigger for webhook verification failures.
     * Called immediately when a verification failure occurs.
     *
     * @param transmissionId PayPal transmission ID
     * @param reason Failure reason
     */
    public void alertWebhookVerificationFailure(String transmissionId, String reason) {
        triggerAlert(
                "WEBHOOK_VERIFICATION_FAILURE",
                "CRITICAL",
                String.format("Webhook signature verification failed for transmission ID: %s. Reason: %s", 
                        transmissionId, reason),
                "SECURITY ALERT: Investigate immediately. Possible unauthorized webhook attempt or configuration issue."
        );
    }

    /**
     * Manual alert trigger for webhook processing failures after retries.
     * Called when webhook processing fails after all retry attempts.
     *
     * @param eventId PayPal event ID
     * @param eventType Event type
     * @param error Error message
     */
    public void alertWebhookProcessingFailedAfterRetries(String eventId, String eventType, String error) {
        triggerAlert(
                "WEBHOOK_PROCESSING_FAILED_AFTER_RETRIES",
                "HIGH",
                String.format("Webhook processing failed after all retries. Event ID: %s, Type: %s, Error: %s", 
                        eventId, eventType, error),
                "Manual intervention required. Check webhook event details and process manually if needed."
        );
    }

    /**
     * Manual alert trigger for subscription creation failures.
     * Called when a subscription creation fails during signup.
     *
     * @param tenantId Tenant ID
     * @param error Error message
     */
    public void alertSubscriptionCreationFailure(Long tenantId, String error) {
        triggerAlert(
                "SUBSCRIPTION_CREATION_FAILURE",
                "HIGH",
                String.format("Failed to create subscription for tenant ID: %s. Error: %s", tenantId, error),
                "Check PayPal configuration and API status. Contact customer to retry signup."
        );
    }

    /**
     * Manual alert trigger for high PayPal API error rate.
     * Called when multiple consecutive API errors occur.
     *
     * @param operation API operation
     * @param errorCount Number of consecutive errors
     */
    public void alertHighPayPalApiErrorRate(String operation, int errorCount) {
        triggerAlert(
                "HIGH_PAYPAL_API_ERROR_RATE",
                "CRITICAL",
                String.format("High PayPal API error rate detected for operation: %s. Consecutive errors: %d", 
                        operation, errorCount),
                "Check PayPal API status page. Verify credentials and network connectivity."
        );
    }
}
