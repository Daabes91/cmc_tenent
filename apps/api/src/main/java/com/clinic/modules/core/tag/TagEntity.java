package com.clinic.modules.core.tag;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tags")
@SQLDelete(sql = "UPDATE tags SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String color;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected TagEntity() {
    }

    public TagEntity(TenantEntity tenant, String name, String color, Long createdBy) {
        this.tenant = tenant;
        this.name = name;
        this.color = color;
        this.createdBy = createdBy;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TagEntity tagEntity))
            return false;
        return Objects.equals(id, tagEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
