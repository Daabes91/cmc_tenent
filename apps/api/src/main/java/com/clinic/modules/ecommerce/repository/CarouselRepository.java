package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.CarouselEntity;
import com.clinic.modules.ecommerce.model.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CarouselEntity operations.
 * 
 * Provides data access methods for carousel management with tenant isolation.
 */
@Repository
public interface CarouselRepository extends JpaRepository<CarouselEntity, Long> {

    /**
     * Find all carousels for a specific tenant.
     */
    @Query("SELECT c FROM CarouselEntity c WHERE c.tenant.id = :tenantId")
    Page<CarouselEntity> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Find active carousels for a specific tenant.
     */
    @Query("SELECT c FROM CarouselEntity c WHERE c.tenant.id = :tenantId AND c.isActive = true")
    List<CarouselEntity> findActiveByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find carousels by placement and platform for a specific tenant.
     */
    @Query("SELECT c FROM CarouselEntity c WHERE c.tenant.id = :tenantId " +
           "AND c.isActive = true " +
           "AND c.placement = :placement " +
           "AND (c.platform = :platform OR c.platform = 'BOTH')")
    List<CarouselEntity> findByTenantIdAndPlacementAndPlatform(
            @Param("tenantId") Long tenantId,
            @Param("placement") String placement,
            @Param("platform") Platform platform);

    /**
     * Find carousel by slug for a specific tenant.
     */
    @Query("SELECT c FROM CarouselEntity c WHERE c.tenant.id = :tenantId AND c.slug = :slug")
    Optional<CarouselEntity> findByTenantIdAndSlug(@Param("tenantId") Long tenantId, @Param("slug") String slug);

    /**
     * Find carousel by ID for a specific tenant.
     */
    @Query("SELECT c FROM CarouselEntity c WHERE c.tenant.id = :tenantId AND c.id = :id")
    Optional<CarouselEntity> findByTenantIdAndId(@Param("tenantId") Long tenantId, @Param("id") Long id);

    /**
     * Check if a carousel slug exists for a specific tenant.
     */
    @Query("SELECT COUNT(c) > 0 FROM CarouselEntity c WHERE c.tenant.id = :tenantId AND c.slug = :slug")
    boolean existsByTenantIdAndSlug(@Param("tenantId") Long tenantId, @Param("slug") String slug);

    /**
     * Check if a carousel slug exists for a specific tenant, excluding a specific ID.
     */
    @Query("SELECT COUNT(c) > 0 FROM CarouselEntity c WHERE c.tenant.id = :tenantId AND c.slug = :slug AND c.id != :excludeId")
    boolean existsByTenantIdAndSlugAndIdNot(@Param("tenantId") Long tenantId, @Param("slug") String slug, @Param("excludeId") Long excludeId);

    /**
     * Find carousels by placement for a specific tenant.
     */
    @Query("SELECT c FROM CarouselEntity c WHERE c.tenant.id = :tenantId " +
           "AND c.isActive = true " +
           "AND c.placement = :placement")
    List<CarouselEntity> findByTenantIdAndPlacement(@Param("tenantId") Long tenantId, @Param("placement") String placement);

    /**
     * Count active carousels for a specific tenant.
     */
    @Query("SELECT COUNT(c) FROM CarouselEntity c WHERE c.tenant.id = :tenantId AND c.isActive = true")
    long countActiveByTenantId(@Param("tenantId") Long tenantId);
}