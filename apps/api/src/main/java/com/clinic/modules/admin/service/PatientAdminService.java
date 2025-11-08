package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.PatientAdminResponse;
import com.clinic.modules.admin.dto.PatientUpsertRequest;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.patient.GlobalPatientEntity;
import com.clinic.modules.core.patient.GlobalPatientService;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@Service
public class PatientAdminService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final GlobalPatientService globalPatientService;
    private final TenantService tenantService;
    private final TenantContextHolder tenantContextHolder;

    public PatientAdminService(PatientRepository patientRepository,
                               AppointmentRepository appointmentRepository,
                               GlobalPatientService globalPatientService,
                               TenantService tenantService,
                               TenantContextHolder tenantContextHolder) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.globalPatientService = globalPatientService;
        this.tenantService = tenantService;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public Page<PatientAdminResponse> listPatients(Pageable pageable) {
        Long tenantId = tenantContextHolder.requireTenantId();
        return patientRepository.findAllByTenantId(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PatientAdminResponse getPatient(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        PatientEntity patient = patientRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        return toResponse(patient);
    }

    @Transactional
    public PatientAdminResponse createPatient(PatientUpsertRequest request) {
        String firstName = requireValue(request.firstName(), "First name is required");
        String lastName = requireValue(request.lastName(), "Last name is required");

        String email = normalize(request.email());
        String phone = normalize(request.phone());
        
        Long tenantId = tenantContextHolder.requireTenantId();
        
        // Check if patient already exists in this tenant
        ensureEmailAvailable(email, null, tenantId);

        // Find or create global patient (using a default password for admin-created patients)
        // Admin-created patients will need to set their password via password reset flow
        String defaultPassword = "ADMIN_CREATED_" + System.currentTimeMillis();
        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email,
                phone,
                request.dateOfBirth(),
                defaultPassword
        );

        // Create tenant profile linked to global patient
        PatientEntity entity = new PatientEntity(
                firstName,
                lastName,
                email,
                phone
        );
        
        // Assign current tenant to patient profile
        entity.setTenant(tenantService.requireTenant(tenantId));
        entity.setGlobalPatient(globalPatient);

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
        Long tenantId = tenantContextHolder.requireTenantId();
        PatientEntity patient = patientRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        String firstName = requireValue(request.firstName(), "First name is required");
        String lastName = requireValue(request.lastName(), "Last name is required");
        String email = normalize(request.email());

        ensureEmailAvailable(email, id, tenantId);

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
        Long tenantId = tenantContextHolder.requireTenantId();
        PatientEntity patient = patientRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        if (appointmentRepository.existsByPatientId(patient.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient has scheduled appointments");
        }

        patientRepository.delete(patient);
    }

    private void ensureEmailAvailable(String email, Long currentId, Long tenantId) {
        if (email == null) {
            return;
        }
        // Check if email is already used by another patient in this tenant
        Optional<PatientEntity> existing = patientRepository.findByTenantIdAndGlobalPatient_Email(tenantId, email);
        if (existing.isPresent() && !Objects.equals(existing.get().getId(), currentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered to another patient in this clinic");
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
