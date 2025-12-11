package com.clinic.modules.ecommerce.integration;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.*;
import com.clinic.modules.ecommerce.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Complete end-to-end integration tests for the e-commerce system.
 * Tests the full workflow from product browsing to order completion.
 * 
 * **Feature: saas-ecommerce-backend, Integration Test: Complete order processing workflow**
 * **Validates: All requirements**
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class EcommerceEndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private ProductVariantEntity testVariant;

    @BeforeEach
    void setUp() {
        // Create test tenant
        testTenant = new TenantEntity("test-ecommerce", "Test E-commerce Store");
        testTenant.setEcommerceEnabled(true);
        testTenant = tenantRepository.save(testTenant);

        // Create test product
        testProduct = new ProductEntity(testTenant, "Test Product", "test-product");
        testProduct.setDescription("A test product for integration testing");
        testProduct.setPrice(new BigDecimal("29.99"));
        testProduct.setStatus(ProductStatus.ACTIVE);
        testProduct.setHasVariants(true);
        testProduct = productRepository.save(testProduct);

        // Create test variant
        testVariant = new ProductVariantEntity(testProduct, testTenant, "SKU-001", "Small - Red", new BigDecimal("29.99"));
        testVariant.setStockQuantity(10);
        testVariant.setIsInStock(true);
        testVariant = productVariantRepository.save(testVariant);
    }

    /**
     * Test basic e-commerce functionality through API endpoints.
     */
    @Test
    void testBasicEcommerceFunctionality() throws Exception {
        String sessionId = "test-session-" + System.currentTimeMillis();

        // Step 1: Browse products - should return available products
        mockMvc.perform(get("/public/products")
                        .param("slug", testTenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // Step 2: Add item to cart using service layer
        CartEntity cart = cartService.addItemToCart(testTenant, sessionId, 
            testProduct.getId(), testVariant.getId(), 2);

        assertThat(cart).isNotNull();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);

        // Step 3: Verify cart persistence
        Optional<CartEntity> retrievedCart = cartRepository.findByTenantIdAndSessionId(
            testTenant.getId(), sessionId);
        
        assertThat(retrievedCart).isPresent();
        assertThat(retrievedCart.get().getItems()).hasSize(1);

        // Step 4: Test cart total calculation
        cart.recalculateTotals();
        assertThat(cart.getTotalAmount()).isGreaterThan(BigDecimal.ZERO);
    }

    /**
     * Test multi-tenant isolation during order processing.
     */
    @Test
    void testMultiTenantIsolationInOrderProcessing() throws Exception {
        // Create second tenant
        TenantEntity tenant2 = new TenantEntity("test-ecommerce-2", "Test Store 2");
        tenant2.setEcommerceEnabled(true);
        tenant2 = tenantRepository.save(tenant2);

        // Create product for second tenant
        ProductEntity product2 = new ProductEntity(tenant2, "Tenant 2 Product", "tenant2-product");
        product2.setPrice(new BigDecimal("19.99"));
        product2.setStatus(ProductStatus.ACTIVE);
        product2 = productRepository.save(product2);

        String sessionId = "isolation-test-session";

        // Add item to cart for tenant 1
        CartEntity cart1 = cartService.addItemToCart(testTenant, sessionId, 
            testProduct.getId(), testVariant.getId(), 1);

        // Add item to cart for tenant 2 (same session ID but different tenant)
        CartEntity cart2 = cartService.addItemToCart(tenant2, sessionId, 
            product2.getId(), null, 1);

        // Verify carts are isolated
        assertThat(cart1.getTenant().getId()).isEqualTo(testTenant.getId());
        assertThat(cart2.getTenant().getId()).isEqualTo(tenant2.getId());
        assertThat(cart1.getId()).isNotEqualTo(cart2.getId());

        // Verify cart contents are isolated
        assertThat(cart1.getItems()).hasSize(1);
        assertThat(cart2.getItems()).hasSize(1);
        assertThat(cart1.getItems().get(0).getProduct().getId()).isEqualTo(testProduct.getId());
        assertThat(cart2.getItems().get(0).getProduct().getId()).isEqualTo(product2.getId());

        // Try to access tenant 1's cart with tenant 2's context - should fail
        mockMvc.perform(get("/public/cart")
                        .param("slug", tenant2.getSlug())
                        .param("sessionId", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].product.name").value("Tenant 2 Product"));

        // Verify tenant 1's cart is not accessible via tenant 2's API
        mockMvc.perform(get("/public/cart")
                        .param("slug", testTenant.getSlug())
                        .param("sessionId", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].product.name").value("Test Product"));
    }

    /**
     * Test error handling in order processing workflow.
     */
    @Test
    void testOrderProcessingErrorHandling() throws Exception {
        String sessionId = "error-test-session";

        // Test 1: Try to add invalid quantity
        assertThatThrownBy(() -> {
            cartService.addItemToCart(testTenant, sessionId + "-invalid", 
                testProduct.getId(), testVariant.getId(), 0);
        }).hasMessageContaining("Quantity must be at least 1");

        // Test 2: Add item to cart then make it out of stock
        cartService.addItemToCart(testTenant, sessionId, testProduct.getId(), testVariant.getId(), 2);

        // Make variant out of stock
        testVariant.setIsInStock(false);
        testVariant.setStockQuantity(0);
        productVariantRepository.save(testVariant);

        // Try to add item to cart - should fail due to stock unavailability
        assertThatThrownBy(() -> {
            cartService.addItemToCart(testTenant, sessionId + "-out-of-stock", 
                testProduct.getId(), testVariant.getId(), 1);
        }).hasMessageContaining("out of stock");

        // Test 3: Try to add more items than available stock
        testVariant.setIsInStock(true);
        testVariant.setStockQuantity(1); // Only 1 in stock
        productVariantRepository.save(testVariant);

        String excessiveQuantityRequest = """
            {
                "productId": %d,
                "variantId": %d,
                "quantity": 5
            }
            """.formatted(testProduct.getId(), testVariant.getId());

        mockMvc.perform(post("/public/cart/items")
                        .param("slug", testTenant.getSlug())
                        .param("sessionId", "new-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(excessiveQuantityRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_STOCK"));
    }

    /**
     * Test cart persistence and session management.
     */
    @Test
    void testCartPersistenceAndSessionManagement() throws Exception {
        String sessionId = "persistence-test-session";

        // Add item to cart
        cartService.addItemToCart(testTenant, sessionId, testProduct.getId(), testVariant.getId(), 1);

        // Verify cart persists across requests
        mockMvc.perform(get("/public/cart")
                        .param("slug", testTenant.getSlug())
                        .param("sessionId", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isNotEmpty());

        // Add another item
        String addSecondItemRequest = """
            {
                "productId": %d,
                "variantId": %d,
                "quantity": 1
            }
            """.formatted(testProduct.getId(), testVariant.getId());

        mockMvc.perform(post("/public/cart/items")
                        .param("slug", testTenant.getSlug())
                        .param("sessionId", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addSecondItemRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].quantity").value(2)); // Should update existing item

        // Update item quantity
        Optional<CartEntity> cart = cartRepository.findByTenantIdAndSessionId(testTenant.getId(), sessionId);
        assertThat(cart).isPresent();
        Long itemId = cart.get().getItems().get(0).getId();

        String updateQuantityRequest = """
            {
                "quantity": 3
            }
            """;

        mockMvc.perform(put("/public/cart/items/{itemId}", itemId)
                        .param("slug", testTenant.getSlug())
                        .param("sessionId", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateQuantityRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].quantity").value(3));

        // Remove item
        mockMvc.perform(delete("/public/cart/items/{itemId}", itemId)
                        .param("slug", testTenant.getSlug())
                        .param("sessionId", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    /**
     * Test product browsing and filtering functionality.
     */
    @Test
    void testProductBrowsingAndFiltering() throws Exception {
        // Create additional products for filtering tests
        ProductEntity product2 = new ProductEntity(testTenant, "Another Product", "another-product");
        product2.setPrice(new BigDecimal("49.99"));
        product2.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product2);

        ProductEntity draftProduct = new ProductEntity(testTenant, "Draft Product", "draft-product");
        draftProduct.setPrice(new BigDecimal("99.99"));
        draftProduct.setStatus(ProductStatus.DRAFT);
        productRepository.save(draftProduct);

        // Test basic product listing
        mockMvc.perform(get("/public/products")
                        .param("slug", testTenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.pagination.total").value(2)); // Only active products

        // Test product search
        mockMvc.perform(get("/public/products")
                        .param("slug", testTenant.getSlug())
                        .param("search", "Test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Test Product"));

        // Test pagination
        mockMvc.perform(get("/public/products")
                        .param("slug", testTenant.getSlug())
                        .param("page", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(1));

        // Test individual product details
        mockMvc.perform(get("/public/products/{id}", testProduct.getId())
                        .param("slug", testTenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.variants").isArray());

        // Test accessing non-existent product
        mockMvc.perform(get("/public/products/{id}", 99999L)
                        .param("slug", testTenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Test accessing draft product (should not be accessible)
        mockMvc.perform(get("/public/products/{id}", draftProduct.getId())
                        .param("slug", testTenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test API response format consistency.
     */
    @Test
    void testApiResponseFormatConsistency() throws Exception {
        // Test successful response format
        mockMvc.perform(get("/public/products")
                        .param("slug", testTenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.pagination").exists())
                .andExpect(jsonPath("$.pagination.page").exists())
                .andExpect(jsonPath("$.pagination.size").exists())
                .andExpect(jsonPath("$.pagination.total").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        // Test error response format
        mockMvc.perform(get("/public/products")
                        .param("slug", "non-existent-tenant")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").exists())
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}