package com.clinic.modules.core.treatment;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

/**
 * Audit log for tracking changes to treatment plans.
 * Records who changed what and when for accountability.
 */
@Entity
@Table(name = "treatment_plan_audit_log")
public class TreatmentPlanAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "treatment_plan_id")
    private TreatmentPlanEntity treatmentPlan;

    @Column(name = "changed_by_staff_id")
    private Long changedByStaffId;

    @Column(name = "changed_by_name")
    private String changedByName;

    @Column(name = "change_type", nullable = false, length = 100)
    private String changeType; // e.g., "PRICE_UPDATE", "FOLLOWUPS_UPDATE", "DISCOUNT_APPLIED", "STATUS_CHANGE"

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    protected TreatmentPlanAuditLogEntity() {
    }

    public TreatmentPlanAuditLogEntity(
            TreatmentPlanEntity treatmentPlan,
            Long changedByStaffId,
            String changedByName,
            String changeType,
            String oldValue,
            String newValue,
            String notes
    ) {
        this.treatmentPlan = treatmentPlan;
        this.changedByStaffId = changedByStaffId;
        this.changedByName = changedByName;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.notes = notes;
    }

    @PrePersist
    public void onCreate() {
        this.changedAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public TreatmentPlanEntity getTreatmentPlan() {
        return treatmentPlan;
    }

    public Long getChangedByStaffId() {
        return changedByStaffId;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreatmentPlanAuditLogEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
