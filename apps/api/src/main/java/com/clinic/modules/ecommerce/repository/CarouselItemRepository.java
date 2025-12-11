package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.CarouselItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CarouselItemEntity operations.
 * 
 * Provides data access methods for carousel item management with tenant isolation.
 */
@Repository
public interface CarouselItemRepository extends JpaRepository<CarouselItemEntity, Long> {

    /**
     * Find all items for a specific carousel and tenant.
     */
    @Query("SELECT ci FROM CarouselItemEntity ci WHERE ci.carousel.id = :carouselId AND ci.tenant.id = :tenantId ORDER BY ci.sortOrder ASC")
    List<CarouselItemEntity> findByCarouselIdAndTenantId(@Param("carouselId") Long carouselId, @Param("tenantId") Long tenantId);

    /**
     * Find active items for a specific carousel and tenant.
     */
    @Query("SELECT ci FROM CarouselItemEntity ci WHERE ci.carousel.id = :carouselId AND ci.tenant.id = :tenantId AND ci.isActive = true ORDER BY ci.sortOrder ASC")
    List<CarouselItemEntity> findActiveByCarouselIdAndTenantId(@Param("carouselId") Long carouselId, @Param("tenantId") Long tenantId);

    /**
     * Find carousel item by ID for a specific tenant.
     */
    @Query("SELECT ci FROM CarouselItemEntity ci WHERE ci.id = :id AND ci.tenant.id = :tenantId")
    Optional<CarouselItemEntity> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Count active items for a specific carousel and tenant.
     */
    @Query("SELECT COUNT(ci) FROM CarouselItemEntity ci WHERE ci.carousel.id = :carouselId AND ci.tenant.id = :tenantId AND ci.isActive = true")
    long countActiveByCarouselIdAndTenantId(@Param("carouselId") Long carouselId, @Param("tenantId") Long tenantId);

    /**
     * Find items by product ID for a specific tenant.
     */
    @Query("SELECT ci FROM CarouselItemEntity ci WHERE ci.product.id = :productId AND ci.tenant.id = :tenantId")
    List<CarouselItemEntity> findByProductIdAndTenantId(@Param("productId") Long productId, @Param("tenantId") Long tenantId);

    /**
     * Find items by category ID for a specific tenant.
     */
    @Query("SELECT ci FROM CarouselItemEntity ci WHERE ci.category.id = :categoryId AND ci.tenant.id = :tenantId")
    List<CarouselItemEntity> findByCategoryIdAndTenantId(@Param("categoryId") Long categoryId, @Param("tenantId") Long tenantId);

    /**
     * Get the maximum sort order for a carousel.
     */
    @Query("SELECT COALESCE(MAX(ci.sortOrder), 0) FROM CarouselItemEntity ci WHERE ci.carousel.id = :carouselId AND ci.tenant.id = :tenantId")
    Integer getMaxSortOrderByCarouselIdAndTenantId(@Param("carouselId") Long carouselId, @Param("tenantId") Long tenantId);
}