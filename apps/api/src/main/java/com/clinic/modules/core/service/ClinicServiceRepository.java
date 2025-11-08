package com.clinic.modules.core.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClinicServiceRepository extends JpaRepository<ClinicServiceEntity, Long> {

    Optional<ClinicServiceEntity> findBySlugAndTenantId(String slug, Long tenantId);

    Optional<ClinicServiceEntity> findByIdAndTenantId(Long id, Long tenantId);

    List<ClinicServiceEntity> findByTenantIdOrderByNameEnAsc(Long tenantId);

    Optional<ClinicServiceEntity> findFirstByTenantIdOrderByCreatedAtAsc(Long tenantId);

    @Query("""
            select s from ClinicServiceEntity s
            where s.tenant.id = :tenantId
              and (
                    lower(s.nameEn) like lower(concat('%', :term, '%'))
                 or lower(coalesce(s.nameAr, '')) like lower(concat('%', :term, '%'))
                 or lower(coalesce(s.summaryEn, '')) like lower(concat('%', :term, '%'))
                 or lower(coalesce(s.summaryAr, '')) like lower(concat('%', :term, '%'))
              )
            order by s.createdAt desc
            """)
    List<ClinicServiceEntity> searchServices(@Param("tenantId") Long tenantId, @Param("term") String term, Pageable pageable);

    @Query("""
            select s from ClinicServiceEntity s
            where s.tenant.id = :tenantId
              and s.id in :ids
            """)
    List<ClinicServiceEntity> findAllByTenantIdAndIdIn(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);
}
