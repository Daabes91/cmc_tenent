package com.clinic.modules.publicapi.service;

import com.clinic.modules.core.patient.PatientContactUtils;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.publicapi.dto.PatientAuthResponse;
import com.clinic.modules.publicapi.dto.PatientLoginRequest;
import com.clinic.modules.publicapi.dto.PatientSignupRequest;
import com.clinic.security.JwtIssuer;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PatientAuthService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;

    public PatientAuthService(PatientRepository patientRepository,
                              PasswordEncoder passwordEncoder,
                              JwtIssuer jwtIssuer) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtIssuer = jwtIssuer;
    }

    @Transactional
    public PatientAuthResponse signup(PatientSignupRequest request) {
        String email = normalizeEmailOrThrow(request.email());
        String phone = normalizePhoneOrThrow(request.phone());
        String firstName = request.firstName().trim();
        String lastName = request.lastName().trim();

        PatientEntity patient = patientRepository.findByEmailIgnoreCase(email).orElse(null);
        if (patient != null) {
            if (StringUtils.hasText(patient.getPasswordHash())) {
                throw conflict("An account already exists for this email address.");
            }
            patient.updateDetails(firstName, lastName, email, phone, request.dateOfBirth());
        } else {
            patient = new PatientEntity(
                    firstName,
                    lastName,
                    email,
                    phone
            );
            if (request.dateOfBirth() != null) {
                patient.setDateOfBirth(request.dateOfBirth());
            }
        }

        patient.setPasswordHash(passwordEncoder.encode(request.password()));
        patientRepository.save(patient);

        var token = jwtIssuer.issuePatientAccessToken(patient);
        return PatientAuthResponse.bearer(token.token(), token.expiresAt(), patient);
    }

    @Transactional
    public PatientAuthResponse login(PatientLoginRequest request) {
        String email = normalizeEmailOrThrow(request.email());
        PatientEntity patient = patientRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> unauthorized("Invalid email or password."));

        if (!StringUtils.hasText(patient.getPasswordHash())
                || !passwordEncoder.matches(request.password(), patient.getPasswordHash())) {
            throw unauthorized("Invalid email or password.");
        }

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

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
