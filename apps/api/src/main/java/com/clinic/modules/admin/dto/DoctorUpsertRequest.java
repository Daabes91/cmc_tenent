package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DoctorUpsertRequest(
        @NotBlank @Size(max = 160) String fullNameEn,
        @Size(max = 160) String fullNameAr,
        @Size(max = 120) String specialtyEn,
        @Size(max = 120) String specialtyAr,
        String bioEn,
        String bioAr,
        @Size(max = 500) String imageUrl,
        @Email @Size(max = 120) String email,
        @Size(max = 20) String phone,
        Integer displayOrder,
        Boolean isActive,
        List<@Size(max = 10) String> locales,
        List<Long> serviceIds
) {
}
