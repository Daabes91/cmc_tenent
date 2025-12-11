package com.clinic.modules.ecommerce.service;

import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.model.ProductImageEntity;
import com.clinic.modules.ecommerce.repository.ProductImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing product images in the e-commerce system.
 * 
 * Provides CRUD operations for product images with tenant isolation,
 * sort order management, and main image designation functionality.
 */
@Service
public class ProductImageService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageService.class);

    private final ProductImageRepository productImageRepository;
    private final ProductService productService;
    private final EcommerceFeatureService ecommerceFeatureService;

    public ProductImageService(
            ProductImageRepository productImageRepository,
            ProductService productService,
            EcommerceFeatureService ecommerceFeatureService) {
        this.productImageRepository = productImageRepository;
        this.productService = productService;
        this.ecommerceFeatureService = ecommerceFeatureService;
    }

    /**
     * Create a new product image.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @param imageUrl the image URL
     * @param altText the alt text (optional)
     * @return the created image
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public ProductImageEntity createImage(Long productId, Long tenantId, String imageUrl, String altText) {
        log.debug("Creating image for product {} tenant {}: url={}", productId, tenantId, imageUrl);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate required fields
        validateImageUrl(imageUrl);
        
        // Get the product and validate it exists
        ProductEntity product = productService.getProduct(productId, tenantId);
        
        // Create image
        ProductImageEntity image = new ProductImageEntity(product, product.getTenant(), imageUrl);
        
        if (StringUtils.hasText(altText)) {
            image.setAltText(altText);
        }
        
        // Set sort order as the next available for this product
        Optional<Integer> maxSortOrder = productImageRepository.findMaxSortOrderByTenantAndProduct(tenantId, productId);
        image.setSortOrder(maxSortOrder.orElse(0) + 1);
        
        // If this is the first image, make it the main image
        if (!productImageRepository.hasMainImageByTenantAndProduct(tenantId, productId)) {
            image.setIsMain(true);
        }
        
        image = productImageRepository.save(image);
        
        log.info("Created image {} for product {} tenant {}", image.getId(), productId, tenantId);
        return image;
    }

    /**
     * Get an image by ID with tenant validation.
     * 
     * @param imageId the image ID
     * @param tenantId the tenant ID
     * @return the image
     * @throws ProductNotFoundException if image not found
     */
    @Transactional(readOnly = true)
    public ProductImageEntity getImage(Long imageId, Long tenantId) {
        log.debug("Getting image {} for tenant {}", imageId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productImageRepository.findByIdAndTenant(imageId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(null, tenantId, 
                    "Product image with ID " + imageId + " not found"));
    }

    /**
     * Get all images for a product.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @return list of images ordered by sort order
     */
    @Transactional(readOnly = true)
    public List<ProductImageEntity> getImagesByProduct(Long productId, Long tenantId) {
        log.debug("Getting images for product {} tenant {}", productId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate product exists
        productService.getProduct(productId, tenantId);
        
        return productImageRepository.findByTenantAndProductOrderBySortOrder(tenantId, productId);
    }

    /**
     * Get the main image for a product.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @return the main image, or null if none exists
     */
    @Transactional(readOnly = true)
    public ProductImageEntity getMainImageByProduct(Long productId, Long tenantId) {
        log.debug("Getting main image for product {} tenant {}", productId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate product exists
        productService.getProduct(productId, tenantId);
        
        return productImageRepository.findMainImageByTenantAndProduct(tenantId, productId).orElse(null);
    }

    /**
     * Update an image.
     * 
     * @param imageId the image ID
     * @param tenantId the tenant ID
     * @param imageUrl the new image URL (optional)
     * @param altText the new alt text (optional)
     * @return the updated image
     */
    @Transactional
    public ProductImageEntity updateImage(Long imageId, Long tenantId, String imageUrl, String altText) {
        log.debug("Updating image {} for tenant {}", imageId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductImageEntity image = getImage(imageId, tenantId);
        
        // Update fields if provided
        if (StringUtils.hasText(imageUrl)) {
            validateImageUrl(imageUrl);
            image.setImageUrl(imageUrl);
        }
        
        if (altText != null) {
            image.setAltText(altText);
        }
        
        image = productImageRepository.save(image);
        
        log.info("Updated image {} for tenant {}", imageId, tenantId);
        return image;
    }

    /**
     * Update image sort order.
     * 
     * @param imageId the image ID
     * @param tenantId the tenant ID
     * @param newSortOrder the new sort order
     * @return the updated image
     */
    @Transactional
    public ProductImageEntity updateImageSortOrder(Long imageId, Long tenantId, int newSortOrder) {
        log.debug("Updating image {} sort order to {} for tenant {}", imageId, newSortOrder, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductImageEntity image = getImage(imageId, tenantId);
        
        if (newSortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
        
        int currentSortOrder = image.getSortOrder();
        Long productId = image.getProduct().getId();
        
        if (newSortOrder != currentSortOrder) {
            // Adjust other images' sort orders
            if (newSortOrder > currentSortOrder) {
                // Moving down: decrement sort orders between current and new position
                productImageRepository.decrementSortOrderAfter(tenantId, productId, currentSortOrder);
            } else {
                // Moving up: increment sort orders from new position
                productImageRepository.incrementSortOrderFrom(tenantId, productId, newSortOrder);
            }
            
            image.setSortOrder(newSortOrder);
            image = productImageRepository.save(image);
        }
        
        log.info("Updated image {} sort order to {} for tenant {}", imageId, newSortOrder, tenantId);
        return image;
    }

    /**
     * Set an image as the main image for its product.
     * 
     * @param imageId the image ID
     * @param tenantId the tenant ID
     * @return the updated image
     */
    @Transactional
    public ProductImageEntity setAsMainImage(Long imageId, Long tenantId) {
        log.debug("Setting image {} as main for tenant {}", imageId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductImageEntity image = getImage(imageId, tenantId);
        Long productId = image.getProduct().getId();
        
        // Remove main flag from other images of the same product
        productImageRepository.clearMainImageByTenantAndProduct(tenantId, productId);
        
        // Set this image as main
        image.setIsMain(true);
        image = productImageRepository.save(image);
        
        log.info("Set image {} as main for product {} tenant {}", imageId, productId, tenantId);
        return image;
    }

    /**
     * Delete an image.
     * 
     * @param imageId the image ID
     * @param tenantId the tenant ID
     */
    @Transactional
    public void deleteImage(Long imageId, Long tenantId) {
        log.debug("Deleting image {} for tenant {}", imageId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductImageEntity image = getImage(imageId, tenantId);
        Long productId = image.getProduct().getId();
        boolean wasMain = image.isMain();
        
        productImageRepository.delete(image);
        
        // If this was the main image, set another image as main
        if (wasMain) {
            Optional<ProductImageEntity> nextImage = productImageRepository.findFirstImageByTenantAndProduct(tenantId, productId);
            if (nextImage.isPresent()) {
                nextImage.get().setIsMain(true);
                productImageRepository.save(nextImage.get());
                log.info("Set image {} as new main for product {} after deleting main image", 
                        nextImage.get().getId(), productId);
            }
        }
        
        log.info("Deleted image {} for tenant {}", imageId, tenantId);
    }

    /**
     * Get all images for a tenant with pagination.
     * 
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of images
     */
    @Transactional(readOnly = true)
    public Page<ProductImageEntity> getImages(Long tenantId, Pageable pageable) {
        log.debug("Getting images for tenant {} with pagination", tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productImageRepository.findAllByTenant(tenantId, pageable);
    }

    // Private helper methods

    private void validateImageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            throw new IllegalArgumentException("Image URL is required");
        }
        if (imageUrl.length() > 500) {
            throw new IllegalArgumentException("Image URL must not exceed 500 characters");
        }
    }
}