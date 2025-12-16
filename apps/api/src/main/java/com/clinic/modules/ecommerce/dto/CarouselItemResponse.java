package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.CallToActionType;
import com.clinic.modules.ecommerce.model.CarouselContentType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response DTO for carousel item information.
 */
public class CarouselItemResponse {

    private Long id;
    private CarouselContentType contentType;
    private String title;
    private String titleAr;
    private String subtitle;
    private String subtitleAr;
    private String imageUrl;
    private String linkUrl;
    private CallToActionType ctaType;
    private String ctaText;
    private String ctaTextAr;
    private Integer sortOrder;
    private Boolean isActive;

    // Product reference
    private Long productId;
    private String productName;

    // Category reference
    private Long categoryId;
    private String categoryName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public CarouselItemResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    @Override
    public String toString() {
        return "CarouselItemResponse{" +
                "id=" + id +
                ", contentType=" + contentType +
                ", title='" + title + '\'' +
                ", ctaType=" + ctaType +
                ", sortOrder=" + sortOrder +
                ", isActive=" + isActive +
                '}';
    }
}
