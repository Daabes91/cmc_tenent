package com.clinic.modules.core.treatment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FollowUpVisitRepository extends JpaRepository<FollowUpVisitEntity, Long> {

    List<FollowUpVisitEntity> findByTreatmentPlanIdOrderByVisitNumberAsc(Long treatmentPlanId);

    List<FollowUpVisitEntity> findByTreatmentPlanId(Long treatmentPlanId);

    long countByVisitDateBetween(Instant start, Instant end);
}
