package com.clinic.modules.core.treatment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a direct payment made against a treatment plan.
 * This is separate from visit-based payments and allows for flexible payment recording.
 */
@Entity
@Table(name = "treatment_plan_payments")
public class TreatmentPlanPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "treatment_plan_id")
    private TreatmentPlanEntity treatmentPlan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "payment_date", nullable = false)
    private Instant paymentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_by_staff_id", nullable = false)
    private Long recordedByStaffId;

    @Column(name = "recorded_by_staff_name", nullable = false)
    private String recordedByStaffName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TreatmentPlanPaymentEntity() {
    }

    public TreatmentPlanPaymentEntity(
            TreatmentPlanEntity treatmentPlan,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            Instant paymentDate,
            String transactionReference,
            String notes,
            Long recordedByStaffId,
            String recordedByStaffName
    ) {
        this.treatmentPlan = treatmentPlan;
        this.amount = amount;
        this.currency = currency != null ? currency : "SAR";
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.transactionReference = transactionReference;
        this.notes = notes;
        this.recordedByStaffId = recordedByStaffId;
        this.recordedByStaffName = recordedByStaffName;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public TreatmentPlanEntity getTreatmentPlan() {
        return treatmentPlan;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public Long getRecordedByStaffId() {
        return recordedByStaffId;
    }

    public String getRecordedByStaffName() {
        return recordedByStaffName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreatmentPlanPaymentEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}