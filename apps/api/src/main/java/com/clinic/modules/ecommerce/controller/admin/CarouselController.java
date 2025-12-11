package com.clinic.modules.ecommerce.controller.admin;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.service.CarouselService;
import com.clinic.modules.ecommerce.repository.CarouselItemRepository;
import com.clinic.modules.ecommerce.dto.*;
import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin controller for carousel management.
 * 
 * Provides CRUD operations for carousels and carousel items with proper
 * tenant isolation and validation.
 */
@RestController
@RequestMapping("/admin/tenants/{tenantId}/carousels")
@Tag(name = "Admin Carousel Management", description = "Admin APIs for managing carousels")
public class CarouselController {

    private final CarouselService carouselService;
    private final TenantService tenantService;
    private final CarouselItemRepository carouselItemRepository;

    @Autowired
    public CarouselController(CarouselService carouselService, TenantService tenantService, CarouselItemRepository carouselItemRepository) {
        this.carouselService = carouselService;
        this.tenantService = tenantService;
        this.carouselItemRepository = carouselItemRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new carousel")
    public ResponseEntity<ApiResponse<CarouselResponse>> createCarousel(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Valid @RequestBody CreateCarouselRequest request) {
        
        TenantEntity tenant = tenantService.requireTenant(tenantId);
        
        CarouselEntity carousel = carouselService.createCarousel(
                tenant,
                request.getName(),
                request.getSlug(),
                request.getType(),
                request.getPlacement(),
                request.getPlatform()
        );
        
        CarouselResponse response = mapToCarouselResponse(carousel);
        return ResponseEntity.ok(ApiResponseFactory.success(response));
    }

    @PutMapping("/{carouselId}")
    @Operation(summary = "Update an existing carousel")
    public ResponseEntity<ApiResponse<CarouselResponse>> updateCarousel(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Carousel ID") @PathVariable Long carouselId,
            @Valid @RequestBody UpdateCarouselRequest request) {
        
        CarouselEntity carousel = carouselService.updateCarousel(
                tenantId,
                carouselId,
                request.getName(),
                request.getSlug(),
                request.getType(),
                request.getPlacement(),
                request.getPlatform(),
                request.getIsActive(),
                request.getMaxItems()
        );
        
        CarouselResponse response = mapToCarouselResponse(carousel);
        return ResponseEntity.ok(ApiResponseFactory.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all carousels for a tenant")
    public ResponseEntity<ApiResponse<Page<CarouselResponse>>> getCarousels(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CarouselEntity> carousels = carouselService.getCarouselsByTenant(tenantId, pageable);
        Page<CarouselResponse> response = carousels.map(this::mapToCarouselResponse);
        
        return ResponseEntity.ok(ApiResponseFactory.success(response));
    }

    @GetMapping("/{carouselId}")
    @Operation(summary = "Get a specific carousel")
    public ResponseEntity<ApiResponse<CarouselResponse>> getCarousel(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Carousel ID") @PathVariable Long carouselId) {
        
        CarouselEntity carousel = carouselService.getCarouselByTenantAndId(tenantId, carouselId);
        CarouselResponse response = mapToCarouselResponse(carousel);
        
        return ResponseEntity.ok(ApiResponseFactory.success(response));
    }

    @GetMapping("/by-placement/{placement}")
    @Operation(summary = "Get carousels by placement")
    public ResponseEntity<ApiResponse<List<CarouselResponse>>> getCarouselsByPlacement(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Placement") @PathVariable String placement) {
        
        List<CarouselEntity> carousels = carouselService.getCarouselsByPlacement(tenantId, placement);
        List<CarouselResponse> response = carousels.stream()
                .map(this::mapToCarouselResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponseFactory.success(response));
    }

    @DeleteMapping("/{carouselId}")
    @Operation(summary = "Delete a carousel")
    public ResponseEntity<ApiResponse<Void>> deleteCarousel(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Carousel ID") @PathVariable Long carouselId) {
        
        carouselService.deleteCarousel(tenantId, carouselId);
        return ResponseEntity.ok(ApiResponseFactory.success(null));
    }

    // Carousel Item endpoints

    @PostMapping("/{carouselId}/items")
    @Operation(summary = "Add an item to a carousel")
    public ResponseEntity<ApiResponse<CarouselItemResponse>> addCarouselItem(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Carousel ID") @PathVariable Long carouselId,
            @Valid @RequestBody CreateCarouselItemRequest request) {
        
        CarouselItemEntity item = carouselService.addCarouselItem(
                tenantId,
                carouselId,
                request.getContentType(),
                request.getTitle(),
                request.getSubtitle(),
                request.getImageUrl(),
                request.getLinkUrl(),
                request.getCtaType(),
                request.getCtaText(),
                request.getProductId(),
                request.getCategoryId()
        );
        
        CarouselItemResponse response = mapToCarouselItemResponse(item);
        return ResponseEntity.ok(ApiResponseFactory.success(response));
    }

    @PutMapping("/{carouselId}/items/{itemId}")
    @Operation(summary = "Update a carousel item")
    public ResponseEntity<ApiResponse<CarouselItemResponse>> updateCarouselItem(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Carousel ID") @PathVariable Long carouselId,
            @Parameter(description = "Item ID") @PathVariable Long itemId,
            @Valid @RequestBody UpdateCarouselItemRequest request) {
        
        CarouselItemEntity item = carouselService.updateCarouselItem(
                tenantId,
                carouselId,
                itemId,
                request.getTitle(),
                request.getSubtitle(),
                request.getImageUrl(),
                request.getLinkUrl(),
                request.getCtaType(),
                request.getCtaText(),
                request.getIsActive(),
                request.getSortOrder()
        );
        
        CarouselItemResponse response = mapToCarouselItemResponse(item);
        return ResponseEntity.ok(ApiResponseFactory.success(response));
    }

    @GetMapping("/{carouselId}/items")
    @Operation(summary = "Get all items for a carousel")
    public ResponseEntity<ApiResponse<List<CarouselItemResponse>>> getCarouselItems(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Carousel ID") @PathVariable Long carouselId) {
        
        List<CarouselItemEntity> items = carouselService.getCarouselItems(tenantId, carouselId);
        List<CarouselItemResponse> response = items.stream()
                .map(this::mapToCarouselItemResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponseFactory.success(response));
    }

    @PutMapping("/{carouselId}/items/reorder")
    @Operation(summary = "Reorder carousel items")
    public ResponseEntity<ApiResponse<Void>> reorderCarouselItems(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Carousel ID") @PathVariable Long carouselId,
            @Valid @RequestBody ReorderCarouselItemsRequest request) {
        
        carouselService.reorderCarouselItems(tenantId, carouselId, request.getItemIds());
        return ResponseEntity.ok(ApiResponseFactory.success(null));
    }

    @DeleteMapping("/{carouselId}/items/{itemId}")
    @Operation(summary = "Delete a carousel item")
    public ResponseEntity<ApiResponse<Void>> deleteCarouselItem(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Carousel ID") @PathVariable Long carouselId,
            @Parameter(description = "Item ID") @PathVariable Long itemId) {
        
        carouselService.deleteCarouselItem(tenantId, carouselId, itemId);
        return ResponseEntity.ok(ApiResponseFactory.success(null));
    }

    // Mapping methods

    private CarouselResponse mapToCarouselResponse(CarouselEntity carousel) {
        CarouselResponse response = new CarouselResponse();
        response.setId(carousel.getId());
        response.setName(carousel.getName());
        response.setSlug(carousel.getSlug());
        response.setType(carousel.getType());
        response.setPlacement(carousel.getPlacement());
        response.setPlatform(carousel.getPlatform());
        response.setIsActive(carousel.getIsActive());
        response.setMaxItems(carousel.getMaxItems());
        Long tenantId = carousel.getTenant() != null ? carousel.getTenant().getId() : null;
        long itemCount = 0;
        if (tenantId != null && carousel.getId() != null) {
            itemCount = carouselItemRepository.countActiveByCarouselIdAndTenantId(carousel.getId(), tenantId);
        }
        response.setItemCount((int) itemCount);
        response.setCreatedAt(carousel.getCreatedAt());
        response.setUpdatedAt(carousel.getUpdatedAt());
        return response;
    }

    private CarouselItemResponse mapToCarouselItemResponse(CarouselItemEntity item) {
        CarouselItemResponse response = new CarouselItemResponse();
        response.setId(item.getId());
        response.setContentType(item.getContentType());
        response.setTitle(item.getTitle());
        response.setSubtitle(item.getSubtitle());
        response.setImageUrl(item.getImageUrl());
        response.setLinkUrl(item.getLinkUrl());
        response.setCtaType(item.getCtaType());
        response.setCtaText(item.getCtaText());
        response.setSortOrder(item.getSortOrder());
        response.setIsActive(item.getIsActive());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        
        if (item.getProduct() != null) {
            response.setProductId(item.getProduct().getId());
            response.setProductName(item.getProduct().getName());
        }
        
        if (item.getCategory() != null) {
            response.setCategoryId(item.getCategory().getId());
            response.setCategoryName(item.getCategory().getName());
        }
        
        return response;
    }
}
