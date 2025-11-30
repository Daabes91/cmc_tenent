package com.clinic.modules.admin.controller;

import com.clinic.modules.core.finance.CategoryExpenseAggregation;
import com.clinic.modules.core.finance.FinanceAggregationService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FinanceSummaryController.
 * Tests monthly summary endpoint with tenant isolation and validation.
 */
@ExtendWith(MockitoExtension.class)
class FinanceSummaryControllerTest {

    @Mock
    private FinanceAggregationService financeAggregationService;

    @Mock
    private TenantContextHolder tenantContextHolder;

    @InjectMocks
    private FinanceSummaryController controller;

    private Long testTenantId;

    @BeforeEach
    void setUp() {
        testTenantId = 123L;
    }

    // ========== GET /admin/finance-summary/monthly Tests ==========

    @Test
    void getMonthlySummary_shouldReturnCorrectTotals_whenExpensesExist() {
        // Arrange
        int year = 2024;
        int month = 3;
        BigDecimal totalExpenses = new BigDecimal("5000.00");
        
        List<CategoryExpenseAggregation> categoryBreakdown = Arrays.asList(
            new CategoryExpenseAggregation("Salaries", new BigDecimal("3000.00")),
            new CategoryExpenseAggregation("Rent", new BigDecimal("1500.00")),
            new CategoryExpenseAggregation("Utilities", new BigDecimal("500.00"))
        );
        
        FinanceAggregationService.MonthlySummary mockSummary = 
            new FinanceAggregationService.MonthlySummary(year, month, totalExpenses, categoryBreakdown);
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(financeAggregationService.getMonthlySummary(testTenantId, year, month))
            .thenReturn(mockSummary);

        // Act
        ResponseEntity<FinanceSummaryController.MonthlySummaryResponse> response = 
            controller.getMonthlySummary(year, month);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        FinanceSummaryController.MonthlySummaryResponse body = response.getBody();
        assertEquals(year, body.getYear());
        assertEquals(month, body.getMonth());
        assertEquals(0, totalExpenses.compareTo(body.getTotalExpenses()));
        assertEquals(3, body.getExpensesByCategory().size());
        
        // Verify category breakdown
        assertEquals("Salaries", body.getExpensesByCategory().get(0).getCategoryName());
        assertEquals(0, new BigDecimal("3000.00").compareTo(
            body.getExpensesByCategory().get(0).getTotalAmount()));
        
        assertEquals("Rent", body.getExpensesByCategory().get(1).getCategoryName());
        assertEquals(0, new BigDecimal("1500.00").compareTo(
            body.getExpensesByCategory().get(1).getTotalAmount()));
        
        assertEquals("Utilities", body.getExpensesByCategory().get(2).getCategoryName());
        assertEquals(0, new BigDecimal("500.00").compareTo(
            body.getExpensesByCategory().get(2).getTotalAmount()));

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(financeAggregationService, times(1)).getMonthlySummary(testTenantId, year, month);
    }

    @Test
    void getMonthlySummary_shouldReturnZero_whenNoExpensesExist() {
        // Arrange
        int year = 2024;
        int month = 6;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        List<CategoryExpenseAggregation> categoryBreakdown = Collections.emptyList();
        
        FinanceAggregationService.MonthlySummary mockSummary = 
            new FinanceAggregationService.MonthlySummary(year, month, totalExpenses, categoryBreakdown);
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(financeAggregationService.getMonthlySummary(testTenantId, year, month))
            .thenReturn(mockSummary);

        // Act
        ResponseEntity<FinanceSummaryController.MonthlySummaryResponse> response = 
            controller.getMonthlySummary(year, month);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        FinanceSummaryController.MonthlySummaryResponse body = response.getBody();
        assertEquals(year, body.getYear());
        assertEquals(month, body.getMonth());
        assertEquals(0, BigDecimal.ZERO.compareTo(body.getTotalExpenses()));
        assertTrue(body.getExpensesByCategory().isEmpty());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(financeAggregationService, times(1)).getMonthlySummary(testTenantId, year, month);
    }

    @Test
    void getMonthlySummary_shouldEnforceTenantIsolation() {
        // Arrange
        int year = 2024;
        int month = 1;
        Long tenant1Id = 100L;
        Long tenant2Id = 200L;
        
        BigDecimal tenant1Total = new BigDecimal("1000.00");
        BigDecimal tenant2Total = new BigDecimal("2000.00");
        
        List<CategoryExpenseAggregation> tenant1Breakdown = Arrays.asList(
            new CategoryExpenseAggregation("Salaries", new BigDecimal("1000.00"))
        );
        
        List<CategoryExpenseAggregation> tenant2Breakdown = Arrays.asList(
            new CategoryExpenseAggregation("Salaries", new BigDecimal("2000.00"))
        );
        
        FinanceAggregationService.MonthlySummary tenant1Summary = 
            new FinanceAggregationService.MonthlySummary(year, month, tenant1Total, tenant1Breakdown);
        FinanceAggregationService.MonthlySummary tenant2Summary = 
            new FinanceAggregationService.MonthlySummary(year, month, tenant2Total, tenant2Breakdown);
        
        // Simulate tenant1 request
        when(tenantContextHolder.requireTenantId()).thenReturn(tenant1Id);
        when(financeAggregationService.getMonthlySummary(tenant1Id, year, month))
            .thenReturn(tenant1Summary);

        // Act - Tenant1 request
        ResponseEntity<FinanceSummaryController.MonthlySummaryResponse> response1 = 
            controller.getMonthlySummary(year, month);

        // Assert - Tenant1 gets only their data
        assertNotNull(response1.getBody());
        assertEquals(0, tenant1Total.compareTo(response1.getBody().getTotalExpenses()));
        
        // Reset mocks for tenant2
        reset(tenantContextHolder, financeAggregationService);
        
        // Simulate tenant2 request
        when(tenantContextHolder.requireTenantId()).thenReturn(tenant2Id);
        when(financeAggregationService.getMonthlySummary(tenant2Id, year, month))
            .thenReturn(tenant2Summary);

        // Act - Tenant2 request
        ResponseEntity<FinanceSummaryController.MonthlySummaryResponse> response2 = 
            controller.getMonthlySummary(year, month);

        // Assert - Tenant2 gets only their data
        assertNotNull(response2.getBody());
        assertEquals(0, tenant2Total.compareTo(response2.getBody().getTotalExpenses()));
        
        // Verify tenant isolation - totals should be different
        assertNotEquals(0, tenant1Total.compareTo(response2.getBody().getTotalExpenses()));
        assertNotEquals(0, tenant2Total.compareTo(response1.getBody().getTotalExpenses()));
    }

    @Test
    void getMonthlySummary_shouldValidateMonthRange_whenMonthIsInvalid() {
        // Arrange
        int year = 2024;
        int invalidMonth = 13; // Invalid month
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.getMonthlySummary(year, invalidMonth)
        );
        
        assertEquals("Month must be between 1 and 12", exception.getMessage());
        
        // Verify service was not called
        verify(financeAggregationService, never()).getMonthlySummary(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getMonthlySummary_shouldValidateMonthRange_whenMonthIsZero() {
        // Arrange
        int year = 2024;
        int invalidMonth = 0; // Invalid month
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.getMonthlySummary(year, invalidMonth)
        );
        
        assertEquals("Month must be between 1 and 12", exception.getMessage());
        
        // Verify service was not called
        verify(financeAggregationService, never()).getMonthlySummary(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getMonthlySummary_shouldHandleCategoryBreakdownAccurately() {
        // Arrange
        int year = 2024;
        int month = 12;
        
        // Create detailed category breakdown
        List<CategoryExpenseAggregation> categoryBreakdown = Arrays.asList(
            new CategoryExpenseAggregation("Salaries", new BigDecimal("10000.00")),
            new CategoryExpenseAggregation("Rent", new BigDecimal("5000.00")),
            new CategoryExpenseAggregation("Electricity", new BigDecimal("500.00")),
            new CategoryExpenseAggregation("Water", new BigDecimal("200.00")),
            new CategoryExpenseAggregation("Internet/Phone", new BigDecimal("300.00")),
            new CategoryExpenseAggregation("Materials", new BigDecimal("2000.00")),
            new CategoryExpenseAggregation("Other", new BigDecimal("1000.00"))
        );
        
        BigDecimal totalExpenses = new BigDecimal("19000.00");
        
        FinanceAggregationService.MonthlySummary mockSummary = 
            new FinanceAggregationService.MonthlySummary(year, month, totalExpenses, categoryBreakdown);
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(financeAggregationService.getMonthlySummary(testTenantId, year, month))
            .thenReturn(mockSummary);

        // Act
        ResponseEntity<FinanceSummaryController.MonthlySummaryResponse> response = 
            controller.getMonthlySummary(year, month);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(7, response.getBody().getExpensesByCategory().size());
        
        // Verify sum of categories equals total
        BigDecimal categorySum = response.getBody().getExpensesByCategory().stream()
            .map(CategoryExpenseAggregation::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        assertEquals(0, totalExpenses.compareTo(categorySum),
            "Sum of category breakdowns should equal total expenses");
        
        // Verify all categories are present
        List<String> categoryNames = response.getBody().getExpensesByCategory().stream()
            .map(CategoryExpenseAggregation::getCategoryName)
            .toList();
        
        assertTrue(categoryNames.contains("Salaries"));
        assertTrue(categoryNames.contains("Rent"));
        assertTrue(categoryNames.contains("Electricity"));
        assertTrue(categoryNames.contains("Water"));
        assertTrue(categoryNames.contains("Internet/Phone"));
        assertTrue(categoryNames.contains("Materials"));
        assertTrue(categoryNames.contains("Other"));
    }
}
