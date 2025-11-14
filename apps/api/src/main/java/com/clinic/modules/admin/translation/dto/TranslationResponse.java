package com.clinic.modules.admin.translation.dto;

import java.time.Instant;

public record TranslationResponse(
        Long id,
        String namespace,
        String key,
        String locale,
        String value,
        Instant updatedAt
) {}
