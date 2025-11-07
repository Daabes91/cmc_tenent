package com.clinic.modules.admin.controller;

import com.clinic.modules.admin.dto.BlogRequest;
import com.clinic.modules.admin.dto.BlogResponse;
import com.clinic.modules.admin.service.BlogService;
import com.clinic.security.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Blog Controller - Full CRUD operations
 * Base path: /admin/blogs
 * Requires: ADMIN role
 */
@RestController
@RequestMapping("/admin/blogs")
public class AdminBlogController {

    private final BlogService blogService;

    public AdminBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    /**
     * Get all blogs (all statuses)
     * GET /admin/blogs
     */
    @GetMapping
    @PreAuthorize("@permissionService.canView('blogs')")
    public ResponseEntity<List<BlogResponse>> getAllBlogs(
            @RequestParam(required = false) String locale
    ) {
        if (locale != null && !locale.isEmpty()) {
            return ResponseEntity.ok(blogService.getAllBlogsByLocale(locale));
        }
        return ResponseEntity.ok(blogService.getAllBlogs());
    }

    /**
     * Get blog by ID
     * GET /admin/blogs/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.canView('blogs')")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    /**
     * Get blog by slug
     * GET /admin/blogs/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    @PreAuthorize("@permissionService.canView('blogs')")
    public ResponseEntity<BlogResponse> getBlogBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(blogService.getBlogBySlug(slug));
    }

    /**
     * Create new blog
     * POST /admin/blogs
     */
    @PostMapping
    @PreAuthorize("@permissionService.canCreate('blogs')")
    public ResponseEntity<BlogResponse> createBlog(
            @Valid @RequestBody BlogRequest request,
            Authentication authentication
    ) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long authorId = Long.parseLong(principal.subject());
        String authorName = authentication.getName(); // Get name from Authentication object

        BlogResponse created = blogService.createBlog(request, authorId, authorName);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update blog
     * PUT /admin/blogs/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit('blogs')")
    public ResponseEntity<BlogResponse> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogRequest request
    ) {
        BlogResponse updated = blogService.updateBlog(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete blog
     * DELETE /admin/blogs/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.canDelete('blogs')")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publish blog
     * PUT /admin/blogs/{id}/publish
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("@permissionService.canEdit('blogs')")
    public ResponseEntity<BlogResponse> publishBlog(@PathVariable Long id) {
        BlogResponse published = blogService.publishBlog(id);
        return ResponseEntity.ok(published);
    }

    /**
     * Unpublish blog (set to DRAFT)
     * PUT /admin/blogs/{id}/unpublish
     */
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("@permissionService.canEdit('blogs')")
    public ResponseEntity<BlogResponse> unpublishBlog(@PathVariable Long id) {
        BlogResponse unpublished = blogService.unpublishBlog(id);
        return ResponseEntity.ok(unpublished);
    }
}
