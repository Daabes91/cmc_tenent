package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.CarouselType;
import com.clinic.modules.ecommerce.model.Platform;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for public carousel display.
 * 
 * Contains carousel information and items for public consumption.
 */
public class PublicCarouselResponse {

    private Long id;
    private String name;
    private String nameAr;
    private String slug;
    private CarouselType type;
    private String placement;
    private Platform platform;
    private Integer maxItems;
    private List<PublicCarouselItemResponse> items;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public PublicCarouselResponse() {}

    public PublicCarouselResponse(Long id, String name, String slug, CarouselType type, 
                                String placement, Platform platform, Integer maxItems,
                                List<PublicCarouselItemResponse> items,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.type = type;
        this.placement = placement;
        this.platform = platform;
        this.maxItems = maxItems;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public CarouselType getType() {
        return type;
    }

    public void setType(CarouselType type) {
        this.type = type;
    }

    public String getPlacement() {
        return placement;
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public List<PublicCarouselItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PublicCarouselItemResponse> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
