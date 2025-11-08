package com.clinic.modules.core.treatment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlanEntity, Long> {

    /**
     * Find all treatment plans for a specific tenant
     */
    List<TreatmentPlanEntity> findAllByTenantId(Long tenantId);

    /**
     * Find treatment plan by ID and tenant ID
     */
    Optional<TreatmentPlanEntity> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * Find treatment plans by patient ID and tenant ID
     */
    List<TreatmentPlanEntity> findByTenantIdAndPatientId(Long tenantId, Long patientId);

    List<TreatmentPlanEntity> findByPatientId(Long patientId);

    /**
     * Find treatment plans by doctor ID and tenant ID
     */
    List<TreatmentPlanEntity> findByTenantIdAndDoctorId(Long tenantId, Long doctorId);

    List<TreatmentPlanEntity> findByDoctorId(Long doctorId);

    /**
     * Find treatment plans by tenant ID and status
     */
    List<TreatmentPlanEntity> findByTenantIdAndStatus(Long tenantId, TreatmentPlanStatus status);

    List<TreatmentPlanEntity> findByStatus(TreatmentPlanStatus status);

    @Query("SELECT t FROM TreatmentPlanEntity t WHERE t.tenant.id = :tenantId AND t.patient.id = :patientId AND t.status = :status")
    List<TreatmentPlanEntity> findByTenantIdAndPatientIdAndStatus(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId, @Param("status") TreatmentPlanStatus status);

    @Query("SELECT t FROM TreatmentPlanEntity t WHERE t.patient.id = :patientId AND t.status = :status")
    List<TreatmentPlanEntity> findByPatientIdAndStatus(@Param("patientId") Long patientId, @Param("status") TreatmentPlanStatus status);

    @Query("SELECT t FROM TreatmentPlanEntity t WHERE t.tenant.id = :tenantId AND t.doctor.id = :doctorId AND t.status = :status")
    List<TreatmentPlanEntity> findByTenantIdAndDoctorIdAndStatus(@Param("tenantId") Long tenantId, @Param("doctorId") Long doctorId, @Param("status") TreatmentPlanStatus status);

    @Query("SELECT t FROM TreatmentPlanEntity t WHERE t.doctor.id = :doctorId AND t.status = :status")
    List<TreatmentPlanEntity> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") TreatmentPlanStatus status);

    long countByTenantIdAndStatus(Long tenantId, TreatmentPlanStatus status);

    long countByStatus(TreatmentPlanStatus status);

    @Query("""
            select t from TreatmentPlanEntity t
            join t.patient p
            join t.doctor d
            join t.treatmentType s
            where t.tenant.id = :tenantId
              and (lower(concat(p.firstName, ' ', p.lastName)) like lower(concat('%', :term, '%'))
               or lower(d.fullNameEn) like lower(concat('%', :term, '%'))
               or lower(s.nameEn) like lower(concat('%', :term, '%')))
            order by t.createdAt desc
            """)
    List<TreatmentPlanEntity> searchTreatmentPlansByTenantId(@Param("tenantId") Long tenantId, @Param("term") String term, Pageable pageable);

    @Query("""
            select t from TreatmentPlanEntity t
            join t.patient p
            join t.doctor d
            join t.treatmentType s
            where lower(concat(p.firstName, ' ', p.lastName)) like lower(concat('%', :term, '%'))
               or lower(d.fullNameEn) like lower(concat('%', :term, '%'))
               or lower(s.nameEn) like lower(concat('%', :term, '%'))
            order by t.createdAt desc
            """)
    List<TreatmentPlanEntity> searchTreatmentPlans(@Param("term") String term, Pageable pageable);
}
