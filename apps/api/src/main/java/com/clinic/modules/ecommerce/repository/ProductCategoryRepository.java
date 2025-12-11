package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.ProductCategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductCategoryEntity with tenant-scoped queries.
 * 
 * Manages the many-to-many relationship between products and categories
 * with automatic tenant filtering for data isolation.
 */
public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, Long> {

    // Basic tenant-scoped queries
    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId")
    Page<ProductCategoryEntity> findAllByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.id = :id")
    Optional<ProductCategoryEntity> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    // Product-based queries
    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId")
    List<ProductCategoryEntity> findByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId")
    Page<ProductCategoryEntity> findByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId, Pageable pageable);

    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id IN :productIds")
    List<ProductCategoryEntity> findByTenantAndProductIn(@Param("tenantId") Long tenantId, @Param("productIds") List<Long> productIds);

    // Category-based queries
    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id = :categoryId")
    List<ProductCategoryEntity> findByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId);

    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id = :categoryId")
    Page<ProductCategoryEntity> findByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id IN :categoryIds")
    List<ProductCategoryEntity> findByTenantAndCategoryIn(@Param("tenantId") Long tenantId, @Param("categoryIds") List<Long> categoryIds);

    // Specific product-category relationship queries
    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId AND pc.category.id = :categoryId")
    Optional<ProductCategoryEntity> findByTenantAndProductAndCategory(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("categoryId") Long categoryId);

    // Product queries through categories
    @Query("SELECT DISTINCT pc.product.id FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id = :categoryId")
    List<Long> findProductIdsByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId);

    @Query("SELECT DISTINCT pc.product.id FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id IN :categoryIds")
    List<Long> findProductIdsByTenantAndCategoryIn(@Param("tenantId") Long tenantId, @Param("categoryIds") List<Long> categoryIds);

    // Category queries through products
    @Query("SELECT DISTINCT pc.category.id FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId")
    List<Long> findCategoryIdsByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT DISTINCT pc.category.id FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id IN :productIds")
    List<Long> findCategoryIdsByTenantAndProductIn(@Param("tenantId") Long tenantId, @Param("productIds") List<Long> productIds);

    // Count queries
    @Query("SELECT COUNT(pc) FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId")
    long countByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(pc) FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId")
    long countByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT COUNT(pc) FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id = :categoryId")
    long countByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(DISTINCT pc.product.id) FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id = :categoryId")
    long countDistinctProductsByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(DISTINCT pc.category.id) FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId")
    long countDistinctCategoriesByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    // Existence checks
    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId AND pc.category.id = :categoryId")
    boolean existsByTenantAndProductAndCategory(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("categoryId") Long categoryId);

    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId")
    boolean existsByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id = :categoryId")
    boolean existsByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId);

    // Bulk operations
    @Query("DELETE FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId")
    int deleteByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("DELETE FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id = :categoryId")
    int deleteByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId);

    @Query("DELETE FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId AND pc.category.id = :categoryId")
    int deleteByTenantAndProductAndCategory(@Param("tenantId") Long tenantId, @Param("productId") Long productId, @Param("categoryId") Long categoryId);

    @Query("DELETE FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id IN :productIds")
    int deleteByTenantAndProductIn(@Param("tenantId") Long tenantId, @Param("productIds") List<Long> productIds);

    @Query("DELETE FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id IN :categoryIds")
    int deleteByTenantAndCategoryIn(@Param("tenantId") Long tenantId, @Param("categoryIds") List<Long> categoryIds);

    // Statistics queries
    @Query("SELECT pc.category.id as categoryId, COUNT(pc.product.id) as productCount " +
           "FROM ProductCategoryEntity pc " +
           "WHERE pc.tenantId = :tenantId " +
           "GROUP BY pc.category.id")
    List<CategoryProductCount> getProductCountByCategory(@Param("tenantId") Long tenantId);

    @Query("SELECT pc.product.id as productId, COUNT(pc.category.id) as categoryCount " +
           "FROM ProductCategoryEntity pc " +
           "WHERE pc.tenantId = :tenantId " +
           "GROUP BY pc.product.id")
    List<ProductCategoryCount> getCategoryCountByProduct(@Param("tenantId") Long tenantId);

    // Most popular categories (by product count)
    @Query("SELECT pc.category.id as categoryId, COUNT(pc.product.id) as productCount " +
           "FROM ProductCategoryEntity pc " +
           "WHERE pc.tenantId = :tenantId " +
           "GROUP BY pc.category.id " +
           "ORDER BY COUNT(pc.product.id) DESC")
    List<CategoryProductCount> getMostPopularCategories(@Param("tenantId") Long tenantId, Pageable pageable);

    // Products with most categories
    @Query("SELECT pc.product.id as productId, COUNT(pc.category.id) as categoryCount " +
           "FROM ProductCategoryEntity pc " +
           "WHERE pc.tenantId = :tenantId " +
           "GROUP BY pc.product.id " +
           "ORDER BY COUNT(pc.category.id) DESC")
    List<ProductCategoryCount> getProductsWithMostCategories(@Param("tenantId") Long tenantId, Pageable pageable);

    // Recent associations
    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId ORDER BY pc.createdAt DESC")
    Page<ProductCategoryEntity> findRecentByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.product.id = :productId ORDER BY pc.createdAt DESC")
    List<ProductCategoryEntity> findRecentByTenantAndProduct(@Param("tenantId") Long tenantId, @Param("productId") Long productId);

    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE pc.tenantId = :tenantId AND pc.category.id = :categoryId ORDER BY pc.createdAt DESC")
    List<ProductCategoryEntity> findRecentByTenantAndCategory(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId);

    // Projection interfaces for statistics
    interface CategoryProductCount {
        Long getCategoryId();
        Long getProductCount();
    }

    interface ProductCategoryCount {
        Long getProductId();
        Long getCategoryCount();
    }
}
