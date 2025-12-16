package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductEntity with tenant-scoped queries.
 * 
 * All queries automatically filter by tenant to ensure data isolation.
 */
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    // Basic tenant-scoped queries
    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId")
    Page<ProductEntity> findAllByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.id = :id")
    Optional<ProductEntity> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND LOWER(p.slug) = LOWER(:slug)")
    Optional<ProductEntity> findBySlugAndTenant(@Param("slug") String slug, @Param("tenantId") Long tenantId);

    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.sku = :sku")
    Optional<ProductEntity> findBySkuAndTenant(@Param("sku") String sku, @Param("tenantId") Long tenantId);

    // Status-based queries
    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.status = :status")
    Page<ProductEntity> findByTenantAndStatus(@Param("tenantId") Long tenantId, @Param("status") ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.status IN :statuses")
    Page<ProductEntity> findByTenantAndStatusIn(@Param("tenantId") Long tenantId, @Param("statuses") List<ProductStatus> statuses, Pageable pageable);

    // Visibility-based queries
    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.isVisible = true AND p.status = 'ACTIVE'")
    Page<ProductEntity> findVisibleByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.isVisible = :visible")
    Page<ProductEntity> findByTenantAndVisible(@Param("tenantId") Long tenantId, @Param("visible") Boolean visible, Pageable pageable);

    // Search queries
    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(p.nameAr, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(p.descriptionAr, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ProductEntity> searchByTenant(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE' AND p.isVisible = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(p.nameAr, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(p.descriptionAr, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ProductEntity> searchVisibleByTenant(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Category-based queries
    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "JOIN p.productCategories pc " +
           "WHERE p.tenantId = :tenantId AND pc.category.id = :categoryId")
    Page<ProductEntity> findByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "JOIN p.productCategories pc " +
           "WHERE p.tenantId = :tenantId AND pc.category.id = :categoryId AND p.status = 'ACTIVE' AND p.isVisible = true")
    Page<ProductEntity> findVisibleByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "JOIN p.productCategories pc " +
           "WHERE p.tenantId = :tenantId AND pc.category.id IN :categoryIds")
    Page<ProductEntity> findByTenantAndCategoryIn(@Param("tenantId") Long tenantId, @Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    // Variant-based queries
    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.hasVariants = :hasVariants")
    Page<ProductEntity> findByTenantAndHasVariants(@Param("tenantId") Long tenantId, @Param("hasVariants") Boolean hasVariants, Pageable pageable);

    // Price range queries
    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<ProductEntity> findByTenantAndPriceBetween(@Param("tenantId") Long tenantId, @Param("minPrice") java.math.BigDecimal minPrice, @Param("maxPrice") java.math.BigDecimal maxPrice, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE' AND p.isVisible = true AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<ProductEntity> findVisibleByTenantAndPriceBetween(@Param("tenantId") Long tenantId, @Param("minPrice") java.math.BigDecimal minPrice, @Param("maxPrice") java.math.BigDecimal maxPrice, Pageable pageable);

    // Complex filtering query
    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "LEFT JOIN p.productCategories pc " +
           "WHERE p.tenantId = :tenantId " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:visible IS NULL OR p.isVisible = :visible) " +
           "AND (:categoryId IS NULL OR pc.category.id = :categoryId) " +
           "AND (:searchTerm IS NULL OR " +
           "     LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(COALESCE(p.nameAr, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(COALESCE(p.descriptionAr, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<ProductEntity> findByTenantWithFilters(
        @Param("tenantId") Long tenantId,
        @Param("status") ProductStatus status,
        @Param("visible") Boolean visible,
        @Param("categoryId") Long categoryId,
        @Param("searchTerm") String searchTerm,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice,
        Pageable pageable
    );

    // Count queries
    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.tenantId = :tenantId")
    long countByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.status = :status")
    long countByTenantAndStatus(@Param("tenantId") Long tenantId, @Param("status") ProductStatus status);

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.isVisible = true AND p.status = 'ACTIVE'")
    long countVisibleByTenant(@Param("tenantId") Long tenantId);

    // Existence checks
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.tenantId = :tenantId AND LOWER(p.slug) = LOWER(:slug)")
    boolean existsBySlugAndTenant(@Param("slug") String slug, @Param("tenantId") Long tenantId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.sku = :sku")
    boolean existsBySkuAndTenant(@Param("sku") String sku, @Param("tenantId") Long tenantId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.tenantId = :tenantId AND LOWER(p.slug) = LOWER(:slug) AND p.id != :excludeId")
    boolean existsBySlugAndTenantExcludingId(@Param("slug") String slug, @Param("tenantId") Long tenantId, @Param("excludeId") Long excludeId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.sku = :sku AND p.id != :excludeId")
    boolean existsBySkuAndTenantExcludingId(@Param("sku") String sku, @Param("tenantId") Long tenantId, @Param("excludeId") Long excludeId);

    // Bulk operations
    @Query("UPDATE ProductEntity p SET p.status = :status, p.updatedAt = CURRENT_TIMESTAMP WHERE p.tenantId = :tenantId AND p.id IN :ids")
    int updateStatusByTenantAndIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids, @Param("status") ProductStatus status);

    @Query("UPDATE ProductEntity p SET p.isVisible = :visible, p.updatedAt = CURRENT_TIMESTAMP WHERE p.tenantId = :tenantId AND p.id IN :ids")
    int updateVisibilityByTenantAndIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids, @Param("visible") Boolean visible);

    // Recent products
    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId ORDER BY p.createdAt DESC")
    Page<ProductEntity> findRecentByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE' AND p.isVisible = true ORDER BY p.createdAt DESC")
    Page<ProductEntity> findRecentVisibleByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    // Additional methods for carousel service
    @Query("SELECT p FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.id = :id")
    Optional<ProductEntity> findByTenantIdAndId(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.tenantId = :tenantId AND p.id = :id")
    boolean existsByTenantIdAndId(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // Note: Individual collection loading methods can be added here if needed in the future
    // Currently using @Transactional approach in controllers to avoid MultipleBagFetchException
}
