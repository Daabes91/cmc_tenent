package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a product in the e-commerce system.
 * 
 * Products are tenant-scoped and support both simple and variable types.
 * Each product can have multiple variants, images, and category associations.
 */
@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "tenant_id", insertable = false, updatable = false)
    private Long tenantId;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Column(nullable = false, length = 255)
    private String slug;

    @Size(max = 100)
    @Column(length = 100)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 500)
    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType = ProductType.SIMPLE;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Size(max = 3)
    @Column(length = 3)
    private String currency = "USD";

    @Column(name = "has_variants")
    private Boolean hasVariants = false;

    @Column(name = "is_taxable")
    private Boolean isTaxable = true;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantEntity> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImageEntity> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategoryEntity> productCategories = new ArrayList<>();

    protected ProductEntity() {
    }

    public ProductEntity(TenantEntity tenant, String name, String slug) {
        this.tenant = tenant;
        this.tenantId = tenant.getId();
        this.name = name;
        this.slug = slug;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
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

    public Boolean getHasVariants() {
        return hasVariants;
    }

    public void setHasVariants(Boolean hasVariants) {
        this.hasVariants = hasVariants;
    }

    public boolean hasVariants() {
        return Boolean.TRUE.equals(hasVariants);
    }

    public Boolean getIsTaxable() {
        return isTaxable;
    }

    public void setIsTaxable(Boolean isTaxable) {
        this.isTaxable = isTaxable;
    }

    public boolean isTaxable() {
        return Boolean.TRUE.equals(isTaxable);
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return Boolean.TRUE.equals(isVisible);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<ProductVariantEntity> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantEntity> variants) {
        this.variants = variants;
    }

    public List<ProductImageEntity> getImages() {
        return images;
    }

    public void setImages(List<ProductImageEntity> images) {
        this.images = images;
    }

    public List<ProductCategoryEntity> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(List<ProductCategoryEntity> productCategories) {
        this.productCategories = productCategories;
    }

    // Helper methods
    public void addVariant(ProductVariantEntity variant) {
        variants.add(variant);
        variant.setProduct(this);
        variant.setTenant(this.tenant);
    }

    public void removeVariant(ProductVariantEntity variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    public void addImage(ProductImageEntity image) {
        images.add(image);
        image.setProduct(this);
        image.setTenant(this.tenant);
    }

    public void removeImage(ProductImageEntity image) {
        images.remove(image);
        image.setProduct(null);
    }

    public void addCategory(CategoryEntity category) {
        ProductCategoryEntity productCategory = new ProductCategoryEntity(this, category, this.tenant);
        productCategories.add(productCategory);
    }

    public void removeCategory(CategoryEntity category) {
        productCategories.removeIf(pc -> pc.getCategory().equals(category));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductEntity)) return false;
        ProductEntity that = (ProductEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", status=" + status +
                '}';
    }
}