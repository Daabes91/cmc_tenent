package com.clinic.modules.ecommerce.service;

import com.clinic.modules.ecommerce.exception.InsufficientStockException;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.model.ProductVariantEntity;
import com.clinic.modules.ecommerce.repository.ProductVariantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing product variants in the e-commerce system.
 * 
 * Provides CRUD operations for product variants with tenant isolation,
 * SKU validation, and stock management functionality.
 */
@Service
public class ProductVariantService {

    private static final Logger log = LoggerFactory.getLogger(ProductVariantService.class);

    private final ProductVariantRepository productVariantRepository;
    private final ProductService productService;
    private final EcommerceFeatureService ecommerceFeatureService;

    public ProductVariantService(
            ProductVariantRepository productVariantRepository,
            ProductService productService,
            EcommerceFeatureService ecommerceFeatureService) {
        this.productVariantRepository = productVariantRepository;
        this.productService = productService;
        this.ecommerceFeatureService = ecommerceFeatureService;
    }

    /**
     * Create a new product variant.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @param sku the variant SKU
     * @param name the variant name
     * @param price the variant price
     * @return the created variant
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public ProductVariantEntity createVariant(Long productId, Long tenantId, String sku, String name, BigDecimal price) {
        log.debug("Creating variant for product {} tenant {}: sku={}, name={}, price={}", 
                 productId, tenantId, sku, name, price);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate required fields
        validateVariantSku(sku);
        validateVariantName(name);
        validateVariantPrice(price);
        
        // Get the product and validate it exists
        ProductEntity product = productService.getProduct(productId, tenantId);
        
        // Check SKU uniqueness within tenant
        if (productVariantRepository.existsBySkuAndTenant(sku, tenantId)) {
            throw new IllegalArgumentException("Variant SKU already exists: " + sku);
        }
        
        // Create variant
        ProductVariantEntity variant = new ProductVariantEntity(product, product.getTenant(), sku, name, price);
        variant = productVariantRepository.save(variant);
        
        // Update product to indicate it has variants
        if (!product.hasVariants()) {
            product.setHasVariants(true);
            // Note: We don't save the product here to avoid circular dependency
            // The product will be updated in a separate transaction if needed
        }
        
        log.info("Created variant {} for product {} tenant {}", variant.getId(), productId, tenantId);
        return variant;
    }

    /**
     * Get a variant by ID with tenant validation.
     * 
     * @param variantId the variant ID
     * @param tenantId the tenant ID
     * @return the variant
     * @throws ProductNotFoundException if variant not found
     */
    @Transactional(readOnly = true)
    public ProductVariantEntity getVariant(Long variantId, Long tenantId) {
        log.debug("Getting variant {} for tenant {}", variantId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productVariantRepository.findByIdAndTenant(variantId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(null, tenantId, 
                    "Product variant with ID " + variantId + " not found"));
    }

    /**
     * Get a variant by SKU with tenant validation.
     * 
     * @param sku the variant SKU
     * @param tenantId the tenant ID
     * @return the variant
     * @throws ProductNotFoundException if variant not found
     */
    @Transactional(readOnly = true)
    public ProductVariantEntity getVariantBySku(String sku, Long tenantId) {
        log.debug("Getting variant by SKU {} for tenant {}", sku, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productVariantRepository.findBySkuAndTenant(sku, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(null, tenantId, 
                    "Product variant with SKU " + sku + " not found"));
    }

    /**
     * Get all variants for a product.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @return list of variants
     */
    @Transactional(readOnly = true)
    public List<ProductVariantEntity> getVariantsByProduct(Long productId, Long tenantId) {
        log.debug("Getting variants for product {} tenant {}", productId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate product exists
        productService.getProduct(productId, tenantId);
        
        return productVariantRepository.findByTenantAndProduct(tenantId, productId);
    }

    /**
     * Get in-stock variants for a product.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @return list of in-stock variants
     */
    @Transactional(readOnly = true)
    public List<ProductVariantEntity> getInStockVariantsByProduct(Long productId, Long tenantId) {
        log.debug("Getting in-stock variants for product {} tenant {}", productId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        // Validate product exists
        productService.getProduct(productId, tenantId);
        
        return productVariantRepository.findInStockByTenantAndProduct(tenantId, productId);
    }

    /**
     * Update a variant.
     * 
     * @param variantId the variant ID
     * @param tenantId the tenant ID
     * @param name the new name (optional)
     * @param price the new price (optional)
     * @param compareAtPrice the new compare at price (optional)
     * @return the updated variant
     */
    @Transactional
    public ProductVariantEntity updateVariant(Long variantId, Long tenantId, String name, 
                                            BigDecimal price, BigDecimal compareAtPrice) {
        log.debug("Updating variant {} for tenant {}", variantId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductVariantEntity variant = getVariant(variantId, tenantId);
        
        // Update fields if provided
        if (StringUtils.hasText(name)) {
            validateVariantName(name);
            variant.setName(name);
        }
        
        if (price != null) {
            validateVariantPrice(price);
            variant.setPrice(price);
        }
        
        if (compareAtPrice != null) {
            validateVariantPrice(compareAtPrice);
            variant.setCompareAtPrice(compareAtPrice);
        }
        
        variant = productVariantRepository.save(variant);
        
        log.info("Updated variant {} for tenant {}", variantId, tenantId);
        return variant;
    }

    /**
     * Update variant SKU with uniqueness validation.
     * 
     * @param variantId the variant ID
     * @param tenantId the tenant ID
     * @param sku the new SKU
     * @return the updated variant
     */
    @Transactional
    public ProductVariantEntity updateVariantSku(Long variantId, Long tenantId, String sku) {
        log.debug("Updating variant {} SKU to {} for tenant {}", variantId, sku, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductVariantEntity variant = getVariant(variantId, tenantId);
        
        // Validate SKU
        validateVariantSku(sku);
        
        // Check SKU uniqueness (excluding current variant)
        if (productVariantRepository.existsBySkuAndTenantExcludingId(sku, tenantId, variantId)) {
            throw new IllegalArgumentException("Variant SKU already exists: " + sku);
        }
        
        variant.setSku(sku);
        variant = productVariantRepository.save(variant);
        
        log.info("Updated variant {} SKU for tenant {}", variantId, tenantId);
        return variant;
    }

    /**
     * Update variant stock quantity.
     * 
     * @param variantId the variant ID
     * @param tenantId the tenant ID
     * @param stockQuantity the new stock quantity
     * @return the updated variant
     */
    @Transactional
    public ProductVariantEntity updateVariantStock(Long variantId, Long tenantId, Integer stockQuantity) {
        log.debug("Updating variant {} stock to {} for tenant {}", variantId, stockQuantity, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductVariantEntity variant = getVariant(variantId, tenantId);
        
        // Validate stock quantity
        if (stockQuantity != null && stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        
        variant.setStockQuantity(stockQuantity);
        variant = productVariantRepository.save(variant);
        
        log.info("Updated variant {} stock to {} for tenant {}", variantId, stockQuantity, tenantId);
        return variant;
    }

    /**
     * Decrease variant stock by specified quantity.
     * 
     * @param variantId the variant ID
     * @param tenantId the tenant ID
     * @param quantity the quantity to decrease
     * @return the updated variant
     * @throws InsufficientStockException if insufficient stock
     */
    @Transactional
    public ProductVariantEntity decreaseStock(Long variantId, Long tenantId, int quantity) {
        log.debug("Decreasing variant {} stock by {} for tenant {}", variantId, quantity, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductVariantEntity variant = getVariant(variantId, tenantId);
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        try {
            variant.decreaseStock(quantity);
            variant = productVariantRepository.save(variant);
            
            log.info("Decreased variant {} stock by {} for tenant {}", variantId, quantity, tenantId);
            return variant;
        } catch (IllegalArgumentException e) {
            throw new InsufficientStockException("Insufficient stock for variant " + variantId + ": " + e.getMessage());
        }
    }

    /**
     * Increase variant stock by specified quantity.
     * 
     * @param variantId the variant ID
     * @param tenantId the tenant ID
     * @param quantity the quantity to increase
     * @return the updated variant
     */
    @Transactional
    public ProductVariantEntity increaseStock(Long variantId, Long tenantId, int quantity) {
        log.debug("Increasing variant {} stock by {} for tenant {}", variantId, quantity, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductVariantEntity variant = getVariant(variantId, tenantId);
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        variant.increaseStock(quantity);
        variant = productVariantRepository.save(variant);
        
        log.info("Increased variant {} stock by {} for tenant {}", variantId, quantity, tenantId);
        return variant;
    }

    /**
     * Check if variant can fulfill the requested quantity.
     * 
     * @param variantId the variant ID
     * @param tenantId the tenant ID
     * @param quantity the requested quantity
     * @return true if can fulfill, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canFulfillQuantity(Long variantId, Long tenantId, int quantity) {
        log.debug("Checking if variant {} can fulfill quantity {} for tenant {}", variantId, quantity, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductVariantEntity variant = getVariant(variantId, tenantId);
        return variant.canFulfillQuantity(quantity);
    }

    /**
     * Delete a variant.
     * 
     * @param variantId the variant ID
     * @param tenantId the tenant ID
     */
    @Transactional
    public void deleteVariant(Long variantId, Long tenantId) {
        log.debug("Deleting variant {} for tenant {}", variantId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductVariantEntity variant = getVariant(variantId, tenantId);
        productVariantRepository.delete(variant);
        
        log.info("Deleted variant {} for tenant {}", variantId, tenantId);
    }

    /**
     * Get all variants for a tenant with pagination.
     * 
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of variants
     */
    @Transactional(readOnly = true)
    public Page<ProductVariantEntity> getVariants(Long tenantId, Pageable pageable) {
        log.debug("Getting variants for tenant {} with pagination", tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productVariantRepository.findAllByTenant(tenantId, pageable);
    }

    /**
     * Get low stock variants for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param threshold the stock threshold
     * @return list of low stock variants
     */
    @Transactional(readOnly = true)
    public List<ProductVariantEntity> getLowStockVariants(Long tenantId, int threshold) {
        log.debug("Getting low stock variants for tenant {} with threshold {}", tenantId, threshold);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productVariantRepository.findLowStockByTenant(tenantId, threshold);
    }

    /**
     * Search variants for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param searchTerm the search term
     * @param pageable pagination parameters
     * @return page of variants
     */
    @Transactional(readOnly = true)
    public Page<ProductVariantEntity> searchVariants(Long tenantId, String searchTerm, Pageable pageable) {
        log.debug("Searching variants for tenant {} with term: {}", tenantId, searchTerm);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productVariantRepository.searchByTenant(tenantId, searchTerm, pageable);
    }

    // Private helper methods

    private void validateVariantSku(String sku) {
        if (!StringUtils.hasText(sku)) {
            throw new IllegalArgumentException("Variant SKU is required");
        }
        if (sku.length() > 100) {
            throw new IllegalArgumentException("Variant SKU must not exceed 100 characters");
        }
    }

    private void validateVariantName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Variant name is required");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Variant name must not exceed 255 characters");
        }
    }

    private void validateVariantPrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Variant price is required");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Variant price must be greater than zero");
        }
    }
}