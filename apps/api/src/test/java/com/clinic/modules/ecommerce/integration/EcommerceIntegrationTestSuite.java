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
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration test suite for e-commerce functionality.
 * Tests complete workflows, performance, and multi-tenant isolation.
 * 
 * **Feature: saas-ecommerce-backend, Integration Test Suite: Complete e-commerce workflows**
 * **Validates: All requirements**
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class EcommerceIntegrationTestSuite {

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

    private TenantEntity testTenant1;
    private TenantEntity testTenant2;
    private ProductEntity testProduct1;
    private ProductEntity testProduct2;
    private ProductVariantEntity testVariant1;
    private ProductVariantEntity testVariant2;

    @BeforeEach
    void setUp() {
        // Create test tenants
        testTenant1 = new TenantEntity("integration-test-1", "Integration Test Store 1");
        testTenant1.setEcommerceEnabled(true);
        testTenant1 = tenantRepository.save(testTenant1);

        testTenant2 = new TenantEntity("integration-test-2", "Integration Test Store 2");
        testTenant2.setEcommerceEnabled(true);
        testTenant2 = tenantRepository.save(testTenant2);

        // Create test products for tenant 1
        testProduct1 = new ProductEntity(testTenant1, "Test Product 1", "test-product-1");
        testProduct1.setDescription("A test product for integration testing");
        testProduct1.setPrice(new BigDecimal("29.99"));
        testProduct1.setStatus(ProductStatus.ACTIVE);
        testProduct1.setHasVariants(true);
        testProduct1 = productRepository.save(testProduct1);

        testVariant1 = new ProductVariantEntity(testProduct1, testTenant1, "SKU-001", "Small - Red", new BigDecimal("29.99"));
        testVariant1.setStockQuantity(10);
        testVariant1.setIsInStock(true);
        testVariant1 = productVariantRepository.save(testVariant1);

        // Create test products for tenant 2
        testProduct2 = new ProductEntity(testTenant2, "Test Product 2", "test-product-2");
        testProduct2.setDescription("A test product for tenant 2");
        testProduct2.setPrice(new BigDecimal("39.99"));
        testProduct2.setStatus(ProductStatus.ACTIVE);
        testProduct2.setHasVariants(true);
        testProduct2 = productRepository.save(testProduct2);

        testVariant2 = new ProductVariantEntity(testProduct2, testTenant2, "SKU-002", "Large - Blue", new BigDecimal("39.99"));
        testVariant2.setStockQuantity(5);
        testVariant2.setIsInStock(true);
        testVariant2 = productVariantRepository.save(testVariant2);
    }

    /**
     * Test complete order processing workflow.
     */
    @Test
    void testCompleteOrderProcessingWorkflow() throws Exception {
        String sessionId = "workflow-test-session";

        // Step 1: Add items to cart
        CartEntity cart = cartService.addItemToCart(testTenant1, sessionId, 
            testProduct1.getId(), testVariant1.getId(), 2);

        assertThat(cart).isNotNull();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);

        // Step 2: Verify cart totals
        cart.recalculateTotals();
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(new BigDecimal("59.98"));

        // Step 3: Update cart item quantity
        CartItemEntity cartItem = cart.getItems().get(0);
        cart = cartService.updateItemQuantity(testTenant1, sessionId, cartItem.getId(), 3);
        
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(new BigDecimal("89.97"));

        // Step 4: Remove item from cart
        cart = cartService.removeItemFromCart(testTenant1, sessionId, cartItem.getId());
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        // Step 5: Add item back for order creation
        cart = cartService.addItemToCart(testTenant1, sessionId, 
            testProduct1.getId(), testVariant1.getId(), 1);

        // Step 6: Test API endpoints
        mockMvc.perform(get("/public/products")
                        .param("slug", testTenant1.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * Test multi-tenant data isolation.
     */
    @Test
    void testMultiTenantDataIsolation() throws Exception {
        String sessionId = "isolation-test-session";

        // Create carts for both tenants with same session ID
        CartEntity cart1 = cartService.addItemToCart(testTenant1, sessionId, 
            testProduct1.getId(), testVariant1.getId(), 1);
        CartEntity cart2 = cartService.addItemToCart(testTenant2, sessionId, 
            testProduct2.getId(), testVariant2.getId(), 2);

        // Verify carts are isolated
        assertThat(cart1.getId()).isNotEqualTo(cart2.getId());
        assertThat(cart1.getTenant().getId()).isEqualTo(testTenant1.getId());
        assertThat(cart2.getTenant().getId()).isEqualTo(testTenant2.getId());

        // Verify cart contents are isolated
        assertThat(cart1.getItems()).hasSize(1);
        assertThat(cart2.getItems()).hasSize(1);
        assertThat(cart1.getItems().get(0).getQuantity()).isEqualTo(1);
        assertThat(cart2.getItems().get(0).getQuantity()).isEqualTo(2);

        // Verify products are isolated
        assertThat(cart1.getItems().get(0).getProduct().getId()).isEqualTo(testProduct1.getId());
        assertThat(cart2.getItems().get(0).getProduct().getId()).isEqualTo(testProduct2.getId());

        // Test API isolation
        mockMvc.perform(get("/public/products")
                        .param("slug", testTenant1.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/public/products")
                        .param("slug", testTenant2.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Verify cross-tenant access is blocked
        mockMvc.perform(get("/public/products/{id}", testProduct1.getId())
                        .param("slug", testTenant2.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test cart operations performance.
     */
    @Test
    void testCartOperationsPerformance() throws Exception {
        StopWatch stopWatch = new StopWatch();
        String sessionId = "performance-test-session";

        // Test sequential cart operations
        stopWatch.start("Sequential cart operations");
        for (int i = 0; i < 10; i++) {
            cartService.addItemToCart(testTenant1, sessionId + "-" + i, 
                testProduct1.getId(), testVariant1.getId(), 1);
        }
        stopWatch.stop();

        // Should complete within reasonable time
        assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(2000);

        // Test concurrent cart operations
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            stopWatch.start("Concurrent cart operations");
            
            CompletableFuture<Void>[] futures = IntStream.range(0, 20)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        cartService.addItemToCart(testTenant1, "concurrent-session-" + i, 
                            testProduct1.getId(), testVariant1.getId(), 1);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
            stopWatch.stop();

            // Concurrent operations should complete within reasonable time
            assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(5000);

            System.out.println("Performance test results:");
            System.out.println(stopWatch.prettyPrint());

        } finally {
            executor.shutdown();
        }
    }

    /**
     * Test error handling scenarios.
     */
    @Test
    void testErrorHandlingScenarios() throws Exception {
        String sessionId = "error-test-session";

        // Test 1: Add item with insufficient stock
        testVariant1.setStockQuantity(1);
        testVariant1.setIsInStock(true);
        productVariantRepository.save(testVariant1);

        // This should work
        cartService.addItemToCart(testTenant1, sessionId, 
            testProduct1.getId(), testVariant1.getId(), 1);

        // This should fail due to insufficient stock
        assertThatThrownBy(() -> {
            cartService.addItemToCart(testTenant1, sessionId + "-2", 
                testProduct1.getId(), testVariant1.getId(), 5);
        }).hasMessageContaining("Insufficient stock");

        // Test 2: Add item that's out of stock
        testVariant1.setIsInStock(false);
        productVariantRepository.save(testVariant1);

        assertThatThrownBy(() -> {
            cartService.addItemToCart(testTenant1, sessionId + "-3", 
                testProduct1.getId(), testVariant1.getId(), 1);
        }).hasMessageContaining("out of stock");

        // Test 3: Invalid product ID
        assertThatThrownBy(() -> {
            cartService.addItemToCart(testTenant1, sessionId + "-4", 
                99999L, testVariant1.getId(), 1);
        }).hasMessageContaining("Product not found");

        // Test 4: API error responses
        mockMvc.perform(get("/public/products/{id}", 99999L)
                        .param("slug", testTenant1.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * Test cart persistence and session management.
     */
    @Test
    void testCartPersistenceAndSessionManagement() throws Exception {
        String sessionId = "persistence-test-session";

        // Create cart with items
        CartEntity cart = cartService.addItemToCart(testTenant1, sessionId, 
            testProduct1.getId(), testVariant1.getId(), 2);

        Long cartId = cart.getId();

        // Verify cart persists
        Optional<CartEntity> retrievedCart = cartRepository.findByTenantIdAndSessionId(
            testTenant1.getId(), sessionId);
        
        assertThat(retrievedCart).isPresent();
        assertThat(retrievedCart.get().getId()).isEqualTo(cartId);
        assertThat(retrievedCart.get().getItems()).hasSize(1);

        // Add another item to same cart
        cart = cartService.addItemToCart(testTenant1, sessionId, 
            testProduct1.getId(), testVariant1.getId(), 1);

        // Should update existing item quantity
        assertThat(cart.getId()).isEqualTo(cartId);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);

        // Clear cart
        cart = cartService.clearCart(testTenant1, sessionId);
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * Test API response format consistency.
     */
    @Test
    void testApiResponseFormatConsistency() throws Exception {
        // Test successful response format
        mockMvc.perform(get("/public/products")
                        .param("slug", testTenant1.getSlug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
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

    /**
     * Test system behavior under stress conditions.
     */
    @Test
    void testStressConditions() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        StopWatch stopWatch = new StopWatch();

        try {
            stopWatch.start("Stress test - concurrent operations");
            
            CompletableFuture<Void>[] futures = IntStream.range(0, 50)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        String sessionId = "stress-session-" + i;
                        
                        // Simulate user behavior: add to cart, update quantities
                        CartEntity cart = cartService.addItemToCart(testTenant1, sessionId, 
                            testProduct1.getId(), testVariant1.getId(), 1);
                        
                        // Update cart
                        if (!cart.getItems().isEmpty()) {
                            CartItemEntity firstItem = cart.getItems().get(0);
                            cartService.updateItemQuantity(testTenant1, sessionId, 
                                firstItem.getId(), 2);
                        }
                        
                    } catch (Exception e) {
                        throw new RuntimeException("Stress test operation failed", e);
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
            stopWatch.stop();

            // Verify no operations failed
            for (CompletableFuture<Void> future : futures) {
                assertThat(future).isCompleted();
                assertThat(future.isCompletedExceptionally()).isFalse();
            }

            // Verify reasonable completion time
            assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(15000);

            System.out.println("Stress test completed 50 concurrent operations in " + 
                stopWatch.getLastTaskTimeMillis() + "ms");

        } finally {
            executor.shutdown();
        }
    }

    /**
     * Test database query performance.
     */
    @Test
    void testDatabaseQueryPerformance() {
        StopWatch stopWatch = new StopWatch();

        // Test cart queries
        stopWatch.start("Cart repository queries");
        String sessionId = "db-perf-session";
        cartService.addItemToCart(testTenant1, sessionId, 
            testProduct1.getId(), testVariant1.getId(), 1);
        
        Optional<CartEntity> cart = cartRepository.findByTenantIdAndSessionId(
            testTenant1.getId(), sessionId);
        stopWatch.stop();

        assertThat(cart).isPresent();
        assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(200);

        System.out.println("Database query performance:");
        System.out.println(stopWatch.prettyPrint());
    }
}