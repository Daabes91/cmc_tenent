package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for cart management with tenant isolation.
 * Provides methods for cart CRUD operations and session management.
 */
@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {

    /**
     * Find cart by tenant ID and session ID.
     * Used for retrieving existing cart for a session.
     */
    @Query("SELECT c FROM CartEntity c WHERE c.tenant.id = :tenantId AND c.sessionId = :sessionId")
    Optional<CartEntity> findByTenantIdAndSessionId(@Param("tenantId") Long tenantId, 
                                                   @Param("sessionId") String sessionId);

    /**
     * Find all carts for a tenant.
     * Used for admin purposes and analytics.
     */
    @Query("SELECT c FROM CartEntity c WHERE c.tenant.id = :tenantId ORDER BY c.updatedAt DESC")
    List<CartEntity> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find carts by customer email within a tenant.
     * Used for customer service and order history.
     */
    @Query("SELECT c FROM CartEntity c WHERE c.tenant.id = :tenantId AND c.customerEmail = :email ORDER BY c.updatedAt DESC")
    List<CartEntity> findByTenantIdAndCustomerEmail(@Param("tenantId") Long tenantId, 
                                                   @Param("email") String email);

    /**
     * Find expired carts for cleanup.
     * Used by scheduled jobs to clean up old carts.
     */
    @Query("SELECT c FROM CartEntity c WHERE c.expiresAt < :now")
    List<CartEntity> findExpiredCarts(@Param("now") LocalDateTime now);

    /**
     * Find expired carts for a specific tenant.
     */
    @Query("SELECT c FROM CartEntity c WHERE c.tenant.id = :tenantId AND c.expiresAt < :now")
    List<CartEntity> findExpiredCartsByTenant(@Param("tenantId") Long tenantId, 
                                             @Param("now") LocalDateTime now);

    /**
     * Find empty carts (carts with no items).
     * Used for cleanup and analytics.
     */
    @Query("SELECT c FROM CartEntity c WHERE c.tenant.id = :tenantId AND SIZE(c.items) = 0")
    List<CartEntity> findEmptyCartsByTenant(@Param("tenantId") Long tenantId);

    /**
     * Find carts created within a date range for a tenant.
     * Used for analytics and reporting.
     */
    @Query("SELECT c FROM CartEntity c WHERE c.tenant.id = :tenantId AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<CartEntity> findByTenantIdAndCreatedAtBetween(@Param("tenantId") Long tenantId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Count active carts for a tenant.
     * Used for analytics and dashboard metrics.
     */
    @Query("SELECT COUNT(c) FROM CartEntity c WHERE c.tenant.id = :tenantId AND c.expiresAt > :now")
    long countActiveCartsByTenant(@Param("tenantId") Long tenantId, @Param("now") LocalDateTime now);

    /**
     * Delete expired carts in batch.
     * Used by cleanup jobs for performance.
     */
    @Modifying
    @Query("DELETE FROM CartEntity c WHERE c.expiresAt < :now")
    int deleteExpiredCarts(@Param("now") LocalDateTime now);

    /**
     * Delete expired carts for a specific tenant.
     */
    @Modifying
    @Query("DELETE FROM CartEntity c WHERE c.tenant.id = :tenantId AND c.expiresAt < :now")
    int deleteExpiredCartsByTenant(@Param("tenantId") Long tenantId, @Param("now") LocalDateTime now);

    /**
     * Delete empty carts for a tenant.
     * Used for cleanup operations.
     */
    @Modifying
    @Query("DELETE FROM CartEntity c WHERE c.tenant.id = :tenantId AND SIZE(c.items) = 0 AND c.updatedAt < :cutoffDate")
    int deleteEmptyCartsByTenant(@Param("tenantId") Long tenantId, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Update cart expiration time.
     * Used to extend cart lifetime when customer is active.
     */
    @Modifying
    @Query("UPDATE CartEntity c SET c.expiresAt = :newExpirationTime, c.updatedAt = :now WHERE c.id = :cartId")
    int updateCartExpiration(@Param("cartId") Long cartId, 
                           @Param("newExpirationTime") LocalDateTime newExpirationTime,
                           @Param("now") LocalDateTime now);

    /**
     * Check if cart exists for tenant and session.
     * Used for quick existence checks without loading the entity.
     */
    @Query("SELECT COUNT(c) > 0 FROM CartEntity c WHERE c.tenant.id = :tenantId AND c.sessionId = :sessionId")
    boolean existsByTenantIdAndSessionId(@Param("tenantId") Long tenantId, @Param("sessionId") String sessionId);

    /**
     * Get cart statistics for a tenant.
     * Returns total carts, active carts, and total value.
     */
    @Query("SELECT COUNT(c), " +
           "SUM(CASE WHEN c.expiresAt > :now THEN 1 ELSE 0 END), " +
           "COALESCE(SUM(c.totalAmount), 0) " +
           "FROM CartEntity c WHERE c.tenant.id = :tenantId")
    Object[] getCartStatisticsByTenant(@Param("tenantId") Long tenantId, @Param("now") LocalDateTime now);
}