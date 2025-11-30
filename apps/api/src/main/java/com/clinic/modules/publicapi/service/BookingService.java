package com.clinic.modules.publicapi.service;

import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentMode;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.appointment.AppointmentStatus;
import com.clinic.modules.core.doctor.DoctorAvailabilityEntity;
import com.clinic.modules.core.doctor.DoctorAvailabilityRepository;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.admin.dto.AppointmentResponse;
import com.clinic.modules.admin.service.NotificationService;
import com.clinic.config.ClinicTimezoneConfig;
import com.clinic.modules.core.email.EmailService;
import com.clinic.modules.core.patient.PatientContactUtils;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.publicapi.dto.BookingRequest;
import com.clinic.modules.publicapi.dto.BookingResponse;
import com.clinic.modules.publicapi.dto.GuestBookingRequest;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class BookingService {

    private final ClinicServiceRepository serviceRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final ClinicSettingsRepository clinicSettingsRepository;
    private final ClinicTimezoneConfig clinicTimezoneConfig;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;
    private static final int DEFAULT_SLOT_DURATION_MINUTES = 30;

    public BookingService(ClinicServiceRepository serviceRepository,
                          DoctorRepository doctorRepository,
                          PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          DoctorAvailabilityRepository availabilityRepository,
                          EmailService emailService,
                          NotificationService notificationService,
                          ClinicSettingsRepository clinicSettingsRepository,
                          ClinicTimezoneConfig clinicTimezoneConfig,
                          TenantContextHolder tenantContextHolder,
                          TenantService tenantService) {
        this.serviceRepository = serviceRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.clinicSettingsRepository = clinicSettingsRepository;
        this.clinicTimezoneConfig = clinicTimezoneConfig;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long authenticatedPatientId) {
        if (authenticatedPatientId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required to book appointments");
        }

        Long tenantId = tenantContextHolder.requireTenantId();

        // Validate service belongs to current tenant
        ClinicServiceEntity service = serviceRepository.findBySlugAndTenantId(request.serviceSlug(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        // Validate doctor belongs to current tenant
        DoctorEntity doctor = resolveDoctor(request, service);

        Instant slotStart;
        try {
            slotStart = Instant.parse(request.slot());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slot timestamp", ex);
        }
        int slotDurationMinutes = getDefaultSlotDurationMinutes();
        validateSlotAlignment(slotStart, slotDurationMinutes);
        Instant slotEnd = slotStart.plus(Duration.ofMinutes(slotDurationMinutes));

        ensureSlotWithinAvailability(doctor, slotStart, slotEnd);

        boolean slotTaken = appointmentRepository.existsActiveByDoctorAndTimeRange(tenantId, doctor.getId(), slotStart, slotEnd, DEFAULT_SLOT_DURATION_MINUTES);
        if (slotTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected slot is no longer available");
        }

        // Validate patient belongs to current tenant
        PatientEntity patient = patientRepository.findByIdAndTenantId(authenticatedPatientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient account not found"));

        AppointmentMode bookingMode = parseBookingMode(request.bookingMode());

        AppointmentEntity appointment = new AppointmentEntity(
                patient,
                doctor,
                service,
                slotStart,
                AppointmentStatus.SCHEDULED,
                bookingMode,
                request.notes()
        );
        
        // Assign current tenant
        TenantEntity tenant = tenantService.requireTenant(tenantId);
        appointment.setTenant(tenant);
        
        appointment.setSlotDurationMinutes(slotDurationMinutes);
        AppointmentEntity saved = appointmentRepository.save(appointment);

        // Send confirmation email
        sendConfirmationEmail(saved, patient, doctor, service);

        // Broadcast real-time notification
        notificationService.broadcastNewAppointment(toAppointmentResponse(saved, ZoneId.systemDefault()));

        return new BookingResponse(saved.getId().toString(), saved.getScheduledAt().toString());
    }

    /**
     * Create a guest booking without authentication.
     * Creates or finds a guest patient by phone number.
     */
    @Transactional
    public BookingResponse createGuestBooking(GuestBookingRequest request) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        // Validate and normalize phone number
        String normalizedPhone = normalizePhoneOrThrow(request.phoneNumber());
        String normalizedEmail = normalizeOptionalEmail(request.guestEmail());

        // Validate service belongs to current tenant
        ClinicServiceEntity service = serviceRepository.findBySlugAndTenantId(request.serviceSlug(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        // Validate doctor belongs to current tenant (auto-assign if not provided)
        DoctorEntity doctor = resolveDoctorForGuest(request.doctorId(), service);

        // Parse and validate slot
        Instant slotStart;
        try {
            slotStart = Instant.parse(request.slot());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slot timestamp", ex);
        }
        int slotDurationMinutes = getDefaultSlotDurationMinutes();
        validateSlotAlignment(slotStart, slotDurationMinutes);
        Instant slotEnd = slotStart.plus(Duration.ofMinutes(slotDurationMinutes));

        // Check doctor availability
        ensureSlotWithinAvailability(doctor, slotStart, slotEnd);

        // Check if slot is already taken
        boolean slotTaken = appointmentRepository.existsActiveByDoctorAndTimeRange(tenantId, doctor.getId(), slotStart, slotEnd, DEFAULT_SLOT_DURATION_MINUTES);
        if (slotTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected slot is no longer available");
        }

        // Find or create guest patient by phone/email
        PatientEntity patient = findOrCreateGuestPatient(normalizedPhone, normalizedEmail, request.guestName());

        // Parse consultation type
        AppointmentMode consultationType = parseBookingMode(request.consultationType());

        // Create appointment
        AppointmentEntity appointment = new AppointmentEntity(
                patient,
                doctor,
                service,
                slotStart,
                AppointmentStatus.SCHEDULED,
                consultationType,
                request.notes()
        );
        
        // Assign current tenant
        TenantEntity tenant = tenantService.requireTenant(tenantId);
        appointment.setTenant(tenant);
        
        appointment.setSlotDurationMinutes(slotDurationMinutes);
        AppointmentEntity saved = appointmentRepository.save(appointment);

        // Send confirmation email
        sendConfirmationEmail(saved, patient, doctor, service);

        // Broadcast real-time notification
        notificationService.broadcastNewAppointment(toAppointmentResponse(saved, ZoneId.systemDefault()));

        return new BookingResponse(saved.getId().toString(), saved.getScheduledAt().toString());
    }

    private DoctorEntity resolveDoctorForGuest(Long doctorId, ClinicServiceEntity service) {
        Long tenantId = tenantContextHolder.requireTenantId();
        if (doctorId == null) {
            // Auto-assign first available doctor for the service
            return doctorRepository.findAllByServiceSlug(service.getSlug(), tenantId).stream()
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No doctor available for service"));
        }

        DoctorEntity doctor = doctorRepository.findByIdAndTenantId(doctorId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        if (!doctor.getServices().contains(service)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor does not provide the requested service");
        }

        return doctor;
    }

    private PatientEntity findOrCreateGuestPatient(String normalizedPhone,
                                                   String normalizedEmail,
                                                   String rawGuestName) {
        NameParts nameParts = extractGuestNameParts(rawGuestName);

        PatientEntity patient = null;
        if (normalizedEmail != null) {
            patient = patientRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        }
        if (patient == null) {
            patient = patientRepository.findByPhone(normalizedPhone).orElse(null);
        }

        if (patient == null) {
            return createGuestPatient(normalizedPhone, normalizedEmail, nameParts);
        }

        boolean shouldUpdateName = shouldRefreshGuestName(patient, nameParts);
        String updatedFirstName = shouldUpdateName ? nameParts.firstName() : patient.getFirstName();
        String updatedLastName = shouldUpdateName ? nameParts.lastName() : patient.getLastName();

        String updatedEmail = patient.getEmail();
        if (normalizedEmail != null && (updatedEmail == null || isPlaceholderEmail(updatedEmail))) {
            updatedEmail = normalizedEmail;
        }

        String updatedPhone = patient.getPhone();
        if (!normalizedPhone.equals(updatedPhone)) {
            updatedPhone = normalizedPhone;
        }

        if (!Objects.equals(updatedFirstName, patient.getFirstName())
                || !Objects.equals(updatedLastName, patient.getLastName())
                || !emailsMatch(updatedEmail, patient.getEmail())
                || !Objects.equals(updatedPhone, patient.getPhone())) {
            patient.updateDetails(
                    updatedFirstName,
                    updatedLastName,
                    updatedEmail,
                    updatedPhone,
                    patient.getDateOfBirth(),
                    patient.getNotes(),
                    patient.getDriveFolderUrl()
            );
        }

        return patient;
    }

    private PatientEntity createGuestPatient(String normalizedPhone,
                                             String normalizedEmail,
                                             NameParts nameParts) {
        String emailToUse = normalizedEmail != null ? normalizedEmail : buildPlaceholderEmail(normalizedPhone);
        PatientEntity patient = new PatientEntity(
                nameParts.firstName(),
                nameParts.lastName(),
                emailToUse,
                normalizedPhone
        );
        return patientRepository.save(patient);
    }

    private NameParts extractGuestNameParts(String rawName) {
        if (rawName != null && !rawName.isBlank()) {
            String[] parts = rawName.trim().split("\\s+", 2);
            String firstName = capitalize(parts[0]);
            String lastName = parts.length > 1 ? capitalize(parts[1]) : "";
            return new NameParts(firstName, lastName);
        }
        return new NameParts("Guest", "");
    }

    private boolean shouldRefreshGuestName(PatientEntity patient, NameParts provided) {
        if (provided == null) {
            return false;
        }
        boolean patientIsPlaceholder = isGuestProfile(patient);
        boolean providedHasRealFirstName = provided.firstName() != null
                && !provided.firstName().isBlank()
                && !"Guest".equalsIgnoreCase(provided.firstName());
        boolean providedHasLastName = provided.lastName() != null && !provided.lastName().isBlank();

        if (!patientIsPlaceholder) {
            return false;
        }

        return providedHasRealFirstName || providedHasLastName;
    }

    private boolean isGuestProfile(PatientEntity patient) {
        boolean firstNameIsGuest = patient.getFirstName() != null
                && patient.getFirstName().equalsIgnoreCase("Guest");
        boolean lastNameMissing = patient.getLastName() == null || patient.getLastName().isBlank();
        return firstNameIsGuest && lastNameMissing;
    }

    private boolean isPlaceholderEmail(String email) {
        return email != null && email.endsWith("@guest.clinic");
    }

    private String buildPlaceholderEmail(String phone) {
        return "guest." + phone.replaceAll("[^0-9]", "") + "@guest.clinic";
    }

    private boolean emailsMatch(String first, String second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.equalsIgnoreCase(second);
    }

    private DoctorEntity resolveDoctor(BookingRequest request, ClinicServiceEntity service) {
        Long tenantId = tenantContextHolder.requireTenantId();
        if (request.doctorId() == null) {
            return doctorRepository.findAllByServiceSlug(service.getSlug(), tenantId).stream()
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No doctor available for service"));
        }

        DoctorEntity doctor = doctorRepository.findByIdAndTenantId(request.doctorId(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        if (!doctor.getServices().contains(service)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor does not provide the requested service");
        }

        return doctor;
    }

    private void ensureSlotWithinAvailability(DoctorEntity doctor, Instant slotStart, Instant slotEnd) {
        LocalDateTime slotStartDateTime = LocalDateTime.ofInstant(slotStart, ZoneOffset.UTC);
        LocalDate slotDate = slotStartDateTime.toLocalDate();
        LocalTime slotStartTime = slotStartDateTime.toLocalTime();
        LocalTime slotEndTime = LocalDateTime.ofInstant(slotEnd, ZoneOffset.UTC).toLocalTime();

        List<DoctorAvailabilityEntity> oneTime = availabilityRepository.findByDoctorIdAndSpecificDate(doctor.getId(), slotDate);
        List<DoctorAvailabilityEntity> weekly = availabilityRepository.findByDoctorIdAndRecurringWeeklyTrueAndDayOfWeek(
                doctor.getId(),
                slotDate.getDayOfWeek()
        );

        boolean matches = oneTime.stream().anyMatch(a -> coversSlot(a, slotStartTime, slotEndTime))
                || weekly.stream().anyMatch(a -> coversSlot(a, slotStartTime, slotEndTime));

        if (!matches) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not available for the selected slot");
        }
    }

    private boolean coversSlot(DoctorAvailabilityEntity availability, LocalTime slotStartTime, LocalTime slotEndTime) {
        return !availability.getStartTime().isAfter(slotStartTime) && !availability.getEndTime().isBefore(slotEndTime);
    }

    private void validateSlotAlignment(Instant slotStart, int baseSlotMinutes) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(slotStart, ZoneOffset.UTC);
        if (dateTime.getSecond() != 0 || dateTime.getNano() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slots must start at exact minutes.");
        }
        int alignment = Math.max(1, greatestCommonDivisor(baseSlotMinutes, 60));
        if (dateTime.getMinute() % alignment != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot start time must align with configured interval.");
        }
    }

    private int getDefaultSlotDurationMinutes() {
        ClinicSettingsEntity settings = requireSettings();
        Integer duration = settings.getSlotDurationMinutes();
        if (duration != null && duration >= 5 && duration <= 240) {
            return duration;
        }
        return DEFAULT_SLOT_DURATION_MINUTES;
    }

    private String getVirtualConsultationMeetingLink() {
        ClinicSettingsEntity settings = requireSettings();
        String link = settings.getVirtualConsultationMeetingLink();
        return (link != null && !link.isBlank()) ? link : null;
    }

    private ClinicSettingsEntity requireSettings() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return clinicSettingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic settings not configured"));
    }

    private int greatestCommonDivisor(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        if (a == 0) {
            return b == 0 ? 1 : b;
        }
        if (b == 0) {
            return a;
        }
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    private String normalizeEmailOrThrow(String email) {
        try {
            return PatientContactUtils.normalizeEmail(email);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private String normalizeOptionalEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return normalizeEmailOrThrow(trimmed);
    }

    private String normalizePhoneOrThrow(String phone) {
        try {
            return PatientContactUtils.normalizePhone(phone);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private AppointmentMode parseBookingMode(String mode) {
        try {
            return AppointmentMode.from(mode);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private void sendConfirmationEmail(
            AppointmentEntity appointment,
            PatientEntity patient,
            DoctorEntity doctor,
            ClinicServiceEntity service
    ) {
        try {
            String patientEmail = patient.getEmail();
            if (patientEmail == null || patientEmail.isBlank()) {
                return; // Skip email if patient has no email
            }

            String patientName = patient.getFirstName() + " " + patient.getLastName();
            String doctorName = doctor.getFullName();
            String serviceName = service.getNameEn();

            // Format date and time in clinic timezone
            ZoneId clinicZone = clinicTimezoneConfig.toZoneId();
            ZonedDateTime appointmentStartTime = appointment.getScheduledAt().atZone(clinicZone);
            int slotDurationMinutes = appointment.getSlotDurationMinutes() != null
                    ? appointment.getSlotDurationMinutes()
                    : getDefaultSlotDurationMinutes();
            ZonedDateTime appointmentEndTime = appointmentStartTime.plusMinutes(slotDurationMinutes);

            // Check if this is a virtual consultation
        if (appointment.getBookingMode() == AppointmentMode.VIRTUAL_CONSULTATION) {
            // Send special virtual consultation email with Google Meet link and calendar
            emailService.sendVirtualConsultationConfirmation(
                    patientEmail,
                    patientName,
                    doctorName,
                    serviceName,
                    appointmentStartTime,
                    appointmentEndTime,
                    getVirtualConsultationMeetingLink()
            );
            } else {
                // Send regular clinic visit confirmation
                String appointmentDate = appointmentStartTime.toLocalDate().toString();
                String appointmentTimeStr = appointmentStartTime.toLocalTime().toString();
                String consultationType = appointment.getBookingMode().displayName();

                emailService.sendAppointmentConfirmation(
                        patientEmail,
                        patientName,
                        doctorName,
                        serviceName,
                        appointmentDate,
                        appointmentTimeStr,
                        consultationType
                );
            }
        } catch (Exception e) {
            // Log but don't fail the booking if email fails
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    private record NameParts(String firstName, String lastName) {
    }

    private AppointmentResponse toAppointmentResponse(AppointmentEntity entity, ZoneId zoneId) {
        String patientName = entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName();
        return new AppointmentResponse(
                entity.getId(),
                patientName,
                entity.getDoctor().getFullNameEn(),
                entity.getService().getNameEn(),
                entity.getScheduledAt().atZone(zoneId).toOffsetDateTime(),
                entity.getStatus().name(),
                entity.getBookingMode().name(),
                entity.getTreatmentPlan() != null ? entity.getTreatmentPlan().getId() : null,
                entity.getFollowUpVisitNumber(),
                entity.isPaymentCollected(),
                entity.getPatientAttended(),
                entity.getSlotDurationMinutes(),
                entity.getPaymentAmount(),
                entity.getPaymentMethod() != null ? entity.getPaymentMethod().name() : null,
                entity.getPaymentCurrency()
        );
    }
}
