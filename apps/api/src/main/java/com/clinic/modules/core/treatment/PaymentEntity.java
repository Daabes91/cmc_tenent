package com.clinic.modules.core.treatment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a payment made during a follow-up visit.
 */
@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "visit_id")
    private FollowUpVisitEntity visit;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PaymentEntity() {
    }

    public PaymentEntity(
            FollowUpVisitEntity visit,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            Instant paymentDate,
            String transactionReference,
            String notes
    ) {
        this.visit = visit;
        this.amount = amount;
        this.currency = currency != null ? currency : "USD";
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.transactionReference = transactionReference;
        this.notes = notes;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public FollowUpVisitEntity getVisit() {
        return visit;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
