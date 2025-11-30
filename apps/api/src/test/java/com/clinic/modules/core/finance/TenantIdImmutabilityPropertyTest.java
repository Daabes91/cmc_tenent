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
 * Property-based test for tenant ID immutability in expense updates.
 * 
 * **Feature: finance-module, Property 8: Tenant ID immutability**
 * **Validates: Requirements 3.4**
 * 
 * This test verifies that:
 * 1. For any expense update, the tenant_id never changes
 * 2. For any expense, the tenant_id remains the same after multiple updates
 * 3. The system maintains tenant_id immutability even when other fields change
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations) since jqwik
 * doesn't integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantIdImmutabilityPropertyTest {

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
     * Property 8: Tenant ID immutability
     * 
     * For any expense update, the tenant_id should never change from its original value.
     * 
     * **Feature: finance-module, Property 8: Tenant ID immutability**
     * **Validates: Requirements 3.4**
     */
    @Test
    public void tenantIdRemainsUnchangedAfterUpdate() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and categories
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId, "Category1 " + UUID.randomUUID());
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId, "Category2 " + UUID.randomUUID());
                
                // Create an expense
                BigDecimal originalAmount = generateRandomAmount();
                LocalDate originalDate = generateRandomDate();
                String originalNotes = generateRandomNotes();
                
                ExpenseEntity expense = expenseService.createExpense(
                    tenantId, category1.getId(), originalAmount, originalDate, originalNotes);
                
                Long expenseId = expense.getId();
                Long originalTenantId = expense.getTenant().getId();
                
                // Verify original tenant_id
                assertEquals(tenantId, originalTenantId, "Expense should have correct initial tenant_id");
                
                // Update expense with different values
                BigDecimal newAmount = generateRandomAmount();
                LocalDate newDate = generateRandomDate();
                String newNotes = generateRandomNotes();
                
                ExpenseEntity updatedExpense = expenseService.updateExpense(
                    tenantId, expenseId, category2.getId(), newAmount, newDate, newNotes);
                
                // Verify tenant_id has not changed
                assertEquals(originalTenantId, updatedExpense.getTenant().getId(),
                    "Tenant ID should remain unchanged after update");
                
                // Retrieve expense from database and verify tenant_id
                ExpenseEntity retrievedExpense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new AssertionError("Expense should exist in database"));
                
                assertEquals(originalTenantId, retrievedExpense.getTenant().getId(),
                    "Tenant ID should remain unchanged in database");
                
                // Verify other fields were updated
                assertEquals(newAmount, updatedExpense.getAmount(), "Amount should be updated");
                assertEquals(newDate, updatedExpense.getExpenseDate(), "Date should be updated");
                assertEquals(category2.getId(), updatedExpense.getCategory().getId(), "Category should be updated");
                
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
     * Property 8 (variant): Tenant ID immutability across multiple updates
     * 
     * For any expense, the tenant_id should remain the same after multiple 
     * consecutive updates.
     * 
     * **Feature: finance-module, Property 8: Tenant ID immutability**
     * **Validates: Requirements 3.4**
     */
    @Test
    public void tenantIdRemainsUnchangedAfterMultipleUpdates() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and categories
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                // Create multiple categories for variety
                List<ExpenseCategoryEntity> categories = new ArrayList<>();
                for (int j = 0; j < 5; j++) {
                    categories.add(categoryService.createCategory(tenantId, "Category" + j + " " + UUID.randomUUID()));
                }
                
                // Create an expense
                ExpenseEntity expense = expenseService.createExpense(
                    tenantId, 
                    categories.get(0).getId(), 
                    generateRandomAmount(), 
                    generateRandomDate(), 
                    generateRandomNotes()
                );
                
                Long expenseId = expense.getId();
                Long originalTenantId = expense.getTenant().getId();
                
                // Perform multiple updates (3-7 updates)
                int updateCount = random.nextInt(5) + 3;
                
                for (int j = 0; j < updateCount; j++) {
                    // Update with random values
                    ExpenseCategoryEntity randomCategory = categories.get(random.nextInt(categories.size()));
                    
                    ExpenseEntity updatedExpense = expenseService.updateExpense(
                        tenantId,
                        expenseId,
                        randomCategory.getId(),
                        generateRandomAmount(),
                        generateRandomDate(),
                        generateRandomNotes()
                    );
                    
                    // Verify tenant_id has not changed
                    assertEquals(originalTenantId, updatedExpense.getTenant().getId(),
                        "Tenant ID should remain unchanged after update " + (j + 1));
                }
                
                // Final verification from database
                ExpenseEntity finalExpense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new AssertionError("Expense should exist in database"));
                
                assertEquals(originalTenantId, finalExpense.getTenant().getId(),
                    "Tenant ID should remain unchanged after " + updateCount + " updates");
                
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
     * Property 8 (variant): Tenant ID immutability with minimal updates
     * 
     * For any expense, even when updating only one field, the tenant_id 
     * should remain unchanged.
     * 
     * **Feature: finance-module, Property 8: Tenant ID immutability**
     * **Validates: Requirements 3.4**
     */
    @Test
    public void tenantIdRemainsUnchangedWithMinimalUpdates() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and category
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                ExpenseCategoryEntity category = categoryService.createCategory(tenantId, "Category " + UUID.randomUUID());
                Long categoryId = category.getId();
                
                // Create an expense
                BigDecimal originalAmount = generateRandomAmount();
                LocalDate originalDate = generateRandomDate();
                String originalNotes = generateRandomNotes();
                
                ExpenseEntity expense = expenseService.createExpense(
                    tenantId, categoryId, originalAmount, originalDate, originalNotes);
                
                Long expenseId = expense.getId();
                Long originalTenantId = expense.getTenant().getId();
                
                // Test 1: Update only amount
                BigDecimal newAmount = generateRandomAmount();
                ExpenseEntity updatedExpense1 = expenseService.updateExpense(
                    tenantId, expenseId, categoryId, newAmount, originalDate, originalNotes);
                
                assertEquals(originalTenantId, updatedExpense1.getTenant().getId(),
                    "Tenant ID should remain unchanged when updating only amount");
                assertEquals(newAmount, updatedExpense1.getAmount(), "Amount should be updated");
                
                // Test 2: Update only date
                LocalDate newDate = generateRandomDate();
                ExpenseEntity updatedExpense2 = expenseService.updateExpense(
                    tenantId, expenseId, categoryId, newAmount, newDate, originalNotes);
                
                assertEquals(originalTenantId, updatedExpense2.getTenant().getId(),
                    "Tenant ID should remain unchanged when updating only date");
                assertEquals(newDate, updatedExpense2.getExpenseDate(), "Date should be updated");
                
                // Test 3: Update only notes
                String newNotes = generateRandomNotes();
                ExpenseEntity updatedExpense3 = expenseService.updateExpense(
                    tenantId, expenseId, categoryId, newAmount, newDate, newNotes);
                
                assertEquals(originalTenantId, updatedExpense3.getTenant().getId(),
                    "Tenant ID should remain unchanged when updating only notes");
                assertEquals(newNotes, updatedExpense3.getNotes(), "Notes should be updated");
                
                // Final verification from database
                ExpenseEntity finalExpense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new AssertionError("Expense should exist in database"));
                
                assertEquals(originalTenantId, finalExpense.getTenant().getId(),
                    "Tenant ID should remain unchanged after all updates");
                
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
     * Property 8 (variant): Tenant ID immutability verification at entity level
     * 
     * For any expense entity, the tenant reference should remain the same 
     * object after updates.
     * 
     * **Feature: finance-module, Property 8: Tenant ID immutability**
     * **Validates: Requirements 3.4**
     */
    @Test
    public void tenantReferenceRemainsUnchangedAfterUpdate() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and category
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId, "Category1 " + UUID.randomUUID());
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId, "Category2 " + UUID.randomUUID());
                
                // Create an expense
                ExpenseEntity expense = expenseService.createExpense(
                    tenantId, 
                    category1.getId(), 
                    generateRandomAmount(), 
                    generateRandomDate(), 
                    generateRandomNotes()
                );
                
                Long expenseId = expense.getId();
                
                // Get the tenant entity reference before update
                ExpenseEntity beforeUpdate = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new AssertionError("Expense should exist"));
                TenantEntity tenantBeforeUpdate = beforeUpdate.getTenant();
                Long tenantIdBeforeUpdate = tenantBeforeUpdate.getId();
                
                // Update the expense
                expenseService.updateExpense(
                    tenantId,
                    expenseId,
                    category2.getId(),
                    generateRandomAmount(),
                    generateRandomDate(),
                    generateRandomNotes()
                );
                
                // Get the tenant entity reference after update
                ExpenseEntity afterUpdate = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new AssertionError("Expense should exist"));
                TenantEntity tenantAfterUpdate = afterUpdate.getTenant();
                Long tenantIdAfterUpdate = tenantAfterUpdate.getId();
                
                // Verify tenant ID is the same
                assertEquals(tenantIdBeforeUpdate, tenantIdAfterUpdate,
                    "Tenant ID should be identical before and after update");
                
                // Verify it's the same tenant entity
                assertEquals(tenantBeforeUpdate.getId(), tenantAfterUpdate.getId(),
                    "Tenant entity should be the same before and after update");
                
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
