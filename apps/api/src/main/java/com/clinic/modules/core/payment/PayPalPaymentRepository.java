package com.clinic.modules.core.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayPalPaymentRepository extends JpaRepository<PayPalPaymentEntity, Long> {
    
    Optional<PayPalPaymentEntity> findByOrderId(String orderId);
    
    boolean existsByOrderId(String orderId);
    
    // Date-filtered queries for reports
    @Query("""
            select p from PayPalPaymentEntity p
            where p.createdAt between :start and :end
            order by p.createdAt desc
            """)
    List<PayPalPaymentEntity> findByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);
    
    @Query("""
            select p from PayPalPaymentEntity p
            where p.createdAt between :start and :end
            order by p.createdAt desc
            """)
    Page<PayPalPaymentEntity> findByCreatedAtBetween(@Param("start") Instant start, 
                                                     @Param("end") Instant end, 
                                                     Pageable pageable);
    
    @Query("""
            select count(p) from PayPalPaymentEntity p
            where p.status = :status
              and p.createdAt between :start and :end
            """)
    long countByStatusAndCreatedAtBetween(@Param("status") PaymentStatus status,
                                          @Param("start") Instant start,
                                          @Param("end") Instant end);
}