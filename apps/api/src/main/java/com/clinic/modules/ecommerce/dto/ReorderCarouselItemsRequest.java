package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for reordering carousel items.
 */
public class ReorderCarouselItemsRequest {

    @NotEmpty(message = "Item IDs list cannot be empty")
    private List<Long> itemIds;

    // Constructors
    public ReorderCarouselItemsRequest() {}

    public ReorderCarouselItemsRequest(List<Long> itemIds) {
        this.itemIds = itemIds;
    }

    // Getters and Setters
    public List<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }

    @Override
    public String toString() {
        return "ReorderCarouselItemsRequest{" +
                "itemIds=" + itemIds +
                '}';
    }
}