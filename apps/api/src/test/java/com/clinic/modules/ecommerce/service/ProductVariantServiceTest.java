package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.exception.InsufficientStockException;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.model.ProductVariantEntity;
import com.clinic.modules.ecommerce.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductVariantService.
 */
@ExtendWith(MockitoExtension.class)
class ProductVariantServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductService productService;

    @Mock
    private EcommerceFeatureService ecommerceFeatureService;

    private ProductVariantService productVariantService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private ProductVariantEntity testVariant;

    @BeforeEach
    void setUp() {
        productVariantService = new ProductVariantService(
                productVariantRepository,
                productService,
                ecommerceFeatureService
        );

        testTenant = new TenantEntity("test-tenant", "Test Tenant");
        testTenant.setEcommerceEnabled(true);

        testProduct = new ProductEntity(testTenant, "Test Product", "test-product");
        testVariant = new ProductVariantEntity(testProduct, testTenant, "TEST-SKU", "Test Variant", new BigDecimal("10.00"));
        testVariant.setStockQuantity(100);
    }

    @Test
    void createVariant_Success() {
        // Arrange
        Long productId = 1L;
        Long tenantId = 1L;
        String sku = "TEST-SKU";
        String name = "Test Variant";
        BigDecimal price = new BigDecimal("10.00");

        when(productService.getProduct(productId, tenantId)).thenReturn(testProduct);
        when(productVariantRepository.existsBySkuAndTenant(sku, tenantId)).thenReturn(false);
        when(productVariantRepository.save(any(ProductVariantEntity.class))).thenReturn(testVariant);

        // Act
        ProductVariantEntity result = productVariantService.createVariant(productId, tenantId, sku, name, price);

        // Assert
        assertNotNull(result);
        assertEquals(sku, result.getSku());
        assertEquals(name, result.getName());
        assertEquals(price, result.getPrice());
        verify(ecommerceFeatureService).validateEcommerceEnabled(tenantId);
        verify(productVariantRepository).save(any(ProductVariantEntity.class));
    }

    @Test
    void createVariant_DuplicateSku_ThrowsException() {
        // Arrange
        Long productId = 1L;
        Long tenantId = 1L;
        String sku = "TEST-SKU";
        String name = "Test Variant";
        BigDecimal price = new BigDecimal("10.00");

        when(productService.getProduct(productId, tenantId)).thenReturn(testProduct);
        when(productVariantRepository.existsBySkuAndTenant(sku, tenantId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productVariantService.createVariant(productId, tenantId, sku, name, price)
        );
        assertEquals("Variant SKU already exists: " + sku, exception.getMessage());
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void getVariant_Success() {
        // Arrange
        Long variantId = 1L;
        Long tenantId = 1L;

        when(productVariantRepository.findByIdAndTenant(variantId, tenantId)).thenReturn(Optional.of(testVariant));

        // Act
        ProductVariantEntity result = productVariantService.getVariant(variantId, tenantId);

        // Assert
        assertNotNull(result);
        assertEquals(testVariant, result);
        verify(ecommerceFeatureService).validateEcommerceEnabled(tenantId);
    }

    @Test
    void getVariant_NotFound_ThrowsException() {
        // Arrange
        Long variantId = 1L;
        Long tenantId = 1L;

        when(productVariantRepository.findByIdAndTenant(variantId, tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productVariantService.getVariant(variantId, tenantId)
        );
        assertTrue(exception.getMessage().contains("Product variant with ID " + variantId + " not found"));
    }

    @Test
    void decreaseStock_Success() {
        // Arrange
        Long variantId = 1L;
        Long tenantId = 1L;
        int quantity = 10;

        when(productVariantRepository.findByIdAndTenant(variantId, tenantId)).thenReturn(Optional.of(testVariant));
        when(productVariantRepository.save(testVariant)).thenReturn(testVariant);

        // Act
        ProductVariantEntity result = productVariantService.decreaseStock(variantId, tenantId, quantity);

        // Assert
        assertNotNull(result);
        verify(ecommerceFeatureService).validateEcommerceEnabled(tenantId);
        verify(productVariantRepository).findByIdAndTenant(variantId, tenantId);
        verify(productVariantRepository).save(testVariant);
    }

    @Test
    void decreaseStock_InsufficientStock_ThrowsException() {
        // Arrange
        Long variantId = 1L;
        Long tenantId = 1L;
        int quantity = 200; // More than available stock (100)

        testVariant.setStockQuantity(50); // Set lower stock
        when(productVariantRepository.findByIdAndTenant(variantId, tenantId)).thenReturn(Optional.of(testVariant));

        // Act & Assert
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> productVariantService.decreaseStock(variantId, tenantId, quantity)
        );
        assertTrue(exception.getMessage().contains("Insufficient stock for variant " + variantId));
    }

    @Test
    void canFulfillQuantity_Success() {
        // Arrange
        Long variantId = 1L;
        Long tenantId = 1L;
        int quantity = 50;

        when(productVariantRepository.findByIdAndTenant(variantId, tenantId)).thenReturn(Optional.of(testVariant));

        // Act
        boolean result = productVariantService.canFulfillQuantity(variantId, tenantId, quantity);

        // Assert
        assertTrue(result);
        verify(ecommerceFeatureService).validateEcommerceEnabled(tenantId);
        verify(productVariantRepository).findByIdAndTenant(variantId, tenantId);
    }

    @Test
    void validateVariantSku_EmptySku_ThrowsException() {
        // Arrange
        Long productId = 1L;
        Long tenantId = 1L;
        String emptySku = "";
        String name = "Test Variant";
        BigDecimal price = new BigDecimal("10.00");

        when(productService.getProduct(productId, tenantId)).thenReturn(testProduct);
        when(productVariantRepository.existsBySkuAndTenant(emptySku, tenantId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productVariantService.createVariant(productId, tenantId, emptySku, name, price)
        );
        assertEquals("Variant SKU is required", exception.getMessage());
    }

    @Test
    void validateVariantPrice_NullPrice_ThrowsException() {
        // Arrange
        Long productId = 1L;
        Long tenantId = 1L;
        String sku = "TEST-SKU";
        String name = "Test Variant";
        BigDecimal nullPrice = null;

        when(productService.getProduct(productId, tenantId)).thenReturn(testProduct);
        when(productVariantRepository.existsBySkuAndTenant(sku, tenantId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productVariantService.createVariant(productId, tenantId, sku, name, nullPrice)
        );
        assertEquals("Variant price is required", exception.getMessage());
    }
}