package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.dto.SaasLoginRequest;
import com.clinic.modules.saas.dto.SaasLoginResponse;
import com.clinic.modules.saas.dto.SaasRefreshRequest;
import com.clinic.modules.saas.service.SaasManagerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for SAAS Manager authentication
 * Handles login and token refresh operations
 */
@RestController
@RequestMapping("/saas/auth")
public class SaasAuthController {

    private static final Logger log = LoggerFactory.getLogger(SaasAuthController.class);

    private final SaasManagerService saasManagerService;

    public SaasAuthController(SaasManagerService saasManagerService) {
        this.saasManagerService = saasManagerService;
    }

    /**
     * Authenticate a SAAS Manager and issue JWT token
     *
     * @param request Login credentials (email and password)
     * @return SaasLoginResponse with JWT token and manager details
     */
    @PostMapping("/login")
    public ResponseEntity<SaasLoginResponse> login(@Valid @RequestBody SaasLoginRequest request) {
        log.info("API request received - POST /saas/auth/login - email: {}", request.email());
        SaasLoginResponse response = saasManagerService.authenticate(request);
        log.info("API response sent - POST /saas/auth/login - status: 200, managerId: {}", response.managerId());
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh SAAS Manager JWT token
     * Note: This endpoint is optional and currently returns NOT_IMPLEMENTED
     *
     * @param request Refresh token request
     * @return New JWT token (when implemented)
     */
    @PostMapping("/refresh")
    public ResponseEntity<SaasLoginResponse> refresh(@Valid @RequestBody SaasRefreshRequest request) {
        log.debug("SAAS Manager token refresh request received");
        // TODO: Implement refresh token logic if needed
        // For now, SAAS Managers can simply re-login when their token expires
        return ResponseEntity.status(501).build(); // 501 Not Implemented
    }
}
