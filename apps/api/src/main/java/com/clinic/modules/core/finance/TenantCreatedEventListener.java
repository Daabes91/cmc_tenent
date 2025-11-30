package com.clinic.modules.core.finance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener that handles tenant creation events.
 * Automatically seeds default expense categories when a new tenant is created.
 */
@Component
public class TenantCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(TenantCreatedEventListener.class);

    private final CategorySeedingService categorySeedingService;

    public TenantCreatedEventListener(CategorySeedingService categorySeedingService) {
        this.categorySeedingService = categorySeedingService;
    }

    /**
     * Handle tenant creation event by seeding default expense categories.
     * This method is executed asynchronously to avoid blocking the tenant creation transaction.
     *
     * @param event the tenant created event
     */
    @EventListener
    @Async
    public void onTenantCreated(TenantCreatedEvent event) {
        log.info("TenantCreatedEvent received - tenantId: {}, tenantSlug: {}",
                event.getTenantId(), event.getTenantSlug());

        try {
            categorySeedingService.seedDefaultCategories(event.getTenantId());
            log.info("Default categories seeded successfully for tenant - tenantId: {}, tenantSlug: {}",
                    event.getTenantId(), event.getTenantSlug());
        } catch (Exception e) {
            log.error("Failed to seed default categories for tenant - tenantId: {}, tenantSlug: {}, error: {}",
                    event.getTenantId(), event.getTenantSlug(), e.getMessage(), e);
            // Don't rethrow - we don't want to fail tenant creation if category seeding fails
            // The categories can be seeded manually later if needed
        }
    }
}
