package com.clinic.modules.core.finance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Service for aggregating and summarizing expense data.
 * Provides methods for calculating totals and category breakdowns with tenant isolation.
 */
@Service
@Transactional(readOnly = true)
public class FinanceAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(FinanceAggregationService.class);

    private final ExpenseRepository expenseRepository;

    public FinanceAggregationService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * Get monthly summary of expenses for a specific tenant.
     * Calculates total expenses and category breakdown for the specified month.
     *
     * @param tenantId the tenant ID
     * @param year     the year
     * @param month    the month (1-12)
     * @return monthly summary with total and category breakdown
     */
    public MonthlySummary getMonthlySummary(Long tenantId, int year, int month) {
        logger.debug("Getting monthly summary for tenant {} for {}-{}", tenantId, year, month);

        // Calculate start and end dates for the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get total expenses for the month
        BigDecimal totalExpenses = getTotalExpenses(tenantId, startDate, endDate);

        // Get expenses by category
        List<CategoryExpenseAggregation> expensesByCategory = getExpensesByCategory(tenantId, startDate, endDate);

        logger.debug("Monthly summary for tenant {}: total={}, categories={}", 
                     tenantId, totalExpenses, expensesByCategory.size());

        return new MonthlySummary(year, month, totalExpenses, expensesByCategory);
    }

    /**
     * Get expenses grouped by category for a specific tenant within a date range.
     *
     * @param tenantId  the tenant ID
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return list of category expense aggregations
     */
    public List<CategoryExpenseAggregation> getExpensesByCategory(Long tenantId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Getting expenses by category for tenant {} from {} to {}", tenantId, startDate, endDate);

        List<CategoryExpenseAggregation> result = expenseRepository
                .sumAmountByTenantIdAndCategoryGroupedByCategory(tenantId, startDate, endDate);

        logger.debug("Found {} categories with expenses for tenant {}", result.size(), tenantId);

        return result;
    }

    /**
     * Get total expenses for a specific tenant within a date range.
     *
     * @param tenantId  the tenant ID
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return total expenses amount (returns zero if no expenses found)
     */
    public BigDecimal getTotalExpenses(Long tenantId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Getting total expenses for tenant {} from {} to {}", tenantId, startDate, endDate);

        BigDecimal total = expenseRepository
                .sumAmountByTenantIdAndExpenseDateBetween(tenantId, startDate, endDate);

        // Handle null case (no expenses found) - return zero
        BigDecimal result = total != null ? total : BigDecimal.ZERO;

        logger.debug("Total expenses for tenant {}: {}", tenantId, result);

        return result;
    }

    /**
     * DTO for monthly expense summary.
     */
    public static class MonthlySummary {
        private final int year;
        private final int month;
        private final BigDecimal totalExpenses;
        private final List<CategoryExpenseAggregation> expensesByCategory;

        public MonthlySummary(int year, int month, BigDecimal totalExpenses, 
                            List<CategoryExpenseAggregation> expensesByCategory) {
            this.year = year;
            this.month = month;
            this.totalExpenses = totalExpenses;
            this.expensesByCategory = expensesByCategory;
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public BigDecimal getTotalExpenses() {
            return totalExpenses;
        }

        public List<CategoryExpenseAggregation> getExpensesByCategory() {
            return expensesByCategory;
        }
    }
}
