package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a product category in the e-commerce system.
 * 
 * Categories support hierarchical structure with parent-child relationships.
 * Each category is tenant-scoped and can have multiple products associated.
 */
@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "tenant_id", insertable = false, updatable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Column(nullable = false, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(0)
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryEntity> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategoryEntity> productCategories = new ArrayList<>();

    protected CategoryEntity() {
    }

    public CategoryEntity(TenantEntity tenant, String name, String slug) {
        this.tenant = tenant;
        this.tenantId = tenant.getId();
        this.name = name;
        this.slug = slug;
    }

    public CategoryEntity(TenantEntity tenant, CategoryEntity parent, String name, String slug) {
        this(tenant, name, slug);
        this.parent = parent;
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

    public CategoryEntity getParent() {
        return parent;
    }

    public void setParent(CategoryEntity parent) {
        this.parent = parent;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<CategoryEntity> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryEntity> children) {
        this.children = children;
    }

    public List<ProductCategoryEntity> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(List<ProductCategoryEntity> productCategories) {
        this.productCategories = productCategories;
    }

    // Helper methods
    public boolean isRootCategory() {
        return parent == null;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public boolean hasProducts() {
        return productCategories != null && !productCategories.isEmpty();
    }

    public void addChild(CategoryEntity child) {
        children.add(child);
        child.setParent(this);
        child.setTenant(this.tenant);
    }

    public void removeChild(CategoryEntity child) {
        children.remove(child);
        child.setParent(null);
    }

    public void addProduct(ProductEntity product) {
        ProductCategoryEntity productCategory = new ProductCategoryEntity(product, this, this.tenant);
        productCategories.add(productCategory);
    }

    public void removeProduct(ProductEntity product) {
        productCategories.removeIf(pc -> pc.getProduct().equals(product));
    }

    /**
     * Gets the full path of this category including all parent categories.
     * Example: "Electronics > Computers > Laptops"
     */
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    /**
     * Gets the depth level of this category in the hierarchy.
     * Root categories have depth 0.
     */
    public int getDepth() {
        if (parent == null) {
            return 0;
        }
        return parent.getDepth() + 1;
    }

    /**
     * Gets all ancestor categories up to the root.
     */
    public List<CategoryEntity> getAncestors() {
        List<CategoryEntity> ancestors = new ArrayList<>();
        CategoryEntity current = this.parent;
        while (current != null) {
            ancestors.add(0, current); // Add at beginning to maintain order
            current = current.getParent();
        }
        return ancestors;
    }

    /**
     * Gets all descendant categories recursively.
     */
    public List<CategoryEntity> getAllDescendants() {
        List<CategoryEntity> descendants = new ArrayList<>();
        for (CategoryEntity child : children) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }

    /**
     * Checks if this category is an ancestor of the given category.
     */
    public boolean isAncestorOf(CategoryEntity category) {
        CategoryEntity current = category.getParent();
        while (current != null) {
            if (current.equals(this)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * Checks if this category is a descendant of the given category.
     */
    public boolean isDescendantOf(CategoryEntity category) {
        return category.isAncestorOf(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryEntity)) return false;
        CategoryEntity that = (CategoryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", parentId=" + (parent != null ? parent.getId() : null) +
                ", isActive=" + isActive +
                '}';
    }
}