package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.CategoryEntity;
import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing categories in the e-commerce system.
 * 
 * Provides CRUD operations for categories with tenant isolation and hierarchical support.
 * Handles category hierarchy validation, sort order management, and product associations.
 */
@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;
    private final EcommerceFeatureService ecommerceFeatureService;

    public CategoryService(
            CategoryRepository categoryRepository,
            TenantRepository tenantRepository,
            EcommerceFeatureService ecommerceFeatureService) {
        this.categoryRepository = categoryRepository;
        this.tenantRepository = tenantRepository;
        this.ecommerceFeatureService = ecommerceFeatureService;
    }

    /**
     * Create a new root category.
     * 
     * @param tenantId the tenant ID
     * @param name the category name
     * @param slug the category slug
     * @return the created category
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public CategoryEntity createRootCategory(Long tenantId, String name, String slug) {
        log.debug("Creating root category for tenant {}: name={}, slug={}", tenantId, name, slug);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate required fields
        validateCategoryName(name);
        validateCategorySlug(slug);
        
        // Get tenant
        TenantEntity tenant = getTenantById(tenantId);
        
        // Check slug uniqueness
        if (categoryRepository.existsBySlugAndTenant(slug, tenantId)) {
            throw new IllegalArgumentException("Category slug already exists: " + slug);
        }
        
        // Create category
        CategoryEntity category = new CategoryEntity(tenant, name, slug);
        
        // Set sort order as the next available for root categories
        Optional<Integer> maxSortOrder = categoryRepository.findMaxSortOrderByTenantForRootCategories(tenantId);
        category.setSortOrder(maxSortOrder.orElse(0) + 1);
        
        category = categoryRepository.save(category);
        
        log.info("Created root category {} for tenant {}", category.getId(), tenantId);
        return category;
    }

    /**
     * Create a new child category.
     * 
     * @param tenantId the tenant ID
     * @param parentId the parent category ID
     * @param name the category name
     * @param slug the category slug
     * @return the created category
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public CategoryEntity createChildCategory(Long tenantId, Long parentId, String name, String slug) {
        log.debug("Creating child category for tenant {} under parent {}: name={}, slug={}", 
                 tenantId, parentId, name, slug);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate required fields
        validateCategoryName(name);
        validateCategorySlug(slug);
        
        // Get tenant and parent category
        TenantEntity tenant = getTenantById(tenantId);
        CategoryEntity parent = getCategory(parentId, tenantId);
        
        // Check slug uniqueness
        if (categoryRepository.existsBySlugAndTenant(slug, tenantId)) {
            throw new IllegalArgumentException("Category slug already exists: " + slug);
        }
        
        // Validate hierarchy depth (prevent too deep nesting)
        if (parent.getDepth() >= 5) { // Max depth of 5 levels
            throw new IllegalArgumentException("Maximum category depth exceeded");
        }
        
        // Create category
        CategoryEntity category = new CategoryEntity(tenant, parent, name, slug);
        
        // Set sort order as the next available for this parent
        Optional<Integer> maxSortOrder = categoryRepository.findMaxSortOrderByTenantAndParent(tenantId, parentId);
        category.setSortOrder(maxSortOrder.orElse(0) + 1);
        
        category = categoryRepository.save(category);
        
        log.info("Created child category {} for tenant {} under parent {}", category.getId(), tenantId, parentId);
        return category;
    }

    /**
     * Get a category by ID with tenant validation.
     * 
     * @param categoryId the category ID
     * @param tenantId the tenant ID
     * @return the category
     * @throws ProductNotFoundException if category not found
     */
    @Transactional(readOnly = true)
    public CategoryEntity getCategory(Long categoryId, Long tenantId) {
        log.debug("Getting category {} for tenant {}", categoryId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return categoryRepository.findByIdAndTenant(categoryId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(null, tenantId, 
                    "Category with ID " + categoryId + " not found"));
    }

    /**
     * Get a category by slug with tenant validation.
     * 
     * @param slug the category slug
     * @param tenantId the tenant ID
     * @return the category
     * @throws ProductNotFoundException if category not found
     */
    @Transactional(readOnly = true)
    public CategoryEntity getCategoryBySlug(String slug, Long tenantId) {
        log.debug("Getting category by slug {} for tenant {}", slug, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return categoryRepository.findBySlugAndTenant(slug, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(null, tenantId, 
                    "Category with slug " + slug + " not found"));
    }

    /**
     * Update a category.
     * 
     * @param categoryId the category ID
     * @param tenantId the tenant ID
     * @param name the new name (optional)
     * @param description the new description (optional)
     * @return the updated category
     */
    @Transactional
    public CategoryEntity updateCategory(Long categoryId, Long tenantId, String name, String description) {
        log.debug("Updating category {} for tenant {}", categoryId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        CategoryEntity category = getCategory(categoryId, tenantId);
        
        // Update fields if provided
        if (StringUtils.hasText(name)) {
            validateCategoryName(name);
            category.setName(name);
        }
        
        if (description != null) {
            category.setDescription(description);
        }
        
        category = categoryRepository.save(category);
        
        log.info("Updated category {} for tenant {}", categoryId, tenantId);
        return category;
    }

    /**
     * Update category active status.
     * 
     * @param categoryId the category ID
     * @param tenantId the tenant ID
     * @param active the active status
     * @return the updated category
     */
    @Transactional
    public CategoryEntity updateCategoryStatus(Long categoryId, Long tenantId, boolean active) {
        log.debug("Updating category {} status to {} for tenant {}", categoryId, active, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        CategoryEntity category = getCategory(categoryId, tenantId);
        category.setIsActive(active);
        category = categoryRepository.save(category);
        
        log.info("Updated category {} status to {} for tenant {}", categoryId, active, tenantId);
        return category;
    }

    /**
     * Update category sort order.
     * 
     * @param categoryId the category ID
     * @param tenantId the tenant ID
     * @param newSortOrder the new sort order
     * @return the updated category
     */
    @Transactional
    public CategoryEntity updateCategorySortOrder(Long categoryId, Long tenantId, int newSortOrder) {
        log.debug("Updating category {} sort order to {} for tenant {}", categoryId, newSortOrder, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        CategoryEntity category = getCategory(categoryId, tenantId);
        
        if (newSortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
        
        int currentSortOrder = category.getSortOrder();
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        
        if (newSortOrder != currentSortOrder) {
            // Adjust other categories' sort orders
            if (newSortOrder > currentSortOrder) {
                // Moving down: decrement sort orders between current and new position
                categoryRepository.decrementSortOrderAfter(tenantId, parentId, currentSortOrder);
            } else {
                // Moving up: increment sort orders from new position
                categoryRepository.incrementSortOrderFrom(tenantId, parentId, newSortOrder);
            }
            
            category.setSortOrder(newSortOrder);
            category = categoryRepository.save(category);
        }
        
        log.info("Updated category {} sort order to {} for tenant {}", categoryId, newSortOrder, tenantId);
        return category;
    }

    /**
     * Move category to a different parent.
     * 
     * @param categoryId the category ID
     * @param tenantId the tenant ID
     * @param newParentId the new parent ID (null for root)
     * @return the updated category
     */
    @Transactional
    public CategoryEntity moveCategoryToParent(Long categoryId, Long tenantId, Long newParentId) {
        log.debug("Moving category {} to parent {} for tenant {}", categoryId, newParentId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        CategoryEntity category = getCategory(categoryId, tenantId);
        
        // Validate new parent if provided
        CategoryEntity newParent = null;
        if (newParentId != null) {
            newParent = getCategory(newParentId, tenantId);
            
            // Prevent circular references
            if (newParent.isDescendantOf(category)) {
                throw new IllegalArgumentException("Cannot move category to its own descendant");
            }
            
            // Validate hierarchy depth
            if (newParent.getDepth() >= 4) { // Max depth of 5 levels
                throw new IllegalArgumentException("Maximum category depth would be exceeded");
            }
        }
        
        category.setParent(newParent);
        
        // Set sort order as the last in the new parent
        Optional<Integer> maxSortOrder = newParentId != null 
            ? categoryRepository.findMaxSortOrderByTenantAndParent(tenantId, newParentId)
            : categoryRepository.findMaxSortOrderByTenantForRootCategories(tenantId);
        category.setSortOrder(maxSortOrder.orElse(0) + 1);
        
        category = categoryRepository.save(category);
        
        log.info("Moved category {} to parent {} for tenant {}", categoryId, newParentId, tenantId);
        return category;
    }

    /**
     * Delete a category.
     * 
     * @param categoryId the category ID
     * @param tenantId the tenant ID
     */
    @Transactional
    public void deleteCategory(Long categoryId, Long tenantId) {
        log.debug("Deleting category {} for tenant {}", categoryId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        CategoryEntity category = getCategory(categoryId, tenantId);
        
        // Check if category has children
        if (categoryRepository.hasChildrenByTenantAndParent(tenantId, categoryId)) {
            throw new IllegalStateException("Cannot delete category with child categories. Delete children first.");
        }
        
        // Check if category has products
        if (categoryRepository.hasProductsByTenantAndCategory(tenantId, categoryId)) {
            throw new IllegalStateException("Cannot delete category with associated products. Remove products first.");
        }
        
        categoryRepository.delete(category);
        
        log.info("Deleted category {} for tenant {}", categoryId, tenantId);
    }

    /**
     * Get all root categories for a tenant.
     * 
     * @param tenantId the tenant ID
     * @return list of root categories
     */
    @Transactional(readOnly = true)
    public List<CategoryEntity> getRootCategories(Long tenantId) {
        log.debug("Getting root categories for tenant {}", tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return categoryRepository.findRootCategoriesByTenant(tenantId);
    }

    /**
     * Get active root categories for a tenant.
     * 
     * @param tenantId the tenant ID
     * @return list of active root categories
     */
    @Transactional(readOnly = true)
    public List<CategoryEntity> getActiveRootCategories(Long tenantId) {
        log.debug("Getting active root categories for tenant {}", tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return categoryRepository.findActiveRootCategoriesByTenant(tenantId);
    }

    /**
     * Get child categories for a parent.
     * 
     * @param parentId the parent category ID
     * @param tenantId the tenant ID
     * @return list of child categories
     */
    @Transactional(readOnly = true)
    public List<CategoryEntity> getChildCategories(Long parentId, Long tenantId) {
        log.debug("Getting child categories for parent {} tenant {}", parentId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate parent exists
        getCategory(parentId, tenantId);
        
        return categoryRepository.findChildrenByTenantAndParent(tenantId, parentId);
    }

    /**
     * Get active child categories for a parent.
     * 
     * @param parentId the parent category ID
     * @param tenantId the tenant ID
     * @return list of active child categories
     */
    @Transactional(readOnly = true)
    public List<CategoryEntity> getActiveChildCategories(Long parentId, Long tenantId) {
        log.debug("Getting active child categories for parent {} tenant {}", parentId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate parent exists
        getCategory(parentId, tenantId);
        
        return categoryRepository.findActiveChildrenByTenantAndParent(tenantId, parentId);
    }

    /**
     * Get all categories for a tenant with pagination.
     * 
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of categories
     */
    @Transactional(readOnly = true)
    public Page<CategoryEntity> getCategories(Long tenantId, Pageable pageable) {
        log.debug("Getting categories for tenant {} with pagination", tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return categoryRepository.findAllByTenant(tenantId, pageable);
    }

    /**
     * Get active categories for a tenant.
     * 
     * @param tenantId the tenant ID
     * @return list of active categories
     */
    @Transactional(readOnly = true)
    public List<CategoryEntity> getActiveCategories(Long tenantId) {
        log.debug("Getting active categories for tenant {}", tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return categoryRepository.findActiveByTenant(tenantId);
    }

    /**
     * Search categories for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param searchTerm the search term
     * @param pageable pagination parameters
     * @return page of categories
     */
    @Transactional(readOnly = true)
    public Page<CategoryEntity> searchCategories(Long tenantId, String searchTerm, Pageable pageable) {
        log.debug("Searching categories for tenant {} with term: {}", tenantId, searchTerm);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return categoryRepository.searchByTenant(tenantId, searchTerm, pageable);
    }

    /**
     * Get categories associated with a product.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @return list of categories
     */
    @Transactional(readOnly = true)
    public List<CategoryEntity> getCategoriesByProduct(Long productId, Long tenantId) {
        log.debug("Getting categories for product {} tenant {}", productId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return categoryRepository.findByTenantAndProduct(tenantId, productId);
    }

    /**
     * Associate a product with a category.
     * 
     * @param categoryId the category ID
     * @param tenantId the tenant ID
     * @param product the product to associate
     * @return the updated category
     */
    @Transactional
    public CategoryEntity addProductToCategory(Long categoryId, Long tenantId, ProductEntity product) {
        log.debug("Adding product {} to category {} for tenant {}", product.getId(), categoryId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        CategoryEntity category = getCategory(categoryId, tenantId);
        
        // Validate product belongs to same tenant
        if (!product.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Product does not belong to the same tenant");
        }
        
        category.addProduct(product);
        category = categoryRepository.save(category);
        
        log.info("Added product {} to category {} for tenant {}", product.getId(), categoryId, tenantId);
        return category;
    }

    /**
     * Remove a product from a category.
     * 
     * @param categoryId the category ID
     * @param tenantId the tenant ID
     * @param product the product to remove
     * @return the updated category
     */
    @Transactional
    public CategoryEntity removeProductFromCategory(Long categoryId, Long tenantId, ProductEntity product) {
        log.debug("Removing product {} from category {} for tenant {}", product.getId(), categoryId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        CategoryEntity category = getCategory(categoryId, tenantId);
        
        category.removeProduct(product);
        category = categoryRepository.save(category);
        
        log.info("Removed product {} from category {} for tenant {}", product.getId(), categoryId, tenantId);
        return category;
    }

    // Private helper methods

    private TenantEntity getTenantById(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    private void validateCategoryName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Category name must not exceed 255 characters");
        }
    }

    private void validateCategorySlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Category slug is required");
        }
        if (slug.length() > 255) {
            throw new IllegalArgumentException("Category slug must not exceed 255 characters");
        }
        if (!slug.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException("Category slug must contain only lowercase letters, numbers, and hyphens");
        }
    }
}