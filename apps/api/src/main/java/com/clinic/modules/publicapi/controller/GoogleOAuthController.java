package com.clinic.modules.publicapi.controller;

import com.clinic.modules.core.oauth.*;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.publicapi.dto.PatientAuthResponse;
import com.clinic.security.JwtIssuer;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for handling Google OAuth authentication flow for patients.
 * 
 * Endpoints:
 * - GET /public/auth/google/authorize - Initiate OAuth flow
 * - GET /public/auth/google/callback - Handle OAuth callback
 * - POST /public/auth/google/link - Link Google account to existing patient (authenticated)
 */
@RestController
@RequestMapping("/public/auth/google")
public class GoogleOAuthController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthController.class);

    private final GoogleOAuthService googleOAuthService;
    private final OAuthStateService oAuthStateService;
    private final PatientGoogleAuthService patientGoogleAuthService;
    private final PatientRepository patientRepository;
    private final TenantService tenantService;
    private final TenantContextHolder tenantContextHolder;
    private final JwtIssuer jwtIssuer;
    private final OAuthErrorLogger errorLogger;
    private final OAuthMetricsService metricsService;
    private final OAuthAlertService alertService;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    public GoogleOAuthController(
            GoogleOAuthService googleOAuthService,
            OAuthStateService oAuthStateService,
            PatientGoogleAuthService patientGoogleAuthService,
            PatientRepository patientRepository,
            TenantService tenantService,
            TenantContextHolder tenantContextHolder,
            JwtIssuer jwtIssuer,
            OAuthErrorLogger errorLogger,
            OAuthMetricsService metricsService,
            OAuthAlertService alertService
    ) {
        this.googleOAuthService = googleOAuthService;
        this.oAuthStateService = oAuthStateService;
        this.patientGoogleAuthService = patientGoogleAuthService;
        this.patientRepository = patientRepository;
        this.tenantService = tenantService;
        this.tenantContextHolder = tenantContextHolder;
        this.jwtIssuer = jwtIssuer;
        this.errorLogger = errorLogger;
        this.metricsService = metricsService;
        this.alertService = alertService;
    }

    /**
     * Initiate Google OAuth flow.
     * Generates authorization URL with tenant context and redirects user to Google.
     * 
     * @param tenantSlug The tenant slug from query parameter or context
     * @param response HTTP response for redirect
     * @throws IOException if redirect fails
     */
    @GetMapping("/authorize")
    public void authorize(
            @RequestParam(required = false) String tenantSlug,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Google OAuth has been disabled");
    }

    /**
     * Handle Google OAuth callback.
     * Exchanges authorization code for tokens, validates ID token,
     * retrieves user info, and creates/links patient account.
     * 
     * @param code Authorization code from Google
     * @param state State token for CSRF protection and tenant context
     * @return PatientAuthResponse with JWT token and patient info
     */
    @GetMapping("/callback")
    public ResponseEntity<PatientAuthResponse> callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Google OAuth has been disabled");
    }

    /**
     * Link Google account to existing authenticated patient.
     * This endpoint requires the patient to be already authenticated.
     * 
     * @param request Request containing Google authorization code
     * @return Success response
     */
    @PostMapping("/link")
    public ResponseEntity<Map<String, Object>> linkGoogleAccount(
            @RequestBody GoogleLinkRequest request
    ) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Google OAuth has been disabled");
    }

    /**
     * Request DTO for linking Google account
     */
    public record GoogleLinkRequest(
        String googleAuthCode
    ) {}
}
