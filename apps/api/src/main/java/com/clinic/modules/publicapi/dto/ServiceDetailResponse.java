package com.clinic.modules.publicapi.dto;

import java.util.List;

public record ServiceDetailResponse(
        String slug,
        String name,
        String summary,
        String description,
        List<String> doctorIds
) {
}
