package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.CarouselType;
import com.clinic.modules.ecommerce.model.Platform;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing carousel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCarouselRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @Size(max = 255, message = "Arabic name must not exceed 255 characters")
    private String nameAr;

    @NotNull(message = "Type is required")
    private CarouselType type;

    @NotBlank(message = "Placement is required")
    @Size(max = 100, message = "Placement must not exceed 100 characters")
    private String placement;

    @NotNull(message = "Platform is required")
    private Platform platform;

    private Boolean isActive;

    private Integer maxItems;

    // Constructors
    public UpdateCarouselRequest() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    public String toString() {
        return "UpdateCarouselRequest{" +
                "name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", type=" + type +
                ", placement='" + placement + '\'' +
                ", platform=" + platform +
                ", isActive=" + isActive +
                ", maxItems=" + maxItems +
                '}';
    }
}
