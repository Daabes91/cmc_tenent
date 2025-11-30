package com.clinic.modules.core.finance;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for required field validation in expense creation.
 * 
 * **Feature: finance-module, Property 1: Required field validation**
 * **Validates: Requirements 1.2**
 * 
 * This test verifies that:
 * 1. For any expense creation attempt with missing date, the system rejects it
 * 2. For any expense creation attempt with missing category, the system rejects it
 * 3. For any expense creation attempt with missing amount, the system rejects it
 * 4. For any expense creation attempt with all required fields, it succeeds
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations) since jqwik
 * doesn't integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RequiredFieldValidationPropertyTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseCategoryService categoryService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private final Random random = new Random();

    /**
     * Property 1: Required field validation
     * 
     * For any expense creation attempt with missing date, category, or amount fields,
     * the system should reject the request with a validation error.
     * 
     * **Feature: finance-module, Property 1: Required field validation**
     * **Validates: Requirements 1.2**
     */
    @Test
    public void expenseCreationRejectsMissingRequiredFields() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and category for testing
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                // Create a test category
                ExpenseCategoryEntity category = categoryService.createCategory(tenantId, "Test Category " + UUID.randomUUID());
                Long categoryId = category.getId();
                
                // Generate random valid values
                BigDecimal validAmount = generateRandomAmount();
                LocalDate validDate = generateRandomDate();
                String validNotes = generateRandomNotes();
                
                // Test 1: Missing category (null categoryId)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, null, validAmount, validDate, validNotes);
                }, "Should reject expense with null categoryId");
                
                // Test 2: Missing amount (null amount)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, categoryId, null, validDate, validNotes);
                }, "Should reject expense with null amount");
                
                // Test 3: Missing date (null expenseDate)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, categoryId, validAmount, null, validNotes);
                }, "Should reject expense with null expenseDate");
                
                // Test 4: Missing multiple fields (category and amount)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, null, null, validDate, validNotes);
                }, "Should reject expense with null categoryId and amount");
                
                // Test 5: Missing multiple fields (category and date)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, null, validAmount, null, validNotes);
                }, "Should reject expense with null categoryId and date");
                
                // Test 6: Missing multiple fields (amount and date)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, categoryId, null, null, validNotes);
                }, "Should reject expense with null amount and date");
                
                // Test 7: Missing all required fields
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, null, null, null, validNotes);
                }, "Should reject expense with all null required fields");
                
                // Test 8: Valid expense with all required fields (should succeed)
                ExpenseEntity validExpense = expenseService.createExpense(
                    tenantId, categoryId, validAmount, validDate, validNotes);
                
                assertNotNull(validExpense, "Valid expense should be created");
                assertNotNull(validExpense.getId(), "Valid expense should have an ID");
                assertEquals(categoryId, validExpense.getCategory().getId(), 
                    "Valid expense should have correct category");
                assertEquals(validAmount, validExpense.getAmount(), 
                    "Valid expense should have correct amount");
                assertEquals(validDate, validExpense.getExpenseDate(), 
                    "Valid expense should have correct date");
                
                // Test 9: Valid expense with null notes (notes are optional)
                ExpenseEntity expenseWithoutNotes = expenseService.createExpense(
                    tenantId, categoryId, validAmount, validDate, null);
                
                assertNotNull(expenseWithoutNotes, "Expense without notes should be created");
                assertNull(expenseWithoutNotes.getNotes(), "Notes should be null");
                
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
     * Property 1 (variant): Zero and negative amounts are rejected
     * 
     * For any expense creation attempt with zero or negative amount,
     * the system should reject the request with a validation error.
     * 
     * **Feature: finance-module, Property 1: Required field validation**
     * **Validates: Requirements 1.2**
     */
    @Test
    public void expenseCreationRejectsInvalidAmounts() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and category for testing
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                // Create a test category
                ExpenseCategoryEntity category = categoryService.createCategory(tenantId, "Test Category " + UUID.randomUUID());
                Long categoryId = category.getId();
                
                // Generate random valid values
                LocalDate validDate = generateRandomDate();
                String validNotes = generateRandomNotes();
                
                // Test 1: Zero amount
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, categoryId, BigDecimal.ZERO, validDate, validNotes);
                }, "Should reject expense with zero amount");
                
                // Test 2: Negative amount
                BigDecimal negativeAmount = generateRandomAmount().negate();
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, categoryId, negativeAmount, validDate, validNotes);
                }, "Should reject expense with negative amount");
                
                // Test 3: Very small negative amount
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, categoryId, new BigDecimal("-0.01"), validDate, validNotes);
                }, "Should reject expense with small negative amount");
                
                // Test 4: Positive amount (should succeed)
                BigDecimal positiveAmount = generateRandomAmount();
                ExpenseEntity validExpense = expenseService.createExpense(
                    tenantId, categoryId, positiveAmount, validDate, validNotes);
                
                assertNotNull(validExpense, "Expense with positive amount should be created");
                assertTrue(validExpense.getAmount().compareTo(BigDecimal.ZERO) > 0,
                    "Expense amount should be positive");
                
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
     * Property 1 (variant): Inactive categories are rejected
     * 
     * For any expense creation attempt with an inactive category,
     * the system should reject the request with a validation error.
     * 
     * **Feature: finance-module, Property 1: Required field validation**
     * **Validates: Requirements 1.2**
     */
    @Test
    public void expenseCreationRejectsInactiveCategories() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and category for testing
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                // Create a test category
                ExpenseCategoryEntity category = categoryService.createCategory(tenantId, "Test Category " + UUID.randomUUID());
                Long categoryId = category.getId();
                
                // Disable the category
                categoryService.toggleActive(tenantId, categoryId);
                
                // Generate random valid values
                BigDecimal validAmount = generateRandomAmount();
                LocalDate validDate = generateRandomDate();
                String validNotes = generateRandomNotes();
                
                // Test: Attempt to create expense with inactive category
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId, categoryId, validAmount, validDate, validNotes);
                }, "Should reject expense with inactive category");
                
                // Re-enable the category
                categoryService.toggleActive(tenantId, categoryId);
                
                // Test: Expense creation should now succeed
                ExpenseEntity validExpense = expenseService.createExpense(
                    tenantId, categoryId, validAmount, validDate, validNotes);
                
                assertNotNull(validExpense, "Expense with active category should be created");
                assertTrue(validExpense.getCategory().getIsActive(),
                    "Expense category should be active");
                
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
     * Property 1 (variant): Category from different tenant is rejected
     * 
     * For any expense creation attempt with a category from a different tenant,
     * the system should reject the request with a validation error.
     * 
     * **Feature: finance-module, Property 1: Required field validation**
     * **Validates: Requirements 1.2**
     */
    @Test
    public void expenseCreationRejectsCategoryFromDifferentTenant() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create two different tenants
                TenantEntity tenant1 = createTestTenant();
                TenantEntity tenant2 = createTestTenant();
                Long tenantId1 = tenant1.getId();
                Long tenantId2 = tenant2.getId();
                
                // Create a category for tenant2
                ExpenseCategoryEntity category = categoryService.createCategory(tenantId2, "Test Category " + UUID.randomUUID());
                Long categoryId = category.getId();
                
                // Generate random valid values
                BigDecimal validAmount = generateRandomAmount();
                LocalDate validDate = generateRandomDate();
                String validNotes = generateRandomNotes();
                
                // Test: Attempt to create expense for tenant1 with tenant2's category
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.createExpense(tenantId1, categoryId, validAmount, validDate, validNotes);
                }, "Should reject expense with category from different tenant");
                
                // Test: Expense creation should succeed with correct tenant
                ExpenseEntity validExpense = expenseService.createExpense(
                    tenantId2, categoryId, validAmount, validDate, validNotes);
                
                assertNotNull(validExpense, "Expense with correct tenant should be created");
                assertEquals(tenantId2, validExpense.getTenant().getId(),
                    "Expense should belong to correct tenant");
                
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

    private TenantEntity createTestTenant() {
        String slug = "tenant-" + UUID.randomUUID().toString().substring(0, 8);
        String name = "Test Tenant " + UUID.randomUUID().toString().substring(0, 8);
        TenantEntity tenant = new TenantEntity(slug, name);
        return tenantRepository.saveAndFlush(tenant);
    }

    private BigDecimal generateRandomAmount() {
        // Generate random amount between 0.01 and 99999.99
        double amount = 0.01 + (random.nextDouble() * 99999.98);
        return BigDecimal.valueOf(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private LocalDate generateRandomDate() {
        // Generate random date within last 2 years
        int daysAgo = random.nextInt(730); // 0 to 730 days ago (2 years)
        return LocalDate.now().minusDays(daysAgo);
    }

    private String generateRandomNotes() {
        if (random.nextBoolean()) {
            return null; // 50% chance of null notes
        }
        String[] noteTemplates = {
            "Payment for services",
            "Monthly expense",
            "Quarterly payment",
            "Annual subscription",
            "One-time purchase",
            "Recurring charge",
            "Emergency expense",
            "Planned expenditure"
        };
        return noteTemplates[random.nextInt(noteTemplates.length)] + " - " + UUID.randomUUID().toString().substring(0, 8);
    }
}
