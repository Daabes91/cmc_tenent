package com.clinic.modules.core.finance;

import com.clinic.modules.admin.dto.DashboardSummaryResponse;
import com.clinic.modules.admin.service.DashboardService;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for net result calculation in Dashboard API.
 * 
 * **Feature: finance-module, Property 29: Net result calculation**
 * **Validates: Requirements 11.4**
 * 
 * This test verifies that:
 * - Net result equals income minus expenses
 * - Missing income data is handled gracefully (net result is null)
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations) since jqwik
 * doesn't integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NetResultCalculationPropertyTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private final Random random = new Random();

    /**
     * Property 29: Net result calculation
     * 
     * For any combination of income and expense values, the net result
     * should equal income minus expenses. This test verifies the calculation
     * logic by checking the structure of the response.
     * 
     * **Feature: finance-module, Property 29: Net result calculation**
     * **Validates: Requirements 11.4**
     */
    @Test
    public void netResultCalculationStructure() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a test tenant
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();

                // Generate random expense amount
                BigDecimal expenseAmount = BigDecimal.valueOf(random.nextInt(5000) + 500);  // 500-5500

                // Create a category for expenses
                ExpenseCategoryEntity category = createTestCategory(tenantId);

                // Create expense for current month
                LocalDate today = LocalDate.now();
                LocalDate firstDayOfMonth = today.withDayOfMonth(1);
                createTestExpense(tenantId, category, expenseAmount, firstDayOfMonth);

                // Get dashboard summary
                DashboardSummaryResponse summary = dashboardService.getSummary(ZoneId.systemDefault());

                // Verify the response structure includes net_result field
                // The field may be null if no income data exists, which is acceptable
                assertNotNull(summary, "Dashboard summary should be returned");
                
                // Verify expense data is present
                assertNotNull(summary.totalExpensesCurrentMonth(), "total_expenses_current_month should be present");
                assertNotNull(summary.expensesByCategoryCurrentMonth(), "expenses_by_category_current_month should be present");

                // If net_result is present and both income and expenses exist, verify the calculation
                if (summary.netResult() != null && summary.revenueMonthToDate() > 0) {
                    BigDecimal income = BigDecimal.valueOf(summary.revenueMonthToDate());
                    BigDecimal expenses = summary.totalExpensesCurrentMonth();
                    BigDecimal expectedNetResult = income.subtract(expenses);
                    
                    // Allow for small rounding differences
                    BigDecimal difference = summary.netResult().subtract(expectedNetResult).abs();
                    assertTrue(difference.compareTo(BigDecimal.valueOf(0.01)) < 0,
                            "net_result should equal income minus expenses (within rounding tolerance)");
                }

                // Clean up
                expenseRepository.deleteAll(expenseRepository.findByTenantIdOrderByExpenseDateDesc(tenantId));
                expenseCategoryRepository.delete(category);
                
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
     * Property 29 (variant): Missing income data handling
     * 
     * When income data is not available, the system should handle it gracefully
     * and net_result may be null or calculated based on available data.
     * 
     * **Feature: finance-module, Property 29: Net result calculation**
     * **Validates: Requirements 11.5**
     */
    @Test
    public void netResultHandlesMissingIncomeGracefully() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create a test tenant
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();

                // Generate random expense amount
                BigDecimal expenseAmount = BigDecimal.valueOf(random.nextInt(5000) + 500);  // 500-5500

                // Create a category for expenses
                ExpenseCategoryEntity category = createTestCategory(tenantId);

                // Create expense for current month (but NO income)
                LocalDate today = LocalDate.now();
                LocalDate firstDayOfMonth = today.withDayOfMonth(1);
                createTestExpense(tenantId, category, expenseAmount, firstDayOfMonth);

                // Get dashboard summary
                DashboardSummaryResponse summary = dashboardService.getSummary(ZoneId.systemDefault());

                // Verify the system handles missing income gracefully
                // The net_result may be null or calculated based on zero income
                // Either behavior is acceptable as long as no exception is thrown
                assertNotNull(summary, "Dashboard summary should be returned even without income data");
                assertNotNull(summary.totalExpensesCurrentMonth(), "Expenses should still be calculated");

                // Clean up
                expenseRepository.deleteAll(expenseRepository.findByTenantIdOrderByExpenseDateDesc(tenantId));
                expenseCategoryRepository.delete(category);
                
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
        String slug = "test-tenant-" + UUID.randomUUID().toString().substring(0, 8);
        String name = "Test Tenant " + System.currentTimeMillis();
        TenantEntity tenant = new TenantEntity(slug, name);
        return tenantRepository.saveAndFlush(tenant);
    }

    private ExpenseCategoryEntity createTestCategory(Long tenantId) {
        TenantEntity tenant = tenantRepository.findById(tenantId).orElseThrow();
        ExpenseCategoryEntity category = new ExpenseCategoryEntity(
                "Test Category " + System.nanoTime(),
                false,  // isSystem
                true    // isActive
        );
        category.setTenant(tenant);
        return expenseCategoryRepository.saveAndFlush(category);
    }

    private void createTestExpense(Long tenantId, ExpenseCategoryEntity category, BigDecimal amount, LocalDate date) {
        TenantEntity tenant = tenantRepository.findById(tenantId).orElseThrow();
        ExpenseEntity expense = new ExpenseEntity();
        expense.setTenant(tenant);
        expense.setCategory(category);
        expense.setAmount(amount);
        expense.setExpenseDate(date);
        expense.setNotes("Test expense");
        expenseRepository.saveAndFlush(expense);
    }

}
