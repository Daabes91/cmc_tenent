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
 * Property-based test for category name uniqueness.
 * 
 * **Feature: finance-module, Property 14: Category name uniqueness**
 * **Validates: Requirements 6.2, 8.2**
 * 
 * This test verifies that:
 * 1. Duplicate category names within the same tenant are rejected
 * 2. Same category name across different tenants is allowed
 * 3. Name uniqueness is enforced for both create and update operations
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations) since jqwik
 * doesn't integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CategoryNameUniquenessPropertyTest {

    @Autowired
    private ExpenseCategoryService categoryService;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private final Random random = new Random();
    private final String[] categoryPrefixes = {"Office", "Medical", "Dental", "Lab", "Admin", "Clinical", "Facility"};
    private final String[] categorySuffixes = {"Supplies", "Equipment", "Services", "Expenses", "Costs", "Fees", "Bills"};

    /**
     * Property 14: Category name uniqueness within tenant
     * 
     * For any category creation attempt, if a category with the same name 
     * already exists for that tenant, the system should reject the operation 
     * with a validation error.
     * 
     * **Feature: finance-module, Property 14: Category name uniqueness**
     * **Validates: Requirements 6.2, 8.2**
     */
    @Test
    public void duplicateCategoryNamesWithinSameTenantAreRejected() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            String tenantSlug = generateTenantSlug();
            String tenantName = generateTenantName();
            String categoryName = generateCategoryName();
            
            try {
                // Create a new tenant
                TenantEntity tenant = new TenantEntity(tenantSlug, tenantName);
                tenant = tenantRepository.saveAndFlush(tenant);
                Long tenantId = tenant.getId();

                // Create first category with the name
                ExpenseCategoryEntity firstCategory = categoryService.createCategory(tenantId, categoryName);
                assertNotNull(firstCategory, "First category should be created successfully");
                assertEquals(categoryName, firstCategory.getName(), "Category name should match");

                // Attempt to create second category with the same name (should fail)
                IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(tenantId, categoryName),
                    "Creating duplicate category name should throw IllegalArgumentException"
                );

                // Verify error message mentions duplicate
                String errorMessage = exception.getMessage().toLowerCase();
                assertTrue(
                    errorMessage.contains("already exists") || errorMessage.contains("duplicate"),
                    "Error message should indicate duplicate name: " + exception.getMessage()
                );

                // Verify only one category with this name exists
                List<ExpenseCategoryEntity> categories = categoryRepository.findByTenantIdOrderByNameAsc(tenantId);
                long matchingCount = categories.stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .count();
                assertEquals(1, matchingCount, "Should have exactly one category with this name");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed with tenant " + tenantSlug + 
                    " and category '" + categoryName + "': " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 14 (variant): Same category name across different tenants is allowed
     * 
     * For any category name, creating categories with the same name for 
     * different tenants should succeed, demonstrating tenant isolation.
     * 
     * **Feature: finance-module, Property 14: Category name uniqueness**
     * **Validates: Requirements 8.2**
     */
    @Test
    public void sameCategoryNameAcrossDifferentTenantsIsAllowed() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            String tenantSlug1 = generateTenantSlug();
            String tenantSlug2 = generateTenantSlug();
            String tenantName1 = generateTenantName();
            String tenantName2 = generateTenantName();
            String categoryName = generateCategoryName();
            
            try {
                // Create two different tenants
                TenantEntity tenant1 = new TenantEntity(tenantSlug1, tenantName1);
                tenant1 = tenantRepository.saveAndFlush(tenant1);
                Long tenantId1 = tenant1.getId();

                TenantEntity tenant2 = new TenantEntity(tenantSlug2, tenantName2);
                tenant2 = tenantRepository.saveAndFlush(tenant2);
                Long tenantId2 = tenant2.getId();

                // Create category with same name for tenant1
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId1, categoryName);
                assertNotNull(category1, "Category for tenant1 should be created successfully");
                assertEquals(categoryName, category1.getName(), "Category name should match");
                assertEquals(tenantId1, category1.getTenant().getId(), "Category should belong to tenant1");

                // Create category with same name for tenant2 (should succeed due to tenant isolation)
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId2, categoryName);
                assertNotNull(category2, "Category for tenant2 should be created successfully");
                assertEquals(categoryName, category2.getName(), "Category name should match");
                assertEquals(tenantId2, category2.getTenant().getId(), "Category should belong to tenant2");

                // Verify both categories exist independently
                assertNotEquals(category1.getId(), category2.getId(), 
                    "Categories should have different IDs");

                // Verify tenant1 has exactly one category with this name
                List<ExpenseCategoryEntity> tenant1Categories = 
                    categoryRepository.findByTenantIdOrderByNameAsc(tenantId1);
                long tenant1MatchingCount = tenant1Categories.stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .count();
                assertEquals(1, tenant1MatchingCount, 
                    "Tenant1 should have exactly one category with this name");

                // Verify tenant2 has exactly one category with this name
                List<ExpenseCategoryEntity> tenant2Categories = 
                    categoryRepository.findByTenantIdOrderByNameAsc(tenantId2);
                long tenant2MatchingCount = tenant2Categories.stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .count();
                assertEquals(1, tenant2MatchingCount, 
                    "Tenant2 should have exactly one category with this name");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed with category '" + categoryName + "': " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 14 (variant): Name uniqueness enforced on update
     * 
     * For any category update attempt, if the new name already exists 
     * for another category in the same tenant, the update should be rejected.
     * 
     * **Feature: finance-module, Property 14: Category name uniqueness**
     * **Validates: Requirements 6.2, 8.2**
     */
    @Test
    public void categoryUpdateEnforcesNameUniqueness() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            String tenantSlug = generateTenantSlug();
            String tenantName = generateTenantName();
            String categoryName1 = generateCategoryName();
            String categoryName2 = generateCategoryName();
            
            try {
                // Create a new tenant
                TenantEntity tenant = new TenantEntity(tenantSlug, tenantName);
                tenant = tenantRepository.saveAndFlush(tenant);
                Long tenantId = tenant.getId();

                // Create two categories with different names
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId, categoryName1);
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId, categoryName2);

                assertNotNull(category1, "First category should be created");
                assertNotNull(category2, "Second category should be created");
                assertEquals(categoryName1, category1.getName(), "First category name should match");
                assertEquals(categoryName2, category2.getName(), "Second category name should match");

                // Attempt to update category2's name to match category1's name (should fail)
                Long category2Id = category2.getId();
                IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(tenantId, category2Id, categoryName1),
                    "Updating to duplicate category name should throw IllegalArgumentException"
                );

                // Verify error message mentions duplicate
                String errorMessage = exception.getMessage().toLowerCase();
                assertTrue(
                    errorMessage.contains("already exists") || errorMessage.contains("duplicate"),
                    "Error message should indicate duplicate name: " + exception.getMessage()
                );

                // Verify category2's name remains unchanged
                ExpenseCategoryEntity unchangedCategory2 = categoryRepository.findByTenantIdAndId(tenantId, category2Id)
                    .orElseThrow();
                assertEquals(categoryName2, unchangedCategory2.getName(), 
                    "Category2 name should remain unchanged after failed update");

                // Verify updating to the same name (no change) is allowed
                ExpenseCategoryEntity updatedCategory1 = categoryService.updateCategory(
                    tenantId, category1.getId(), categoryName1);
                assertEquals(categoryName1, updatedCategory1.getName(), 
                    "Updating to same name should succeed");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed: " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 14 (variant): Case-sensitive name uniqueness
     * 
     * For any category name, the uniqueness check should be case-sensitive,
     * allowing "Supplies" and "supplies" as different categories.
     * 
     * **Feature: finance-module, Property 14: Category name uniqueness**
     * **Validates: Requirements 6.2**
     */
    @Test
    public void categoryNameUniquenessIsCaseSensitive() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            String tenantSlug = generateTenantSlug();
            String tenantName = generateTenantName();
            String baseName = generateCategoryName();
            String upperCaseName = baseName.toUpperCase();
            String lowerCaseName = baseName.toLowerCase();
            
            // Skip if base name is already all uppercase or all lowercase
            if (baseName.equals(upperCaseName) || baseName.equals(lowerCaseName)) {
                continue;
            }
            
            try {
                // Create a new tenant
                TenantEntity tenant = new TenantEntity(tenantSlug, tenantName);
                tenant = tenantRepository.saveAndFlush(tenant);
                Long tenantId = tenant.getId();

                // Create category with original case
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId, baseName);
                assertNotNull(category1, "Category with original case should be created");
                assertEquals(baseName, category1.getName(), "Category name should match original case");

                // Create category with uppercase version (should succeed - case sensitive)
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId, upperCaseName);
                assertNotNull(category2, "Category with uppercase should be created");
                assertEquals(upperCaseName, category2.getName(), "Category name should match uppercase");

                // Create category with lowercase version (should succeed - case sensitive)
                ExpenseCategoryEntity category3 = categoryService.createCategory(tenantId, lowerCaseName);
                assertNotNull(category3, "Category with lowercase should be created");
                assertEquals(lowerCaseName, category3.getName(), "Category name should match lowercase");

                // Verify all three categories exist
                List<ExpenseCategoryEntity> categories = categoryRepository.findByTenantIdOrderByNameAsc(tenantId);
                assertTrue(categories.size() >= 3, "Should have at least 3 categories");

                // Verify each case variant exists
                assertTrue(categories.stream().anyMatch(c -> c.getName().equals(baseName)),
                    "Should have category with original case");
                assertTrue(categories.stream().anyMatch(c -> c.getName().equals(upperCaseName)),
                    "Should have category with uppercase");
                assertTrue(categories.stream().anyMatch(c -> c.getName().equals(lowerCaseName)),
                    "Should have category with lowercase");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed with base name '" + baseName + "': " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 14 (variant): Whitespace trimming in name uniqueness
     * 
     * For any category name with leading/trailing whitespace, the system 
     * should trim the whitespace and enforce uniqueness on the trimmed name.
     * 
     * **Feature: finance-module, Property 14: Category name uniqueness**
     * **Validates: Requirements 6.2**
     */
    @Test
    public void categoryNameUniquenessTrimsWhitespace() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            String tenantSlug = generateTenantSlug();
            String tenantName = generateTenantName();
            String baseName = generateCategoryName();
            String nameWithLeadingSpace = "  " + baseName;
            String nameWithTrailingSpace = baseName + "  ";
            String nameWithBothSpaces = "  " + baseName + "  ";
            
            try {
                // Create a new tenant
                TenantEntity tenant = new TenantEntity(tenantSlug, tenantName);
                tenant = tenantRepository.saveAndFlush(tenant);
                Long tenantId = tenant.getId();

                // Create category with base name
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId, baseName);
                assertNotNull(category1, "Category should be created");
                assertEquals(baseName, category1.getName(), "Category name should be trimmed");

                // Attempt to create with leading space (should fail - trimmed to same name)
                IllegalArgumentException exception1 = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(tenantId, nameWithLeadingSpace),
                    "Creating with leading space should fail (duplicate after trim)"
                );
                assertTrue(exception1.getMessage().toLowerCase().contains("already exists"),
                    "Error should indicate duplicate");

                // Attempt to create with trailing space (should fail - trimmed to same name)
                IllegalArgumentException exception2 = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(tenantId, nameWithTrailingSpace),
                    "Creating with trailing space should fail (duplicate after trim)"
                );
                assertTrue(exception2.getMessage().toLowerCase().contains("already exists"),
                    "Error should indicate duplicate");

                // Attempt to create with both spaces (should fail - trimmed to same name)
                IllegalArgumentException exception3 = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(tenantId, nameWithBothSpaces),
                    "Creating with both spaces should fail (duplicate after trim)"
                );
                assertTrue(exception3.getMessage().toLowerCase().contains("already exists"),
                    "Error should indicate duplicate");

                // Verify only one category exists
                List<ExpenseCategoryEntity> categories = categoryRepository.findByTenantIdOrderByNameAsc(tenantId);
                long matchingCount = categories.stream()
                    .filter(c -> c.getName().equals(baseName))
                    .count();
                assertEquals(1, matchingCount, "Should have exactly one category with this name");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed with base name '" + baseName + "': " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    // ========== Helper Methods for Generating Random Test Data ==========

    private String generateTenantSlug() {
        return "tenant-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateTenantName() {
        return "Tenant " + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateCategoryName() {
        String prefix = categoryPrefixes[random.nextInt(categoryPrefixes.length)];
        String suffix = categorySuffixes[random.nextInt(categorySuffixes.length)];
        int number = random.nextInt(999) + 1;
        return prefix + " " + suffix + " " + number;
    }
}
