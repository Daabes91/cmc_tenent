package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an item within a carousel.
 * 
 * Carousel items can contain various types of content including images, products,
 * categories, and promotional materials with optional call-to-action buttons.
 */
@Entity
@Table(name = "carousel_items")
public class CarouselItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carousel_id", nullable = false)
    @NotNull
    private CarouselEntity carousel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private TenantEntity tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    @NotNull
    private CarouselContentType contentType;

    @Column(name = "title")
    @Size(max = 255)
    private String title;

    @Column(name = "subtitle")
    @Size(max = 255)
    private String subtitle;

    @Column(name = "image_url")
    @Size(max = 500)
    private String imageUrl;

    @Column(name = "link_url")
    @Size(max = 500)
    private String linkUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "cta_type")
    private CallToActionType ctaType = CallToActionType.NONE;

    @Column(name = "cta_text")
    @Size(max = 100)
    private String ctaText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public CarouselItemEntity() {}

    public CarouselItemEntity(CarouselEntity carousel, TenantEntity tenant, CarouselContentType contentType) {
        this.carousel = carousel;
        this.tenant = tenant;
        this.contentType = contentType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CarouselEntity getCarousel() {
        return carousel;
    }

    public void setCarousel(CarouselEntity carousel) {
        this.carousel = carousel;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
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

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarouselItemEntity)) return false;
        CarouselItemEntity that = (CarouselItemEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CarouselItemEntity{" +
                "id=" + id +
                ", contentType=" + contentType +
                ", title='" + title + '\'' +
                ", sortOrder=" + sortOrder +
                ", isActive=" + isActive +
                '}';
    }
}