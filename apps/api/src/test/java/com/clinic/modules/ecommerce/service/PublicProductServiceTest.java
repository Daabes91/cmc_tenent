package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.ecommerce.dto.PublicProductResponse;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.model.ProductStatus;
import com.clinic.modules.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PublicProductService.
 */
@ExtendWith(MockitoExtension.class)
class PublicProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private EcommerceFeatureService ecommerceFeatureService;

    private PublicProductService publicProductService;

    @BeforeEach
    void setUp() {
        publicProductService = new PublicProductService(
                productRepository, tenantService, ecommerceFeatureService);
    }

    @Test
    void resolveTenant_WithValidSlug_ReturnsTenant() {
        // Given
        TenantEntity tenant = createMockTenant();
        when(tenantService.findActiveBySlug("test-clinic")).thenReturn(Optional.of(tenant));
        doNothing().when(ecommerceFeatureService).validateEcommerceEnabled(1L);

        // When
        TenantEntity result = publicProductService.resolveTenant("test-clinic", null);

        // Then
        assertNotNull(result);
        assertEquals("test-clinic", result.getSlug());
        verify(ecommerceFeatureService).validateEcommerceEnabled(1L);
    }

    @Test
    void resolveTenant_WithInvalidSlug_ThrowsException() {
        // Given
        when(tenantService.findActiveBySlug("invalid")).thenReturn(Optional.empty());
        when(tenantService.findActiveByDomain(null)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                publicProductService.resolveTenant("invalid", null));
    }

    @Test
    void getVisibleProducts_WithValidTenant_ReturnsProducts() {
        // Given
        ProductEntity product = createMockProduct();
        Page<ProductEntity> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findVisibleByTenant(eq(1L), any())).thenReturn(productPage);

        // When
        Page<PublicProductResponse> result = publicProductService.getVisibleProducts(1L, PageRequest.of(0, 20));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).name());
    }

    @Test
    void getVisibleProduct_WithValidId_ReturnsProduct() {
        // Given
        ProductEntity product = createMockProduct();
        when(productRepository.findByIdAndTenant(1L, 1L)).thenReturn(Optional.of(product));

        // When
        PublicProductResponse result = publicProductService.getVisibleProduct(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.name());
        assertEquals(ProductStatus.ACTIVE, result.status());
    }

    @Test
    void getVisibleProduct_WithInvisibleProduct_ThrowsException() {
        // Given
        ProductEntity product = createMockProduct();
        when(product.isVisible()).thenReturn(false);
        when(productRepository.findByIdAndTenant(1L, 1L)).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> 
                publicProductService.getVisibleProduct(1L, 1L));
    }

    @Test
    void getVisibleProduct_WithDraftProduct_ThrowsException() {
        // Given
        ProductEntity product = createMockProduct();
        when(product.getStatus()).thenReturn(ProductStatus.DRAFT);
        when(productRepository.findByIdAndTenant(1L, 1L)).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> 
                publicProductService.getVisibleProduct(1L, 1L));
    }

    @Test
    void searchVisibleProducts_WithValidTerm_ReturnsMatchingProducts() {
        // Given
        ProductEntity product = createMockProduct();
        Page<ProductEntity> productPage = new PageImpl<>(List.of(product));
        when(productRepository.searchVisibleByTenant(eq(1L), eq("test"), any())).thenReturn(productPage);

        // When
        Page<PublicProductResponse> result = publicProductService.searchVisibleProducts(1L, "test", PageRequest.of(0, 20));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).name());
    }

    private TenantEntity createMockTenant() {
        TenantEntity tenant = mock(TenantEntity.class);
        when(tenant.getId()).thenReturn(1L);
        when(tenant.getSlug()).thenReturn("test-clinic");
        when(tenant.getName()).thenReturn("Test Clinic");
        return tenant;
    }

    private ProductEntity createMockProduct() {
        ProductEntity product = mock(ProductEntity.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Test Product");
        when(product.getSlug()).thenReturn("test-product");
        when(product.getDescription()).thenReturn("A test product");
        when(product.getShortDescription()).thenReturn("Short description");
        when(product.getStatus()).thenReturn(ProductStatus.ACTIVE);
        when(product.getPrice()).thenReturn(new BigDecimal("99.99"));
        when(product.getCompareAtPrice()).thenReturn(new BigDecimal("129.99"));
        when(product.getCurrency()).thenReturn("USD");
        when(product.hasVariants()).thenReturn(false);
        when(product.isTaxable()).thenReturn(true);
        when(product.isVisible()).thenReturn(true);
        when(product.getCreatedAt()).thenReturn(Instant.now());
        when(product.getUpdatedAt()).thenReturn(Instant.now());
        when(product.getVariants()).thenReturn(List.of());
        when(product.getImages()).thenReturn(List.of());
        when(product.getProductCategories()).thenReturn(List.of());
        return product;
    }
}