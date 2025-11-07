package com.clinic.modules.core.treatment;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single follow-up visit within a treatment plan.
 * Records what happened during the visit, payments made, and materials used.
 */
@Entity
@Table(name = "followup_visits")
public class FollowUpVisitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "treatment_plan_id")
    private TreatmentPlanEntity treatmentPlan;

    @Column(name = "visit_number", nullable = false)
    private Integer visitNumber;

    @Column(name = "visit_date", nullable = false)
    private Instant visitDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "performed_procedures", columnDefinition = "TEXT")
    private String performedProcedures;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentEntity> payments = new ArrayList<>();

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialUsageEntity> materialUsages = new ArrayList<>();

    protected FollowUpVisitEntity() {
    }

    public FollowUpVisitEntity(
            TreatmentPlanEntity treatmentPlan,
            Integer visitNumber,
            Instant visitDate,
            String notes,
            String performedProcedures
    ) {
        this.treatmentPlan = treatmentPlan;
        this.visitNumber = visitNumber;
        this.visitDate = visitDate;
        this.notes = notes;
        this.performedProcedures = performedProcedures;
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

    public void updateDetails(Instant visitDate, String notes, String performedProcedures) {
        this.visitDate = visitDate;
        this.notes = notes;
        this.performedProcedures = performedProcedures;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public TreatmentPlanEntity getTreatmentPlan() {
        return treatmentPlan;
    }

    public Integer getVisitNumber() {
        return visitNumber;
    }

    public Instant getVisitDate() {
        return visitDate;
    }

    public String getNotes() {
        return notes;
    }

    public String getPerformedProcedures() {
        return performedProcedures;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<PaymentEntity> getPayments() {
        return payments;
    }

    public List<MaterialUsageEntity> getMaterialUsages() {
        return materialUsages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowUpVisitEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
