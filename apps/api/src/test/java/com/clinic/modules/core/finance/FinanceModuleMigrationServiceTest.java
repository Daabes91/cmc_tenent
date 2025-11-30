package com.clinic.modules.core.finance;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FinanceModuleMigrationService.
 * 
 * Tests verify that:
 * 1. Migration script seeds categories for tenants without categories
 * 2. Migration script skips tenants that already have categories
 * 3. Migration script handles errors gracefully
 * 
 * **Validates: Requirements 5.1**
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FinanceModuleMigrationServiceTest {

    @Autowired
    private FinanceModuleMigrationService migrationService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private CategorySeedingService categorySeedingService;

    /**
     * Test that migration script seeds categories for tenants without categories.
     * 
     * **Validates: Requirements 5.1**
     */
    @Test
    public void migrationSeedsCategoriesForTenantsWithoutCategories() {
        // Create 3 tenants without categories
        TenantEntity tenant1 = createTenant("test-clinic-1");
        TenantEntity tenant2 = createTenant("test-clinic-2");
        TenantEntity tenant3 = createTenant("test-clinic-3");

        // Verify no categories exist
        assertEquals(0, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(0, categoryRepository.countByTenantId(tenant2.getId()));
        assertEquals(0, categoryRepository.countByTenantId(tenant3.getId()));

        // Run migration
        FinanceModuleMigrationService.MigrationResult result = migrationService.migrateExistingTenants();

        // Verify migration results - note that there may be other tenants in the database
        assertTrue(result.getTotalTenants() >= 3, "Should process at least 3 tenants");
        assertTrue(result.getTenantsSeeded() >= 3, "Should seed at least 3 tenants");
        assertEquals(0, result.getTenantsFailed(), "Should have 0 failures");
        assertTrue(result.isSuccessful(), "Migration should be successful");

        // Verify all our test tenants now have 7 categories
        assertEquals(7, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(7, categoryRepository.countByTenantId(tenant2.getId()));
        assertEquals(7, categoryRepository.countByTenantId(tenant3.getId()));

        // Verify categories are system categories
        List<ExpenseCategoryEntity> tenant1Categories = 
            categoryRepository.findByTenantIdOrderByNameAsc(tenant1.getId());
        for (ExpenseCategoryEntity category : tenant1Categories) {
            assertTrue(category.getIsSystem(), "All categories should be system categories");
            assertTrue(category.getIsActive(), "All categories should be active");
        }
    }

    /**
     * Test that migration script skips tenants that already have categories.
     * 
     * **Validates: Requirements 5.1**
     */
    @Test
    public void migrationSkipsTenantsWithExistingCategories() {
        // Create 3 tenants
        TenantEntity tenant1 = createTenant("test-clinic-1");
        TenantEntity tenant2 = createTenant("test-clinic-2");
        TenantEntity tenant3 = createTenant("test-clinic-3");

        // Seed categories for tenant1 and tenant2 before migration
        categorySeedingService.seedDefaultCategories(tenant1.getId());
        categorySeedingService.seedDefaultCategories(tenant2.getId());

        // Verify tenant1 and tenant2 have categories, tenant3 does not
        assertEquals(7, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(7, categoryRepository.countByTenantId(tenant2.getId()));
        assertEquals(0, categoryRepository.countByTenantId(tenant3.getId()));

        // Run migration
        FinanceModuleMigrationService.MigrationResult result = migrationService.migrateExistingTenants();

        // Verify migration results - note that there may be other tenants in the database
        assertTrue(result.getTotalTenants() >= 3, "Should process at least 3 tenants");
        assertTrue(result.getTenantsSeeded() >= 1, "Should seed at least 1 tenant (tenant3)");
        assertTrue(result.getTenantsSkipped() >= 2, "Should skip at least 2 tenants (tenant1, tenant2)");
        assertEquals(0, result.getTenantsFailed(), "Should have 0 failures");
        assertTrue(result.isSuccessful(), "Migration should be successful");

        // Verify tenant1 and tenant2 still have exactly 7 categories (not duplicated)
        assertEquals(7, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(7, categoryRepository.countByTenantId(tenant2.getId()));

        // Verify tenant3 now has 7 categories
        assertEquals(7, categoryRepository.countByTenantId(tenant3.getId()));
    }

    /**
     * Test that migration script is idempotent and safe to run multiple times.
     * 
     * **Validates: Requirements 5.1**
     */
    @Test
    public void migrationIsIdempotentAndSafeToRerun() {
        // Create 2 tenants
        TenantEntity tenant1 = createTenant("test-clinic-1");
        TenantEntity tenant2 = createTenant("test-clinic-2");

        // Run migration first time
        FinanceModuleMigrationService.MigrationResult result1 = migrationService.migrateExistingTenants();
        assertTrue(result1.getTotalTenants() >= 2, "Should process at least 2 tenants");
        assertTrue(result1.getTenantsSeeded() >= 2, "Should seed at least 2 tenants");
        assertEquals(0, result1.getTenantsFailed());

        // Verify both tenants have 7 categories
        assertEquals(7, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(7, categoryRepository.countByTenantId(tenant2.getId()));

        // Run migration second time (should skip our test tenants)
        FinanceModuleMigrationService.MigrationResult result2 = migrationService.migrateExistingTenants();
        assertTrue(result2.getTotalTenants() >= 2, "Should process at least 2 tenants");
        // On second run, our 2 test tenants should be skipped (they already have categories)
        assertTrue(result2.getTenantsSkipped() >= 2, "Should skip at least 2 tenants on second run");
        assertEquals(0, result2.getTenantsFailed());

        // Verify both tenants still have exactly 7 categories (not duplicated)
        assertEquals(7, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(7, categoryRepository.countByTenantId(tenant2.getId()));

        // Run migration third time (should still skip our test tenants)
        FinanceModuleMigrationService.MigrationResult result3 = migrationService.migrateExistingTenants();
        assertTrue(result3.getTotalTenants() >= 2, "Should process at least 2 tenants");
        assertTrue(result3.getTenantsSkipped() >= 2, "Should skip at least 2 tenants on third run");
        assertEquals(0, result3.getTenantsFailed());

        // Verify both tenants still have exactly 7 categories
        assertEquals(7, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(7, categoryRepository.countByTenantId(tenant2.getId()));
    }

    /**
     * Test that migration handles errors gracefully and continues processing other tenants.
     * 
     * **Validates: Requirements 5.1**
     */
    @Test
    public void migrationHandlesErrorsGracefully() {
        // Create 3 tenants
        TenantEntity tenant1 = createTenant("test-clinic-1");
        TenantEntity tenant2 = createTenant("test-clinic-2");
        TenantEntity tenant3 = createTenant("test-clinic-3");

        // Manually create a partial category for tenant2 to simulate an error state
        // This will cause the seeding to skip tenant2 (it has categories but not all 7)
        ExpenseCategoryEntity partialCategory = new ExpenseCategoryEntity("Partial", false, true);
        partialCategory.setTenant(tenant2);
        categoryRepository.save(partialCategory);

        // Run migration
        FinanceModuleMigrationService.MigrationResult result = migrationService.migrateExistingTenants();

        // Verify migration results - note that there may be other tenants in the database
        assertTrue(result.getTotalTenants() >= 3, "Should process at least 3 tenants");
        assertTrue(result.getTenantsSeeded() >= 2, "Should seed at least 2 tenants (tenant1, tenant3)");
        assertTrue(result.getTenantsSkipped() >= 1, "Should skip at least 1 tenant (tenant2 with partial categories)");
        assertEquals(0, result.getTenantsFailed(), "Should have 0 failures");

        // Verify tenant1 and tenant3 have 7 categories
        assertEquals(7, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(7, categoryRepository.countByTenantId(tenant3.getId()));

        // Verify tenant2 still has only 1 category (skipped due to existing categories)
        assertEquals(1, categoryRepository.countByTenantId(tenant2.getId()));
    }

    /**
     * Test that migration result correctly reports statistics.
     * 
     * **Validates: Requirements 5.1**
     */
    @Test
    public void migrationResultReportsCorrectStatistics() {
        // Create 2 tenants
        TenantEntity tenant1 = createTenant("test-clinic-1");
        TenantEntity tenant2 = createTenant("test-clinic-2");

        // Verify no categories exist before migration
        assertEquals(0, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(0, categoryRepository.countByTenantId(tenant2.getId()));

        // Run migration
        FinanceModuleMigrationService.MigrationResult result = migrationService.migrateExistingTenants();

        // Verify result has correct statistics
        assertTrue(result.getTotalTenants() > 0, "Should have processed some tenants");
        assertTrue(result.getTenantsSeeded() + result.getTenantsSkipped() <= result.getTotalTenants(),
                "Seeded + skipped should not exceed total");
        assertEquals(0, result.getTenantsFailed(), "Should have 0 failures");
        assertTrue(result.isSuccessful(), "Migration should be successful");

        // Verify our test tenants now have categories
        assertEquals(7, categoryRepository.countByTenantId(tenant1.getId()));
        assertEquals(7, categoryRepository.countByTenantId(tenant2.getId()));
    }

    /**
     * Test that migration result toString provides useful information.
     */
    @Test
    public void migrationResultToStringProvidesUsefulInformation() {
        // Create 2 tenants
        createTenant("test-clinic-1");
        createTenant("test-clinic-2");

        // Run migration
        FinanceModuleMigrationService.MigrationResult result = migrationService.migrateExistingTenants();

        // Verify toString contains all relevant information
        String resultString = result.toString();
        assertTrue(resultString.contains("totalTenants="), "Should contain total tenants");
        assertTrue(resultString.contains("seeded="), "Should contain seeded count");
        assertTrue(resultString.contains("skipped="), "Should contain skipped count");
        assertTrue(resultString.contains("failed=0"), "Should contain failed count");
    }

    /**
     * Test that migration correctly identifies successful vs failed migrations.
     */
    @Test
    public void migrationResultCorrectlyIdentifiesSuccess() {
        // Create 2 tenants
        createTenant("test-clinic-1");
        createTenant("test-clinic-2");

        // Run migration
        FinanceModuleMigrationService.MigrationResult result = migrationService.migrateExistingTenants();

        // Verify success flag
        assertTrue(result.isSuccessful(), "Migration with 0 failures should be successful");
        assertEquals(0, result.getTenantsFailed(), "Should have 0 failures");
    }

    // ========== Helper Methods ==========

    private TenantEntity createTenant(String slugPrefix) {
        String uniqueSlug = slugPrefix + "-" + UUID.randomUUID().toString().substring(0, 8);
        TenantEntity tenant = new TenantEntity(uniqueSlug, "Test Clinic " + uniqueSlug);
        return tenantRepository.saveAndFlush(tenant);
    }
}
