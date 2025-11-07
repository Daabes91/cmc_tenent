package com.clinic.modules.admin.insurance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InsuranceCompanyRequest(
        @NotBlank(message = "Company name (English) is required")
        @Size(max = 160, message = "Company name (English) must not exceed 160 characters")
        String nameEn,

        @Size(max = 160, message = "Company name (Arabic) must not exceed 160 characters")
        String nameAr,

        @Size(max = 500, message = "Logo URL must not exceed 500 characters")
        String logoUrl,

        @Size(max = 255, message = "Website URL must not exceed 255 characters")
        String websiteUrl,

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        String descriptionEn,

        String descriptionAr,

        Boolean isActive,

        Integer displayOrder
) {
}