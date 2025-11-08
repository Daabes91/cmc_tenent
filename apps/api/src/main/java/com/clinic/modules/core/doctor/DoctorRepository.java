package com.clinic.modules.core.doctor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {

    // Tenant-filtered query methods using Spring Data JPA naming conventions
    List<DoctorEntity> findAllByTenantIdAndIsActiveTrue(Long tenantId);

    Optional<DoctorEntity> findByIdAndTenantId(Long id, Long tenantId);

    Optional<DoctorEntity> findByTenantIdAndEmail(Long tenantId, String email);

    // Custom query methods with tenant filtering
    @Query("select distinct d from DoctorEntity d join d.services s where s.slug = :slug and s.tenant.id = :tenantId and d.tenant.id = :tenantId")
    List<DoctorEntity> findAllByServiceSlug(@Param("slug") String slug, @Param("tenantId") Long tenantId);

    @Query("select distinct d from DoctorEntity d left join fetch d.services where d.tenant.id = :tenantId order by d.fullNameEn asc")
    List<DoctorEntity> findAllWithServices(@Param("tenantId") Long tenantId);

    @Query("""
            select d from DoctorEntity d
            where d.tenant.id = :tenantId
              and (lower(coalesce(d.fullNameEn, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(d.fullNameAr, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(d.specialtyEn, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(d.specialtyAr, '')) like lower(concat('%', :term, '%')))
            order by d.createdAt desc
            """)
    List<DoctorEntity> searchDoctors(@Param("tenantId") Long tenantId, @Param("term") String term, Pageable pageable);

    @Query("""
            select count(d) from DoctorEntity d
            where d.tenant.id = :tenantId
            """)
    long countByTenantId(@Param("tenantId") Long tenantId);
}
