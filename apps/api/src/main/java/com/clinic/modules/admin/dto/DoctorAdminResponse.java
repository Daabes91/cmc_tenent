package com.clinic.modules.admin.dto;

import java.time.Instant;
import java.util.List;

public record DoctorAdminResponse(
        Long id,
        String fullNameEn,
        String fullNameAr,
        String specialtyEn,
        String specialtyAr,
        String bioEn,
        String bioAr,
        String imageUrl,
        String email,
        String phone,
        Integer displayOrder,
        Boolean isActive,
        List<String> locales,
        List<ServiceReference> services,
        Instant createdAt
) {
    public record ServiceReference(Long id, String slug, String nameEn) {
    }
}
