package com.clinic.modules.admin.dto;

import com.clinic.modules.core.tag.TagEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PatientAdminResponse(
                Long id,
                String externalId,
                String firstName,
                String lastName,
                String email,
                String phone,
                String profileImageUrl,
                String driveFolderUrl,
                Instant createdAt,
                LocalDate dateOfBirth,
                String notes,
                List<TagEntity> tags) {
}
