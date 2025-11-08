package com.clinic.modules.core.treatment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FollowUpVisitRepository extends JpaRepository<FollowUpVisitEntity, Long> {

    List<FollowUpVisitEntity> findByTreatmentPlanIdOrderByVisitNumberAsc(Long treatmentPlanId);

    List<FollowUpVisitEntity> findByTreatmentPlanId(Long treatmentPlanId);

    long countByVisitDateBetween(Instant start, Instant end);

    @Query("""
            select count(v) from FollowUpVisitEntity v
            where v.treatmentPlan.tenant.id = :tenantId
              and v.visitDate between :start and :end
            """)
    long countByTenantIdAndVisitDateBetween(@Param("tenantId") Long tenantId,
                                            @Param("start") Instant start,
                                            @Param("end") Instant end);
}
