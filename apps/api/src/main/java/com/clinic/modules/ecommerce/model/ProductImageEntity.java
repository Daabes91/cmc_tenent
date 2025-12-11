package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.Instant;

/**
 * JPA entity representing a product image in the e-commerce system.
 * 
 * Product images support ordering and main image designation.
 * Each image is associated with a specific product and tenant.
 */
@Entity
@Table(name = "product_images")
public class ProductImageEntity {

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
    @Size(max = 500)
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "alt_text", length = 255)
    private String altText;

    @Min(0)
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_main")
    private Boolean isMain = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductImageEntity() {
    }

    public ProductImageEntity(ProductEntity product, TenantEntity tenant, String imageUrl) {
        this.product = product;
        this.tenant = tenant;
        this.tenantId = tenant.getId();
        this.imageUrl = imageUrl;
    }

    public ProductImageEntity(ProductEntity product, TenantEntity tenant, String imageUrl, String altText) {
        this(product, tenant, imageUrl);
        this.altText = altText;
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
        
        // Set default sort order if not specified
        if (sortOrder == null) {
            sortOrder = 0;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }

    public boolean isMain() {
        return Boolean.TRUE.equals(isMain);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Helper methods
    public void markAsMain() {
        this.isMain = true;
    }

    public void unmarkAsMain() {
        this.isMain = false;
    }

    /**
     * Generates alt text based on product name if not explicitly set.
     */
    public String getEffectiveAltText() {
        if (altText != null && !altText.trim().isEmpty()) {
            return altText;
        }
        
        if (product != null && product.getName() != null) {
            String suffix = isMain() ? " - Main Image" : " - Image " + (sortOrder + 1);
            return product.getName() + suffix;
        }
        
        return "Product Image";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductImageEntity)) return false;
        ProductImageEntity that = (ProductImageEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProductImageEntity{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", imageUrl='" + imageUrl + '\'' +
                ", sortOrder=" + sortOrder +
                ", isMain=" + isMain +
                '}';
    }
}