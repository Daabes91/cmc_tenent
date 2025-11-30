package com.clinic.modules.admin.controller;

import com.clinic.modules.admin.dto.ExpenseCategoryCreateRequest;
import com.clinic.modules.admin.dto.ExpenseCategoryResponse;
import com.clinic.modules.admin.dto.ExpenseCategoryUpdateRequest;
import com.clinic.modules.core.finance.ExpenseCategoryEntity;
import com.clinic.modules.core.finance.ExpenseCategoryService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for expense category management.
 * Provides endpoints for listing, creating, updating, and toggling category status.
 * All operations enforce tenant isolation via JWT authentication.
 */
@RestController
@RequestMapping("/admin/expense-categories")
@Tag(name = "Expense Categories", description = "Endpoints for managing expense categories")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseCategoryController.class);

    private final ExpenseCategoryService categoryService;
    private final TenantContextHolder tenantContextHolder;

    public ExpenseCategoryController(
            ExpenseCategoryService categoryService,
            TenantContextHolder tenantContextHolder) {
        this.categoryService = categoryService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get all expense categories for the authenticated tenant.
     * Optionally filter by active status.
     *
     * @param activeOnly if true, return only active categories; if false or null, return all categories
     * @return list of expense categories ordered by name
     */
    @GetMapping
    @Operation(
            summary = "List expense categories",
            description = "Retrieve all expense categories for the authenticated tenant, optionally filtered by active status. Results are ordered alphabetically by name."
    )
    public ResponseEntity<List<ExpenseCategoryResponse>> getCategories(
            @Parameter(description = "Filter by active status (true = active only, false/null = all)")
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        logger.info("Fetching expense categories - tenantId: {}, activeOnly: {}", tenantId, activeOnly);

        List<ExpenseCategoryEntity> categories = categoryService.getCategories(tenantId, activeOnly);
        
        List<ExpenseCategoryResponse> response = categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        logger.info("Successfully fetched expense categories - tenantId: {}, count: {}", 
                tenantId, response.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new custom expense category.
     * System categories cannot be created through this endpoint.
     *
     * @param request the category creation request
     * @return the created category
     */
    @PostMapping
    @Operation(
            summary = "Create expense category",
            description = "Create a new custom expense category for the authenticated tenant. The category will be marked as non-system (is_system=false) and active (is_active=true) by default."
    )
    public ResponseEntity<ExpenseCategoryResponse> createCategory(
            @Valid @RequestBody ExpenseCategoryCreateRequest request) {
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        logger.info("Creating expense category - tenantId: {}, name: {}", tenantId, request.name());

        try {
            ExpenseCategoryEntity category = categoryService.createCategory(tenantId, request.name());
            ExpenseCategoryResponse response = toResponse(category);

            logger.info("Successfully created expense category - tenantId: {}, categoryId: {}, name: {}",
                    tenantId, category.getId(), request.name());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create expense category - tenantId: {}, name: {}, error: {}",
                    tenantId, request.name(), e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing expense category's name.
     * Validates tenant ownership and name uniqueness.
     *
     * @param id      the category ID
     * @param request the category update request
     * @return the updated category
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update expense category",
            description = "Update an existing expense category's name. The category must belong to the authenticated tenant, and the new name must be unique within the tenant."
    )
    public ResponseEntity<ExpenseCategoryResponse> updateCategory(
            @Parameter(description = "Category ID")
            @PathVariable Long id,
            @Valid @RequestBody ExpenseCategoryUpdateRequest request) {
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        logger.info("Updating expense category - tenantId: {}, categoryId: {}, newName: {}",
                tenantId, id, request.name());

        try {
            ExpenseCategoryEntity category = categoryService.updateCategory(tenantId, id, request.name());
            ExpenseCategoryResponse response = toResponse(category);

            logger.info("Successfully updated expense category - tenantId: {}, categoryId: {}, newName: {}",
                    tenantId, id, request.name());

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update expense category - tenantId: {}, categoryId: {}, error: {}",
                    tenantId, id, e.getMessage());
            throw e;
        }
    }

    /**
     * Toggle the active status of an expense category.
     * System categories can be disabled but not deleted.
     *
     * @param id the category ID
     * @return the updated category
     */
    @PatchMapping("/{id}/toggle")
    @Operation(
            summary = "Toggle category active status",
            description = "Toggle the active status of an expense category. If active, it will be disabled; if disabled, it will be activated. System categories can be disabled but not deleted."
    )
    public ResponseEntity<ExpenseCategoryResponse> toggleActive(
            @Parameter(description = "Category ID")
            @PathVariable Long id) {
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        logger.info("Toggling expense category active status - tenantId: {}, categoryId: {}", tenantId, id);

        try {
            ExpenseCategoryEntity category = categoryService.toggleActive(tenantId, id);
            ExpenseCategoryResponse response = toResponse(category);

            logger.info("Successfully toggled expense category active status - tenantId: {}, categoryId: {}, newStatus: {}",
                    tenantId, id, category.getIsActive());

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to toggle expense category - tenantId: {}, categoryId: {}, error: {}",
                    tenantId, id, e.getMessage());
            throw e;
        }
    }

    /**
     * Convert entity to response DTO.
     *
     * @param entity the expense category entity
     * @return the response DTO
     */
    private ExpenseCategoryResponse toResponse(ExpenseCategoryEntity entity) {
        return new ExpenseCategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getIsSystem(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
