package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.model.ProductStatus;
import com.clinic.modules.ecommerce.repository.ProductRepository;
import com.clinic.modules.ecommerce.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EcommerceFeatureService ecommerceFeatureService;

    private ProductService productService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        productService = new ProductService(
                productRepository,
                productVariantRepository,
                tenantRepository,
                ecommerceFeatureService
        );

        testTenant = new TenantEntity("test-tenant", "Test Tenant");
        testTenant.setEcommerceEnabled(true);

        testProduct = new ProductEntity(testTenant, "Test Product", "test-product");
    }

    @Test
    void createProduct_Success() {
        // Arrange
        Long tenantId = 1L;
        String name = "Test Product";
        String slug = "test-product";

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(productRepository.existsBySlugAndTenant(slug, tenantId)).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class))).thenReturn(testProduct);

        // Act
        ProductEntity result = productService.createProduct(tenantId, name, slug);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(slug, result.getSlug());
        verify(ecommerceFeatureService).validateEcommerceEnabled(tenantId);
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void createProduct_DuplicateSlug_ThrowsException() {
        // Arrange
        Long tenantId = 1L;
        String name = "Test Product";
        String slug = "test-product";

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(productRepository.existsBySlugAndTenant(slug, tenantId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(tenantId, name, slug)
        );
        assertEquals("Product slug already exists: " + slug, exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProduct_Success() {
        // Arrange
        Long productId = 1L;
        Long tenantId = 1L;

        when(productRepository.findByIdAndTenant(productId, tenantId)).thenReturn(Optional.of(testProduct));

        // Act
        ProductEntity result = productService.getProduct(productId, tenantId);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(ecommerceFeatureService).validateEcommerceEnabled(tenantId);
    }

    @Test
    void getProduct_NotFound_ThrowsException() {
        // Arrange
        Long productId = 1L;
        Long tenantId = 1L;

        when(productRepository.findByIdAndTenant(productId, tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProduct(productId, tenantId)
        );
        assertEquals(productId, exception.getProductId());
        assertEquals(tenantId, exception.getTenantId());
    }

    @Test
    void updateProductStatus_Success() {
        // Arrange
        Long productId = 1L;
        Long tenantId = 1L;
        ProductStatus newStatus = ProductStatus.ACTIVE;

        when(productRepository.findByIdAndTenant(productId, tenantId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(testProduct);

        // Act
        ProductEntity result = productService.updateProductStatus(productId, tenantId, newStatus);

        // Assert
        assertNotNull(result);
        verify(ecommerceFeatureService).validateEcommerceEnabled(tenantId);
        verify(productRepository).save(testProduct);
    }

    @Test
    void validateProductName_EmptyName_ThrowsException() {
        // Arrange
        Long tenantId = 1L;
        String emptyName = "";
        String slug = "test-product";

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(productRepository.existsBySlugAndTenant(slug, tenantId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(tenantId, emptyName, slug)
        );
        assertEquals("Product name is required", exception.getMessage());
    }

    @Test
    void validateProductSlug_InvalidSlug_ThrowsException() {
        // Arrange
        Long tenantId = 1L;
        String name = "Test Product";
        String invalidSlug = "Test Product!"; // Contains invalid characters

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(productRepository.existsBySlugAndTenant(invalidSlug, tenantId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(tenantId, name, invalidSlug)
        );
        assertEquals("Product slug must contain only lowercase letters, numbers, and hyphens", exception.getMessage());
    }
}