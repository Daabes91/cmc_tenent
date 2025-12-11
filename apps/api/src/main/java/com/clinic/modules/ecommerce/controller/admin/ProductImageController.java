package com.clinic.modules.ecommerce.controller.admin;

import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.ecommerce.config.RequiresEcommerceFeature;
import com.clinic.modules.ecommerce.dto.*;
import com.clinic.modules.ecommerce.model.ProductImageEntity;
import com.clinic.modules.ecommerce.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin controller for product image management.
 * Provides endpoints for creating, listing, updating, and deleting product images.
 * All operations enforce tenant isolation and e-commerce feature validation.
 */
@RestController
@RequestMapping("/admin/tenants/{tenantId}/products/{productId}/images")
@Tag(name = "Admin Product Images", description = "Admin endpoints for managing product images")
@SecurityRequirement(name = "bearerAuth")
@RequiresEcommerceFeature
public class ProductImageController {

    private static final Logger logger = LoggerFactory.getLogger(ProductImageController.class);

    private final ProductImageService productImageService;
    private final TenantContextHolder tenantContextHolder;

    public ProductImageController(
            ProductImageService productImageService,
            TenantContextHolder tenantContextHolder) {
        this.productImageService = productImageService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get all images for a product.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @return list of images ordered by sort order
     */
    @GetMapping
    @Operation(
            summary = "List product images",
            description = "Retrieve all images for a specific product, ordered by sort order."
    )
    public ResponseEntity<List<ProductImageResponse>> getImages(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching images - tenantId: {}, productId: {}", tenantId, productId);

        List<ProductImageEntity> images = productImageService.getImagesByProduct(productId, tenantId);
        List<ProductImageResponse> response = images.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        logger.info("Successfully fetched images - tenantId: {}, productId: {}, count: {}", 
                   tenantId, productId, response.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific image by ID.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param imageId the image ID
     * @return the image details
     */
    @GetMapping("/{imageId}")
    @Operation(
            summary = "Get product image",
            description = "Retrieve a specific product image by ID."
    )
    public ResponseEntity<ProductImageResponse> getImage(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Image ID")
            @PathVariable Long imageId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching image - tenantId: {}, productId: {}, imageId: {}", 
                   tenantId, productId, imageId);

        ProductImageEntity image = productImageService.getImage(imageId, tenantId);
        
        // Validate that image belongs to the specified product
        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to the specified product");
        }
        
        ProductImageResponse response = toResponse(image);

        logger.info("Successfully fetched image - tenantId: {}, productId: {}, imageId: {}", 
                   tenantId, productId, imageId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get the main image for a product.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @return the main image, or 404 if none exists
     */
    @GetMapping("/main")
    @Operation(
            summary = "Get main product image",
            description = "Retrieve the main image for a product."
    )
    public ResponseEntity<ProductImageResponse> getMainImage(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching main image - tenantId: {}, productId: {}", tenantId, productId);

        ProductImageEntity image = productImageService.getMainImageByProduct(productId, tenantId);
        
        if (image == null) {
            logger.info("No main image found - tenantId: {}, productId: {}", tenantId, productId);
            return ResponseEntity.notFound().build();
        }
        
        ProductImageResponse response = toResponse(image);

        logger.info("Successfully fetched main image - tenantId: {}, productId: {}, imageId: {}", 
                   tenantId, productId, image.getId());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new image for a product.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param request the image creation request
     * @return the created image
     */
    @PostMapping
    @Operation(
            summary = "Create product image",
            description = "Create a new image for a product. If this is the first image, it will be set as the main image."
    )
    public ResponseEntity<ProductImageResponse> createImage(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Valid @RequestBody ProductImageCreateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Creating image - tenantId: {}, productId: {}, url: {}", 
                   tenantId, productId, request.imageUrl());

        try {
            ProductImageEntity image = productImageService.createImage(
                productId, tenantId, request.imageUrl(), request.altText());
            
            // Update additional fields if provided
            if (request.sortOrder() != null) {
                image = productImageService.updateImageSortOrder(image.getId(), tenantId, request.sortOrder());
            }
            
            if (Boolean.TRUE.equals(request.isMain())) {
                image = productImageService.setAsMainImage(image.getId(), tenantId);
            }
            
            ProductImageResponse response = toResponse(image);

            logger.info("Successfully created image - tenantId: {}, productId: {}, imageId: {}", 
                       tenantId, productId, image.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create image - tenantId: {}, productId: {}, error: {}", 
                        tenantId, productId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing image.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param imageId the image ID
     * @param request the image update request
     * @return the updated image
     */
    @PutMapping("/{imageId}")
    @Operation(
            summary = "Update product image",
            description = "Update an existing product image. Only provided fields will be updated."
    )
    public ResponseEntity<ProductImageResponse> updateImage(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Image ID")
            @PathVariable Long imageId,
            
            @Valid @RequestBody ProductImageUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating image - tenantId: {}, productId: {}, imageId: {}", 
                   tenantId, productId, imageId);

        try {
            // Validate that image belongs to the specified product
            ProductImageEntity existingImage = productImageService.getImage(imageId, tenantId);
            if (!existingImage.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("Image does not belong to the specified product");
            }
            
            ProductImageEntity image = productImageService.updateImage(
                imageId, tenantId, request.imageUrl(), request.altText());
            
            if (request.sortOrder() != null) {
                image = productImageService.updateImageSortOrder(imageId, tenantId, request.sortOrder());
            }
            
            if (Boolean.TRUE.equals(request.isMain())) {
                image = productImageService.setAsMainImage(imageId, tenantId);
            }
            
            ProductImageResponse response = toResponse(image);

            logger.info("Successfully updated image - tenantId: {}, productId: {}, imageId: {}", 
                       tenantId, productId, imageId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update image - tenantId: {}, productId: {}, imageId: {}, error: {}", 
                        tenantId, productId, imageId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update image sort order.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param imageId the image ID
     * @param request the sort order update request
     * @return the updated image
     */
    @PutMapping("/{imageId}/sort-order")
    @Operation(
            summary = "Update image sort order",
            description = "Update the sort order of an image within the product."
    )
    public ResponseEntity<ProductImageResponse> updateImageSortOrder(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Image ID")
            @PathVariable Long imageId,
            
            @Valid @RequestBody ProductImageSortOrderUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating image sort order - tenantId: {}, productId: {}, imageId: {}, sortOrder: {}", 
                   tenantId, productId, imageId, request.sortOrder());

        try {
            // Validate that image belongs to the specified product
            ProductImageEntity existingImage = productImageService.getImage(imageId, tenantId);
            if (!existingImage.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("Image does not belong to the specified product");
            }
            
            ProductImageEntity image = productImageService.updateImageSortOrder(
                imageId, tenantId, request.sortOrder());
            ProductImageResponse response = toResponse(image);

            logger.info("Successfully updated image sort order - tenantId: {}, productId: {}, imageId: {}", 
                       tenantId, productId, imageId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update image sort order - tenantId: {}, productId: {}, imageId: {}, error: {}", 
                        tenantId, productId, imageId, e.getMessage());
            throw e;
        }
    }

    /**
     * Set an image as the main image.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param imageId the image ID
     * @return the updated image
     */
    @PutMapping("/{imageId}/set-main")
    @Operation(
            summary = "Set as main image",
            description = "Set an image as the main image for the product. This will remove the main flag from other images."
    )
    public ResponseEntity<ProductImageResponse> setAsMainImage(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Image ID")
            @PathVariable Long imageId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Setting image as main - tenantId: {}, productId: {}, imageId: {}", 
                   tenantId, productId, imageId);

        try {
            // Validate that image belongs to the specified product
            ProductImageEntity existingImage = productImageService.getImage(imageId, tenantId);
            if (!existingImage.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("Image does not belong to the specified product");
            }
            
            ProductImageEntity image = productImageService.setAsMainImage(imageId, tenantId);
            ProductImageResponse response = toResponse(image);

            logger.info("Successfully set image as main - tenantId: {}, productId: {}, imageId: {}", 
                       tenantId, productId, imageId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to set image as main - tenantId: {}, productId: {}, imageId: {}, error: {}", 
                        tenantId, productId, imageId, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete an image.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param imageId the image ID
     * @return no content response
     */
    @DeleteMapping("/{imageId}")
    @Operation(
            summary = "Delete product image",
            description = "Delete a product image. If this is the main image, another image will be automatically set as main."
    )
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Image ID")
            @PathVariable Long imageId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Deleting image - tenantId: {}, productId: {}, imageId: {}", 
                   tenantId, productId, imageId);

        try {
            // Validate that image belongs to the specified product
            ProductImageEntity existingImage = productImageService.getImage(imageId, tenantId);
            if (!existingImage.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("Image does not belong to the specified product");
            }
            
            productImageService.deleteImage(imageId, tenantId);

            logger.info("Successfully deleted image - tenantId: {}, productId: {}, imageId: {}", 
                       tenantId, productId, imageId);

            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete image - tenantId: {}, productId: {}, imageId: {}, error: {}", 
                        tenantId, productId, imageId, e.getMessage());
            throw e;
        }
    }

    /**
     * Validate that the authenticated user has access to the specified tenant.
     */
    private void validateTenantAccess(Long tenantId) {
        Long authenticatedTenantId = tenantContextHolder.requireTenantId();
        if (!authenticatedTenantId.equals(tenantId)) {
            throw new IllegalArgumentException("Access denied to tenant: " + tenantId);
        }
    }

    /**
     * Convert entity to response DTO.
     */
    private ProductImageResponse toResponse(ProductImageEntity entity) {
        return new ProductImageResponse(
                entity.getId(),
                entity.getProduct().getId(),
                entity.getImageUrl(),
                entity.getAltText(),
                entity.getSortOrder(),
                entity.getIsMain(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}