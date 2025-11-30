package com.clinic.modules.publicapi.service;

import com.clinic.modules.core.patient.GlobalPatientEntity;
import com.clinic.modules.core.patient.GlobalPatientService;
import com.clinic.modules.core.patient.PatientContactUtils;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.publicapi.dto.PatientAuthResponse;
import com.clinic.modules.publicapi.dto.PatientLoginRequest;
import com.clinic.modules.publicapi.dto.PatientSignupRequest;
import com.clinic.security.JwtIssuer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class PatientAuthService {

    private final PatientRepository patientRepository;
    private final GlobalPatientService globalPatientService;
    private final TenantService tenantService;
    private final TenantContextHolder tenantContextHolder;
    private final JwtIssuer jwtIssuer;

    public PatientAuthService(PatientRepository patientRepository,
                              GlobalPatientService globalPatientService,
                              TenantService tenantService,
                              TenantContextHolder tenantContextHolder,
                              JwtIssuer jwtIssuer) {
        this.patientRepository = patientRepository;
        this.globalPatientService = globalPatientService;
        this.tenantService = tenantService;
        this.tenantContextHolder = tenantContextHolder;
        this.jwtIssuer = jwtIssuer;
    }

    @Transactional
    public PatientAuthResponse signup(PatientSignupRequest request) {
        String email = normalizeEmailOrThrow(request.email());
        String phone = normalizePhoneOrThrow(request.phone());
        String firstName = request.firstName().trim();
        String lastName = request.lastName().trim();

        Long tenantId = tenantContextHolder.requireTenantId();

        // Find or create global patient
        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email,
                phone,
                request.dateOfBirth(),
                request.password()
        );

        // Check if tenant profile already exists for this tenant
        Optional<PatientEntity> existingProfile = patientRepository.findByTenantIdAndGlobalPatientId(
                tenantId,
                globalPatient.getId()
        );

        PatientEntity patient;
        if (existingProfile.isPresent()) {
            // Update existing tenant profile
            patient = existingProfile.get();
            patient.updateDetails(firstName, lastName, email, phone, request.dateOfBirth(), patient.getNotes(),
                    patient.getDriveFolderUrl());
        } else {
            // Create new tenant profile linked to global patient
            patient = new PatientEntity(
                    firstName,
                    lastName,
                    email,
                    phone
            );
            patient.setTenant(tenantService.requireTenant(tenantId));
            patient.setGlobalPatient(globalPatient);
            
            if (request.dateOfBirth() != null) {
                patient.setDateOfBirth(request.dateOfBirth());
            }
        }

        patientRepository.save(patient);

        var token = jwtIssuer.issuePatientAccessToken(patient);
        return PatientAuthResponse.bearer(token.token(), token.expiresAt(), patient);
    }

    @Transactional
    public PatientAuthResponse login(PatientLoginRequest request) {
        String email = normalizeEmailOrThrow(request.email());
        Long tenantId = tenantContextHolder.requireTenantId();

        // Authenticate against global patient
        GlobalPatientEntity globalPatient = globalPatientService.findByEmail(email)
                .orElseThrow(() -> unauthorized("Invalid email or password."));

        // Validate password using global patient password
        if (!globalPatientService.validatePassword(globalPatient, request.password())) {
            throw unauthorized("Invalid email or password.");
        }

        // Check if tenant profile exists for current tenant
        PatientEntity patient = patientRepository.findByTenantIdAndGlobalPatientId(
                tenantId,
                globalPatient.getId()
        ).orElseThrow(() -> unauthorized("No account found for this clinic. Please sign up first."));

        patient.markLogin();
        patientRepository.save(patient);

        var token = jwtIssuer.issuePatientAccessToken(patient);
        return PatientAuthResponse.bearer(token.token(), token.expiresAt(), patient);
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

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
