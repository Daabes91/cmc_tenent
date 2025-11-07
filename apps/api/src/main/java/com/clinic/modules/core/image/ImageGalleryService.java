package com.clinic.modules.core.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing image galleries and collections.
 */
@Service
public class ImageGalleryService {

    private static final Logger log = LoggerFactory.getLogger(ImageGalleryService.class);

    // In-memory storage for demo purposes - replace with database in production
    private final Map<String, ImageGallery> galleries = new ConcurrentHashMap<>();
    private final Map<String, ImageMetadata> imageMetadata = new ConcurrentHashMap<>();

    /**
     * Create a new image gallery.
     */
    public ImageGallery createGallery(String name, String description, GalleryType type) {
        String galleryId = UUID.randomUUID().toString();
        ImageGallery gallery = new ImageGallery(
                galleryId,
                name,
                description,
                type,
                new ArrayList<>(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        galleries.put(galleryId, gallery);
        log.info("Created gallery: {} ({})", name, galleryId);
        return gallery;
    }

    /**
     * Add image to gallery with metadata.
     */
    public void addImageToGallery(String galleryId, String imageId, String title, String description, List<String> tags) {
        ImageGallery gallery = galleries.get(galleryId);
        if (gallery == null) {
            throw new IllegalArgumentException("Gallery not found: " + galleryId);
        }

        ImageMetadata metadata = new ImageMetadata(
                imageId,
                title,
                description,
                tags,
                galleryId,
                LocalDateTime.now()
        );

        imageMetadata.put(imageId, metadata);
        gallery.imageIds().add(imageId);
        
        // Update gallery modified time
        ImageGallery updatedGallery = new ImageGallery(
                gallery.id(),
                gallery.name(),
                gallery.description(),
                gallery.type(),
                gallery.imageIds(),
                gallery.createdAt(),
                LocalDateTime.now()
        );
        galleries.put(galleryId, updatedGallery);

        log.info("Added image {} to gallery {}", imageId, galleryId);
    }

    /**
     * Get gallery with image metadata.
     */
    public GalleryWithImages getGalleryWithImages(String galleryId) {
        ImageGallery gallery = galleries.get(galleryId);
        if (gallery == null) {
            return null;
        }

        List<ImageMetadata> images = gallery.imageIds().stream()
                .map(imageMetadata::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new GalleryWithImages(gallery, images);
    }

    /**
     * Get all galleries by type.
     */
    public List<ImageGallery> getGalleriesByType(GalleryType type) {
        return galleries.values().stream()
                .filter(gallery -> gallery.type() == type)
                .sorted((a, b) -> b.modifiedAt().compareTo(a.modifiedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Search images by tags.
     */
    public List<ImageMetadata> searchImagesByTags(List<String> tags) {
        return imageMetadata.values().stream()
                .filter(metadata -> metadata.tags().stream().anyMatch(tags::contains))
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .collect(Collectors.toList());
    }

    /**
     * Remove image from gallery.
     */
    public void removeImageFromGallery(String galleryId, String imageId) {
        ImageGallery gallery = galleries.get(galleryId);
        if (gallery != null) {
            gallery.imageIds().remove(imageId);
            imageMetadata.remove(imageId);
            log.info("Removed image {} from gallery {}", imageId, galleryId);
        }
    }

    /**
     * Delete gallery.
     */
    public void deleteGallery(String galleryId) {
        ImageGallery gallery = galleries.remove(galleryId);
        if (gallery != null) {
            // Remove all image metadata for this gallery
            gallery.imageIds().forEach(imageMetadata::remove);
            log.info("Deleted gallery: {}", galleryId);
        }
    }

    /**
     * Get image metadata.
     */
    public ImageMetadata getImageMetadata(String imageId) {
        return imageMetadata.get(imageId);
    }

    /**
     * Update image metadata.
     */
    public void updateImageMetadata(String imageId, String title, String description, List<String> tags) {
        ImageMetadata existing = imageMetadata.get(imageId);
        if (existing != null) {
            ImageMetadata updated = new ImageMetadata(
                    imageId,
                    title,
                    description,
                    tags,
                    existing.galleryId(),
                    existing.createdAt()
            );
            imageMetadata.put(imageId, updated);
            log.info("Updated metadata for image: {}", imageId);
        }
    }

    // Data classes
    public record ImageGallery(
            String id,
            String name,
            String description,
            GalleryType type,
            List<String> imageIds,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt
    ) {}

    public record ImageMetadata(
            String imageId,
            String title,
            String description,
            List<String> tags,
            String galleryId,
            LocalDateTime createdAt
    ) {}

    public record GalleryWithImages(
            ImageGallery gallery,
            List<ImageMetadata> images
    ) {}

    public enum GalleryType {
        CLINIC_PHOTOS,
        STAFF_PHOTOS,
        EQUIPMENT,
        BEFORE_AFTER,
        CERTIFICATES,
        GENERAL
    }
}