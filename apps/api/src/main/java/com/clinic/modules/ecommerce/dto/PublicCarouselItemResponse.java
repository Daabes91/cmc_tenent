package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.CallToActionType;
import com.clinic.modules.ecommerce.model.CarouselContentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for public carousel item display.
 * 
 * Contains carousel item information for public consumption with optional product details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicCarouselItemResponse {

    private Long id;
    private CarouselContentType contentType;
    private String title;
    private String titleAr;
    private String subtitle;
    private String subtitleAr;
    private String imageUrl;
    private String mobileImageUrl;
    private String linkUrl;
    private CallToActionType ctaType;
    private String ctaText;
    private String ctaTextAr;
    private Integer sortOrder;
    private ProductInfo product;
    private CategoryInfo category;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public PublicCarouselItemResponse() {}

    // Nested classes for product and category information
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductInfo {
        private Long id;
        private String name;
        private String nameAr;
        private String slug;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private String currency;
        private String shortDescription;
        private String shortDescriptionAr;
        private String mainImageUrl;
        private java.util.List<String> images;
        private Boolean inStock;

        // Constructors
        public ProductInfo() {}

        public ProductInfo(Long id, String name, String nameAr, String slug, BigDecimal price, 
                          BigDecimal compareAtPrice, String currency, String shortDescription, String shortDescriptionAr,
                          String mainImageUrl, java.util.List<String> images, Boolean inStock) {
            this.id = id;
            this.name = name;
            this.nameAr = nameAr;
            this.slug = slug;
            this.price = price;
            this.compareAtPrice = compareAtPrice;
            this.currency = currency;
            this.shortDescription = shortDescription;
            this.shortDescriptionAr = shortDescriptionAr;
            this.mainImageUrl = mainImageUrl;
            this.images = images;
            this.inStock = inStock;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getNameAr() { return nameAr; }
        public void setNameAr(String nameAr) { this.nameAr = nameAr; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getCompareAtPrice() { return compareAtPrice; }
        public void setCompareAtPrice(BigDecimal compareAtPrice) { this.compareAtPrice = compareAtPrice; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getShortDescription() { return shortDescription; }
        public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
        public String getShortDescriptionAr() { return shortDescriptionAr; }
        public void setShortDescriptionAr(String shortDescriptionAr) { this.shortDescriptionAr = shortDescriptionAr; }
        public String getMainImageUrl() { return mainImageUrl; }
        public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
        public java.util.List<String> getImages() { return images; }
        public void setImages(java.util.List<String> images) { this.images = images; }
        public Boolean getInStock() { return inStock; }
        public void setInStock(Boolean inStock) { this.inStock = inStock; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String slug;
        private String description;

        // Constructors
        public CategoryInfo() {}

        public CategoryInfo(Long id, String name, String slug, String description) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.description = description;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

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

    public String getMobileImageUrl() {
        return mobileImageUrl;
    }

    public void setMobileImageUrl(String mobileImageUrl) {
        this.mobileImageUrl = mobileImageUrl;
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

    public ProductInfo getProduct() {
        return product;
    }

    public void setProduct(ProductInfo product) {
        this.product = product;
    }

    public CategoryInfo getCategory() {
        return category;
    }

    public void setCategory(CategoryInfo category) {
        this.category = category;
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
