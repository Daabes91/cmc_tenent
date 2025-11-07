package com.clinic.modules.core.insurance;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompanyEntity, Long> {

    /**
     * Find all active insurance companies ordered by display order
     */
    List<InsuranceCompanyEntity> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find all insurance companies ordered by display order
     */
    List<InsuranceCompanyEntity> findAllByOrderByDisplayOrderAsc();

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
            where lower(i.nameEn) like lower(concat('%', :term, '%'))
               or lower(coalesce(i.nameAr, '')) like lower(concat('%', :term, '%'))
            order by coalesce(i.displayOrder, 0) asc, lower(i.nameEn) asc
            """)
    List<InsuranceCompanyEntity> searchInsuranceCompanies(@Param("term") String term, Pageable pageable);
}
