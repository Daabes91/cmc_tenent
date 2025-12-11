package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.CarouselRepository;
import com.clinic.modules.ecommerce.repository.CarouselItemRepository;
import com.clinic.modules.ecommerce.repository.ProductRepository;
import com.clinic.modules.ecommerce.repository.CategoryRepository;
import com.clinic.modules.ecommerce.exception.EcommerceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing carousels in the admin interface.
 * 
 * Provides CRUD operations for carousels and carousel items with proper
 * tenant isolation and content type validation.
 */
@Service
@Transactional
public class CarouselService {

    private final CarouselRepository carouselRepository;
    private final CarouselItemRepository carouselItemRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public CarouselService(
            CarouselRepository carouselRepository,
            CarouselItemRepository carouselItemRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.carouselRepository = carouselRepository;
        this.carouselItemRepository = carouselItemRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Create a new carousel.
     */
    public CarouselEntity createCarousel(TenantEntity tenant, String name, String slug, 
                                       CarouselType type, String placement, Platform platform) {
        validateCarouselType(type);
        validatePlatform(platform);
        validatePlacement(placement);
        
        if (carouselRepository.existsByTenantIdAndSlug(tenant.getId(), slug)) {
            throw new EcommerceException("Carousel with slug '" + slug + "' already exists");
        }

        CarouselEntity carousel = new CarouselEntity(tenant, name, slug, type, placement);
        carousel.setPlatform(platform);
        
        return carouselRepository.save(carousel);
    }

    /**
     * Update an existing carousel.
     */
    public CarouselEntity updateCarousel(Long tenantId, Long carouselId, String name, String slug,
                                       CarouselType type, String placement, Platform platform,
                                       Boolean isActive, Integer maxItems) {
        CarouselEntity carousel = getCarouselByTenantAndId(tenantId, carouselId);
        
        validateCarouselType(type);
        validatePlatform(platform);
        validatePlacement(placement);
        
        // Check slug uniqueness if changed
        if (!carousel.getSlug().equals(slug) && 
            carouselRepository.existsByTenantIdAndSlugAndIdNot(tenantId, slug, carouselId)) {
            throw new EcommerceException("Carousel with slug '" + slug + "' already exists");
        }

        carousel.setName(name);
        carousel.setSlug(slug);
        carousel.setType(type);
        carousel.setPlacement(placement);
        carousel.setPlatform(platform);
        
        if (isActive != null) {
            carousel.setIsActive(isActive);
        }
        
        if (maxItems != null && maxItems > 0) {
            carousel.setMaxItems(maxItems);
        }
        
        return carouselRepository.save(carousel);
    }

    /**
     * Get carousel by tenant and ID.
     */
    @Transactional(readOnly = true)
    public CarouselEntity getCarouselByTenantAndId(Long tenantId, Long carouselId) {
        return carouselRepository.findByTenantIdAndId(tenantId, carouselId)
                .orElseThrow(() -> new EcommerceException("Carousel not found"));
    }

    /**
     * Get carousel by tenant and slug.
     */
    @Transactional(readOnly = true)
    public Optional<CarouselEntity> getCarouselByTenantAndSlug(Long tenantId, String slug) {
        return carouselRepository.findByTenantIdAndSlug(tenantId, slug);
    }

    /**
     * Get all carousels for a tenant with pagination.
     */
    @Transactional(readOnly = true)
    public Page<CarouselEntity> getCarouselsByTenant(Long tenantId, Pageable pageable) {
        return carouselRepository.findByTenantId(tenantId, pageable);
    }

    /**
     * Get active carousels for a tenant.
     */
    @Transactional(readOnly = true)
    public List<CarouselEntity> getActiveCarouselsByTenant(Long tenantId) {
        return carouselRepository.findActiveByTenantId(tenantId);
    }

    /**
     * Get carousels by placement for a tenant.
     */
    @Transactional(readOnly = true)
    public List<CarouselEntity> getCarouselsByPlacement(Long tenantId, String placement) {
        return carouselRepository.findByTenantIdAndPlacement(tenantId, placement);
    }

    /**
     * Delete a carousel.
     */
    public void deleteCarousel(Long tenantId, Long carouselId) {
        CarouselEntity carousel = getCarouselByTenantAndId(tenantId, carouselId);
        carouselRepository.delete(carousel);
    }

    /**
     * Add an item to a carousel.
     */
    public CarouselItemEntity addCarouselItem(Long tenantId, Long carouselId, 
                                            CarouselContentType contentType, String title, 
                                            String subtitle, String imageUrl, String linkUrl,
                                            CallToActionType ctaType, String ctaText,
                                            Long productId, Long categoryId) {
        CarouselEntity carousel = getCarouselByTenantAndId(tenantId, carouselId);
        
        validateContentType(contentType);
        validateCallToActionType(ctaType);
        
        CarouselItemEntity item = new CarouselItemEntity(carousel, carousel.getTenant(), contentType);
        item.setTitle(title);
        item.setSubtitle(subtitle);
        item.setImageUrl(imageUrl);
        item.setLinkUrl(linkUrl);
        item.setCtaType(ctaType);
        item.setCtaText(ctaText);
        
        // Validate and set product/category references
        if (productId != null) {
            validateProductReference(tenantId, productId, contentType);
            ProductEntity product = productRepository.findByTenantIdAndId(tenantId, productId)
                    .orElseThrow(() -> new EcommerceException("Product not found"));
            item.setProduct(product);
        }
        
        if (categoryId != null) {
            validateCategoryReference(tenantId, categoryId, contentType);
            CategoryEntity category = categoryRepository.findByTenantIdAndId(tenantId, categoryId)
                    .orElseThrow(() -> new EcommerceException("Category not found"));
            item.setCategory(category);
        }
        
        // Set sort order
        Integer maxSortOrder = carouselItemRepository.getMaxSortOrderByCarouselIdAndTenantId(carouselId, tenantId);
        item.setSortOrder(maxSortOrder + 1);
        
        // Validate content type specific requirements
        validateCarouselItemContent(item);
        
        return carouselItemRepository.save(item);
    }

    /**
     * Update a carousel item.
     */
    public CarouselItemEntity updateCarouselItem(Long tenantId, Long carouselId, Long itemId,
                                               String title, String subtitle, String imageUrl,
                                               String linkUrl, CallToActionType ctaType,
                                               String ctaText, Boolean isActive, Integer sortOrder) {
        CarouselItemEntity item = carouselItemRepository.findByIdAndTenantId(itemId, tenantId)
                .orElseThrow(() -> new EcommerceException("Carousel item not found"));

        if (!item.getCarousel().getId().equals(carouselId)) {
            throw new EcommerceException("Carousel item does not belong to the specified carousel");
        }
        
        if (ctaType != null) {
            validateCallToActionType(ctaType);
            item.setCtaType(ctaType);
        }
        
        if (StringUtils.hasText(title)) {
            item.setTitle(title);
        }
        
        if (StringUtils.hasText(subtitle)) {
            item.setSubtitle(subtitle);
        }
        
        if (StringUtils.hasText(imageUrl)) {
            item.setImageUrl(imageUrl);
        }
        
        if (StringUtils.hasText(linkUrl)) {
            item.setLinkUrl(linkUrl);
        }
        
        if (StringUtils.hasText(ctaText)) {
            item.setCtaText(ctaText);
        }
        
        if (isActive != null) {
            item.setIsActive(isActive);
        }
        
        if (sortOrder != null && sortOrder >= 0) {
            item.setSortOrder(sortOrder);
        }
        
        validateCarouselItemContent(item);
        
        return carouselItemRepository.save(item);
    }

    /**
     * Delete a carousel item.
     */
    public void deleteCarouselItem(Long tenantId, Long carouselId, Long itemId) {
        CarouselItemEntity item = carouselItemRepository.findByIdAndTenantId(itemId, tenantId)
                .orElseThrow(() -> new EcommerceException("Carousel item not found"));
        if (!item.getCarousel().getId().equals(carouselId)) {
            throw new EcommerceException("Carousel item does not belong to the specified carousel");
        }
        carouselItemRepository.delete(item);
    }

    /**
     * Get carousel items for a carousel.
     */
    @Transactional(readOnly = true)
    public List<CarouselItemEntity> getCarouselItems(Long tenantId, Long carouselId) {
        return carouselItemRepository.findByCarouselIdAndTenantId(carouselId, tenantId);
    }

    /**
     * Reorder carousel items.
     */
    public void reorderCarouselItems(Long tenantId, Long carouselId, List<Long> itemIds) {
        CarouselEntity carousel = getCarouselByTenantAndId(tenantId, carouselId);
        
        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            CarouselItemEntity item = carouselItemRepository.findByIdAndTenantId(itemId, tenantId)
                    .orElseThrow(() -> new EcommerceException("Carousel item not found: " + itemId));
            
            if (!item.getCarousel().getId().equals(carouselId)) {
                throw new EcommerceException("Item does not belong to the specified carousel");
            }
            
            item.setSortOrder(i);
            carouselItemRepository.save(item);
        }
    }

    // Validation methods

    private void validateCarouselType(CarouselType type) {
        if (type == null) {
            throw new EcommerceException("Carousel type is required");
        }
        // CarouselType enum already validates valid values
    }

    private void validatePlatform(Platform platform) {
        if (platform == null) {
            throw new EcommerceException("Platform is required");
        }
        // Platform enum already validates valid values
    }

    private void validatePlacement(String placement) {
        if (!StringUtils.hasText(placement)) {
            throw new EcommerceException("Placement is required");
        }
        
        // Validate placement values
        String[] validPlacements = {"HEADER", "HERO", "SIDEBAR", "FOOTER", "CATEGORY_PAGE", "PRODUCT_PAGE", "HOME_PAGE"};
        boolean isValid = false;
        for (String validPlacement : validPlacements) {
            if (validPlacement.equals(placement)) {
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            throw new EcommerceException("Invalid placement: " + placement);
        }
    }

    private void validateContentType(CarouselContentType contentType) {
        if (contentType == null) {
            throw new EcommerceException("Content type is required");
        }
        // CarouselContentType enum already validates valid values
    }

    private void validateCallToActionType(CallToActionType ctaType) {
        if (ctaType == null) {
            throw new EcommerceException("Call-to-action type is required");
        }
        // CallToActionType enum already validates valid values
    }

    private void validateProductReference(Long tenantId, Long productId, CarouselContentType contentType) {
        if (contentType == CarouselContentType.PRODUCT && productId == null) {
            throw new EcommerceException("Product ID is required for PRODUCT content type");
        }
        
        if (productId != null && !productRepository.existsByTenantIdAndId(tenantId, productId)) {
            throw new EcommerceException("Product not found: " + productId);
        }
    }

    private void validateCategoryReference(Long tenantId, Long categoryId, CarouselContentType contentType) {
        if (contentType == CarouselContentType.CATEGORY && categoryId == null) {
            throw new EcommerceException("Category ID is required for CATEGORY content type");
        }
        
        if (categoryId != null && !categoryRepository.existsByTenantIdAndId(tenantId, categoryId)) {
            throw new EcommerceException("Category not found: " + categoryId);
        }
    }

    private void validateCarouselItemContent(CarouselItemEntity item) {
        CarouselContentType contentType = item.getContentType();
        
        switch (contentType) {
            case IMAGE:
                if (!StringUtils.hasText(item.getImageUrl())) {
                    throw new EcommerceException("Image URL is required for IMAGE content type");
                }
                break;
                
            case PRODUCT:
                if (item.getProduct() == null) {
                    throw new EcommerceException("Product reference is required for PRODUCT content type");
                }
                break;
                
            case CATEGORY:
                if (item.getCategory() == null) {
                    throw new EcommerceException("Category reference is required for CATEGORY content type");
                }
                break;
                
            case BRAND:
            case OFFER:
            case TESTIMONIAL:
            case BLOG:
                if (!StringUtils.hasText(item.getTitle())) {
                    throw new EcommerceException("Title is required for " + contentType + " content type");
                }
                break;
                
            default:
                throw new EcommerceException("Unsupported content type: " + contentType);
        }
        
        // Validate CTA requirements
        if (item.getCtaType() == CallToActionType.ADD_TO_CART && item.getProduct() == null) {
            throw new EcommerceException("Product reference is required for ADD_TO_CART call-to-action");
        }
        
        if (item.getCtaType() == CallToActionType.LINK && !StringUtils.hasText(item.getLinkUrl())) {
            throw new EcommerceException("Link URL is required for LINK call-to-action");
        }
    }
}
