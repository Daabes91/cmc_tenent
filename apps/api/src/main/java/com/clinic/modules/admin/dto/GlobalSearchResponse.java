package com.clinic.modules.admin.dto;

import java.util.List;

public record GlobalSearchResponse(
        List<GlobalSearchResultItem> results
) {
}
