package com.clinic.modules.admin.controller;

import com.clinic.modules.admin.dto.ExpenseCreateRequest;
import com.clinic.modules.admin.dto.ExpenseResponse;
import com.clinic.modules.admin.dto.ExpenseUpdateRequest;
import com.clinic.modules.core.finance.ExpenseEntity;
import com.clinic.modules.core.finance.ExpenseService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for expense management.
 * Provides endpoints for creating, listing, updating, and deleting expenses.
 * All operations enforce tenant isolation via JWT authentication.
 */
@RestController
@RequestMapping("/admin/expenses")
@Tag(name = "Expenses", description = "Endpoints for managing clinic expenses")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;
    private final TenantContextHolder tenantContextHolder;

    public ExpenseController(
            ExpenseService expenseService,
            TenantContextHolder tenantContextHolder) {
        this.expenseService = expenseService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get all expenses for the authenticated tenant with optional filtering.
     *
     * @param startDate  optional start date filter (inclusive)
     * @param endDate    optional end date filter (inclusive)
     * @param categoryId optional category ID filter
     * @return list of expenses matching the filters, ordered by date descending
     */
    @GetMapping
    @Operation(
            summary = "List expenses",
            description = "Retrieve all expenses for the authenticated tenant with optional filtering by date range and/or category. Results are ordered by expense date (newest first)."
    )
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @Parameter(description = "Start date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Category ID for filtering")
            @RequestParam(required = false) Long categoryId) {
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        logger.info("Fetching expenses - tenantId: {}, startDate: {}, endDate: {}, categoryId: {}",
                tenantId, startDate, endDate, categoryId);

        List<ExpenseEntity> expenses = expenseService.getExpenses(tenantId, startDate, endDate, categoryId);
        
        List<ExpenseResponse> response = expenses.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        logger.info("Successfully fetched expenses - tenantId: {}, count: {}", tenantId, response.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new expense for the authenticated tenant.
     *
     * @param request the expense creation request
     * @return the created expense
     */
    @PostMapping
    @Operation(
            summary = "Create expense",
            description = "Create a new expense for the authenticated tenant. The expense will be associated with the tenant automatically."
    )
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseCreateRequest request) {
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        logger.info("Creating expense - tenantId: {}, categoryId: {}, amount: {}, date: {}",
                tenantId, request.categoryId(), request.amount(), request.expenseDate());

        try {
            ExpenseEntity expense = expenseService.createExpense(
                    tenantId,
                    request.categoryId(),
                    request.amount(),
                    request.expenseDate(),
                    request.notes()
            );
            
            ExpenseResponse response = toResponse(expense);

            logger.info("Successfully created expense - tenantId: {}, expenseId: {}, amount: {}",
                    tenantId, expense.getId(), request.amount());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create expense - tenantId: {}, error: {}", tenantId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing expense.
     * Validates tenant ownership before allowing the update.
     *
     * @param id      the expense ID
     * @param request the expense update request
     * @return the updated expense
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update expense",
            description = "Update an existing expense. The expense must belong to the authenticated tenant. The tenant ID cannot be changed."
    )
    public ResponseEntity<ExpenseResponse> updateExpense(
            @Parameter(description = "Expense ID")
            @PathVariable Long id,
            @Valid @RequestBody ExpenseUpdateRequest request) {
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        logger.info("Updating expense - tenantId: {}, expenseId: {}, categoryId: {}, amount: {}, date: {}",
                tenantId, id, request.categoryId(), request.amount(), request.expenseDate());

        try {
            ExpenseEntity expense = expenseService.updateExpense(
                    tenantId,
                    id,
                    request.categoryId(),
                    request.amount(),
                    request.expenseDate(),
                    request.notes()
            );
            
            ExpenseResponse response = toResponse(expense);

            logger.info("Successfully updated expense - tenantId: {}, expenseId: {}, amount: {}",
                    tenantId, id, request.amount());

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update expense - tenantId: {}, expenseId: {}, error: {}",
                    tenantId, id, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete an expense.
     * Validates tenant ownership before allowing the deletion.
     *
     * @param id the expense ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete expense",
            description = "Delete an existing expense. The expense must belong to the authenticated tenant."
    )
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "Expense ID")
            @PathVariable Long id) {
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        logger.info("Deleting expense - tenantId: {}, expenseId: {}", tenantId, id);

        try {
            expenseService.deleteExpense(tenantId, id);

            logger.info("Successfully deleted expense - tenantId: {}, expenseId: {}", tenantId, id);

            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete expense - tenantId: {}, expenseId: {}, error: {}",
                    tenantId, id, e.getMessage());
            throw e;
        }
    }

    /**
     * Convert entity to response DTO.
     *
     * @param entity the expense entity
     * @return the response DTO
     */
    private ExpenseResponse toResponse(ExpenseEntity entity) {
        return new ExpenseResponse(
                entity.getId(),
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                entity.getAmount(),
                entity.getExpenseDate(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
