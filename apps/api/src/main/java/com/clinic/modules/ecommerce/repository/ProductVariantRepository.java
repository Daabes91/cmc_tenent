package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.ProductVariantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductVariantEntity with tenant-scoped queries.
 * 
 * All queries automatically filter by tenant to ensure data isolation.
 */
public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {

    // Basic tenant-scoped queries
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId")
    Page<ProductVariantEntity> findAllByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.id = :id")
    Optional<ProductVariantEntity> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.sku = :sku")
    Optional<ProductVariantEntity> findBySkuAndTenant(@Param("sku") String sku, @Param("tenantId") Long tenantId);

    // Product-based queries
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId")
    List<ProductVariantEntity> findByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId")
    Page<ProductVariantEntity> findByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId, Pageable pageable);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id IN :productIds")
    List<ProductVariantEntity> findByTenantAndProductIn(@Param("tenantId") Long tenantId, @Param("productIds") List<Long> productIds);

    // Stock-based queries
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.isInStock = true")
    Page<ProductVariantEntity> findInStockByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.isInStock = false")
    Page<ProductVariantEntity> findOutOfStockByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.stockQuantity <= :threshold")
    List<ProductVariantEntity> findLowStockByTenant(@Param("tenantId") Long tenantId, @Param("threshold") Integer threshold);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId AND pv.isInStock = true")
    List<ProductVariantEntity> findInStockByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId AND pv.stockQuantity >= :quantity")
    List<ProductVariantEntity> findAvailableByTenantAndProductWithQuantity(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("quantity") Integer quantity);

    // Price-based queries
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.price BETWEEN :minPrice AND :maxPrice")
    Page<ProductVariantEntity> findByTenantAndPriceBetween(@Param("tenantId") Long tenantId, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId ORDER BY pv.price ASC")
    List<ProductVariantEntity> findByTenantAndProductOrderByPriceAsc(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId ORDER BY pv.price DESC")
    List<ProductVariantEntity> findByTenantAndProductOrderByPriceDesc(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    // Search queries
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND " +
           "(LOWER(pv.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pv.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ProductVariantEntity> searchByTenant(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Complex filtering
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId " +
           "AND (:productId IS NULL OR pv.product.id = :productId) " +
           "AND (:inStock IS NULL OR pv.isInStock = :inStock) " +
           "AND (:minPrice IS NULL OR pv.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR pv.price <= :maxPrice) " +
           "AND (:minStock IS NULL OR pv.stockQuantity >= :minStock) " +
           "AND (:searchTerm IS NULL OR " +
           "     LOWER(pv.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(pv.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ProductVariantEntity> findByTenantWithFilters(
        @Param("tenantId") Long tenantId,
        @Param("productId") Long productId,
        @Param("inStock") Boolean inStock,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minStock") Integer minStock,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // Count queries
    @Query("SELECT COUNT(pv) FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId")
    long countByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(pv) FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId")
    long countByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT COUNT(pv) FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.isInStock = true")
    long countInStockByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(pv) FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.stockQuantity <= :threshold")
    long countLowStockByTenant(@Param("tenantId") Long tenantId, @Param("threshold") Integer threshold);

    // Existence checks
    @Query("SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.sku = :sku")
    boolean existsBySkuAndTenant(@Param("sku") String sku, @Param("tenantId") Long tenantId);

    @Query("SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.sku = :sku AND pv.id != :excludeId")
    boolean existsBySkuAndTenantExcludingId(@Param("sku") String sku, @Param("tenantId") Long tenantId, @Param("excludeId") Long excludeId);

    // Aggregation queries
    @Query("SELECT MIN(pv.price) FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId AND pv.isInStock = true")
    Optional<BigDecimal> findMinPriceByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT MAX(pv.price) FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId AND pv.isInStock = true")
    Optional<BigDecimal> findMaxPriceByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT SUM(pv.stockQuantity) FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId")
    Optional<Long> sumStockQuantityByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    // Bulk operations
    @Query("UPDATE ProductVariantEntity pv SET pv.isInStock = :inStock, pv.updatedAt = CURRENT_TIMESTAMP WHERE pv.tenantId = :tenantId AND pv.id IN :ids")
    int updateStockStatusByTenantAndIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids, @Param("inStock") Boolean inStock);

    @Query("UPDATE ProductVariantEntity pv SET pv.price = :price, pv.updatedAt = CURRENT_TIMESTAMP WHERE pv.tenantId = :tenantId AND pv.id IN :ids")
    int updatePriceByTenantAndIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids, @Param("price") BigDecimal price);

    // Stock management queries
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId AND pv.stockQuantity >= :requiredQuantity ORDER BY pv.price ASC")
    List<ProductVariantEntity> findAvailableVariantsByTenantAndProductOrderByPrice(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("requiredQuantity") Integer requiredQuantity);

    // Default variant queries (assuming first variant or lowest price is default)
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.product.id = :productId ORDER BY pv.price ASC, pv.id ASC")
    List<ProductVariantEntity> findByTenantAndProductOrderByPriceAndId(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    // Currency-based queries
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.tenantId = :tenantId AND pv.currency = :currency")
    Page<ProductVariantEntity> findByTenantAndCurrency(@Param("tenantId") Long tenantId, @Param("currency") String currency, Pageable pageable);
}