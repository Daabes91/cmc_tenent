package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.CallToActionType;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing carousel item.
 */
public class UpdateCarouselItemRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Arabic title must not exceed 255 characters")
    private String titleAr;

    @Size(max = 255, message = "Subtitle must not exceed 255 characters")
    private String subtitle;

    @Size(max = 255, message = "Arabic subtitle must not exceed 255 characters")
    private String subtitleAr;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Size(max = 500, message = "Link URL must not exceed 500 characters")
    private String linkUrl;

    private CallToActionType ctaType;

    @Size(max = 100, message = "CTA text must not exceed 100 characters")
    private String ctaText;

    @Size(max = 100, message = "Arabic CTA text must not exceed 100 characters")
    private String ctaTextAr;

    private Boolean isActive;

    private Integer sortOrder;

    // Constructors
    public UpdateCarouselItemRequest() {}

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleAr() {
        return titleAr;
    }

    public void setTitleAr(String titleAr) {
        this.titleAr = titleAr;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getSubtitleAr() {
        return subtitleAr;
    }

    public void setSubtitleAr(String subtitleAr) {
        this.subtitleAr = subtitleAr;
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

    public String getCtaTextAr() {
        return ctaTextAr;
    }

    public void setCtaTextAr(String ctaTextAr) {
        this.ctaTextAr = ctaTextAr;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return "UpdateCarouselItemRequest{" +
                "title='" + title + '\'' +
                ", ctaType=" + ctaType +
                ", isActive=" + isActive +
                ", sortOrder=" + sortOrder +
                '}';
    }
}
