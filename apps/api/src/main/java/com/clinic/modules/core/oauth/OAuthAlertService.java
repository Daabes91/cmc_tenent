package com.clinic.modules.core.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for monitoring OAuth metrics and triggering alerts.
 * Checks metrics periodically and logs alerts when thresholds are exceeded.
 */
@Service
public class OAuthAlertService {

    private static final Logger logger = LoggerFactory.getLogger(OAuthAlertService.class);
    private static final Logger alertLogger = LoggerFactory.getLogger("OAUTH_ALERTS");

    // Alert thresholds
    private static final double OAUTH_FLOW_SUCCESS_RATE_THRESHOLD = 90.0; // Alert if below 90%
    private static final double GOOGLE_API_SUCCESS_RATE_THRESHOLD = 95.0; // Alert if below 95%
    private static final double ACCOUNT_LINKING_SUCCESS_RATE_THRESHOLD = 85.0; // Alert if below 85%

    private final OAuthMetricsService metricsService;

    public OAuthAlertService(OAuthMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Check OAuth flow success rate.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkOAuthFlowSuccessRate() {
        try {
            double successRate = metricsService.getOAuthFlowSuccessRate();
            
            if (successRate < OAUTH_FLOW_SUCCESS_RATE_THRESHOLD) {
                triggerAlert(
                        "OAUTH_FLOW_SUCCESS_RATE_LOW",
                        "HIGH",
                        String.format("OAuth flow success rate is %.2f%% (threshold: %.2f%%)", 
                                successRate, OAUTH_FLOW_SUCCESS_RATE_THRESHOLD),
                        "Check OAuth flow logs and Google OAuth configuration. Verify redirect URIs and credentials."
                );
            }
        } catch (Exception e) {
            logger.error("Error checking OAuth flow success rate", e);
        }
    }

    /**
     * Check Google API success rate.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkGoogleApiSuccessRate() {
        try {
            double successRate = metricsService.getGoogleApiSuccessRate();
            
            if (successRate < GOOGLE_API_SUCCESS_RATE_THRESHOLD) {
                triggerAlert(
                        "GOOGLE_API_ERROR_RATE_HIGH",
                        "CRITICAL",
                        String.format("Google API success rate is %.2f%% (threshold: %.2f%%)", 
                                successRate, GOOGLE_API_SUCCESS_RATE_THRESHOLD),
                        "Check Google API status and credentials. Review error logs for details. Verify network connectivity."
                );
            }
        } catch (Exception e) {
            logger.error("Error checking Google API success rate", e);
        }
    }

    /**
     * Check account linking success rate.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void checkAccountLinkingSuccessRate() {
        try {
            double successRate = metricsService.getAccountLinkingSuccessRate();
            
            if (successRate < ACCOUNT_LINKING_SUCCESS_RATE_THRESHOLD) {
                triggerAlert(
                        "ACCOUNT_LINKING_FAILURE_RATE_HIGH",
                        "MEDIUM",
                        String.format("Account linking success rate is %.2f%% (threshold: %.2f%%)", 
                                successRate, ACCOUNT_LINKING_SUCCESS_RATE_THRESHOLD),
                        "Check account linking logic and email verification. Review patient account states."
                );
            }
        } catch (Exception e) {
            logger.error("Error checking account linking success rate", e);
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
        logger.error("OAUTH ALERT: {}", alertMessage);

        // TODO: Extend this to send notifications via:
        // - Email (using SendGrid)
        // - Slack webhook
        // - PagerDuty
        // - Custom monitoring system
    }

    /**
     * Manual alert trigger for invalid state parameter.
     * Called immediately when an invalid state is detected.
     *
     * @param stateToken State token that failed validation
     * @param reason Failure reason
     */
    public void alertInvalidState(String stateToken, String reason) {
        triggerAlert(
                "INVALID_STATE_PARAMETER",
                "CRITICAL",
                String.format("Invalid OAuth state parameter detected. Reason: %s", reason),
                "SECURITY ALERT: Investigate immediately. Possible CSRF attack or expired state token."
        );
    }

    /**
     * Manual alert trigger for invalid token.
     * Called when ID token validation fails.
     *
     * @param reason Failure reason
     */
    public void alertInvalidToken(String reason) {
        triggerAlert(
                "INVALID_ID_TOKEN",
                "CRITICAL",
                String.format("ID token validation failed. Reason: %s", reason),
                "SECURITY ALERT: Investigate immediately. Possible token tampering or configuration issue."
        );
    }

    /**
     * Manual alert trigger for state tampering.
     * Called when state parameter shows signs of tampering.
     */
    public void alertStateTampering() {
        triggerAlert(
                "STATE_TAMPERING_DETECTED",
                "CRITICAL",
                "OAuth state parameter tampering detected",
                "SECURITY ALERT: Investigate immediately. Possible CSRF attack attempt."
        );
    }

    /**
     * Manual alert trigger for high Google API error rate.
     * Called when multiple consecutive API errors occur.
     *
     * @param operation API operation
     * @param errorCount Number of consecutive errors
     */
    public void alertHighGoogleApiErrorRate(String operation, int errorCount) {
        triggerAlert(
                "HIGH_GOOGLE_API_ERROR_RATE",
                "CRITICAL",
                String.format("High Google API error rate detected for operation: %s. Consecutive errors: %d", 
                        operation, errorCount),
                "Check Google API status page. Verify credentials and network connectivity. Consider circuit breaker."
        );
    }

    /**
     * Manual alert trigger for OAuth flow failures.
     * Called when OAuth flow fails after all retry attempts.
     *
     * @param tenantSlug Tenant slug
     * @param errorType Error type
     * @param error Error message
     */
    public void alertOAuthFlowFailure(String tenantSlug, String errorType, String error) {
        triggerAlert(
                "OAUTH_FLOW_FAILURE",
                "HIGH",
                String.format("OAuth flow failed for tenant: %s. Error type: %s, Error: %s", 
                        tenantSlug, errorType, error),
                "Check OAuth configuration and Google credentials. Review tenant-specific settings."
        );
    }

    /**
     * Manual alert trigger for account linking failures.
     * Called when account linking fails.
     *
     * @param tenantSlug Tenant slug
     * @param patientId Patient ID
     * @param error Error message
     */
    public void alertAccountLinkingFailure(String tenantSlug, Long patientId, String error) {
        triggerAlert(
                "ACCOUNT_LINKING_FAILURE",
                "MEDIUM",
                String.format("Failed to link Google account for patient ID: %s in tenant: %s. Error: %s", 
                        patientId, tenantSlug, error),
                "Check patient account state and Google account status. Verify email verification."
        );
    }

    /**
     * Manual alert trigger for tenant context errors.
     * Called when tenant context is lost or invalid.
     *
     * @param tenantSlug Tenant slug (may be null)
     * @param error Error message
     */
    public void alertTenantContextError(String tenantSlug, String error) {
        triggerAlert(
                "TENANT_CONTEXT_ERROR",
                "HIGH",
                String.format("Tenant context error for tenant: %s. Error: %s", 
                        tenantSlug != null ? tenantSlug : "unknown", error),
                "Check tenant configuration and state parameter handling. Verify tenant exists and is active."
        );
    }

    /**
     * Manual alert trigger for new patient creation failures.
     * Called when creating a new patient via OAuth fails.
     *
     * @param tenantSlug Tenant slug
     * @param email Patient email
     * @param error Error message
     */
    public void alertNewPatientCreationFailure(String tenantSlug, String email, String error) {
        triggerAlert(
                "NEW_PATIENT_CREATION_FAILURE",
                "HIGH",
                String.format("Failed to create new patient via OAuth for email: %s in tenant: %s. Error: %s", 
                        email, tenantSlug, error),
                "Check database constraints and patient creation logic. Verify tenant capacity and limits."
        );
    }
}
