package com.clinic.modules.ecommerce.controller.publicapi;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.dto.PublicProductResponse;
import com.clinic.modules.ecommerce.model.ProductStatus;
import com.clinic.modules.ecommerce.service.PublicProductService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PublicProductController.
 */
@WebMvcTest(PublicProductController.class)
class PublicProductControllerTest {

    @Autowired
    private MockMvc mockMvc;



    @MockBean
    private PublicProductService publicProductService;

    @Test
    void getProducts_WithValidTenant_ReturnsProducts() throws Exception {
        // Given
        TenantEntity tenant = createMockTenant();
        PublicProductResponse product = createMockProduct();
        Page<PublicProductResponse> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

        when(publicProductService.resolveTenant("test-clinic", null)).thenReturn(tenant);
        when(publicProductService.getVisibleProducts(eq(1L), any())).thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/public/products")
                .param("slug", "test-clinic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Test Product"))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.total").value(1));
    }

    @Test
    void getProduct_WithValidId_ReturnsProduct() throws Exception {
        // Given
        TenantEntity tenant = createMockTenant();
        PublicProductResponse product = createMockProduct();

        when(publicProductService.resolveTenant("test-clinic", null)).thenReturn(tenant);
        when(publicProductService.getVisibleProduct(1L, 1L)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/public/products/1")
                .param("slug", "test-clinic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getProductBySlug_WithValidSlug_ReturnsProduct() throws Exception {
        // Given
        TenantEntity tenant = createMockTenant();
        PublicProductResponse product = createMockProduct();

        when(publicProductService.resolveTenant("test-clinic", null)).thenReturn(tenant);
        when(publicProductService.getVisibleProductBySlug("test-product", 1L)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/public/products/by-slug/test-product")
                .param("slug", "test-clinic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.slug").value("test-product"));
    }

    @Test
    void searchProducts_WithValidTerm_ReturnsMatchingProducts() throws Exception {
        // Given
        TenantEntity tenant = createMockTenant();
        PublicProductResponse product = createMockProduct();
        Page<PublicProductResponse> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

        when(publicProductService.resolveTenant("test-clinic", null)).thenReturn(tenant);
        when(publicProductService.searchVisibleProducts(eq(1L), eq("test"), any())).thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/public/products/search")
                .param("q", "test")
                .param("slug", "test-clinic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].name").value("Test Product"));
    }

    @Test
    void getProducts_WithInvalidTenant_ReturnsBadRequest() throws Exception {
        // Given
        when(publicProductService.resolveTenant("invalid", null))
                .thenThrow(new IllegalArgumentException("Tenant not found"));

        // When & Then
        mockMvc.perform(get("/public/products")
                .param("slug", "invalid"))
                .andExpect(status().isBadRequest());
    }

    private TenantEntity createMockTenant() {
        TenantEntity tenant = mock(TenantEntity.class);
        when(tenant.getId()).thenReturn(1L);
        when(tenant.getSlug()).thenReturn("test-clinic");
        when(tenant.getName()).thenReturn("Test Clinic");
        return tenant;
    }

    private PublicProductResponse createMockProduct() {
        return new PublicProductResponse(
                1L,
                "Test Product",
                "test-product",
                "A test product description",
                "Short description",
                ProductStatus.ACTIVE,
                new BigDecimal("99.99"),
                new BigDecimal("129.99"),
                "USD",
                false,
                true,
                true,
                Instant.now(),
                Instant.now(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}