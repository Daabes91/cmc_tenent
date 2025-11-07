package com.clinic.modules.publicapi.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentMode;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.appointment.AppointmentStatus;
import com.clinic.modules.core.appointment.AppointmentSource;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.security.JwtPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicServiceRepository serviceRepository;

    @Autowired
    public BookingController(AppointmentRepository appointmentRepository,
                           PatientRepository patientRepository,
                           DoctorRepository doctorRepository,
                           ClinicServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.serviceRepository = serviceRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createBooking(
            @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        
        try {
            logger.info("Creating booking for patient {} with doctor {}", principal.subject(), request.doctorId());
            
            // Only allow clinic visits through this endpoint
            if (request.bookingMode() == AppointmentMode.VIRTUAL_CONSULTATION) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.errorWithType("VIRTUAL_REQUIRES_PAYMENT", "Virtual consultations require payment. Use the payment flow instead.", List.of()));
            }

            // Get patient from JWT
            PatientEntity patient = patientRepository.findById(Long.parseLong(principal.subject()))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

            // Get doctor
            DoctorEntity doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

            // Get service (for now, use a default service - you may need to implement service lookup by slug)
            ClinicServiceEntity service = serviceRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

            // Parse slot time (simplified - you may need proper slot parsing)
            Instant scheduledAt = Instant.parse(request.slot());

            // Create appointment
            AppointmentEntity appointment = new AppointmentEntity(
                patient,
                doctor,
                service,
                scheduledAt,
                AppointmentStatus.CONFIRMED,
                AppointmentMode.CLINIC_VISIT,
                request.notes()
            );
            
            appointment.setSource(AppointmentSource.WEB);
            appointment = appointmentRepository.save(appointment);

            Map<String, Object> response = Map.of(
                "appointmentId", appointment.getId(),
                "status", "CONFIRMED",
                "message", "Appointment booked successfully"
            );

            logger.info("Successfully created appointment {} for patient {}", appointment.getId(), principal.subject());
            return ResponseEntity.ok(ApiResponseFactory.success("BOOKING_CREATED", "Appointment booked successfully", response));

        } catch (Exception e) {
            logger.error("Error creating booking for patient " + principal.subject(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponseFactory.errorWithType("BOOKING_FAILED", "Failed to create booking: " + e.getMessage(), List.of()));
        }
    }

    public record CreateBookingRequest(
        Long doctorId,
        String serviceSlug,
        String slot,
        AppointmentMode bookingMode,
        String notes
    ) {}
}