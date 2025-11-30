package com.clinic.modules.core.finance;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing expense categories.
 * Provides operations for listing, creating, updating, and toggling category status.
 * All operations enforce strict tenant isolation.
 */
@Service
public class ExpenseCategoryService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseCategoryService.class);

    private final ExpenseCategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;

    public ExpenseCategoryService(
            ExpenseCategoryRepository categoryRepository,
            TenantRepository tenantRepository) {
        this.categoryRepository = categoryRepository;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Get all categories for a tenant, optionally filtered by active status.
     *
     * @param tenantId   the tenant ID
     * @param activeOnly if true, return only active categories; if false, return all categories
     * @return list of expense categories ordered by name
     */
    @Transactional(readOnly = true)
    public List<ExpenseCategoryEntity> getCategories(Long tenantId, boolean activeOnly) {
        log.debug("Fetching categories - tenantId: {}, activeOnly: {}", tenantId, activeOnly);

        List<ExpenseCategoryEntity> categories;
        if (activeOnly) {
            categories = categoryRepository.findByTenantIdAndIsActiveOrderByNameAsc(tenantId, true);
        } else {
            categories = categoryRepository.findByTenantIdOrderByNameAsc(tenantId);
        }

        log.info("Categories fetched - tenantId: {}, activeOnly: {}, count: {}",
                tenantId, activeOnly, categories.size());
        return categories;
    }

    /**
     * Create a new custom expense category for a tenant.
     * System categories cannot be created through this method.
     *
     * @param tenantId the tenant ID
     * @param name     the category name
     * @return the created category
     * @throws IllegalArgumentException if tenant not found or category name already exists
     */
    @Transactional
    public ExpenseCategoryEntity createCategory(Long tenantId, String name) {
        log.info("Creating custom category - tenantId: {}, name: {}", tenantId, name);

        // Validate tenant exists
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    log.error("Category creation failed - tenant not found: tenantId: {}", tenantId);
                    return new IllegalArgumentException("Tenant with ID " + tenantId + " not found");
                });

        // Validate name is not blank
        if (name == null || name.trim().isEmpty()) {
            log.error("Category creation failed - name is blank: tenantId: {}", tenantId);
            throw new IllegalArgumentException("Category name cannot be blank");
        }

        String trimmedName = name.trim();

        // Check uniqueness within tenant
        if (categoryRepository.existsByTenantIdAndName(tenantId, trimmedName)) {
            log.error("Category creation failed - duplicate name: tenantId: {}, name: {}",
                    tenantId, trimmedName);
            throw new IllegalArgumentException(
                    "Category with name '" + trimmedName + "' already exists for this tenant");
        }

        // Create custom category (is_system = false, is_active = true)
        ExpenseCategoryEntity category = new ExpenseCategoryEntity(
                trimmedName,
                false,  // is_system = false for custom categories
                true    // is_active = true by default
        );
        category.setTenant(tenant);

        ExpenseCategoryEntity savedCategory = categoryRepository.save(category);

        log.info("Custom category created successfully - tenantId: {}, categoryId: {}, name: {}, isSystem: false, isActive: true",
                tenantId, savedCategory.getId(), trimmedName);

        return savedCategory;
    }

    /**
     * Update an existing category's name.
     * Validates tenant ownership and name uniqueness.
     *
     * @param tenantId the tenant ID
     * @param id       the category ID
     * @param name     the new category name
     * @return the updated category
     * @throws IllegalArgumentException if category not found, tenant mismatch, or name already exists
     */
    @Transactional
    public ExpenseCategoryEntity updateCategory(Long tenantId, Long id, String name) {
        log.info("Updating category - tenantId: {}, categoryId: {}, newName: {}",
                tenantId, id, name);

        // Validate name is not blank
        if (name == null || name.trim().isEmpty()) {
            log.error("Category update failed - name is blank: tenantId: {}, categoryId: {}",
                    tenantId, id);
            throw new IllegalArgumentException("Category name cannot be blank");
        }

        String trimmedName = name.trim();

        // Find category and validate tenant ownership
        ExpenseCategoryEntity category = categoryRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> {
                    log.error("Category update failed - category not found or tenant mismatch: tenantId: {}, categoryId: {}",
                            tenantId, id);
                    return new IllegalArgumentException(
                            "Category with ID " + id + " not found for tenant " + tenantId);
                });

        // Check uniqueness within tenant (excluding current category)
        if (categoryRepository.existsByTenantIdAndNameAndIdNot(tenantId, trimmedName, id)) {
            log.error("Category update failed - duplicate name: tenantId: {}, categoryId: {}, name: {}",
                    tenantId, id, trimmedName);
            throw new IllegalArgumentException(
                    "Category with name '" + trimmedName + "' already exists for this tenant");
        }

        // Update name
        String oldName = category.getName();
        category.updateName(trimmedName);
        ExpenseCategoryEntity updatedCategory = categoryRepository.save(category);

        log.info("Category updated successfully - tenantId: {}, categoryId: {}, oldName: {}, newName: {}",
                tenantId, id, oldName, trimmedName);

        return updatedCategory;
    }

    /**
     * Toggle the active status of a category.
     * System categories can be disabled but not deleted.
     *
     * @param tenantId the tenant ID
     * @param id       the category ID
     * @return the updated category
     * @throws IllegalArgumentException if category not found or tenant mismatch
     */
    @Transactional
    public ExpenseCategoryEntity toggleActive(Long tenantId, Long id) {
        log.info("Toggling category active status - tenantId: {}, categoryId: {}", tenantId, id);

        // Find category and validate tenant ownership
        ExpenseCategoryEntity category = categoryRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> {
                    log.error("Category toggle failed - category not found or tenant mismatch: tenantId: {}, categoryId: {}",
                            tenantId, id);
                    return new IllegalArgumentException(
                            "Category with ID " + id + " not found for tenant " + tenantId);
                });

        // Toggle active status
        boolean oldStatus = category.getIsActive();
        category.toggleActive();
        ExpenseCategoryEntity updatedCategory = categoryRepository.save(category);

        log.info("Category active status toggled - tenantId: {}, categoryId: {}, oldStatus: {}, newStatus: {}, isSystem: {}",
                tenantId, id, oldStatus, updatedCategory.getIsActive(), category.getIsSystem());

        return updatedCategory;
    }
}
