package com.clinic.modules.core.patient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    // Tenant-scoped query methods
    Optional<PatientEntity> findByTenantIdAndGlobalPatientId(Long tenantId, Long globalPatientId);

    Optional<PatientEntity> findByIdAndTenantId(Long id, Long tenantId);

    Optional<PatientEntity> findByTenantIdAndGlobalPatient_Email(Long tenantId, String email);

    List<PatientEntity> findAllByTenantId(Long tenantId);
    
    @Query("""
            select p from PatientEntity p
            where p.tenant.id = :tenantId
            order by p.createdAt desc
            """)
    Page<PatientEntity> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    // Legacy methods (deprecated - use tenant-scoped versions)
    @Deprecated
    Optional<PatientEntity> findByEmailIgnoreCase(String email);

    @Deprecated
    Optional<PatientEntity> findByPhone(String phone);

    @Deprecated
    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select count(p) from PatientEntity p
            where p.tenant.id = :tenantId
            and p.createdAt > :threshold
            """)
    long countByTenantIdAndCreatedAtAfter(@Param("tenantId") Long tenantId, @Param("threshold") Instant threshold);

    @Query("""
            select count(p) from PatientEntity p
            where p.tenant.id = :tenantId
            and p.createdAt between :start and :end
            """)
    long countByTenantIdAndCreatedAtBetween(@Param("tenantId") Long tenantId, @Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select count(p) from PatientEntity p
            where p.tenant.id = :tenantId
            """)
    long countByTenantId(@Param("tenantId") Long tenantId);

    @Query("""
            select p from PatientEntity p
            where p.tenant.id = :tenantId
            and (lower(concat(p.firstName, ' ', p.lastName)) like lower(concat('%', :term, '%'))
               or lower(p.email) like lower(concat('%', :term, '%'))
               or lower(coalesce(p.phone, '')) like lower(concat('%', :term, '%')))
            order by p.createdAt desc
            """)
    List<PatientEntity> searchPatientsByTenantId(@Param("tenantId") Long tenantId, @Param("term") String term, Pageable pageable);
}
