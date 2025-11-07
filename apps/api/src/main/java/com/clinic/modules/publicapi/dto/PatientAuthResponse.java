package com.clinic.modules.publicapi.dto;

import com.clinic.modules.core.patient.PatientEntity;

import java.time.Instant;
import java.time.LocalDate;

public record PatientAuthResponse(
        String tokenType,
        String accessToken,
        Instant accessTokenExpiresAt,
        PatientProfile patient
) {
    public static PatientAuthResponse bearer(String accessToken,
                                             Instant accessTokenExpiresAt,
                                             PatientEntity patient) {
        return new PatientAuthResponse(
                "Bearer",
                accessToken,
                accessTokenExpiresAt,
                new PatientProfile(
                        patient.getId(),
                        patient.getExternalId(),
                        patient.getFirstName(),
                        patient.getLastName(),
                        patient.getEmail(),
                        patient.getProfileImageUrl(),
                        patient.getDateOfBirth()
                )
        );
    }

    public record PatientProfile(
            Long id,
            String externalId,
            String firstName,
            String lastName,
            String email,
            String profileImageUrl,
            LocalDate dateOfBirth
    ) {
    }
}
