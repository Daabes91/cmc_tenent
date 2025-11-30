package com.clinic.modules.core.finance;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for category seeding service.
 * 
 * **Feature: finance-module, Property 4: Default category seeding**
 * **Validates: Requirements 5.1, 5.2**
 * 
 * This test verifies that:
 * 1. For any tenant, exactly 7 categories are created
 * 2. All seeded categories have is_system=true
 * 3. All seeded categories have is_active=true
 * 4. The seeding operation is idempotent
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations) since jqwik
 * doesn't integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CategorySeedingPropertyTest {

    @Autowired
    private CategorySeedingService categorySeedingService;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private final Random random = new Random();
    private final String[] prefixes = {"clinic", "dental", "medical", "health", "care", "wellness", "family", "city"};
    private final String[] suffixes = {"center", "practice", "group", "associates", "clinic", "health", "care"};
    private final String[] adjectives = {"Premier", "Advanced", "Modern", "Family", "Community", "Professional"};
    private final String[] types = {"Dental", "Medical", "Health", "Wellness", "Care"};
    private final String[] locations = {"Downtown", "Uptown", "Central", "North", "South", "East", "West"};

    /**
     * Property 4: Default category seeding
     * 
     * For any newly created tenant, the system should automatically create 
     * exactly seven expense categories (Salaries, Rent, Electricity, Water, 
     * Internet/Phone, Materials, Other) marked as system categories.
     * 
     * **Feature: finance-module, Property 4: Default category seeding**
     * **Validates: Requirements 5.1, 5.2**
     */
    @Test
    public void defaultCategorySeedingCreatesExactlySevenCategories() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            String tenantSlug = generateTenantSlug();
            String tenantName = generateTenantName();
            
            try {
                // Create a new tenant
                TenantEntity tenant = new TenantEntity(tenantSlug, tenantName);
                tenant = tenantRepository.saveAndFlush(tenant);
                Long tenantId = tenant.getId();

                // Verify no categories exist before seeding
                long countBefore = categoryRepository.countByTenantId(tenantId);
                assertEquals(0, countBefore, "Tenant should have no categories before seeding");

                // Seed default categories
                categorySeedingService.seedDefaultCategories(tenantId);

                // Verify exactly 7 categories were created
                long countAfter = categoryRepository.countByTenantId(tenantId);
                assertEquals(7, countAfter, 
                    "Exactly 7 categories should be created for tenant " + tenantId);

                // Retrieve all categories for this tenant
                List<ExpenseCategoryEntity> categories = categoryRepository.findByTenantIdOrderByNameAsc(tenantId);
                
                // Verify count matches
                assertEquals(7, categories.size(), 
                    "Should retrieve exactly 7 categories for tenant " + tenantId);

                // Verify all categories have is_system=true
                for (ExpenseCategoryEntity category : categories) {
                    assertTrue(category.getIsSystem(), 
                        "Category '" + category.getName() + "' should have is_system=true");
                }

                // Verify all categories have is_active=true
                for (ExpenseCategoryEntity category : categories) {
                    assertTrue(category.getIsActive(), 
                        "Category '" + category.getName() + "' should have is_active=true");
                }

                // Verify the expected category names are present
                List<String> expectedNames = List.of(
                    "Salaries", "Rent", "Electricity", "Water", "Internet/Phone", "Materials", "Other"
                );
                
                List<String> actualNames = categories.stream()
                    .map(ExpenseCategoryEntity::getName)
                    .sorted()
                    .toList();
                
                List<String> sortedExpectedNames = expectedNames.stream().sorted().toList();
                
                assertEquals(sortedExpectedNames, actualNames,
                    "Seeded categories should match expected names");
                    
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed with tenant " + tenantSlug + ": " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 4 (variant): Idempotent seeding
     * 
     * For any tenant, calling seedDefaultCategories multiple times 
     * should not create duplicate categories.
     * 
     * **Feature: finance-module, Property 4: Default category seeding**
     * **Validates: Requirements 5.1**
     */
    @Test
    public void categorySeedingIsIdempotent() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            String tenantSlug = generateTenantSlug();
            String tenantName = generateTenantName();
            
            try {
                // Create a new tenant
                TenantEntity tenant = new TenantEntity(tenantSlug, tenantName);
                tenant = tenantRepository.saveAndFlush(tenant);
                Long tenantId = tenant.getId();

                // Seed categories first time
                categorySeedingService.seedDefaultCategories(tenantId);
                long countAfterFirstSeed = categoryRepository.countByTenantId(tenantId);
                assertEquals(7, countAfterFirstSeed, "Should have 7 categories after first seed");

                // Seed categories second time (should be idempotent)
                categorySeedingService.seedDefaultCategories(tenantId);
                long countAfterSecondSeed = categoryRepository.countByTenantId(tenantId);
                assertEquals(7, countAfterSecondSeed, 
                    "Should still have exactly 7 categories after second seed (idempotent)");

                // Seed categories third time
                categorySeedingService.seedDefaultCategories(tenantId);
                long countAfterThirdSeed = categoryRepository.countByTenantId(tenantId);
                assertEquals(7, countAfterThirdSeed, 
                    "Should still have exactly 7 categories after third seed (idempotent)");
                    
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed with tenant " + tenantSlug + ": " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 4 (variant): System categories are correctly flagged
     * 
     * For any tenant, all seeded categories should have is_system=true 
     * and is_active=true, distinguishing them from custom categories.
     * 
     * **Feature: finance-module, Property 4: Default category seeding**
     * **Validates: Requirements 5.2, 5.3, 5.4**
     */
    @Test
    public void seededCategoriesHaveCorrectSystemFlags() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            String tenantSlug = generateTenantSlug();
            String tenantName = generateTenantName();
            
            try {
                // Create a new tenant
                TenantEntity tenant = new TenantEntity(tenantSlug, tenantName);
                tenant = tenantRepository.saveAndFlush(tenant);
                Long tenantId = tenant.getId();

                // Seed default categories
                categorySeedingService.seedDefaultCategories(tenantId);

                // Retrieve all system categories
                List<ExpenseCategoryEntity> systemCategories = 
                    categoryRepository.findByTenantIdAndIsSystem(tenantId, true);
                
                // Verify exactly 7 system categories
                assertEquals(7, systemCategories.size(), 
                    "Should have exactly 7 system categories");

                // Verify all system categories are active
                long activeSystemCount = systemCategories.stream()
                    .filter(ExpenseCategoryEntity::getIsActive)
                    .count();
                
                assertEquals(7, activeSystemCount, 
                    "All 7 system categories should be active");

                // Verify no non-system categories exist
                List<ExpenseCategoryEntity> allCategories = 
                    categoryRepository.findByTenantIdOrderByNameAsc(tenantId);
                
                long nonSystemCount = allCategories.stream()
                    .filter(c -> !c.getIsSystem())
                    .count();
                
                assertEquals(0, nonSystemCount, 
                    "Should have no non-system categories after seeding");
                    
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed with tenant " + tenantSlug + ": " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 4 (variant): Tenant isolation in category seeding
     * 
     * For any two different tenants, seeding categories for one tenant 
     * should not affect the other tenant's categories.
     * 
     * **Feature: finance-module, Property 4: Default category seeding**
     * **Validates: Requirements 5.1**
     */
    @Test
    public void categorySeedingRespectsTenantIsolation() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            String tenantSlug1 = generateTenantSlug();
            String tenantSlug2 = generateTenantSlug();
            String tenantName1 = generateTenantName();
            String tenantName2 = generateTenantName();
            
            try {
                // Create two different tenants
                TenantEntity tenant1 = new TenantEntity(tenantSlug1, tenantName1);
                tenant1 = tenantRepository.saveAndFlush(tenant1);
                Long tenantId1 = tenant1.getId();

                TenantEntity tenant2 = new TenantEntity(tenantSlug2, tenantName2);
                tenant2 = tenantRepository.saveAndFlush(tenant2);
                Long tenantId2 = tenant2.getId();

                // Seed categories for tenant1
                categorySeedingService.seedDefaultCategories(tenantId1);

                // Verify tenant1 has 7 categories
                long tenant1Count = categoryRepository.countByTenantId(tenantId1);
                assertEquals(7, tenant1Count, "Tenant1 should have 7 categories");

                // Verify tenant2 has 0 categories (not affected by tenant1 seeding)
                long tenant2Count = categoryRepository.countByTenantId(tenantId2);
                assertEquals(0, tenant2Count, 
                    "Tenant2 should have 0 categories (tenant isolation)");

                // Seed categories for tenant2
                categorySeedingService.seedDefaultCategories(tenantId2);

                // Verify tenant2 now has 7 categories
                tenant2Count = categoryRepository.countByTenantId(tenantId2);
                assertEquals(7, tenant2Count, "Tenant2 should have 7 categories");

                // Verify tenant1 still has exactly 7 categories (not affected by tenant2 seeding)
                tenant1Count = categoryRepository.countByTenantId(tenantId1);
                assertEquals(7, tenant1Count, 
                    "Tenant1 should still have exactly 7 categories (tenant isolation)");

                // Verify categories are properly isolated
                List<ExpenseCategoryEntity> tenant1Categories = 
                    categoryRepository.findByTenantIdOrderByNameAsc(tenantId1);
                List<ExpenseCategoryEntity> tenant2Categories = 
                    categoryRepository.findByTenantIdOrderByNameAsc(tenantId2);

                // Verify all tenant1 categories belong to tenant1
                for (ExpenseCategoryEntity category : tenant1Categories) {
                    assertEquals(tenantId1, category.getTenant().getId(),
                        "Category should belong to tenant1");
                }

                // Verify all tenant2 categories belong to tenant2
                for (ExpenseCategoryEntity category : tenant2Categories) {
                    assertEquals(tenantId2, category.getTenant().getId(),
                        "Category should belong to tenant2");
                }
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed: " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    // ========== Helper Methods for Generating Random Test Data ==========

    private String generateTenantSlug() {
        String prefix = prefixes[random.nextInt(prefixes.length)];
        String suffix = suffixes[random.nextInt(suffixes.length)];
        int number = random.nextInt(9999) + 1;
        // Add UUID to ensure uniqueness
        return prefix + "-" + suffix + "-" + number + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateTenantName() {
        String adj = adjectives[random.nextInt(adjectives.length)];
        String type = types[random.nextInt(types.length)];
        String loc = locations[random.nextInt(locations.length)];
        return adj + " " + type + " " + loc;
    }
}
