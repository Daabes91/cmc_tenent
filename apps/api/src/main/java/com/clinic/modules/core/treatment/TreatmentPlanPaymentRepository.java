package com.clinic.modules.core.treatment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TreatmentPlanPaymentRepository extends JpaRepository<TreatmentPlanPaymentEntity, Long> {

    /**
     * Find all payments for a specific treatment plan.
     */
    List<TreatmentPlanPaymentEntity> findByTreatmentPlanIdOrderByPaymentDateDesc(Long treatmentPlanId);

    /**
     * Find payments by date range.
     */
    List<TreatmentPlanPaymentEntity> findByPaymentDateBetween(Instant start, Instant end);

    /**
     * Find payments by tenant and date range.
     */
    @Query("""
            select p from TreatmentPlanPaymentEntity p
            where p.treatmentPlan.tenant.id = :tenantId
              and p.paymentDate between :start and :end
            """)
    List<TreatmentPlanPaymentEntity> findByTenantIdAndPaymentDateBetween(@Param("tenantId") Long tenantId,
                                                                          @Param("start") Instant start,
                                                                          @Param("end") Instant end);

    /**
     * Find payments by patient (through treatment plan).
     */
    @Query("SELECT p FROM TreatmentPlanPaymentEntity p WHERE p.treatmentPlan.patient.id = :patientId ORDER BY p.paymentDate DESC")
    List<TreatmentPlanPaymentEntity> findByPatientIdOrderByPaymentDateDesc(@Param("patientId") Long patientId);

    /**
     * Find payments by doctor (through treatment plan).
     */
    @Query("SELECT p FROM TreatmentPlanPaymentEntity p WHERE p.treatmentPlan.doctor.id = :doctorId ORDER BY p.paymentDate DESC")
    List<TreatmentPlanPaymentEntity> findByDoctorIdOrderByPaymentDateDesc(@Param("doctorId") Long doctorId);

    /**
     * Find payments recorded by specific staff member.
     */
    List<TreatmentPlanPaymentEntity> findByRecordedByStaffIdOrderByCreatedAtDesc(Long staffId);
}