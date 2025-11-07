package com.clinic.modules.admin.dto;

import com.clinic.modules.core.blog.BlogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BlogRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 250, message = "Slug must not exceed 250 characters")
    private String slug;

    @Size(max = 500, message = "Excerpt must not exceed 500 characters")
    private String excerpt;

    @NotBlank(message = "Content is required")
    private String content;

    private String featuredImage;

    // SEO Fields
    @Size(max = 70, message = "Meta title must not exceed 70 characters")
    private String metaTitle;

    @Size(max = 160, message = "Meta description must not exceed 160 characters")
    private String metaDescription;

    @Size(max = 255, message = "Meta keywords must not exceed 255 characters")
    private String metaKeywords;

    @Size(max = 95, message = "OG title must not exceed 95 characters")
    private String ogTitle;

    @Size(max = 200, message = "OG description must not exceed 200 characters")
    private String ogDescription;

    private String ogImage;

    private BlogStatus status;

    @Size(max = 5, message = "Locale must not exceed 5 characters")
    private String locale;

    // Getters and Setters
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

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
