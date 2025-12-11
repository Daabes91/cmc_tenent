package com.clinic.modules.ecommerce.controller.admin;

import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.ecommerce.config.RequiresEcommerceFeature;
import com.clinic.modules.ecommerce.dto.*;
import com.clinic.modules.ecommerce.model.ProductVariantEntity;
import com.clinic.modules.ecommerce.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin controller for product variant management.
 * Provides endpoints for creating, listing, updating, and deleting product variants.
 * All operations enforce tenant isolation and e-commerce feature validation.
 */
@RestController
@RequestMapping("/admin/tenants/{tenantId}/products/{productId}/variants")
@Tag(name = "Admin Product Variants", description = "Admin endpoints for managing product variants")
@SecurityRequirement(name = "bearerAuth")
@RequiresEcommerceFeature
public class ProductVariantController {

    private static final Logger logger = LoggerFactory.getLogger(ProductVariantController.class);

    private final ProductVariantService productVariantService;
    private final TenantContextHolder tenantContextHolder;

    public ProductVariantController(
            ProductVariantService productVariantService,
            TenantContextHolder tenantContextHolder) {
        this.productVariantService = productVariantService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get all variants for a product.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @return list of variants
     */
    @GetMapping
    @Operation(
            summary = "List product variants",
            description = "Retrieve all variants for a specific product."
    )
    public ResponseEntity<List<ProductVariantResponse>> getVariants(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching variants - tenantId: {}, productId: {}", tenantId, productId);

        List<ProductVariantEntity> variants = productVariantService.getVariantsByProduct(productId, tenantId);
        List<ProductVariantResponse> response = variants.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        logger.info("Successfully fetched variants - tenantId: {}, productId: {}, count: {}", 
                   tenantId, productId, response.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific variant by ID.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param variantId the variant ID
     * @return the variant details
     */
    @GetMapping("/{variantId}")
    @Operation(
            summary = "Get product variant",
            description = "Retrieve a specific product variant by ID."
    )
    public ResponseEntity<ProductVariantResponse> getVariant(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Variant ID")
            @PathVariable Long variantId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching variant - tenantId: {}, productId: {}, variantId: {}", 
                   tenantId, productId, variantId);

        ProductVariantEntity variant = productVariantService.getVariant(variantId, tenantId);
        
        // Validate that variant belongs to the specified product
        if (!variant.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Variant does not belong to the specified product");
        }
        
        ProductVariantResponse response = toResponse(variant);

        logger.info("Successfully fetched variant - tenantId: {}, productId: {}, variantId: {}", 
                   tenantId, productId, variantId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new variant for a product.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param request the variant creation request
     * @return the created variant
     */
    @PostMapping
    @Operation(
            summary = "Create product variant",
            description = "Create a new variant for a product. SKU must be unique within the tenant."
    )
    public ResponseEntity<ProductVariantResponse> createVariant(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Valid @RequestBody ProductVariantCreateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Creating variant - tenantId: {}, productId: {}, sku: {}, name: {}", 
                   tenantId, productId, request.sku(), request.name());

        try {
            ProductVariantEntity variant = productVariantService.createVariant(
                productId, tenantId, request.sku(), request.name(), request.price());
            
            // Update additional fields if provided
            if (request.compareAtPrice() != null || request.stockQuantity() != null) {
                variant = productVariantService.updateVariant(
                    variant.getId(), tenantId, null, null, request.compareAtPrice());
                
                if (request.stockQuantity() != null) {
                    variant = productVariantService.updateVariantStock(
                        variant.getId(), tenantId, request.stockQuantity());
                }
            }
            
            if (request.currency() != null) {
                variant.setCurrency(request.currency());
            }
            
            ProductVariantResponse response = toResponse(variant);

            logger.info("Successfully created variant - tenantId: {}, productId: {}, variantId: {}", 
                       tenantId, productId, variant.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create variant - tenantId: {}, productId: {}, error: {}", 
                        tenantId, productId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing variant.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param variantId the variant ID
     * @param request the variant update request
     * @return the updated variant
     */
    @PutMapping("/{variantId}")
    @Operation(
            summary = "Update product variant",
            description = "Update an existing product variant. Only provided fields will be updated."
    )
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Variant ID")
            @PathVariable Long variantId,
            
            @Valid @RequestBody ProductVariantUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating variant - tenantId: {}, productId: {}, variantId: {}", 
                   tenantId, productId, variantId);

        try {
            // Validate that variant belongs to the specified product
            ProductVariantEntity existingVariant = productVariantService.getVariant(variantId, tenantId);
            if (!existingVariant.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("Variant does not belong to the specified product");
            }
            
            ProductVariantEntity variant = productVariantService.updateVariant(
                variantId, tenantId, request.name(), request.price(), request.compareAtPrice());
            
            if (request.stockQuantity() != null) {
                variant = productVariantService.updateVariantStock(variantId, tenantId, request.stockQuantity());
            }
            
            ProductVariantResponse response = toResponse(variant);

            logger.info("Successfully updated variant - tenantId: {}, productId: {}, variantId: {}", 
                       tenantId, productId, variantId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update variant - tenantId: {}, productId: {}, variantId: {}, error: {}", 
                        tenantId, productId, variantId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update variant SKU.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param variantId the variant ID
     * @param request the SKU update request
     * @return the updated variant
     */
    @PutMapping("/{variantId}/sku")
    @Operation(
            summary = "Update variant SKU",
            description = "Update the SKU of a variant. SKU must be unique within the tenant."
    )
    public ResponseEntity<ProductVariantResponse> updateVariantSku(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Variant ID")
            @PathVariable Long variantId,
            
            @Valid @RequestBody ProductVariantSkuUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating variant SKU - tenantId: {}, productId: {}, variantId: {}, sku: {}", 
                   tenantId, productId, variantId, request.sku());

        try {
            // Validate that variant belongs to the specified product
            ProductVariantEntity existingVariant = productVariantService.getVariant(variantId, tenantId);
            if (!existingVariant.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("Variant does not belong to the specified product");
            }
            
            ProductVariantEntity variant = productVariantService.updateVariantSku(variantId, tenantId, request.sku());
            ProductVariantResponse response = toResponse(variant);

            logger.info("Successfully updated variant SKU - tenantId: {}, productId: {}, variantId: {}", 
                       tenantId, productId, variantId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update variant SKU - tenantId: {}, productId: {}, variantId: {}, error: {}", 
                        tenantId, productId, variantId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update variant stock.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param variantId the variant ID
     * @param request the stock update request
     * @return the updated variant
     */
    @PutMapping("/{variantId}/stock")
    @Operation(
            summary = "Update variant stock",
            description = "Update the stock quantity of a variant."
    )
    public ResponseEntity<ProductVariantResponse> updateVariantStock(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Variant ID")
            @PathVariable Long variantId,
            
            @Valid @RequestBody ProductVariantStockUpdateRequest request) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Updating variant stock - tenantId: {}, productId: {}, variantId: {}, stock: {}", 
                   tenantId, productId, variantId, request.stockQuantity());

        try {
            // Validate that variant belongs to the specified product
            ProductVariantEntity existingVariant = productVariantService.getVariant(variantId, tenantId);
            if (!existingVariant.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("Variant does not belong to the specified product");
            }
            
            ProductVariantEntity variant = productVariantService.updateVariantStock(
                variantId, tenantId, request.stockQuantity());
            ProductVariantResponse response = toResponse(variant);

            logger.info("Successfully updated variant stock - tenantId: {}, productId: {}, variantId: {}", 
                       tenantId, productId, variantId);

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update variant stock - tenantId: {}, productId: {}, variantId: {}, error: {}", 
                        tenantId, productId, variantId, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a variant.
     *
     * @param tenantId the tenant ID from path
     * @param productId the product ID from path
     * @param variantId the variant ID
     * @return no content response
     */
    @DeleteMapping("/{variantId}")
    @Operation(
            summary = "Delete product variant",
            description = "Delete a product variant."
    )
    public ResponseEntity<Void> deleteVariant(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Product ID")
            @PathVariable Long productId,
            
            @Parameter(description = "Variant ID")
            @PathVariable Long variantId) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Deleting variant - tenantId: {}, productId: {}, variantId: {}", 
                   tenantId, productId, variantId);

        try {
            // Validate that variant belongs to the specified product
            ProductVariantEntity existingVariant = productVariantService.getVariant(variantId, tenantId);
            if (!existingVariant.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("Variant does not belong to the specified product");
            }
            
            productVariantService.deleteVariant(variantId, tenantId);

            logger.info("Successfully deleted variant - tenantId: {}, productId: {}, variantId: {}", 
                       tenantId, productId, variantId);

            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete variant - tenantId: {}, productId: {}, variantId: {}, error: {}", 
                        tenantId, productId, variantId, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all variants for a tenant with pagination.
     *
     * @param tenantId the tenant ID from path
     * @param search optional search term
     * @param pageable pagination parameters
     * @return page of variants
     */
    @GetMapping("/all")
    @Operation(
            summary = "List all variants for tenant",
            description = "Retrieve all variants for the tenant with optional search. Results are paginated."
    )
    public ResponseEntity<Page<ProductVariantResponse>> getAllVariants(
            @Parameter(description = "Tenant ID")
            @PathVariable Long tenantId,
            
            @Parameter(description = "Search term for variant name or SKU")
            @RequestParam(required = false) String search,
            
            @PageableDefault(size = 20) Pageable pageable) {
        
        validateTenantAccess(tenantId);
        
        logger.info("Fetching all variants - tenantId: {}, search: {}, page: {}", 
                   tenantId, search, pageable.getPageNumber());

        Page<ProductVariantEntity> variants;
        
        if (search != null && !search.trim().isEmpty()) {
            variants = productVariantService.searchVariants(tenantId, search.trim(), pageable);
        } else {
            variants = productVariantService.getVariants(tenantId, pageable);
        }
        
        Page<ProductVariantResponse> response = variants.map(this::toResponse);

        logger.info("Successfully fetched all variants - tenantId: {}, count: {}", 
                   tenantId, response.getNumberOfElements());
        
        return ResponseEntity.ok(response);
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
    private ProductVariantResponse toResponse(ProductVariantEntity entity) {
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
}