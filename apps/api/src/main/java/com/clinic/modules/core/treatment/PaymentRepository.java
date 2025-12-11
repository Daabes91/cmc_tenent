package com.clinic.modules.core.treatment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository("treatmentPaymentRepository")
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByPaymentDateBetween(Instant start, Instant end);

    @Query("""
            select p from PaymentEntity p
            where p.visit.treatmentPlan.tenant.id = :tenantId
              and p.paymentDate between :start and :end
            """)
    List<PaymentEntity> findByTenantIdAndPaymentDateBetween(@Param("tenantId") Long tenantId, 
                                                             @Param("start") Instant start, 
                                                             @Param("end") Instant end);
}
