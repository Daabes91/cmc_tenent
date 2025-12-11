package com.clinic.modules.ecommerce.controller.admin;

import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.ecommerce.config.RequiresEcommerceFeature;
import com.clinic.modules.ecommerce.dto.*;
import com.clinic.modules.ecommerce.model.CategoryEntity;
import com.clinic.modules.ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin controller for category management.
 * Provides endpoints for creating, listing, updating, and deleting categories.
 * All operations enforce tenant isolation and e-commerce feature validation.
 */
@RestController
@RequestMapping("/admin/tenants/{tenantId}/categories")
@Tag(name = "Admin Categories", description = "Admin endpoints for managing categories")
@SecurityRequirement(name = "bearerAuth")
@RequiresEcommerceFeature
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;
    private final TenantContextHolder tenantContextHolder;

    public CategoryController(
            CategoryService categoryService,
            TenantContextHolder tenantContextHolder) {
        this.categoryService = categoryService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get all categories for the tenant with pagination.
     *
     * @param tenantId the tenant ID from path
     * @param search optional search term
     * @param pageable pagination parameters
     * @return page of categories
     */
    @GetMapping
    @Operation(
            summary = "List categories",
            description = "Retrieve all categories for the tenant with optional search. Results are paginated."
    )
    public ResponseEntity<Page<CategoryResponse>> getCategories(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Search term for category name or description")
            @RequestParam(required = false) String search,
            
            @PageableDefault(size = 20) Pageable pageable) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching categories - tenantId: {}, search: {}, page: {}", 
                   tenantId, search, pageable.getPageNumber());

        Page<CategoryEntity> categories;
        
        if (search != null && !search.trim().isEmpty()) {
            categories = categoryService.searchCategories(tenantId, search.trim(), pageable);
        } else {
            categories = categoryService.getCategories(tenantId, pageable);
        }
        
        Page<CategoryResponse> response = categories.map(this::toResponse);

        logger.info("Successfully fetched categories - tenantId: {}, count: {}", 
                   tenantId, response.getNumberOfElements());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get root categories for the tenant.
     *
     * @param tenantId the tenant ID from path
     * @param activeOnly whether to return only active categories
     * @return list of root categories
     */
    @GetMapping("/root")
    @Operation(
            summary = "List root categories",
            description = "Retrieve all root categories (categories without parent) for the tenant."
    )
    public ResponseEntity<List<CategoryResponse>> getRootCategories(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Return only active categories")
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching root categories - tenantId: {}, activeOnly: {}", tenantId, activeOnly);

        List<CategoryEntity> categories = activeOnly 
            ? categoryService.getActiveRootCategories(tenantId)
            : categoryService.getRootCategories(tenantId);
        
        List<CategoryResponse> response = categories.stream()
                .map(this::toResponseWithChildren)
                .collect(Collectors.toList());

        logger.info("Successfully fetched root categories - tenantId: {}, count: {}", tenantId, response.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get child categories for a parent category.
     *
     * @param tenantId the tenant ID from path
     * @param parentId the parent category ID
     * @param activeOnly whether to return only active categories
     * @return list of child categories
     */
    @GetMapping("/{parentId}/children")
    @Operation(
            summary = "List child categories",
            description = "Retrieve all child categories for a parent category."
    )
    public ResponseEntity<List<CategoryResponse>> getChildCategories(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Parent Category ID")
            @PathVariable Long parentId,
            
            @Parameter(description = "Return only active categories")
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching child categories - tenantId: {}, parentId: {}, activeOnly: {}", 
                   tenantId, parentId, activeOnly);

        List<CategoryEntity> categories = activeOnly 
            ? categoryService.getActiveChildCategories(parentId, tenantId)
            : categoryService.getChildCategories(parentId, tenantId);
        
        List<CategoryResponse> response = categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        logger.info("Successfully fetched child categories - tenantId: {}, parentId: {}, count: {}", 
                   tenantId, parentId, response.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific category by ID.
     *
     * @param tenantId the tenant ID from path
     * @param categoryId the category ID
     * @return the category details
     */
    @GetMapping("/{categoryId}")
    @Operation(
            summary = "Get category",
            description = "Retrieve a specific category by ID with all its details including children."
    )
    public ResponseEntity<CategoryResponse> getCategory(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Category ID")
            @PathVariable Long categoryId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching category - tenantId: {}, categoryId: {}", tenantId, categoryId);

        CategoryEntity category = categoryService.getCategory(categoryId, tenantId);
        CategoryResponse response = toResponseWithChildren(category);

        logger.info("Successfully fetched category - tenantId: {}, categoryId: {}", tenantId, categoryId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new category.
     *
     * @param tenantId the tenant ID from path
     * @param request the category creation request
     * @return the created category
     */
    @PostMapping
    @Operation(
            summary = "Create category",
            description = "Create a new category for the tenant. If parentId is provided, creates a child category."
    )
    public ResponseEntity<CategoryResponse> createCategory(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Valid @RequestBody CategoryCreateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Creating category - tenantId: {}, name: {}, slug: {}, parentId: {}", 
                   tenantId, request.name(), request.slug(), request.parentId());

        try {
            CategoryEntity category;
            
            if (request.parentId() != null) {
                category = categoryService.createChildCategory(
                    tenantId, request.parentId(), request.name(), request.slug());
            } else {
                category = categoryService.createRootCategory(tenantId, request.name(), request.slug());
            }
            
            // Update additional fields if provided
            if (request.description() != null || request.isActive() != null) {
                category = categoryService.updateCategory(
                    category.getId(), tenantId, null, request.description());
                
                if (request.isActive() != null) {
                    category = categoryService.updateCategoryStatus(
                        category.getId(), tenantId, request.isActive());
                }
            }
            
            if (request.sortOrder() != null) {
                category = categoryService.updateCategorySortOrder(
                    category.getId(), tenantId, request.sortOrder());
            }
            
            CategoryResponse response = toResponse(category);

            logger.info("Successfully created category - tenantId: {}, categoryId: {}", 
                       tenantId, category.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create category - tenantId: {}, error: {}", tenantId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing category.
     *
     * @param tenantId the tenant ID from path
     * @param categoryId the category ID
     * @param request the category update request
     * @return the updated category
     */
    @PutMapping("/{categoryId}")
    @Operation(
            summary = "Update category",
            description = "Update an existing category. Only provided fields will be updated."
    )
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Category ID")
            @PathVariable Long categoryId,
            
            @Valid @RequestBody CategoryUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating category - tenantId: {}, categoryId: {}", tenantId, categoryId);

        try {
            CategoryEntity category = categoryService.updateCategory(
                categoryId, tenantId, request.name(), request.description());
            
            if (request.isActive() != null) {
                category = categoryService.updateCategoryStatus(categoryId, tenantId, request.isActive());
            }
            
            if (request.sortOrder() != null) {
                category = categoryService.updateCategorySortOrder(categoryId, tenantId, request.sortOrder());
            }
            
            if (request.parentId() != null) {
                category = categoryService.moveCategoryToParent(categoryId, tenantId, request.parentId());
            }
            
            CategoryResponse response = toResponse(category);

            logger.info("Successfully updated category - tenantId: {}, categoryId: {}", tenantId, categoryId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update category - tenantId: {}, categoryId: {}, error: {}", 
                        tenantId, categoryId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update category status.
     *
     * @param tenantId the tenant ID from path
     * @param categoryId the category ID
     * @param request the status update request
     * @return the updated category
     */
    @PutMapping("/{categoryId}/status")
    @Operation(
            summary = "Update category status",
            description = "Update the active status of a category."
    )
    public ResponseEntity<CategoryResponse> updateCategoryStatus(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Category ID")
            @PathVariable Long categoryId,
            
            @Valid @RequestBody CategoryStatusUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating category status - tenantId: {}, categoryId: {}, active: {}", 
                   tenantId, categoryId, request.isActive());

        try {
            CategoryEntity category = categoryService.updateCategoryStatus(
                categoryId, tenantId, request.isActive());
            CategoryResponse response = toResponse(category);

            logger.info("Successfully updated category status - tenantId: {}, categoryId: {}, active: {}", 
                       tenantId, categoryId, request.isActive());

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update category status - tenantId: {}, categoryId: {}, error: {}", 
                        tenantId, categoryId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update category sort order.
     *
     * @param tenantId the tenant ID from path
     * @param categoryId the category ID
     * @param request the sort order update request
     * @return the updated category
     */
    @PutMapping("/{categoryId}/sort-order")
    @Operation(
            summary = "Update category sort order",
            description = "Update the sort order of a category within its parent level."
    )
    public ResponseEntity<CategoryResponse> updateCategorySortOrder(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Category ID")
            @PathVariable Long categoryId,
            
            @Valid @RequestBody CategorySortOrderUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating category sort order - tenantId: {}, categoryId: {}, sortOrder: {}", 
                   tenantId, categoryId, request.sortOrder());

        try {
            CategoryEntity category = categoryService.updateCategorySortOrder(
                categoryId, tenantId, request.sortOrder());
            CategoryResponse response = toResponse(category);

            logger.info("Successfully updated category sort order - tenantId: {}, categoryId: {}", 
                       tenantId, categoryId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update category sort order - tenantId: {}, categoryId: {}, error: {}", 
                        tenantId, categoryId, e.getMessage());
            throw e;
        }
    }

    /**
     * Move category to a different parent.
     *
     * @param tenantId the tenant ID from path
     * @param categoryId the category ID
     * @param request the parent move request
     * @return the updated category
     */
    @PutMapping("/{categoryId}/move")
    @Operation(
            summary = "Move category to different parent",
            description = "Move a category to a different parent. Set parentId to null to make it a root category."
    )
    public ResponseEntity<CategoryResponse> moveCategoryToParent(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Category ID")
            @PathVariable Long categoryId,
            
            @Valid @RequestBody CategoryMoveRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Moving category - tenantId: {}, categoryId: {}, newParentId: {}", 
                   tenantId, categoryId, request.parentId());

        try {
            CategoryEntity category = categoryService.moveCategoryToParent(
                categoryId, tenantId, request.parentId());
            CategoryResponse response = toResponse(category);

            logger.info("Successfully moved category - tenantId: {}, categoryId: {}, newParentId: {}", 
                       tenantId, categoryId, request.parentId());

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to move category - tenantId: {}, categoryId: {}, error: {}", 
                        tenantId, categoryId, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a category.
     *
     * @param tenantId the tenant ID from path
     * @param categoryId the category ID
     * @return no content response
     */
    @DeleteMapping("/{categoryId}")
    @Operation(
            summary = "Delete category",
            description = "Delete a category. The category must not have child categories or associated products."
    )
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Category ID")
            @PathVariable Long categoryId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Deleting category - tenantId: {}, categoryId: {}", tenantId, categoryId);

        try {
            categoryService.deleteCategory(categoryId, tenantId);

            logger.info("Successfully deleted category - tenantId: {}, categoryId: {}", tenantId, categoryId);

            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Failed to delete category - tenantId: {}, categoryId: {}, error: {}", 
                        tenantId, categoryId, e.getMessage());
            throw e;
        }
    }

    /**
     * Validate that the authenticated user has access to the specified tenant.
     */
    private void validateTenantAccess(Long tenantId) {
        Long authenticatedTenantId = tenantContextHolder.requireTenantId();
        if (!authenticatedTenantId.equals(tenantId)) {
            throw new IllegalArgumentException("Access denied to tenant: " + tenantId);
        }
    }

    /**
     * Convert entity to response DTO without children.
     */
    private CategoryResponse toResponse(CategoryEntity entity) {
        return new CategoryResponse(
                entity.getId(),
                entity.getParent() != null ? entity.getParent().getId() : null,
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getSortOrder(),
                entity.getIsActive(),
                entity.getFullPath(),
                entity.getDepth(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                List.of() // Don't include children to avoid deep nesting
        );
    }

    /**
     * Convert entity to response DTO with children.
     */
    private CategoryResponse toResponseWithChildren(CategoryEntity entity) {
        List<CategoryResponse> children = entity.getChildren().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return new CategoryResponse(
                entity.getId(),
                entity.getParent() != null ? entity.getParent().getId() : null,
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getSortOrder(),
                entity.getIsActive(),
                entity.getFullPath(),
                entity.getDepth(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                children
        );
    }
}