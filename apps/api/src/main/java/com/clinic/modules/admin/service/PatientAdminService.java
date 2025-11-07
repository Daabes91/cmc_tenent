package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.PatientAdminResponse;
import com.clinic.modules.admin.dto.PatientUpsertRequest;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PatientAdminService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientAdminService(PatientRepository patientRepository,
                               AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<PatientAdminResponse> listPatients(Pageable pageable) {
        return patientRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PatientAdminResponse getPatient(Long id) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        return toResponse(patient);
    }

    @Transactional
    public PatientAdminResponse createPatient(PatientUpsertRequest request) {
        String firstName = requireValue(request.firstName(), "First name is required");
        String lastName = requireValue(request.lastName(), "Last name is required");

        String email = normalize(request.email());
        ensureEmailAvailable(email, null);

        PatientEntity entity = new PatientEntity(
                firstName,
                lastName,
                email,
                normalize(request.phone())
        );

        // Set profile image URL if provided
        if (request.profileImageUrl() != null) {
            entity.setProfileImageUrl(normalize(request.profileImageUrl()));
        }

        if (request.dateOfBirth() != null) {
            entity.setDateOfBirth(request.dateOfBirth());
        }

        PatientEntity saved = patientRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public PatientAdminResponse updatePatient(Long id, PatientUpsertRequest request) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        String firstName = requireValue(request.firstName(), "First name is required");
        String lastName = requireValue(request.lastName(), "Last name is required");
        String email = normalize(request.email());

        ensureEmailAvailable(email, id);

        patient.updateDetails(
                firstName,
                lastName,
                email,
                normalize(request.phone()),
                request.dateOfBirth()
        );

        // Update profile image URL if provided
        if (request.profileImageUrl() != null) {
            patient.setProfileImageUrl(normalize(request.profileImageUrl()));
        }

        return toResponse(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        if (appointmentRepository.existsByPatientId(patient.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient has scheduled appointments");
        }

        patientRepository.delete(patient);
    }

    private void ensureEmailAvailable(String email, Long currentId) {
        if (email == null) {
            return;
        }
        Optional<PatientEntity> existing = patientRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent() && !Objects.equals(existing.get().getId(), currentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered to another patient");
        }
    }

    private PatientAdminResponse toResponse(PatientEntity entity) {
        return new PatientAdminResponse(
                entity.getId(),
                entity.getExternalId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getProfileImageUrl(),
                entity.getCreatedAt(),
                entity.getDateOfBirth()
        );
    }

    private String requireValue(String input, String message) {
        String normalized = normalize(input);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
