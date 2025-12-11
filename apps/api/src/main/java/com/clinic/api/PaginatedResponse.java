package com.clinic.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standardized paginated response wrapper for all API endpoints.
 * Provides consistent structure for paginated data responses.
 */
public class PaginatedResponse<T> {

    @JsonProperty("data")
    private final List<T> data;

    @JsonProperty("pagination")
    private final PaginationMetadata pagination;

    public PaginatedResponse(List<T> data, PaginationMetadata pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    /**
     * Create a paginated response from Spring Data Page.
     */
    public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                PaginationMetadata.fromPage(page)
        );
    }

    /**
     * Create a paginated response from a list and pagination info.
     */
    public static <T> PaginatedResponse<T> of(List<T> data, int page, int pageSize, long total) {
        return new PaginatedResponse<>(
                data,
                PaginationMetadata.forList(page, pageSize, total)
        );
    }

    /**
     * Create a single-page response from a list.
     */
    public static <T> PaginatedResponse<T> singlePage(List<T> data) {
        return new PaginatedResponse<>(
                data,
                PaginationMetadata.forList(0, data.size(), data.size())
        );
    }

    // Getters
    public List<T> getData() { return data; }
    public PaginationMetadata getPagination() { return pagination; }

    @Override
    public String toString() {
        return "PaginatedResponse{" +
                "data=" + (data != null ? data.size() + " items" : "null") +
                ", pagination=" + pagination +
                '}';
    }
}