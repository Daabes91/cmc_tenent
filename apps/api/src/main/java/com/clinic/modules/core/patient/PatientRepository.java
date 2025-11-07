package com.clinic.modules.core.patient;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    Optional<PatientEntity> findByEmailIgnoreCase(String email);

    Optional<PatientEntity> findByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    long countByCreatedAtAfter(Instant threshold);

    @Query("""
            select count(p) from PatientEntity p
            where p.createdAt between :start and :end
            """)
    long countByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select p from PatientEntity p
            where lower(concat(p.firstName, ' ', p.lastName)) like lower(concat('%', :term, '%'))
               or lower(p.email) like lower(concat('%', :term, '%'))
               or lower(coalesce(p.phone, '')) like lower(concat('%', :term, '%'))
            order by p.createdAt desc
            """)
    List<PatientEntity> searchPatients(@Param("term") String term, Pageable pageable);
}
