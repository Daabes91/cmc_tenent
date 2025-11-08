package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.AppointmentAdminDetailResponse;
import com.clinic.modules.admin.dto.AppointmentAdminUpsertRequest;
import com.clinic.modules.admin.dto.AppointmentResponse;
import com.clinic.config.ClinicTimezoneConfig;
import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentMode;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.appointment.AppointmentStatus;
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
import com.clinic.modules.core.treatment.PaymentMethod;
import com.clinic.modules.core.treatment.TreatmentPlanEntity;
import com.clinic.modules.core.treatment.TreatmentPlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ClinicServiceRepository serviceRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final ClinicSettingsRepository clinicSettingsRepository;
    private final ClinicTimezoneConfig clinicTimezoneConfig;
    private final TenantContextHolder tenantContextHolder;

    private static final int DEFAULT_SLOT_DURATION_MINUTES = 30;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              ClinicServiceRepository serviceRepository,
                              TreatmentPlanRepository treatmentPlanRepository,
                              EmailService emailService,
                              NotificationService notificationService,
                              ClinicSettingsRepository clinicSettingsRepository,
                              ClinicTimezoneConfig clinicTimezoneConfig,
                              TenantContextHolder tenantContextHolder) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.serviceRepository = serviceRepository;
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.clinicSettingsRepository = clinicSettingsRepository;
        this.clinicTimezoneConfig = clinicTimezoneConfig;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> fetchAppointments(String filter, Long doctorId, Long patientId, ZoneId zoneId, Pageable pageable) {
        // Backward compatibility method - delegate to advanced filtering
        return fetchAppointmentsWithAdvancedFilters(
            filter, null, doctorId, patientId, null, null, null, null, null, null, null, zoneId, pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> fetchAppointmentsWithAdvancedFilters(
            String filter, String status, Long doctorId, Long patientId, Long serviceId, 
            String bookingMode, Boolean paymentCollected, Boolean patientAttended, 
            String fromDate, String toDate, String search, ZoneId zoneId, Pageable pageable) {
        
        // For now, use simple filtering until repository-level queries are introduced.
        List<AppointmentEntity> allAppointments;

        boolean hasCustomDateRange = (fromDate != null && !fromDate.isBlank()) || (toDate != null && !toDate.isBlank());
        Instant rangeStart = null;
        Instant rangeEnd = null;

        if (hasCustomDateRange) {
            rangeStart = fromDate != null && !fromDate.isBlank()
                    ? parseStartDate(fromDate, zoneId)
                    : Instant.EPOCH;
            rangeEnd = toDate != null && !toDate.isBlank()
                    ? parseEndDate(toDate, zoneId)
                    : Instant.now().plus(5, java.time.temporal.ChronoUnit.YEARS);

            if (!rangeEnd.isAfter(rangeStart)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate must be after fromDate.");
            }

            allAppointments = appointmentRepository.findByScheduledBetween(rangeStart, rangeEnd);
        } else if ("upcoming".equalsIgnoreCase(filter)) {
            Instant now = Instant.now();
            allAppointments = appointmentRepository.findUpcomingAfter(now);
        } else if ("today".equalsIgnoreCase(filter)) {
            Instant startOfDay = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            Instant endOfDay = startOfDay.plus(1, java.time.temporal.ChronoUnit.DAYS);
            allAppointments = appointmentRepository.findByScheduledBetween(startOfDay, endOfDay);
        } else if ("week".equalsIgnoreCase(filter)) {
            Instant now = Instant.now();
            Instant weekFromNow = now.plus(7, java.time.temporal.ChronoUnit.DAYS);
            allAppointments = appointmentRepository.findByScheduledBetween(now, weekFromNow);
        } else {
            // Default: get recent appointments
            Instant start = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
            Instant end = Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS);
            allAppointments = appointmentRepository.findByScheduledBetween(start, end);
        }

        // Apply filters
        final Instant finalRangeStart = rangeStart;
        final Instant finalRangeEnd = rangeEnd;

        allAppointments = allAppointments.stream()
            .filter(appointment -> {
                // Status filter
                if (status != null && !status.equalsIgnoreCase("all")) {
                    try {
                        AppointmentStatus statusEnum = AppointmentStatus.valueOf(status.toUpperCase());
                        if (appointment.getStatus() != statusEnum) return false;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                }
                
                // Doctor filter
                if (doctorId != null && (appointment.getDoctor() == null || !appointment.getDoctor().getId().equals(doctorId))) {
                    return false;
                }
                
                // Patient filter
                if (patientId != null && (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patientId))) {
                    return false;
                }
                
                // Service filter
                if (serviceId != null && (appointment.getService() == null || !appointment.getService().getId().equals(serviceId))) {
                    return false;
                }
                
                // Booking mode filter
                if (bookingMode != null && !bookingMode.equalsIgnoreCase("all")) {
                    try {
                        AppointmentMode modeEnum = AppointmentMode.valueOf(bookingMode.toUpperCase());
                        if (appointment.getBookingMode() != modeEnum) return false;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                }
                
                // Payment collected filter
                if (paymentCollected != null && appointment.isPaymentCollected() != paymentCollected) {
                    return false;
                }
                
                // Patient attended filter
                if (patientAttended != null) {
                    Boolean attended = appointment.getPatientAttended();
                    if (!patientAttended.equals(attended)) return false;
                }
                
                // Date range filter (inclusive of toDate, exclusive of next day)
                Instant scheduledAt = appointment.getScheduledAt();
                if (finalRangeStart != null && scheduledAt.isBefore(finalRangeStart)) {
                    return false;
                }
                if (finalRangeEnd != null && !scheduledAt.isBefore(finalRangeEnd)) {
                    return false;
                }

                // Search filter
                if (search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase().trim();
                    String patientName = appointment.getPatient() != null ? 
                        (appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName()).toLowerCase() : "";
                    String doctorName = appointment.getDoctor() != null ? 
                        appointment.getDoctor().getFullName().toLowerCase() : "";
                    String serviceName = appointment.getService() != null ? 
                        appointment.getService().getNameEn().toLowerCase() : "";
                    
                    if (!patientName.contains(searchLower) && 
                        !doctorName.contains(searchLower) && 
                        !serviceName.contains(searchLower)) {
                        return false;
                    }
                }
                
                return true;
            })
            .toList();

        // Convert to DTOs
        List<AppointmentResponse> allDtos = allAppointments.stream()
                .map(entity -> toDto(entity, zoneId))
                .toList();

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allDtos.size());

        List<AppointmentResponse> pageContent = start >= allDtos.size()
            ? List.of()
            : allDtos.subList(start, end);

        return new PageImpl<>(pageContent, pageable, allDtos.size());
    }

    private Instant parseStartDate(String value, ZoneId zoneId) {
        try {
            return LocalDate.parse(value).atStartOfDay(zoneId).toInstant();
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid fromDate format. Expected yyyy-MM-dd.", ex);
        }
    }

    private Instant parseEndDate(String value, ZoneId zoneId) {
        try {
            return LocalDate.parse(value)
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant();
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid toDate format. Expected yyyy-MM-dd.", ex);
        }
    }

    private AppointmentResponse toDto(AppointmentEntity entity, ZoneId zoneId) {
        OffsetDateTime scheduledAt = OffsetDateTime.ofInstant(entity.getScheduledAt(), zoneId);
        String patientName = Optional.ofNullable(entity.getPatient())
                .map(patient -> patient.getFirstName() + " " + patient.getLastName())
                .orElse("Unknown patient");
        String doctorName = Optional.ofNullable(entity.getDoctor())
                .map(doctor -> doctor.getFullName())
                .orElse("Assigned soon");
        String serviceName = Optional.ofNullable(entity.getService())
                .map(service -> service.getNameEn())
                .orElse("General consultation");
        Long treatmentPlanId = Optional.ofNullable(entity.getTreatmentPlan())
                .map(TreatmentPlanEntity::getId)
                .orElse(null);
        Integer followUpVisitNumber = entity.getFollowUpVisitNumber();
        String paymentMethod = Optional.ofNullable(entity.getPaymentMethod())
                .map(PaymentMethod::name)
                .orElse(null);
        return new AppointmentResponse(
                entity.getId(),
                patientName,
                doctorName,
                serviceName,
                scheduledAt,
                entity.getStatus().name(),
                entity.getBookingMode().name(),
                treatmentPlanId,
                followUpVisitNumber,
                entity.isPaymentCollected(),
                entity.getPatientAttended(),
                entity.getSlotDurationMinutes(),
                entity.getPaymentAmount(),
                paymentMethod,
                entity.getPaymentCurrency()
        );
    }

    @Transactional(readOnly = true)
    public AppointmentAdminDetailResponse getAppointment(Long id) {
        AppointmentEntity appointment = getAppointmentOrThrow(id);
        return toDetailResponse(appointment);
    }

    @Transactional
    public AppointmentAdminDetailResponse createAppointment(AppointmentAdminUpsertRequest request) {
        PatientEntity patient = getPatient(request.patientId());
        DoctorEntity doctor = getDoctor(request.doctorId());
        ClinicServiceEntity service = getService(request.serviceId());
        ensureDoctorProvidesService(doctor, service);

        Instant scheduledAt = parseScheduledAt(request.scheduledAt());
        int slotDurationMinutes = resolveSlotDurationMinutes(request.slotDurationMinutes());
        ensureSlotAvailable(doctor.getId(), scheduledAt, slotDurationMinutes, null);

        AppointmentMode bookingMode = parseBookingMode(request.bookingMode());

        TreatmentPlanEntity treatmentPlan = null;
        Integer followUpVisitNumber = request.followUpVisitNumber();
        if (request.treatmentPlanId() != null) {
            treatmentPlan = getTreatmentPlan(request.treatmentPlanId());
            validatePlanAssociation(treatmentPlan, patient, doctor, service);
            if (followUpVisitNumber == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "followUpVisitNumber is required when linking to a treatment plan.");
            }
            ensureFollowUpNumberAvailable(treatmentPlan, followUpVisitNumber, null);
        }

        AppointmentEntity appointment = new AppointmentEntity(
                patient,
                doctor,
                service,
                scheduledAt,
                AppointmentStatus.SCHEDULED,
                bookingMode,
                normalize(request.notes())
        );
        boolean paymentCollected = Boolean.TRUE.equals(request.paymentCollected());
        appointment.setTreatmentPlan(treatmentPlan);
        appointment.setFollowUpVisitNumber(followUpVisitNumber);
        appointment.setPaymentCollected(paymentCollected);
        appointment.setPatientAttended(request.patientAttended());
        appointment.setSlotDurationMinutes(slotDurationMinutes);
        if (treatmentPlan == null) {
            applyStandalonePaymentDetails(appointment, request, paymentCollected);
        } else {
            clearStandalonePaymentDetails(appointment);
        }

        AppointmentEntity saved = appointmentRepository.save(appointment);

        // Broadcast notification to connected clients
        notificationService.broadcastNewAppointment(toDto(saved, ZoneId.systemDefault()));

        return toDetailResponse(saved);
    }

    @Transactional
    public AppointmentAdminDetailResponse updateAppointment(Long id, AppointmentAdminUpsertRequest request) {
        AppointmentEntity appointment = getAppointmentOrThrow(id);

        PatientEntity patient = getPatient(request.patientId());
        DoctorEntity doctor = getDoctor(request.doctorId());
        ClinicServiceEntity service = getService(request.serviceId());
        ensureDoctorProvidesService(doctor, service);

        Instant scheduledAt = parseScheduledAt(request.scheduledAt());
        int slotDurationMinutes = resolveSlotDurationMinutes(request.slotDurationMinutes(), appointment.getSlotDurationMinutes());
        ensureSlotAvailable(doctor.getId(), scheduledAt, slotDurationMinutes, appointment.getId());

        AppointmentMode bookingMode = parseBookingMode(request.bookingMode());

        TreatmentPlanEntity treatmentPlan = appointment.getTreatmentPlan();
        Integer followUpVisitNumber = appointment.getFollowUpVisitNumber();

        if (request.treatmentPlanId() != null) {
            treatmentPlan = getTreatmentPlan(request.treatmentPlanId());
        }

        if (treatmentPlan != null) {
            if (request.followUpVisitNumber() != null) {
                followUpVisitNumber = request.followUpVisitNumber();
            }
            validatePlanAssociation(treatmentPlan, patient, doctor, service);
            ensureFollowUpNumberAvailable(treatmentPlan, followUpVisitNumber, appointment.getId());
        } else {
            followUpVisitNumber = null;
        }

        appointment.updateDetails(
                patient,
                doctor,
                service,
                scheduledAt,
                bookingMode,
                normalize(request.notes())
        );
        appointment.setTreatmentPlan(treatmentPlan);
        appointment.setFollowUpVisitNumber(followUpVisitNumber);
        boolean effectivePaymentCollected = request.paymentCollected() != null
                ? Boolean.TRUE.equals(request.paymentCollected())
                : appointment.isPaymentCollected();
        appointment.setPaymentCollected(effectivePaymentCollected);
        appointment.setPatientAttended(request.patientAttended());
        appointment.setSlotDurationMinutes(slotDurationMinutes);
        if (treatmentPlan == null) {
            applyStandalonePaymentDetails(appointment, request, effectivePaymentCollected);
        } else {
            clearStandalonePaymentDetails(appointment);
        }

        return toDetailResponse(appointment);
    }

    @Transactional
    public void deleteAppointment(Long id) {
        AppointmentEntity appointment = getAppointmentOrThrow(id);
        appointmentRepository.delete(appointment);
    }

    @Transactional
    public AppointmentAdminDetailResponse approveAppointment(Long id, String notes) {
        AppointmentEntity appointment = getAppointmentOrThrow(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled appointments cannot be approved");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed appointments cannot be modified");
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        if (notes != null) {
            appointment.updateDetails(
                    appointment.getPatient(),
                    appointment.getDoctor(),
                    appointment.getService(),
                    appointment.getScheduledAt(),
                    appointment.getBookingMode(),
                    normalize(notes)
            );
        }

        // Send confirmation email when appointment is approved
        sendConfirmationEmail(appointment);

        return toDetailResponse(appointment);
    }

    @Transactional
    public AppointmentAdminDetailResponse declineAppointment(Long id, String notes) {
        AppointmentEntity appointment = getAppointmentOrThrow(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed appointments cannot be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (notes != null) {
            appointment.updateDetails(
                    appointment.getPatient(),
                    appointment.getDoctor(),
                    appointment.getService(),
                    appointment.getScheduledAt(),
                    appointment.getBookingMode(),
                    normalize(notes)
            );
        }

        // Send cancellation email when appointment is declined
        sendCancellationEmail(appointment);

        return toDetailResponse(appointment);
    }

    private AppointmentEntity getAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private PatientEntity getPatient(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }

    private DoctorEntity getDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    private ClinicServiceEntity getService(Long id) {
        return serviceRepository.findByIdAndTenantId(id, tenantContextHolder.requireTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    }

    private void ensureDoctorProvidesService(DoctorEntity doctor, ClinicServiceEntity service) {
        Set<ClinicServiceEntity> services = doctor.getServices();
        if (services == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor does not provide the selected service");
        }
        boolean provides = services.stream().anyMatch(s -> s.getId().equals(service.getId()));
        if (!provides) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor does not provide the selected service");
        }
    }

    private Instant parseScheduledAt(String input) {
        if (input == null || input.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled time is required");
        }
        try {
            return Instant.parse(input);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid scheduled time", ex);
        }
    }

    private void ensureSlotAvailable(Long doctorId, Instant scheduledAt, int slotDurationMinutes, Long excludeAppointmentId) {
        Instant slotEnd = scheduledAt.plus(Duration.ofMinutes(slotDurationMinutes));
        boolean conflict = excludeAppointmentId == null
                ? appointmentRepository.existsActiveByDoctorAndTimeRange(doctorId, scheduledAt, slotEnd, DEFAULT_SLOT_DURATION_MINUTES)
                : appointmentRepository.existsActiveByDoctorAndTimeRangeExcluding(doctorId, scheduledAt, slotEnd, excludeAppointmentId, DEFAULT_SLOT_DURATION_MINUTES);
        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor already has an appointment during this time");
        }
    }

    private int resolveSlotDurationMinutes(Integer requested) {
        return resolveSlotDurationMinutes(requested, null);
    }

    private int resolveSlotDurationMinutes(Integer requested, Integer existing) {
        if (requested != null && requested >= 5 && requested <= 240) {
            return requested;
        }
        if (existing != null && existing >= 5 && existing <= 240) {
            return existing;
        }
        ClinicSettingsEntity settings = requireSettings();
        Integer duration = settings.getSlotDurationMinutes();
        if (duration != null && duration >= 5 && duration <= 240) {
            return duration;
        }
        return DEFAULT_SLOT_DURATION_MINUTES;
    }

    private String resolveVirtualConsultationMeetingLink() {
        ClinicSettingsEntity settings = requireSettings();
        String link = settings.getVirtualConsultationMeetingLink();
        return (link != null && !link.isBlank()) ? link : null;
    }

    private ClinicSettingsEntity requireSettings() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return clinicSettingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic settings not configured"));
    }

    private AppointmentAdminDetailResponse toDetailResponse(AppointmentEntity entity) {
        AppointmentAdminDetailResponse.Reference patientRef = Optional.ofNullable(entity.getPatient())
                .map(patient -> new AppointmentAdminDetailResponse.Reference(
                        patient.getId(),
                        (patient.getFirstName() + " " + patient.getLastName()).trim()
                ))
                .orElse(null);

        AppointmentAdminDetailResponse.Reference doctorRef = Optional.ofNullable(entity.getDoctor())
                .map(doctor -> new AppointmentAdminDetailResponse.Reference(
                        doctor.getId(),
                        doctor.getFullName()
                ))
                .orElse(null);

        AppointmentAdminDetailResponse.Reference serviceRef = Optional.ofNullable(entity.getService())
                .map(service -> new AppointmentAdminDetailResponse.Reference(
                        service.getId(),
                        Optional.ofNullable(service.getNameEn()).orElse(service.getSlug())
                ))
                .orElse(null);

        String scheduled = Optional.ofNullable(entity.getScheduledAt())
                .map(Instant::toString)
                .orElse(null);
        String created = Optional.ofNullable(entity.getCreatedAt())
                .map(Instant::toString)
                .orElse(null);
        Long treatmentPlanId = Optional.ofNullable(entity.getTreatmentPlan())
                .map(TreatmentPlanEntity::getId)
                .orElse(null);
        Integer followUpVisitNumber = entity.getFollowUpVisitNumber();
        String paymentMethod = Optional.ofNullable(entity.getPaymentMethod())
                .map(PaymentMethod::name)
                .orElse(null);
        String paymentDate = Optional.ofNullable(entity.getPaymentDate())
                .map(Instant::toString)
                .orElse(null);

        return new AppointmentAdminDetailResponse(
                entity.getId(),
                patientRef,
                doctorRef,
                serviceRef,
                entity.getStatus().name(),
                entity.getBookingMode().name(),
                scheduled,
                created,
                entity.getNotes(),
                treatmentPlanId,
                followUpVisitNumber,
                entity.isPaymentCollected(),
                entity.getPatientAttended(),
                entity.getSlotDurationMinutes(),
                entity.getPaymentAmount(),
                paymentMethod,
                entity.getPaymentCurrency(),
                paymentDate,
                entity.getPaymentReference(),
                entity.getPaymentNotes()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void applyStandalonePaymentDetails(AppointmentEntity appointment,
                                               AppointmentAdminUpsertRequest request,
                                               boolean paymentCollected) {
        if (!paymentCollected) {
            clearStandalonePaymentDetails(appointment);
            return;
        }

        appointment.setPaymentAmount(request.paymentAmount());
        appointment.setPaymentMethod(request.paymentMethod());
        String fallbackCurrency = appointment.getPaymentCurrency() != null
                ? appointment.getPaymentCurrency()
                : "JOD";
        appointment.setPaymentCurrency(normalizeCurrency(request.paymentCurrency(), fallbackCurrency));
        appointment.setPaymentDate(parseOptionalInstant(request.paymentDate()));
        appointment.setPaymentReference(normalize(request.paymentReference()));
        appointment.setPaymentNotes(normalize(request.paymentNotes()));
    }

    private void clearStandalonePaymentDetails(AppointmentEntity appointment) {
        appointment.setPaymentAmount(null);
        appointment.setPaymentMethod(null);
        appointment.setPaymentCurrency(null);
        appointment.setPaymentDate(null);
        appointment.setPaymentReference(null);
        appointment.setPaymentNotes(null);
    }

    private String normalizeCurrency(String candidate, String fallback) {
        if (candidate == null || candidate.trim().isEmpty()) {
            return fallback;
        }
        String normalized = candidate.trim().toUpperCase();
        return normalized.length() > 3 ? normalized.substring(0, 3) : normalized;
    }

    private Instant parseOptionalInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paymentDate format. Expected ISO-8601.");
        }
    }

    private TreatmentPlanEntity getTreatmentPlan(Long id) {
        return treatmentPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));
    }

    private void validatePlanAssociation(TreatmentPlanEntity plan,
                                         PatientEntity patient,
                                         DoctorEntity doctor,
                                         ClinicServiceEntity service) {
        if (!plan.getPatient().getId().equals(patient.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected patient does not belong to the treatment plan.");
        }
        if (!plan.getDoctor().getId().equals(doctor.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected doctor does not match the treatment plan.");
        }
        if (!plan.getTreatmentType().getId().equals(service.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected treatment type does not match the plan.");
        }
    }

    private void ensureFollowUpNumberAvailable(TreatmentPlanEntity plan,
                                               Integer visitNumber,
                                               Long excludeAppointmentId) {
        if (visitNumber == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "followUpVisitNumber is required for treatment plan appointments.");
        }

        appointmentRepository.findByTreatmentPlanIdAndFollowUpVisitNumber(plan.getId(), visitNumber)
                .ifPresent(existing -> {
                    if (excludeAppointmentId != null && existing.getId().equals(excludeAppointmentId)) {
                        return;
                    }
                    if (existing.getStatus() != AppointmentStatus.CANCELLED) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Follow-up visit #" + visitNumber + " already has an active appointment."
                        );
                    }
                });
    }

    private AppointmentMode parseBookingMode(String input) {
        try {
            return AppointmentMode.from(input);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private void sendConfirmationEmail(AppointmentEntity appointment) {
        try {
            PatientEntity patient = appointment.getPatient();
            if (patient == null || patient.getEmail() == null || patient.getEmail().isBlank()) {
                return; // Skip if no patient or email
            }

            DoctorEntity doctor = appointment.getDoctor();
            ClinicServiceEntity service = appointment.getService();

            String patientName = patient.getFirstName() + " " + patient.getLastName();
            String doctorName = doctor != null ? doctor.getFullName() : "To be assigned";
            String serviceName = service != null ? service.getNameEn() : "General consultation";

            // Use clinic timezone for all appointments
            ZoneId clinicZone = clinicTimezoneConfig.toZoneId();
            ZonedDateTime appointmentStartTime = appointment.getScheduledAt().atZone(clinicZone);
            int slotDurationMinutes = resolveSlotDurationMinutes(null, appointment.getSlotDurationMinutes());
            ZonedDateTime appointmentEndTime = appointmentStartTime.plusMinutes(slotDurationMinutes);

            // Check if this is a virtual consultation
            if (appointment.getBookingMode() == AppointmentMode.VIRTUAL_CONSULTATION) {
                // Send special virtual consultation email with Google Meet link and calendar
                emailService.sendVirtualConsultationConfirmation(
                        patient.getEmail(),
                        patientName,
                        doctorName,
                        serviceName,
                        appointmentStartTime,
                        appointmentEndTime,
                        resolveVirtualConsultationMeetingLink()
                );
            } else {
                // Send regular clinic visit confirmation
                String appointmentDate = appointmentStartTime.toLocalDate().toString();
                String appointmentTime = appointmentStartTime.toLocalTime().toString();
                String consultationType = appointment.getBookingMode().displayName();

                emailService.sendAppointmentConfirmation(
                        patient.getEmail(),
                        patientName,
                        doctorName,
                        serviceName,
                        appointmentDate,
                        appointmentTime,
                        consultationType
                );
            }
        } catch (Exception e) {
            // Log but don't fail the approval if email fails
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    private void sendCancellationEmail(AppointmentEntity appointment) {
        try {
            PatientEntity patient = appointment.getPatient();
            if (patient == null || patient.getEmail() == null || patient.getEmail().isBlank()) {
                return; // Skip if no patient or email
            }

            DoctorEntity doctor = appointment.getDoctor();
            ClinicServiceEntity service = appointment.getService();

            String patientName = patient.getFirstName() + " " + patient.getLastName();
            String doctorName = doctor != null ? doctor.getFullName() : "To be assigned";
            String serviceName = service != null ? service.getNameEn() : "General consultation";
            String appointmentDate = appointment.getScheduledAt().toString().substring(0, 10);
            String appointmentTime = appointment.getScheduledAt().toString().substring(11, 16);

            emailService.sendAppointmentCancellation(
                    patient.getEmail(),
                    patientName,
                    doctorName,
                    serviceName,
                    appointmentDate,
                    appointmentTime
            );
        } catch (Exception e) {
            // Log but don't fail the cancellation if email fails
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public long getPendingAppointmentsCount() {
        Instant now = Instant.now();
        Instant futureLimit = now.plus(30, java.time.temporal.ChronoUnit.DAYS);
        return appointmentRepository.countByStatusAndScheduledAtBetween(
                AppointmentStatus.SCHEDULED,
                now,
                futureLimit
        );
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getRecentAppointments(String since, int limit, ZoneId zoneId) {
        Instant sinceInstant;
        if (since != null && !since.isEmpty()) {
            try {
                sinceInstant = Instant.parse(since);
            } catch (Exception ex) {
                // Default to last 5 minutes if invalid
                sinceInstant = Instant.now().minus(5, java.time.temporal.ChronoUnit.MINUTES);
            }
        } else {
            // Default to last 5 minutes
            sinceInstant = Instant.now().minus(5, java.time.temporal.ChronoUnit.MINUTES);
        }

        List<AppointmentEntity> recentAppointments = appointmentRepository.findRecentByCreatedAtAfter(
                sinceInstant,
                limit
        );

        return recentAppointments.stream()
                .map(entity -> toDto(entity, zoneId))
                .toList();
    }
}
