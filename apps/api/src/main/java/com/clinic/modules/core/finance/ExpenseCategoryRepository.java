package com.clinic.modules.core.finance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ExpenseCategoryEntity operations.
 * Provides methods for querying expense categories with tenant isolation.
 */
@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategoryEntity, Long> {

    /**
     * Find all active categories for a specific tenant, ordered by name.
     *
     * @param tenantId the tenant ID
     * @param isActive the active status filter
     * @return list of active expense categories
     */
    List<ExpenseCategoryEntity> findByTenantIdAndIsActiveOrderByNameAsc(Long tenantId, Boolean isActive);

    /**
     * Find all categories for a specific tenant, ordered by name.
     *
     * @param tenantId the tenant ID
     * @return list of all expense categories for the tenant
     */
    List<ExpenseCategoryEntity> findByTenantIdOrderByNameAsc(Long tenantId);

    /**
     * Check if a category with the given name exists for a tenant.
     *
     * @param tenantId the tenant ID
     * @param name     the category name
     * @return true if a category with this name exists for the tenant
     */
    boolean existsByTenantIdAndName(Long tenantId, String name);

    /**
     * Check if a category with the given name exists for a tenant, excluding a specific ID.
     * Used for update operations to check uniqueness.
     *
     * @param tenantId the tenant ID
     * @param name     the category name
     * @param id       the category ID to exclude
     * @return true if a category with this name exists for the tenant (excluding the given ID)
     */
    @Query("SELECT COUNT(c) > 0 FROM ExpenseCategoryEntity c WHERE c.tenant.id = :tenantId AND c.name = :name AND c.id != :id")
    boolean existsByTenantIdAndNameAndIdNot(@Param("tenantId") Long tenantId, @Param("name") String name, @Param("id") Long id);

    /**
     * Find a category by ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param id       the category ID
     * @return optional containing the category if found
     */
    Optional<ExpenseCategoryEntity> findByTenantIdAndId(Long tenantId, Long id);

    /**
     * Find all system categories for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @param isSystem the system flag
     * @return list of system expense categories
     */
    List<ExpenseCategoryEntity> findByTenantIdAndIsSystem(Long tenantId, Boolean isSystem);

    /**
     * Count categories for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @return count of categories for the tenant
     */
    long countByTenantId(Long tenantId);
}
