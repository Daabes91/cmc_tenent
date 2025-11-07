package com.clinic.modules.core.treatment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlanEntity, Long> {

    List<TreatmentPlanEntity> findByPatientId(Long patientId);

    List<TreatmentPlanEntity> findByDoctorId(Long doctorId);

    List<TreatmentPlanEntity> findByStatus(TreatmentPlanStatus status);

    @Query("SELECT t FROM TreatmentPlanEntity t WHERE t.patient.id = :patientId AND t.status = :status")
    List<TreatmentPlanEntity> findByPatientIdAndStatus(@Param("patientId") Long patientId, @Param("status") TreatmentPlanStatus status);

    @Query("SELECT t FROM TreatmentPlanEntity t WHERE t.doctor.id = :doctorId AND t.status = :status")
    List<TreatmentPlanEntity> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") TreatmentPlanStatus status);

    long countByStatus(TreatmentPlanStatus status);



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
