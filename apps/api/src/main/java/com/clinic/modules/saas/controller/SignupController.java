package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.dto.SignupRequest;
import com.clinic.modules.saas.dto.SignupResponse;
import com.clinic.modules.saas.dto.InlineSignupResponse;
import com.clinic.modules.saas.exception.PayPalApiException;
import com.clinic.modules.saas.exception.PayPalConfigurationException;
import com.clinic.modules.saas.exception.SubdomainExistsException;
import com.clinic.modules.saas.service.SignupService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for public signup endpoints.
 * Handles clinic registration and PayPal subscription initiation using redirect-based flow.
 * Users are redirected to PayPal to approve subscriptions.
 */
@RestController
@RequestMapping({"/api/public/signup", "/public/signup"})
@CrossOrigin(origins = "*")
public class SignupController {

    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    /**
     * Register a new clinic and initiate PayPal subscription with redirect flow.
     * Creates a tenant with billing_status = 'pending_payment', creates owner user,
     * creates PayPal subscription with return_url and cancel_url, and returns approval_url.
     *
     * @param request SignupRequest with clinic and owner information
     * @return SignupResponse with PayPal approval URL or error message
     */
    @PostMapping
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        logger.info("Received signup request for subdomain: {}", request.getSubdomain());

        try {
            SignupResponse response = signupService.signup(request);

            if (response.isSuccess()) {
                logger.info("Signup successful for subdomain: {}. Approval URL: {}", 
                    request.getSubdomain(), response.getApprovalUrl());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }

            logger.error("Signup failed for subdomain {} with error: {}", request.getSubdomain(), response.getError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (SubdomainExistsException e) {
            logger.warn("Subdomain conflict for: {}", request.getSubdomain(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(SignupResponse.error("This subdomain is already taken. Please choose another one."));
        } catch (PayPalConfigurationException e) {
            logger.error("PayPal configuration error during signup for subdomain: {}", request.getSubdomain(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(SignupResponse.error("Payment service is not configured. Please contact support."));
        } catch (PayPalApiException e) {
            logger.error("PayPal API error during signup for subdomain: {}", request.getSubdomain(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(SignupResponse.error("Payment service is temporarily unavailable. Please try again later."));
        } catch (Exception e) {
            logger.error("Unexpected error during signup for subdomain: {}", request.getSubdomain(), e);
            SignupResponse errorResponse = SignupResponse.error("An unexpected error occurred. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Inline signup entrypoint for PayPal JS SDK (no redirect).
     */
    @PostMapping("/inline")
    public ResponseEntity<InlineSignupResponse> signupInline(@Valid @RequestBody SignupRequest request) {
        logger.info("Received inline signup request for subdomain: {}", request.getSubdomain());

        try {
            InlineSignupResponse response = signupService.signupInline(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SubdomainExistsException e) {
            logger.warn("Subdomain conflict (inline) for: {}", request.getSubdomain(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(InlineSignupResponse.error("This subdomain is already taken. Please choose another one."));
        } catch (PayPalConfigurationException e) {
            logger.error("PayPal configuration error (inline) during signup for subdomain: {}", request.getSubdomain(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(InlineSignupResponse.error("Payment service is not configured. Please contact support."));
        } catch (PayPalApiException e) {
            logger.error("PayPal API error (inline) during signup for subdomain: {}", request.getSubdomain(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(InlineSignupResponse.error("Payment service is temporarily unavailable. Please try again later."));
        } catch (Exception e) {
            logger.error("Unexpected error (inline) during signup for subdomain: {}", request.getSubdomain(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(InlineSignupResponse.error("An unexpected error occurred. Please try again later."));
        }
    }

    /**
     * Check if a subdomain is available.
     *
     * @param subdomain the subdomain to check
     * @return availability status
     */
    @GetMapping("/check-subdomain")
    public ResponseEntity<Map<String, Object>> checkSubdomain(@RequestParam String subdomain) {
        logger.debug("Checking subdomain availability: {}", subdomain);

        Map<String, Object> response = new HashMap<>();

        String normalized = subdomain == null ? "" : subdomain.trim().toLowerCase();
        if (!normalized.matches("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$")) {
            response.put("available", false);
            response.put("error", "Invalid subdomain format");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            boolean available = signupService.isSubdomainAvailable(normalized);
            response.put("available", available);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking subdomain availability for {}: {}", normalized, e.getMessage(), e);
            response.put("available", false);
            response.put("error", "Unable to check subdomain availability right now");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
