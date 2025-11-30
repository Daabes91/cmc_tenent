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
                        join fetch p.globalPatient gp
                        where p.id = :id
                        """)
        Optional<PatientEntity> findByIdWithGlobalPatient(@Param("id") Long id);

        @Query("""
                        select p from PatientEntity p
                        where p.tenant.id = :tenantId
                        order by p.createdAt desc
                        """)
        Page<PatientEntity> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

        @Query("""
                        select distinct p from PatientEntity p
                        join p.tags t
                        where p.tenant.id = :tenantId
                        and t.id in :tagIds
                        """)
        Page<PatientEntity> findAllByTenantIdAndTagsIdIn(@Param("tenantId") Long tenantId,
                        @Param("tagIds") List<Long> tagIds, Pageable pageable);

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
        long countByTenantIdAndCreatedAtBetween(@Param("tenantId") Long tenantId, @Param("start") Instant start,
                        @Param("end") Instant end);

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
        List<PatientEntity> searchPatientsByTenantId(@Param("tenantId") Long tenantId, @Param("term") String term,
                        Pageable pageable);

        /**
         * Find a patient by Google ID and tenant ID.
         * Ensures proper tenant scoping for Google OAuth accounts.
         *
         * @param googleId the Google ID to search for
         * @param tenantId the tenant ID to scope the search
         * @return an Optional containing the patient if found
         */
        Optional<PatientEntity> findByGoogleIdAndTenantId(String googleId, Long tenantId);

        /**
         * Find a patient by tenant ID and Google ID.
         * Used for validation to ensure Google ID uniqueness within a tenant.
         *
         * @param tenantId the tenant ID
         * @param googleId the Google ID to search for
         * @return an Optional containing the patient if found
         */
        Optional<PatientEntity> findByTenantIdAndGoogleId(Long tenantId, String googleId);

        /**
         * Find a patient by tenant ID and email.
         * Used for validation to ensure email uniqueness within a tenant.
         *
         * @param tenantId the tenant ID
         * @param email the email to search for
         * @return an Optional containing the patient if found
         */
        Optional<PatientEntity> findByTenantIdAndEmail(Long tenantId, String email);
}
