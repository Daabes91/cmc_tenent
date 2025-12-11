package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.OrderEntity;
import com.clinic.modules.ecommerce.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for order management with tenant-scoped queries.
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /**
     * Find order by ID and tenant.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.id = :id AND o.tenant.id = :tenantId")
    Optional<OrderEntity> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Find order by order number and tenant.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.orderNumber = :orderNumber AND o.tenant.id = :tenantId")
    Optional<OrderEntity> findByOrderNumberAndTenant(@Param("orderNumber") String orderNumber, @Param("tenantId") Long tenantId);

    /**
     * Find all orders for a tenant with pagination.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.tenant.id = :tenantId ORDER BY o.createdAt DESC")
    Page<OrderEntity> findByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Find orders by status and tenant.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.tenant.id = :tenantId AND o.status = :status ORDER BY o.createdAt DESC")
    Page<OrderEntity> findByTenantAndStatus(@Param("tenantId") Long tenantId, @Param("status") OrderStatus status, Pageable pageable);

    /**
     * Find orders by customer email and tenant.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.tenant.id = :tenantId AND o.customerEmail = :email ORDER BY o.createdAt DESC")
    Page<OrderEntity> findByTenantAndCustomerEmail(@Param("tenantId") Long tenantId, @Param("email") String email, Pageable pageable);

    /**
     * Find orders created within date range for tenant.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.tenant.id = :tenantId AND o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    Page<OrderEntity> findByTenantAndDateRange(@Param("tenantId") Long tenantId, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate, 
                                              Pageable pageable);

    /**
     * Count orders by status for tenant.
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.tenant.id = :tenantId AND o.status = :status")
    Long countByTenantAndStatus(@Param("tenantId") Long tenantId, @Param("status") OrderStatus status);

    /**
     * Get order statistics for tenant.
     */
    @Query("SELECT COUNT(o), SUM(o.totalAmount), AVG(o.totalAmount) FROM OrderEntity o WHERE o.tenant.id = :tenantId")
    Object[] getOrderStatisticsByTenant(@Param("tenantId") Long tenantId);

    /**
     * Get order statistics by status for tenant.
     */
    @Query("SELECT o.status, COUNT(o), SUM(o.totalAmount) FROM OrderEntity o WHERE o.tenant.id = :tenantId GROUP BY o.status")
    List<Object[]> getOrderStatisticsByStatusAndTenant(@Param("tenantId") Long tenantId);

    /**
     * Find recent orders for tenant.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.tenant.id = :tenantId ORDER BY o.createdAt DESC")
    List<OrderEntity> findRecentOrdersByTenant(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Check if order number exists for tenant.
     */
    @Query("SELECT COUNT(o) > 0 FROM OrderEntity o WHERE o.tenant.id = :tenantId AND o.orderNumber = :orderNumber")
    boolean existsByTenantAndOrderNumber(@Param("tenantId") Long tenantId, @Param("orderNumber") String orderNumber);

    /**
     * Find orders that need status updates (e.g., pending payment for too long).
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.tenant.id = :tenantId AND o.status = :status AND o.createdAt < :cutoffDate")
    List<OrderEntity> findOrdersForStatusUpdate(@Param("tenantId") Long tenantId, 
                                               @Param("status") OrderStatus status, 
                                               @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get monthly order summary for tenant.
     */
    @Query("SELECT YEAR(o.createdAt), MONTH(o.createdAt), COUNT(o), SUM(o.totalAmount) " +
           "FROM OrderEntity o WHERE o.tenant.id = :tenantId " +
           "AND o.createdAt >= :startDate " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY YEAR(o.createdAt) DESC, MONTH(o.createdAt) DESC")
    List<Object[]> getMonthlyOrderSummary(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate);

    /**
     * Search orders by customer name or email.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.tenant.id = :tenantId " +
           "AND (LOWER(o.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY o.createdAt DESC")
    Page<OrderEntity> searchOrdersByCustomer(@Param("tenantId") Long tenantId, 
                                           @Param("searchTerm") String searchTerm, 
                                           Pageable pageable);
}