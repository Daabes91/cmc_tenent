package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing products in the e-commerce system.
 * 
 * Provides CRUD operations for products with tenant isolation and business rule validation.
 * Handles product status transitions, SKU validation, and variant management.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;
    private final EcommerceFeatureService ecommerceFeatureService;

    public ProductService(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductVariantRepository productVariantRepository,
            ProductCategoryRepository productCategoryRepository,
            CategoryRepository categoryRepository,
            TenantRepository tenantRepository,
            EcommerceFeatureService ecommerceFeatureService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.tenantRepository = tenantRepository;
        this.ecommerceFeatureService = ecommerceFeatureService;
    }

    /**
     * Create a new product.
     * 
     * @param tenantId the tenant ID
     * @param name the product name
     * @param slug the product slug
     * @return the created product
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public ProductEntity createProduct(Long tenantId,
                                       String name,
                                       String nameAr,
                                       String slug,
                                       String sku,
                                       String description,
                                       String descriptionAr,
                                       String shortDescription,
                                       String shortDescriptionAr,
                                       java.math.BigDecimal price,
                                       java.math.BigDecimal compareAtPrice,
                                       String currency,
                                       Boolean isTaxable,
                                       Boolean isVisible,
                                       java.util.List<String> images) {
        log.debug("Creating product for tenant {}: name={}, slug={}, sku={}", tenantId, name, slug, sku);

        ecommerceFeatureService.validateEcommerceEnabled(tenantId);

        validateProductName(name);
        validateProductSlug(slug);
        if (sku != null && !sku.isBlank()) {
            validateSku(sku);
            if (productRepository.existsBySkuAndTenant(sku, tenantId)) {
                throw new IllegalArgumentException("SKU already exists: " + sku);
            }
        }
        if (price != null) {
            validatePrice(price);
        }
        if (compareAtPrice != null) {
            validatePrice(compareAtPrice);
        }
        if (currency != null && currency.length() > 3) {
            throw new IllegalArgumentException("Currency must be 3 characters");
        }

        TenantEntity tenant = getTenantById(tenantId);

        if (productRepository.existsBySlugAndTenant(slug, tenantId)) {
            throw new IllegalArgumentException("Product slug already exists: " + slug);
        }

        ProductEntity product = new ProductEntity(tenant, name, slug);
        product.setSku(sku);
        product.setDescription(description);
        product.setDescriptionAr(descriptionAr);
        product.setShortDescription(shortDescription);
        product.setShortDescriptionAr(shortDescriptionAr);
        product.setNameAr(nameAr);
        if (price != null) product.setPrice(price);
        if (compareAtPrice != null) product.setCompareAtPrice(compareAtPrice);
        if (currency != null && !currency.isBlank()) product.setCurrency(currency);
        if (isTaxable != null) product.setIsTaxable(isTaxable);
        if (isVisible != null) product.setIsVisible(isVisible);

        product = productRepository.save(product);

        // Optional images
        if (images != null && !images.isEmpty()) {
            int sortOrder = 1;
            boolean hasMain = productImageRepository.hasMainImageByTenantAndProduct(tenantId, product.getId());
            for (String url : images) {
                if (!StringUtils.hasText(url)) {
                    continue;
                }
                ProductImageEntity image = new ProductImageEntity(product, tenant, url);
                image.setSortOrder(sortOrder++);
                if (!hasMain) {
                    image.setIsMain(true);
                    hasMain = true;
                }
                productImageRepository.save(image);
            }
        }
        log.info("Created product {} for tenant {}", product.getId(), tenantId);
        return product;
    }

    /**
     * Get a product by ID with tenant validation.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @return the product
     * @throws ProductNotFoundException if product not found
     */
    @Transactional(readOnly = true)
    public ProductEntity getProduct(Long productId, Long tenantId) {
        log.debug("Getting product {} for tenant {}", productId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productRepository.findByIdAndTenant(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(productId, tenantId));
    }

    /**
     * Get a product by ID with tenant validation and initialize collections.
     * This method is used when the product collections (variants, images, categories) are needed.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @return the product with collections initialized
     * @throws ProductNotFoundException if product not found
     */
    @Transactional(readOnly = true)
    public ProductEntity getProductWithCollections(Long productId, Long tenantId) {
        log.debug("Getting product with collections {} for tenant {}", productId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductEntity product = productRepository.findByIdAndTenant(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(productId, tenantId));
        
        // Initialize collections within the transaction to avoid LazyInitializationException
        product.getVariants().size(); // Force initialization
        product.getImages().size(); // Force initialization
        product.getProductCategories().size(); // Force initialization
        
        // Initialize nested category entities
        product.getProductCategories().forEach(pc -> pc.getCategory().getName());
        
        return product;
    }

    /**
     * Get a product by slug with tenant validation.
     * 
     * @param slug the product slug
     * @param tenantId the tenant ID
     * @return the product
     * @throws ProductNotFoundException if product not found
     */
    @Transactional(readOnly = true)
    public ProductEntity getProductBySlug(String slug, Long tenantId) {
        log.debug("Getting product by slug {} for tenant {}", slug, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productRepository.findBySlugAndTenant(slug, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(null, tenantId, "Product with slug " + slug + " not found"));
    }

    /**
     * Update a product.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @param name the new name (optional)
     * @param description the new description (optional)
     * @param shortDescription the new short description (optional)
     * @param price the new price (optional)
     * @param compareAtPrice the new compare at price (optional)
     * @return the updated product
     */
    @Transactional
    public ProductEntity updateProduct(Long productId, Long tenantId, String name, String nameAr, String slug, String description, 
                                     String descriptionAr, String shortDescription, String shortDescriptionAr,
                                     BigDecimal price, BigDecimal compareAtPrice,
                                     Boolean isTaxable, Boolean isVisible, String currency, ProductStatus status,
                                     List<Long> categoryIds) {
        log.debug("Updating product {} for tenant {}", productId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductEntity product = getProduct(productId, tenantId);
        
        // Update fields if provided
        if (StringUtils.hasText(name)) {
            validateProductName(name);
            product.setName(name);
        }

        if (nameAr != null) {
            product.setNameAr(nameAr);
        }

        if (StringUtils.hasText(slug)) {
            validateProductSlug(slug);
            if (productRepository.existsBySlugAndTenantExcludingId(slug, tenantId, productId)) {
                throw new IllegalArgumentException("Product slug already exists: " + slug);
            }
            product.setSlug(slug);
        }
        
        if (description != null) {
            product.setDescription(description);
        }

        if (descriptionAr != null) {
            product.setDescriptionAr(descriptionAr);
        }
        
        if (shortDescription != null) {
            product.setShortDescription(shortDescription);
        }

        if (shortDescriptionAr != null) {
            product.setShortDescriptionAr(shortDescriptionAr);
        }

        if (price != null) {
            validatePrice(price);
            product.setPrice(price);
        }
        
        if (compareAtPrice != null) {
            validatePrice(compareAtPrice);
            product.setCompareAtPrice(compareAtPrice);
        }

        if (isTaxable != null) {
            product.setIsTaxable(isTaxable);
        }

        if (isVisible != null) {
            product.setIsVisible(isVisible);
        }

        if (currency != null) {
            validateCurrency(currency);
            product.setCurrency(currency);
        }

        if (status != null) {
            validateStatusTransition(product.getStatus(), status);
            product.setStatus(status);
        }

        if (categoryIds != null) {
            updateProductCategories(product, categoryIds);
        }
        
        product = productRepository.save(product);
        
        log.info("Updated product {} for tenant {}", productId, tenantId);
        return product;
    }

    private void updateProductCategories(ProductEntity product, List<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            // Clear existing associations - let Hibernate handle orphan removal
            product.getProductCategories().clear();
            return;
        }

        // Get current category IDs to avoid unnecessary database operations
        Set<Long> currentCategoryIds = product.getProductCategories().stream()
                .map(pc -> pc.getCategory().getId())
                .collect(Collectors.toSet());

        Set<Long> newCategoryIds = new HashSet<>(categoryIds);

        // Remove categories that are no longer needed
        product.getProductCategories().removeIf(pc -> !newCategoryIds.contains(pc.getCategory().getId()));

        // Add only new categories that don't already exist
        Set<Long> categoriesToAdd = newCategoryIds.stream()
                .filter(id -> !currentCategoryIds.contains(id))
                .collect(Collectors.toSet());

        if (!categoriesToAdd.isEmpty()) {
            List<CategoryEntity> categories = categoriesToAdd.stream()
                    .map(id -> categoryRepository.findByIdAndTenant(id, product.getTenantId())
                            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id)))
                    .collect(Collectors.toList());

            List<ProductCategoryEntity> links = categories.stream()
                    .map(cat -> new ProductCategoryEntity(product, cat, product.getTenant()))
                    .collect(Collectors.toList());

            // Add new associations to the existing collection
            product.getProductCategories().addAll(links);
        }
    }

    /**
     * Update product status with validation.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @param status the new status
     * @return the updated product
     */
    @Transactional
    public ProductEntity updateProductStatus(Long productId, Long tenantId, ProductStatus status) {
        log.debug("Updating product {} status to {} for tenant {}", productId, status, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductEntity product = getProduct(productId, tenantId);
        
        // Validate status transition
        validateStatusTransition(product.getStatus(), status);
        
        product.setStatus(status);
        product = productRepository.save(product);
        
        log.info("Updated product {} status to {} for tenant {}", productId, status, tenantId);
        return product;
    }

    /**
     * Update product SKU with uniqueness validation.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @param sku the new SKU
     * @return the updated product
     */
    @Transactional
    public ProductEntity updateProductSku(Long productId, Long tenantId, String sku) {
        log.debug("Updating product {} SKU to {} for tenant {}", productId, sku, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductEntity product = getProduct(productId, tenantId);
        
        // Validate SKU if provided
        if (StringUtils.hasText(sku)) {
            validateSku(sku);
            
            // Check SKU uniqueness (excluding current product)
            if (productRepository.existsBySkuAndTenantExcludingId(sku, tenantId, productId)) {
                throw new IllegalArgumentException("SKU already exists: " + sku);
            }
        }
        
        product.setSku(sku);
        product = productRepository.save(product);
        
        log.info("Updated product {} SKU for tenant {}", productId, tenantId);
        return product;
    }

    /**
     * Update product visibility.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     * @param visible the visibility flag
     * @return the updated product
     */
    @Transactional
    public ProductEntity updateProductVisibility(Long productId, Long tenantId, boolean visible) {
        log.debug("Updating product {} visibility to {} for tenant {}", productId, visible, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductEntity product = getProduct(productId, tenantId);
        product.setIsVisible(visible);
        product = productRepository.save(product);
        
        log.info("Updated product {} visibility to {} for tenant {}", productId, visible, tenantId);
        return product;
    }

    /**
     * Update product taxable flag.
     */
    @Transactional
    public ProductEntity updateProductTaxable(Long productId, Long tenantId, boolean isTaxable) {
        log.debug("Updating product {} taxable flag to {} for tenant {}", productId, isTaxable, tenantId);

        ecommerceFeatureService.validateEcommerceEnabled(tenantId);

        ProductEntity product = getProduct(productId, tenantId);
        product.setIsTaxable(isTaxable);

        product = productRepository.save(product);
        log.info("Updated product {} taxable flag to {} for tenant {}", productId, isTaxable, tenantId);
        return product;
    }

    /**
     * Update product currency with basic validation.
     */
    @Transactional
    public ProductEntity updateProductCurrency(Long productId, Long tenantId, String currency) {
        log.debug("Updating product {} currency to {} for tenant {}", productId, currency, tenantId);

        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        validateCurrency(currency);

        ProductEntity product = getProduct(productId, tenantId);
        product.setCurrency(currency);

        product = productRepository.save(product);
        log.info("Updated product {} currency to {} for tenant {}", productId, currency, tenantId);
        return product;
    }

    /**
     * Delete a product.
     * 
     * @param productId the product ID
     * @param tenantId the tenant ID
     */
    @Transactional
    public void deleteProduct(Long productId, Long tenantId) {
        log.debug("Deleting product {} for tenant {}", productId, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        ProductEntity product = getProduct(productId, tenantId);
        
        // Check if product has variants
        long variantCount = productVariantRepository.countByTenantAndProduct(tenantId, productId);
        if (variantCount > 0) {
            log.warn("Attempting to delete product {} with {} variants", productId, variantCount);
            throw new IllegalStateException("Cannot delete product with existing variants. Delete variants first.");
        }
        
        productRepository.delete(product);
        
        log.info("Deleted product {} for tenant {}", productId, tenantId);
    }

    /**
     * Get all products for a tenant with pagination.
     * 
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<ProductEntity> getProducts(Long tenantId, Pageable pageable) {
        log.debug("Getting products for tenant {} with pagination", tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productRepository.findAllByTenant(tenantId, pageable);
    }

    /**
     * Get products by status for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param status the product status
     * @param pageable pagination parameters
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<ProductEntity> getProductsByStatus(Long tenantId, ProductStatus status, Pageable pageable) {
        log.debug("Getting products by status {} for tenant {}", status, tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productRepository.findByTenantAndStatus(tenantId, status, pageable);
    }

    /**
     * Search products for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param searchTerm the search term
     * @param pageable pagination parameters
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<ProductEntity> searchProducts(Long tenantId, String searchTerm, Pageable pageable) {
        log.debug("Searching products for tenant {} with term: {}", tenantId, searchTerm);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productRepository.searchByTenant(tenantId, searchTerm, pageable);
    }

    /**
     * Get visible products for public access.
     * 
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of visible products
     */
    @Transactional(readOnly = true)
    public Page<ProductEntity> getVisibleProducts(Long tenantId, Pageable pageable) {
        log.debug("Getting visible products for tenant {}", tenantId);
        
        // Validate e-commerce feature is enabled
        ecommerceFeatureService.validateEcommerceEnabled(tenantId);
        
        return productRepository.findVisibleByTenant(tenantId, pageable);
    }

    // Private helper methods

    private TenantEntity getTenantById(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    private void validateProductName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Product name must not exceed 255 characters");
        }
    }

    private void validateProductSlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Product slug is required");
        }
        if (slug.length() > 255) {
            throw new IllegalArgumentException("Product slug must not exceed 255 characters");
        }
        if (!slug.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException("Product slug must contain only lowercase letters, numbers, and hyphens");
        }
    }

    private void validateSku(String sku) {
        if (sku.length() > 100) {
            throw new IllegalArgumentException("SKU must not exceed 100 characters");
        }
    }

    private void validateCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (currency.length() > 10) {
            throw new IllegalArgumentException("Currency must not exceed 10 characters");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
    }

    private void validateStatusTransition(ProductStatus currentStatus, ProductStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change
        }
        
        // All status transitions are allowed for now
        // Future business rules can be added here
        log.debug("Status transition from {} to {} is valid", currentStatus, newStatus);
    }
}
