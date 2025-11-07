package com.clinic.modules.publicapi.controller;

import com.clinic.modules.admin.dto.BlogResponse;
import com.clinic.modules.admin.service.BlogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public Blog Controller - Read-only operations for published blogs
 * Base path: /public/blogs
 * No authentication required
 */
@RestController
@RequestMapping("/public/blogs")
public class PublicBlogController {

    private final BlogService blogService;

    public PublicBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    /**
     * Get all published blogs
     * GET /public/blogs
     * Optional: ?locale=en or ?locale=ar
     */
    @GetMapping
    public ResponseEntity<List<BlogResponse>> getPublishedBlogs(
            @RequestParam(required = false) String locale
    ) {
        return ResponseEntity.ok(blogService.getPublishedBlogs(locale));
    }

    /**
     * Get published blog by slug
     * GET /public/blogs/{slug}
     * This endpoint increments view count
     */
    @GetMapping("/{slug}")
    public ResponseEntity<BlogResponse> getPublishedBlogBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(blogService.getPublishedBlogBySlug(slug));
    }
}
