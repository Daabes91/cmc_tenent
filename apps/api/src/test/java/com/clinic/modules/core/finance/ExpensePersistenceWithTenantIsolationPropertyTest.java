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
 * Property-based test for expense persistence with tenant isolation.
 * 
 * **Feature: finance-module, Property 2: Expense persistence with tenant isolation**
 * **Validates: Requirements 1.3, 1.4, 1.5**
 * 
 * This test verifies that:
 * 1. For any valid expense, it is persisted with the correct tenant_id
 * 2. For any tenant, queries only return expenses for that tenant
 * 3. For any expense, it is associated with the authenticated tenant
 * 4. Tenant isolation is maintained across multiple tenants
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations) since jqwik
 * doesn't integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ExpensePersistenceWithTenantIsolationPropertyTest {

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
     * Property 2: Expense persistence with tenant isolation
     * 
     * For any valid expense submitted by an authenticated user, the expense should be 
     * persisted to the database with the correct tenant ID and be retrievable only by that tenant.
     * 
     * **Feature: finance-module, Property 2: Expense persistence with tenant isolation**
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    @Test
    public void expensePersistenceWithCorrectTenantId() {
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
                
                // Generate random valid expense data
                BigDecimal amount = generateRandomAmount();
                LocalDate date = generateRandomDate();
                String notes = generateRandomNotes();
                
                // Create expense
                ExpenseEntity expense = expenseService.createExpense(tenantId, categoryId, amount, date, notes);
                
                // Verify expense was persisted
                assertNotNull(expense, "Expense should be persisted");
                assertNotNull(expense.getId(), "Expense should have an ID");
                
                // Verify expense has correct tenant_id
                assertNotNull(expense.getTenant(), "Expense should have a tenant");
                assertEquals(tenantId, expense.getTenant().getId(), 
                    "Expense should have correct tenant_id");
                
                // Verify expense can be retrieved by tenant
                List<ExpenseEntity> expenses = expenseService.getExpenses(tenantId, null, null, null);
                assertTrue(expenses.stream().anyMatch(e -> e.getId().equals(expense.getId())),
                    "Expense should be retrievable by tenant");
                
                // Verify all retrieved expenses belong to the tenant
                for (ExpenseEntity e : expenses) {
                    assertEquals(tenantId, e.getTenant().getId(),
                        "All retrieved expenses should belong to the tenant");
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

    /**
     * Property 2 (variant): Tenant isolation across multiple tenants
     * 
     * For any two different tenants, expenses created for one tenant should not be 
     * retrievable by the other tenant.
     * 
     * **Feature: finance-module, Property 2: Expense persistence with tenant isolation**
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    @Test
    public void expenseQueriesRespectTenantIsolation() {
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
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId1, "Category " + UUID.randomUUID());
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId2, "Category " + UUID.randomUUID());
                
                // Create random number of expenses for tenant1 (1-5)
                int expenseCount1 = random.nextInt(5) + 1;
                List<Long> tenant1ExpenseIds = new ArrayList<>();
                for (int j = 0; j < expenseCount1; j++) {
                    ExpenseEntity expense = expenseService.createExpense(
                        tenantId1, 
                        category1.getId(), 
                        generateRandomAmount(), 
                        generateRandomDate(), 
                        generateRandomNotes()
                    );
                    tenant1ExpenseIds.add(expense.getId());
                }
                
                // Create random number of expenses for tenant2 (1-5)
                int expenseCount2 = random.nextInt(5) + 1;
                List<Long> tenant2ExpenseIds = new ArrayList<>();
                for (int j = 0; j < expenseCount2; j++) {
                    ExpenseEntity expense = expenseService.createExpense(
                        tenantId2, 
                        category2.getId(), 
                        generateRandomAmount(), 
                        generateRandomDate(), 
                        generateRandomNotes()
                    );
                    tenant2ExpenseIds.add(expense.getId());
                }
                
                // Query expenses for tenant1
                List<ExpenseEntity> tenant1Expenses = expenseService.getExpenses(tenantId1, null, null, null);
                
                // Verify tenant1 gets only their expenses
                assertEquals(expenseCount1, tenant1Expenses.size(),
                    "Tenant1 should retrieve exactly their expenses");
                
                for (ExpenseEntity expense : tenant1Expenses) {
                    assertEquals(tenantId1, expense.getTenant().getId(),
                        "All tenant1 expenses should belong to tenant1");
                    assertTrue(tenant1ExpenseIds.contains(expense.getId()),
                        "Tenant1 should only see their own expenses");
                    assertFalse(tenant2ExpenseIds.contains(expense.getId()),
                        "Tenant1 should not see tenant2 expenses");
                }
                
                // Query expenses for tenant2
                List<ExpenseEntity> tenant2Expenses = expenseService.getExpenses(tenantId2, null, null, null);
                
                // Verify tenant2 gets only their expenses
                assertEquals(expenseCount2, tenant2Expenses.size(),
                    "Tenant2 should retrieve exactly their expenses");
                
                for (ExpenseEntity expense : tenant2Expenses) {
                    assertEquals(tenantId2, expense.getTenant().getId(),
                        "All tenant2 expenses should belong to tenant2");
                    assertTrue(tenant2ExpenseIds.contains(expense.getId()),
                        "Tenant2 should only see their own expenses");
                    assertFalse(tenant1ExpenseIds.contains(expense.getId()),
                        "Tenant2 should not see tenant1 expenses");
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

    /**
     * Property 2 (variant): Expense filtering respects tenant isolation
     * 
     * For any tenant, filtered queries (by date range or category) should only 
     * return expenses belonging to that tenant.
     * 
     * **Feature: finance-module, Property 2: Expense persistence with tenant isolation**
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    @Test
    public void expenseFilteringRespectsTenantIsolation() {
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
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId1, "Category " + UUID.randomUUID());
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId2, "Category " + UUID.randomUUID());
                
                // Create expenses for both tenants with specific dates
                LocalDate startDate = LocalDate.now().minusDays(30);
                LocalDate endDate = LocalDate.now();
                LocalDate midDate = startDate.plusDays(15);
                
                // Tenant1 expenses
                ExpenseEntity tenant1Expense1 = expenseService.createExpense(
                    tenantId1, category1.getId(), generateRandomAmount(), midDate, "Tenant1 Expense1");
                ExpenseEntity tenant1Expense2 = expenseService.createExpense(
                    tenantId1, category1.getId(), generateRandomAmount(), midDate.plusDays(1), "Tenant1 Expense2");
                
                // Tenant2 expenses
                ExpenseEntity tenant2Expense1 = expenseService.createExpense(
                    tenantId2, category2.getId(), generateRandomAmount(), midDate, "Tenant2 Expense1");
                ExpenseEntity tenant2Expense2 = expenseService.createExpense(
                    tenantId2, category2.getId(), generateRandomAmount(), midDate.plusDays(1), "Tenant2 Expense2");
                
                // Test 1: Date range filter for tenant1
                List<ExpenseEntity> tenant1FilteredByDate = expenseService.getExpenses(
                    tenantId1, startDate, endDate, null);
                
                assertTrue(tenant1FilteredByDate.size() >= 2, 
                    "Tenant1 should have at least 2 expenses in date range");
                for (ExpenseEntity expense : tenant1FilteredByDate) {
                    assertEquals(tenantId1, expense.getTenant().getId(),
                        "Filtered expenses should belong to tenant1");
                }
                
                // Test 2: Category filter for tenant1
                List<ExpenseEntity> tenant1FilteredByCategory = expenseService.getExpenses(
                    tenantId1, null, null, category1.getId());
                
                assertTrue(tenant1FilteredByCategory.size() >= 2,
                    "Tenant1 should have at least 2 expenses in category");
                for (ExpenseEntity expense : tenant1FilteredByCategory) {
                    assertEquals(tenantId1, expense.getTenant().getId(),
                        "Filtered expenses should belong to tenant1");
                    assertEquals(category1.getId(), expense.getCategory().getId(),
                        "Filtered expenses should have correct category");
                }
                
                // Test 3: Combined filters for tenant1
                List<ExpenseEntity> tenant1FilteredBoth = expenseService.getExpenses(
                    tenantId1, startDate, endDate, category1.getId());
                
                for (ExpenseEntity expense : tenant1FilteredBoth) {
                    assertEquals(tenantId1, expense.getTenant().getId(),
                        "Filtered expenses should belong to tenant1");
                    assertEquals(category1.getId(), expense.getCategory().getId(),
                        "Filtered expenses should have correct category");
                    assertTrue(!expense.getExpenseDate().isBefore(startDate) && 
                              !expense.getExpenseDate().isAfter(endDate),
                        "Filtered expenses should be within date range");
                }
                
                // Test 4: Verify tenant2 cannot see tenant1 expenses even with same filters
                List<ExpenseEntity> tenant2FilteredByDate = expenseService.getExpenses(
                    tenantId2, startDate, endDate, null);
                
                for (ExpenseEntity expense : tenant2FilteredByDate) {
                    assertEquals(tenantId2, expense.getTenant().getId(),
                        "Tenant2 filtered expenses should belong to tenant2");
                    assertNotEquals(tenant1Expense1.getId(), expense.getId(),
                        "Tenant2 should not see tenant1 expenses");
                    assertNotEquals(tenant1Expense2.getId(), expense.getId(),
                        "Tenant2 should not see tenant1 expenses");
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

    /**
     * Property 2 (variant): Multiple expenses for same tenant
     * 
     * For any tenant, creating multiple expenses should persist all of them 
     * with the correct tenant_id and all should be retrievable.
     * 
     * **Feature: finance-module, Property 2: Expense persistence with tenant isolation**
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    @Test
    public void multipleExpensesForSameTenantArePersisted() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and category
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                ExpenseCategoryEntity category = categoryService.createCategory(tenantId, "Category " + UUID.randomUUID());
                Long categoryId = category.getId();
                
                // Create random number of expenses (5-20)
                int expenseCount = random.nextInt(16) + 5;
                List<Long> createdExpenseIds = new ArrayList<>();
                
                for (int j = 0; j < expenseCount; j++) {
                    ExpenseEntity expense = expenseService.createExpense(
                        tenantId,
                        categoryId,
                        generateRandomAmount(),
                        generateRandomDate(),
                        generateRandomNotes()
                    );
                    createdExpenseIds.add(expense.getId());
                    
                    // Verify each expense has correct tenant_id
                    assertEquals(tenantId, expense.getTenant().getId(),
                        "Each expense should have correct tenant_id");
                }
                
                // Query all expenses for tenant
                List<ExpenseEntity> allExpenses = expenseService.getExpenses(tenantId, null, null, null);
                
                // Verify all expenses were persisted
                assertEquals(expenseCount, allExpenses.size(),
                    "All expenses should be persisted and retrievable");
                
                // Verify all created expenses are in the result
                for (Long expenseId : createdExpenseIds) {
                    assertTrue(allExpenses.stream().anyMatch(e -> e.getId().equals(expenseId)),
                        "All created expenses should be retrievable");
                }
                
                // Verify all expenses belong to the tenant
                for (ExpenseEntity expense : allExpenses) {
                    assertEquals(tenantId, expense.getTenant().getId(),
                        "All expenses should belong to the tenant");
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
