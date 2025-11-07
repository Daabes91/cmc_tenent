package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.DoctorAvailabilityRequest;
import com.clinic.modules.admin.dto.DoctorAvailabilityResponse;
import com.clinic.modules.core.doctor.DoctorAvailabilityEntity;
import com.clinic.modules.core.doctor.DoctorAvailabilityRepository;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class DoctorAvailabilityAdminService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    public DoctorAvailabilityAdminService(DoctorRepository doctorRepository,
                                          DoctorAvailabilityRepository availabilityRepository) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> listAvailabilities(Long doctorId) {
        ensureDoctorExists(doctorId);
        return availabilityRepository.findByDoctorId(doctorId).stream()
                .sorted(Comparator.comparing((DoctorAvailabilityEntity a) -> a.getSpecificDate() != null ? a.getSpecificDate() : LocalDate.now())
                        .thenComparing(DoctorAvailabilityEntity::getDayOfWeek, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(DoctorAvailabilityEntity::getStartTime))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DoctorAvailabilityResponse createAvailability(Long doctorId, DoctorAvailabilityRequest request) {
        DoctorEntity doctor = ensureDoctorExists(doctorId);
        AvailabilityDescriptor descriptor = parseRequest(request);
        validateNoOverlap(doctorId, null, descriptor);

        DoctorAvailabilityEntity entity = new DoctorAvailabilityEntity(
                doctor,
                descriptor.recurringWeekly,
                descriptor.dayOfWeek,
                descriptor.specificDate,
                descriptor.startTime,
                descriptor.endTime
        );

        DoctorAvailabilityEntity saved = availabilityRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public DoctorAvailabilityResponse updateAvailability(Long doctorId, Long availabilityId, DoctorAvailabilityRequest request) {
        ensureDoctorExists(doctorId);
        DoctorAvailabilityEntity entity = availabilityRepository.findByIdAndDoctorId(availabilityId, doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability not found"));

        AvailabilityDescriptor descriptor = parseRequest(request);
        validateNoOverlap(doctorId, availabilityId, descriptor);

        entity.update(
                descriptor.recurringWeekly,
                descriptor.dayOfWeek,
                descriptor.specificDate,
                descriptor.startTime,
                descriptor.endTime
        );

        return toResponse(entity);
    }

    @Transactional
    public void deleteAvailability(Long doctorId, Long availabilityId) {
        ensureDoctorExists(doctorId);
        DoctorAvailabilityEntity entity = availabilityRepository.findByIdAndDoctorId(availabilityId, doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability not found"));
        availabilityRepository.delete(entity);
    }

    private DoctorAvailabilityResponse toResponse(DoctorAvailabilityEntity entity) {
        String type = entity.isRecurringWeekly() ? "WEEKLY" : "ONE_TIME";
        return new DoctorAvailabilityResponse(
                entity.getId(),
                type,
                entity.getDayOfWeek() != null ? entity.getDayOfWeek().name() : null,
                entity.getSpecificDate() != null ? entity.getSpecificDate().toString() : null,
                entity.getStartTime().format(TIME_FORMATTER),
                entity.getEndTime().format(TIME_FORMATTER),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt().toString()
        );
    }

    private DoctorEntity ensureDoctorExists(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    private AvailabilityDescriptor parseRequest(DoctorAvailabilityRequest request) {
        String normalizedType = request.type() == null ? "" : request.type().trim().toUpperCase();
        boolean isWeekly = switch (normalizedType) {
            case "WEEKLY", "RECURRING", "RECURRING_WEEKLY" -> true;
            case "ONE_TIME", "SINGLE", "SPECIFIC" -> false;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid availability type");
        };

        LocalTime startTime = LocalTime.parse(request.startTime());
        LocalTime endTime = LocalTime.parse(request.endTime());

        validateSlotAlignment(startTime, endTime);

        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        DayOfWeek dayOfWeek = null;
        LocalDate specificDate = null;

        if (isWeekly) {
            if (request.dayOfWeek() == null || request.dayOfWeek().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dayOfWeek is required for weekly availability");
            }
            dayOfWeek = DayOfWeek.valueOf(request.dayOfWeek().trim().toUpperCase());
        } else {
            if (request.date() == null || request.date().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required for one-time availability");
            }
            specificDate = LocalDate.parse(request.date().trim());
        }

        return new AvailabilityDescriptor(isWeekly, dayOfWeek, specificDate, startTime, endTime);
    }

    private void validateNoOverlap(Long doctorId, Long currentAvailabilityId, AvailabilityDescriptor descriptor) {
        if (descriptor.recurringWeekly) {
            List<DoctorAvailabilityEntity> overlaps = availabilityRepository.findOverlappingWeekly(
                    doctorId,
                    descriptor.dayOfWeek,
                    descriptor.startTime,
                    descriptor.endTime
            );
            if (overlaps.stream().anyMatch(a -> !a.getId().equals(currentAvailabilityId))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Availability overlaps with an existing weekly slot");
            }
        } else {
            List<DoctorAvailabilityEntity> overlaps = availabilityRepository.findOverlappingOneTime(
                    doctorId,
                    descriptor.specificDate,
                    descriptor.startTime,
                    descriptor.endTime
            );
            if (overlaps.stream().anyMatch(a -> !a.getId().equals(currentAvailabilityId))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Availability overlaps with an existing slot on that date");
            }
        }
    }

    private record AvailabilityDescriptor(boolean recurringWeekly,
                                          DayOfWeek dayOfWeek,
                                          LocalDate specificDate,
                                          LocalTime startTime,
                                          LocalTime endTime) {
    }

    private void validateSlotAlignment(LocalTime startTime, LocalTime endTime) {
        if (!isHalfHourAligned(startTime) || !isHalfHourAligned(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end times must fall on the hour or half-hour");
        }

        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (minutes <= 0 || minutes % 30 != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability window must be a positive multiple of 30 minutes");
        }
    }

    private boolean isHalfHourAligned(LocalTime time) {
        return time.getMinute() % 30 == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }
}
