package com.clinic.modules.core.finance;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Service for seeding default expense categories for new tenants.
 * Creates 7 system categories: Salaries, Rent, Electricity, Water, Internet/Phone, Materials, Other.
 */
@Service
public class CategorySeedingService {

    private static final Logger log = LoggerFactory.getLogger(CategorySeedingService.class);

    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Salaries",
            "Rent",
            "Electricity",
            "Water",
            "Internet/Phone",
            "Materials",
            "Other"
    );

    private final ExpenseCategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;

    public CategorySeedingService(
            ExpenseCategoryRepository categoryRepository,
            TenantRepository tenantRepository) {
        this.categoryRepository = categoryRepository;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Seeds default expense categories for a tenant.
     * This operation is idempotent - if categories already exist, it will skip seeding.
     *
     * @param tenantId the tenant ID to seed categories for
     * @throws IllegalArgumentException if tenant does not exist
     */
    @Transactional
    public void seedDefaultCategories(Long tenantId) {
        log.info("Category seeding initiated - tenantId: {}", tenantId);

        // Verify tenant exists
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    log.error("Category seeding failed - tenant not found: tenantId: {}", tenantId);
                    return new IllegalArgumentException("Tenant with ID " + tenantId + " not found");
                });

        // Check if categories already seeded (idempotent check)
        long existingCategoryCount = categoryRepository.countByTenantId(tenantId);
        if (existingCategoryCount > 0) {
            log.info("Category seeding skipped - categories already exist: tenantId: {}, existingCount: {}",
                    tenantId, existingCategoryCount);
            return;
        }

        // Create and save default categories
        int seededCount = 0;
        for (String categoryName : DEFAULT_CATEGORIES) {
            ExpenseCategoryEntity category = new ExpenseCategoryEntity(
                    categoryName,
                    true,  // is_system = true
                    true   // is_active = true
            );
            category.setTenant(tenant);
            categoryRepository.save(category);
            seededCount++;
            log.debug("System category created - tenantId: {}, categoryName: {}, isSystem: true, isActive: true",
                    tenantId, categoryName);
        }

        log.info("Category seeding completed successfully - tenantId: {}, categoriesSeeded: {}",
                tenantId, seededCount);
    }

    /**
     * Gets the list of default category names.
     *
     * @return list of default category names
     */
    public List<String> getDefaultCategoryNames() {
        return DEFAULT_CATEGORIES;
    }
}
