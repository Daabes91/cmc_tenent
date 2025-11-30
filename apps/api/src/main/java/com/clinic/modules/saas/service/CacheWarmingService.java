package com.clinic.modules.saas.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.saas.config.PlanTierConfig;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for warming up caches on application startup.
 * Pre-populates frequently accessed data to improve initial response times.
 */
@Service
public class CacheWarmingService {

    private static final Logger logger = LoggerFactory.getLogger(CacheWarmingService.class);

    private final PlanTierConfig planTierConfig;
    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;

    public CacheWarmingService(
            PlanTierConfig planTierConfig,
            SubscriptionService subscriptionService,
            SubscriptionRepository subscriptionRepository) {
        this.planTierConfig = planTierConfig;
        this.subscriptionService = subscriptionService;
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Warm up caches when the application is ready.
     * This runs asynchronously to avoid blocking application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void warmUpCaches() {
        logger.info("Starting cache warming process...");
        
        try {
            // Warm up plan tier configuration cache
            warmUpPlanTierConfigCache();
            
            // Warm up tenant subscription cache
            warmUpTenantSubscriptionCache();
            
            logger.info("Cache warming completed successfully");
        } catch (Exception e) {
            logger.error("Error during cache warming. Application will continue but initial requests may be slower.", e);
        }
    }

    /**
     * Pre-load all plan tier configurations into cache.
     * This includes plan details, pricing, features, and PayPal IDs for all tiers.
     */
    private void warmUpPlanTierConfigCache() {
        logger.info("Warming up plan tier configuration cache...");
        
        try {
            int cachedItems = 0;
            
            // Cache plan details for all tiers
            for (PlanTier tier : PlanTier.values()) {
                try {
                    // Cache plan details
                    planTierConfig.getPlanDetails(tier);
                    cachedItems++;
                    
                    // Cache PayPal product ID
                    planTierConfig.getPayPalProductId(tier);
                    cachedItems++;
                    
                    // Cache PayPal plan IDs for both billing cycles
                    planTierConfig.getPayPalPlanId(tier, "MONTHLY");
                    planTierConfig.getPayPalPlanId(tier, "ANNUAL");
                    cachedItems += 2;
                    
                    // Cache pricing for common currencies and billing cycles
                    String[] currencies = {"USD", "EUR", "GBP"};
                    String[] cycles = {"MONTHLY", "ANNUAL"};
                    
                    for (String currency : currencies) {
                        for (String cycle : cycles) {
                            planTierConfig.getPrice(tier, currency, cycle);
                            cachedItems++;
                        }
                    }
                    
                    // Cache features
                    planTierConfig.getFeatures(tier);
                    cachedItems++;
                    
                } catch (Exception e) {
                    logger.warn("Failed to cache configuration for tier: {}", tier, e);
                }
            }
            
            logger.info("Plan tier configuration cache warmed up with {} items", cachedItems);
        } catch (Exception e) {
            logger.error("Error warming up plan tier configuration cache", e);
        }
    }

    /**
     * Pre-load active tenant subscriptions into cache.
     * This caches plan details for tenants with active subscriptions.
     */
    private void warmUpTenantSubscriptionCache() {
        logger.info("Warming up tenant subscription cache...");
        
        try {
            // Find all active subscriptions
            List<SubscriptionEntity> activeSubscriptions = subscriptionRepository.findByStatus("ACTIVE");
            
            int cachedTenants = 0;
            int failedTenants = 0;
            
            for (SubscriptionEntity subscription : activeSubscriptions) {
                try {
                    TenantEntity tenant = subscription.getTenant();
                    if (tenant != null && tenant.getId() != null) {
                        // This will cache the plan details for this tenant
                        subscriptionService.getPlanDetails(tenant.getId());
                        cachedTenants++;
                    }
                } catch (Exception e) {
                    // Log but continue with other tenants
                    logger.debug("Failed to cache subscription for tenant: {}", 
                        subscription.getTenant() != null ? subscription.getTenant().getId() : "unknown", e);
                    failedTenants++;
                }
            }
            
            logger.info("Tenant subscription cache warmed up: {} tenants cached, {} failed", 
                cachedTenants, failedTenants);
        } catch (Exception e) {
            logger.error("Error warming up tenant subscription cache", e);
        }
    }

    /**
     * Manually trigger cache warming.
     * This can be called by administrators to refresh caches without restarting the application.
     */
    public void manualCacheWarmup() {
        logger.info("Manual cache warming triggered");
        warmUpCaches();
    }
}
