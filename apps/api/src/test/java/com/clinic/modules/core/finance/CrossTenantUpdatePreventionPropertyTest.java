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
 * Property-based test for cross-tenant update prevention.
 * 
 * **Feature: finance-module, Property 33: Cross-tenant update prevention**
 * **Validates: Requirements 12.4**
 * 
 * This test verifies that:
 * 1. For any expense, updates from a different tenant are rejected
 * 2. For any expense, only the owning tenant can update it
 * 3. Cross-tenant update attempts result in appropriate errors
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations) since jqwik
 * doesn't integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CrossTenantUpdatePreventionPropertyTest {

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
     * Property 33: Cross-tenant update prevention
     * 
     * For any expense belonging to tenant A, update attempts from tenant B 
     * should be rejected with an error.
     * 
     * **Feature: finance-module, Property 33: Cross-tenant update prevention**
     * **Validates: Requirements 12.4**
     */
    @Test
    public void crossTenantUpdateAttemptsAreRejected() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create two different tenants
                TenantEntity tenant1 = createTestTenant();
                TenantEntity tenant2 = createTestTenant();
                Long tenantId1 = tenant1.getId();
                Long tenantId2 = tenant2.getId();
                
                // Create categories for both tenants
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId1, "Category1 " + UUID.randomUUID());
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId2, "Category2 " + UUID.randomUUID());
                
                // Create an expense for tenant1
                ExpenseEntity expense = expenseService.createExpense(
                    tenantId1,
                    category1.getId(),
                    generateRandomAmount(),
                    generateRandomDate(),
                    generateRandomNotes()
                );
                
                Long expenseId = expense.getId();
                Long originalTenantId = expense.getTenant().getId();
                
                // Verify expense belongs to tenant1
                assertEquals(tenantId1, originalTenantId, "Expense should belong to tenant1");
                
                // Attempt to update expense from tenant2 (should fail)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.updateExpense(
                        tenantId2,  // Wrong tenant!
                        expenseId,
                        category2.getId(),
                        generateRandomAmount(),
                        generateRandomDate(),
                        generateRandomNotes()
                    );
                }, "Cross-tenant update should be rejected");
                
                // Verify expense still belongs to tenant1 and is unchanged
                ExpenseEntity unchangedExpense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new AssertionError("Expense should still exist"));
                
                assertEquals(tenantId1, unchangedExpense.getTenant().getId(),
                    "Expense should still belong to tenant1 after failed update attempt");
                
                // Verify tenant1 can still update the expense
                BigDecimal newAmount = generateRandomAmount();
                ExpenseEntity updatedExpense = expenseService.updateExpense(
                    tenantId1,  // Correct tenant
                    expenseId,
                    category1.getId(),
                    newAmount,
                    generateRandomDate(),
                    generateRandomNotes()
                );
                
                assertNotNull(updatedExpense, "Tenant1 should be able to update their expense");
                assertEquals(tenantId1, updatedExpense.getTenant().getId(),
                    "Expense should still belong to tenant1");
                assertEquals(newAmount, updatedExpense.getAmount(),
                    "Expense should be updated with new amount");
                
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
     * Property 33 (variant): Cross-tenant deletion prevention
     * 
     * For any expense belonging to tenant A, deletion attempts from tenant B 
     * should be rejected with an error.
     * 
     * **Feature: finance-module, Property 33: Cross-tenant update prevention**
     * **Validates: Requirements 12.4**
     */
    @Test
    public void crossTenantDeletionAttemptsAreRejected() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create two different tenants
                TenantEntity tenant1 = createTestTenant();
                TenantEntity tenant2 = createTestTenant();
                Long tenantId1 = tenant1.getId();
                Long tenantId2 = tenant2.getId();
                
                // Create categories for both tenants
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId1, "Category1 " + UUID.randomUUID());
                
                // Create an expense for tenant1
                ExpenseEntity expense = expenseService.createExpense(
                    tenantId1,
                    category1.getId(),
                    generateRandomAmount(),
                    generateRandomDate(),
                    generateRandomNotes()
                );
                
                Long expenseId = expense.getId();
                
                // Attempt to delete expense from tenant2 (should fail)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.deleteExpense(tenantId2, expenseId);
                }, "Cross-tenant deletion should be rejected");
                
                // Verify expense still exists
                assertTrue(expenseRepository.findById(expenseId).isPresent(),
                    "Expense should still exist after failed deletion attempt");
                
                // Verify tenant1 can delete the expense
                expenseService.deleteExpense(tenantId1, expenseId);
                
                // Verify expense is deleted
                assertFalse(expenseRepository.findById(expenseId).isPresent(),
                    "Expense should be deleted by tenant1");
                
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
     * Property 33 (variant): Multiple tenants cannot update same expense
     * 
     * For any expense, only the owning tenant can update it, regardless of 
     * how many other tenants attempt to update it.
     * 
     * **Feature: finance-module, Property 33: Cross-tenant update prevention**
     * **Validates: Requirements 12.4**
     */
    @Test
    public void onlyOwningTenantCanUpdateExpense() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create multiple tenants (3-5)
                int tenantCount = random.nextInt(3) + 3;
                List<TenantEntity> tenants = new ArrayList<>();
                List<ExpenseCategoryEntity> categories = new ArrayList<>();
                
                for (int j = 0; j < tenantCount; j++) {
                    TenantEntity tenant = createTestTenant();
                    tenants.add(tenant);
                    ExpenseCategoryEntity category = categoryService.createCategory(
                        tenant.getId(), "Category " + UUID.randomUUID());
                    categories.add(category);
                }
                
                // Pick a random tenant as the owner
                int ownerIndex = random.nextInt(tenantCount);
                TenantEntity ownerTenant = tenants.get(ownerIndex);
                Long ownerTenantId = ownerTenant.getId();
                ExpenseCategoryEntity ownerCategory = categories.get(ownerIndex);
                
                // Create an expense for the owner tenant
                ExpenseEntity expense = expenseService.createExpense(
                    ownerTenantId,
                    ownerCategory.getId(),
                    generateRandomAmount(),
                    generateRandomDate(),
                    generateRandomNotes()
                );
                
                Long expenseId = expense.getId();
                
                // Attempt to update from all non-owner tenants (should all fail)
                for (int j = 0; j < tenantCount; j++) {
                    if (j != ownerIndex) {
                        Long nonOwnerTenantId = tenants.get(j).getId();
                        Long nonOwnerCategoryId = categories.get(j).getId();
                        
                        assertThrows(IllegalArgumentException.class, () -> {
                            expenseService.updateExpense(
                                nonOwnerTenantId,
                                expenseId,
                                nonOwnerCategoryId,
                                generateRandomAmount(),
                                generateRandomDate(),
                                generateRandomNotes()
                            );
                        }, "Non-owner tenant " + nonOwnerTenantId + " should not be able to update expense");
                    }
                }
                
                // Verify expense still belongs to owner
                ExpenseEntity unchangedExpense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new AssertionError("Expense should still exist"));
                
                assertEquals(ownerTenantId, unchangedExpense.getTenant().getId(),
                    "Expense should still belong to owner tenant");
                
                // Verify owner can still update
                BigDecimal newAmount = generateRandomAmount();
                ExpenseEntity updatedExpense = expenseService.updateExpense(
                    ownerTenantId,
                    expenseId,
                    ownerCategory.getId(),
                    newAmount,
                    generateRandomDate(),
                    generateRandomNotes()
                );
                
                assertNotNull(updatedExpense, "Owner should be able to update expense");
                assertEquals(ownerTenantId, updatedExpense.getTenant().getId(),
                    "Expense should still belong to owner");
                assertEquals(newAmount, updatedExpense.getAmount(),
                    "Expense should be updated");
                
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
     * Property 33 (variant): Cross-tenant update with valid category from wrong tenant
     * 
     * For any expense, even if the update uses a valid category from a different 
     * tenant, the update should be rejected if the tenant ID doesn't match.
     * 
     * **Feature: finance-module, Property 33: Cross-tenant update prevention**
     * **Validates: Requirements 12.4**
     */
    @Test
    public void crossTenantUpdateRejectedEvenWithValidCategory() {
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
                
                // Create categories for both tenants
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId1, "Category1 " + UUID.randomUUID());
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId2, "Category2 " + UUID.randomUUID());
                
                // Create an expense for tenant1
                ExpenseEntity expense = expenseService.createExpense(
                    tenantId1,
                    category1.getId(),
                    generateRandomAmount(),
                    generateRandomDate(),
                    generateRandomNotes()
                );
                
                Long expenseId = expense.getId();
                
                // Attempt to update from tenant2 with tenant2's valid category (should still fail)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.updateExpense(
                        tenantId2,
                        expenseId,
                        category2.getId(),  // Valid category, but for wrong tenant
                        generateRandomAmount(),
                        generateRandomDate(),
                        generateRandomNotes()
                    );
                }, "Cross-tenant update should be rejected even with valid category from that tenant");
                
                // Verify expense is unchanged
                ExpenseEntity unchangedExpense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new AssertionError("Expense should still exist"));
                
                assertEquals(tenantId1, unchangedExpense.getTenant().getId(),
                    "Expense should still belong to tenant1");
                assertEquals(category1.getId(), unchangedExpense.getCategory().getId(),
                    "Expense category should be unchanged");
                
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
     * Property 33 (variant): Non-existent expense ID with wrong tenant
     * 
     * For any non-existent expense ID, update attempts from any tenant 
     * should be rejected with an appropriate error.
     * 
     * **Feature: finance-module, Property 33: Cross-tenant update prevention**
     * **Validates: Requirements 12.4**
     */
    @Test
    public void updateNonExistentExpenseIsRejected() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and category
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                ExpenseCategoryEntity category = categoryService.createCategory(tenantId, "Category " + UUID.randomUUID());
                
                // Generate a non-existent expense ID (very large number)
                Long nonExistentExpenseId = 999999999L + random.nextInt(1000000);
                
                // Attempt to update non-existent expense (should fail)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.updateExpense(
                        tenantId,
                        nonExistentExpenseId,
                        category.getId(),
                        generateRandomAmount(),
                        generateRandomDate(),
                        generateRandomNotes()
                    );
                }, "Update of non-existent expense should be rejected");
                
                // Attempt to delete non-existent expense (should fail)
                assertThrows(IllegalArgumentException.class, () -> {
                    expenseService.deleteExpense(tenantId, nonExistentExpenseId);
                }, "Deletion of non-existent expense should be rejected");
                
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
