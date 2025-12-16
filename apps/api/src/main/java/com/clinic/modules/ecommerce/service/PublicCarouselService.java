package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.ecommerce.dto.PublicCarouselItemResponse;
import com.clinic.modules.ecommerce.dto.PublicCarouselResponse;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.CarouselRepository;
import com.clinic.modules.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for public carousel operations.
 * 
 * Handles carousel display for public-facing APIs with tenant isolation.
 */
@Service
@Transactional(readOnly = true)
public class PublicCarouselService {

    private static final Logger logger = LoggerFactory.getLogger(PublicCarouselService.class);

    private final CarouselRepository carouselRepository;
    private final ProductRepository productRepository;
    private final TenantService tenantService;
    private final EcommerceFeatureService ecommerceFeatureService;

    @Autowired
    public PublicCarouselService(CarouselRepository carouselRepository,
                                ProductRepository productRepository,
                                TenantService tenantService,
                                EcommerceFeatureService ecommerceFeatureService) {
        this.carouselRepository = carouselRepository;
        this.productRepository = productRepository;
        this.tenantService = tenantService;
        this.ecommerceFeatureService = ecommerceFeatureService;
    }

    /**
     * Resolve tenant from slug or domain parameter.
     * 
     * @param slug the tenant slug (optional)
     * @param domain the tenant domain (optional)
     * @return the resolved tenant
     * @throws IllegalArgumentException if tenant cannot be resolved
     */
    public TenantEntity resolveTenant(String slug, String domain) {
        logger.debug("Resolving tenant from slug: {}, domain: {}", slug, domain);
        
        TenantEntity tenant = null;
        
        // Try to resolve by slug first
        if (StringUtils.hasText(slug)) {
            tenant = tenantService.findActiveBySlug(slug).orElse(null);
        }
        
        // Try to resolve by domain if slug didn't work
        if (tenant == null && StringUtils.hasText(domain)) {
            tenant = tenantService.findActiveByDomain(domain).orElse(null);
        }
        
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant not found for slug: " + slug + ", domain: " + domain);
        }
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenant.getId());
        
        logger.debug("Resolved tenant: {} (ID: {})", tenant.getSlug(), tenant.getId());
        return tenant;
    }

    /**
     * Get carousels by placement and platform for a tenant.
     * 
     * @param tenantId The tenant ID
     * @param placement The carousel placement
     * @param platform The target platform
     * @return List of carousel responses
     */
    public List<PublicCarouselResponse> getCarouselsByPlacementAndPlatform(Long tenantId, String placement, Platform platform, String locale) {
        logger.debug("Getting carousels for tenant: {}, placement: {}, platform: {}", tenantId, placement, platform);
        
        List<CarouselEntity> carousels = carouselRepository.findByTenantIdAndPlacementAndPlatform(tenantId, placement, platform);
        
        return carousels.stream()
                .map(carousel -> convertToPublicResponse(carousel, locale))
                .collect(Collectors.toList());
    }

    /**
     * Get carousels by placement for a tenant (all platforms).
     * 
     * @param tenantId The tenant ID
     * @param placement The carousel placement
     * @return List of carousel responses
     */
    public List<PublicCarouselResponse> getCarouselsByPlacement(Long tenantId, String placement, String locale) {
        logger.debug("Getting carousels for tenant: {}, placement: {}", tenantId, placement);
        
        List<CarouselEntity> carousels = carouselRepository.findByTenantIdAndPlacement(tenantId, placement);
        
        return carousels.stream()
                .map(carousel -> convertToPublicResponse(carousel, locale))
                .collect(Collectors.toList());
    }

    /**
     * Get all active carousels for a tenant.
     * 
     * @param tenantId The tenant ID
     * @return List of carousel responses
     */
    public List<PublicCarouselResponse> getActiveCarousels(Long tenantId, String locale) {
        logger.debug("Getting all active carousels for tenant: {}", tenantId);
        
        List<CarouselEntity> carousels = carouselRepository.findActiveByTenantId(tenantId);
        
        return carousels.stream()
                .map(carousel -> convertToPublicResponse(carousel, locale))
                .collect(Collectors.toList());
    }

    /**
     * Convert carousel entity to public response DTO.
     * 
     * @param carousel The carousel entity
     * @return Public carousel response
     */
    private PublicCarouselResponse convertToPublicResponse(CarouselEntity carousel, String locale) {
        List<PublicCarouselItemResponse> itemResponses;

        if (carousel.getType() == CarouselType.VIEW_ALL_PRODUCTS) {
            itemResponses = buildDynamicProductItems(carousel.getTenant().getId(), carousel.getMaxItems(), locale);
        } else {
            itemResponses = carousel.getItems().stream()
                    .filter(item -> item.getIsActive() != null && item.getIsActive())
                    .map(item -> convertToPublicItemResponse(item, locale))
                    .collect(Collectors.toList());
        }

        PublicCarouselResponse response = new PublicCarouselResponse(
                carousel.getId(),
                localize(carousel.getName(), carousel.getNameAr(), locale),
                carousel.getSlug(),
                carousel.getType(),
                carousel.getPlacement(),
                carousel.getPlatform(),
                carousel.getMaxItems(),
                itemResponses,
                carousel.getCreatedAt(),
                carousel.getUpdatedAt()
        );
        response.setNameAr(carousel.getNameAr());
        return response;
    }

    /**
     * Convert carousel item entity to public response DTO.
     * 
     * @param item The carousel item entity
     * @return Public carousel item response
     */
    private PublicCarouselItemResponse convertToPublicItemResponse(CarouselItemEntity item, String locale) {
        PublicCarouselItemResponse response = new PublicCarouselItemResponse();
        
        response.setId(item.getId());
        response.setContentType(item.getContentType());
        response.setTitle(localize(item.getTitle(), item.getTitleAr(), locale));
        response.setTitleAr(item.getTitleAr());
        response.setSubtitle(localize(item.getSubtitle(), item.getSubtitleAr(), locale));
        response.setSubtitleAr(item.getSubtitleAr());
        response.setImageUrl(item.getImageUrl());
        response.setLinkUrl(item.getLinkUrl());
        response.setCtaType(item.getCtaType());
        response.setCtaText(localize(item.getCtaText(), item.getCtaTextAr(), locale));
        response.setCtaTextAr(item.getCtaTextAr());
        response.setSortOrder(item.getSortOrder());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());

        // Generate mobile-optimized image URL
        response.setMobileImageUrl(generateMobileImageUrl(item.getImageUrl()));

        // Include product information if available
        if (item.getProduct() != null) {
            response.setProduct(convertToProductInfo(item.getProduct(), locale));
        }

        // Include category information if available
        if (item.getCategory() != null) {
            response.setCategory(convertToCategoryInfo(item.getCategory()));
        }

        return response;
    }

    private List<PublicCarouselItemResponse> buildDynamicProductItems(Long tenantId, Integer maxItems, String locale) {
        int limit = (maxItems != null && maxItems > 0) ? maxItems : 10;

        var page = productRepository.findVisibleByTenant(
                tenantId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return page.getContent().stream()
                .map(product -> convertToProductInfo(product, locale))
                .map(productInfo -> {
                    PublicCarouselItemResponse response = new PublicCarouselItemResponse();
                    response.setContentType(CarouselContentType.PRODUCT);
                    response.setProduct(productInfo);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Convert product entity to product info DTO.
     * 
     * @param product The product entity
     * @return Product info DTO
     */
    private PublicCarouselItemResponse.ProductInfo convertToProductInfo(ProductEntity product, String locale) {
        // Get main image URL
        String mainImageUrl = product.getImages().stream()
                .filter(img -> img.getIsMain() != null && img.getIsMain())
                .findFirst()
                .map(ProductImageEntity::getImageUrl)
                .orElse(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl());

        // Collect all image URLs
        List<String> imageUrls = product.getImages().stream()
                .map(ProductImageEntity::getImageUrl)
                .filter(StringUtils::hasText)
                .toList();

        // Check stock availability
        Boolean inStock = product.getHasVariants() != null && product.getHasVariants() ?
                product.getVariants().stream().anyMatch(v -> v.getIsInStock() != null && v.getIsInStock()) :
                true; // Simple products are considered in stock if active

        String localizedName = localize(product.getName(), product.getNameAr(), locale);
        String localizedShortDescription = localize(product.getShortDescription(), product.getShortDescriptionAr(), locale);

        return new PublicCarouselItemResponse.ProductInfo(
                product.getId(),
                localizedName,
                product.getNameAr(),
                product.getSlug(),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getCurrency(),
                localizedShortDescription,
                product.getShortDescriptionAr(),
                mainImageUrl,
                imageUrls,
                inStock
        );
    }

    private String localize(String en, String ar, String locale) {
        boolean useArabic = locale != null && locale.toLowerCase(Locale.ROOT).startsWith("ar");
        if (useArabic && StringUtils.hasText(ar)) {
            return ar;
        }
        if (StringUtils.hasText(en)) {
            return en;
        }
        return ar;
    }

    /**
     * Convert category entity to category info DTO.
     * 
     * @param category The category entity
     * @return Category info DTO
     */
    private PublicCarouselItemResponse.CategoryInfo convertToCategoryInfo(CategoryEntity category) {
        return new PublicCarouselItemResponse.CategoryInfo(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription()
        );
    }

    /**
     * Generate mobile-optimized image URL.
     * 
     * This is a placeholder implementation. In a real system, this would
     * integrate with an image processing service or CDN to provide
     * mobile-optimized versions of images.
     * 
     * @param originalUrl The original image URL
     * @return Mobile-optimized image URL
     */
    private String generateMobileImageUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return null;
        }

        // Simple implementation: add mobile parameter
        // In production, this would integrate with image processing services
        if (originalUrl.contains("?")) {
            return originalUrl + "&mobile=true&w=400&h=300&fit=crop";
        } else {
            return originalUrl + "?mobile=true&w=400&h=300&fit=crop";
        }
    }
}
