package com.clinic.modules.publicapi.service;

import com.clinic.config.ClinicTimezoneConfig;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.doctor.DoctorAvailabilityEntity;
import com.clinic.modules.core.doctor.DoctorAvailabilityRepository;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.publicapi.dto.AvailabilityRequest;
import com.clinic.modules.publicapi.dto.AvailabilitySlotResponse;
import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

    private final DoctorRepository doctorRepository;
    private final ClinicServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final ClinicTimezoneConfig timezoneConfig;
    private final ClinicSettingsRepository clinicSettingsRepository;
    private final TenantContextHolder tenantContextHolder;
    private static final int DEFAULT_SLOT_DURATION_MINUTES = 30;

    public AvailabilityService(DoctorRepository doctorRepository,
                               ClinicServiceRepository serviceRepository,
                               AppointmentRepository appointmentRepository,
                               DoctorAvailabilityRepository availabilityRepository,
                               ClinicTimezoneConfig timezoneConfig,
                               ClinicSettingsRepository clinicSettingsRepository,
                               TenantContextHolder tenantContextHolder) {
        this.doctorRepository = doctorRepository;
        this.serviceRepository = serviceRepository;
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.timezoneConfig = timezoneConfig;
        this.clinicSettingsRepository = clinicSettingsRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> computeAvailability(AvailabilityRequest request) {
        ClinicServiceEntity service = serviceRepository.findBySlugAndTenantId(request.serviceSlug(), tenantContextHolder.requireTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        List<DoctorEntity> doctors;
        if (request.doctorId() != null) {
            DoctorEntity doctor = doctorRepository.findById(request.doctorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
            if (!doctor.getServices().contains(service)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor does not provide the requested service");
            }
            doctors = List.of(doctor);
        } else {
            doctors = doctorRepository.findAllByServiceSlug(service.getSlug(), tenantContextHolder.requireTenantId());
        }

        LocalDate requestedDate = parseDateOrDefault(request.date());
        List<AvailabilitySlotResponse> slots = new ArrayList<>();

        for (DoctorEntity doctor : doctors) {
            slots.addAll(generateSlotsForDoctor(doctor, requestedDate));
        }

        return slots;
    }

    private List<AvailabilitySlotResponse> generateSlotsForDoctor(DoctorEntity doctor, LocalDate date) {
        List<DoctorAvailabilityEntity> specificAvailabilities = availabilityRepository.findByDoctorIdAndSpecificDate(doctor.getId(), date);
        List<DoctorAvailabilityEntity> weeklyAvailabilities = availabilityRepository.findByDoctorIdAndRecurringWeeklyTrueAndDayOfWeek(
                doctor.getId(),
                date.getDayOfWeek()
        );

        if (specificAvailabilities.isEmpty() && weeklyAvailabilities.isEmpty()) {
            return List.of();
        }

        List<AvailabilitySlotResponse> slots = new ArrayList<>();
        // Use configured clinic timezone for determining "today" and "now"
        ZoneId clinicZone = timezoneConfig.toZoneId();
        LocalDate today = LocalDate.now(clinicZone);
        Instant now = Instant.now();

        for (DoctorAvailabilityEntity availability : specificAvailabilities) {
            slots.addAll(generateSlotsFromAvailability(doctor, availability, availability.getSpecificDate(), today, now));
        }

        for (DoctorAvailabilityEntity availability : weeklyAvailabilities) {
            slots.addAll(generateSlotsFromAvailability(doctor, availability, date, today, now));
        }

        return slots;
    }

    private List<AvailabilitySlotResponse> generateSlotsFromAvailability(DoctorEntity doctor,
                                                                         DoctorAvailabilityEntity availability,
                                                                         LocalDate targetDate,
                                                                         LocalDate today,
                                                                         Instant nowInstant) {
        if (targetDate == null) {
            return List.of();
        }

        List<AvailabilitySlotResponse> slots = new ArrayList<>();
        LocalDateTime windowStart = LocalDateTime.of(targetDate, availability.getStartTime());
        LocalDateTime windowEnd = LocalDateTime.of(targetDate, availability.getEndTime());

        LocalDateTime pointer = windowStart;
        // Use configured clinic timezone instead of UTC
        ZoneId clinicZone = timezoneConfig.toZoneId();
        int slotDurationMinutes = getDefaultSlotDurationMinutes();
        Duration slotDuration = Duration.ofMinutes(slotDurationMinutes);

        while (pointer.isBefore(windowEnd)) {
            LocalDateTime slotEnd = pointer.plus(slotDuration);
            if (slotEnd.isAfter(windowEnd.plusNanos(1))) {
                break;
            }

            Instant slotStartInstant = pointer.atZone(clinicZone).toInstant();
            Instant slotEndInstant = slotEnd.atZone(clinicZone).toInstant();

            if (targetDate.equals(today) && slotStartInstant.isBefore(nowInstant)) {
                pointer = pointer.plus(slotDuration);
                continue;
            }

            boolean conflict = appointmentRepository.existsActiveByDoctorAndTimeRange(
                    doctor.getId(),
                    slotStartInstant,
                    slotEndInstant,
                    DEFAULT_SLOT_DURATION_MINUTES
            );

            if (!conflict) {
                slots.add(new AvailabilitySlotResponse(
                        doctor.getId(),
                        doctor.getFullName(),
                        slotStartInstant.toString(),
                        slotEndInstant.toString()
                ));
            }

            pointer = pointer.plus(slotDuration);
        }

        return slots;
    }

    private int getDefaultSlotDurationMinutes() {
        try {
            Long tenantId = tenantContextHolder.requireTenantId();
            return clinicSettingsRepository.findByTenantId(tenantId)
                    .map(ClinicSettingsEntity::getSlotDurationMinutes)
                    .filter(duration -> duration != null && duration >= 5 && duration <= 240)
                    .orElse(DEFAULT_SLOT_DURATION_MINUTES);
        } catch (RuntimeException ex) {
            return DEFAULT_SLOT_DURATION_MINUTES;
        }
    }

    private LocalDate parseDateOrDefault(String raw) {
        if (raw == null || raw.isBlank()) {
            // Use configured clinic timezone for default date
            return LocalDate.now(timezoneConfig.toZoneId());
        }
        return LocalDate.parse(raw);
    }
}
