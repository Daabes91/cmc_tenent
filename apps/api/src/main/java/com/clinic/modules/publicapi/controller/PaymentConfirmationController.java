package com.clinic.modules.publicapi.controller;

import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.dto.PaymentConfirmationResponse;
import com.clinic.modules.saas.exception.InvalidSubscriptionStatusException;
import com.clinic.modules.saas.exception.PayPalApiException;
import com.clinic.modules.saas.exception.SubscriptionNotFoundException;
import com.clinic.modules.saas.service.BillingAuditLogger;
import com.clinic.modules.saas.service.SubscriptionService;
import com.clinic.security.JwtIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for handling PayPal payment confirmation callbacks.
 * Processes the redirect from PayPal after subscription approval.
 */
@RestController
@RequestMapping({"/api/public/payment-confirmation", "/public/payment-confirmation"})
@CrossOrigin(origins = "*")
public class PaymentConfirmationController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentConfirmationController.class);

    private final SubscriptionService subscriptionService;
    private final TenantRepository tenantRepository;
    private final StaffUserRepository staffUserRepository;
    private final JwtIssuer jwtIssuer;
    private final BillingAuditLogger auditLogger;

    @Value("${app.admin-url:http://localhost:3001}")
    private String adminAppUrl;

    public PaymentConfirmationController(
            SubscriptionService subscriptionService,
            TenantRepository tenantRepository,
            StaffUserRepository staffUserRepository,
            JwtIssuer jwtIssuer,
            BillingAuditLogger auditLogger) {
        this.subscriptionService = subscriptionService;
        this.tenantRepository = tenantRepository;
        this.staffUserRepository = staffUserRepository;
        this.jwtIssuer = jwtIssuer;
        this.auditLogger = auditLogger;
    }

    /**
     * Handle PayPal payment confirmation callback.
     * Verifies subscription with PayPal API, updates tenant billing status to active,
     * creates session for owner user, and returns session token and redirect URL.
     *
     * @param subscriptionId PayPal subscription ID from query parameter
     * @param token PayPal token from query parameter
     * @return PaymentConfirmationResponse with session token and redirect URL or error
     */
    @GetMapping
    public ResponseEntity<PaymentConfirmationResponse> confirmPayment(
            @RequestParam("subscription_id") String subscriptionId,
            @RequestParam(value = "token", required = false) String token) {
        
        logger.info("Received payment confirmation callback for subscription: {}, token: {}", 
            subscriptionId, token);
        auditLogger.logSuccess("PAYMENT_CONFIRMATION_RECEIVED", null, null, 
            "Subscription ID: " + subscriptionId + ", Token: " + token);

        try {
            // Verify subscription with PayPal API and activate tenant
            subscriptionService.activateTenant(subscriptionId);
            logger.info("Successfully activated tenant for subscription: {}", subscriptionId);

            // Get tenant ID from subscription
            Long tenantId = getTenantIdFromSubscription(subscriptionId);
            if (tenantId == null) {
                logger.error("Failed to retrieve tenant ID for subscription: {}", subscriptionId);
                auditLogger.logFailure("PAYMENT_CONFIRMATION", null, null, 
                    "Failed to retrieve tenant ID for subscription: " + subscriptionId, null);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(PaymentConfirmationResponse.error(
                            "Failed to retrieve tenant information. Please contact support."));
            }

            // Get tenant
            TenantEntity tenant = tenantRepository.findById(tenantId)
                    .orElse(null);
            if (tenant == null) {
                logger.error("Tenant not found for ID: {}", tenantId);
                auditLogger.logFailure("PAYMENT_CONFIRMATION", tenantId, null, 
                    "Tenant not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(PaymentConfirmationResponse.error(
                            "Tenant not found. Please contact support."));
            }

            // Find owner user (first ADMIN user for the tenant)
            Optional<StaffUser> ownerUserOpt = staffUserRepository.findAllByTenantId(tenantId)
                    .stream()
                    .filter(user -> user.getRole() != null && 
                                  user.getRole().name().equals("ADMIN"))
                    .findFirst();

            if (ownerUserOpt.isEmpty()) {
                logger.error("Owner user not found for tenant: {}", tenantId);
                auditLogger.logFailure("PAYMENT_CONFIRMATION", tenantId, null, 
                    "Owner user not found", null);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(PaymentConfirmationResponse.error(
                            "Owner user not found. Please contact support."));
            }

            StaffUser ownerUser = ownerUserOpt.get();

            // Create session token for owner user
            JwtIssuer.IssuedToken issuedToken = jwtIssuer.issueStaffAccessToken(ownerUser);
            String sessionToken = issuedToken.token();
            logger.info("Created session token for owner user: {} of tenant: {}", 
                ownerUser.getId(), tenantId);
            auditLogger.logSuccess("SESSION_CREATED", tenantId, ownerUser.getId(), 
                "Session created after payment confirmation");

            // Build redirect URL to admin panel
            String redirectUrl = buildAdminRedirectUrl(tenant.getSlug());
            logger.info("Payment confirmation successful for subscription: {}. Redirecting to: {}", 
                subscriptionId, redirectUrl);
            auditLogger.logSuccess("PAYMENT_CONFIRMATION_COMPLETED", tenantId, ownerUser.getId(), 
                "Payment confirmed, session created, redirecting to admin panel");

            return ResponseEntity.ok(PaymentConfirmationResponse.success(sessionToken, redirectUrl));

        } catch (SubscriptionNotFoundException e) {
            logger.error("Subscription not found: {}", subscriptionId, e);
            auditLogger.logFailure("PAYMENT_CONFIRMATION", null, null, 
                "Subscription not found: " + subscriptionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(PaymentConfirmationResponse.error(
                        "Subscription not found. Please try signing up again."));
        } catch (InvalidSubscriptionStatusException e) {
            logger.error("Invalid subscription status for: {}. Status: {}", 
                subscriptionId, e.getCurrentStatus(), e);
            auditLogger.logFailure("PAYMENT_CONFIRMATION", null, null, 
                "Invalid subscription status: " + e.getCurrentStatus(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentConfirmationResponse.error(
                        "Subscription is not active. Status: " + e.getCurrentStatus()));
        } catch (PayPalApiException e) {
            logger.error("PayPal API error during payment confirmation for subscription: {}", 
                subscriptionId, e);
            auditLogger.logFailure("PAYMENT_CONFIRMATION", null, null, 
                "PayPal API error: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(PaymentConfirmationResponse.error(
                        "Payment verification failed. Please try again or contact support."));
        } catch (Exception e) {
            logger.error("Unexpected error during payment confirmation for subscription: {}", 
                subscriptionId, e);
            auditLogger.logFailure("PAYMENT_CONFIRMATION", null, null, 
                "Unexpected error: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentConfirmationResponse.error(
                        "An unexpected error occurred. Please contact support."));
        }
    }

    /**
     * Extract tenant ID from subscription by querying the subscription data.
     *
     * @param subscriptionId PayPal subscription ID
     * @return tenant ID or null if not found
     */
    private Long getTenantIdFromSubscription(String subscriptionId) {
        try {
            // Verify subscription to get custom_id
            var subscriptionData = subscriptionService.verifySubscription(subscriptionId);
            String customId = (String) subscriptionData.get("custom_id");
            
            if (customId != null && customId.startsWith("tenant_")) {
                return Long.parseLong(customId.substring(7));
            }
            
            logger.warn("Invalid custom_id format in subscription: {}. Value: {}", 
                subscriptionId, customId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to extract tenant ID from subscription: {}", subscriptionId, e);
            return null;
        }
    }

    /**
     * Build redirect URL to admin panel for the tenant.
     *
     * @param tenantSlug tenant subdomain slug
     * @return redirect URL
     */
    private String buildAdminRedirectUrl(String tenantSlug) {
        // Normalize admin URL
        String baseUrl = adminAppUrl.endsWith("/")
            ? adminAppUrl.substring(0, adminAppUrl.length() - 1)
            : adminAppUrl;

        // Redirect new clinics into onboarding with tenant context
        return baseUrl + "/onboarding?tenant=" + tenantSlug;
    }
}
