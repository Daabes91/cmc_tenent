package com.clinic.modules.ecommerce.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Common API documentation annotations for e-commerce endpoints.
 * Provides standardized documentation patterns for consistent API docs.
 */
public class ApiDocumentation {

    /**
     * Standard tenant resolution parameters for public endpoints.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(
            name = "slug",
            description = "Tenant slug for tenant resolution (alternative to domain)",
            in = ParameterIn.QUERY,
            required = false,
            example = "my-clinic"
    )
    @Parameter(
            name = "domain",
            description = "Tenant domain for tenant resolution (alternative to slug)",
            in = ParameterIn.QUERY,
            required = false,
            example = "my-clinic.example.com"
    )
    public @interface TenantResolutionParams {}

    /**
     * Standard pagination parameters.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(
            name = "page",
            description = "Page number (0-based)",
            in = ParameterIn.QUERY,
            required = false,
            example = "0"
    )
    @Parameter(
            name = "size",
            description = "Page size (max 100)",
            in = ParameterIn.QUERY,
            required = false,
            example = "20"
    )
    @Parameter(
            name = "sort",
            description = "Sort criteria (property,direction)",
            in = ParameterIn.QUERY,
            required = false,
            example = "createdAt,desc"
    )
    public @interface PaginationParams {}

    /**
     * Standard success response for single item.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Success Response",
                            value = """
                                    {
                                      "success": true,
                                      "data": { ... },
                                      "timestamp": "2024-01-15T10:30:00Z"
                                    }
                                    """
                    )
            )
    )
    public @interface SuccessResponse {}

    /**
     * Standard success response for paginated list.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Paginated Response",
                            value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "data": [ ... ],
                                        "pagination": {
                                          "page": 0,
                                          "page_size": 20,
                                          "total": 100,
                                          "total_pages": 5,
                                          "has_next": true,
                                          "has_previous": false,
                                          "first": true,
                                          "last": false
                                        }
                                      },
                                      "timestamp": "2024-01-15T10:30:00Z"
                                    }
                                    """
                    )
            )
    )
    public @interface PaginatedSuccessResponse {}

    /**
     * Standard error responses.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid parameters",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                              "success": false,
                                              "error_code": "VALIDATION_ERROR",
                                              "message": "Invalid request parameters",
                                              "errors": [
                                                {
                                                  "field": "price",
                                                  "message": "Price must be positive",
                                                  "rejected_value": -10.00,
                                                  "timestamp": "2024-01-15T10:30:00Z"
                                                }
                                              ],
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "path": "/public/products"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Resource not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Not Found Error",
                                    value = """
                                            {
                                              "success": false,
                                              "error_code": "NOT_FOUND",
                                              "message": "Product not found",
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "path": "/public/products/123"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Server Error",
                                    value = """
                                            {
                                              "success": false,
                                              "error_code": "INTERNAL_SERVER_ERROR",
                                              "message": "An unexpected error occurred",
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "path": "/public/products"
                                            }
                                            """
                            )
                    )
            )
    })
    public @interface StandardErrorResponses {}

    /**
     * E-commerce feature disabled response.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - E-commerce feature not enabled for tenant",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Feature Disabled",
                            value = """
                                    {
                                      "success": false,
                                      "error_code": "ECOMMERCE_FEATURE_DISABLED",
                                      "message": "E-commerce functionality is not enabled for this tenant",
                                      "timestamp": "2024-01-15T10:30:00Z",
                                      "path": "/public/products"
                                    }
                                    """
                    )
            )
    )
    public @interface EcommerceFeatureDisabledResponse {}

    /**
     * Rate limit exceeded response.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests - Rate limit exceeded",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Rate Limit Exceeded",
                            value = """
                                    {
                                      "success": false,
                                      "error_code": "RATE_LIMIT_EXCEEDED",
                                      "message": "Rate limit exceeded. Please try again later.",
                                      "timestamp": "2024-01-15T10:30:00Z",
                                      "path": "/public/products"
                                    }
                                    """
                    )
            )
    )
    public @interface RateLimitResponse {}
}