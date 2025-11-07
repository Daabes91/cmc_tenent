package com.clinic.modules.admin.image;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.core.image.ImageUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Controller for handling image uploads in the admin panel.
 * Requires ADMIN role or specific module permissions.
 */
@RestController
@RequestMapping("/admin/images")
public class ImageUploadController {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadController.class);

    private final ImageUploadService imageUploadService;

    public ImageUploadController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    /**
     * Upload an image to Cloudflare.
     * Requires ADMIN role or permission to edit the relevant module.
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('doctors', 'EDIT') or @permissionService.hasPermission('services', 'EDIT')")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "metadata", required = false) String metadata,
            @RequestParam(value = "requireSignedUrls", defaultValue = "false") boolean requireSignedUrls) {

        try {
            log.info("Received image upload request: filename={}, size={} bytes, requireSignedUrls={}",
                    file.getOriginalFilename(), file.getSize(), requireSignedUrls);

            ImageUploadService.ImageUploadResponse response = imageUploadService.uploadImage(file, metadata, requireSignedUrls);

            ImageUploadResponse dto = new ImageUploadResponse(
                    response.imageId(),
                    response.filename(),
                    response.publicUrl(),
                    response.variants()
            );

            log.info("Successfully uploaded image: imageId={}", response.imageId());

            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Image uploaded successfully", dto));

        } catch (ImageUploadService.ImageUploadException e) {
            log.error("Image upload failed", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseFactory.errorWithType("UPLOAD_FAILED", e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error during image upload", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseFactory.errorWithType("INTERNAL_ERROR", "An unexpected error occurred", null));
        }
    }

    /**
     * Upload multiple images in bulk.
     * Requires ADMIN role or permission to edit the relevant module.
     */
    @PostMapping("/bulk-upload")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('doctors', 'EDIT') or @permissionService.hasPermission('services', 'EDIT')")
    public ResponseEntity<ApiResponse<BulkUploadResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "metadata", required = false) String metadata) {

        try {
            log.info("Received bulk image upload request: {} files", files.size());

            if (files.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseFactory.errorWithType("INVALID_REQUEST", "No files provided", null));
            }

            if (files.size() > 10) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseFactory.errorWithType("TOO_MANY_FILES", "Maximum 10 files allowed per bulk upload", null));
            }

            ImageUploadService.BulkUploadResponse response = imageUploadService.uploadImages(files, metadata);

            BulkUploadResponse dto = new BulkUploadResponse(
                    response.results().stream()
                            .map(result -> new BulkUploadResult(
                                    result.index(),
                                    result.filename(),
                                    result.success(),
                                    result.errorMessage(),
                                    result.response() != null ? new ImageUploadResponse(
                                            result.response().imageId(),
                                            result.response().filename(),
                                            result.response().publicUrl(),
                                            result.response().variants()
                                    ) : null
                            ))
                            .toList(),
                    response.successCount(),
                    response.failureCount()
            );

            log.info("Bulk upload completed: {} successful, {} failed", response.successCount(), response.failureCount());

            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", 
                    String.format("Bulk upload completed: %d successful, %d failed", 
                            response.successCount(), response.failureCount()), dto));

        } catch (Exception e) {
            log.error("Unexpected error during bulk image upload", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseFactory.errorWithType("INTERNAL_ERROR", "An unexpected error occurred", null));
        }
    }

    /**
     * Get image variants for a specific image.
     */
    @GetMapping("/{imageId}/variants")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('doctors', 'VIEW') or @permissionService.hasPermission('services', 'VIEW')")
    public ResponseEntity<ApiResponse<Map<String, String>>> getImageVariants(@PathVariable String imageId) {
        try {
            Map<String, String> variants = imageUploadService.getImageVariants(imageId);
            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Image variants retrieved successfully", variants));
        } catch (Exception e) {
            log.error("Error retrieving image variants for imageId: {}", imageId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseFactory.errorWithType("INTERNAL_ERROR", "Failed to retrieve image variants", null));
        }
    }

    /**
     * Delete an image from Cloudflare.
     * Requires ADMIN role or permission to edit the relevant module.
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission('doctors', 'EDIT') or @permissionService.hasPermission('services', 'EDIT')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable String imageId) {

        try {
            log.info("Received image delete request: imageId={}", imageId);

            imageUploadService.deleteImage(imageId);

            log.info("Successfully deleted image: imageId={}", imageId);

            return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Image deleted successfully"));

        } catch (ImageUploadService.ImageUploadException e) {
            log.error("Image deletion failed: imageId={}", imageId, e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseFactory.error("DELETE_FAILED", e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error during image deletion: imageId={}", imageId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseFactory.error("INTERNAL_ERROR", "An unexpected error occurred", null));
        }
    }

    /**
     * Response DTO for image upload.
     */
    public record ImageUploadResponse(
            String imageId,
            String filename,
            String publicUrl,
            Map<String, String> variants
    ) {}

    /**
     * Response DTO for bulk upload.
     */
    public record BulkUploadResponse(
            List<BulkUploadResult> results,
            int successCount,
            int failureCount
    ) {}

    /**
     * Individual result for bulk upload.
     */
    public record BulkUploadResult(
            int index,
            String filename,
            boolean success,
            String errorMessage,
            ImageUploadResponse response
    ) {}
}
