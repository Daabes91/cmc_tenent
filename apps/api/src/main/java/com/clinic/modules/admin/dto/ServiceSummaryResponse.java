package com.clinic.modules.admin.dto;

import java.time.Instant;

public record ServiceSummaryResponse(
        Long id,
        String slug,
        String nameEn,
        String nameAr,
        String summaryEn,
        String summaryAr,
        Instant createdAt,
        int doctorCount
) {
}
