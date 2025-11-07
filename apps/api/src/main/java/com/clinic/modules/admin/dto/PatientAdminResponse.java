package com.clinic.modules.admin.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PatientAdminResponse(
        Long id,
        String externalId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profileImageUrl,
        Instant createdAt,
        LocalDate dateOfBirth
) {
}
