package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.ProductImageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductImageEntity with tenant-scoped queries.
 * 
 * All queries automatically filter by tenant to ensure data isolation.
 */
public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

    // Basic tenant-scoped queries
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId")
    Page<ProductImageEntity> findAllByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.id = :id")
    Optional<ProductImageEntity> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    // Product-based queries
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId ORDER BY pi.sortOrder ASC, pi.id ASC")
    List<ProductImageEntity> findByTenantAndProductOrderBySortOrder(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId")
    Page<ProductImageEntity> findByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId, Pageable pageable);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id IN :productIds ORDER BY pi.product.id, pi.sortOrder ASC")
    List<ProductImageEntity> findByTenantAndProductIn(@Param("tenantId") Long tenantId, @Param("productIds") List<Long> productIds);

    // Main image queries
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId AND pi.isMain = true")
    Optional<ProductImageEntity> findMainImageByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.isMain = true")
    List<ProductImageEntity> findAllMainImagesByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id IN :productIds AND pi.isMain = true")
    List<ProductImageEntity> findMainImagesByTenantAndProductIn(@Param("tenantId") Long tenantId, @Param("productIds") List<Long> productIds);

    // Sort order queries
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId AND pi.sortOrder = :sortOrder")
    Optional<ProductImageEntity> findByTenantAndProductAndSortOrder(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("sortOrder") Integer sortOrder);

    @Query("SELECT MAX(pi.sortOrder) FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId")
    Optional<Integer> findMaxSortOrderByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId AND pi.sortOrder > :sortOrder ORDER BY pi.sortOrder ASC")
    List<ProductImageEntity> findByTenantAndProductWithSortOrderGreaterThan(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("sortOrder") Integer sortOrder);

    // First/Primary image queries (fallback when no main image is set)
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId ORDER BY " +
           "CASE WHEN pi.isMain = true THEN 0 ELSE 1 END, pi.sortOrder ASC, pi.id ASC")
    List<ProductImageEntity> findByTenantAndProductOrderByMainAndSortOrder(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId ORDER BY " +
           "CASE WHEN pi.isMain = true THEN 0 ELSE 1 END, pi.sortOrder ASC, pi.id ASC")
    Optional<ProductImageEntity> findFirstImageByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    // URL-based queries
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.imageUrl = :imageUrl")
    Optional<ProductImageEntity> findByTenantAndImageUrl(@Param("tenantId") Long tenantId, @Param("imageUrl") String imageUrl);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.imageUrl LIKE CONCAT('%', :urlPattern, '%')")
    List<ProductImageEntity> findByTenantAndImageUrlContaining(@Param("tenantId") Long tenantId, @Param("urlPattern") String urlPattern);

    // Count queries
    @Query("SELECT COUNT(pi) FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId")
    long countByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(pi) FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId")
    long countByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT COUNT(pi) FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.isMain = true")
    long countMainImagesByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(pi) FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId AND pi.isMain = true")
    long countMainImagesByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    // Existence checks
    @Query("SELECT CASE WHEN COUNT(pi) > 0 THEN true ELSE false END FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId AND pi.isMain = true")
    boolean hasMainImageByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT CASE WHEN COUNT(pi) > 0 THEN true ELSE false END FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.imageUrl = :imageUrl")
    boolean existsByTenantAndImageUrl(@Param("tenantId") Long tenantId, @Param("imageUrl") String imageUrl);

    @Query("SELECT CASE WHEN COUNT(pi) > 0 THEN true ELSE false END FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId AND pi.sortOrder = :sortOrder")
    boolean existsByTenantAndProductAndSortOrder(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("sortOrder") Integer sortOrder);

    // Bulk operations
    @Query("UPDATE ProductImageEntity pi SET pi.isMain = false WHERE pi.tenantId = :tenantId AND pi.product.id = :productId")
    int clearMainImageByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("UPDATE ProductImageEntity pi SET pi.sortOrder = pi.sortOrder - 1 WHERE pi.tenantId = :tenantId AND pi.product.id = :productId AND pi.sortOrder > :sortOrder")
    int decrementSortOrderAfter(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("sortOrder") Integer sortOrder);

    @Query("UPDATE ProductImageEntity pi SET pi.sortOrder = pi.sortOrder + 1 WHERE pi.tenantId = :tenantId AND pi.product.id = :productId AND pi.sortOrder >= :sortOrder")
    int incrementSortOrderFrom(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("sortOrder") Integer sortOrder);

    // Batch queries for multiple products
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id IN :productIds AND pi.isMain = true")
    List<ProductImageEntity> findMainImagesByTenantAndProducts(@Param("tenantId") Long tenantId, @Param("productIds") List<Long> productIds);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id IN :productIds ORDER BY pi.product.id, " +
           "CASE WHEN pi.isMain = true THEN 0 ELSE 1 END, pi.sortOrder ASC")
    List<ProductImageEntity> findByTenantAndProductsOrderByMainAndSortOrder(@Param("tenantId") Long tenantId, @Param("productIds") List<Long> productIds);

    // Alt text queries
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.altText IS NULL OR pi.altText = ''")
    List<ProductImageEntity> findByTenantWithMissingAltText(@Param("tenantId") Long tenantId);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId AND (pi.altText IS NULL OR pi.altText = '')")
    List<ProductImageEntity> findByTenantAndProductWithMissingAltText(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    // Recent images
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId ORDER BY pi.createdAt DESC")
    Page<ProductImageEntity> findRecentByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.tenantId = :tenantId AND pi.product.id = :productId ORDER BY pi.createdAt DESC")
    List<ProductImageEntity> findRecentByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);
}