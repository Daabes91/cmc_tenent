package com.clinic.modules.ecommerce.dto;

import com.clinic.api.PaginatedResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO for paginated product list responses.
 * 
 * Contains pagination metadata and product list for customer-facing APIs.
 * Uses standardized pagination format for consistency across all endpoints.
 */
public class PublicProductListResponse extends PaginatedResponse<PublicProductResponse> {
    
    public PublicProductListResponse(List<PublicProductResponse> data, 
                                   com.clinic.api.PaginationMetadata pagination) {
        super(data, pagination);
    }
    
    /**
     * Creates a PublicProductListResponse from a Page of products.
     * 
     * @param productPage the page of products
     * @return the response DTO
     */
    public static PublicProductListResponse fromProductPage(Page<PublicProductResponse> productPage) {
        return new PublicProductListResponse(
            productPage.getContent(),
            com.clinic.api.PaginationMetadata.fromPage(productPage)
        );
    }
    
    /**
     * Get products list for backward compatibility.
     */
    public List<PublicProductResponse> getProducts() {
        return getData();
    }
}