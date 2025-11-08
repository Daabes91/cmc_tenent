package com.clinic.modules.core.treatment;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Simple catalog of materials/supplies used in treatments.
 * Stores the name and unit cost for tracking COGS.
 */
@Entity
@Table(
        name = "material_catalog",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_material_catalog_tenant_name",
                        columnNames = {"tenant_id", "name"}
                )
        }
)
public class MaterialCatalogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "unit_of_measure", length = 50)
    private String unitOfMeasure; // e.g., "piece", "ml", "g", "box"

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MaterialCatalogEntity() {
    }

    public MaterialCatalogEntity(String name, String description, BigDecimal unitCost, String currency, String unitOfMeasure) {
        this.name = name;
        this.description = description;
        this.unitCost = unitCost;
        this.currency = currency != null ? currency : "USD";
        this.unitOfMeasure = unitOfMeasure;
        this.active = true;
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

    public void updateDetails(String name, String description, BigDecimal unitCost, String currency, String unitOfMeasure) {
        this.name = name;
        this.description = description;
        this.unitCost = unitCost;
        this.currency = currency != null ? currency : "USD";
        this.unitOfMeasure = unitOfMeasure;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public String getCurrency() {
        return currency;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public Boolean getActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaterialCatalogEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
