package com.clinic.modules.admin.translation.dto;

import jakarta.validation.constraints.NotBlank;

public record TranslationUpsertRequest(
        @NotBlank String namespace,
        @NotBlank String key,
        @NotBlank String locale,
        String value
) {}
