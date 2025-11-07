package com.clinic.modules.core.payment;

import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.patient.PatientEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "paypal_payments", indexes = {
    @Index(name = "idx_payment_order_id", columnList = "order_id", unique = true),
    @Index(name = "idx_payment_patient_id", columnList = "patient_id"),
    @Index(name = "idx_payment_status", columnList = "status")
})
public class PayPalPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(name = "capture_id", length = 100)
    private String captureId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private PatientEntity patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id")
    private DoctorEntity doctor;

    @Column(name = "slot_id")
    private String slotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentType type;

    @Column(name = "payer_email", length = 150)
    private String payerEmail;

    @Column(name = "payer_name", length = 200)
    private String payerName;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @OneToOne(mappedBy = "paypalPayment")
    private AppointmentEntity appointment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected PayPalPaymentEntity() {
    }

    public PayPalPaymentEntity(String orderId, PaymentStatus status, BigDecimal amount, String currency,
                         PatientEntity patient, DoctorEntity doctor, String slotId, PaymentType type) {
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.patient = patient;
        this.doctor = doctor;
        this.slotId = slotId;
        this.type = type;
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

    public void updateStatus(PaymentStatus status, String captureId, String payerEmail, String payerName, String rawPayload) {
        this.status = status;
        this.captureId = captureId;
        this.payerEmail = payerEmail;
        this.payerName = payerName;
        this.rawPayload = rawPayload;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCaptureId() {
        return captureId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public DoctorEntity getDoctor() {
        return doctor;
    }

    public String getSlotId() {
        return slotId;
    }

    public PaymentType getType() {
        return type;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public String getPayerName() {
        return payerName;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public AppointmentEntity getAppointment() {
        return appointment;
    }

    public void setAppointment(AppointmentEntity appointment) {
        this.appointment = appointment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalPaymentEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}