package com.clinic.modules.core.insurance;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompanyEntity, Long> {

    /**
     * Find all insurance companies for a specific tenant
     */
    List<InsuranceCompanyEntity> findAllByTenantId(Long tenantId);

    /**
     * Find insurance company by ID and tenant ID
     */
    Optional<InsuranceCompanyEntity> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * Find insurance company by name (English or Arabic) and tenant ID
     */
    @Query("SELECT i FROM InsuranceCompanyEntity i WHERE i.tenant.id = :tenantId AND (LOWER(i.nameEn) = LOWER(:name) OR LOWER(i.nameAr) = LOWER(:name))")
    Optional<InsuranceCompanyEntity> findByTenantIdAndName(@Param("tenantId") Long tenantId, @Param("name") String name);

    /**
     * Find all active insurance companies ordered by display order
     */
    List<InsuranceCompanyEntity> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find all insurance companies ordered by display order
     */
    List<InsuranceCompanyEntity> findAllByOrderByDisplayOrderAsc();

    /**
     * Check if a company with the given name already exists (case-insensitive) within tenant
     */
    @Query("SELECT COUNT(i) > 0 FROM InsuranceCompanyEntity i WHERE i.tenant.id = ?1 AND (LOWER(i.nameEn) = LOWER(?2) OR LOWER(i.nameAr) = LOWER(?2)) AND i.id != ?3")
    boolean existsByTenantIdAndNameIgnoreCaseAndIdNot(Long tenantId, String name, Long id);

    /**
     * Check if a company with the given name already exists (case-insensitive) within tenant
     */
    @Query("SELECT COUNT(i) > 0 FROM InsuranceCompanyEntity i WHERE i.tenant.id = ?1 AND (LOWER(i.nameEn) = LOWER(?2) OR LOWER(i.nameAr) = LOWER(?2))")
    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);

    /**
     * Check if a company with the given name already exists (case-insensitive)
     */
    @Query("SELECT COUNT(i) > 0 FROM InsuranceCompanyEntity i WHERE (LOWER(i.nameEn) = LOWER(?1) OR LOWER(i.nameAr) = LOWER(?1)) AND i.id != ?2")
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Check if a company with the given name already exists (case-insensitive)
     */
    @Query("SELECT COUNT(i) > 0 FROM InsuranceCompanyEntity i WHERE LOWER(i.nameEn) = LOWER(?1) OR LOWER(i.nameAr) = LOWER(?1)")
    boolean existsByNameIgnoreCase(String name);

    @Query("""
            select i from InsuranceCompanyEntity i
            where i.tenant.id = :tenantId
              and (lower(i.nameEn) like lower(concat('%', :term, '%'))
               or lower(coalesce(i.nameAr, '')) like lower(concat('%', :term, '%')))
            order by coalesce(i.displayOrder, 0) asc, lower(i.nameEn) asc
            """)
    List<InsuranceCompanyEntity> searchInsuranceCompaniesByTenantId(@Param("tenantId") Long tenantId, @Param("term") String term, Pageable pageable);

    @Query("""
            select i from InsuranceCompanyEntity i
            where lower(i.nameEn) like lower(concat('%', :term, '%'))
               or lower(coalesce(i.nameAr, '')) like lower(concat('%', :term, '%'))
            order by coalesce(i.displayOrder, 0) asc, lower(i.nameEn) asc
            """)
    List<InsuranceCompanyEntity> searchInsuranceCompanies(@Param("term") String term, Pageable pageable);
}
