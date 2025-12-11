package com.clinic.modules.ecommerce.controller.publicapi;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.ecommerce.dto.*;
import com.clinic.modules.ecommerce.model.CartEntity;
import com.clinic.modules.ecommerce.model.CartItemEntity;
import com.clinic.modules.ecommerce.service.CartService;
import com.clinic.modules.ecommerce.exception.InvalidCartStateException;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.exception.InsufficientStockException;
import com.clinic.modules.ecommerce.service.EcommerceFeatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Public API controller for shopping cart management.
 * Handles cart operations for customers without authentication.
 */
@RestController
@RequestMapping("/public/cart")
public class PublicCartController {

    private static final Logger logger = LoggerFactory.getLogger(PublicCartController.class);

    private final CartService cartService;
    private final TenantService tenantService;
    private final EcommerceFeatureService ecommerceFeatureService;

    @Autowired
    public PublicCartController(CartService cartService, TenantService tenantService,
                                EcommerceFeatureService ecommerceFeatureService) {
        this.cartService = cartService;
        this.tenantService = tenantService;
        this.ecommerceFeatureService = ecommerceFeatureService;
    }

    /**
     * Get current cart for session.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String domain,
            @RequestParam(name = "session_id", required = false) String sessionIdParam,
            HttpServletRequest request) {
        
        logger.debug("Getting cart for slug: {}, domain: {}", slug, domain);

        try {
            TenantEntity tenant = resolveTenant(slug, domain, request);
            String sessionId = getOrCreateSessionId(request, sessionIdParam);

            Optional<CartEntity> cartOpt = cartService.getCart(tenant, sessionId);

            if (cartOpt.isEmpty()) {
                // Return empty cart response with session id so client can reuse it
                CartResponse emptyCart = new CartResponse();
                emptyCart.setSessionId(sessionId);
                emptyCart.setItems(List.of());
                emptyCart.setItemCount(0);
                emptyCart.setTotalQuantity(0);
                return ResponseEntity.ok(emptyCart);
            }

            CartEntity cart = cartOpt.get();
            CartResponse response = mapToCartResponse(cart, sessionId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get cart", e);
            CartResponse error = new CartResponse();
            error.setItems(List.of());
            error.setItemCount(0);
            error.setTotalQuantity(0);
            error.setSessionId(sessionIdParam);
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Add item to cart.
     */
    @PostMapping("/items")
    @Transactional
    public ResponseEntity<CartResponse> addToCart(
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String domain,
            @RequestParam(name = "session_id", required = false) String sessionIdParam,
            @Valid @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest) {
        
        logger.debug("Adding item to cart: {}", request);
        
        TenantEntity tenant = resolveTenant(slug, domain, httpRequest);
        String sessionId = getOrCreateSessionId(httpRequest, sessionIdParam);

        try {
            CartEntity cart = cartService.addItemToCart(
                tenant, sessionId, request.getProductId(),
                request.getVariantId(), request.getQuantity());

            CartResponse response = mapToCartResponse(cart, sessionId);
            return ResponseEntity.ok(response);
        } catch (ProductNotFoundException e) {
            logger.warn("Add to cart failed - product not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(errorCartResponse(sessionId, "PRODUCT_NOT_FOUND", e.getMessage()));
        } catch (InsufficientStockException | InvalidCartStateException e) {
            logger.warn("Add to cart failed - {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorCartResponse(sessionId, "INVALID_CART_STATE", e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Add to cart failed - bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorCartResponse(sessionId, "INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            logger.error("Add to cart failed unexpectedly", e);
            return ResponseEntity.internalServerError().body(errorCartResponse(sessionId, "INTERNAL_ERROR", "Failed to add item to cart"));
        }
    }

    /**
     * Update cart item quantity.
     */
    @PutMapping("/items/{itemId}")
    @Transactional
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long itemId,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String domain,
            @RequestParam(name = "session_id", required = false) String sessionIdParam,
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpServletRequest httpRequest) {
        
        logger.debug("Updating cart item {}: {}", itemId, request);
        
        TenantEntity tenant = resolveTenant(slug, domain, httpRequest);
        String sessionId = getOrCreateSessionId(httpRequest, sessionIdParam);
        
        CartEntity cart = cartService.updateItemQuantity(
            tenant, sessionId, itemId, request.getQuantity());
        
        CartResponse response = mapToCartResponse(cart, sessionId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Remove item from cart.
     */
    @DeleteMapping("/items/{itemId}")
    @Transactional
    public ResponseEntity<CartResponse> removeCartItem(
            @PathVariable Long itemId,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String domain,
            @RequestParam(name = "session_id", required = false) String sessionIdParam,
            HttpServletRequest httpRequest) {
        
        logger.debug("Removing cart item: {}", itemId);
        
        TenantEntity tenant = resolveTenant(slug, domain, httpRequest);
        String sessionId = getOrCreateSessionId(httpRequest, sessionIdParam);
        
        CartEntity cart = cartService.removeItemFromCart(tenant, sessionId, itemId);
        
        CartResponse response = mapToCartResponse(cart, sessionId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clear all items from cart.
     */
    @DeleteMapping
    @Transactional
    public ResponseEntity<CartResponse> clearCart(
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String domain,
            @RequestParam(name = "session_id", required = false) String sessionIdParam,
            HttpServletRequest httpRequest) {
        
        logger.debug("Clearing cart");
        
        TenantEntity tenant = resolveTenant(slug, domain, httpRequest);
        String sessionId = getOrCreateSessionId(httpRequest, sessionIdParam);
        
        CartEntity cart = cartService.clearCart(tenant, sessionId);
        
        CartResponse response = mapToCartResponse(cart, sessionId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update customer email for cart.
     */
    @PutMapping("/customer")
    public ResponseEntity<CartResponse> updateCustomerEmail(
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String domain,
            @RequestParam(name = "session_id", required = false) String sessionIdParam,
            @Valid @RequestBody UpdateCustomerEmailRequest request,
            HttpServletRequest httpRequest) {
        
        logger.debug("Updating customer email: {}", request.getEmail());
        
        TenantEntity tenant = resolveTenant(slug, domain, httpRequest);
        String sessionId = getOrCreateSessionId(httpRequest, sessionIdParam);
        
        CartEntity cart = cartService.updateCustomerEmail(
            tenant, sessionId, request.getEmail());
        
        CartResponse response = mapToCartResponse(cart, sessionId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Validate cart items availability.
     */
    @GetMapping("/validate")
    public ResponseEntity<List<CartItemResponse>> validateCart(
            @RequestParam(required = false) String slug,
        @RequestParam(required = false) String domain,
        @RequestParam(name = "session_id", required = false) String sessionIdParam,
        HttpServletRequest httpRequest) {
        
        logger.debug("Validating cart availability");
        
        TenantEntity tenant = resolveTenant(slug, domain, httpRequest);
        String sessionId = getOrCreateSessionId(httpRequest, sessionIdParam);
        
        List<CartItemEntity> unavailableItems = cartService.validateCartAvailability(tenant, sessionId);
        
        List<CartItemResponse> response = unavailableItems.stream()
            .map(this::mapToCartItemResponse)
            .toList();
        
        return ResponseEntity.ok(response);
    }

    // Helper methods
    private String getOrCreateSessionId(HttpServletRequest request, String providedSessionId) {
        if (providedSessionId != null && !providedSessionId.isBlank()) {
            return providedSessionId.trim();
        }
        HttpSession session = request.getSession(true);
        return session.getId();
    }

    private TenantEntity resolveTenant(String slug, String domain, HttpServletRequest request) {
        String headerSlug = request.getHeader("x-tenant-slug");
        String effectiveSlug = (slug != null && !slug.isBlank()) ? slug : headerSlug;
        if (effectiveSlug != null && !effectiveSlug.isBlank()) {
            TenantEntity tenant = tenantService.requireActiveTenantBySlug(effectiveSlug.trim());
            ecommerceFeatureService.validateEcommerceEnabled(tenant.getId());
            return tenant;
        }

        if (domain != null && !domain.trim().isEmpty()) {
            TenantEntity tenant = tenantService.findActiveByDomain(domain.trim())
                .orElseThrow(() -> new RuntimeException("Tenant not found for domain: " + domain));
            ecommerceFeatureService.validateEcommerceEnabled(tenant.getId());
            return tenant;
        }

        throw new RuntimeException("Either slug or domain parameter is required");
    }

    private CartResponse mapToCartResponse(CartEntity cart, String sessionId) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setSessionId(sessionId);
        response.setCustomerEmail(cart.getCustomerEmail());
        response.setSubtotal(cart.getSubtotal(), cart.getCurrency());
        response.setTaxAmount(cart.getTaxAmount(), cart.getCurrency());
        response.setTotalAmount(cart.getTotalAmount(), cart.getCurrency());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        response.setExpiresAt(cart.getExpiresAt());
        
        List<CartItemResponse> items = cart.getItems().stream()
            .map(this::mapToCartItemResponse)
            .toList();
        
        response.setItems(items);
        response.setItemCount(items.size());
        response.setTotalQuantity(cart.getTotalItemCount());
        
        return response;
    }

    private CartItemResponse mapToCartItemResponse(CartItemEntity item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setProductSlug(item.getProduct().getSlug());
        response.setSku(item.getSku());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());
        response.setCurrency(item.getCurrency());
        response.setIsAvailable(item.isAvailable());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        
        if (item.getVariant() != null) {
            response.setVariantId(item.getVariant().getId());
            response.setVariantName(item.getVariant().getName());
            response.setStockQuantity(item.getVariant().getStockQuantity());
        }
        
        // Get main product image if available
        if (!item.getProduct().getImages().isEmpty()) {
            response.setImageUrl(item.getProduct().getImages().get(0).getImageUrl());
        }
        
        return response;
    }

    private CartResponse errorCartResponse(String sessionId, String code, String message) {
        CartResponse response = new CartResponse();
        response.setSessionId(sessionId);
        response.setItems(List.of());
        response.setItemCount(0);
        response.setTotalQuantity(0);
        return response;
    }
}
