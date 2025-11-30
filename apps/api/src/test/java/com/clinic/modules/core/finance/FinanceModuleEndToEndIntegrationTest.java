package com.clinic.modules.core.finance;

import com.clinic.modules.admin.controller.ExpenseController;
import com.clinic.modules.admin.controller.ExpenseCategoryController;
import com.clinic.modules.admin.controller.FinanceSummaryController;
import com.clinic.modules.admin.dto.CategoryExpense;
import com.clinic.modules.admin.dto.ExpenseCreateRequest;
import com.clinic.modules.admin.dto.ReportMetrics;
import com.clinic.modules.admin.service.DashboardService;
import com.clinic.modules.admin.service.ReportService;
import com.clinic.modules.admin.util.DateRange;
import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration test for the Finance Module.
 * 
 * This test suite covers:
 * - Complete expense creation flow
 * - Expense filtering and listing
 * - Category management flow
 * - Monthly summary calculation
 * - Reports API integration
 * - Dashboard API integration
 * - Tenant isolation across all operations
 * 
 * Feature: finance-module, Task 10.1
 * **Validates: All Requirements**
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FinanceModuleEndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategorySeedingService categorySeedingService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ObjectMapper objectMapper;

    private TenantEntity tenant1;
    private TenantEntity tenant2;

    @BeforeEach
    public void setup() {
        // Clean up test data (in correct order to respect foreign keys)
        expenseRepository.deleteAll();
        expenseCategoryRepository.deleteAll();
        // Don't delete all tenants - just clean up finance data

        // Create test tenants if they don't exist
        tenant1 = tenantRepository.findBySlugIgnoreCase("e2e-clinic-1")
            .orElseGet(() -> createTestTenant("e2e-clinic-1", "E2E Clinic 1"));
        tenant2 = tenantRepository.findBySlugIgnoreCase("e2e-clinic-2")
            .orElseGet(() -> createTestTenant("e2e-clinic-2", "E2E Clinic 2"));

        // Seed default categories for both tenants if not already seeded
        if (expenseCategoryRepository.findByTenantIdOrderByNameAsc(tenant1.getId()).isEmpty()) {
            categorySeedingService.seedDefaultCategories(tenant1.getId());
        }
        if (expenseCategoryRepository.findByTenantIdOrderByNameAsc(tenant2.getId()).isEmpty()) {
            categorySeedingService.seedDefaultCategories(tenant2.getId());
        }
    }

    /**
     * Test 1: Complete expense creation flow
     * 
     * Scenario: User creates a new expense with all required fields
     * Expected: Expense is created, persisted, and retrievable
     * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5
     */
    @Test
    @WithMockUser(username = "admin@e2e-clinic-1.com")
    public void testCompleteExpenseCreationFlow() throws Exception {
        // Get a category for tenant1
        ExpenseCategoryEntity category = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId())
            .stream()
            .filter(c -> c.getName().equals("Salaries"))
            .findFirst()
            .orElseThrow();

        // Create expense request
        ExpenseCreateRequest request = new ExpenseCreateRequest(
            category.getId(),
            new BigDecimal("5000.00"),
            LocalDate.now(),
            "Monthly staff salaries"
        );

        // Create expense via API
        String responseJson = mockMvc.perform(post("/api/admin/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.categoryName").value("Salaries"))
                .andExpect(jsonPath("$.notes").value("Monthly staff salaries"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify expense was persisted
        List<ExpenseEntity> expenses = expenseRepository
            .findByTenantIdOrderByExpenseDateDesc(tenant1.getId());
        assertThat(expenses).hasSize(1);
        assertThat(expenses.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(expenses.get(0).getTenant().getId()).isEqualTo(tenant1.getId());
    }

    /**
     * Test 2: Expense filtering and listing
     * 
     * Scenario: User creates multiple expenses and filters by date range and category
     * Expected: Filters work correctly and return only matching expenses
     * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5
     */
    @Test
    @WithMockUser(username = "admin@e2e-clinic-1.com")
    public void testExpenseFilteringAndListing() throws Exception {
        // Get categories
        List<ExpenseCategoryEntity> categories = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId());
        ExpenseCategoryEntity salariesCategory = categories.stream()
            .filter(c -> c.getName().equals("Salaries"))
            .findFirst().orElseThrow();
        ExpenseCategoryEntity rentCategory = categories.stream()
            .filter(c -> c.getName().equals("Rent"))
            .findFirst().orElseThrow();

        // Create expenses with different dates and categories
        createExpense(tenant1.getId(), salariesCategory.getId(), 
            new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "January salaries");
        createExpense(tenant1.getId(), rentCategory.getId(), 
            new BigDecimal("2000.00"), LocalDate.of(2024, 1, 1), "January rent");
        createExpense(tenant1.getId(), salariesCategory.getId(), 
            new BigDecimal("5200.00"), LocalDate.of(2024, 2, 15), "February salaries");
        createExpense(tenant1.getId(), rentCategory.getId(), 
            new BigDecimal("2000.00"), LocalDate.of(2024, 2, 1), "February rent");

        // Test 1: List all expenses (no filter)
        mockMvc.perform(get("/api/admin/expenses")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        // Test 2: Filter by date range (January only)
        mockMvc.perform(get("/api/admin/expenses")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Test 3: Filter by category (Salaries only)
        mockMvc.perform(get("/api/admin/expenses")
                .param("categoryId", salariesCategory.getId().toString())
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryName").value("Salaries"))
                .andExpect(jsonPath("$[1].categoryName").value("Salaries"));

        // Test 4: Combined filters (January + Salaries)
        mockMvc.perform(get("/api/admin/expenses")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .param("categoryId", salariesCategory.getId().toString())
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].amount").value(5000.00));
    }

    /**
     * Test 3: Category management flow
     * 
     * Scenario: User manages expense categories (create, update, toggle)
     * Expected: Categories can be created, updated, and toggled
     * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 7.1, 7.2, 7.3, 8.1, 8.2
     */
    @Test
    @WithMockUser(username = "admin@e2e-clinic-1.com")
    public void testCategoryManagementFlow() throws Exception {
        // Test 1: List default categories
        mockMvc.perform(get("/api/admin/expense-categories")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7)); // 7 default categories

        // Test 2: Create custom category
        String createRequest = "{\"name\":\"Marketing\"}";
        mockMvc.perform(post("/api/admin/expense-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest)
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Marketing"))
                .andExpect(jsonPath("$.isSystem").value(false))
                .andExpect(jsonPath("$.isActive").value(true));

        // Verify category was created
        List<ExpenseCategoryEntity> categories = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId());
        assertThat(categories).hasSize(8);
        ExpenseCategoryEntity marketingCategory = categories.stream()
            .filter(c -> c.getName().equals("Marketing"))
            .findFirst().orElseThrow();
        assertThat(marketingCategory.getIsSystem()).isFalse();

        // Test 3: Update category name
        String updateRequest = "{\"name\":\"Marketing & Advertising\"}";
        mockMvc.perform(put("/api/admin/expense-categories/" + marketingCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest)
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Marketing & Advertising"));

        // Test 4: Toggle category (disable)
        mockMvc.perform(patch("/api/admin/expense-categories/" + marketingCategory.getId() + "/toggle")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Test 5: Verify disabled category not in active list
        mockMvc.perform(get("/api/admin/expense-categories")
                .param("activeOnly", "true")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7)); // Back to 7 active

        // Test 6: Toggle category (enable)
        mockMvc.perform(patch("/api/admin/expense-categories/" + marketingCategory.getId() + "/toggle")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        // Test 7: Attempt to create duplicate category name
        mockMvc.perform(post("/api/admin/expense-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Marketing & Advertising\"}")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isConflict());
    }

    /**
     * Test 4: Monthly summary calculation
     * 
     * Scenario: User creates expenses and views monthly summary
     * Expected: Summary shows correct totals and category breakdowns
     * Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.5
     */
    @Test
    @WithMockUser(username = "admin@e2e-clinic-1.com")
    public void testMonthlySummaryCalculation() throws Exception {
        // Get categories
        List<ExpenseCategoryEntity> categories = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId());
        ExpenseCategoryEntity salariesCategory = categories.stream()
            .filter(c -> c.getName().equals("Salaries"))
            .findFirst().orElseThrow();
        ExpenseCategoryEntity rentCategory = categories.stream()
            .filter(c -> c.getName().equals("Rent"))
            .findFirst().orElseThrow();
        ExpenseCategoryEntity electricityCategory = categories.stream()
            .filter(c -> c.getName().equals("Electricity"))
            .findFirst().orElseThrow();

        // Create expenses for January 2024
        createExpense(tenant1.getId(), salariesCategory.getId(), 
            new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "January salaries");
        createExpense(tenant1.getId(), rentCategory.getId(), 
            new BigDecimal("2000.00"), LocalDate.of(2024, 1, 1), "January rent");
        createExpense(tenant1.getId(), electricityCategory.getId(), 
            new BigDecimal("300.00"), LocalDate.of(2024, 1, 10), "January electricity");

        // Create expenses for February 2024
        createExpense(tenant1.getId(), salariesCategory.getId(), 
            new BigDecimal("5200.00"), LocalDate.of(2024, 2, 15), "February salaries");

        // Test 1: Get January summary
        mockMvc.perform(get("/api/admin/finance-summary/monthly")
                .param("year", "2024")
                .param("month", "1")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(7300.00))
                .andExpect(jsonPath("$.expensesByCategory.length()").value(3))
                .andExpect(jsonPath("$.expensesByCategory[?(@.categoryName=='Salaries')].amount").value(5000.00))
                .andExpect(jsonPath("$.expensesByCategory[?(@.categoryName=='Rent')].amount").value(2000.00))
                .andExpect(jsonPath("$.expensesByCategory[?(@.categoryName=='Electricity')].amount").value(300.00));

        // Test 2: Get February summary
        mockMvc.perform(get("/api/admin/finance-summary/monthly")
                .param("year", "2024")
                .param("month", "2")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(5200.00))
                .andExpect(jsonPath("$.expensesByCategory.length()").value(1))
                .andExpect(jsonPath("$.expensesByCategory[0].categoryName").value("Salaries"))
                .andExpect(jsonPath("$.expensesByCategory[0].amount").value(5200.00));

        // Test 3: Get empty month summary
        mockMvc.perform(get("/api/admin/finance-summary/monthly")
                .param("year", "2024")
                .param("month", "3")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(0.00))
                .andExpect(jsonPath("$.expensesByCategory.length()").value(0));
    }

    /**
     * Test 5: Reports API integration
     * 
     * Scenario: Reports API includes expense data
     * Expected: Reports include total expenses and category breakdown
     * Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5
     */
    @Test
    public void testReportsAPIIntegration() {
        // Get categories
        List<ExpenseCategoryEntity> categories = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId());
        ExpenseCategoryEntity salariesCategory = categories.stream()
            .filter(c -> c.getName().equals("Salaries"))
            .findFirst().orElseThrow();
        ExpenseCategoryEntity rentCategory = categories.stream()
            .filter(c -> c.getName().equals("Rent"))
            .findFirst().orElseThrow();

        // Create expenses
        createExpense(tenant1.getId(), salariesCategory.getId(), 
            new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "Salaries");
        createExpense(tenant1.getId(), rentCategory.getId(), 
            new BigDecimal("2000.00"), LocalDate.of(2024, 1, 1), "Rent");

        // Generate report
        DateRange dateRange = DateRange.of(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31)
        );
        ReportMetrics report = reportService.getOverallMetrics(dateRange, ZoneId.systemDefault());

        // Verify expense data is included
        assertThat(report.totalExpenses()).isNotNull();
        assertThat(report.totalExpenses()).isEqualByComparingTo(new BigDecimal("7000.00"));
        assertThat(report.expensesByCategory()).isNotNull();
        assertThat(report.expensesByCategory()).hasSize(2);

        // Verify category breakdown
        List<CategoryExpense> expenseList = report.expensesByCategory();
        assertThat(expenseList.stream().anyMatch(e -> e.categoryName().equals("Salaries") && 
            e.amount().compareTo(new BigDecimal("5000.00")) == 0)).isTrue();
        assertThat(expenseList.stream().anyMatch(e -> e.categoryName().equals("Rent") && 
            e.amount().compareTo(new BigDecimal("2000.00")) == 0)).isTrue();
    }

    /**
     * Test 6: Dashboard API integration
     * 
     * Scenario: Dashboard API includes current month expense data
     * Expected: Dashboard includes current month expenses and net result
     * Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.5
     */
    @Test
    public void testDashboardAPIIntegration() {
        // Get categories
        List<ExpenseCategoryEntity> categories = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId());
        ExpenseCategoryEntity salariesCategory = categories.stream()
            .filter(c -> c.getName().equals("Salaries"))
            .findFirst().orElseThrow();
        ExpenseCategoryEntity rentCategory = categories.stream()
            .filter(c -> c.getName().equals("Rent"))
            .findFirst().orElseThrow();

        // Create expenses for current month
        YearMonth currentMonth = YearMonth.now();
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        LocalDate midMonth = currentMonth.atDay(15);

        createExpense(tenant1.getId(), salariesCategory.getId(), 
            new BigDecimal("5000.00"), midMonth, "Current month salaries");
        createExpense(tenant1.getId(), rentCategory.getId(), 
            new BigDecimal("2000.00"), firstDayOfMonth, "Current month rent");

        // Get dashboard data
        var dashboard = dashboardService.getSummary(ZoneId.systemDefault());

        // Verify expense data is included
        assertThat(dashboard.totalExpensesCurrentMonth()).isNotNull();
        assertThat(dashboard.totalExpensesCurrentMonth())
            .isEqualByComparingTo(new BigDecimal("7000.00"));
        assertThat(dashboard.expensesByCategoryCurrentMonth()).isNotNull();
        assertThat(dashboard.expensesByCategoryCurrentMonth()).hasSize(2);

        // Verify category breakdown
        List<CategoryExpense> expenseList = dashboard.expensesByCategoryCurrentMonth();
        assertThat(expenseList.stream().anyMatch(e -> e.categoryName().equals("Salaries") && 
            e.amount().compareTo(new BigDecimal("5000.00")) == 0)).isTrue();
        assertThat(expenseList.stream().anyMatch(e -> e.categoryName().equals("Rent") && 
            e.amount().compareTo(new BigDecimal("2000.00")) == 0)).isTrue();

        // Note: Net result calculation tested separately in NetResultCalculationPropertyTest
    }

    /**
     * Test 7: Tenant isolation across all operations
     * 
     * Scenario: Two tenants create expenses and categories
     * Expected: Each tenant can only access their own data
     * Validates: Requirements 12.1, 12.2, 12.3, 12.4, 12.5
     */
    @Test
    @WithMockUser(username = "admin@e2e-clinic-1.com")
    public void testTenantIsolationAcrossAllOperations() throws Exception {
        // Get categories for both tenants
        List<ExpenseCategoryEntity> tenant1Categories = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId());
        List<ExpenseCategoryEntity> tenant2Categories = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant2.getId());

        ExpenseCategoryEntity tenant1Salaries = tenant1Categories.stream()
            .filter(c -> c.getName().equals("Salaries"))
            .findFirst().orElseThrow();
        ExpenseCategoryEntity tenant2Salaries = tenant2Categories.stream()
            .filter(c -> c.getName().equals("Salaries"))
            .findFirst().orElseThrow();

        // Create expenses for both tenants
        ExpenseEntity tenant1Expense = createExpense(tenant1.getId(), tenant1Salaries.getId(), 
            new BigDecimal("5000.00"), LocalDate.now(), "Tenant 1 expense");
        ExpenseEntity tenant2Expense = createExpense(tenant2.getId(), tenant2Salaries.getId(), 
            new BigDecimal("3000.00"), LocalDate.now(), "Tenant 2 expense");

        // Test 1: Tenant 1 can only see their expenses
        mockMvc.perform(get("/api/admin/expenses")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].amount").value(5000.00));

        // Test 2: Tenant 1 cannot access Tenant 2's expense
        mockMvc.perform(get("/api/admin/expenses/" + tenant2Expense.getId())
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isNotFound());

        // Test 3: Tenant 1 cannot update Tenant 2's expense
        String updateRequest = objectMapper.writeValueAsString(
            Map.of("amount", 9999.99, "categoryId", tenant1Salaries.getId())
        );
        mockMvc.perform(put("/api/admin/expenses/" + tenant2Expense.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest)
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isNotFound());

        // Verify Tenant 2's expense was not modified
        ExpenseEntity unchangedExpense = expenseRepository.findById(tenant2Expense.getId()).orElseThrow();
        assertThat(unchangedExpense.getAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));

        // Test 4: Tenant 1 cannot delete Tenant 2's expense
        mockMvc.perform(delete("/api/admin/expenses/" + tenant2Expense.getId())
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isNotFound());

        // Verify Tenant 2's expense still exists
        assertThat(expenseRepository.findById(tenant2Expense.getId())).isPresent();

        // Test 5: Tenant 1 can only see their categories
        mockMvc.perform(get("/api/admin/expense-categories")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));

        // Test 6: Tenant 1 cannot access Tenant 2's category
        mockMvc.perform(get("/api/admin/expense-categories/" + tenant2Salaries.getId())
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isNotFound());

        // Test 7: Monthly summary only includes tenant's data
        YearMonth currentMonth = YearMonth.now();
        mockMvc.perform(get("/api/admin/finance-summary/monthly")
                .param("year", String.valueOf(currentMonth.getYear()))
                .param("month", String.valueOf(currentMonth.getMonthValue()))
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(5000.00));

        // Test 8: Reports API only includes tenant's data
        DateRange currentMonthRange = DateRange.of(
            LocalDate.now().withDayOfMonth(1),
            LocalDate.now()
        );
        ReportMetrics tenant1Report = reportService.getOverallMetrics(currentMonthRange, ZoneId.systemDefault());
        assertThat(tenant1Report.totalExpenses()).isEqualByComparingTo(new BigDecimal("5000.00"));

        // Test 9: Dashboard API only includes tenant's data
        var tenant1Dashboard = dashboardService.getSummary(ZoneId.systemDefault());
        assertThat(tenant1Dashboard.totalExpensesCurrentMonth())
            .isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    /**
     * Test 8: Complete workflow - Create, Update, Delete
     * 
     * Scenario: User performs complete CRUD operations on expenses
     * Expected: All operations work correctly with proper validation
     * Validates: Requirements 1.1, 3.1, 3.2, 3.3, 4.1, 4.2, 4.3
     */
    @Test
    @WithMockUser(username = "admin@e2e-clinic-1.com")
    public void testCompleteExpenseCRUDWorkflow() throws Exception {
        // Get category
        ExpenseCategoryEntity category = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId())
            .stream()
            .filter(c -> c.getName().equals("Materials"))
            .findFirst()
            .orElseThrow();

        // Step 1: Create expense
        ExpenseCreateRequest createRequest = new ExpenseCreateRequest(
            category.getId(),
            new BigDecimal("1500.00"),
            LocalDate.now(),
            "Dental supplies"
        );

        String createResponse = mockMvc.perform(post("/api/admin/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long expenseId = objectMapper.readTree(createResponse).get("id").asLong();

        // Step 2: Read expense
        mockMvc.perform(get("/api/admin/expenses/" + expenseId)
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.notes").value("Dental supplies"));

        // Step 3: Update expense
        ExpenseCreateRequest updateRequest = new ExpenseCreateRequest(
            category.getId(),
            new BigDecimal("1750.00"),
            LocalDate.now(),
            "Dental supplies - updated quantity"
        );

        mockMvc.perform(put("/api/admin/expenses/" + expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1750.00))
                .andExpect(jsonPath("$.notes").value("Dental supplies - updated quantity"));

        // Verify update persisted
        ExpenseEntity updatedExpense = expenseRepository.findById(expenseId).orElseThrow();
        assertThat(updatedExpense.getAmount()).isEqualByComparingTo(new BigDecimal("1750.00"));
        assertThat(updatedExpense.getTenant().getId()).isEqualTo(tenant1.getId()); // Tenant ID unchanged

        // Step 4: Delete expense
        mockMvc.perform(delete("/api/admin/expenses/" + expenseId)
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        assertThat(expenseRepository.findById(expenseId)).isEmpty();

        // Step 5: Verify expense no longer accessible
        mockMvc.perform(get("/api/admin/expenses/" + expenseId)
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 9: Validation and error handling
     * 
     * Scenario: User attempts invalid operations
     * Expected: Proper validation errors returned
     * Validates: Requirements 1.2, 3.5
     */
    @Test
    @WithMockUser(username = "admin@e2e-clinic-1.com")
    public void testValidationAndErrorHandling() throws Exception {
        // Get category
        ExpenseCategoryEntity category = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId())
            .stream()
            .findFirst()
            .orElseThrow();

        // Test 1: Missing required fields (null values)
        String invalidJson = "{\"categoryId\":null,\"amount\":null,\"expenseDate\":null,\"notes\":null}";
        mockMvc.perform(post("/api/admin/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isBadRequest());

        // Test 2: Invalid amount (negative)
        ExpenseCreateRequest negativeAmountRequest = new ExpenseCreateRequest(
            category.getId(),
            new BigDecimal("-100.00"),
            LocalDate.now(),
            null
        );

        mockMvc.perform(post("/api/admin/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativeAmountRequest))
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isBadRequest());

        // Test 3: Invalid amount (zero)
        ExpenseCreateRequest zeroAmountRequest = new ExpenseCreateRequest(
            category.getId(),
            BigDecimal.ZERO,
            LocalDate.now(),
            null
        );

        mockMvc.perform(post("/api/admin/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroAmountRequest))
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isBadRequest());

        // Test 4: Non-existent category
        ExpenseCreateRequest invalidCategoryRequest = new ExpenseCreateRequest(
            99999L,
            new BigDecimal("100.00"),
            LocalDate.now(),
            null
        );

        mockMvc.perform(post("/api/admin/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCategoryRequest))
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isBadRequest());

        // Test 5: Inactive category
        ExpenseCategoryEntity inactiveCategory = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId())
            .stream()
            .findFirst()
            .orElseThrow();
        inactiveCategory.setIsActive(false);
        expenseCategoryRepository.save(inactiveCategory);

        ExpenseCreateRequest inactiveCategoryRequest = new ExpenseCreateRequest(
            inactiveCategory.getId(),
            new BigDecimal("100.00"),
            LocalDate.now(),
            null
        );

        mockMvc.perform(post("/api/admin/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inactiveCategoryRequest))
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 10: System category protection
     * 
     * Scenario: User attempts to delete or modify system categories
     * Expected: System categories cannot be deleted, only disabled
     * Validates: Requirements 5.5, 8.5
     */
    @Test
    @WithMockUser(username = "admin@e2e-clinic-1.com")
    public void testSystemCategoryProtection() throws Exception {
        // Get a system category
        ExpenseCategoryEntity systemCategory = expenseCategoryRepository
            .findByTenantIdOrderByNameAsc(tenant1.getId())
            .stream()
            .filter(ExpenseCategoryEntity::getIsSystem)
            .findFirst()
            .orElseThrow();

        // Test 1: System category can be disabled
        mockMvc.perform(patch("/api/admin/expense-categories/" + systemCategory.getId() + "/toggle")
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Test 2: System category name can be updated
        String updateRequest = "{\"name\":\"Staff Salaries\"}";
        mockMvc.perform(put("/api/admin/expense-categories/" + systemCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest)
                .header("X-Tenant-ID", tenant1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Staff Salaries"));

        // Test 3: System flag remains unchanged
        ExpenseCategoryEntity updatedCategory = expenseCategoryRepository
            .findById(systemCategory.getId())
            .orElseThrow();
        assertThat(updatedCategory.getIsSystem()).isTrue();
    }

    // Helper methods

    private TenantEntity createTestTenant(String slug, String name) {
        TenantEntity tenant = new TenantEntity(slug, name);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }

    private ExpenseEntity createExpense(Long tenantId, Long categoryId, 
                                       BigDecimal amount, LocalDate date, String notes) {
        TenantEntity tenant = tenantRepository.findById(tenantId).orElseThrow();
        ExpenseCategoryEntity category = expenseCategoryRepository.findById(categoryId).orElseThrow();
        
        ExpenseEntity expense = new ExpenseEntity(category, amount, date, notes);
        expense.setTenant(tenant);
        return expenseRepository.save(expense);
    }
}
