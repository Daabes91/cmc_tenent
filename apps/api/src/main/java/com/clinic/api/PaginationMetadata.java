package com.clinic.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

/**
 * Standardized pagination metadata for all API responses.
 * Provides consistent pagination information across all endpoints.
 */
public class PaginationMetadata {

    @JsonProperty("page")
    private final int page;

    @JsonProperty("page_size")
    private final int pageSize;

    @JsonProperty("total")
    private final long total;

    @JsonProperty("total_pages")
    private final int totalPages;

    @JsonProperty("has_next")
    private final boolean hasNext;

    @JsonProperty("has_previous")
    private final boolean hasPrevious;

    @JsonProperty("first")
    private final boolean first;

    @JsonProperty("last")
    private final boolean last;

    public PaginationMetadata(int page, int pageSize, long total, int totalPages, 
                             boolean hasNext, boolean hasPrevious, boolean first, boolean last) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.first = first;
        this.last = last;
    }

    /**
     * Create pagination metadata from Spring Data Page.
     */
    public static PaginationMetadata fromPage(Page<?> page) {
        return new PaginationMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast()
        );
    }

    /**
     * Create empty pagination metadata for single item responses.
     */
    public static PaginationMetadata empty() {
        return new PaginationMetadata(0, 1, 1, 1, false, false, true, true);
    }

    /**
     * Create pagination metadata for a list without Spring Data Page.
     */
    public static PaginationMetadata forList(int page, int pageSize, long total) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        boolean first = page == 0;
        boolean last = page == totalPages - 1 || totalPages == 0;

        return new PaginationMetadata(page, pageSize, total, totalPages, hasNext, hasPrevious, first, last);
    }

    // Getters
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotal() { return total; }
    public int getTotalPages() { return totalPages; }
    public boolean isHasNext() { return hasNext; }
    public boolean isHasPrevious() { return hasPrevious; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }

    @Override
    public String toString() {
        return "PaginationMetadata{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", total=" + total +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                ", first=" + first +
                ", last=" + last +
                '}';
    }
}