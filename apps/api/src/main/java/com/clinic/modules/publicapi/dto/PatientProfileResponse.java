package com.clinic.modules.publicapi.dto;

import java.time.LocalDate;

public record PatientProfileResponse(
        Long id,
        String externalId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profileImageUrl,
        LocalDate dateOfBirth
) {
}
