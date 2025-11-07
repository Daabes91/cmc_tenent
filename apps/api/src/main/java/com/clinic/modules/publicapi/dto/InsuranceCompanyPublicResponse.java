package com.clinic.modules.publicapi.dto;

public record InsuranceCompanyPublicResponse(
        Long id,
        String name,
        String logoUrl,
        String websiteUrl,
        String description
) {
}