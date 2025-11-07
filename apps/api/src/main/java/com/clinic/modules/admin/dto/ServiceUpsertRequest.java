package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceUpsertRequest(
        @Size(max = 120) String slug,
        @NotBlank @Size(max = 160) String nameEn,
        @Size(max = 160) String nameAr,
        @Size(max = 500) String summaryEn,
        @Size(max = 500) String summaryAr
) {
}
