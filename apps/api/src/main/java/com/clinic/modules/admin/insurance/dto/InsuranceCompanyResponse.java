package com.clinic.modules.admin.insurance.dto;

import java.time.Instant;

public record InsuranceCompanyResponse(
        Long id,
        String nameEn,
        String nameAr,
        String logoUrl,
        String websiteUrl,
        String phone,
        String email,
        String descriptionEn,
        String descriptionAr,
        Boolean isActive,
        Integer displayOrder,
        Instant createdAt,
        Instant updatedAt
) {
}