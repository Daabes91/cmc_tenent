package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.BlogRequest;
import com.clinic.modules.admin.dto.BlogResponse;
import com.clinic.modules.core.blog.BlogEntity;
import com.clinic.modules.core.blog.BlogRepository;
import com.clinic.modules.core.blog.BlogStatus;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final TenantService tenantService;
    private final TenantContextHolder tenantContextHolder;

    public BlogService(BlogRepository blogRepository, TenantService tenantService, TenantContextHolder tenantContextHolder) {
        this.blogRepository = blogRepository;
        this.tenantService = tenantService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get all blogs (admin view - includes all statuses)
     */
    public List<BlogResponse> getAllBlogs() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return blogRepository.findAllByTenantId(tenantId).stream()
                .map(BlogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all blogs by locale (admin view)
     */
    public List<BlogResponse> getAllBlogsByLocale(String locale) {
        Long tenantId = tenantContextHolder.requireTenantId();
        // Filter by tenant first, then by locale in memory
        return blogRepository.findAllByTenantId(tenantId).stream()
                .filter(blog -> locale.equals(blog.getLocale()))
                .map(BlogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all published blogs (public view)
     */
    public List<BlogResponse> getPublishedBlogs(String locale) {
        Long tenantId = tenantContextHolder.requireTenantId();
        List<BlogEntity> blogs = blogRepository.findAllByTenantIdAndStatus(tenantId, BlogStatus.PUBLISHED);
        
        if (locale != null && !locale.isEmpty()) {
            blogs = blogs.stream()
                    .filter(blog -> locale.equals(blog.getLocale()))
                    .collect(Collectors.toList());
        }
        
        return blogs.stream()
                .sorted((b1, b2) -> {
                    Instant t1 = b1.getPublishedAt();
                    Instant t2 = b2.getPublishedAt();
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    return t2.compareTo(t1); // Descending order
                })
                .map(BlogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get blog by ID (admin view)
     */
    public BlogResponse getBlogById(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        BlogEntity blog = blogRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));
        return BlogResponse.fromEntity(blog);
    }

    /**
     * Get blog by slug (public view - only published)
     */
    @Transactional
    public BlogResponse getPublishedBlogBySlug(String slug) {
        Long tenantId = tenantContextHolder.requireTenantId();
        BlogEntity blog = blogRepository.findByTenantIdAndSlug(tenantId, slug)
                .filter(b -> b.getStatus() == BlogStatus.PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Published blog not found with slug: " + slug));

        // Increment view count
        blogRepository.incrementViewCount(blog.getId());

        return BlogResponse.fromEntity(blog);
    }

    /**
     * Get blog by slug (admin view - any status)
     */
    public BlogResponse getBlogBySlug(String slug) {
        Long tenantId = tenantContextHolder.requireTenantId();
        BlogEntity blog = blogRepository.findByTenantIdAndSlug(tenantId, slug)
                .orElseThrow(() -> new RuntimeException("Blog not found with slug: " + slug));
        return BlogResponse.fromEntity(blog);
    }

    /**
     * Create new blog
     */
    public BlogResponse createBlog(BlogRequest request, Long authorId, String authorName) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        // Check if slug already exists in current tenant
        if (blogRepository.findByTenantIdAndSlug(tenantId, request.getSlug()).isPresent()) {
            throw new RuntimeException("Blog with slug '" + request.getSlug() + "' already exists");
        }

        BlogEntity blog = new BlogEntity();
        mapRequestToEntity(request, blog);
        blog.setAuthorId(authorId);
        blog.setAuthorName(authorName);
        
        // Assign current tenant before save
        blog.setTenant(tenantService.requireTenant(tenantId));

        // Set published date if status is PUBLISHED
        if (request.getStatus() == BlogStatus.PUBLISHED && blog.getPublishedAt() == null) {
            blog.setPublishedAt(Instant.now());
        }

        BlogEntity saved = blogRepository.save(blog);
        return BlogResponse.fromEntity(saved);
    }

    /**
     * Update blog
     */
    public BlogResponse updateBlog(Long id, BlogRequest request) {
        Long tenantId = tenantContextHolder.requireTenantId();
        BlogEntity blog = blogRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));

        // Check if slug is being changed and if new slug already exists in current tenant
        if (!blog.getSlug().equals(request.getSlug())) {
            if (blogRepository.findByTenantIdAndSlug(tenantId, request.getSlug()).isPresent()) {
                throw new RuntimeException("Blog with slug '" + request.getSlug() + "' already exists");
            }
        }

        // Store old status to check for changes
        BlogStatus oldStatus = blog.getStatus();

        mapRequestToEntity(request, blog);

        // Set published date when status changes to PUBLISHED
        if (request.getStatus() == BlogStatus.PUBLISHED &&
            oldStatus != BlogStatus.PUBLISHED &&
            blog.getPublishedAt() == null) {
            blog.setPublishedAt(Instant.now());
        }

        BlogEntity updated = blogRepository.save(blog);
        return BlogResponse.fromEntity(updated);
    }

    /**
     * Delete blog
     */
    public void deleteBlog(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        BlogEntity blog = blogRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));
        blogRepository.delete(blog);
    }

    /**
     * Publish blog (change status to PUBLISHED)
     */
    public BlogResponse publishBlog(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        BlogEntity blog = blogRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));

        blog.setStatus(BlogStatus.PUBLISHED);
        if (blog.getPublishedAt() == null) {
            blog.setPublishedAt(Instant.now());
        }

        BlogEntity updated = blogRepository.save(blog);
        return BlogResponse.fromEntity(updated);
    }

    /**
     * Unpublish blog (change status to DRAFT)
     */
    public BlogResponse unpublishBlog(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        BlogEntity blog = blogRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));

        blog.setStatus(BlogStatus.DRAFT);

        BlogEntity updated = blogRepository.save(blog);
        return BlogResponse.fromEntity(updated);
    }

    /**
     * Helper method to map request to entity
     */
    private void mapRequestToEntity(BlogRequest request, BlogEntity entity) {
        entity.setTitle(request.getTitle());
        entity.setSlug(request.getSlug());
        entity.setExcerpt(request.getExcerpt());
        entity.setContent(request.getContent());
        entity.setFeaturedImage(request.getFeaturedImage());
        entity.setMetaTitle(request.getMetaTitle());
        entity.setMetaDescription(request.getMetaDescription());
        entity.setMetaKeywords(request.getMetaKeywords());
        entity.setOgTitle(request.getOgTitle());
        entity.setOgDescription(request.getOgDescription());
        entity.setOgImage(request.getOgImage());

        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }

        if (request.getLocale() != null && !request.getLocale().isEmpty()) {
            entity.setLocale(request.getLocale());
        }
    }
}
