package com.clinic.modules.ecommerce.controller.admin;

import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.ecommerce.config.RequiresEcommerceFeature;
import com.clinic.modules.ecommerce.dto.*;
import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.model.ProductStatus;
import com.clinic.modules.ecommerce.service.ProductService;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin controller for product management.
 * Provides endpoints for creating, listing, updating, and deleting products.
 * All operations enforce tenant isolation and e-commerce feature validation.
 */
@RestController
@RequestMapping("/admin/tenants/{tenantId}/products")
@Tag(name = "Admin Products", description = "Admin endpoints for managing products")
@SecurityRequirement(name = "bearerAuth")
@RequiresEcommerceFeature
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final TenantContextHolder tenantContextHolder;

    public ProductController(
            ProductService productService,
            TenantContextHolder tenantContextHolder) {
        this.productService = productService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get all products for the tenant with pagination.
     *
     * @param tenantId the tenant ID from path
     * @param status optional status filter
     * @param search optional search term
     * @param pageable pagination parameters
     * @return page of products
     */
    @GetMapping
    @Operation(
            summary = "List products",
            description = "Retrieve all products for the tenant with optional filtering by status and search term. Results are paginated."
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Filter by product status")
            @RequestParam(required = false) ProductStatus status,
            
            @Parameter(description = "Search term for product name or description")
            @RequestParam(required = false) String search,
            
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        
        validateTenantAccess(tenantId);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                parseSort(sort)
        );

        logger.info("Fetching products - tenantId: {}, status: {}, search: {}, page: {}", 
                   tenantId, status, search, pageable.getPageNumber());

        try {
            Page<ProductEntity> products;

            if (search != null && !search.trim().isEmpty()) {
                products = productService.searchProducts(tenantId, search.trim(), pageable);
            } else if (status != null) {
                products = productService.getProductsByStatus(tenantId, status, pageable);
            } else {
                products = productService.getProducts(tenantId, pageable);
            }

            Page<ProductResponse> response = products.map(this::toResponse);

            logger.info("Successfully fetched products - tenantId: {}, count: {}", tenantId, response.getNumberOfElements());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to fetch products - tenantId: {}, error: {}", tenantId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching products for tenant {}: {}", tenantId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch products", e);
        }
    }

    /**
     * Get a specific product by ID.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID
     * @return the product details
     */
    @GetMapping("/{productId}")
    @Operation(
            summary = "Get product",
            description = "Retrieve a specific product by ID with all its details including variants, images, and categories."
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching product - tenantId: {}, productId: {}", tenantId, productId);

        try {
            ProductEntity product = productService.getProduct(productId, tenantId);
            ProductResponse response = toResponse(product);

            logger.info("Successfully fetched product - tenantId: {}, productId: {}", tenantId, productId);
            
            return ResponseEntity.ok(response);
        } catch (ProductNotFoundException e) {
            logger.warn("Product not found - tenantId: {}, productId: {}", tenantId, productId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request while fetching product - tenantId: {}, productId: {}, error: {}", tenantId, productId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching product - tenantId: {}, productId: {}", tenantId, productId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch product", e);
        }
    }

    /**
     * Create a new product.
     *
     * @param tenantId the tenant ID from path
     * @param request the product creation request
     * @return the created product
     */
    @PostMapping
    @Operation(
            summary = "Create product",
            description = "Create a new product for the tenant. The product will be created in DRAFT status by default."
    )
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Valid @RequestBody ProductCreateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Creating product - tenantId: {}, name: {}, slug: {}", 
                   tenantId, request.name(), request.slug());

        try {
            ProductEntity product = productService.createProduct(
                    tenantId,
                    request.name(),
                    request.slug(),
                    request.sku(),
                    request.description(),
                    request.shortDescription(),
                    request.price(),
                    request.compareAtPrice(),
                    request.currency(),
                    request.isTaxable(),
                    request.isVisible(),
                    request.images()
            );
            
            ProductResponse response = toResponse(product);

            logger.info("Successfully created product - tenantId: {}, productId: {}", tenantId, product.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create product - tenantId: {}, error: {}", tenantId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Update an existing product.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID
     * @param request the product update request
     * @return the updated product
     */
    @PutMapping("/{productId}")
    @Operation(
            summary = "Update product",
            description = "Update an existing product. Only provided fields will be updated."
    )
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Valid @RequestBody ProductUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating product - tenantId: {}, productId: {}", tenantId, productId);

        try {
            ProductEntity product = productService.updateProduct(
                productId, tenantId, request.name(), request.description(), 
                request.shortDescription(), request.price(), request.compareAtPrice());
            
            if (request.isTaxable() != null) {
                product = productService.updateProductTaxable(productId, tenantId, request.isTaxable());
            }
            
            if (request.isVisible() != null) {
                product = productService.updateProductVisibility(productId, tenantId, request.isVisible());
            }
            
            ProductResponse response = toResponse(product);

            logger.info("Successfully updated product - tenantId: {}, productId: {}", tenantId, productId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update product - tenantId: {}, productId: {}, error: {}", 
                        tenantId, productId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Update product status.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID
     * @param request the status update request
     * @return the updated product
     */
    @PutMapping("/{productId}/status")
    @Operation(
            summary = "Update product status",
            description = "Update the status of a product (DRAFT, ACTIVE, ARCHIVED)."
    )
    public ResponseEntity<ProductResponse> updateProductStatus(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Valid @RequestBody ProductStatusUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating product status - tenantId: {}, productId: {}, status: {}", 
                   tenantId, productId, request.status());

        try {
            ProductEntity product = productService.updateProductStatus(productId, tenantId, request.status());
            ProductResponse response = toResponse(product);

            logger.info("Successfully updated product status - tenantId: {}, productId: {}, status: {}", 
                       tenantId, productId, request.status());

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update product status - tenantId: {}, productId: {}, error: {}", 
                        tenantId, productId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Update product SKU.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID
     * @param request the SKU update request
     * @return the updated product
     */
    @PutMapping("/{productId}/sku")
    @Operation(
            summary = "Update product SKU",
            description = "Update the SKU of a product. SKU must be unique within the tenant."
    )
    public ResponseEntity<ProductResponse> updateProductSku(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Valid @RequestBody ProductSkuUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating product SKU - tenantId: {}, productId: {}, sku: {}", 
                   tenantId, productId, request.sku());

        try {
            ProductEntity product = productService.updateProductSku(productId, tenantId, request.sku());
            ProductResponse response = toResponse(product);

            logger.info("Successfully updated product SKU - tenantId: {}, productId: {}", tenantId, productId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update product SKU - tenantId: {}, productId: {}, error: {}", 
                        tenantId, productId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Delete a product.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID
     * @return no content response
     */
    @DeleteMapping("/{productId}")
    @Operation(
            summary = "Delete product",
            description = "Delete a product. The product must not have any variants."
    )
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Deleting product - tenantId: {}, productId: {}", tenantId, productId);

        try {
            productService.deleteProduct(productId, tenantId);

            logger.info("Successfully deleted product - tenantId: {}, productId: {}", tenantId, productId);

            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Failed to delete product - tenantId: {}, productId: {}, error: {}", 
                        tenantId, productId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
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
    private ProductResponse toResponse(ProductEntity entity) {
        return new ProductResponse(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getSku(),
                entity.getDescription(),
                entity.getShortDescription(),
                entity.getStatus(),
                entity.getProductType(),
                entity.getPrice(),
                entity.getCompareAtPrice(),
                entity.getCurrency(),
                entity.getHasVariants(),
                entity.getIsTaxable(),
                entity.getIsVisible(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVariants().stream()
                        .map(this::toVariantResponse)
                        .collect(Collectors.toList()),
                entity.getImages().stream()
                        .map(this::toImageResponse)
                        .collect(Collectors.toList()),
                entity.getProductCategories().stream()
                        .map(pc -> toCategoryResponse(pc.getCategory()))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Convert variant entity to response DTO.
     */
    private ProductVariantResponse toVariantResponse(com.clinic.modules.ecommerce.model.ProductVariantEntity entity) {
        return new ProductVariantResponse(
                entity.getId(),
                entity.getProduct().getId(),
                entity.getSku(),
                entity.getName(),
                entity.getPrice(),
                entity.getCompareAtPrice(),
                entity.getCurrency(),
                entity.getStockQuantity(),
                entity.getIsInStock(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Convert image entity to response DTO.
     */
    private ProductImageResponse toImageResponse(com.clinic.modules.ecommerce.model.ProductImageEntity entity) {
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

    /**
     * Convert category entity to response DTO.
     */
    private CategoryResponse toCategoryResponse(com.clinic.modules.ecommerce.model.CategoryEntity entity) {
        return new CategoryResponse(
                entity.getId(),
                entity.getParent() != null ? entity.getParent().getId() : null,
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getSortOrder(),
                entity.getIsActive(),
                entity.getFullPath(),
                entity.getDepth(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                List.of() // Don't include children in product response to avoid deep nesting
        );
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        try {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            // Allow only known properties to avoid runtime errors
            return switch (property) {
                case "name", "price", "createdAt", "updatedAt", "status" -> Sort.by(direction, property);
                default -> Sort.by(Sort.Direction.DESC, "createdAt");
            };
        } catch (Exception e) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}
