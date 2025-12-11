package com.clinic.api;

import com.clinic.modules.ecommerce.dto.CartResponse;
import com.clinic.modules.ecommerce.dto.PublicProductListResponse;
import com.clinic.modules.ecommerce.dto.PublicProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for API response format consistency.
 * Validates that all responses follow standardized structure and formatting.
 */
class ApiConsistencyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testApiResponseStructure() {
        // Test success response
        ApiResponse<String> successResponse = ApiResponse.success("test data", "Operation successful");
        
        assertTrue(successResponse.isSuccess());
        assertEquals("test data", successResponse.getData());
        assertEquals("Operation successful", successResponse.getMessage());
        assertNull(successResponse.getErrorCode());
        assertNull(successResponse.getErrors());
        assertNotNull(successResponse.getTimestamp());
        assertTrue(TimestampFormatter.isValidIso8601(successResponse.getTimestamp()));

        // Test error response
        List<ApiError> errors = List.of(ApiError.of("field", "error message"));
        ApiResponse<Void> errorResponse = ApiResponse.error("ERROR_CODE", "Error message", errors);
        
        assertFalse(errorResponse.isSuccess());
        assertNull(errorResponse.getData());
        assertEquals("Error message", errorResponse.getMessage());
        assertEquals("ERROR_CODE", errorResponse.getErrorCode());
        assertEquals(1, errorResponse.getErrors().size());
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(TimestampFormatter.isValidIso8601(errorResponse.getTimestamp()));
    }

    @Test
    void testPaginationMetadataConsistency() {
        // Create test page
        List<String> content = List.of("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(1, 10), 25);
        
        PaginationMetadata metadata = PaginationMetadata.fromPage(page);
        
        assertEquals(1, metadata.getPage());
        assertEquals(10, metadata.getPageSize());
        assertEquals(25, metadata.getTotal());
        assertEquals(3, metadata.getTotalPages());
        assertTrue(metadata.isHasNext());
        assertTrue(metadata.isHasPrevious());
        assertFalse(metadata.isFirst());
        assertFalse(metadata.isLast());
    }

    @Test
    void testPaginatedResponseStructure() {
        List<String> data = List.of("item1", "item2");
        PaginatedResponse<String> response = PaginatedResponse.of(data, 0, 10, 2);
        
        assertEquals(2, response.getData().size());
        assertNotNull(response.getPagination());
        assertEquals(0, response.getPagination().getPage());
        assertEquals(10, response.getPagination().getPageSize());
        assertEquals(2, response.getPagination().getTotal());
    }

    @Test
    void testCurrencyFormatting() {
        BigDecimal amount = new BigDecimal("123.456");
        CurrencyFormatter.CurrencyAmount currency = CurrencyFormatter.of(amount, "USD");
        
        assertEquals(new BigDecimal("123.46"), currency.getAmount());
        assertEquals("USD", currency.getCurrency());
        assertNotNull(currency.getFormatted());
        assertTrue(currency.getFormatted().contains("123.46"));
    }

    @Test
    void testTimestampFormatting() {
        LocalDateTime now = LocalDateTime.now();
        String isoString = TimestampFormatter.formatToIso8601(now);
        
        assertNotNull(isoString);
        assertTrue(TimestampFormatter.isValidIso8601(isoString));
        assertTrue(isoString.endsWith("Z"));
        assertTrue(isoString.contains("T"));
    }

    @Test
    void testCartResponseFormatConsistency() {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setId(1L);
        cartResponse.setSessionId("session123");
        cartResponse.setSubtotal(new BigDecimal("100.00"), "USD");
        cartResponse.setTaxAmount(new BigDecimal("10.00"), "USD");
        cartResponse.setTotalAmount(new BigDecimal("110.00"), "USD");
        cartResponse.setCreatedAt(LocalDateTime.now());
        cartResponse.setUpdatedAt(LocalDateTime.now());

        // Validate currency formatting
        assertNotNull(cartResponse.getSubtotal());
        assertEquals("USD", cartResponse.getSubtotal().getCurrency());
        assertEquals(new BigDecimal("100.00"), cartResponse.getSubtotal().getAmount());

        // Validate timestamp formatting
        assertNotNull(cartResponse.getCreatedAt());
        assertTrue(TimestampFormatter.isValidIso8601(cartResponse.getCreatedAt()));
    }

    @Test
    void testPublicProductListResponseConsistency() {
        List<PublicProductResponse> products = List.of(); // Empty list for test
        Page<PublicProductResponse> page = new PageImpl<>(products, PageRequest.of(0, 20), 0);
        
        PublicProductListResponse response = PublicProductListResponse.fromProductPage(page);
        
        assertNotNull(response.getData());
        assertNotNull(response.getPagination());
        assertEquals(0, response.getPagination().getPage());
        assertEquals(20, response.getPagination().getPageSize());
        assertEquals(0, response.getPagination().getTotal());
        
        // Test backward compatibility
        assertEquals(response.getData(), response.getProducts());
    }

    @Test
    void testResponseFormatValidation() {
        // Test valid success response
        ApiResponse<String> validResponse = ApiResponse.success("data");
        ResponseFormatValidator.ValidationResult result = ResponseFormatValidator.validateResponse(validResponse);
        assertTrue(result.isValid(), "Valid response should pass validation: " + result.getErrors());

        // Test valid error response
        ApiResponse<Void> errorResponse = ApiResponse.error("ERROR_CODE", "Error message");
        result = ResponseFormatValidator.validateResponse(errorResponse);
        assertTrue(result.isValid(), "Valid error response should pass validation: " + result.getErrors());
    }

    @Test
    void testPaginatedResponseValidation() {
        List<String> data = List.of("item1", "item2");
        PaginatedResponse<String> paginatedResponse = PaginatedResponse.of(data, 0, 10, 2);
        ApiResponse<PaginatedResponse<String>> response = ApiResponse.success(paginatedResponse);
        
        ResponseFormatValidator.ValidationResult result = ResponseFormatValidator.validatePaginatedResponse(response);
        assertTrue(result.isValid(), "Valid paginated response should pass validation: " + result.getErrors());
    }

    @Test
    void testCurrencyValidation() {
        // Test valid currencies
        assertTrue(CurrencyFormatter.isValidCurrency("USD"));
        assertTrue(CurrencyFormatter.isValidCurrency("EUR"));
        assertTrue(CurrencyFormatter.isValidCurrency("GBP"));
        
        // Test invalid currencies
        assertFalse(CurrencyFormatter.isValidCurrency("INVALID"));
        assertFalse(CurrencyFormatter.isValidCurrency(""));
        assertFalse(CurrencyFormatter.isValidCurrency(null));
    }

    @Test
    void testApiErrorStructure() {
        ApiError error = ApiError.of("fieldName", "Error message", "rejectedValue", "ERROR_CODE");
        
        assertEquals("fieldName", error.getField());
        assertEquals("Error message", error.getMessage());
        assertEquals("rejectedValue", error.getRejectedValue());
        assertEquals("ERROR_CODE", error.getCode());
        assertNotNull(error.getTimestamp());
        assertTrue(TimestampFormatter.isValidIso8601(error.getTimestamp()));
    }

    @Test
    void testJsonPropertyNaming() throws Exception {
        // Test that JSON properties use snake_case
        ApiResponse<String> response = ApiResponse.success("test");
        String json = objectMapper.writeValueAsString(response);
        
        // Should contain snake_case properties
        assertTrue(json.contains("\"success\""));
        assertTrue(json.contains("\"timestamp\""));
        assertFalse(json.contains("\"errorCode\""));
        
        // Test error response
        ApiResponse<Void> errorResponse = ApiResponse.error("ERROR_CODE", "Error message");
        String errorJson = objectMapper.writeValueAsString(errorResponse);
        assertTrue(errorJson.contains("\"error_code\""));
    }
}