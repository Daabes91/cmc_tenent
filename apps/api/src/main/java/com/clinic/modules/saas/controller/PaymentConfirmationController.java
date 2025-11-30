package com.clinic.modules.saas.controller;

import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.saas.dto.PaymentConfirmationResponse;
import com.clinic.modules.saas.exception.InvalidSubscriptionStatusException;
import com.clinic.modules.saas.service.SubscriptionService;
import com.clinic.security.JwtIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling payment confirmation after PayPal approval.
 * Verifies subscription, activates tenant, and generates session token.
 */
@RestController
@RequestMapping("/api/public/payment-confirmation")
public class PaymentConfirmationController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentConfirmationController.class);

    private final SubscriptionService subscriptionService;
    private final StaffUserRepository staffUserRepository;
    private final JwtIssuer jwtIssuer;

    @Value("${app.admin-url:http://localhost:3001}")
    private String adminUrl;

    public PaymentConfirmationController(
            SubscriptionService subscriptionService,
            StaffUserRepository staffUserRepository,
            JwtIssuer jwtIssuer) {
        this.subscriptionService = subscriptionService;
        this.staffUserRepository = staffUserRepository;
        this.jwtIssuer = jwtIssuer;
    }

    /**
     * Handle payment confirmation after PayPal approval.
     * Verifies subscription, activates tenant, and returns session token.
     *
     * @param subscriptionId PayPal subscription ID from query parameter
     * @param token PayPal token from query parameter (optional)
     * @return PaymentConfirmationResponse with session token and redirect URL
     */
    @GetMapping
    public ResponseEntity<PaymentConfirmationResponse> confirmPayment(
            @RequestParam(name = "subscription_id", required = false) String subscriptionId,
            @RequestParam(name = "token", required = false) String token) {
        
        logger.info("Payment confirmation request received for subscription: {}", subscriptionId);

        try {
            // Validate subscription_id parameter
            if (subscriptionId == null || subscriptionId.isBlank()) {
                logger.warn("Payment confirmation failed: missing subscription_id");
                return ResponseEntity
                        .badRequest()
                        .body(PaymentConfirmationResponse.error("Missing subscription_id parameter"));
            }

            // Activate tenant (verifies subscription and creates subscription record)
            subscriptionService.activateTenant(subscriptionId);

            // Get subscription to find tenant
            var subscription = subscriptionService.verifySubscription(subscriptionId);
            String customId = (String) subscription.get("custom_id");
            
            if (customId == null || !customId.startsWith("tenant_")) {
                logger.error("Invalid custom_id in subscription: {}", customId);
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(PaymentConfirmationResponse.error("Invalid subscription data"));
            }

            Long tenantId = Long.parseLong(customId.substring(7));

            // Find owner user for the tenant (first ADMIN user)
            List<StaffUser> staffUsers = staffUserRepository.findAllByTenantId(tenantId);
            StaffUser ownerUser = staffUsers.stream()
                    .filter(user -> user.getRole().name().equals("ADMIN"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Owner user not found for tenant: " + tenantId));

            // Generate JWT token for owner user
            JwtIssuer.IssuedToken issuedToken = jwtIssuer.issueStaffAccessToken(ownerUser);
            String sessionToken = issuedToken.token();

            // Build redirect URL to tenant admin onboarding page
            String redirectUrl = adminUrl + "/onboarding";

            logger.info("Payment confirmation successful for tenant: {}, redirecting to: {}", tenantId, redirectUrl);

            return ResponseEntity.ok(PaymentConfirmationResponse.success(sessionToken, redirectUrl));

        } catch (InvalidSubscriptionStatusException e) {
            logger.error("Payment confirmation failed due to invalid subscription status: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(PaymentConfirmationResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error("Payment confirmation failed: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(PaymentConfirmationResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Payment confirmation failed with unexpected error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentConfirmationResponse.error("Payment confirmation failed. Please contact support."));
        }
    }
}
