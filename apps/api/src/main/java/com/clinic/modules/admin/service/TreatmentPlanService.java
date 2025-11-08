package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.*;
import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentMode;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.appointment.AppointmentStatus;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.core.service.CurrencyConversionService;
import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.treatment.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing treatment plans with dynamic payment calculations.
 */
@Service
public class TreatmentPlanService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentPlanPaymentRepository treatmentPlanPaymentRepository;
    private final FollowUpVisitRepository followUpVisitRepository;
    private final MaterialCatalogRepository materialCatalogRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicSettingsRepository clinicSettingsRepository;
    private final CurrencyConversionService currencyConversionService;
    private final TenantContextHolder tenantContextHolder;
    private final com.clinic.modules.core.tenant.TenantService tenantService;

    private static final int DEFAULT_SLOT_DURATION_MINUTES = 30;
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Amman");
    private static final LocalTime DEFAULT_SLOT_TIME = LocalTime.of(10, 0);
    private static final int MAX_SLOT_ADJUSTMENTS_PER_DAY = 16;

    public TreatmentPlanService(
            TreatmentPlanRepository treatmentPlanRepository,
            TreatmentPlanPaymentRepository treatmentPlanPaymentRepository,
            FollowUpVisitRepository followUpVisitRepository,
            MaterialCatalogRepository materialCatalogRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            ClinicServiceRepository clinicServiceRepository,
            AppointmentRepository appointmentRepository,
            ClinicSettingsRepository clinicSettingsRepository,
            CurrencyConversionService currencyConversionService,
            TenantContextHolder tenantContextHolder,
            com.clinic.modules.core.tenant.TenantService tenantService
    ) {
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.treatmentPlanPaymentRepository = treatmentPlanPaymentRepository;
        this.followUpVisitRepository = followUpVisitRepository;
        this.materialCatalogRepository = materialCatalogRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.clinicServiceRepository = clinicServiceRepository;
        this.appointmentRepository = appointmentRepository;
        this.clinicSettingsRepository = clinicSettingsRepository;
        this.currencyConversionService = currencyConversionService;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    /**
     * Create a new treatment plan.
     */
    @Transactional
    public TreatmentPlanResponse createTreatmentPlan(TreatmentPlanRequest request, Long staffId, String staffName) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        // Validate patient belongs to current tenant
        PatientEntity patient = patientRepository.findByIdAndTenantId(request.patientId(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        // Validate doctor belongs to current tenant
        DoctorEntity doctor = doctorRepository.findByIdAndTenantId(request.doctorId(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        // Validate treatment type belongs to current tenant
        ClinicServiceEntity treatmentType = clinicServiceRepository.findByIdAndTenantId(request.treatmentTypeId(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment type not found"));

        TreatmentPlanEntity plan = new TreatmentPlanEntity(
                patient,
                doctor,
                treatmentType,
                request.totalPrice(),
                request.currency(),
                request.plannedFollowups(),
                request.followUpCadence(),
                request.notes()
        );
        
        // Assign current tenant
        plan.setTenant(tenantService.requireTenant(tenantId));

        TreatmentPlanEntity saved = treatmentPlanRepository.save(plan);

        // Create audit log for creation
        TreatmentPlanAuditLogEntity auditLog = new TreatmentPlanAuditLogEntity(
                saved,
                staffId,
                staffName,
                "PLAN_CREATED",
                null,
                "Total: " + request.totalPrice() + " " + request.currency() + ", Follow-ups: " + request.plannedFollowups(),
                "Treatment plan created"
        );
        saved.getAuditLogs().add(auditLog);

        syncFollowUpAppointmentPlaceholders(saved);

        return toResponse(saved);
    }

    /**
     * Get treatment plan by ID.
     */
    @Transactional(readOnly = true)
    public TreatmentPlanResponse getTreatmentPlan(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        TreatmentPlanEntity plan = treatmentPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));
        return toResponse(plan);
    }

    /**
     * Get all treatment plans for a patient.
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlanResponse> getTreatmentPlansByPatient(Long patientId) {
        Long tenantId = tenantContextHolder.requireTenantId();
        return treatmentPlanRepository.findByTenantIdAndPatientId(tenantId, patientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all treatment plans for a doctor.
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlanResponse> getTreatmentPlansByDoctor(Long doctorId) {
        Long tenantId = tenantContextHolder.requireTenantId();
        return treatmentPlanRepository.findByTenantIdAndDoctorId(tenantId, doctorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all treatment plans by status.
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlanResponse> getTreatmentPlansByStatus(TreatmentPlanStatus status) {
        Long tenantId = tenantContextHolder.requireTenantId();
        return treatmentPlanRepository.findByTenantIdAndStatus(tenantId, status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all treatment plans.
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlanResponse> getAllTreatmentPlans() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return treatmentPlanRepository.findAllByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update treatment plan pricing and follow-ups.
     */
    @Transactional
    public TreatmentPlanResponse updateTreatmentPlan(Long id, TreatmentPlanUpdateRequest request, Long staffId, String staffName) {
        Long tenantId = tenantContextHolder.requireTenantId();
        TreatmentPlanEntity plan = treatmentPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));

        // Create audit logs for changes
        String oldValue = "Total: " + plan.getTotalPrice() + ", Follow-ups: " + plan.getPlannedFollowups() + ", Cadence: " + plan.getFollowUpCadence();
        String newValue = "Total: " + request.totalPrice() + ", Follow-ups: " + request.plannedFollowups() + ", Cadence: " + request.followUpCadence();

        if (!plan.getTotalPrice().equals(request.totalPrice())
                || !plan.getPlannedFollowups().equals(request.plannedFollowups())
                || !plan.getFollowUpCadence().equals(request.followUpCadence())) {
            if (request.plannedFollowups() < plan.getCompletedVisits()) {
                throw new IllegalArgumentException("Planned follow-up visits cannot be less than completed visits (" + plan.getCompletedVisits() + ")");
            }
            TreatmentPlanAuditLogEntity auditLog = new TreatmentPlanAuditLogEntity(
                    plan,
                    staffId,
                    staffName,
                    "PLAN_UPDATED",
                    oldValue,
                    newValue,
                    "Treatment plan updated"
            );
            plan.getAuditLogs().add(auditLog);
        }

        plan.updatePlan(request.totalPrice(), request.plannedFollowups(), request.followUpCadence(), request.notes());
        syncFollowUpAppointmentPlaceholders(plan);

        return toResponse(plan);
    }

    /**
     * Apply discount to treatment plan.
     */
    @Transactional
    public TreatmentPlanResponse applyDiscount(Long id, DiscountRequest request, Long staffId, String staffName) {
        Long tenantId = tenantContextHolder.requireTenantId();
        TreatmentPlanEntity plan = treatmentPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));

        String oldValue = plan.getDiscountAmount() != null ? plan.getDiscountAmount().toString() : "None";
        String newValue = request.discountAmount().toString();

        TreatmentPlanAuditLogEntity auditLog = new TreatmentPlanAuditLogEntity(
                plan,
                staffId,
                staffName,
                "DISCOUNT_APPLIED",
                oldValue,
                newValue,
                request.discountReason()
        );
        plan.getAuditLogs().add(auditLog);

        plan.applyDiscount(request.discountAmount(), request.discountReason());

        return toResponse(plan);
    }

    /**
     * Record a follow-up visit with payments and materials.
     */
    @Transactional
    public TreatmentPlanResponse recordFollowUpVisit(Long planId, FollowUpVisitRequest request, Long staffId, String staffName) {
        Long tenantId = tenantContextHolder.requireTenantId();
        TreatmentPlanEntity plan = treatmentPlanRepository.findByIdAndTenantId(planId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));

        AppointmentEntity linkedAppointment = null;
        Integer visitNumber;

        if (request.appointmentId() != null) {
            linkedAppointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + request.appointmentId()));

            if (linkedAppointment.getTreatmentPlan() == null || !linkedAppointment.getTreatmentPlan().getId().equals(planId)) {
                throw new IllegalArgumentException("Appointment does not belong to treatment plan ID: " + planId);
            }

            visitNumber = linkedAppointment.getFollowUpVisitNumber();
            if (visitNumber == null) {
                visitNumber = findNextVisitNumber(plan);
                linkedAppointment.setFollowUpVisitNumber(visitNumber);
            }

            for (FollowUpVisitEntity existingVisit : plan.getFollowUpVisits()) {
                if (existingVisit.getVisitNumber() != null && existingVisit.getVisitNumber().equals(visitNumber)) {
                    throw new IllegalStateException("Follow-up visit number " + visitNumber + " has already been recorded.");
                }
            }
        } else {
            visitNumber = findNextVisitNumber(plan);
        }

        final Integer resolvedVisitNumber = visitNumber;

        FollowUpVisitEntity visit = new FollowUpVisitEntity(
                plan,
                resolvedVisitNumber,
                request.visitDate(),
                request.notes(),
                request.performedProcedures()
        );

        // Add payments
        if (request.payments() != null) {
            String clinicCurrency = getClinicCurrency();
            for (PaymentRequest paymentReq : request.payments()) {
                PaymentEntity payment = new PaymentEntity(
                        visit,
                        paymentReq.amount(),
                        clinicCurrency,
                        paymentReq.paymentMethod(),
                        paymentReq.paymentDate(),
                        paymentReq.transactionReference(),
                        paymentReq.notes()
                );
                visit.getPayments().add(payment);
            }
        }

        // Add materials
        if (request.materials() != null) {
            for (MaterialUsageRequest materialReq : request.materials()) {
                MaterialCatalogEntity material = materialCatalogRepository.findByIdAndTenantId(materialReq.materialId(), tenantId)
                        .orElseThrow(() -> new IllegalArgumentException("Material not found with ID: " + materialReq.materialId()));

                MaterialUsageEntity usage = new MaterialUsageEntity(
                        visit,
                        material,
                        materialReq.quantity(),
                        materialReq.notes()
                );
                visit.getMaterialUsages().add(usage);
            }
        }

        plan.getFollowUpVisits().add(visit);
        plan.incrementCompletedVisits();
        markAssociatedAppointmentCompleted(plan, visit, resolvedVisitNumber, linkedAppointment);

        // Create audit log
        BigDecimal totalPayment = visit.getPayments().stream()
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TreatmentPlanAuditLogEntity auditLog = new TreatmentPlanAuditLogEntity(
                plan,
                staffId,
                staffName,
                "VISIT_RECORDED",
                null,
                "Visit #" + resolvedVisitNumber + ", Payment: " + totalPayment,
                "Follow-up visit recorded"
        );
        plan.getAuditLogs().add(auditLog);

        return toResponse(plan);
    }

    /**
     * Update an existing follow-up visit.
     */
    @Transactional
    public TreatmentPlanResponse updateFollowUpVisit(Long planId, Long visitId, FollowUpVisitRequest request, Long staffId, String staffName) {
        Long tenantId = tenantContextHolder.requireTenantId();
        TreatmentPlanEntity plan = treatmentPlanRepository.findByIdAndTenantId(planId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));

        FollowUpVisitEntity visit = followUpVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Follow-up visit not found with ID: " + visitId));

        if (!visit.getTreatmentPlan().getId().equals(planId)) {
            throw new IllegalArgumentException("Follow-up visit does not belong to treatment plan ID: " + planId);
        }

        Integer visitNumber = visit.getVisitNumber();
        if (visitNumber == null) {
            throw new IllegalStateException("Recorded visit is missing a visit number and cannot be edited.");
        }

        AppointmentEntity appointmentToUpdate = appointmentRepository.findByTreatmentPlanIdAndFollowUpVisitNumber(planId, visitNumber)
                .orElse(null);

        if (request.appointmentId() != null) {
            AppointmentEntity providedAppointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + request.appointmentId()));

            if (providedAppointment.getTreatmentPlan() == null || !providedAppointment.getTreatmentPlan().getId().equals(planId)) {
                throw new IllegalArgumentException("Appointment does not belong to treatment plan ID: " + planId);
            }

            if (appointmentToUpdate != null && !appointmentToUpdate.getId().equals(providedAppointment.getId())) {
                throw new IllegalArgumentException("Recorded visit is already linked to appointment ID: " + appointmentToUpdate.getId());
            }

            if (appointmentToUpdate == null) {
                providedAppointment.setFollowUpVisitNumber(visitNumber);
                appointmentToUpdate = providedAppointment;
            }
        }

        visit.updateDetails(request.visitDate(), request.notes(), request.performedProcedures());

        visit.getPayments().clear();
        if (request.payments() != null) {
            String clinicCurrency = getClinicCurrency();
            for (PaymentRequest paymentReq : request.payments()) {
                PaymentEntity payment = new PaymentEntity(
                        visit,
                        paymentReq.amount(),
                        clinicCurrency,
                        paymentReq.paymentMethod(),
                        paymentReq.paymentDate(),
                        paymentReq.transactionReference(),
                        paymentReq.notes()
                );
                visit.getPayments().add(payment);
            }
        }

        visit.getMaterialUsages().clear();
        if (request.materials() != null) {
            for (MaterialUsageRequest materialReq : request.materials()) {
                MaterialCatalogEntity material = materialCatalogRepository.findByIdAndTenantId(materialReq.materialId(), tenantId)
                        .orElseThrow(() -> new IllegalArgumentException("Material not found with ID: " + materialReq.materialId()));

                MaterialUsageEntity usage = new MaterialUsageEntity(
                        visit,
                        material,
                        materialReq.quantity(),
                        materialReq.notes()
                );
                visit.getMaterialUsages().add(usage);
            }
        }

        markAssociatedAppointmentCompleted(plan, visit, visitNumber, appointmentToUpdate);

        BigDecimal totalPayment = visit.getPayments().stream()
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TreatmentPlanAuditLogEntity auditLog = new TreatmentPlanAuditLogEntity(
                plan,
                staffId,
                staffName,
                "VISIT_UPDATED",
                null,
                "Visit #" + visitNumber + ", Payment: " + totalPayment,
                "Follow-up visit updated"
        );
        plan.getAuditLogs().add(auditLog);

        return toResponse(plan);
    }

    /**
     * Complete a treatment plan.
     */
    @Transactional
    public TreatmentPlanResponse completeTreatmentPlan(Long id, Long staffId, String staffName) {
        Long tenantId = tenantContextHolder.requireTenantId();
        TreatmentPlanEntity plan = treatmentPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));

        TreatmentPlanAuditLogEntity auditLog = new TreatmentPlanAuditLogEntity(
                plan,
                staffId,
                staffName,
                "STATUS_CHANGE",
                plan.getStatus().toString(),
                "COMPLETED",
                "Treatment plan completed"
        );
        plan.getAuditLogs().add(auditLog);

        cancelOpenFollowUpAppointments(plan, plan.getCompletedVisits());
        plan.complete();

        return toResponse(plan);
    }

    /**
     * Cancel a treatment plan.
     */
    @Transactional
    public TreatmentPlanResponse cancelTreatmentPlan(Long id, String reason, Long staffId, String staffName) {
        Long tenantId = tenantContextHolder.requireTenantId();
        TreatmentPlanEntity plan = treatmentPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));

        TreatmentPlanAuditLogEntity auditLog = new TreatmentPlanAuditLogEntity(
                plan,
                staffId,
                staffName,
                "STATUS_CHANGE",
                plan.getStatus().toString(),
                "CANCELLED",
                reason
        );
        plan.getAuditLogs().add(auditLog);

        cancelOpenFollowUpAppointments(plan, plan.getCompletedVisits());
        plan.cancel(reason);

        return toResponse(plan);
    }

    /**
     * Convert entity to response DTO with all calculated fields.
     */
    private TreatmentPlanResponse toResponse(TreatmentPlanEntity plan) {
        List<AppointmentEntity> appointments = appointmentRepository
                .findByTreatmentPlanIdOrderByFollowUpVisitNumberAsc(plan.getId());

        var appointmentIdByVisitNumber = appointments.stream()
                .filter(appt -> appt.getFollowUpVisitNumber() != null)
                .collect(Collectors.toMap(
                        AppointmentEntity::getFollowUpVisitNumber,
                        AppointmentEntity::getId,
                        (existing, duplicate) -> existing
                ));

        List<FollowUpVisitResponse> visits = plan.getFollowUpVisits().stream()
                .sorted(Comparator.comparing(
                        FollowUpVisitEntity::getVisitNumber,
                        Comparator.nullsLast(Integer::compare)
                ))
                .map(visit -> toVisitResponse(visit, appointmentIdByVisitNumber.get(visit.getVisitNumber())))
                .collect(Collectors.toList());

        List<ScheduledFollowUpResponse> scheduledFollowUps = appointments.stream()
                .filter(appt -> appt.getStatus() != AppointmentStatus.CANCELLED)
                .map(appt -> new ScheduledFollowUpResponse(
                        appt.getId(),
                        appt.getFollowUpVisitNumber(),
                        appt.getScheduledAt(),
                        appt.getStatus().name(),
                        appt.getBookingMode().name(),
                        appt.isPaymentCollected(),
                        appt.getPatientAttended()
                ))
                .collect(Collectors.toList());

        // Calculate converted prices in clinic currency
        String clinicCurrency = currencyConversionService.getClinicCurrency();

        // Convert treatment plan price
        BigDecimal convertedTotalPrice = currencyConversionService.convert(
                plan.getTotalPrice(),
                plan.getCurrency(),
                clinicCurrency
        );

        // Calculate totals from visit-level converted values (properly handles multi-currency)
        BigDecimal convertedTotalPaid = visits.stream()
                .map(FollowUpVisitResponse::totalPaymentsThisVisit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add direct payments (converted to clinic currency)
        BigDecimal convertedDirectPayments = plan.getDirectPayments().stream()
                .map(payment -> currencyConversionService.convert(
                        payment.getAmount(),
                        payment.getCurrency(),
                        clinicCurrency
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        convertedTotalPaid = convertedTotalPaid.add(convertedDirectPayments);

        BigDecimal convertedTotalMaterialsCost = visits.stream()
                .map(FollowUpVisitResponse::totalMaterialsCostThisVisit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate derived values using converted amounts
        BigDecimal convertedDiscountAmount = plan.getDiscountAmount() != null
                ? currencyConversionService.convert(plan.getDiscountAmount(), plan.getCurrency(), clinicCurrency)
                : BigDecimal.ZERO;

        BigDecimal convertedRemainingBalance = convertedTotalPrice
                .subtract(convertedDiscountAmount)
                .subtract(convertedTotalPaid);

        BigDecimal convertedExpectedPaymentPerVisit = plan.calculateRemainingVisits() > 0
                ? convertedRemainingBalance.divide(
                        BigDecimal.valueOf(plan.calculateRemainingVisits()),
                        2,
                        RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal convertedNetRevenue = convertedTotalPaid.subtract(convertedTotalMaterialsCost);

        return new TreatmentPlanResponse(
                plan.getId(),
                plan.getPatient().getId(),
                plan.getPatient().getFirstName() + " " + plan.getPatient().getLastName(),
                plan.getDoctor().getId(),
                plan.getDoctor().getFullName(),
                plan.getTreatmentType().getId(),
                plan.getTreatmentType().getNameEn(),
                plan.getTotalPrice(),
                plan.getCurrency(),
                plan.getPlannedFollowups(),
                plan.getFollowUpCadence(),
                plan.getCompletedVisits(),
                plan.getStatus(),
                plan.getNotes(),
                plan.getDiscountAmount(),
                plan.getDiscountReason(),
                plan.getCreatedAt(),
                plan.getUpdatedAt(),
                plan.getStartedAt(),
                plan.getCompletedAt(),
                plan.calculateTotalPaid(),
                plan.calculateRemainingBalance(),
                plan.calculateRemainingVisits(),
                plan.calculateExpectedPaymentPerVisit(),
                plan.calculateTotalMaterialsCost(),
                plan.calculateNetRevenue(),
                convertedTotalPrice,
                clinicCurrency,
                convertedTotalPaid,
                convertedRemainingBalance,
                convertedExpectedPaymentPerVisit,
                convertedTotalMaterialsCost,
                convertedNetRevenue,
                visits,
                scheduledFollowUps
        );
    }

    /**
     * Convert visit entity to response DTO.
     */
    private FollowUpVisitResponse toVisitResponse(FollowUpVisitEntity visit, Long appointmentId) {
        String clinicCurrency = currencyConversionService.getClinicCurrency();

        List<PaymentResponse> payments = visit.getPayments().stream()
                .map(p -> {
                    BigDecimal convertedAmount = currencyConversionService.convert(
                            p.getAmount(),
                            p.getCurrency(),
                            clinicCurrency
                    );
                    return new PaymentResponse(
                            p.getId(),
                            p.getAmount(),
                            p.getCurrency(),
                            convertedAmount,
                            clinicCurrency,
                            p.getPaymentMethod(),
                            p.getTransactionReference(),
                            p.getPaymentDate(),
                            p.getNotes(),
                            p.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        List<MaterialUsageResponse> materials = visit.getMaterialUsages().stream()
                .map(m -> {
                    BigDecimal totalCost = m.getUnitCost().multiply(m.getQuantity());
                    BigDecimal convertedTotalCost = currencyConversionService.convert(
                            totalCost,
                            m.getMaterial().getCurrency(),
                            clinicCurrency
                    );
                    return new MaterialUsageResponse(
                            m.getId(),
                            m.getMaterial().getId(),
                            m.getMaterial().getName(),
                            m.getQuantity(),
                            m.getUnitCost(),
                            m.getMaterial().getCurrency(),
                            totalCost,
                            convertedTotalCost,
                            clinicCurrency,
                            m.getNotes(),
                            m.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        BigDecimal totalPayments = payments.stream()
                .map(PaymentResponse::convertedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMaterials = materials.stream()
                .map(MaterialUsageResponse::convertedTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FollowUpVisitResponse(
                visit.getId(),
                visit.getVisitNumber(),
                appointmentId,
                visit.getVisitDate(),
                visit.getNotes(),
                visit.getPerformedProcedures(),
                visit.getCreatedAt(),
                payments,
                materials,
                totalPayments,
                totalMaterials
        );
    }

    private void syncFollowUpAppointmentPlaceholders(TreatmentPlanEntity plan) {
        int requiredFollowups = plan.getPlannedFollowups() != null ? plan.getPlannedFollowups() : 0;
        if (requiredFollowups <= 0) {
            cancelOpenFollowUpAppointments(plan, 0);
            return;
        }

        List<AppointmentEntity> existing = appointmentRepository.findByTreatmentPlanIdOrderByFollowUpVisitNumberAsc(plan.getId());

        Set<Integer> activeVisitNumbers = existing.stream()
                .filter(appointment -> appointment.getFollowUpVisitNumber() != null)
                .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELLED)
                .map(AppointmentEntity::getFollowUpVisitNumber)
                .collect(Collectors.toCollection(HashSet::new));

        Instant latestScheduled = existing.stream()
                .filter(appointment -> appointment.getScheduledAt() != null)
                .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELLED)
                .map(AppointmentEntity::getScheduledAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        for (int visitNumber = 1; visitNumber <= requiredFollowups; visitNumber++) {
            if (!activeVisitNumbers.contains(visitNumber)) {
                Instant suggested = determineSuggestedFollowUpStart(plan, latestScheduled, visitNumber);
                Instant scheduledAt = findNextAvailableSlot(plan.getDoctor().getId(), suggested);

                AppointmentEntity appointment = new AppointmentEntity(
                        plan.getPatient(),
                        plan.getDoctor(),
                        plan.getTreatmentType(),
                        scheduledAt,
                        AppointmentStatus.SCHEDULED,
                        AppointmentMode.CLINIC_VISIT,
                        "Follow-up visit #" + visitNumber + " for treatment plan #" + plan.getId()
                );
                appointment.setTreatmentPlan(plan);
                appointment.setFollowUpVisitNumber(visitNumber);
                appointment.setSlotDurationMinutes(getDefaultSlotDurationMinutes());
                appointmentRepository.save(appointment);

                activeVisitNumbers.add(visitNumber);
                latestScheduled = scheduledAt;
            }
        }

        cancelOpenFollowUpAppointments(plan, requiredFollowups);
    }

    private void cancelOpenFollowUpAppointments(TreatmentPlanEntity plan, int keepThroughVisit) {
        List<AppointmentEntity> existing = appointmentRepository.findByTreatmentPlanIdOrderByFollowUpVisitNumberAsc(plan.getId());
        for (AppointmentEntity appointment : existing) {
            Integer visitNumber = appointment.getFollowUpVisitNumber();
            if (visitNumber == null) {
                continue;
            }
            if (visitNumber > keepThroughVisit
                    && appointment.getStatus() != AppointmentStatus.CANCELLED
                    && appointment.getStatus() != AppointmentStatus.COMPLETED) {
                appointment.setStatus(AppointmentStatus.CANCELLED);
            }
        }
    }

    private Instant determineSuggestedFollowUpStart(TreatmentPlanEntity plan, Instant latestScheduled, int visitNumber) {
        Instant candidate;
        if (latestScheduled != null) {
            ZonedDateTime base = latestScheduled.atZone(CLINIC_ZONE);
            ZonedDateTime next = applyCadence(base, plan.getFollowUpCadence(), 1);
            candidate = next.toInstant();
        } else {
            candidate = defaultFollowUpInstant(plan, visitNumber);
        }

        Instant minFuture = Instant.now().plus(1, ChronoUnit.DAYS);
        if (candidate.isBefore(minFuture)) {
            candidate = minFuture;
        }

        return candidate;
    }

    private Instant defaultFollowUpInstant(TreatmentPlanEntity plan, int visitNumber) {
        int steps = Math.max(visitNumber, 1);
        ZonedDateTime zoned = ZonedDateTime.of(LocalDate.now(CLINIC_ZONE), DEFAULT_SLOT_TIME, CLINIC_ZONE);
        zoned = applyCadence(zoned, plan.getFollowUpCadence(), steps);
        return zoned.toInstant();
    }

    private ZonedDateTime applyCadence(ZonedDateTime base, FollowUpCadence cadence, int steps) {
        if (cadence == null) {
            cadence = FollowUpCadence.defaultCadence();
        }
        return switch (cadence) {
            case MONTHLY -> base.plusMonths(steps);
            case WEEKLY -> base.plusWeeks(steps);
        };
    }

    private Instant findNextAvailableSlot(Long doctorId, Instant preferredStart) {
        int slotDurationMinutes = getDefaultSlotDurationMinutes();
        Duration slotDuration = Duration.ofMinutes(slotDurationMinutes);
        Instant candidate = ensureFutureInstant(preferredStart, slotDuration);
        int adjustments = 0;

        Long tenantId = tenantContextHolder.requireTenantId();
        while (appointmentRepository.existsActiveByDoctorAndTimeRange(
                tenantId,
                doctorId,
                candidate,
                candidate.plus(slotDuration),
                DEFAULT_SLOT_DURATION_MINUTES
        )) {
            candidate = candidate.plus(slotDuration);
            adjustments++;

            if (adjustments >= MAX_SLOT_ADJUSTMENTS_PER_DAY) {
                ZonedDateTime nextDay = candidate.atZone(CLINIC_ZONE)
                        .plusDays(1)
                        .withHour(DEFAULT_SLOT_TIME.getHour())
                        .withMinute(DEFAULT_SLOT_TIME.getMinute())
                        .withSecond(0)
                        .withNano(0);
                candidate = nextDay.toInstant();
                adjustments = 0;
            }
        }

        return candidate;
    }

    private Instant ensureFutureInstant(Instant value, Duration slotDuration) {
        Instant now = Instant.now();
        if (value == null || value.isBefore(now)) {
            return now.plus(slotDuration);
        }
        return value;
    }

    private int getDefaultSlotDurationMinutes() {
        ClinicSettingsEntity settings = requireSettings();
        Integer duration = settings.getSlotDurationMinutes();
        if (duration != null && duration >= 5 && duration <= 240) {
            return duration;
        }
        return DEFAULT_SLOT_DURATION_MINUTES;
    }

    private void markAssociatedAppointmentCompleted(TreatmentPlanEntity plan, FollowUpVisitEntity visit, int visitNumber, AppointmentEntity appointment) {
        AppointmentEntity target = appointment;
        if (target == null) {
            target = appointmentRepository.findByTreatmentPlanIdAndFollowUpVisitNumber(plan.getId(), visitNumber).orElse(null);
        }

        if (target == null) {
            return;
        }

        target.updateDetails(
                target.getPatient(),
                target.getDoctor(),
                target.getService(),
                visit.getVisitDate(),
                target.getBookingMode(),
                target.getNotes()
        );
        target.setStatus(AppointmentStatus.COMPLETED);
        target.setPatientAttended(true);
        boolean hasPayments = visit.getPayments().stream()
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(BigDecimal.ZERO) > 0;
        if (hasPayments) {
            target.setPaymentCollected(true);
        }
    }

    private int findNextVisitNumber(TreatmentPlanEntity plan) {
        Set<Integer> usedVisitNumbers = plan.getFollowUpVisits().stream()
                .map(FollowUpVisitEntity::getVisitNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        int planned = plan.getPlannedFollowups() != null ? plan.getPlannedFollowups() : 0;
        int searchLimit = Math.max(planned, usedVisitNumbers.size() + 1);

        for (int candidate = 1; candidate <= searchLimit; candidate++) {
            if (!usedVisitNumbers.contains(candidate)) {
                return candidate;
            }
        }

        return usedVisitNumbers.isEmpty()
                ? 1
                : usedVisitNumbers.stream().max(Integer::compareTo).orElse(0) + 1;
    }

    /**
     * Record a direct payment against a treatment plan.
     */
    @Transactional
    public TreatmentPlanResponse recordDirectPayment(
            Long treatmentPlanId,
            TreatmentPlanPaymentRequest request,
            Long staffId,
            String staffName
    ) {
        Long tenantId = tenantContextHolder.requireTenantId();
        TreatmentPlanEntity treatmentPlan = treatmentPlanRepository.findByIdAndTenantId(treatmentPlanId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));
        
        // Force loading of direct payments within transaction to ensure accurate balance calculation
        treatmentPlan.getDirectPayments().size();

        // Validate payment amount doesn't exceed remaining balance
        BigDecimal remainingBalance = treatmentPlan.calculateRemainingBalance();
        if (request.amount().compareTo(remainingBalance) > 0) {
            throw new RuntimeException("Payment amount exceeds remaining balance");
        }

        // Create the payment entity
        TreatmentPlanPaymentEntity payment = new TreatmentPlanPaymentEntity(
                treatmentPlan,
                request.amount(),
                treatmentPlan.getCurrency(),
                request.paymentMethod(),
                request.paymentDate(),
                request.transactionReference(),
                request.notes(),
                staffId,
                staffName
        );

        // Save the payment
        treatmentPlanPaymentRepository.save(payment);

        // Add the payment to the in-memory collection so calculateRemainingBalance() includes it
        treatmentPlan.getDirectPayments().add(payment);

        // Log the payment in audit trail
        TreatmentPlanAuditLogEntity auditLog = new TreatmentPlanAuditLogEntity(
                treatmentPlan,
                staffId,
                staffName,
                "PAYMENT_RECORDED",
                null,
                String.format("Payment of %s %s recorded via %s",
                    request.amount(), treatmentPlan.getCurrency(), request.paymentMethod()),
                request.notes()
        );
        treatmentPlan.getAuditLogs().add(auditLog);

        // Save the treatment plan (updatedAt will be set automatically by @PreUpdate)
        treatmentPlanRepository.save(treatmentPlan);

        return toResponse(treatmentPlan);
    }

    /**
     * Get all payments for a treatment plan.
     */
    public List<TreatmentPlanPaymentResponse> getTreatmentPlanPayments(Long treatmentPlanId) {
        List<TreatmentPlanPaymentEntity> payments = treatmentPlanPaymentRepository
                .findByTreatmentPlanIdOrderByPaymentDateDesc(treatmentPlanId);

        return payments.stream()
                .map(this::mapPaymentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map payment entity to response DTO with currency conversion.
     */
    private TreatmentPlanPaymentResponse mapPaymentToResponse(TreatmentPlanPaymentEntity payment) {
        String clinicCurrency = getClinicCurrency();
        BigDecimal convertedAmount = null;
        String convertedCurrency = null;

        // Convert currency if different from clinic currency
        if (!payment.getCurrency().equals(clinicCurrency)) {
            try {
                convertedAmount = currencyConversionService.convert(
                        payment.getAmount(),
                        payment.getCurrency(),
                        clinicCurrency
                );
                convertedCurrency = clinicCurrency;
            } catch (Exception e) {
                // If conversion fails, use original values
                convertedAmount = payment.getAmount();
                convertedCurrency = payment.getCurrency();
            }
        }

        return new TreatmentPlanPaymentResponse(
                payment.getId(),
                payment.getTreatmentPlan().getId(),
                payment.getAmount(),
                payment.getCurrency(),
                convertedAmount,
                convertedCurrency,
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getPaymentDate(),
                payment.getNotes(),
                payment.getRecordedByStaffId(),
                payment.getRecordedByStaffName(),
                payment.getCreatedAt()
        );
    }

    /**
     * Get the clinic's default currency from settings.
     * Defaults to USD if not configured.
     */
    private String getClinicCurrency() {
        ClinicSettingsEntity settings = requireSettings();
        String currency = settings.getCurrency();
        return (currency != null && !currency.isBlank()) ? currency : "USD";
    }

    private ClinicSettingsEntity requireSettings() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return clinicSettingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic settings not configured"));
    }
}
