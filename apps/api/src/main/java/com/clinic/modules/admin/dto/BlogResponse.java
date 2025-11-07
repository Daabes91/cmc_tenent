package com.clinic.modules.admin.dto;

import com.clinic.modules.core.blog.BlogEntity;
import com.clinic.modules.core.blog.BlogStatus;

import java.time.Instant;

public class BlogResponse {

    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String featuredImage;
    private Long authorId;
    private String authorName;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String ogTitle;
    private String ogDescription;
    private String ogImage;
    private BlogStatus status;
    private Instant publishedAt;
    private Long viewCount;
    private String locale;
    private Instant createdAt;
    private Instant updatedAt;

    public static BlogResponse fromEntity(BlogEntity entity) {
        BlogResponse response = new BlogResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setSlug(entity.getSlug());
        response.setExcerpt(entity.getExcerpt());
        response.setContent(entity.getContent());
        response.setFeaturedImage(entity.getFeaturedImage());
        response.setAuthorId(entity.getAuthorId());
        response.setAuthorName(entity.getAuthorName());
        response.setMetaTitle(entity.getMetaTitle());
        response.setMetaDescription(entity.getMetaDescription());
        response.setMetaKeywords(entity.getMetaKeywords());
        response.setOgTitle(entity.getOgTitle());
        response.setOgDescription(entity.getOgDescription());
        response.setOgImage(entity.getOgImage());
        response.setStatus(entity.getStatus());
        response.setPublishedAt(entity.getPublishedAt());
        response.setViewCount(entity.getViewCount());
        response.setLocale(entity.getLocale());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFeaturedImage() {
        return featuredImage;
    }

    public void setFeaturedImage(String featuredImage) {
        this.featuredImage = featuredImage;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public String getOgTitle() {
        return ogTitle;
    }

    public void setOgTitle(String ogTitle) {
        this.ogTitle = ogTitle;
    }

    public String getOgDescription() {
        return ogDescription;
    }

    public void setOgDescription(String ogDescription) {
        this.ogDescription = ogDescription;
    }

    public String getOgImage() {
        return ogImage;
    }

    public void setOgImage(String ogImage) {
        this.ogImage = ogImage;
    }

    public BlogStatus getStatus() {
        return status;
    }

    public void setStatus(BlogStatus status) {
        this.status = status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
