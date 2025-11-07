package com.clinic.modules.publicapi.controller;

import com.clinic.modules.core.image.ImageUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller for handling patient profile image uploads.
 * Allows authenticated patients to upload their profile images.
 */
@RestController
@RequestMapping("/public/patient")
public class PatientImageUploadController {

    private static final Logger log = LoggerFactory.getLogger(PatientImageUploadController.class);

    private final ImageUploadService imageUploadService;

    public PatientImageUploadController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    /**
     * Upload a profile image for the authenticated patient.
     * Requires patient authentication.
     */
    @PostMapping("/upload-profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            // Verify authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Unauthorized profile image upload attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
            }

            // Validate file
            if (file == null || file.isEmpty()) {
                log.warn("Empty file provided for upload");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid File", "message", "No file provided"));
            }

            log.info("Patient profile image upload request: patient={}, filename={}, size={} bytes, contentType={}",
                    authentication.getName(), file.getOriginalFilename(), file.getSize(), file.getContentType());

            // Add metadata to identify this as a patient profile image
            String metadata = String.format("{\"type\":\"patient-profile\",\"patientId\":\"%s\"}",
                    authentication.getName());

            ImageUploadService.ImageUploadResponse response = imageUploadService.uploadImage(
                    file,
                    metadata,
                    false // Don't require signed URLs for profile images
            );

            ImageUploadResponse dto = new ImageUploadResponse(
                    response.imageId(),
                    response.filename(),
                    response.publicUrl(),
                    response.variants()
            );

            log.info("Successfully uploaded patient profile image: patientId={}, imageId={}",
                    authentication.getName(), response.imageId());

            return ResponseEntity.ok(dto);

        } catch (ImageUploadService.ImageUploadException e) {
            log.error("Patient profile image upload failed: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Upload Failed", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during patient profile image upload", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", "An unexpected error occurred"));
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
}
