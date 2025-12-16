package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

/**
 * JPA entity representing the many-to-many relationship between products and categories.
 * 
 * This junction table allows products to be associated with multiple categories
 * while maintaining tenant isolation.
 */
@Entity
@Table(name = "product_categories",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_product_categories_product_category",
           columnNames = {"product_id", "category_id"}
       ))
public class ProductCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "tenant_id", insertable = false, updatable = false)
    private Long tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProductCategoryEntity() {
    }

    public ProductCategoryEntity(ProductEntity product, CategoryEntity category, TenantEntity tenant) {
        this.product = product;
        this.category = category;
        this.tenant = tenant;
        this.tenantId = tenant.getId();
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        
        // Ensure tenant ID is set
        if (tenant != null && tenantId == null) {
            this.tenantId = tenant.getId();
        }
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

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCategoryEntity)) return false;
        ProductCategoryEntity that = (ProductCategoryEntity) o;
        
        // Use business key equality: same product, category, and tenant
        return product != null && category != null && tenantId != null &&
               product.getId() != null && category.getId() != null &&
               product.getId().equals(that.product != null ? that.product.getId() : null) &&
               category.getId().equals(that.category != null ? that.category.getId() : null) &&
               tenantId.equals(that.tenantId);
    }

    @Override
    public int hashCode() {
        // Use business key for hash code
        return Objects.hash(
            product != null ? product.getId() : null,
            category != null ? category.getId() : null,
            tenantId
        );
    }

    @Override
    public String toString() {
        return "ProductCategoryEntity{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", productId=" + (product != null ? product.getId() : null) +
                ", categoryId=" + (category != null ? category.getId() : null) +
                '}';
    }
}