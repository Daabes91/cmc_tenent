package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.dto.PayPalConfigRequest;
import com.clinic.modules.saas.dto.PayPalConfigResponse;
import com.clinic.modules.saas.service.PayPalConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing PayPal configuration.
 * Only accessible by SaaS managers.
 */
@RestController
@RequestMapping("/saas/paypal-config")
@Tag(name = "SaaS PayPal Configuration", description = "Endpoints for managing PayPal billing configuration")
@SecurityRequirement(name = "bearerAuth")
public class PayPalConfigController {

    private static final Logger logger = LoggerFactory.getLogger(PayPalConfigController.class);

    private final PayPalConfigService payPalConfigService;

    public PayPalConfigController(PayPalConfigService payPalConfigService) {
        this.payPalConfigService = payPalConfigService;
    }

    /**
     * Get current PayPal configuration.
     * Client secret is masked for security.
     *
     * @return PayPalConfigResponse with current configuration
     */
    @GetMapping
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Get PayPal configuration",
            description = "Retrieve the current PayPal API configuration. Client secret is masked."
    )
    public ResponseEntity<PayPalConfigResponse> getConfig() {
        logger.info("Fetching PayPal configuration");
        PayPalConfigResponse config = payPalConfigService.getConfig();
        return ResponseEntity.ok(config);
    }

    /**
     * Update PayPal configuration.
     * Validates credentials before saving.
     *
     * @param request PayPalConfigRequest with new configuration
     * @return Updated PayPalConfigResponse
     */
    @PutMapping
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Update PayPal configuration",
            description = "Update PayPal API credentials and settings. Credentials are validated before saving."
    )
    public ResponseEntity<PayPalConfigResponse> updateConfig(@Valid @RequestBody PayPalConfigRequest request) {
        logger.info("Updating PayPal configuration");
        PayPalConfigResponse config = payPalConfigService.updateConfig(request);
        return ResponseEntity.ok(config);
    }
}
