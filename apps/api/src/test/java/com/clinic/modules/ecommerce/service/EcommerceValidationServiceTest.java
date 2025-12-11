package com.clinic.modules.ecommerce.service;

import com.clinic.api.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EcommerceValidationService.
 * Tests validation logic for e-commerce operations.
 */
class EcommerceValidationServiceTest {

    @Mock
    private Validator validator;

    private EcommerceValidationService validationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validationService = new EcommerceValidationService(validator);
    }

    @Test
    void testValidateProductData_ValidData() {
        // Given
        String name = "Test Product";
        String sku = "TEST-SKU-123";
        BigDecimal price = new BigDecimal("99.99");
        String description = "A test product description";
        String slug = "test-product";

        // When
        List<ApiError> errors = validationService.validateProductData(name, sku, price, description, slug);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateProductData_InvalidName() {
        // Given - empty name
        String name = "";
        String sku = "TEST-SKU-123";
        BigDecimal price = new BigDecimal("99.99");
        String description = "A test product description";
        String slug = "test-product";

        // When
        List<ApiError> errors = validationService.validateProductData(name, sku, price, description, slug);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "name".equals(error.getField())));

        // Given - null name
        errors = validationService.validateProductData(null, sku, price, description, slug);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "name".equals(error.getField())));

        // Given - name too long
        String longName = "A".repeat(300);
        errors = validationService.validateProductData(longName, sku, price, description, slug);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "name".equals(error.getField())));
    }

    @Test
    void testValidateProductData_InvalidSku() {
        // Given - invalid SKU format
        String name = "Test Product";
        String sku = "invalid sku with spaces";
        BigDecimal price = new BigDecimal("99.99");
        String description = "A test product description";
        String slug = "test-product";

        // When
        List<ApiError> errors = validationService.validateProductData(name, sku, price, description, slug);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "sku".equals(error.getField())));
    }

    @Test
    void testValidateProductData_InvalidPrice() {
        // Given - negative price
        String name = "Test Product";
        String sku = "TEST-SKU-123";
        BigDecimal price = new BigDecimal("-10.00");
        String description = "A test product description";
        String slug = "test-product";

        // When
        List<ApiError> errors = validationService.validateProductData(name, sku, price, description, slug);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "price".equals(error.getField())));

        // Given - price too high
        price = new BigDecimal("9999999.99");
        errors = validationService.validateProductData(name, sku, price, description, slug);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "price".equals(error.getField())));
    }

    @Test
    void testValidateProductData_InvalidSlug() {
        // Given - invalid slug format
        String name = "Test Product";
        String sku = "TEST-SKU-123";
        BigDecimal price = new BigDecimal("99.99");
        String description = "A test product description";
        String slug = "Invalid Slug With Spaces";

        // When
        List<ApiError> errors = validationService.validateProductData(name, sku, price, description, slug);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "slug".equals(error.getField())));
    }

    @Test
    void testValidateCartItem_ValidData() {
        // Given
        Long productId = 123L;
        Long variantId = 456L;
        Integer quantity = 5;

        // When
        List<ApiError> errors = validationService.validateCartItem(productId, variantId, quantity);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateCartItem_InvalidData() {
        // Given - null product ID
        Long productId = null;
        Long variantId = 456L;
        Integer quantity = 5;

        // When
        List<ApiError> errors = validationService.validateCartItem(productId, variantId, quantity);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "productId".equals(error.getField())));

        // Given - invalid quantity
        productId = 123L;
        quantity = 0;
        errors = validationService.validateCartItem(productId, variantId, quantity);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "quantity".equals(error.getField())));

        // Given - quantity too high
        quantity = 20000;
        errors = validationService.validateCartItem(productId, variantId, quantity);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "quantity".equals(error.getField())));
    }

    @Test
    void testValidateCustomerInfo_ValidData() {
        // Given
        String name = "John Doe";
        String email = "john.doe@example.com";
        String phone = "+1234567890";
        String addressLine1 = "123 Main St";
        String city = "New York";
        String country = "USA";

        // When
        List<ApiError> errors = validationService.validateCustomerInfo(name, email, phone, addressLine1, city, country);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateCustomerInfo_InvalidEmail() {
        // Given
        String name = "John Doe";
        String email = "invalid-email";
        String phone = "+1234567890";
        String addressLine1 = "123 Main St";
        String city = "New York";
        String country = "USA";

        // When
        List<ApiError> errors = validationService.validateCustomerInfo(name, email, phone, addressLine1, city, country);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "customerEmail".equals(error.getField())));
    }

    @Test
    void testValidateCustomerInfo_MissingRequiredFields() {
        // Given - missing name
        String name = null;
        String email = "john.doe@example.com";
        String phone = "+1234567890";
        String addressLine1 = "123 Main St";
        String city = "New York";
        String country = "USA";

        // When
        List<ApiError> errors = validationService.validateCustomerInfo(name, email, phone, addressLine1, city, country);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "customerName".equals(error.getField())));

        // Given - missing address
        name = "John Doe";
        addressLine1 = null;
        errors = validationService.validateCustomerInfo(name, email, phone, addressLine1, city, country);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "billingAddressLine1".equals(error.getField())));
    }

    @Test
    void testValidateCarouselData_ValidData() {
        // Given
        String name = "Hero Carousel";
        String type = "HERO";
        String placement = "homepage";
        String platform = "WEB";

        // When
        List<ApiError> errors = validationService.validateCarouselData(name, type, placement, platform);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateCarouselData_InvalidType() {
        // Given
        String name = "Hero Carousel";
        String type = "INVALID_TYPE";
        String placement = "homepage";
        String platform = "WEB";

        // When
        List<ApiError> errors = validationService.validateCarouselData(name, type, placement, platform);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "type".equals(error.getField())));
    }

    @Test
    void testValidateCarouselData_InvalidPlatform() {
        // Given
        String name = "Hero Carousel";
        String type = "HERO";
        String placement = "homepage";
        String platform = "INVALID_PLATFORM";

        // When
        List<ApiError> errors = validationService.validateCarouselData(name, type, placement, platform);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "platform".equals(error.getField())));
    }

    @Test
    void testValidatePaymentData_ValidData() {
        // Given
        BigDecimal amount = new BigDecimal("99.99");
        String currency = "USD";

        // When
        List<ApiError> errors = validationService.validatePaymentData(amount, currency);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidatePaymentData_InvalidAmount() {
        // Given - zero amount
        BigDecimal amount = BigDecimal.ZERO;
        String currency = "USD";

        // When
        List<ApiError> errors = validationService.validatePaymentData(amount, currency);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "amount".equals(error.getField())));

        // Given - negative amount
        amount = new BigDecimal("-10.00");
        errors = validationService.validatePaymentData(amount, currency);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "amount".equals(error.getField())));
    }

    @Test
    void testValidatePaymentData_InvalidCurrency() {
        // Given
        BigDecimal amount = new BigDecimal("99.99");
        String currency = "INVALID";

        // When
        List<ApiError> errors = validationService.validatePaymentData(amount, currency);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "currency".equals(error.getField())));
    }

    @Test
    void testValidatePaginationParams_ValidData() {
        // Given
        Integer page = 0;
        Integer size = 20;

        // When
        List<ApiError> errors = validationService.validatePaginationParams(page, size);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidatePaginationParams_InvalidData() {
        // Given - negative page
        Integer page = -1;
        Integer size = 20;

        // When
        List<ApiError> errors = validationService.validatePaginationParams(page, size);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "page".equals(error.getField())));

        // Given - size too large
        page = 0;
        size = 200;
        errors = validationService.validatePaginationParams(page, size);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "size".equals(error.getField())));

        // Given - size too small
        size = 0;
        errors = validationService.validatePaginationParams(page, size);

        // Then
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> "size".equals(error.getField())));
    }
}