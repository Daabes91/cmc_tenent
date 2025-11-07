package com.clinic.modules.core.treatment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Records materials used during a specific follow-up visit.
 * Tracks quantity and cost for COGS calculation.
 */
@Entity
@Table(name = "material_usage")
public class MaterialUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "visit_id")
    private FollowUpVisitEntity visit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "material_id")
    private MaterialCatalogEntity material;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost; // Snapshot of cost at time of use

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MaterialUsageEntity() {
    }

    public MaterialUsageEntity(
            FollowUpVisitEntity visit,
            MaterialCatalogEntity material,
            BigDecimal quantity,
            String notes
    ) {
        this.visit = visit;
        this.material = material;
        this.quantity = quantity;
        this.unitCost = material.getUnitCost(); // Snapshot current cost
        this.notes = notes;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    /**
     * Calculate total cost for this material usage.
     */
    public BigDecimal calculateTotalCost() {
        return unitCost.multiply(quantity);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public FollowUpVisitEntity getVisit() {
        return visit;
    }

    public MaterialCatalogEntity getMaterial() {
        return material;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaterialUsageEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
