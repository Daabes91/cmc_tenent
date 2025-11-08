package com.clinic.modules.core.appointment;

import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.payment.PayPalPaymentEntity;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.treatment.PaymentMethod;
import com.clinic.modules.core.treatment.TreatmentPlanEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private PatientEntity patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id")
    private DoctorEntity doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id")
    private ClinicServiceEntity service;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_mode", nullable = false, length = 32)
    private AppointmentMode bookingMode = AppointmentMode.CLINIC_VISIT;

    @Column
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "payment_collected", nullable = false)
    private boolean paymentCollected = false;

    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes = 30;

    @Column(name = "payment_amount", precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_currency", length = 3)
    private String paymentCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 32)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_date")
    private Instant paymentDate;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "payment_notes", columnDefinition = "TEXT")
    private String paymentNotes;

    @Column(name = "patient_attended")
    private Boolean patientAttended;

    @ManyToOne
    @JoinColumn(name = "treatment_plan_id")
    private TreatmentPlanEntity treatmentPlan;

    @Column(name = "follow_up_visit_number")
    private Integer followUpVisitNumber;

    @OneToOne
    @JoinColumn(name = "paypal_payment_id")
    private PayPalPaymentEntity paypalPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private AppointmentSource source = AppointmentSource.WEB;

    protected AppointmentEntity() {
    }

    public AppointmentEntity(PatientEntity patient,
                             DoctorEntity doctor,
                             ClinicServiceEntity service,
                             Instant scheduledAt,
                             AppointmentStatus status,
                             AppointmentMode bookingMode,
                             String notes) {
        this.patient = patient;
        this.doctor = doctor;
        this.service = service;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.bookingMode = bookingMode == null ? AppointmentMode.CLINIC_VISIT : bookingMode;
        this.notes = notes;
        this.source = AppointmentSource.WEB; // Default source
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        if (this.slotDurationMinutes == null) {
            this.slotDurationMinutes = 30;
        }
    }

    public Long getId() {
        return id;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public DoctorEntity getDoctor() {
        return doctor;
    }

    public ClinicServiceEntity getService() {
        return service;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public Integer getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void updateDetails(PatientEntity patient,
                              DoctorEntity doctor,
                              ClinicServiceEntity service,
                              Instant scheduledAt,
                              AppointmentMode bookingMode,
                              String notes) {
        this.patient = patient;
        this.doctor = doctor;
        this.service = service;
        this.scheduledAt = scheduledAt;
        if (bookingMode != null) {
            this.bookingMode = bookingMode;
        }
        this.notes = notes;
    }

    public AppointmentMode getBookingMode() {
        return bookingMode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isPaymentCollected() {
        return paymentCollected;
    }

    public void setPaymentCollected(boolean paymentCollected) {
        this.paymentCollected = paymentCollected;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentNotes() {
        return paymentNotes;
    }

    public void setPaymentNotes(String paymentNotes) {
        this.paymentNotes = paymentNotes;
    }

    public Boolean getPatientAttended() {
        return patientAttended;
    }

    public void setPatientAttended(Boolean patientAttended) {
        this.patientAttended = patientAttended;
    }

    public TreatmentPlanEntity getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(TreatmentPlanEntity treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public Integer getFollowUpVisitNumber() {
        return followUpVisitNumber;
    }

    public void setFollowUpVisitNumber(Integer followUpVisitNumber) {
        this.followUpVisitNumber = followUpVisitNumber;
    }

    public PayPalPaymentEntity getPaypalPayment() {
        return paypalPayment;
    }

    public void setPaypalPayment(PayPalPaymentEntity paypalPayment) {
        this.paypalPayment = paypalPayment;
    }

    public AppointmentSource getSource() {
        return source;
    }

    public void setSource(AppointmentSource source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppointmentEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
