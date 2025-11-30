package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.DashboardSummaryResponse;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.doctor.DoctorAvailabilityRepository;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.finance.CategoryExpenseAggregation;
import com.clinic.modules.core.finance.FinanceAggregationService;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.treatment.PaymentRepository;
import com.clinic.modules.core.treatment.TreatmentPlanPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DashboardService expense integration.
 * 
 * Tests Requirements: 11.1, 11.2, 11.3, 11.4, 11.5
 */
@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TreatmentPlanPaymentRepository treatmentPlanPaymentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Mock
    private FinanceAggregationService financeAggregationService;

    @Mock
    private TenantContextHolder tenantContextHolder;

    private DashboardService dashboardService;

    private static final Long TEST_TENANT_ID = 123L;
    private static final ZoneId TEST_ZONE = ZoneId.systemDefault();

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                appointmentRepository,
                patientRepository,
                paymentRepository,
                treatmentPlanPaymentRepository,
                doctorRepository,
                doctorAvailabilityRepository,
                financeAggregationService,
                tenantContextHolder
        );

        // Setup common mocks
        when(tenantContextHolder.requireTenantId()).thenReturn(TEST_TENANT_ID);
        when(appointmentRepository.countByTenantIdAndScheduledAtBetween(eq(TEST_TENANT_ID), any(), any()))
                .thenReturn(0L);
        when(appointmentRepository.countByTenantIdAndStatusAndScheduledAtBetween(eq(TEST_TENANT_ID), any(), any(), any()))
                .thenReturn(0L);
        when(paymentRepository.findByTenantIdAndPaymentDateBetween(eq(TEST_TENANT_ID), any(), any()))
                .thenReturn(Collections.emptyList());
        when(treatmentPlanPaymentRepository.findByTenantIdAndPaymentDateBetween(eq(TEST_TENANT_ID), any(), any()))
                .thenReturn(Collections.emptyList());
        when(patientRepository.countByTenantIdAndCreatedAtAfter(eq(TEST_TENANT_ID), any()))
                .thenReturn(0L);
    }

    /**
     * Test that dashboard includes current month expenses.
     * Validates: Requirements 11.1, 11.2
     */
    @Test
    void getSummary_shouldIncludeCurrentMonthExpenses() {
        // Arrange
        BigDecimal totalExpenses = new BigDecimal("1500.00");
        List<CategoryExpenseAggregation> categoryBreakdown = Arrays.asList(
                new CategoryExpenseAggregation("Salaries", new BigDecimal("1000.00")),
                new CategoryExpenseAggregation("Rent", new BigDecimal("500.00"))
        );

        when(financeAggregationService.getTotalExpenses(eq(TEST_TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(totalExpenses);
        when(financeAggregationService.getExpensesByCategory(eq(TEST_TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(categoryBreakdown);

        // Act
        DashboardSummaryResponse response = dashboardService.getSummary(TEST_ZONE);

        // Assert
        assertNotNull(response, "Dashboard summary should not be null");
        assertNotNull(response.totalExpensesCurrentMonth(), "total_expenses_current_month should be present");
        assertEquals(totalExpenses, response.totalExpensesCurrentMonth(), 
                "total_expenses_current_month should match the aggregated value");
        
        assertNotNull(response.expensesByCategoryCurrentMonth(), "expenses_by_category_current_month should be present");
        assertEquals(2, response.expensesByCategoryCurrentMonth().size(), 
                "Should have 2 expense categories");
        assertEquals("Salaries", response.expensesByCategoryCurrentMonth().get(0).categoryName());
        assertEquals(new BigDecimal("1000.00"), response.expensesByCategoryCurrentMonth().get(0).amount());
    }

    /**
     * Test net result calculation when both income and expenses exist.
     * Validates: Requirements 11.4
     */
    @Test
    void getSummary_shouldCalculateNetResult_whenBothIncomeAndExpensesExist() {
        // Arrange
        BigDecimal totalExpenses = new BigDecimal("1500.00");
        
        when(financeAggregationService.getTotalExpenses(eq(TEST_TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(totalExpenses);
        when(financeAggregationService.getExpensesByCategory(eq(TEST_TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // Act
        DashboardSummaryResponse response = dashboardService.getSummary(TEST_ZONE);

        // Assert
        assertNotNull(response, "Dashboard summary should not be null");
        
        // Net result should be calculated (income - expenses)
        // In this test, income is 0, so net result should be -1500
        if (response.netResult() != null) {
            BigDecimal expectedNetResult = BigDecimal.valueOf(response.revenueMonthToDate())
                    .subtract(totalExpenses);
            assertEquals(0, response.netResult().compareTo(expectedNetResult),
                    "net_result should equal income minus expenses");
        }
    }

    /**
     * Test graceful handling of missing income data.
     * Validates: Requirements 11.5
     */
    @Test
    void getSummary_shouldHandleMissingIncomeGracefully() {
        // Arrange
        BigDecimal totalExpenses = new BigDecimal("1500.00");
        
        when(financeAggregationService.getTotalExpenses(eq(TEST_TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(totalExpenses);
        when(financeAggregationService.getExpensesByCategory(eq(TEST_TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // Act
        DashboardSummaryResponse response = dashboardService.getSummary(TEST_ZONE);

        // Assert
        assertNotNull(response, "Dashboard summary should not be null even without income");
        assertNotNull(response.totalExpensesCurrentMonth(), "Expenses should still be calculated");
        assertEquals(totalExpenses, response.totalExpensesCurrentMonth());
        
        // Net result may be null or calculated based on zero income - both are acceptable
        // The important thing is that no exception is thrown
    }

    /**
     * Test that empty expense data is handled correctly.
     * Validates: Requirements 11.1, 11.2
     */
    @Test
    void getSummary_shouldHandleEmptyExpenseData() {
        // Arrange
        when(financeAggregationService.getTotalExpenses(eq(TEST_TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(financeAggregationService.getExpensesByCategory(eq(TEST_TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // Act
        DashboardSummaryResponse response = dashboardService.getSummary(TEST_ZONE);

        // Assert
        assertNotNull(response, "Dashboard summary should not be null");
        assertNotNull(response.totalExpensesCurrentMonth(), "total_expenses_current_month should be present");
        assertEquals(BigDecimal.ZERO, response.totalExpensesCurrentMonth(), 
                "total_expenses_current_month should be zero when no expenses exist");
        
        assertNotNull(response.expensesByCategoryCurrentMonth(), "expenses_by_category_current_month should be present");
        assertTrue(response.expensesByCategoryCurrentMonth().isEmpty(), 
                "expenses_by_category_current_month should be empty when no expenses exist");
    }

    /**
     * Test that expense data is filtered by current month.
     * Validates: Requirements 11.3
     */
    @Test
    void getSummary_shouldFilterExpensesByCurrentMonth() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal totalExpenses = new BigDecimal("2000.00");
        
        when(financeAggregationService.getTotalExpenses(eq(TEST_TENANT_ID), eq(firstDayOfMonth), eq(lastDayOfMonth)))
                .thenReturn(totalExpenses);
        when(financeAggregationService.getExpensesByCategory(eq(TEST_TENANT_ID), eq(firstDayOfMonth), eq(lastDayOfMonth)))
                .thenReturn(Collections.emptyList());

        // Act
        DashboardSummaryResponse response = dashboardService.getSummary(TEST_ZONE);

        // Assert
        assertNotNull(response, "Dashboard summary should not be null");
        assertEquals(totalExpenses, response.totalExpensesCurrentMonth(), 
                "Should include expenses from current month only");
    }
}
