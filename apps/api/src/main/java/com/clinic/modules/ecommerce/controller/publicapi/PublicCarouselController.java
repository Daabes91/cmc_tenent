package com.clinic.modules.ecommerce.controller.publicapi;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.dto.PublicCarouselResponse;
import com.clinic.modules.ecommerce.model.Platform;
import com.clinic.modules.ecommerce.service.PublicCarouselService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public API controller for carousel display operations.
 * 
 * Provides endpoints for retrieving carousel content for public consumption
 * with tenant isolation and platform-specific filtering.
 */
@RestController
@RequestMapping("/public/carousels")
@Tag(name = "Public Carousels", description = "Public carousel display operations")
public class PublicCarouselController {

    private static final Logger logger = LoggerFactory.getLogger(PublicCarouselController.class);

    private final PublicCarouselService publicCarouselService;

    @Autowired
    public PublicCarouselController(PublicCarouselService publicCarouselService) {
        this.publicCarouselService = publicCarouselService;
    }

    /**
     * Get carousels by placement and platform.
     * 
     * @param slug The tenant slug for tenant resolution (optional)
     * @param domain The tenant domain for tenant resolution (optional)
     * @param placement The carousel placement (e.g., "hero", "sidebar", "footer")
     * @param platform The target platform (WEB, MOBILE, BOTH)
     * @return List of carousels matching the criteria
     */
    @GetMapping
    @Operation(
        summary = "Get carousels by placement and platform",
        description = "Retrieve carousels filtered by placement and platform for the specified tenant"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carousels retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = PublicCarouselResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PublicCarouselResponse>> getCarousels(
            @Parameter(description = "Tenant slug for tenant resolution", required = false)
            @RequestParam(name = "slug", required = false) String slug,
            
            @Parameter(description = "Tenant domain for tenant resolution", required = false)
            @RequestParam(name = "domain", required = false) String domain,

            @Parameter(description = "Tenant slug header fallback", required = false)
            @RequestHeader(name = "X-Tenant-Slug", required = false) String headerSlug,

            @Parameter(description = "Tenant domain header fallback", required = false)
            @RequestHeader(name = "X-Tenant-Domain", required = false) String headerDomain,
            
            @Parameter(description = "Carousel placement", required = false)
            @RequestParam(required = false) String placement,
            
            @Parameter(description = "Target platform", required = false)
            @RequestParam(required = false) Platform platform) {

        // Prefer query params, otherwise fall back to headers sent by clients (web-next sets X-Tenant-Slug)
        String effectiveSlug = StringUtils.hasText(slug) ? slug : headerSlug;
        String effectiveDomain = StringUtils.hasText(domain) ? domain : headerDomain;

        logger.info("Getting carousels for tenant slug: {}, domain: {}, placement: {}, platform: {}",
                effectiveSlug, effectiveDomain, placement, platform);

        try {
            // Resolve tenant from slug or domain
            TenantEntity tenant = publicCarouselService.resolveTenant(effectiveSlug, effectiveDomain);
            
            List<PublicCarouselResponse> carousels;
            
            if (placement != null && platform != null) {
                carousels = publicCarouselService.getCarouselsByPlacementAndPlatform(tenant.getId(), placement, platform);
            } else if (placement != null) {
                carousels = publicCarouselService.getCarouselsByPlacement(tenant.getId(), placement);
            } else {
                carousels = publicCarouselService.getActiveCarousels(tenant.getId());
            }

            logger.info("Retrieved {} carousels for tenant: {}", carousels.size(), tenant.getSlug());
            return ResponseEntity.ok(carousels);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving carousels", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get carousels by placement (alternative endpoint).
     * 
     * @param slug The tenant slug for tenant resolution (optional)
     * @param domain The tenant domain for tenant resolution (optional)
     * @param placement The carousel placement
     * @param platform The target platform (optional)
     * @return List of carousels for the specified placement
     */
    @GetMapping("/placement/{placement}")
    @Operation(
        summary = "Get carousels by placement",
        description = "Retrieve carousels for a specific placement, optionally filtered by platform"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carousels retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = PublicCarouselResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PublicCarouselResponse>> getCarouselsByPlacement(
            @Parameter(description = "Tenant slug for tenant resolution", required = false)
            @RequestParam(name = "slug", required = false) String slug,
            
            @Parameter(description = "Tenant domain for tenant resolution", required = false)
            @RequestParam(name = "domain", required = false) String domain,

            @Parameter(description = "Tenant slug header fallback", required = false)
            @RequestHeader(name = "X-Tenant-Slug", required = false) String headerSlug,

            @Parameter(description = "Tenant domain header fallback", required = false)
            @RequestHeader(name = "X-Tenant-Domain", required = false) String headerDomain,
            
            @Parameter(description = "Carousel placement", required = true)
            @PathVariable String placement,
            
            @Parameter(description = "Target platform", required = false)
            @RequestParam(required = false) Platform platform) {

        String effectiveSlug = StringUtils.hasText(slug) ? slug : headerSlug;
        String effectiveDomain = StringUtils.hasText(domain) ? domain : headerDomain;

        logger.info("Getting carousels for tenant slug: {}, domain: {}, placement: {}, platform: {}",
                effectiveSlug, effectiveDomain, placement, platform);

        try {
            // Resolve tenant from slug or domain
            TenantEntity tenant = publicCarouselService.resolveTenant(effectiveSlug, effectiveDomain);
            
            List<PublicCarouselResponse> carousels;
            
            if (platform != null) {
                carousels = publicCarouselService.getCarouselsByPlacementAndPlatform(tenant.getId(), placement, platform);
            } else {
                carousels = publicCarouselService.getCarouselsByPlacement(tenant.getId(), placement);
            }

            logger.info("Retrieved {} carousels for tenant: {}, placement: {}", 
                       carousels.size(), tenant.getSlug(), placement);
            return ResponseEntity.ok(carousels);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving carousels for placement: {}", placement, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all active carousels for the tenant.
     * 
     * @param slug The tenant slug for tenant resolution (optional)
     * @param domain The tenant domain for tenant resolution (optional)
     * @return List of all active carousels
     */
    @GetMapping("/all")
    @Operation(
        summary = "Get all active carousels",
        description = "Retrieve all active carousels for the specified tenant"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carousels retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = PublicCarouselResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PublicCarouselResponse>> getAllActiveCarousels(
            @Parameter(description = "Tenant slug for tenant resolution", required = false)
            @RequestParam(name = "slug", required = false) String slug,
            
            @Parameter(description = "Tenant domain for tenant resolution", required = false)
            @RequestParam(name = "domain", required = false) String domain,

            @Parameter(description = "Tenant slug header fallback", required = false)
            @RequestHeader(name = "X-Tenant-Slug", required = false) String headerSlug,

            @Parameter(description = "Tenant domain header fallback", required = false)
            @RequestHeader(name = "X-Tenant-Domain", required = false) String headerDomain) {

        String effectiveSlug = StringUtils.hasText(slug) ? slug : headerSlug;
        String effectiveDomain = StringUtils.hasText(domain) ? domain : headerDomain;

        logger.info("Getting all active carousels for tenant slug: {}, domain: {}", effectiveSlug, effectiveDomain);

        try {
            // Resolve tenant from slug or domain
            TenantEntity tenant = publicCarouselService.resolveTenant(effectiveSlug, effectiveDomain);
            List<PublicCarouselResponse> carousels = publicCarouselService.getActiveCarousels(tenant.getId());

            logger.info("Retrieved {} active carousels for tenant: {}", carousels.size(), tenant.getSlug());
            return ResponseEntity.ok(carousels);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving active carousels", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
