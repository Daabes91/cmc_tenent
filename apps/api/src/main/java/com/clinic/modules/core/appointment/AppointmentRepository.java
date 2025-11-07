package com.clinic.modules.core.appointment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    long countByScheduledAtBetween(Instant start, Instant end);

    long countByStatusAndScheduledAtBetween(AppointmentStatus status, Instant start, Instant end);

    @Query(value = """
            select case when count(*) > 0 then true else false end
            from appointments a
            where a.doctor_id = :doctorId
              and a.status <> 'CANCELLED'
              and a.scheduled_at < :proposedEnd
              and (a.scheduled_at + (interval '1 minute' * coalesce(a.slot_duration_minutes, :defaultDuration))) > :proposedStart
            """, nativeQuery = true)
    boolean existsActiveByDoctorAndTimeRange(@Param("doctorId") Long doctorId,
                                             @Param("proposedStart") Instant proposedStart,
                                             @Param("proposedEnd") Instant proposedEnd,
                                             @Param("defaultDuration") int defaultDuration);

    @Query(value = """
            select case when count(*) > 0 then true else false end
            from appointments a
            where a.doctor_id = :doctorId
              and a.id <> :appointmentId
              and a.status <> 'CANCELLED'
              and a.scheduled_at < :proposedEnd
              and (a.scheduled_at + (interval '1 minute' * coalesce(a.slot_duration_minutes, :defaultDuration))) > :proposedStart
            """, nativeQuery = true)
    boolean existsActiveByDoctorAndTimeRangeExcluding(@Param("doctorId") Long doctorId,
                                                      @Param("proposedStart") Instant proposedStart,
                                                      @Param("proposedEnd") Instant proposedEnd,
                                                      @Param("appointmentId") Long appointmentId,
                                                      @Param("defaultDuration") int defaultDuration);

    boolean existsByPatientId(Long patientId);

    @Query("""
            select a from AppointmentEntity a
            where a.scheduledAt >= :after
            order by a.scheduledAt asc
            """)
    List<AppointmentEntity> findUpcomingAfter(@Param("after") Instant after);

    @Query("""
            select a from AppointmentEntity a
            where a.scheduledAt between :start and :end
            order by a.scheduledAt asc
            """)
    List<AppointmentEntity> findByScheduledBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select coalesce(count(a), 0) from AppointmentEntity a
            where a.status = com.clinic.modules.core.appointment.AppointmentStatus.COMPLETED
              and a.scheduledAt between :start and :end
            """)
    long countCompletedBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select a from AppointmentEntity a
            where a.patient.id = :patientId
            order by a.scheduledAt desc
            """)
    List<AppointmentEntity> findByPatientIdOrderByScheduledAtDesc(@Param("patientId") Long patientId);

    List<AppointmentEntity> findByTreatmentPlanIdOrderByFollowUpVisitNumberAsc(Long treatmentPlanId);

    Optional<AppointmentEntity> findByTreatmentPlanIdAndFollowUpVisitNumber(Long treatmentPlanId, Integer followUpVisitNumber);

    @Query(value = """
            select * from appointments a
            where a.created_at >= :since
            order by a.created_at desc
            limit :limit
            """, nativeQuery = true)
    List<AppointmentEntity> findRecentByCreatedAtAfter(@Param("since") Instant since, @Param("limit") int limit);

    @Query("""
            select a from AppointmentEntity a
            join a.patient p
            join a.doctor d
            join a.service s
            where lower(concat(p.firstName, ' ', p.lastName)) like lower(concat('%', :term, '%'))
               or lower(d.fullNameEn) like lower(concat('%', :term, '%'))
               or lower(s.nameEn) like lower(concat('%', :term, '%'))
            order by a.scheduledAt desc
            """)
    List<AppointmentEntity> searchAppointments(@Param("term") String term, Pageable pageable);

    // Date-filtered queries for reports
    @Query("""
            select a from AppointmentEntity a
            where (a.scheduledAt is not null and a.scheduledAt between :start and :end)
               or (a.scheduledAt is null and a.createdAt between :start and :end)
            order by coalesce(a.scheduledAt, a.createdAt) asc
            """)
    List<AppointmentEntity> findByDateRange(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select count(a) from AppointmentEntity a
            where a.createdAt between :start and :end
            """)
    long countByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select count(a) from AppointmentEntity a
            where a.status = :status
              and ((a.scheduledAt between :start and :end)
                   or (a.scheduledAt is null and a.createdAt between :start and :end))
            """)
    long countByStatusAndDateRange(@Param("status") AppointmentStatus status, 
                                   @Param("start") Instant start, 
                                   @Param("end") Instant end);

    @Query("""
            select count(a) from AppointmentEntity a
            where a.paymentCollected = true
              and ((a.scheduledAt between :start and :end)
                   or (a.scheduledAt is null and a.createdAt between :start and :end))
            """)
    long countPaymentCollectedByDateRange(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select count(a) from AppointmentEntity a
            where a.paymentCollected = false
              and a.status <> com.clinic.modules.core.appointment.AppointmentStatus.CANCELLED
              and ((a.scheduledAt is not null and a.scheduledAt between :start and :end)
                   or (a.scheduledAt is null and a.createdAt between :start and :end))
            """)
    long countPaymentOutstandingByDateRange(@Param("start") Instant start, @Param("end") Instant end);

    // Simple count query for debugging date range issues
    @Query("""
            select count(a) from AppointmentEntity a
            where (a.scheduledAt is not null and a.scheduledAt between :start and :end)
               or (a.scheduledAt is null and a.createdAt between :start and :end)
            """)
    long countByDateRange(@Param("start") Instant start, @Param("end") Instant end);
}
