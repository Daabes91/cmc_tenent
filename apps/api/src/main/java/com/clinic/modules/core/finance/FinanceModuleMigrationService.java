package com.clinic.modules.core.finance;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for migrating existing tenants to have default expense categories.
 * This is a one-time migration script that can be run safely multiple times (idempotent).
 */
@Service
public class FinanceModuleMigrationService {

    private static final Logger log = LoggerFactory.getLogger(FinanceModuleMigrationService.class);

    private final TenantRepository tenantRepository;
    private final CategorySeedingService categorySeedingService;
    private final ExpenseCategoryRepository categoryRepository;

    public FinanceModuleMigrationService(
            TenantRepository tenantRepository,
            CategorySeedingService categorySeedingService,
            ExpenseCategoryRepository categoryRepository) {
        this.tenantRepository = tenantRepository;
        this.categorySeedingService = categorySeedingService;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Migrates all existing tenants to have default expense categories.
     * This operation is idempotent and safe to run multiple times.
     * 
     * @return MigrationResult containing statistics about the migration
     */
    @Transactional
    public MigrationResult migrateExistingTenants() {
        log.info("Finance module migration started - seeding categories for existing tenants");

        List<TenantEntity> allTenants = tenantRepository.findAll();
        int totalTenants = allTenants.size();
        int tenantsSeeded = 0;
        int tenantsSkipped = 0;
        int tenantsFailed = 0;

        log.info("Found {} tenants to process", totalTenants);

        for (TenantEntity tenant : allTenants) {
            Long tenantId = tenant.getId();
            String tenantSlug = tenant.getSlug();

            try {
                // Check if categories already seeded
                long existingCategoryCount = categoryRepository.countByTenantId(tenantId);
                
                if (existingCategoryCount > 0) {
                    log.debug("Migration skipped - tenant already has categories: tenantId: {}, slug: {}, existingCount: {}",
                            tenantId, tenantSlug, existingCategoryCount);
                    tenantsSkipped++;
                    continue;
                }

                // Seed default categories
                categorySeedingService.seedDefaultCategories(tenantId);
                tenantsSeeded++;
                log.info("Migration success - categories seeded: tenantId: {}, slug: {}", tenantId, tenantSlug);

            } catch (Exception e) {
                tenantsFailed++;
                log.error("Migration failed - error seeding categories: tenantId: {}, slug: {}, error: {}",
                        tenantId, tenantSlug, e.getMessage(), e);
            }
        }

        MigrationResult result = new MigrationResult(totalTenants, tenantsSeeded, tenantsSkipped, tenantsFailed);
        
        log.info("Finance module migration completed - totalTenants: {}, seeded: {}, skipped: {}, failed: {}",
                totalTenants, tenantsSeeded, tenantsSkipped, tenantsFailed);

        return result;
    }

    /**
     * Result object containing migration statistics.
     */
    public static class MigrationResult {
        private final int totalTenants;
        private final int tenantsSeeded;
        private final int tenantsSkipped;
        private final int tenantsFailed;

        public MigrationResult(int totalTenants, int tenantsSeeded, int tenantsSkipped, int tenantsFailed) {
            this.totalTenants = totalTenants;
            this.tenantsSeeded = tenantsSeeded;
            this.tenantsSkipped = tenantsSkipped;
            this.tenantsFailed = tenantsFailed;
        }

        public int getTotalTenants() {
            return totalTenants;
        }

        public int getTenantsSeeded() {
            return tenantsSeeded;
        }

        public int getTenantsSkipped() {
            return tenantsSkipped;
        }

        public int getTenantsFailed() {
            return tenantsFailed;
        }

        public boolean isSuccessful() {
            return tenantsFailed == 0;
        }

        @Override
        public String toString() {
            return String.format("MigrationResult{totalTenants=%d, seeded=%d, skipped=%d, failed=%d}",
                    totalTenants, tenantsSeeded, tenantsSkipped, tenantsFailed);
        }
    }
}
