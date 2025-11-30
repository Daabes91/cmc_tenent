package com.clinic.modules.admin.controller;

import com.clinic.modules.core.finance.CategoryExpenseAggregation;
import com.clinic.modules.core.finance.FinanceAggregationService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for finance summary and aggregation endpoints.
 * Provides endpoints for retrieving monthly summaries and expense aggregations.
 * All operations enforce tenant isolation via JWT authentication.
 */
@RestController
@RequestMapping("/admin/finance-summary")
@Tag(name = "Finance Summary", description = "Endpoints for financial summaries and aggregations")
@SecurityRequirement(name = "bearerAuth")
public class FinanceSummaryController {

    private static final Logger logger = LoggerFactory.getLogger(FinanceSummaryController.class);

    private final FinanceAggregationService financeAggregationService;
    private final TenantContextHolder tenantContextHolder;

    public FinanceSummaryController(
            FinanceAggregationService financeAggregationService,
            TenantContextHolder tenantContextHolder) {
        this.financeAggregationService = financeAggregationService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get monthly expense summary for the authenticated tenant.
     *
     * @param year  the year (e.g., 2024)
     * @param month the month (1-12)
     * @return monthly summary with total expenses and category breakdown
     */
    @GetMapping("/monthly")
    @Operation(
            summary = "Get monthly expense summary",
            description = "Retrieve total expenses and category breakdown for a specific month. Returns zero if no expenses found for the period."
    )
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @Parameter(description = "Year (e.g., 2024)", required = true)
            @RequestParam int year,
            
            @Parameter(description = "Month (1-12)", required = true)
            @RequestParam int month) {
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        logger.info("Fetching monthly summary - tenantId: {}, year: {}, month: {}", tenantId, year, month);

        // Validate month range
        if (month < 1 || month > 12) {
            logger.warn("Invalid month value: {}", month);
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        FinanceAggregationService.MonthlySummary summary = 
                financeAggregationService.getMonthlySummary(tenantId, year, month);

        MonthlySummaryResponse response = new MonthlySummaryResponse(
                summary.getYear(),
                summary.getMonth(),
                summary.getTotalExpenses(),
                summary.getExpensesByCategory()
        );

        logger.info("Monthly summary retrieved - tenantId: {}, total: {}, categories: {}", 
                    tenantId, response.getTotalExpenses(), response.getExpensesByCategory().size());

        return ResponseEntity.ok(response);
    }

    /**
     * Response DTO for monthly summary.
     */
    public static class MonthlySummaryResponse {
        private final int year;
        private final int month;
        private final BigDecimal totalExpenses;
        private final List<CategoryExpenseAggregation> expensesByCategory;

        public MonthlySummaryResponse(int year, int month, BigDecimal totalExpenses, 
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
