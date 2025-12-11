package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA entity representing a product variant in the e-commerce system.
 * 
 * Product variants allow for different variations of a product (size, color, etc.)
 * Each variant has its own SKU, price, and stock information.
 */
@Entity
@Table(name = "product_variants")
public class ProductVariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "tenant_id", insertable = false, updatable = false)
    private Long tenantId;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String sku;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Size(max = 3)
    @Column(length = 3)
    private String currency = "USD";

    @Min(0)
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "is_in_stock")
    private Boolean isInStock = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductVariantEntity() {
    }

    public ProductVariantEntity(ProductEntity product, TenantEntity tenant, String sku, String name, BigDecimal price) {
        this.product = product;
        this.tenant = tenant;
        this.tenantId = tenant.getId();
        this.sku = sku;
        this.name = name;
        this.price = price;
    }

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        
        // Ensure tenant ID is set
        if (tenant != null && tenantId == null) {
            this.tenantId = tenant.getId();
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
        this.tenantId = tenant != null ? tenant.getId() : null;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCompareAtPrice() {
        return compareAtPrice;
    }

    public void setCompareAtPrice(BigDecimal compareAtPrice) {
        this.compareAtPrice = compareAtPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        // Update in-stock status based on quantity
        this.isInStock = stockQuantity != null && stockQuantity > 0;
    }

    public Boolean getIsInStock() {
        return isInStock;
    }

    public void setIsInStock(Boolean isInStock) {
        this.isInStock = isInStock;
    }

    public boolean isInStock() {
        return Boolean.TRUE.equals(isInStock) && (stockQuantity == null || stockQuantity > 0);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Helper methods
    public boolean hasStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean canFulfillQuantity(int requestedQuantity) {
        return isInStock() && hasStock() && stockQuantity >= requestedQuantity;
    }

    public void decreaseStock(int quantity) {
        if (stockQuantity != null && stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
            this.isInStock = this.stockQuantity > 0;
        } else {
            throw new IllegalArgumentException("Insufficient stock. Available: " + stockQuantity + ", Requested: " + quantity);
        }
    }

    public void increaseStock(int quantity) {
        if (stockQuantity == null) {
            this.stockQuantity = quantity;
        } else {
            this.stockQuantity += quantity;
        }
        this.isInStock = this.stockQuantity > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariantEntity)) return false;
        ProductVariantEntity that = (ProductVariantEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProductVariantEntity{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}