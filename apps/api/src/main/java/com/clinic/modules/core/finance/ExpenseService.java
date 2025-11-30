package com.clinic.modules.core.finance;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing expenses.
 * Provides operations for creating, updating, deleting, and querying expenses.
 * All operations enforce strict tenant isolation and validate business rules.
 */
@Service
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExpenseCategoryRepository categoryRepository,
            TenantRepository tenantRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Get expenses for a tenant with optional filtering.
     *
     * @param tenantId   the tenant ID
     * @param startDate  optional start date filter (inclusive)
     * @param endDate    optional end date filter (inclusive)
     * @param categoryId optional category ID filter
     * @return list of expenses matching the filters, ordered by date descending
     */
    @Transactional(readOnly = true)
    public List<ExpenseEntity> getExpenses(Long tenantId, LocalDate startDate, LocalDate endDate, Long categoryId) {
        log.debug("Fetching expenses - tenantId: {}, startDate: {}, endDate: {}, categoryId: {}",
                tenantId, startDate, endDate, categoryId);

        List<ExpenseEntity> expenses;

        // Apply filters based on what's provided
        if (startDate != null && endDate != null && categoryId != null) {
            // All filters
            expenses = expenseRepository.findByTenantIdAndCategoryIdAndExpenseDateBetween(
                    tenantId, categoryId, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            // Date range only
            expenses = expenseRepository.findByTenantIdAndExpenseDateBetween(
                    tenantId, startDate, endDate);
        } else if (categoryId != null) {
            // Category only
            expenses = expenseRepository.findByTenantIdAndCategoryId(tenantId, categoryId);
        } else {
            // No filters
            expenses = expenseRepository.findByTenantIdOrderByExpenseDateDesc(tenantId);
        }

        log.info("Expenses fetched - tenantId: {}, count: {}", tenantId, expenses.size());
        return expenses;
    }

    /**
     * Create a new expense for a tenant.
     * Validates required fields, amount positivity, and category ownership/active status.
     *
     * @param tenantId    the tenant ID
     * @param categoryId  the category ID
     * @param amount      the expense amount
     * @param expenseDate the date of the expense
     * @param notes       optional notes
     * @return the created expense
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public ExpenseEntity createExpense(
            Long tenantId,
            Long categoryId,
            BigDecimal amount,
            LocalDate expenseDate,
            String notes) {
        log.info("Creating expense - tenantId: {}, categoryId: {}, amount: {}, date: {}",
                tenantId, categoryId, amount, expenseDate);

        // Validate required fields
        validateRequiredFields(categoryId, amount, expenseDate);

        // Validate amount is positive
        validateAmountPositive(amount);

        // Validate tenant exists
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    log.error("Expense creation failed - tenant not found: tenantId: {}", tenantId);
                    return new IllegalArgumentException("Tenant with ID " + tenantId + " not found");
                });

        // Validate category belongs to tenant and is active
        ExpenseCategoryEntity category = validateCategoryBelongsToTenantAndIsActive(tenantId, categoryId);

        // Create expense
        ExpenseEntity expense = new ExpenseEntity(category, amount, expenseDate, notes);
        expense.setTenant(tenant);

        ExpenseEntity savedExpense = expenseRepository.save(expense);

        log.info("Expense created successfully - tenantId: {}, expenseId: {}, categoryId: {}, amount: {}",
                tenantId, savedExpense.getId(), categoryId, amount);

        return savedExpense;
    }

    /**
     * Update an existing expense.
     * Validates tenant ownership, required fields, amount positivity, and category ownership/active status.
     * Tenant ID is immutable and cannot be changed.
     *
     * @param tenantId    the tenant ID
     * @param id          the expense ID
     * @param categoryId  the new category ID
     * @param amount      the new expense amount
     * @param expenseDate the new expense date
     * @param notes       the new notes
     * @return the updated expense
     * @throws IllegalArgumentException if validation fails or expense not found
     */
    @Transactional
    public ExpenseEntity updateExpense(
            Long tenantId,
            Long id,
            Long categoryId,
            BigDecimal amount,
            LocalDate expenseDate,
            String notes) {
        log.info("Updating expense - tenantId: {}, expenseId: {}, categoryId: {}, amount: {}, date: {}",
                tenantId, id, categoryId, amount, expenseDate);

        // Validate required fields
        validateRequiredFields(categoryId, amount, expenseDate);

        // Validate amount is positive
        validateAmountPositive(amount);

        // Find expense and validate tenant ownership
        ExpenseEntity expense = expenseRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> {
                    log.error("Expense update failed - expense not found or tenant mismatch: tenantId: {}, expenseId: {}",
                            tenantId, id);
                    return new IllegalArgumentException(
                            "Expense with ID " + id + " not found for tenant " + tenantId);
                });

        // Validate category belongs to tenant and is active
        ExpenseCategoryEntity category = validateCategoryBelongsToTenantAndIsActive(tenantId, categoryId);

        // Update expense details (tenant ID remains unchanged)
        expense.updateDetails(category, amount, expenseDate, notes);
        ExpenseEntity updatedExpense = expenseRepository.save(expense);

        log.info("Expense updated successfully - tenantId: {}, expenseId: {}, categoryId: {}, amount: {}",
                tenantId, id, categoryId, amount);

        return updatedExpense;
    }

    /**
     * Delete an expense.
     * Validates tenant ownership before deletion.
     *
     * @param tenantId the tenant ID
     * @param id       the expense ID
     * @throws IllegalArgumentException if expense not found or tenant mismatch
     */
    @Transactional
    public void deleteExpense(Long tenantId, Long id) {
        log.info("Deleting expense - tenantId: {}, expenseId: {}", tenantId, id);

        // Find expense and validate tenant ownership
        ExpenseEntity expense = expenseRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> {
                    log.error("Expense deletion failed - expense not found or tenant mismatch: tenantId: {}, expenseId: {}",
                            tenantId, id);
                    return new IllegalArgumentException(
                            "Expense with ID " + id + " not found for tenant " + tenantId);
                });

        // Delete expense
        expenseRepository.delete(expense);

        log.info("Expense deleted successfully - tenantId: {}, expenseId: {}", tenantId, id);
    }

    /**
     * Validates that required fields are not null.
     */
    private void validateRequiredFields(Long categoryId, BigDecimal amount, LocalDate expenseDate) {
        if (categoryId == null) {
            log.error("Validation failed - category is required");
            throw new IllegalArgumentException("Category is required");
        }
        if (amount == null) {
            log.error("Validation failed - amount is required");
            throw new IllegalArgumentException("Amount is required");
        }
        if (expenseDate == null) {
            log.error("Validation failed - expense date is required");
            throw new IllegalArgumentException("Expense date is required");
        }
    }

    /**
     * Validates that amount is positive.
     */
    private void validateAmountPositive(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Validation failed - amount must be positive: amount: {}", amount);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    /**
     * Validates that category belongs to tenant and is active.
     */
    private ExpenseCategoryEntity validateCategoryBelongsToTenantAndIsActive(Long tenantId, Long categoryId) {
        ExpenseCategoryEntity category = categoryRepository.findByTenantIdAndId(tenantId, categoryId)
                .orElseThrow(() -> {
                    log.error("Validation failed - category not found or tenant mismatch: tenantId: {}, categoryId: {}",
                            tenantId, categoryId);
                    return new IllegalArgumentException(
                            "Category with ID " + categoryId + " not found for tenant " + tenantId);
                });

        if (!category.getIsActive()) {
            log.error("Validation failed - category is not active: tenantId: {}, categoryId: {}",
                    tenantId, categoryId);
            throw new IllegalArgumentException(
                    "Category with ID " + categoryId + " is not active");
        }

        return category;
    }
}
