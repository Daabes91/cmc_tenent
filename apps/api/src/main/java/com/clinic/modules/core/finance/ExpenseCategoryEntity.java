package com.clinic.modules.core.finance;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing an expense category for clinic financial tracking.
 * Categories can be system-seeded defaults or custom clinic-created categories.
 */
@Entity
@Table(name = "expense_categories")
public class ExpenseCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull(message = "is_system flag is required")
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem;

    @NotNull(message = "is_active flag is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Default constructor for JPA.
     */
    protected ExpenseCategoryEntity() {
    }

    /**
     * Constructor for creating a new expense category.
     *
     * @param name     the category name
     * @param isSystem whether this is a system-seeded category
     * @param isActive whether this category is active
     */
    public ExpenseCategoryEntity(String name, Boolean isSystem, Boolean isActive) {
        this.name = name;
        this.isSystem = isSystem != null ? isSystem : false;
        this.isActive = isActive != null ? isActive : true;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Updates the category name.
     *
     * @param name the new category name
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * Toggles the active status of the category.
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseCategoryEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ExpenseCategoryEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isSystem=" + isSystem +
                ", isActive=" + isActive +
                '}';
    }
}
