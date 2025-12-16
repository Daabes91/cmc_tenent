package com.clinic.modules.core.payment;

import com.clinic.modules.admin.dto.AppointmentResponse;
import com.clinic.modules.admin.service.NotificationService;
import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentMode;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.appointment.AppointmentStatus;
import com.clinic.modules.core.appointment.AppointmentSource;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.email.EmailService;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.StringUtils;

@Service
public class PayPalPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PayPalPaymentService.class);

    private final PayPalPaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicServiceRepository serviceRepository;
    private final ClinicSettingsRepository settingsRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final PayPalService payPalService;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;

    @Autowired
    public PayPalPaymentService(PayPalPaymentRepository paymentRepository,
                         AppointmentRepository appointmentRepository,
                         PatientRepository patientRepository,
                         DoctorRepository doctorRepository,
                         ClinicServiceRepository serviceRepository,
                         ClinicSettingsRepository settingsRepository,
                         EmailService emailService,
                         NotificationService notificationService,
                         PayPalService payPalService,
                         TenantContextHolder tenantContextHolder,
                         TenantService tenantService) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.serviceRepository = serviceRepository;
        this.settingsRepository = settingsRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.payPalService = payPalService;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    @Transactional
    public String createPayPalOrder(Long patientId, Long doctorId, Long serviceId, String slotId) {
        try {
            // Validate entities exist
            PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
            
            DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
            
            ClinicServiceEntity service = requireServiceForTenant(serviceId);

            // Get clinic settings for fee
            ClinicSettingsEntity settings = requireSettings();

            BigDecimal fee = settings.getVirtualConsultationFee();
            if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Virtual consultation fee not configured");
            }

            // PayPal payments are always in USD regardless of clinic currency
            String currency = "USD";

            // Create metadata for PayPal order
            Map<String, Object> metadata = Map.of(
                "patientId", patientId,
                "doctorId", doctorId,
                "serviceId", serviceId,
                "slotId", slotId,
                "type", PaymentType.VIRTUAL_CONSULTATION.name()
            );

            // Create PayPal order
            String orderId = payPalService.createOrder(fee, currency, metadata);

            // Create payment record
            PayPalPaymentEntity payment = new PayPalPaymentEntity(
                orderId,
                PaymentStatus.PENDING,
                fee,
                currency,
                patient,
                doctor,
                slotId,
                PaymentType.VIRTUAL_CONSULTATION
            );
            
            // Assign current tenant
            Long tenantId = tenantContextHolder.requireTenantId();
            payment.setTenant(tenantService.requireTenant(tenantId));

            paymentRepository.save(payment);

            logger.info("Created PayPal order {} for patient {} with doctor {}", orderId, patientId, doctorId);
            return orderId;

        } catch (Exception e) {
            logger.error("Error creating PayPal order for patient {} with doctor {}", patientId, doctorId, e);
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    @Transactional(noRollbackFor = Exception.class)
    public boolean capturePayPalOrder(String orderId) {
        try {
            Long tenantId = tenantContextHolder.requireTenantId();
            Optional<PayPalPaymentEntity> paymentOpt = paymentRepository.findByTenantIdAndOrderId(tenantId, orderId);
            if (paymentOpt.isEmpty()) {
                logger.error("Payment not found for order ID: {}", orderId);
                return false;
            }

            PayPalPaymentEntity payment = paymentOpt.get();
            
            // Capture the payment with PayPal
            PayPalService.PayPalCaptureResult result = payPalService.captureOrder(orderId);
            
            if (result.isSuccess()) {
                // Update payment status
                payment.updateStatus(
                    PaymentStatus.COMPLETED,
                    result.getCaptureId(),
                    result.getPayerEmail(),
                    result.getPayerName(),
                    result.getRawResponse()
                );
                paymentRepository.save(payment);

                // Create appointment
                createAppointmentFromPayment(payment);

                logger.info("Successfully captured PayPal order {} with capture ID {}", orderId, result.getCaptureId());
                return true;
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                
                logger.error("Failed to capture PayPal order: {}", orderId);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error capturing PayPal order: " + orderId, e);
            return false;
        }
    }

    @Transactional(noRollbackFor = Exception.class)
    public boolean processWebhookPayment(String orderId, String captureId, String payerEmail, String payerName, String rawPayload) {
        try {
            Long tenantId = tenantContextHolder.requireTenantId();
            // Check for idempotency - prevent duplicate processing
            Optional<PayPalPaymentEntity> paymentOpt = paymentRepository.findByTenantIdAndOrderId(tenantId, orderId);
            if (paymentOpt.isEmpty()) {
                logger.error("Payment not found for webhook order ID: {}", orderId);
                return false;
            }

            PayPalPaymentEntity payment = paymentOpt.get();
            
            // If already processed, skip
            if (payment.getStatus() == PaymentStatus.COMPLETED && payment.getAppointment() != null) {
                logger.info("Payment {} already processed, skipping webhook", orderId);
                return true;
            }

            // Update payment status
            payment.updateStatus(PaymentStatus.COMPLETED, captureId, payerEmail, payerName, rawPayload);
            paymentRepository.save(payment);

            // Create appointment if not already created
            if (payment.getAppointment() == null) {
                createAppointmentFromPayment(payment);
            }

            logger.info("Successfully processed webhook for PayPal order {} with capture ID {}", orderId, captureId);
            return true;

        } catch (Exception e) {
            logger.error("Error processing webhook for PayPal order: " + orderId, e);
            return false;
        }
    }

    private void createAppointmentFromPayment(PayPalPaymentEntity payment) {
        try {
            // Parse slot ID to get scheduled time
            Instant scheduledAt;
            try {
                // The slotId should be in ISO format like "2025-10-28T06:30:00Z"
                scheduledAt = Instant.parse(payment.getSlotId());
            } catch (Exception e) {
                logger.warn("Failed to parse slot ID '{}', using current time + 1 hour", payment.getSlotId());
                scheduledAt = Instant.now().plusSeconds(3600); // Fallback to 1 hour from now
            }
            
            // Get service
            ClinicServiceEntity service = resolveDefaultServiceForTenant();

            // Determine slot duration and meeting link from clinic settings
            ClinicSettingsEntity settings = requireSettings();
            int slotDurationMinutes = settings.getSlotDurationMinutes() != null
                    ? settings.getSlotDurationMinutes()
                    : 30;
            String meetingLink = StringUtils.hasText(settings.getVirtualConsultationMeetingLink())
                    ? settings.getVirtualConsultationMeetingLink()
                    : null;

            // Create appointment
            AppointmentEntity appointment = new AppointmentEntity(
                payment.getPatient(),
                payment.getDoctor(),
                service,
                scheduledAt,
                AppointmentStatus.CONFIRMED,
                AppointmentMode.VIRTUAL_CONSULTATION,
                "Virtual consultation - Payment confirmed"
            );
            appointment.setTenant(tenantService.requireTenant(tenantContextHolder.requireTenantId()));
            
            appointment.setPaypalPayment(payment);
            appointment.setSource(AppointmentSource.WEB);
            appointment.setPaymentCollected(true);
            appointment.setSlotDurationMinutes(slotDurationMinutes);

            // Set payment details from PayPal transaction
            appointment.setPaymentAmount(payment.getAmount());
            appointment.setPaymentCurrency(payment.getCurrency()); // Always USD for PayPal
            appointment.setPaymentMethod(com.clinic.modules.core.treatment.PaymentMethod.PAYPAL);
            appointment.setPaymentDate(payment.getCreatedAt());

            appointmentRepository.save(appointment);
            sendVirtualConsultationEmail(appointment, meetingLink);
            notifyStaffOfNewAppointment(appointment);
            
            // Update payment to link back to appointment
            payment.setAppointment(appointment);
            paymentRepository.save(payment);

            logger.info("Created appointment {} for payment {}", appointment.getId(), payment.getOrderId());

        } catch (Exception e) {
            logger.error("Error creating appointment for payment: " + payment.getOrderId(), e);
            throw new RuntimeException("Failed to create appointment: " + e.getMessage());
        }
    }

    private ClinicSettingsEntity requireSettings() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return settingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Clinic settings not configured for tenant " + tenantId));
    }

    private void sendVirtualConsultationEmail(AppointmentEntity appointment, String meetingLink) {
        try {
            PatientEntity patient = appointment.getPatient();
            if (patient == null) {
                return;
            }

            String patientEmail = patient.getEmail();
            if (patientEmail == null || patientEmail.isBlank()) {
                return;
            }

            DoctorEntity doctor = appointment.getDoctor();
            ClinicServiceEntity service = appointment.getService();

            String patientName = String.format("%s %s",
                    Optional.ofNullable(patient.getFirstName()).orElse(""),
                    Optional.ofNullable(patient.getLastName()).orElse("" )).trim();
            if (patientName.isBlank()) {
                patientName = "Patient";
            }

            String doctorName = doctor != null ? Optional.ofNullable(doctor.getFullName()).orElse("Doctor") : "Doctor";
            String serviceName = service != null
                    ? Optional.ofNullable(service.getNameEn()).orElse(service.getSlug())
                    : "Virtual Consultation";

            int slotDurationMinutes = appointment.getSlotDurationMinutes() != null
                    ? appointment.getSlotDurationMinutes()
                    : 30;

            ZoneId clinicZone = ZoneId.of("Asia/Amman");
            ZonedDateTime start = appointment.getScheduledAt().atZone(clinicZone);
            ZonedDateTime end = start.plusMinutes(slotDurationMinutes);

            emailService.sendVirtualConsultationConfirmation(
                    patientEmail,
                    patientName,
                    doctorName,
                    serviceName,
                    start,
                    end,
                    meetingLink
            );
        } catch (Exception ex) {
            logger.warn("Failed to send virtual consultation email for appointment {}: {}",
                    appointment.getId(), ex.getMessage());
        }
    }

    private void notifyStaffOfNewAppointment(AppointmentEntity appointment) {
        try {
            notificationService.broadcastNewAppointment(toAppointmentResponse(appointment));
        } catch (Exception ex) {
            logger.warn("Failed to broadcast appointment notification for {}: {}",
                    appointment.getId(), ex.getMessage());
        }
    }

    private AppointmentResponse toAppointmentResponse(AppointmentEntity entity) {
        String patientName = Optional.ofNullable(entity.getPatient())
                .map(patient -> {
                    String first = Optional.ofNullable(patient.getFirstName()).orElse("").trim();
                    String last = Optional.ofNullable(patient.getLastName()).orElse("").trim();
                    String combined = (first + " " + last).trim();
                    return combined.isBlank() ? null : combined;
                })
                .orElse("Patient");

        String doctorName = Optional.ofNullable(entity.getDoctor())
                .map(DoctorEntity::getFullName)
                .orElse("Doctor");

        String serviceName = Optional.ofNullable(entity.getService())
                .map(service -> Optional.ofNullable(service.getNameEn()).orElse(service.getSlug()))
                .orElse("Virtual Consultation");

        ZoneId clinicZone = ZoneId.of("Asia/Amman");

        return new AppointmentResponse(
                entity.getId(),
                patientName,
                doctorName,
                serviceName,
                entity.getScheduledAt().atZone(clinicZone).toOffsetDateTime(),
                entity.getStatus().name(),
                entity.getBookingMode().name(),
                Optional.ofNullable(entity.getTreatmentPlan()).map(plan -> plan.getId()).orElse(null),
                entity.getFollowUpVisitNumber(),
                entity.isPaymentCollected(),
                entity.getPatientAttended(),
                entity.getSlotDurationMinutes(),
                entity.getPaymentAmount(),
                Optional.ofNullable(entity.getPaymentMethod()).map(Enum::name).orElse(null),
                entity.getPaymentCurrency(),
                entity.isPatientConfirmed(),
                entity.getPatientConfirmedAt() != null
                        ? entity.getPatientConfirmedAt().atZone(clinicZone).toOffsetDateTime()
                        : null
        );
    }

    public Optional<PayPalPaymentEntity> findByOrderId(String orderId) {
        Long tenantId = tenantContextHolder.requireTenantId();
        return paymentRepository.findByTenantIdAndOrderId(tenantId, orderId);
    }

    private ClinicServiceEntity requireServiceForTenant(Long serviceId) {
        Long tenantId = tenantContextHolder.requireTenantId();
        return serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseGet(() -> {
                    // Fallback: use the first available service for this tenant
                    return serviceRepository.findFirstByTenantIdOrderByCreatedAtAsc(tenantId)
                            .orElseGet(() -> createDefaultVirtualService(tenantId));
                });
    }

    private ClinicServiceEntity createDefaultVirtualService(Long tenantId) {
        var tenant = tenantService.requireTenant(tenantId);
        String slug = "virtual-consultation-" + tenantId;
        // Ensure uniqueness if slug exists; append random suffix
        if (serviceRepository.findBySlugAndTenantId(slug, tenantId).isPresent()) {
            slug = slug + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }

        ClinicServiceEntity svc = new ClinicServiceEntity(
                slug,
                tenant,
                "Virtual Consultation",
                "استشارة افتراضية",
                "Virtual consultation service",
                "خدمة استشارة افتراضية"
        );
        return serviceRepository.save(svc);
    }

    private ClinicServiceEntity resolveDefaultServiceForTenant() {
        return serviceRepository.findFirstByTenantIdOrderByCreatedAtAsc(tenantContextHolder.requireTenantId())
                .orElseThrow(() -> new IllegalArgumentException("No services configured for this clinic"));
    }
}
