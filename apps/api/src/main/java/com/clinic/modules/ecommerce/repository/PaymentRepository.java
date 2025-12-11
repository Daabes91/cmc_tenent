package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.PaymentEntity;
import com.clinic.modules.ecommerce.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentEntity with tenant-scoped queries.
 * All queries automatically filter by tenant to ensure data isolation.
 */
@Repository("ecommercePaymentRepository")
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    /**
     * Find payment by ID with tenant isolation.
     *
     * @param id Payment ID
     * @param tenantId Tenant ID for isolation
     * @return Optional PaymentEntity
     */
    @Query("SELECT p FROM EcommercePayment p WHERE p.id = :id AND p.tenant.id = :tenantId")
    Optional<PaymentEntity> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Find payment by order ID with tenant isolation.
     *
     * @param orderId Order ID
     * @param tenantId Tenant ID for isolation
     * @return Optional PaymentEntity
     */
    @Query("SELECT p FROM EcommercePayment p WHERE p.order.id = :orderId AND p.tenant.id = :tenantId")
    Optional<PaymentEntity> findByOrderIdAndTenantId(@Param("orderId") Long orderId, @Param("tenantId") Long tenantId);

    /**
     * Find payment by provider order ID with tenant isolation.
     *
     * @param providerOrderId Provider order ID (e.g., PayPal order ID)
     * @param tenantId Tenant ID for isolation
     * @return Optional PaymentEntity
     */
    @Query("SELECT p FROM EcommercePayment p WHERE p.providerOrderId = :providerOrderId AND p.tenant.id = :tenantId")
    Optional<PaymentEntity> findByProviderOrderIdAndTenantId(@Param("providerOrderId") String providerOrderId, 
                                                             @Param("tenantId") Long tenantId);

    /**
     * Find payment by provider payment ID with tenant isolation.
     *
     * @param providerPaymentId Provider payment ID (e.g., PayPal payment ID)
     * @param tenantId Tenant ID for isolation
     * @return Optional PaymentEntity
     */
    @Query("SELECT p FROM EcommercePayment p WHERE p.providerPaymentId = :providerPaymentId AND p.tenant.id = :tenantId")
    Optional<PaymentEntity> findByProviderPaymentIdAndTenantId(@Param("providerPaymentId") String providerPaymentId, 
                                                               @Param("tenantId") Long tenantId);

    /**
     * Find all payments for a tenant with pagination.
     *
     * @param tenantId Tenant ID for isolation
     * @param pageable Pagination parameters
     * @return Page of PaymentEntity
     */
    @Query("SELECT p FROM EcommercePayment p WHERE p.tenant.id = :tenantId ORDER BY p.createdAt DESC")
    Page<PaymentEntity> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Find payments by status for a tenant.
     *
     * @param status Payment status
     * @param tenantId Tenant ID for isolation
     * @return List of PaymentEntity
     */
    @Query("SELECT p FROM EcommercePayment p WHERE p.status = :status AND p.tenant.id = :tenantId ORDER BY p.createdAt DESC")
    List<PaymentEntity> findByStatusAndTenantId(@Param("status") PaymentStatus status, @Param("tenantId") Long tenantId);

    /**
     * Find payments by provider for a tenant.
     *
     * @param provider Payment provider (e.g., "PAYPAL")
     * @param tenantId Tenant ID for isolation
     * @param pageable Pagination parameters
     * @return Page of PaymentEntity
     */
    @Query("SELECT p FROM EcommercePayment p WHERE p.provider = :provider AND p.tenant.id = :tenantId ORDER BY p.createdAt DESC")
    Page<PaymentEntity> findByProviderAndTenantId(@Param("provider") String provider, 
                                                  @Param("tenantId") Long tenantId, 
                                                  Pageable pageable);

    /**
     * Count payments by status for a tenant.
     *
     * @param status Payment status
     * @param tenantId Tenant ID for isolation
     * @return Count of payments
     */
    @Query("SELECT COUNT(p) FROM EcommercePayment p WHERE p.status = :status AND p.tenant.id = :tenantId")
    long countByStatusAndTenantId(@Param("status") PaymentStatus status, @Param("tenantId") Long tenantId);

    /**
     * Find all payments for multiple orders with tenant isolation.
     *
     * @param orderIds List of order IDs
     * @param tenantId Tenant ID for isolation
     * @return List of PaymentEntity
     */
    @Query("SELECT p FROM EcommercePayment p WHERE p.order.id IN :orderIds AND p.tenant.id = :tenantId")
    List<PaymentEntity> findByOrderIdsAndTenantId(@Param("orderIds") List<Long> orderIds, @Param("tenantId") Long tenantId);

    /**
     * Check if payment exists for order with tenant isolation.
     *
     * @param orderId Order ID
     * @param tenantId Tenant ID for isolation
     * @return true if payment exists
     */
    @Query("SELECT COUNT(p) > 0 FROM EcommercePayment p WHERE p.order.id = :orderId AND p.tenant.id = :tenantId")
    boolean existsByOrderIdAndTenantId(@Param("orderId") Long orderId, @Param("tenantId") Long tenantId);

    /**
     * Find the latest payment for an order with tenant isolation.
     *
     * @param orderId Order ID
     * @param tenantId Tenant ID for isolation
     * @return Optional PaymentEntity
     */
    @Query("SELECT p FROM EcommercePayment p WHERE p.order.id = :orderId AND p.tenant.id = :tenantId ORDER BY p.createdAt DESC LIMIT 1")
    Optional<PaymentEntity> findLatestByOrderIdAndTenantId(@Param("orderId") Long orderId, @Param("tenantId") Long tenantId);
}
