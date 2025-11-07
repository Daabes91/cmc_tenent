package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.GlobalSearchResponse;
import com.clinic.modules.admin.service.GlobalSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;

@RestController
@RequestMapping("/admin/search")
public class GlobalSearchController {

    private final GlobalSearchService searchService;

    public GlobalSearchController(GlobalSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<GlobalSearchResponse>> search(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "limit", defaultValue = "5") int limitPerType
    ) {
        GlobalSearchResponse response = searchService.search(query, limitPerType, ZoneId.systemDefault());
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "SEARCH_COMPLETED",
                        "Search completed successfully.",
                        response
                )
        );
    }
}
