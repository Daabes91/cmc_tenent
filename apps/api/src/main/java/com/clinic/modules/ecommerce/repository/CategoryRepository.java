package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CategoryEntity with tenant-scoped queries.
 * 
 * All queries automatically filter by tenant to ensure data isolation.
 * Supports hierarchical category operations.
 */
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // Basic tenant-scoped queries
    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId")
    Page<CategoryEntity> findAllByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.id = :id")
    Optional<CategoryEntity> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND LOWER(c.slug) = LOWER(:slug)")
    Optional<CategoryEntity> findBySlugAndTenant(@Param("slug") String slug, @Param("tenantId") Long tenantId);

    // Active/Inactive queries
    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<CategoryEntity> findActiveByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.isActive = true")
    Page<CategoryEntity> findActiveByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.isActive = :active")
    Page<CategoryEntity> findByTenantAndActive(@Param("tenantId") Long tenantId, @Param("active") Boolean active, Pageable pageable);

    // Hierarchical queries - Root categories
    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent IS NULL ORDER BY c.sortOrder ASC, c.name ASC")
    List<CategoryEntity> findRootCategoriesByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent IS NULL")
    Page<CategoryEntity> findRootCategoriesByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<CategoryEntity> findActiveRootCategoriesByTenant(@Param("tenantId") Long tenantId);

    // Hierarchical queries - Child categories
    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent.id = :parentId ORDER BY c.sortOrder ASC, c.name ASC")
    List<CategoryEntity> findChildrenByTenantAndParent(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent.id = :parentId")
    Page<CategoryEntity> findChildrenByTenantAndParent(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId, Pageable pageable);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent.id = :parentId AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<CategoryEntity> findActiveChildrenByTenantAndParent(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent.id IN :parentIds ORDER BY c.parent.id, c.sortOrder ASC, c.name ASC")
    List<CategoryEntity> findChildrenByTenantAndParentIn(@Param("tenantId") Long tenantId, @Param("parentIds") List<Long> parentIds);

    // Search queries
    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CategoryEntity> searchByTenant(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CategoryEntity> searchActiveByTenant(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Sort order queries
    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent.id = :parentId AND c.sortOrder = :sortOrder")
    Optional<CategoryEntity> findByTenantAndParentAndSortOrder(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId, @Param("sortOrder") Integer sortOrder);

    @Query("SELECT MAX(c.sortOrder) FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent.id = :parentId")
    Optional<Integer> findMaxSortOrderByTenantAndParent(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId);

    @Query("SELECT MAX(c.sortOrder) FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent IS NULL")
    Optional<Integer> findMaxSortOrderByTenantForRootCategories(@Param("tenantId") Long tenantId);

    // Product association queries
    @Query("SELECT DISTINCT c FROM CategoryEntity c " +
           "JOIN c.productCategories pc " +
           "WHERE c.tenantId = :tenantId AND pc.product.id = :productId")
    List<CategoryEntity> findByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT DISTINCT c FROM CategoryEntity c " +
           "JOIN c.productCategories pc " +
           "WHERE c.tenantId = :tenantId AND pc.product.id IN :productIds")
    List<CategoryEntity> findByTenantAndProductIn(@Param("tenantId") Long tenantId, @Param("productIds") List<Long> productIds);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND " +
           "EXISTS (SELECT 1 FROM ProductCategoryEntity pc WHERE pc.category.id = c.id)")
    List<CategoryEntity> findCategoriesWithProductsByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.isActive = true AND " +
           "EXISTS (SELECT 1 FROM ProductCategoryEntity pc WHERE pc.category.id = c.id)")
    List<CategoryEntity> findActiveCategoriesWithProductsByTenant(@Param("tenantId") Long tenantId);

    // Count queries
    @Query("SELECT COUNT(c) FROM CategoryEntity c WHERE c.tenantId = :tenantId")
    long countByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(c) FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.isActive = true")
    long countActiveByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(c) FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent IS NULL")
    long countRootCategoriesByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(c) FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent.id = :parentId")
    long countChildrenByTenantAndParent(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId);

    @Query("SELECT COUNT(DISTINCT pc.category.id) FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId")
    long countCategoriesWithProductsByTenant(@Param("tenantId") Long tenantId);

    // Existence checks
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CategoryEntity c WHERE c.tenantId = :tenantId AND LOWER(c.slug) = LOWER(:slug)")
    boolean existsBySlugAndTenant(@Param("slug") String slug, @Param("tenantId") Long tenantId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CategoryEntity c WHERE c.tenantId = :tenantId AND LOWER(c.slug) = LOWER(:slug) AND c.id != :excludeId")
    boolean existsBySlugAndTenantExcludingId(@Param("slug") String slug, @Param("tenantId") Long tenantId, @Param("excludeId") Long excludeId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.parent.id = :parentId")
    boolean hasChildrenByTenantAndParent(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId);

    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id = :categoryId")
    boolean hasProductsByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId);

    // Bulk operations
    @Query("UPDATE CategoryEntity c SET c.isActive = :active WHERE c.tenantId = :tenantId AND c.id IN :ids")
    int updateActiveStatusByTenantAndIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids, @Param("active") Boolean active);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CategoryEntity c SET c.sortOrder = c.sortOrder - 1 WHERE c.tenantId = :tenantId AND c.parent.id = :parentId AND c.sortOrder > :sortOrder")
    int decrementSortOrderAfter(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId, @Param("sortOrder") Integer sortOrder);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CategoryEntity c SET c.sortOrder = c.sortOrder + 1 WHERE c.tenantId = :tenantId AND c.parent.id = :parentId AND c.sortOrder >= :sortOrder")
    int incrementSortOrderFrom(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId, @Param("sortOrder") Integer sortOrder);

    // Complex filtering
    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId " +
           "AND (:active IS NULL OR c.isActive = :active) " +
           "AND (:parentId IS NULL OR c.parent.id = :parentId) " +
           "AND (:hasProducts IS NULL OR " +
           "     (:hasProducts = true AND EXISTS (SELECT 1 FROM ProductCategoryEntity pc WHERE pc.category.id = c.id)) OR " +
           "     (:hasProducts = false AND NOT EXISTS (SELECT 1 FROM ProductCategoryEntity pc WHERE pc.category.id = c.id))) " +
           "AND (:searchTerm IS NULL OR " +
           "     LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(c.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CategoryEntity> findByTenantWithFilters(
        @Param("tenantId") Long tenantId,
        @Param("active") Boolean active,
        @Param("parentId") Long parentId,
        @Param("hasProducts") Boolean hasProducts,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // Recent categories
    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId ORDER BY c.createdAt DESC")
    Page<CategoryEntity> findRecentByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.isActive = true ORDER BY c.createdAt DESC")
    Page<CategoryEntity> findRecentActiveByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    // Additional methods for carousel service
    @Query("SELECT c FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.id = :id")
    Optional<CategoryEntity> findByTenantIdAndId(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CategoryEntity c WHERE c.tenantId = :tenantId AND c.id = :id")
    boolean existsByTenantIdAndId(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
