package com.clinic.modules.core.treatment;

import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.service.ClinicServiceEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a treatment plan for a specific patient.
 * Each plan is unique to a patient and stores custom pricing, follow-ups, and progress.
 */
@Entity
@Table(name = "treatment_plans")
public class TreatmentPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private PatientEntity patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id")
    private DoctorEntity doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "treatment_type_id")
    private ClinicServiceEntity treatmentType;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "JOD";

    @Column(name = "planned_followups", nullable = false)
    private Integer plannedFollowups;

    @Column(name = "completed_visits", nullable = false)
    private Integer completedVisits = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TreatmentPlanStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "followup_cadence", nullable = false, length = 16)
    private FollowUpCadence followUpCadence = FollowUpCadence.defaultCadence();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "discount_reason")
    private String discountReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "treatmentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FollowUpVisitEntity> followUpVisits = new ArrayList<>();

    @OneToMany(mappedBy = "treatmentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TreatmentPlanAuditLogEntity> auditLogs = new ArrayList<>();

    @OneToMany(mappedBy = "treatmentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TreatmentPlanPaymentEntity> directPayments = new ArrayList<>();

    protected TreatmentPlanEntity() {
    }

    public TreatmentPlanEntity(
            PatientEntity patient,
            DoctorEntity doctor,
            ClinicServiceEntity treatmentType,
            BigDecimal totalPrice,
            String currency,
            Integer plannedFollowups,
            FollowUpCadence followUpCadence,
            String notes
    ) {
        this.patient = patient;
        this.doctor = doctor;
        this.treatmentType = treatmentType;
        this.totalPrice = totalPrice;
        this.currency = currency;
        this.plannedFollowups = plannedFollowups;
        this.followUpCadence = followUpCadence == null ? FollowUpCadence.defaultCadence() : followUpCadence;
        this.notes = notes;
        this.status = TreatmentPlanStatus.PLANNED;
        this.completedVisits = 0;
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

    @PostLoad
    public void ensureCadence() {
        if (this.followUpCadence == null) {
            this.followUpCadence = FollowUpCadence.defaultCadence();
        }
    }

    public void updatePlan(BigDecimal totalPrice, Integer plannedFollowups, FollowUpCadence followUpCadence, String notes) {
        this.totalPrice = totalPrice;
        this.plannedFollowups = plannedFollowups;
        this.followUpCadence = followUpCadence == null ? FollowUpCadence.defaultCadence() : followUpCadence;
        this.notes = notes;
    }

    public void applyDiscount(BigDecimal discountAmount, String reason) {
        this.discountAmount = discountAmount;
        this.discountReason = reason;
    }

    public void start() {
        if (this.status == TreatmentPlanStatus.PLANNED) {
            this.status = TreatmentPlanStatus.IN_PROGRESS;
            this.startedAt = Instant.now();
        }
    }

    public void complete() {
        this.status = TreatmentPlanStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel(String reason) {
        this.status = TreatmentPlanStatus.CANCELLED;
        this.notes = (this.notes != null ? this.notes + "\n\n" : "") + "Cancelled: " + reason;
    }

    public void incrementCompletedVisits() {
        this.completedVisits++;
        if (this.status == TreatmentPlanStatus.PLANNED) {
            start();
        }
    }

    /**
     * Calculate total amount paid so far.
     */
    public BigDecimal calculateTotalPaid() {
        // Calculate payments from visits
        BigDecimal visitPayments = followUpVisits.stream()
                .flatMap(visit -> visit.getPayments().stream())
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate direct payments
        BigDecimal directPaymentTotal = directPayments.stream()
                .map(TreatmentPlanPaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return visitPayments.add(directPaymentTotal);
    }

    /**
     * Calculate remaining balance.
     */
    public BigDecimal calculateRemainingBalance() {
        BigDecimal effectiveTotal = totalPrice;
        if (discountAmount != null) {
            effectiveTotal = effectiveTotal.subtract(discountAmount);
        }
        return effectiveTotal.subtract(calculateTotalPaid());
    }

    /**
     * Calculate remaining visits.
     */
    public Integer calculateRemainingVisits() {
        return Math.max(0, plannedFollowups - completedVisits);
    }

    /**
     * Calculate expected payment per remaining visit.
     */
    public BigDecimal calculateExpectedPaymentPerVisit() {
        Integer remainingVisits = calculateRemainingVisits();
        if (remainingVisits == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = calculateRemainingBalance();
        return remaining.divide(BigDecimal.valueOf(remainingVisits), 2, java.math.RoundingMode.CEILING);
    }

    /**
     * Calculate total materials cost (COGS).
     */
    public BigDecimal calculateTotalMaterialsCost() {
        return followUpVisits.stream()
                .flatMap(visit -> visit.getMaterialUsages().stream())
                .map(usage -> usage.getUnitCost().multiply(usage.getQuantity()))
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
    }

    /**
     * Calculate net revenue (total paid - materials cost).
     */
    public BigDecimal calculateNetRevenue() {
        return calculateTotalPaid().subtract(calculateTotalMaterialsCost());
    }

    // Getters
    public Long getId() {
        return id;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public DoctorEntity getDoctor() {
        return doctor;
    }

    public ClinicServiceEntity getTreatmentType() {
        return treatmentType;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getPlannedFollowups() {
        return plannedFollowups;
    }

    public FollowUpCadence getFollowUpCadence() {
        return followUpCadence;
    }

    public Integer getCompletedVisits() {
        return completedVisits;
    }

    public TreatmentPlanStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public String getDiscountReason() {
        return discountReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public List<FollowUpVisitEntity> getFollowUpVisits() {
        return followUpVisits;
    }

    public List<TreatmentPlanAuditLogEntity> getAuditLogs() {
        return auditLogs;
    }

    public List<TreatmentPlanPaymentEntity> getDirectPayments() {
        return directPayments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreatmentPlanEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
