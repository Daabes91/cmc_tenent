package com.clinic.modules.ecommerce.repository;

import com.clinic.modules.ecommerce.model.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for cart item management with tenant isolation.
 * Provides methods for cart item CRUD operations and availability checking.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    /**
     * Find all items in a cart.
     * Used for cart display and total calculations.
     */
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.cart.id = :cartId ORDER BY ci.createdAt ASC")
    List<CartItemEntity> findByCartId(@Param("cartId") Long cartId);

    /**
     * Find all items in carts for a tenant.
     * Used for analytics and reporting.
     */
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.tenant.id = :tenantId ORDER BY ci.createdAt DESC")
    List<CartItemEntity> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find cart item by cart, product, and variant.
     * Used to check if item already exists before adding.
     */
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId AND " +
           "(:variantId IS NULL AND ci.variant IS NULL OR ci.variant.id = :variantId)")
    Optional<CartItemEntity> findByCartIdAndProductIdAndVariantId(@Param("cartId") Long cartId,
                                                                 @Param("productId") Long productId,
                                                                 @Param("variantId") Long variantId);

    /**
     * Find all cart items for a specific product.
     * Used for inventory management and product analytics.
     */
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.product.id = :productId")
    List<CartItemEntity> findByProductId(@Param("productId") Long productId);

    /**
     * Find all cart items for a specific product variant.
     * Used for inventory management and variant analytics.
     */
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.variant.id = :variantId")
    List<CartItemEntity> findByVariantId(@Param("variantId") Long variantId);

    /**
     * Count items in a cart.
     * Used for quick cart item count without loading all items.
     */
    @Query("SELECT COUNT(ci) FROM CartItemEntity ci WHERE ci.cart.id = :cartId")
    long countByCartId(@Param("cartId") Long cartId);

    /**
     * Get total quantity of items in a cart.
     * Used for cart badge display and analytics.
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItemEntity ci WHERE ci.cart.id = :cartId")
    int getTotalQuantityByCartId(@Param("cartId") Long cartId);

    /**
     * Get total value of items in a cart.
     * Used for cart total calculation verification.
     */
    @Query("SELECT COALESCE(SUM(ci.totalPrice), 0) FROM CartItemEntity ci WHERE ci.cart.id = :cartId")
    java.math.BigDecimal getTotalValueByCartId(@Param("cartId") Long cartId);

    /**
     * Find cart items by product within a tenant.
     * Used for product impact analysis and inventory management.
     */
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.tenant.id = :tenantId AND ci.product.id = :productId")
    List<CartItemEntity> findByTenantIdAndProductId(@Param("tenantId") Long tenantId, 
                                                   @Param("productId") Long productId);

    /**
     * Find cart items by variant within a tenant.
     * Used for variant impact analysis and inventory management.
     */
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.tenant.id = :tenantId AND ci.variant.id = :variantId")
    List<CartItemEntity> findByTenantIdAndVariantId(@Param("tenantId") Long tenantId, 
                                                   @Param("variantId") Long variantId);

    /**
     * Delete all items from a cart.
     * Used for cart clearing operations.
     */
    @Modifying
    @Query("DELETE FROM CartItemEntity ci WHERE ci.cart.id = :cartId")
    int deleteByCartId(@Param("cartId") Long cartId);

    /**
     * Delete cart items for a specific product.
     * Used when a product is deleted or becomes unavailable.
     */
    @Modifying
    @Query("DELETE FROM CartItemEntity ci WHERE ci.product.id = :productId")
    int deleteByProductId(@Param("productId") Long productId);

    /**
     * Delete cart items for a specific variant.
     * Used when a variant is deleted or becomes unavailable.
     */
    @Modifying
    @Query("DELETE FROM CartItemEntity ci WHERE ci.variant.id = :variantId")
    int deleteByVariantId(@Param("variantId") Long variantId);

    /**
     * Check if cart item exists.
     * Used for quick existence checks without loading the entity.
     */
    @Query("SELECT COUNT(ci) > 0 FROM CartItemEntity ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId AND " +
           "(:variantId IS NULL AND ci.variant IS NULL OR ci.variant.id = :variantId)")
    boolean existsByCartIdAndProductIdAndVariantId(@Param("cartId") Long cartId,
                                                  @Param("productId") Long productId,
                                                  @Param("variantId") Long variantId);

    /**
     * Get cart item statistics for a tenant.
     * Returns total items, total quantity, and total value.
     */
    @Query("SELECT COUNT(ci), COALESCE(SUM(ci.quantity), 0), COALESCE(SUM(ci.totalPrice), 0) " +
           "FROM CartItemEntity ci WHERE ci.tenant.id = :tenantId")
    Object[] getCartItemStatisticsByTenant(@Param("tenantId") Long tenantId);

    /**
     * Find items that may be out of stock.
     * Used for inventory validation and customer notifications.
     */
    @Query("SELECT ci FROM CartItemEntity ci " +
           "LEFT JOIN ci.variant v " +
           "WHERE ci.tenant.id = :tenantId AND " +
           "(ci.variant IS NOT NULL AND (v.isInStock = false OR v.stockQuantity < ci.quantity) OR " +
           "ci.variant IS NULL AND ci.product.status != 'ACTIVE')")
    List<CartItemEntity> findUnavailableItemsByTenant(@Param("tenantId") Long tenantId);
}
