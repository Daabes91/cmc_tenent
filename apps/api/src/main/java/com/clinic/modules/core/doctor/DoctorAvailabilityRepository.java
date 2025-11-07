package com.clinic.modules.core.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailabilityEntity, Long> {

    List<DoctorAvailabilityEntity> findByDoctorId(Long doctorId);

    List<DoctorAvailabilityEntity> findByDoctorIdAndRecurringWeeklyTrueAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);

    List<DoctorAvailabilityEntity> findByDoctorIdAndSpecificDate(Long doctorId, LocalDate specificDate);

    @Query("""
            select a from DoctorAvailabilityEntity a
            where a.doctor.id = :doctorId
              and a.recurringWeekly = false
              and a.specificDate = :specificDate
              and ((a.startTime <= :endTime) and (a.endTime >= :startTime))
            """)
    List<DoctorAvailabilityEntity> findOverlappingOneTime(@Param("doctorId") Long doctorId,
                                                          @Param("specificDate") LocalDate specificDate,
                                                          @Param("startTime") LocalTime startTime,
                                                          @Param("endTime") LocalTime endTime);

    @Query("""
            select a from DoctorAvailabilityEntity a
            where a.doctor.id = :doctorId
              and a.recurringWeekly = true
              and a.dayOfWeek = :dayOfWeek
              and ((a.startTime <= :endTime) and (a.endTime >= :startTime))
            """)
    List<DoctorAvailabilityEntity> findOverlappingWeekly(@Param("doctorId") Long doctorId,
                                                         @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                                         @Param("startTime") LocalTime startTime,
                                                         @Param("endTime") LocalTime endTime);

    Optional<DoctorAvailabilityEntity> findByIdAndDoctorId(Long id, Long doctorId);
}
