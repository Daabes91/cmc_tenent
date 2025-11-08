package com.clinic.modules.saas.dto;

import java.util.List;

/**
 * Response DTO for paginated tenant list
 */
public record TenantListResponse(
        List<TenantResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
