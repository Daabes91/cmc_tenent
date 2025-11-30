package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.migration.SubscriptionDataMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for subscription data migration operations.
 * Provides endpoints for SaaS managers to trigger one-time data migrations.
 */
@RestController
@RequestMapping("/saas/subscriptions/migration")
public class SubscriptionMigrationController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionMigrationController.class);

    @Autowired
    private SubscriptionDataMigrationService migrationService;

    /**
     * Trigger payment method details migration for all subscriptions.
     * This endpoint fetches payment method information from PayPal API
     * and updates subscription records.
     * 
     * @return Migration results including success and failure counts
     */
    @PostMapping("/payment-methods")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<Map<String, Object>> migratePaymentMethods() {
        logger.info("Payment method migration triggered by SaaS manager");
        
        try {
            int successCount = migrationService.migratePaymentMethodDetails();
            String statistics = migrationService.getMigrationStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("migratedCount", successCount);
            response.put("statistics", statistics);
            response.put("message", "Payment method migration completed successfully");
            
            logger.info("Payment method migration completed. Migrated: {}", successCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Payment method migration failed", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Payment method migration failed");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get migration statistics without triggering migration.
     * 
     * @return Current migration statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<Map<String, Object>> getMigrationStatistics() {
        logger.info("Migration statistics requested by SaaS manager");
        
        try {
            String statistics = migrationService.getMigrationStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get migration statistics", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
