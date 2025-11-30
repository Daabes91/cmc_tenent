package com.clinic.modules.core.tenant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {

    Optional<TenantEntity> findBySlugIgnoreCase(String slug);

    Optional<TenantEntity> findByCustomDomainIgnoreCase(String domain);

    // Find tenant by ID excluding soft-deleted
    @Query("SELECT t FROM TenantEntity t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<TenantEntity> findByIdAndNotDeleted(@Param("id") Long id);

    // Find tenant by ID including soft-deleted if requested
    @Query("SELECT t FROM TenantEntity t WHERE t.id = :id AND (:includeDeleted = true OR t.deletedAt IS NULL)")
    Optional<TenantEntity> findByIdWithDeletedFilter(@Param("id") Long id, @Param("includeDeleted") boolean includeDeleted);

    // Find all tenants excluding soft-deleted
    @Query("SELECT t FROM TenantEntity t WHERE t.deletedAt IS NULL")
    Page<TenantEntity> findAllNotDeleted(Pageable pageable);

    // Find all tenants with optional soft-deleted filter
    @Query("SELECT t FROM TenantEntity t WHERE :includeDeleted = true OR t.deletedAt IS NULL")
    Page<TenantEntity> findAllWithDeletedFilter(@Param("includeDeleted") boolean includeDeleted, Pageable pageable);

    // Find by slug excluding soft-deleted
    @Query("SELECT t FROM TenantEntity t WHERE LOWER(t.slug) = LOWER(:slug) AND t.deletedAt IS NULL")
    Optional<TenantEntity> findBySlugIgnoreCaseAndNotDeleted(@Param("slug") String slug);

    // Find all tenants with optional status and deleted filter
    @Query("SELECT t FROM TenantEntity t WHERE " +
           "(:includeDeleted = true OR t.deletedAt IS NULL) AND " +
           "(:status IS NULL OR t.status = :status)")
    Page<TenantEntity> findAllWithFilters(
        @Param("includeDeleted") boolean includeDeleted,
        @Param("status") TenantStatus status,
        Pageable pageable
    );

    // Find all tenants with optional status, billing status, and deleted filter
    @Query("SELECT t FROM TenantEntity t WHERE " +
           "(:includeDeleted = true OR t.deletedAt IS NULL) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:billingStatus IS NULL OR t.billingStatus = :billingStatus)")
    Page<TenantEntity> findAllWithFiltersAndBillingStatus(
        @Param("includeDeleted") boolean includeDeleted,
        @Param("status") TenantStatus status,
        @Param("billingStatus") BillingStatus billingStatus,
        Pageable pageable
    );

    // Find all tenants with optional status, billing status, plan tier, and deleted filter
    @Query("SELECT t FROM TenantEntity t LEFT JOIN com.clinic.modules.saas.model.SubscriptionEntity s ON s.tenant.id = t.id WHERE " +
           "(:includeDeleted = true OR t.deletedAt IS NULL) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:billingStatus IS NULL OR t.billingStatus = :billingStatus) AND " +
           "(:planTier IS NULL OR s.planTier = :planTier)")
    Page<TenantEntity> findAllWithFiltersAndPlanTier(
        @Param("includeDeleted") boolean includeDeleted,
        @Param("status") TenantStatus status,
        @Param("billingStatus") BillingStatus billingStatus,
        @Param("planTier") com.clinic.modules.saas.model.PlanTier planTier,
        Pageable pageable
    );

    // Count tenants by billing status (for monitoring)
    @Query("SELECT t.billingStatus as status, COUNT(t) as count FROM TenantEntity t WHERE t.deletedAt IS NULL GROUP BY t.billingStatus")
    List<BillingStatusCount> countByBillingStatusGroupedRaw();

    default Map<BillingStatus, Long> countByBillingStatusGrouped() {
        return countByBillingStatusGroupedRaw().stream()
                .collect(Collectors.toMap(
                        BillingStatusCount::getStatus,
                        BillingStatusCount::getCount
                ));
    }

    interface BillingStatusCount {
        BillingStatus getStatus();
        Long getCount();
    }
}
