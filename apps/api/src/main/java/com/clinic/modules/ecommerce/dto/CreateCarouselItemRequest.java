package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.CallToActionType;
import com.clinic.modules.ecommerce.model.CarouselContentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new carousel item.
 */
public class CreateCarouselItemRequest {

    @NotNull(message = "Content type is required")
    private CarouselContentType contentType;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Subtitle must not exceed 255 characters")
    private String subtitle;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Size(max = 500, message = "Link URL must not exceed 500 characters")
    private String linkUrl;

    private CallToActionType ctaType = CallToActionType.NONE;

    @Size(max = 100, message = "CTA text must not exceed 100 characters")
    private String ctaText;

    private Long productId;

    private Long categoryId;

    private Boolean isActive = true;

    // Constructors
    public CreateCarouselItemRequest() {}

    public CreateCarouselItemRequest(CarouselContentType contentType) {
        this.contentType = contentType;
    }

    // Getters and Setters
    public CarouselContentType getContentType() {
        return contentType;
    }

    public void setContentType(CarouselContentType contentType) {
        this.contentType = contentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public CallToActionType getCtaType() {
        return ctaType;
    }

    public void setCtaType(CallToActionType ctaType) {
        this.ctaType = ctaType;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "CreateCarouselItemRequest{" +
                "contentType=" + contentType +
                ", title='" + title + '\'' +
                ", ctaType=" + ctaType +
                ", productId=" + productId +
                ", categoryId=" + categoryId +
                ", isActive=" + isActive +
                '}';
    }
}