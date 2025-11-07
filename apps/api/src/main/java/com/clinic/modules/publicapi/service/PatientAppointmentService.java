package com.clinic.modules.publicapi.service;

import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.publicapi.dto.PatientAppointmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PatientAppointmentService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final AppointmentRepository appointmentRepository;

    public PatientAppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<PatientAppointmentResponse> getPatientAppointments(Long patientId) {
        var appointments = appointmentRepository.findByPatientIdOrderByScheduledAtDesc(patientId);
        return appointments.stream()
                .map(this::toResponse)
                .toList();
    }

    private PatientAppointmentResponse toResponse(AppointmentEntity appointment) {
        var doctor = appointment.getDoctor();
        var doctorInfo = new PatientAppointmentResponse.DoctorInfo(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getSpecialty()
        );

        return new PatientAppointmentResponse(
                appointment.getId(),
                appointment.getService().getNameEn(),
                doctorInfo,
                FORMATTER.format(appointment.getScheduledAt()),
                appointment.getStatus().name(),
                appointment.getBookingMode().name(),
                appointment.getNotes(),
                FORMATTER.format(appointment.getCreatedAt())
        );
    }
}
