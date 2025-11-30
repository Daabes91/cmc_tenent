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
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for tenant boundary enforcement in aggregations.
 * 
 * **Feature: finance-module, Property 30: Universal expense query isolation**
 * **Validates: Requirements 9.5, 12.1**
 * 
 * This test verifies that:
 * 1. For any financial aggregation, only data from the authenticated tenant is included
 * 2. For any two different tenants, aggregations never include data from other tenants
 * 3. For any multi-tenant scenario, tenant boundaries are strictly enforced
 * 4. For any aggregation method, tenant isolation is maintained
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations) since jqwik
 * doesn't integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantBoundaryEnforcementInAggregationsPropertyTest {

    @Autowired
    private FinanceAggregationService financeAggregationService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseCategoryService categoryService;

    @Autowired
    private TenantRepository tenantRepository;

    private final Random random = new Random();

    /**
     * Property 30: Tenant isolation in monthly summaries
     * 
     * For any two different tenants with expenses in the same month, each tenant's
     * monthly summary should only include their own expenses, never the other tenant's.
     * 
     * **Feature: finance-module, Property 30: Universal expense query isolation**
     * **Validates: Requirements 9.5, 12.1**
     */
    @Test
    public void monthlySummariesRespectTenantBoundaries() {
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
                ExpenseCategoryEntity category1 = categoryService.createCategory(
                    tenantId1, 
                    "Category " + UUID.randomUUID().toString().substring(0, 8)
                );
                ExpenseCategoryEntity category2 = categoryService.createCategory(
                    tenantId2, 
                    "Category " + UUID.randomUUID().toString().substring(0, 8)
                );
                
                // Use same year and month for both tenants
                int year = 2024;
                int month = random.nextInt(12) + 1;
                
                // Create random expenses for tenant1
                int expenseCount1 = random.nextInt(10) + 1;
                BigDecimal expectedTotal1 = BigDecimal.ZERO;
                for (int j = 0; j < expenseCount1; j++) {
                    BigDecimal amount = generateRandomAmount();
                    LocalDate date = generateRandomDateInMonth(year, month);
                    expenseService.createExpense(tenantId1, category1.getId(), amount, date, null);
                    expectedTotal1 = expectedTotal1.add(amount);
                }
                
                // Create random expenses for tenant2
                int expenseCount2 = random.nextInt(10) + 1;
                BigDecimal expectedTotal2 = BigDecimal.ZERO;
                for (int j = 0; j < expenseCount2; j++) {
                    BigDecimal amount = generateRandomAmount();
                    LocalDate date = generateRandomDateInMonth(year, month);
                    expenseService.createExpense(tenantId2, category2.getId(), amount, date, null);
                    expectedTotal2 = expectedTotal2.add(amount);
                }
                
                // Get monthly summaries for both tenants
                FinanceAggregationService.MonthlySummary summary1 = 
                    financeAggregationService.getMonthlySummary(tenantId1, year, month);
                FinanceAggregationService.MonthlySummary summary2 = 
                    financeAggregationService.getMonthlySummary(tenantId2, year, month);
                
                // Verify tenant1 summary only includes tenant1 expenses
                assertEquals(0, expectedTotal1.compareTo(summary1.getTotalExpenses()),
                    "Tenant1 summary should only include tenant1 expenses. Expected: " + expectedTotal1 + 
                    ", Actual: " + summary1.getTotalExpenses());
                
                // Verify tenant2 summary only includes tenant2 expenses
                assertEquals(0, expectedTotal2.compareTo(summary2.getTotalExpenses()),
                    "Tenant2 summary should only include tenant2 expenses. Expected: " + expectedTotal2 + 
                    ", Actual: " + summary2.getTotalExpenses());
                
                // Verify totals are different (unless by extreme coincidence they're equal)
                // This ensures we're not accidentally returning the same data for both
                if (expenseCount1 != expenseCount2 || expectedTotal1.compareTo(expectedTotal2) != 0) {
                    assertNotEquals(0, expectedTotal1.compareTo(summary2.getTotalExpenses()),
                        "Tenant1 total should not equal tenant2 summary");
                    assertNotEquals(0, expectedTotal2.compareTo(summary1.getTotalExpenses()),
                        "Tenant2 total should not equal tenant1 summary");
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
     * Property 30 (variant): Tenant isolation in category breakdowns
     * 
     * For any two different tenants with expenses in the same categories (by name),
     * each tenant's category breakdown should only include their own expenses.
     * 
     * **Feature: finance-module, Property 30: Universal expense query isolation**
     * **Validates: Requirements 9.5, 12.1**
     */
    @Test
    public void categoryBreakdownsRespectTenantBoundaries() {
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
                
                // Create categories with same names for both tenants
                String categoryName = "Shared Category " + UUID.randomUUID().toString().substring(0, 8);
                ExpenseCategoryEntity category1 = categoryService.createCategory(tenantId1, categoryName);
                ExpenseCategoryEntity category2 = categoryService.createCategory(tenantId2, categoryName);
                
                // Use same date range for both tenants
                LocalDate startDate = LocalDate.of(2024, 1, 1);
                LocalDate endDate = LocalDate.of(2024, 1, 31);
                
                // Create random expenses for tenant1
                int expenseCount1 = random.nextInt(5) + 1;
                BigDecimal expectedTotal1 = BigDecimal.ZERO;
                for (int j = 0; j < expenseCount1; j++) {
                    BigDecimal amount = generateRandomAmount();
                    LocalDate date = generateRandomDateInRange(startDate, endDate);
                    expenseService.createExpense(tenantId1, category1.getId(), amount, date, null);
                    expectedTotal1 = expectedTotal1.add(amount);
                }
                
                // Create random expenses for tenant2
                int expenseCount2 = random.nextInt(5) + 1;
                BigDecimal expectedTotal2 = BigDecimal.ZERO;
                for (int j = 0; j < expenseCount2; j++) {
                    BigDecimal amount = generateRandomAmount();
                    LocalDate date = generateRandomDateInRange(startDate, endDate);
                    expenseService.createExpense(tenantId2, category2.getId(), amount, date, null);
                    expectedTotal2 = expectedTotal2.add(amount);
                }
                
                // Get category breakdowns for both tenants
                List<CategoryExpenseAggregation> breakdown1 = 
                    financeAggregationService.getExpensesByCategory(tenantId1, startDate, endDate);
                List<CategoryExpenseAggregation> breakdown2 = 
                    financeAggregationService.getExpensesByCategory(tenantId2, startDate, endDate);
                
                // Verify tenant1 breakdown
                assertEquals(1, breakdown1.size(), "Tenant1 should have 1 category");
                assertEquals(categoryName, breakdown1.get(0).getCategoryName(), 
                    "Tenant1 category name should match");
                assertEquals(0, expectedTotal1.compareTo(breakdown1.get(0).getTotalAmount()),
                    "Tenant1 category total should match. Expected: " + expectedTotal1 + 
                    ", Actual: " + breakdown1.get(0).getTotalAmount());
                
                // Verify tenant2 breakdown
                assertEquals(1, breakdown2.size(), "Tenant2 should have 1 category");
                assertEquals(categoryName, breakdown2.get(0).getCategoryName(), 
                    "Tenant2 category name should match");
                assertEquals(0, expectedTotal2.compareTo(breakdown2.get(0).getTotalAmount()),
                    "Tenant2 category total should match. Expected: " + expectedTotal2 + 
                    ", Actual: " + breakdown2.get(0).getTotalAmount());
                
                // Verify the totals are different (tenant isolation)
                if (expectedTotal1.compareTo(expectedTotal2) != 0) {
                    assertNotEquals(0, expectedTotal1.compareTo(breakdown2.get(0).getTotalAmount()),
                        "Tenant1 total should not equal tenant2 breakdown");
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
     * Property 30 (variant): Tenant isolation in total expenses calculation
     * 
     * For any multi-tenant scenario with expenses in the same date range,
     * each tenant's total should only include their own expenses.
     * 
     * **Feature: finance-module, Property 30: Universal expense query isolation**
     * **Validates: Requirements 9.5, 12.1**
     */
    @Test
    public void totalExpensesRespectTenantBoundaries() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create 3-5 different tenants
                int tenantCount = random.nextInt(3) + 3;
                List<TenantEntity> tenants = new ArrayList<>();
                Map<Long, BigDecimal> expectedTotals = new HashMap<>();
                
                LocalDate startDate = LocalDate.of(2024, 1, 1);
                LocalDate endDate = LocalDate.of(2024, 12, 31);
                
                for (int j = 0; j < tenantCount; j++) {
                    TenantEntity tenant = createTestTenant();
                    tenants.add(tenant);
                    Long tenantId = tenant.getId();
                    
                    // Create category for this tenant
                    ExpenseCategoryEntity category = categoryService.createCategory(
                        tenantId, 
                        "Category " + UUID.randomUUID().toString().substring(0, 8)
                    );
                    
                    // Create random expenses
                    int expenseCount = random.nextInt(10) + 1;
                    BigDecimal total = BigDecimal.ZERO;
                    for (int k = 0; k < expenseCount; k++) {
                        BigDecimal amount = generateRandomAmount();
                        LocalDate date = generateRandomDateInRange(startDate, endDate);
                        expenseService.createExpense(tenantId, category.getId(), amount, date, null);
                        total = total.add(amount);
                    }
                    
                    expectedTotals.put(tenantId, total);
                }
                
                // Verify each tenant's total is correct and isolated
                for (TenantEntity tenant : tenants) {
                    Long tenantId = tenant.getId();
                    BigDecimal expected = expectedTotals.get(tenantId);
                    
                    BigDecimal actual = financeAggregationService.getTotalExpenses(
                        tenantId, startDate, endDate
                    );
                    
                    assertEquals(0, expected.compareTo(actual),
                        "Tenant " + tenantId + " total should match. Expected: " + expected + 
                        ", Actual: " + actual);
                    
                    // Verify this tenant's total doesn't match other tenants' totals
                    // (unless by extreme coincidence)
                    for (TenantEntity otherTenant : tenants) {
                        if (!otherTenant.getId().equals(tenantId)) {
                            BigDecimal otherExpected = expectedTotals.get(otherTenant.getId());
                            if (expected.compareTo(otherExpected) != 0) {
                                assertNotEquals(0, otherExpected.compareTo(actual),
                                    "Tenant " + tenantId + " should not have same total as tenant " + 
                                    otherTenant.getId());
                            }
                        }
                    }
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
        // Generate random amount between 0.01 and 9999.99
        double amount = 0.01 + (random.nextDouble() * 9999.98);
        return BigDecimal.valueOf(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private LocalDate generateRandomDateInMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int day = random.nextInt(yearMonth.lengthOfMonth()) + 1;
        return LocalDate.of(year, month, day);
    }

    private LocalDate generateRandomDateInRange(LocalDate startDate, LocalDate endDate) {
        long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();
        long randomDays = random.nextInt((int) daysBetween + 1);
        return startDate.plusDays(randomDays);
    }
}
