package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a carousel for content display.
 * 
 * Carousels are configurable content sliders that can display various types of content
 * including products, categories, images, and promotional materials.
 */
@Entity
@Table(name = "carousels", uniqueConstraints = {
    @UniqueConstraint(name = "uk_carousels_tenant_slug", columnNames = {"tenant_id", "slug"})
})
public class CarouselEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private TenantEntity tenant;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "slug", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @NotNull
    private CarouselType type;

    @Column(name = "placement", nullable = false)
    @NotBlank
    @Size(max = 100)
    private String placement;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    @NotNull
    private Platform platform = Platform.BOTH;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "max_items")
    private Integer maxItems = 10;

    @OneToMany(mappedBy = "carousel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<CarouselItemEntity> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public CarouselEntity() {}

    public CarouselEntity(TenantEntity tenant, String name, String slug, CarouselType type, String placement) {
        this.tenant = tenant;
        this.name = name;
        this.slug = slug;
        this.type = type;
        this.placement = placement;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

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

    public List<CarouselItemEntity> getItems() {
        return items;
    }

    public void setItems(List<CarouselItemEntity> items) {
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

    // Helper methods
    public void addItem(CarouselItemEntity item) {
        items.add(item);
        item.setCarousel(this);
    }

    public void removeItem(CarouselItemEntity item) {
        items.remove(item);
        item.setCarousel(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarouselEntity)) return false;
        CarouselEntity that = (CarouselEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CarouselEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", type=" + type +
                ", placement='" + placement + '\'' +
                ", platform=" + platform +
                ", isActive=" + isActive +
                '}';
    }
}