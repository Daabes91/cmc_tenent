package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.dto.BuyNowRequest;
import com.clinic.modules.ecommerce.dto.CreateOrderRequest;
import com.clinic.modules.ecommerce.exception.InvalidCartStateException;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.OrderItemRepository;
import com.clinic.modules.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for order processing with cart validation and order creation.
 * Handles order lifecycle management and customer validation.
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final DateTimeFormatter ORDER_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final ProductService productService;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                       OrderItemRepository orderItemRepository,
                       CartService cartService,
                       ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.productService = productService;
    }

    /**
     * Create order from cart contents with customer information validation.
     */
    public OrderEntity createOrderFromCart(TenantEntity tenant, CreateOrderRequest request) {
        logger.debug("Creating order from cart for tenant {} and session {}", 
                    tenant.getId(), request.getSessionId());

        // Validate cart exists and has items
        Optional<CartEntity> cartOpt = cartService.getCart(tenant, request.getSessionId());
        if (cartOpt.isEmpty()) {
            throw new InvalidCartStateException("Cart not found for session: " + request.getSessionId());
        }

        CartEntity cart = cartOpt.get();
        if (cart.isEmpty()) {
            throw new InvalidCartStateException("Cannot create order from empty cart");
        }

        // Validate cart item availability
        List<CartItemEntity> unavailableItems = cartService.validateCartAvailability(tenant, request.getSessionId());
        if (!unavailableItems.isEmpty()) {
            String unavailableProducts = unavailableItems.stream()
                .map(item -> item.getDisplayName())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Unknown products");
            throw new InvalidCartStateException("Some items are no longer available: " + unavailableProducts);
        }

        // Generate unique order number
        String orderNumber = generateOrderNumber(tenant);

        // Create order entity
        OrderEntity order = new OrderEntity(tenant, orderNumber);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setBillingAddressLine1(request.getBillingAddressLine1());
        order.setBillingAddressLine2(request.getBillingAddressLine2());
        order.setBillingAddressCity(request.getBillingAddressCity());
        order.setBillingAddressState(request.getBillingAddressState());
        order.setBillingAddressPostalCode(request.getBillingAddressPostalCode());
        order.setBillingAddressCountry(request.getBillingAddressCountry());
        order.setNotes(request.getNotes());
        order.setCurrency(cart.getCurrency());
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        // Copy cart totals to order
        order.setSubtotal(cart.getSubtotal());
        order.setTaxAmount(cart.getTaxAmount());
        order.setTotalAmount(cart.getTotalAmount());

        // Save order first to get ID
        OrderEntity savedOrder = orderRepository.save(order);

        // Create order items from cart items
        for (CartItemEntity cartItem : cart.getItems()) {
            OrderItemEntity orderItem = OrderItemEntity.fromCartItem(savedOrder, cartItem);
            orderItemRepository.save(orderItem);
            savedOrder.addItem(orderItem);
        }

        // Recalculate totals and save
        savedOrder.calculateTotals();
        OrderEntity finalOrder = orderRepository.save(savedOrder);

        logger.info("Created order {} from cart {} for tenant {}", 
                   finalOrder.getOrderNumber(), cart.getId(), tenant.getId());

        return finalOrder;
    }

    /**
     * Create order directly from product (Buy Now functionality).
     * Bypasses cart and creates order immediately with single product.
     */
    public OrderEntity createDirectOrder(TenantEntity tenant, BuyNowRequest request) {
        logger.debug("Creating direct order for product {} and tenant {}", 
                    request.getProductId(), tenant.getId());

        // Validate product exists and is available
        ProductEntity product = productService.getProductById(tenant, request.getProductId());
        if (!product.isActive()) {
            throw new ProductNotFoundException("Product is not available: " + product.getName());
        }

        // Validate variant if specified
        ProductVariantEntity variant = null;
        if (request.getVariantId() != null) {
            variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(request.getVariantId()) && v.isActive())
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product variant not found or inactive: " + request.getVariantId()));
        }

        // Check stock availability
        int availableStock = variant != null ? variant.getStockQuantity() : product.getStockQuantity();
        if (availableStock < request.getQuantity()) {
            throw new InvalidCartStateException("Insufficient stock. Available: " + availableStock + ", Requested: " + request.getQuantity());
        }

        // Generate unique order number
        String orderNumber = generateOrderNumber(tenant);

        // Create order entity
        OrderEntity order = new OrderEntity(tenant, orderNumber);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setBillingAddressLine1(request.getBillingAddressLine1());
        order.setBillingAddressLine2(request.getBillingAddressLine2());
        order.setBillingAddressCity(request.getBillingAddressCity());
        order.setBillingAddressState(request.getBillingAddressState());
        order.setBillingAddressPostalCode(request.getBillingAddressPostalCode());
        order.setBillingAddressCountry(request.getBillingAddressCountry());
        order.setNotes(request.getNotes());
        order.setCurrency("USD"); // Default currency, could be made configurable
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        // Save order first to get ID
        OrderEntity savedOrder = orderRepository.save(order);

        // Create single order item
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(savedOrder);
        orderItem.setProduct(product);
        orderItem.setVariant(variant);
        orderItem.setQuantity(request.getQuantity());
        
        // Set pricing
        java.math.BigDecimal unitPrice = variant != null ? variant.getPrice() : product.getPrice();
        orderItem.setUnitPrice(unitPrice);
        orderItem.setTotalPrice(unitPrice.multiply(java.math.BigDecimal.valueOf(request.getQuantity())));
        
        // Set display information
        orderItem.setProductName(product.getName());
        orderItem.setProductSku(variant != null ? variant.getSku() : product.getSku());
        if (variant != null) {
            orderItem.setVariantName(variant.getName());
        }

        orderItemRepository.save(orderItem);
        savedOrder.addItem(orderItem);

        // Calculate totals and save
        savedOrder.calculateTotals();
        OrderEntity finalOrder = orderRepository.save(savedOrder);

        logger.info("Created direct order {} for product {} and tenant {}", 
                   finalOrder.getOrderNumber(), product.getName(), tenant.getId());

        return finalOrder;
    }

    /**
     * Get order by ID with tenant validation.
     */
    @Transactional(readOnly = true)
    public Optional<OrderEntity> getOrder(TenantEntity tenant, Long orderId) {
        return orderRepository.findByIdAndTenant(orderId, tenant.getId());
    }

    /**
     * Get order by order number with tenant validation.
     */
    @Transactional(readOnly = true)
    public Optional<OrderEntity> getOrderByNumber(TenantEntity tenant, String orderNumber) {
        return orderRepository.findByOrderNumberAndTenant(orderNumber, tenant.getId());
    }

    /**
     * Get orders for tenant with pagination.
     */
    @Transactional(readOnly = true)
    public Page<OrderEntity> getOrders(TenantEntity tenant, Pageable pageable) {
        return orderRepository.findByTenant(tenant.getId(), pageable);
    }

    /**
     * Get orders by status for tenant.
     */
    @Transactional(readOnly = true)
    public Page<OrderEntity> getOrdersByStatus(TenantEntity tenant, OrderStatus status, Pageable pageable) {
        return orderRepository.findByTenantAndStatus(tenant.getId(), status, pageable);
    }

    /**
     * Get orders by customer email for tenant.
     */
    @Transactional(readOnly = true)
    public Page<OrderEntity> getOrdersByCustomerEmail(TenantEntity tenant, String email, Pageable pageable) {
        return orderRepository.findByTenantAndCustomerEmail(tenant.getId(), email, pageable);
    }

    /**
     * Search orders by customer name or email.
     */
    @Transactional(readOnly = true)
    public Page<OrderEntity> searchOrdersByCustomer(TenantEntity tenant, String searchTerm, Pageable pageable) {
        return orderRepository.searchOrdersByCustomer(tenant.getId(), searchTerm, pageable);
    }

    /**
     * Get order by id with tenant isolation.
     */
    @Transactional(readOnly = true)
    public OrderEntity getOrderById(TenantEntity tenant, Long orderId) {
        return orderRepository.findByIdAndTenant(orderId, tenant.getId())
            .orElseThrow(() -> new ProductNotFoundException("Order not found: " + orderId));
    }

    /**
     * Update order status.
     */
    public OrderEntity updateOrderStatus(TenantEntity tenant, Long orderId, OrderStatus newStatus) {
        logger.debug("Updating order {} status to {} for tenant {}", orderId, newStatus, tenant.getId());

        OrderEntity order = orderRepository.findByIdAndTenant(orderId, tenant.getId())
            .orElseThrow(() -> new ProductNotFoundException("Order not found: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        OrderEntity savedOrder = orderRepository.save(order);

        logger.info("Updated order {} status from {} to {} for tenant {}", 
                   order.getOrderNumber(), oldStatus, newStatus, tenant.getId());

        return savedOrder;
    }

    /**
     * Update order fields (admin only).
     */
    public OrderEntity updateOrder(TenantEntity tenant, Long orderId, com.clinic.modules.ecommerce.dto.AdminUpdateOrderRequest request) {
        logger.debug("Updating order {} for tenant {}", orderId, tenant.getId());

        OrderEntity order = orderRepository.findByIdAndTenant(orderId, tenant.getId())
            .orElseThrow(() -> new ProductNotFoundException("Order not found: " + orderId));

        if (request.getCustomerName() != null) order.setCustomerName(request.getCustomerName());
        if (request.getCustomerEmail() != null) order.setCustomerEmail(request.getCustomerEmail());
        if (request.getCustomerPhone() != null) order.setCustomerPhone(request.getCustomerPhone());
        if (request.getBillingAddressLine1() != null) order.setBillingAddressLine1(request.getBillingAddressLine1());
        if (request.getBillingAddressLine2() != null) order.setBillingAddressLine2(request.getBillingAddressLine2());
        if (request.getBillingAddressCity() != null) order.setBillingAddressCity(request.getBillingAddressCity());
        if (request.getBillingAddressState() != null) order.setBillingAddressState(request.getBillingAddressState());
        if (request.getBillingAddressPostalCode() != null) order.setBillingAddressPostalCode(request.getBillingAddressPostalCode());
        if (request.getBillingAddressCountry() != null) order.setBillingAddressCountry(request.getBillingAddressCountry());
        if (request.getStatus() != null) order.setStatus(request.getStatus());
        if (request.getSubtotal() != null) order.setSubtotal(request.getSubtotal());
        if (request.getTaxAmount() != null) order.setTaxAmount(request.getTaxAmount());
        if (request.getShippingAmount() != null) order.setShippingAmount(request.getShippingAmount());
        if (request.getTotalAmount() != null) order.setTotalAmount(request.getTotalAmount());
        if (request.getCurrency() != null) order.setCurrency(request.getCurrency());
        if (request.getNotes() != null) order.setNotes(request.getNotes());

        OrderEntity savedOrder = orderRepository.save(order);
        logger.info("Updated order {} for tenant {}", savedOrder.getOrderNumber(), tenant.getId());
        return savedOrder;
    }

    /**
     * Cancel order if possible.
     */
    public OrderEntity cancelOrder(TenantEntity tenant, Long orderId, String reason) {
        logger.debug("Cancelling order {} for tenant {}", orderId, tenant.getId());

        OrderEntity order = orderRepository.findByIdAndTenant(orderId, tenant.getId())
            .orElseThrow(() -> new ProductNotFoundException("Order not found: " + orderId));

        if (!order.canBeCancelled()) {
            throw new InvalidCartStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            String currentNotes = order.getNotes();
            String cancellationNote = "Cancelled: " + reason;
            order.setNotes(currentNotes != null ? currentNotes + "\n" + cancellationNote : cancellationNote);
        }

        OrderEntity savedOrder = orderRepository.save(order);

        logger.info("Cancelled order {} for tenant {}: {}", 
                   order.getOrderNumber(), tenant.getId(), reason);

        return savedOrder;
    }

    /**
     * Get order statistics for tenant.
     */
    @Transactional(readOnly = true)
    public OrderStatistics getOrderStatistics(TenantEntity tenant) {
        Object[] stats = orderRepository.getOrderStatisticsByTenant(tenant.getId());
        
        Long totalOrders = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
        java.math.BigDecimal totalRevenue = (java.math.BigDecimal) stats[1];
        java.math.BigDecimal averageOrderValue = (java.math.BigDecimal) stats[2];

        // Get status breakdown
        List<Object[]> statusStats = orderRepository.getOrderStatisticsByStatusAndTenant(tenant.getId());
        
        return new OrderStatistics(totalOrders, totalRevenue, averageOrderValue, statusStats);
    }

    /**
     * Get recent orders for tenant.
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> getRecentOrders(TenantEntity tenant, int limit) {
        return orderRepository.findRecentOrdersByTenant(tenant.getId(), 
            org.springframework.data.domain.PageRequest.of(0, limit));
    }

    /**
     * Clear cart after successful order creation.
     */
    public void clearCartAfterOrder(TenantEntity tenant, String sessionId) {
        logger.debug("Clearing cart after order creation for tenant {} and session {}", 
                    tenant.getId(), sessionId);
        
        try {
            cartService.clearCart(tenant, sessionId);
            logger.info("Cleared cart for session {} after order creation", sessionId);
        } catch (Exception e) {
            logger.warn("Failed to clear cart for session {} after order creation: {}", 
                       sessionId, e.getMessage());
            // Don't fail the order creation if cart clearing fails
        }
    }

    /**
     * Generate unique order number for tenant.
     */
    private String generateOrderNumber(TenantEntity tenant) {
        String datePrefix = LocalDateTime.now().format(ORDER_NUMBER_FORMAT);
        String tenantPrefix = String.format("%04d", tenant.getId() % 10000);
        
        int attempts = 0;
        while (attempts < 10) {
            int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999);
            String orderNumber = tenantPrefix + "-" + datePrefix + "-" + randomSuffix;
            
            if (!orderRepository.existsByTenantAndOrderNumber(tenant.getId(), orderNumber)) {
                return orderNumber;
            }
            attempts++;
        }
        
        // Fallback with timestamp if all attempts fail
        long timestamp = System.currentTimeMillis() % 100000;
        return tenantPrefix + "-" + datePrefix + "-" + timestamp;
    }

    /**
     * Order statistics data class.
     */
    public static class OrderStatistics {
        private final Long totalOrders;
        private final java.math.BigDecimal totalRevenue;
        private final java.math.BigDecimal averageOrderValue;
        private final List<Object[]> statusBreakdown;

        public OrderStatistics(Long totalOrders, java.math.BigDecimal totalRevenue, 
                             java.math.BigDecimal averageOrderValue, List<Object[]> statusBreakdown) {
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue != null ? totalRevenue : java.math.BigDecimal.ZERO;
            this.averageOrderValue = averageOrderValue != null ? averageOrderValue : java.math.BigDecimal.ZERO;
            this.statusBreakdown = statusBreakdown;
        }

        public Long getTotalOrders() { return totalOrders; }
        public java.math.BigDecimal getTotalRevenue() { return totalRevenue; }
        public java.math.BigDecimal getAverageOrderValue() { return averageOrderValue; }
        public List<Object[]> getStatusBreakdown() { return statusBreakdown; }
    }
}
