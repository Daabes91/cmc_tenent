package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.exception.InsufficientStockException;
import com.clinic.modules.ecommerce.exception.InvalidCartStateException;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.CartItemRepository;
import com.clinic.modules.ecommerce.repository.CartRepository;
import com.clinic.modules.ecommerce.repository.ProductRepository;
import com.clinic.modules.ecommerce.repository.ProductVariantRepository;
import com.clinic.modules.ecommerce.service.EcommerceFeatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for shopping cart management with availability checking and total calculation.
 * Handles cart creation, item management, and validation operations.
 */
@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final EcommerceFeatureService ecommerceFeatureService;

    @Autowired
    public CartService(CartRepository cartRepository,
                      CartItemRepository cartItemRepository,
                      ProductRepository productRepository,
                      ProductVariantRepository productVariantRepository,
                      EcommerceFeatureService ecommerceFeatureService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.ecommerceFeatureService = ecommerceFeatureService;
    }

    /**
     * Get or create cart for a session.
     * Creates new cart if none exists for the session.
     */
    public CartEntity getOrCreateCart(TenantEntity tenant, String sessionId) {
        logger.debug("Getting or creating cart for tenant {} and session {}", tenant.getId(), sessionId);

        requireEcommerceEnabled(tenant);
        
        Optional<CartEntity> existingCart = cartRepository.findByTenantIdAndSessionId(tenant.getId(), sessionId);
        
        if (existingCart.isPresent()) {
            CartEntity cart = existingCart.get();
            
            // Extend expiration if cart is close to expiring
            if (cart.getExpiresAt() != null && cart.getExpiresAt().isBefore(LocalDateTime.now().plusDays(1))) {
                cart.extendExpiration();
                cartRepository.save(cart);
                logger.debug("Extended expiration for cart {}", cart.getId());
            }
            
            return cart;
        }
        
        // Create new cart
        CartEntity newCart = new CartEntity(tenant, sessionId);
        CartEntity savedCart = cartRepository.save(newCart);
        logger.info("Created new cart {} for tenant {} and session {}", 
                   savedCart.getId(), tenant.getId(), sessionId);
        
        return savedCart;
    }

    /**
     * Add item to cart with availability checking.
     * Updates quantity if item already exists.
     */
    public CartEntity addItemToCart(TenantEntity tenant, String sessionId, Long productId, 
                                   Long variantId, Integer quantity) {
        logger.debug("Adding item to cart: tenant={}, session={}, product={}, variant={}, quantity={}", 
                    tenant.getId(), sessionId, productId, variantId, quantity);

        requireEcommerceEnabled(tenant);
        
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        
        // Get or create cart
        CartEntity cart = getOrCreateCart(tenant, sessionId);
        
        // Validate product
        ProductEntity product = productRepository.findByIdAndTenant(productId, tenant.getId())
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
        
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new InvalidCartStateException("Product is not available for purchase: " + productId);
        }
        
        ProductVariantEntity variant = null;
        BigDecimal unitPrice = product.getPrice();
        
        // Handle variant if specified
        if (variantId != null) {
            variant = productVariantRepository.findByIdAndTenant(variantId, tenant.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product variant not found: " + variantId));
            
            if (!variant.getIsInStock()) {
                throw new InsufficientStockException("Product variant is out of stock: " + variantId);
            }
            
            if (variant.getStockQuantity() < quantity) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", 
                                variant.getStockQuantity(), quantity));
            }
            
            unitPrice = variant.getPrice();
        }
        
        // Check if item already exists in cart
        Optional<CartItemEntity> existingItem = cartItemRepository
            .findByCartIdAndProductIdAndVariantId(cart.getId(), productId, variantId);
        
        if (existingItem.isPresent()) {
            // Update existing item
            CartItemEntity item = existingItem.get();
            Integer newQuantity = item.getQuantity() + quantity;
            
            // Validate new quantity against stock
            if (variant != null && variant.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for total quantity. Available: %d, Requested: %d", 
                                variant.getStockQuantity(), newQuantity));
            }
            
            item.updateQuantity(newQuantity);
            cartItemRepository.save(item);
            logger.debug("Updated existing cart item {} with new quantity {}", item.getId(), newQuantity);
        } else {
            // Create new item
            CartItemEntity newItem = new CartItemEntity(cart, product, variant, quantity, unitPrice);
            cartItemRepository.save(newItem);
            cart.addItem(newItem);
            logger.debug("Added new cart item {} to cart {}", newItem.getId(), cart.getId());
        }
        
        // Recalculate totals and save cart
        cart.recalculateTotals();
        CartEntity savedCart = cartRepository.save(cart);
        
        logger.info("Successfully added item to cart {}: product={}, variant={}, quantity={}", 
                   cart.getId(), productId, variantId, quantity);
        
        return savedCart;
    }

    /**
     * Update item quantity in cart.
     */
    public CartEntity updateItemQuantity(TenantEntity tenant, String sessionId, Long itemId, Integer quantity) {
        logger.debug("Updating cart item quantity: tenant={}, session={}, item={}, quantity={}", 
                    tenant.getId(), sessionId, itemId, quantity);

        requireEcommerceEnabled(tenant);
        
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        
        CartEntity cart = getOrCreateCart(tenant, sessionId);
        
        CartItemEntity item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new InvalidCartStateException("Cart item not found: " + itemId));
        
        // Verify item belongs to the cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new InvalidCartStateException("Cart item does not belong to the specified cart");
        }
        
        // Validate stock availability
        if (item.getVariant() != null) {
            if (!item.getVariant().getIsInStock()) {
                throw new InsufficientStockException("Product variant is out of stock");
            }
            
            if (item.getVariant().getStockQuantity() < quantity) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", 
                                item.getVariant().getStockQuantity(), quantity));
            }
        }
        
        item.updateQuantity(quantity);
        cartItemRepository.save(item);
        
        cart.recalculateTotals();
        CartEntity savedCart = cartRepository.save(cart);
        
        logger.info("Updated cart item {} quantity to {}", itemId, quantity);
        
        return savedCart;
    }

    /**
     * Remove item from cart.
     */
    public CartEntity removeItemFromCart(TenantEntity tenant, String sessionId, Long itemId) {
        logger.debug("Removing item from cart: tenant={}, session={}, item={}", 
                    tenant.getId(), sessionId, itemId);

        requireEcommerceEnabled(tenant);
        
        CartEntity cart = getOrCreateCart(tenant, sessionId);
        
        CartItemEntity item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new InvalidCartStateException("Cart item not found: " + itemId));
        
        // Verify item belongs to the cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new InvalidCartStateException("Cart item does not belong to the specified cart");
        }
        
        cart.removeItem(item);
        cartItemRepository.delete(item);
        
        cart.recalculateTotals();
        CartEntity savedCart = cartRepository.save(cart);
        
        logger.info("Removed cart item {} from cart {}", itemId, cart.getId());
        
        return savedCart;
    }

    /**
     * Clear all items from cart.
     */
    public CartEntity clearCart(TenantEntity tenant, String sessionId) {
        logger.debug("Clearing cart: tenant={}, session={}", tenant.getId(), sessionId);

        requireEcommerceEnabled(tenant);
        
        CartEntity cart = getOrCreateCart(tenant, sessionId);
        
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();
        cart.recalculateTotals();
        
        CartEntity savedCart = cartRepository.save(cart);
        
        logger.info("Cleared all items from cart {}", cart.getId());
        
        return savedCart;
    }

    /**
     * Get cart by session ID.
     */
    @Transactional(readOnly = true)
    public Optional<CartEntity> getCart(TenantEntity tenant, String sessionId) {
        requireEcommerceEnabled(tenant);
        return cartRepository.findByTenantIdAndSessionId(tenant.getId(), sessionId);
    }

    /**
     * Validate cart items availability.
     * Returns list of unavailable items.
     */
    @Transactional(readOnly = true)
    public List<CartItemEntity> validateCartAvailability(TenantEntity tenant, String sessionId) {
        logger.debug("Validating cart availability: tenant={}, session={}", tenant.getId(), sessionId);

        requireEcommerceEnabled(tenant);
        
        Optional<CartEntity> cartOpt = getCart(tenant, sessionId);
        if (cartOpt.isEmpty()) {
            return List.of();
        }
        
        CartEntity cart = cartOpt.get();
        return cart.getItems().stream()
            .filter(item -> !item.isAvailable())
            .toList();
    }

    /**
     * Update customer email for cart.
     */
    public CartEntity updateCustomerEmail(TenantEntity tenant, String sessionId, String email) {
        logger.debug("Updating customer email for cart: tenant={}, session={}, email={}", 
                    tenant.getId(), sessionId, email);

        requireEcommerceEnabled(tenant);
        
        CartEntity cart = getOrCreateCart(tenant, sessionId);
        cart.setCustomerEmail(email);
        
        CartEntity savedCart = cartRepository.save(cart);
        
        logger.info("Updated customer email for cart {}", cart.getId());
        
        return savedCart;
    }

    /**
     * Get cart statistics for tenant.
     */
    @Transactional(readOnly = true)
    public CartStatistics getCartStatistics(TenantEntity tenant) {
        requireEcommerceEnabled(tenant);
        Object[] stats = cartRepository.getCartStatisticsByTenant(tenant.getId(), LocalDateTime.now());
        
        Long totalCarts = ((Number) stats[0]).longValue();
        Long activeCarts = ((Number) stats[1]).longValue();
        BigDecimal totalValue = (BigDecimal) stats[2];
        
        return new CartStatistics(totalCarts, activeCarts, totalValue);
    }

    /**
     * Clean up expired carts for tenant.
     */
    public int cleanupExpiredCarts(TenantEntity tenant) {
        logger.debug("Cleaning up expired carts for tenant {}", tenant.getId());

        requireEcommerceEnabled(tenant);
        
        int deletedCount = cartRepository.deleteExpiredCartsByTenant(tenant.getId(), LocalDateTime.now());
        
        logger.info("Cleaned up {} expired carts for tenant {}", deletedCount, tenant.getId());
        
        return deletedCount;
    }

    /**
     * Cart statistics data class.
     */
    public static class CartStatistics {
        private final Long totalCarts;
        private final Long activeCarts;
        private final BigDecimal totalValue;

        public CartStatistics(Long totalCarts, Long activeCarts, BigDecimal totalValue) {
            this.totalCarts = totalCarts;
            this.activeCarts = activeCarts;
            this.totalValue = totalValue != null ? totalValue : BigDecimal.ZERO;
        }

        public Long getTotalCarts() { return totalCarts; }
        public Long getActiveCarts() { return activeCarts; }
        public BigDecimal getTotalValue() { return totalValue; }
    }

    private void requireEcommerceEnabled(TenantEntity tenant) {
        ecommerceFeatureService.validateEcommerceEnabled(tenant.getId());
    }
}
