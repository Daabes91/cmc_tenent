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
 * Property-based test for monthly expense aggregation.
 * 
 * **Feature: finance-module, Property 21: Monthly expense aggregation**
 * **Validates: Requirements 9.2, 9.3**
 * 
 * This test verifies that:
 * 1. For any selected month and year, the system calculates the sum of all expenses correctly
 * 2. For any month, expenses are grouped by category accurately
 * 3. For any month, the sum of category breakdowns equals the total
 * 4. For any month with no expenses, the system returns zero
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations) since jqwik
 * doesn't integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MonthlyExpenseAggregationPropertyTest {

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
     * Property 21: Monthly expense aggregation correctness
     * 
     * For any selected month and year with random expenses, the monthly total should equal
     * the sum of all expenses in that month, and the category breakdown should sum to the total.
     * 
     * **Feature: finance-module, Property 21: Monthly expense aggregation**
     * **Validates: Requirements 9.2, 9.3**
     */
    @Test
    public void monthlyTotalsAreCalculatedCorrectly() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                // Create 2-5 categories
                int categoryCount = random.nextInt(4) + 2;
                List<ExpenseCategoryEntity> categories = new ArrayList<>();
                for (int j = 0; j < categoryCount; j++) {
                    ExpenseCategoryEntity category = categoryService.createCategory(
                        tenantId, 
                        "Category " + j + " " + UUID.randomUUID().toString().substring(0, 8)
                    );
                    categories.add(category);
                }
                
                // Generate random year and month
                int year = 2023 + random.nextInt(2); // 2023 or 2024
                int month = random.nextInt(12) + 1; // 1-12
                YearMonth yearMonth = YearMonth.of(year, month);
                LocalDate startDate = yearMonth.atDay(1);
                LocalDate endDate = yearMonth.atEndOfMonth();
                
                // Create 5-20 random expenses in this month
                int expenseCount = random.nextInt(16) + 5;
                BigDecimal expectedTotal = BigDecimal.ZERO;
                Map<String, BigDecimal> expectedByCategory = new HashMap<>();
                
                for (int j = 0; j < expenseCount; j++) {
                    ExpenseCategoryEntity category = categories.get(random.nextInt(categories.size()));
                    BigDecimal amount = generateRandomAmount();
                    LocalDate date = generateRandomDateInMonth(year, month);
                    
                    expenseService.createExpense(tenantId, category.getId(), amount, date, null);
                    
                    expectedTotal = expectedTotal.add(amount);
                    expectedByCategory.merge(category.getName(), amount, BigDecimal::add);
                }
                
                // Get monthly summary
                FinanceAggregationService.MonthlySummary summary = 
                    financeAggregationService.getMonthlySummary(tenantId, year, month);
                
                // Verify total matches expected
                assertEquals(0, expectedTotal.compareTo(summary.getTotalExpenses()),
                    "Monthly total should match sum of all expenses. Expected: " + expectedTotal + 
                    ", Actual: " + summary.getTotalExpenses());
                
                // Verify category breakdown
                List<CategoryExpenseAggregation> categoryBreakdown = summary.getExpensesByCategory();
                assertEquals(expectedByCategory.size(), categoryBreakdown.size(),
                    "Category breakdown should have correct number of categories");
                
                // Verify each category total
                BigDecimal categorySum = BigDecimal.ZERO;
                for (CategoryExpenseAggregation agg : categoryBreakdown) {
                    BigDecimal expected = expectedByCategory.get(agg.getCategoryName());
                    assertNotNull(expected, "Category " + agg.getCategoryName() + " should exist in expected data");
                    assertEquals(0, expected.compareTo(agg.getTotalAmount()),
                        "Category " + agg.getCategoryName() + " total should match. Expected: " + 
                        expected + ", Actual: " + agg.getTotalAmount());
                    categorySum = categorySum.add(agg.getTotalAmount());
                }
                
                // Verify category breakdown sums to total
                assertEquals(0, expectedTotal.compareTo(categorySum),
                    "Sum of category breakdowns should equal total. Expected: " + expectedTotal + 
                    ", Actual: " + categorySum);
                
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
     * Property 21 (variant): Empty month returns zero
     * 
     * For any month with no expenses, the system should return zero for total and empty category breakdown.
     * 
     * **Feature: finance-module, Property 21: Monthly expense aggregation**
     * **Validates: Requirements 9.2, 9.3**
     */
    @Test
    public void emptyMonthReturnsZero() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                // Generate random year and month (no expenses created)
                int year = 2023 + random.nextInt(2);
                int month = random.nextInt(12) + 1;
                
                // Get monthly summary for empty month
                FinanceAggregationService.MonthlySummary summary = 
                    financeAggregationService.getMonthlySummary(tenantId, year, month);
                
                // Verify total is zero
                assertEquals(0, BigDecimal.ZERO.compareTo(summary.getTotalExpenses()),
                    "Empty month should have zero total expenses");
                
                // Verify category breakdown is empty
                assertTrue(summary.getExpensesByCategory().isEmpty(),
                    "Empty month should have empty category breakdown");
                
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
     * Property 21 (variant): Expenses across multiple months
     * 
     * For any tenant with expenses across multiple months, each month's aggregation
     * should only include expenses from that specific month.
     * 
     * **Feature: finance-module, Property 21: Monthly expense aggregation**
     * **Validates: Requirements 9.2, 9.3**
     */
    @Test
    public void monthlyAggregationIsolatesMonths() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a tenant and category
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                ExpenseCategoryEntity category = categoryService.createCategory(
                    tenantId, 
                    "Category " + UUID.randomUUID().toString().substring(0, 8)
                );
                
                // Create expenses in 3 different months
                int year = 2024;
                Map<Integer, BigDecimal> expectedByMonth = new HashMap<>();
                
                for (int month = 1; month <= 3; month++) {
                    int expenseCount = random.nextInt(5) + 1;
                    BigDecimal monthTotal = BigDecimal.ZERO;
                    
                    for (int j = 0; j < expenseCount; j++) {
                        BigDecimal amount = generateRandomAmount();
                        LocalDate date = generateRandomDateInMonth(year, month);
                        expenseService.createExpense(tenantId, category.getId(), amount, date, null);
                        monthTotal = monthTotal.add(amount);
                    }
                    
                    expectedByMonth.put(month, monthTotal);
                }
                
                // Verify each month's total is correct and isolated
                for (int month = 1; month <= 3; month++) {
                    FinanceAggregationService.MonthlySummary summary = 
                        financeAggregationService.getMonthlySummary(tenantId, year, month);
                    
                    BigDecimal expected = expectedByMonth.get(month);
                    assertEquals(0, expected.compareTo(summary.getTotalExpenses()),
                        "Month " + month + " total should match. Expected: " + expected + 
                        ", Actual: " + summary.getTotalExpenses());
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
}
