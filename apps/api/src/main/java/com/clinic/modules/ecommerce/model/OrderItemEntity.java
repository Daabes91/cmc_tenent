package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order item entity representing individual products within an order.
 * Stores snapshot of product information at time of order to preserve historical data.
 */
@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull(message = "Tenant is required")
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariantEntity variant;

    @Column(name = "product_name", nullable = false)
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String productName;

    @Column(name = "variant_name")
    @Size(max = 255, message = "Variant name must not exceed 255 characters")
    private String variantName;

    @Column(name = "sku", length = 100)
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @Column(name = "quantity", nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price must be non-negative")
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", message = "Total price must be non-negative")
    private BigDecimal totalPrice;

    @Column(name = "currency", length = 3)
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency = "USD";

    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;

    // Constructors
    public OrderItemEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public OrderItemEntity(OrderEntity order, ProductEntity product, Integer quantity, BigDecimal unitPrice) {
        this();
        this.order = order;
        this.tenant = order.getTenant();
        this.product = product;
        this.productName = product.getName();
        this.sku = product.getSku();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.currency = product.getCurrency();
        calculateTotalPrice();
    }

    public OrderItemEntity(OrderEntity order, ProductEntity product, ProductVariantEntity variant, 
                          Integer quantity, BigDecimal unitPrice) {
        this(order, product, quantity, unitPrice);
        this.variant = variant;
        this.variantName = variant.getName();
        this.sku = variant.getSku();
    }

    // Lifecycle callbacks
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        calculateTotalPrice();
    }

    // Business methods
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public String getDisplayName() {
        if (variantName != null && !variantName.trim().isEmpty()) {
            return productName + " - " + variantName;
        }
        return productName;
    }

    public String getEffectiveSku() {
        return sku != null ? sku : "N/A";
    }

    /**
     * Create order item from cart item, capturing current product state.
     */
    public static OrderItemEntity fromCartItem(OrderEntity order, CartItemEntity cartItem) {
        ProductEntity product = cartItem.getProduct();
        ProductVariantEntity variant = cartItem.getVariant();
        
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(order);
        orderItem.setTenant(order.getTenant());
        orderItem.setProduct(product);
        orderItem.setVariant(variant);
        
        // Capture product information at time of order
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setUnitPrice(cartItem.getUnitPrice());
        orderItem.setCurrency(cartItem.getCurrency());
        
        if (variant != null) {
            orderItem.setVariantName(variant.getName());
            orderItem.setSku(variant.getSku());
        } else {
            orderItem.setSku(product.getSku());
        }
        
        orderItem.calculateTotalPrice();
        
        return orderItem;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
        if (order != null) {
            this.tenant = order.getTenant();
        }
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public ProductVariantEntity getVariant() {
        return variant;
    }

    public void setVariant(ProductVariantEntity variant) {
        this.variant = variant;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItemEntity)) return false;
        OrderItemEntity that = (OrderItemEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OrderItemEntity{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", variantName='" + variantName + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", currency='" + currency + '\'' +
                '}';
    }
}