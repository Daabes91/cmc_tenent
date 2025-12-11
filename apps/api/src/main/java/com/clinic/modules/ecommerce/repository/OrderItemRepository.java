package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for order item management with tenant-scoped queries.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    /**
     * Find order item by ID and tenant.
     */
    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.id = :id AND oi.tenant.id = :tenantId")
    Optional<OrderItemEntity> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Find all order items for a specific order.
     */
    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.order.id = :orderId AND oi.tenant.id = :tenantId ORDER BY oi.id")
    List<OrderItemEntity> findByOrderIdAndTenant(@Param("orderId") Long orderId, @Param("tenantId") Long tenantId);

    /**
     * Find order items by product and tenant.
     */
    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.product.id = :productId AND oi.tenant.id = :tenantId ORDER BY oi.createdAt DESC")
    List<OrderItemEntity> findByProductIdAndTenant(@Param("productId") Long productId, @Param("tenantId") Long tenantId);

    /**
     * Find order items by product variant and tenant.
     */
    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.variant.id = :variantId AND oi.tenant.id = :tenantId ORDER BY oi.createdAt DESC")
    List<OrderItemEntity> findByVariantIdAndTenant(@Param("variantId") Long variantId, @Param("tenantId") Long tenantId);

    /**
     * Get product sales statistics for tenant.
     */
    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity), SUM(oi.totalPrice) " +
           "FROM OrderItemEntity oi WHERE oi.tenant.id = :tenantId " +
           "GROUP BY oi.product.id, oi.productName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getProductSalesStatistics(@Param("tenantId") Long tenantId);

    /**
     * Get variant sales statistics for tenant.
     */
    @Query("SELECT oi.variant.id, oi.variantName, SUM(oi.quantity), SUM(oi.totalPrice) " +
           "FROM OrderItemEntity oi WHERE oi.tenant.id = :tenantId AND oi.variant IS NOT NULL " +
           "GROUP BY oi.variant.id, oi.variantName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getVariantSalesStatistics(@Param("tenantId") Long tenantId);

    /**
     * Count total items sold for a product.
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItemEntity oi WHERE oi.product.id = :productId AND oi.tenant.id = :tenantId")
    Long getTotalQuantitySoldForProduct(@Param("productId") Long productId, @Param("tenantId") Long tenantId);

    /**
     * Count total items sold for a variant.
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItemEntity oi WHERE oi.variant.id = :variantId AND oi.tenant.id = :tenantId")
    Long getTotalQuantitySoldForVariant(@Param("variantId") Long variantId, @Param("tenantId") Long tenantId);

    /**
     * Get top selling products for tenant.
     */
    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity) as totalSold " +
           "FROM OrderItemEntity oi WHERE oi.tenant.id = :tenantId " +
           "GROUP BY oi.product.id, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProducts(@Param("tenantId") Long tenantId);

    /**
     * Delete all order items for a specific order.
     */
    @Query("DELETE FROM OrderItemEntity oi WHERE oi.order.id = :orderId AND oi.tenant.id = :tenantId")
    void deleteByOrderIdAndTenant(@Param("orderId") Long orderId, @Param("tenantId") Long tenantId);
}