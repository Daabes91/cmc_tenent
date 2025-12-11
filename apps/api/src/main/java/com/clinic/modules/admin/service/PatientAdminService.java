package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.PatientAdminResponse;
import com.clinic.modules.admin.dto.PatientTagInput;
import com.clinic.modules.admin.dto.PatientUpsertRequest;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.patient.GlobalPatientEntity;
import com.clinic.modules.core.patient.GlobalPatientService;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tag.TagEntity;
import com.clinic.modules.core.tag.TagService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.saas.service.PlanLimitValidationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class PatientAdminService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final GlobalPatientService globalPatientService;
    private final TagService tagService;
    private final TenantService tenantService;
    private final TenantContextHolder tenantContextHolder;
    private final PlanLimitValidationService planLimitValidationService;

    public PatientAdminService(PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            GlobalPatientService globalPatientService,
            TagService tagService,
            TenantService tenantService,
            TenantContextHolder tenantContextHolder,
            PlanLimitValidationService planLimitValidationService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.globalPatientService = globalPatientService;
        this.tagService = tagService;
        this.tenantService = tenantService;
        this.tenantContextHolder = tenantContextHolder;
        this.planLimitValidationService = planLimitValidationService;
    }

    @Transactional(readOnly = true)
    public Page<PatientAdminResponse> listPatients(Pageable pageable, List<Long> tagIds) {
        Long tenantId = tenantContextHolder.requireTenantId();

        Page<PatientEntity> patients;
        if (tagIds != null && !tagIds.isEmpty()) {
            patients = patientRepository.findAllByTenantIdAndTagsIdIn(tenantId, tagIds, pageable);
        } else {
            patients = patientRepository.findAllByTenantId(tenantId, pageable);
        }

        return patients.map(this::toResponse);
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
        String driveFolderUrl = normalize(request.driveFolderUrl());

        Long tenantId = tenantContextHolder.requireTenantId();

        // Validate plan limits before creating patient
        planLimitValidationService.validatePatientLimit(tenantId);

        // Check if patient already exists in this tenant
        ensureEmailAvailable(email, null, tenantId);

        // Find or create global patient (using a default password for admin-created
        // patients)
        // Admin-created patients will need to set their password via password reset
        // flow
        String defaultPassword = "ADMIN_CREATED_" + System.currentTimeMillis();
        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email,
                phone,
                request.dateOfBirth(),
                defaultPassword);

        // Create tenant profile linked to global patient
        PatientEntity entity = new PatientEntity(
                firstName,
                lastName,
                email,
                phone);

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

        entity.setNotes(normalize(request.notes()));
        entity.setDriveFolderUrl(driveFolderUrl);

        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            List<TagEntity> tags = resolveTags(request.tagIds());
            entity.getTags().addAll(tags);
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
        String driveFolderUrl = normalize(request.driveFolderUrl());

        ensureEmailAvailable(email, id, tenantId);

        patient.updateDetails(
                firstName,
                lastName,
                email,
                normalize(request.phone()),
                request.dateOfBirth(),
                normalize(request.notes()),
                driveFolderUrl);

        if (request.tagIds() != null) {
            patient.getTags().clear();
            if (!request.tagIds().isEmpty()) {
                List<TagEntity> tags = resolveTags(request.tagIds());
                patient.getTags().addAll(tags);
            }
        }

        // Update profile image URL if provided
        if (request.profileImageUrl() != null) {
            patient.setProfileImageUrl(normalize(request.profileImageUrl()));
        }
        patient.setDriveFolderUrl(driveFolderUrl);

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
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email already registered to another patient in this clinic");
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
                entity.getDriveFolderUrl(),
                entity.getCreatedAt(),
                entity.getDateOfBirth(),
                entity.getNotes(),
                entity.getTags().stream().toList());
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

    private List<TagEntity> resolveTags(List<PatientTagInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for (PatientTagInput input : inputs) {
            if (input == null) {
                continue;
            }
            if (input.id() != null) {
                ids.add(input.id());
            } else if (input.name() != null && !input.name().isBlank()) {
                names.add(input.name().trim());
            }
        }

        List<TagEntity> tags = new ArrayList<>();
        if (!ids.isEmpty()) {
            tags.addAll(tagService.findAllByIds(ids));
        }
        for (String name : names) {
            tags.add(tagService.createOrGetTag(name, null));
        }

        return tags;
    }
}
