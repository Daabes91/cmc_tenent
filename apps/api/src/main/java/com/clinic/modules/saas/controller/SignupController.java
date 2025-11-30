package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.dto.SignupRequest;
import com.clinic.modules.saas.dto.SignupResponse;
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
 * Handles clinic registration and PayPal subscription initiation.
 */
@RestController
@RequestMapping("/api/public/signup")
@CrossOrigin(origins = "*")
public class SignupController {

    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    /**
     * Register a new clinic and initiate PayPal subscription.
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
                logger.info("Signup successful for subdomain: {}", request.getSubdomain());
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
     * Check if a subdomain is available.
     *
     * @param subdomain the subdomain to check
     * @return availability status
     */
    @GetMapping("/check-subdomain")
    public ResponseEntity<Map<String, Boolean>> checkSubdomain(@RequestParam String subdomain) {
        logger.debug("Checking subdomain availability: {}", subdomain);

        boolean available = signupService.isSubdomainAvailable(subdomain);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);

        return ResponseEntity.ok(response);
    }
}
