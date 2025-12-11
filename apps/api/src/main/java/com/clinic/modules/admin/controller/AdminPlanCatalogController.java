package com.clinic.modules.admin.controller;

import com.clinic.modules.saas.config.PlanTierConfig;
import com.clinic.modules.saas.dto.PayPalPlanConfigDto;
import com.clinic.modules.saas.dto.PublicPlanDto;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.service.PayPalConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

/**
 * Admin-only endpoint to list available subscription plans.
 * Mirrors the public plan catalog but stays behind admin auth.
 */
@RestController
@RequestMapping("/admin/plans")
@Tag(name = "Admin Plan Catalog", description = "Admin-only plan catalog for billing flows")
public class AdminPlanCatalogController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPlanCatalogController.class);

    private final PayPalConfigService payPalConfigService;
    private final PlanTierConfig planTierConfig;

    public AdminPlanCatalogController(PayPalConfigService payPalConfigService, PlanTierConfig planTierConfig) {
        this.payPalConfigService = payPalConfigService;
        this.planTierConfig = planTierConfig;
    }

    @GetMapping
    @Operation(summary = "List admin plans", description = "Returns available plans with pricing, features, and PayPal plan IDs (admin scoped)")
    public ResponseEntity<List<PublicPlanDto>> listPlans(@RequestParam(defaultValue = "USD") String currency) {
        String normalizedCurrency = currency == null ? "USD" : currency.toUpperCase(Locale.ROOT);
        logger.info("[Admin] Listing plans for currency {}", normalizedCurrency);

        List<PayPalPlanConfigDto> configs = new ArrayList<>();
        try {
            configs = payPalConfigService.getPlanConfigsList();
        } catch (Exception e) {
            logger.warn("[Admin] No PayPal plan configs found; using defaults", e);
        }
        List<PublicPlanDto> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (PayPalPlanConfigDto config : configs) {
            if (config == null || config.getTier() == null) {
                continue;
            }

            String tierKey = config.getTier().toUpperCase(Locale.ROOT);
            if (!seen.add(tierKey)) {
                continue; // avoid duplicates
            }

            PlanTier planTier;
            try {
                planTier = PlanTier.valueOf(tierKey);
            } catch (IllegalArgumentException e) {
                logger.warn("[Admin] Skipping unknown plan tier returned from config: {}", tierKey);
                continue;
            }

            PlanTierConfig.PlanTierDetails details = planTierConfig.getPlanDetails(planTier);
            BigDecimal monthly = config.getMonthlyPrice() != null ? config.getMonthlyPrice() : planTierConfig.getPrice(planTier, normalizedCurrency, "MONTHLY");
            BigDecimal annual = config.getAnnualPrice() != null ? config.getAnnualPrice() : planTierConfig.getPrice(planTier, normalizedCurrency, "ANNUAL");
            String resolvedCurrency = config.getCurrency() != null ? config.getCurrency().toUpperCase(Locale.ROOT) : normalizedCurrency;

            boolean popular = planTier == PlanTier.PROFESSIONAL; // default highlight

            PublicPlanDto dto = new PublicPlanDto(
                    planTier.name(),
                    config.getDisplayName() != null ? config.getDisplayName() : (details != null ? details.getTierName() : planTier.name()),
                    config.getDescription() != null ? config.getDescription() : (details != null ? details.getDescription() : ""),
                    monthly,
                    annual,
                    resolvedCurrency,
                    config.getFeatures() != null && !config.getFeatures().isEmpty()
                            ? config.getFeatures()
                            : (details != null ? details.getFeatures() : Collections.emptyList()),
                    details != null ? details.getPaypalProductId() : null,
                    config.getMonthlyPlanId(),
                    config.getAnnualPlanId(),
                    popular
            );

            results.add(dto);
        }

        // If no config rows are found, fall back to default plan tier config
        if (results.isEmpty()) {
            logger.warn("[Admin] No PayPal plan configs found; falling back to PlanTierConfig defaults");
            for (PlanTier tier : PlanTier.values()) {
                if (!planTierConfig.tierExists(tier)) {
                    continue;
                }

                PlanTierConfig.PlanTierDetails details = planTierConfig.getPlanDetails(tier);
                BigDecimal monthly = planTierConfig.getPrice(tier, normalizedCurrency, "MONTHLY");
                BigDecimal annual = planTierConfig.getPrice(tier, normalizedCurrency, "ANNUAL");
                boolean popular = tier == PlanTier.PROFESSIONAL;

                PublicPlanDto dto = new PublicPlanDto(
                        tier.name(),
                        details != null ? details.getTierName() : tier.name(),
                        details != null ? details.getDescription() : "",
                        monthly,
                        annual,
                        normalizedCurrency,
                        details != null ? details.getFeatures() : Collections.emptyList(),
                        details != null ? details.getPaypalProductId() : null,
                        details != null ? details.getPaypalMonthlyPlanId() : null,
                        details != null ? details.getPaypalAnnualPlanId() : null,
                        popular
                );
                results.add(dto);
            }
        }

        return ResponseEntity.ok(results);
    }
}
