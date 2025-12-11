package com.clinic.modules.ecommerce.controller.publicapi;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.dto.PublicProductListResponse;
import com.clinic.modules.ecommerce.dto.PublicProductResponse;
import com.clinic.modules.ecommerce.service.PublicProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Public API controller for product browsing.
 * 
 * Provides customer-facing endpoints for browsing products, searching,
 * filtering, and viewing product details. All operations are tenant-scoped
 * and only return visible, active products.
 * 
 * Base path: /public/products
 */
@RestController
@RequestMapping("/public/products")
public class PublicProductController {

    private static final Logger log = LoggerFactory.getLogger(PublicProductController.class);

    private final PublicProductService publicProductService;

    public PublicProductController(PublicProductService publicProductService) {
        this.publicProductService = publicProductService;
    }

    /**
     * Get all visible products with optional filtering and pagination.
     * 
     * @param slug tenant slug (optional, used for tenant resolution)
     * @param domain tenant domain (optional, used for tenant resolution)
     * @param category category ID filter (optional)
     * @param search search term filter (optional)
     * @param minPrice minimum price filter (optional)
     * @param maxPrice maximum price filter (optional)
     * @param page page number (0-based, default: 0)
     * @param size page size (default: 20, max: 100)
     * @param sort sort criteria (default: createdAt,desc)
     * @return paginated list of products
     */
    @GetMapping
    public ResponseEntity<PublicProductListResponse> getProducts(
            @RequestParam(name = "slug", required = false) String slug,
            @RequestParam(name = "domain", required = false) String domain,
            @RequestHeader(name = "X-Tenant-Slug", required = false) String headerSlug,
            @RequestHeader(name = "X-Tenant-Domain", required = false) String headerDomain,
            @RequestParam(name = "category", required = false) Long category,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {
        
        String effectiveSlug = StringUtils.hasText(slug) ? slug : headerSlug;
        String effectiveDomain = StringUtils.hasText(domain) ? domain : headerDomain;

        log.debug("Getting products - slug: {}, domain: {}, category: {}, search: {}, priceRange: [{}, {}], page: {}, size: {}",
                effectiveSlug, effectiveDomain, category, search, minPrice, maxPrice, page, size);
        
        try {
            // Resolve tenant from slug or domain
            TenantEntity tenant = publicProductService.resolveTenant(effectiveSlug, effectiveDomain);
            
            // Validate and create pagination
            Pageable pageable = createPageable(page, size, sort);
            
            // Get products with filters
            Page<PublicProductResponse> products;
            
            if (hasFilters(category, search, minPrice, maxPrice)) {
                products = publicProductService.getVisibleProductsWithFilters(
                        tenant.getId(), category, search, minPrice, maxPrice, pageable);
            } else {
                products = publicProductService.getVisibleProducts(tenant.getId(), pageable);
            }
            
            PublicProductListResponse response = PublicProductListResponse.fromProductPage(products);
            
            log.info("Retrieved {} products for tenant {} (page {}/{})", 
                    products.getNumberOfElements(), tenant.getSlug(), page + 1, products.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving products", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get a specific product by ID.
     * 
     * @param productId the product ID
     * @param slug tenant slug (optional, used for tenant resolution)
     * @param domain tenant domain (optional, used for tenant resolution)
     * @return the product details
     */
    @GetMapping("/{productId}")
    public ResponseEntity<PublicProductResponse> getProduct(
            @PathVariable Long productId,
            @RequestParam(name = "slug", required = false) String slug,
            @RequestParam(name = "domain", required = false) String domain,
            @RequestHeader(name = "X-Tenant-Slug", required = false) String headerSlug,
            @RequestHeader(name = "X-Tenant-Domain", required = false) String headerDomain) {
        
        String effectiveSlug = StringUtils.hasText(slug) ? slug : headerSlug;
        String effectiveDomain = StringUtils.hasText(domain) ? domain : headerDomain;

        log.debug("Getting product {} - slug: {}, domain: {}", productId, effectiveSlug, effectiveDomain);
        
        try {
            // Resolve tenant from slug or domain
            TenantEntity tenant = publicProductService.resolveTenant(effectiveSlug, effectiveDomain);
            
            // Get product
            PublicProductResponse product = publicProductService.getVisibleProduct(productId, tenant.getId());
            
            log.info("Retrieved product {} for tenant {}", productId, tenant.getSlug());
            
            return ResponseEntity.ok(product);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving product {}", productId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get a specific product by slug.
     * 
     * @param productSlug the product slug
     * @param slug tenant slug (optional, used for tenant resolution)
     * @param domain tenant domain (optional, used for tenant resolution)
     * @return the product details
     */
    @GetMapping("/by-slug/{productSlug}")
    public ResponseEntity<PublicProductResponse> getProductBySlug(
            @PathVariable String productSlug,
            @RequestParam(name = "slug", required = false) String slug,
            @RequestParam(name = "domain", required = false) String domain,
            @RequestHeader(name = "X-Tenant-Slug", required = false) String headerSlug,
            @RequestHeader(name = "X-Tenant-Domain", required = false) String headerDomain) {
        
        String effectiveSlug = StringUtils.hasText(slug) ? slug : headerSlug;
        String effectiveDomain = StringUtils.hasText(domain) ? domain : headerDomain;

        log.debug("Getting product by slug {} - tenant slug: {}, domain: {}", productSlug, effectiveSlug, effectiveDomain);
        
        try {
            // Resolve tenant from slug or domain
            TenantEntity tenant = publicProductService.resolveTenant(effectiveSlug, effectiveDomain);
            
            // Get product by slug
            PublicProductResponse product = publicProductService.getVisibleProductBySlug(productSlug, tenant.getId());
            
            log.info("Retrieved product by slug {} for tenant {}", productSlug, tenant.getSlug());
            
            return ResponseEntity.ok(product);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving product by slug {}", productSlug, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search products by term.
     * 
     * @param searchTerm the search term
     * @param slug tenant slug (optional, used for tenant resolution)
     * @param domain tenant domain (optional, used for tenant resolution)
     * @param page page number (0-based, default: 0)
     * @param size page size (default: 20, max: 100)
     * @param sort sort criteria (default: createdAt,desc)
     * @return paginated list of matching products
     */
    @GetMapping("/search")
    public ResponseEntity<PublicProductListResponse> searchProducts(
            @RequestParam(name = "q") String searchTerm,
            @RequestParam(name = "slug", required = false) String slug,
            @RequestParam(name = "domain", required = false) String domain,
            @RequestHeader(name = "X-Tenant-Slug", required = false) String headerSlug,
            @RequestHeader(name = "X-Tenant-Domain", required = false) String headerDomain,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {
        
        String effectiveSlug = StringUtils.hasText(slug) ? slug : headerSlug;
        String effectiveDomain = StringUtils.hasText(domain) ? domain : headerDomain;

        log.debug("Searching products with term '{}' - slug: {}, domain: {}, page: {}, size: {}", 
                searchTerm, effectiveSlug, effectiveDomain, page, size);
        
        try {
            // Resolve tenant from slug or domain
            TenantEntity tenant = publicProductService.resolveTenant(effectiveSlug, effectiveDomain);
            
            // Validate and create pagination
            Pageable pageable = createPageable(page, size, sort);
            
            // Search products
            Page<PublicProductResponse> products = publicProductService.searchVisibleProducts(
                    tenant.getId(), searchTerm, pageable);
            
            PublicProductListResponse response = PublicProductListResponse.fromProductPage(products);
            
            log.info("Found {} products for search term '{}' for tenant {} (page {}/{})", 
                    products.getNumberOfElements(), searchTerm, tenant.getSlug(), page + 1, products.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error searching products with term '{}'", searchTerm, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get products by category.
     * 
     * @param categoryId the category ID
     * @param slug tenant slug (optional, used for tenant resolution)
     * @param domain tenant domain (optional, used for tenant resolution)
     * @param page page number (0-based, default: 0)
     * @param size page size (default: 20, max: 100)
     * @param sort sort criteria (default: createdAt,desc)
     * @return paginated list of products in the category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PublicProductListResponse> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name = "slug", required = false) String slug,
            @RequestParam(name = "domain", required = false) String domain,
            @RequestHeader(name = "X-Tenant-Slug", required = false) String headerSlug,
            @RequestHeader(name = "X-Tenant-Domain", required = false) String headerDomain,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {
        
        String effectiveSlug = StringUtils.hasText(slug) ? slug : headerSlug;
        String effectiveDomain = StringUtils.hasText(domain) ? domain : headerDomain;

        log.debug("Getting products by category {} - slug: {}, domain: {}, page: {}, size: {}", 
                categoryId, effectiveSlug, effectiveDomain, page, size);
        
        try {
            // Resolve tenant from slug or domain
            TenantEntity tenant = publicProductService.resolveTenant(effectiveSlug, effectiveDomain);
            
            // Validate and create pagination
            Pageable pageable = createPageable(page, size, sort);
            
            // Get products by category
            Page<PublicProductResponse> products = publicProductService.getVisibleProductsByCategory(
                    tenant.getId(), categoryId, pageable);
            
            PublicProductListResponse response = PublicProductListResponse.fromProductPage(products);
            
            log.info("Retrieved {} products for category {} for tenant {} (page {}/{})", 
                    products.getNumberOfElements(), categoryId, tenant.getSlug(), page + 1, products.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving products for category {}", categoryId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get recently added products.
     * 
     * @param slug tenant slug (optional, used for tenant resolution)
     * @param domain tenant domain (optional, used for tenant resolution)
     * @param page page number (0-based, default: 0)
     * @param size page size (default: 20, max: 100)
     * @return paginated list of recent products
     */
    @GetMapping("/recent")
    public ResponseEntity<PublicProductListResponse> getRecentProducts(
            @RequestParam(name = "slug", required = false) String slug,
            @RequestParam(name = "domain", required = false) String domain,
            @RequestHeader(name = "X-Tenant-Slug", required = false) String headerSlug,
            @RequestHeader(name = "X-Tenant-Domain", required = false) String headerDomain,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        String effectiveSlug = StringUtils.hasText(slug) ? slug : headerSlug;
        String effectiveDomain = StringUtils.hasText(domain) ? domain : headerDomain;

        log.debug("Getting recent products - slug: {}, domain: {}, page: {}, size: {}", 
                effectiveSlug, effectiveDomain, page, size);
        
        try {
            // Resolve tenant from slug or domain
            TenantEntity tenant = publicProductService.resolveTenant(effectiveSlug, effectiveDomain);
            
            // Create pagination (always sort by creation date descending for recent products)
            Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // Get recent products
            Page<PublicProductResponse> products = publicProductService.getRecentVisibleProducts(
                    tenant.getId(), pageable);
            
            PublicProductListResponse response = PublicProductListResponse.fromProductPage(products);
            
            log.info("Retrieved {} recent products for tenant {} (page {}/{})", 
                    products.getNumberOfElements(), tenant.getSlug(), page + 1, products.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving recent products", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Private helper methods

    private Pageable createPageable(int page, int size, String sort) {
        // Validate page parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        
        // Limit page size
        size = Math.min(Math.max(size, 1), 100);
        
        // Parse sort parameter
        Sort sortObj = parseSort(sort);
        
        return PageRequest.of(page, size, sortObj);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        try {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()) 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC;
            
            // Validate allowed sort properties
            if (!isValidSortProperty(property)) {
                log.warn("Invalid sort property: {}, using default", property);
                return Sort.by(Sort.Direction.DESC, "createdAt");
            }
            
            return Sort.by(direction, property);
            
        } catch (Exception e) {
            log.warn("Invalid sort parameter: {}, using default", sort);
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    private boolean isValidSortProperty(String property) {
        return switch (property) {
            case "name", "price", "createdAt", "updatedAt" -> true;
            default -> false;
        };
    }

    private boolean hasFilters(Long category, String search, BigDecimal minPrice, BigDecimal maxPrice) {
        return category != null || 
               (search != null && !search.trim().isEmpty()) || 
               minPrice != null || 
               maxPrice != null;
    }
}
