package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.dto.PublicProductResponse;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.model.ProductStatus;
import com.clinic.modules.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Service for public product browsing operations.
 * 
 * Provides read-only access to products for customer-facing APIs.
 * All operations are tenant-scoped and only return visible, active products.
 */
@Service
public class PublicProductService {

    private static final Logger log = LoggerFactory.getLogger(PublicProductService.class);

    private final ProductRepository productRepository;
    private final TenantService tenantService;
    private final EcommerceFeatureService ecommerceFeatureService;

    public PublicProductService(
            ProductRepository productRepository,
            TenantService tenantService,
            EcommerceFeatureService ecommerceFeatureService) {
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
        log.debug("Resolving tenant from slug: {}, domain: {}", slug, domain);
        
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
        
        log.debug("Resolved tenant: {} (ID: {})", tenant.getSlug(), tenant.getId());
        return tenant;
    }

    /**
     * Get all visible products for a tenant with pagination.
     * 
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of visible products
     */
    @Transactional(readOnly = true)
    public Page<PublicProductResponse> getVisibleProducts(Long tenantId, Pageable pageable, String locale) {
        log.debug("Getting visible products for tenant {} with pagination", tenantId);
        
        Page<ProductEntity> products = productRepository.findVisibleByTenant(tenantId, pageable);
        return products.map(product -> PublicProductResponse.fromEntity(product, locale));
    }

    /**
     * Get a product by ID for public access.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @return the product response
     * @throws ProductNotFoundException if product not found or not visible
     */
    @Transactional(readOnly = true)
    public PublicProductResponse getVisibleProduct(Long productId, Long tenantId, String locale) {
        log.debug("Getting visible product {} for tenant {}", productId, tenantId);
        
        ProductEntity product = productRepository.findByIdAndTenant(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(productId, tenantId));
        
        // Ensure product is visible and active
        if (!product.isVisible() || product.getStatus() != ProductStatus.ACTIVE) {
            throw new ProductNotFoundException(productId, tenantId, "Product not available");
        }
        
        return PublicProductResponse.fromEntity(product, locale);
    }

    /**
     * Get a product by slug for public access.
     * 
     * @param slug the product slug
     * @param tenantId the tenant ID
     * @return the product response
     * @throws ProductNotFoundException if product not found or not visible
     */
    @Transactional(readOnly = true)
    public PublicProductResponse getVisibleProductBySlug(String slug, Long tenantId, String locale) {
        log.debug("Getting visible product by slug {} for tenant {}", slug, tenantId);
        
        ProductEntity product = productRepository.findBySlugAndTenant(slug, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(null, tenantId, "Product with slug " + slug + " not found"));
        
        // Ensure product is visible and active
        if (!product.isVisible() || product.getStatus() != ProductStatus.ACTIVE) {
            throw new ProductNotFoundException(null, tenantId, "Product not available");
        }
        
        return PublicProductResponse.fromEntity(product, locale);
    }

    /**
     * Search visible products for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param searchTerm the search term
     * @param pageable pagination parameters
     * @return page of matching products
     */
    @Transactional(readOnly = true)
    public Page<PublicProductResponse> searchVisibleProducts(Long tenantId, String searchTerm, Pageable pageable, String locale) {
        log.debug("Searching visible products for tenant {} with term: {}", tenantId, searchTerm);
        
        Page<ProductEntity> products = productRepository.searchVisibleByTenant(tenantId, searchTerm, pageable);
        return products.map(product -> PublicProductResponse.fromEntity(product, locale));
    }

    /**
     * Get visible products by category for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param categoryId the category ID
     * @param pageable pagination parameters
     * @return page of products in the category
     */
    @Transactional(readOnly = true)
    public Page<PublicProductResponse> getVisibleProductsByCategory(Long tenantId, Long categoryId, Pageable pageable, String locale) {
        log.debug("Getting visible products by category {} for tenant {}", categoryId, tenantId);
        
        Page<ProductEntity> products = productRepository.findVisibleByTenantAndCategory(tenantId, categoryId, pageable);
        return products.map(product -> PublicProductResponse.fromEntity(product, locale));
    }

    /**
     * Get visible products within a price range for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param minPrice the minimum price (optional)
     * @param maxPrice the maximum price (optional)
     * @param pageable pagination parameters
     * @return page of products within the price range
     */
    @Transactional(readOnly = true)
    public Page<PublicProductResponse> getVisibleProductsByPriceRange(Long tenantId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable, String locale) {
        log.debug("Getting visible products by price range [{}, {}] for tenant {}", minPrice, maxPrice, tenantId);
        
        Page<ProductEntity> products = productRepository.findVisibleByTenantAndPriceBetween(tenantId, minPrice, maxPrice, pageable);
        return products.map(product -> PublicProductResponse.fromEntity(product, locale));
    }

    /**
     * Get visible products with complex filtering.
     * 
     * @param tenantId the tenant ID
     * @param categoryId the category ID (optional)
     * @param searchTerm the search term (optional)
     * @param minPrice the minimum price (optional)
     * @param maxPrice the maximum price (optional)
     * @param pageable pagination parameters
     * @return page of filtered products
     */
    @Transactional(readOnly = true)
    public Page<PublicProductResponse> getVisibleProductsWithFilters(
            Long tenantId, 
            Long categoryId, 
            String searchTerm, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            Pageable pageable,
            String locale) {
        
        log.debug("Getting visible products with filters for tenant {}: category={}, search={}, priceRange=[{}, {}]", 
                tenantId, categoryId, searchTerm, minPrice, maxPrice);
        
        Page<ProductEntity> products = productRepository.findByTenantWithFilters(
                tenantId, 
                ProductStatus.ACTIVE, 
                true, // visible only
                categoryId, 
                searchTerm, 
                minPrice, 
                maxPrice, 
                pageable
        );
        
        return products.map(product -> PublicProductResponse.fromEntity(product, locale));
    }

    /**
     * Get recently added visible products for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of recent products
     */
    @Transactional(readOnly = true)
    public Page<PublicProductResponse> getRecentVisibleProducts(Long tenantId, Pageable pageable, String locale) {
        log.debug("Getting recent visible products for tenant {}", tenantId);
        
        Page<ProductEntity> products = productRepository.findRecentVisibleByTenant(tenantId, pageable);
        return products.map(product -> PublicProductResponse.fromEntity(product, locale));
    }

    /**
     * Count visible products for a tenant.
     * 
     * @param tenantId the tenant ID
     * @return the count of visible products
     */
    @Transactional(readOnly = true)
    public long countVisibleProducts(Long tenantId) {
        log.debug("Counting visible products for tenant {}", tenantId);
        
        return productRepository.countVisibleByTenant(tenantId);
    }
}
