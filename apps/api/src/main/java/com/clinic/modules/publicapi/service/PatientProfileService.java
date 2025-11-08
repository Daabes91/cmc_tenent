package com.clinic.modules.publicapi.service;

import com.clinic.modules.core.patient.PatientContactUtils;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.publicapi.dto.PatientProfileResponse;
import com.clinic.modules.publicapi.dto.PatientProfileUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PatientProfileService {

    private final PatientRepository patientRepository;
    private final TenantContextHolder tenantContextHolder;

    public PatientProfileService(PatientRepository patientRepository,
                                 TenantContextHolder tenantContextHolder) {
        this.patientRepository = patientRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientProfile(Long patientId) {
        Long tenantId = tenantContextHolder.requireTenantId();
        PatientEntity patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> notFound("Patient account not found."));

        return toResponse(patient);
    }

    @Transactional
    public PatientProfileResponse updatePatientProfile(Long patientId, PatientProfileUpdateRequest request) {
        Long tenantId = tenantContextHolder.requireTenantId();
        PatientEntity patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> notFound("Patient account not found."));

        String firstName = request.firstName().trim();
        String lastName = request.lastName().trim();

        if (!StringUtils.hasText(firstName) || !StringUtils.hasText(lastName)) {
            throw badRequest("First name and last name are required.");
        }

        String email = normalizeEmailOrThrow(request.email());
        String phone = normalizePhoneOrThrow(request.phone());

        // Check if email is already used by another patient in this tenant
        patientRepository.findByTenantIdAndGlobalPatient_Email(tenantId, email)
                .filter(existing -> !existing.getId().equals(patientId))
                .ifPresent(existing -> {
                    throw conflict("Another account already uses this email address in this clinic.");
                });

        patient.updateDetails(firstName, lastName, email, phone, request.dateOfBirth());

        // Update profile image URL if provided
        if (request.profileImageUrl() != null) {
            patient.setProfileImageUrl(request.profileImageUrl().trim());
        }

        patientRepository.save(patient);

        return toResponse(patient);
    }

    private String normalizeEmailOrThrow(String email) {
        try {
            return PatientContactUtils.normalizeEmail(email);
        } catch (IllegalArgumentException ex) {
            throw badRequest(ex.getMessage());
        }
    }

    private String normalizePhoneOrThrow(String phone) {
        try {
            return PatientContactUtils.normalizePhone(phone);
        } catch (IllegalArgumentException ex) {
            throw badRequest(ex.getMessage());
        }
    }

    private PatientProfileResponse toResponse(PatientEntity patient) {
        return new PatientProfileResponse(
                patient.getId(),
                patient.getExternalId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getProfileImageUrl(),
                patient.getDateOfBirth()
        );
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
