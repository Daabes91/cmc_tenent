package com.clinic.modules.admin.image;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.core.image.ImageGalleryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing image galleries.
 */
@RestController
@RequestMapping("/admin/galleries")
public class ImageGalleryController {

    private static final Logger log = LoggerFactory.getLogger(ImageGalleryController.class);

    private final ImageGalleryService galleryService;

    public ImageGalleryController(ImageGalleryService galleryService) {
        this.galleryService = galleryService;
    }

    /**
     * Create a new image gallery.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('galleries', 'CREATE')")
    public ResponseEntity<ApiResponse<ImageGalleryService.ImageGallery>> createGallery(
            @RequestBody CreateGalleryRequest request) {
        try {
            ImageGalleryService.ImageGallery gallery = galleryService.createGallery(
                    request.name(),
                    request.description(),
                    request.type()
            );
            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Gallery created successfully", gallery));
        } catch (Exception e) {
            log.error("Error creating gallery", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.errorWithType("CREATE_FAILED", e.getMessage(), null));
        }
    }

    /**
     * Get gallery with images.
     */
    @GetMapping("/{galleryId}")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('galleries', 'VIEW')")
    public ResponseEntity<ApiResponse<ImageGalleryService.GalleryWithImages>> getGallery(@PathVariable String galleryId) {
        try {
            ImageGalleryService.GalleryWithImages gallery = galleryService.getGalleryWithImages(galleryId);
            if (gallery == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Gallery retrieved successfully", gallery));
        } catch (Exception e) {
            log.error("Error retrieving gallery: {}", galleryId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.errorWithType("RETRIEVAL_FAILED", e.getMessage(), null));
        }
    }

    /**
     * Get galleries by type.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('galleries', 'VIEW')")
    public ResponseEntity<ApiResponse<List<ImageGalleryService.ImageGallery>>> getGalleries(
            @RequestParam(required = false) ImageGalleryService.GalleryType type) {
        try {
            List<ImageGalleryService.ImageGallery> galleries = type != null 
                    ? galleryService.getGalleriesByType(type)
                    : galleryService.getGalleriesByType(ImageGalleryService.GalleryType.GENERAL);
            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Galleries retrieved successfully", galleries));
        } catch (Exception e) {
            log.error("Error retrieving galleries", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.errorWithType("RETRIEVAL_FAILED", e.getMessage(), null));
        }
    }

    /**
     * Add image to gallery.
     */
    @PostMapping("/{galleryId}/images")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('galleries', 'EDIT')")
    public ResponseEntity<ApiResponse<Void>> addImageToGallery(
            @PathVariable String galleryId,
            @RequestBody AddImageRequest request) {
        try {
            galleryService.addImageToGallery(
                    galleryId,
                    request.imageId(),
                    request.title(),
                    request.description(),
                    request.tags()
            );
            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Image added to gallery successfully"));
        } catch (Exception e) {
            log.error("Error adding image to gallery: {}", galleryId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.error("ADD_FAILED", e.getMessage(), null));
        }
    }

    /**
     * Remove image from gallery.
     */
    @DeleteMapping("/{galleryId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('galleries', 'EDIT')")
    public ResponseEntity<ApiResponse<Void>> removeImageFromGallery(
            @PathVariable String galleryId,
            @PathVariable String imageId) {
        try {
            galleryService.removeImageFromGallery(galleryId, imageId);
            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Image removed from gallery successfully"));
        } catch (Exception e) {
            log.error("Error removing image from gallery: {} - {}", galleryId, imageId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.error("REMOVE_FAILED", e.getMessage(), null));
        }
    }

    /**
     * Search images by tags.
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('galleries', 'VIEW')")
    public ResponseEntity<ApiResponse<List<ImageGalleryService.ImageMetadata>>> searchImages(
            @RequestParam List<String> tags) {
        try {
            List<ImageGalleryService.ImageMetadata> images = galleryService.searchImagesByTags(tags);
            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Images found", images));
        } catch (Exception e) {
            log.error("Error searching images by tags: {}", tags, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.errorWithType("SEARCH_FAILED", e.getMessage(), null));
        }
    }

    /**
     * Delete gallery.
     */
    @DeleteMapping("/{galleryId}")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('galleries', 'DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteGallery(@PathVariable String galleryId) {
        try {
            galleryService.deleteGallery(galleryId);
            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Gallery deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting gallery: {}", galleryId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.error("DELETE_FAILED", e.getMessage(), null));
        }
    }

    // Request DTOs
    public record CreateGalleryRequest(
            String name,
            String description,
            ImageGalleryService.GalleryType type
    ) {}

    public record AddImageRequest(
            String imageId,
            String title,
            String description,
            List<String> tags
    ) {}
}