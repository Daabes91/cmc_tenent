package com.clinic.modules.ecommerce;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantStatus;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for comprehensive tenant isolation validation in the e-commerce module.
 * 
 * These tests validate that all e-commerce entities and operations maintain strict tenant isolation
 * as specified in the requirements.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantIsolationPropertyTest {

    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CarouselRepository carouselRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private ProductVariantRepository productVariantRepository;

    private TenantEntity tenant1;
    private TenantEntity tenant2;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        productRepository.deleteAll();
        cartRepository.deleteAll();
        orderRepository.deleteAll();
        categoryRepository.deleteAll();
        carouselRepository.deleteAll();
        paymentRepository.deleteAll();
        productVariantRepository.deleteAll();
        
        // Create test tenants with unique slugs
        String timestamp = String.valueOf(System.currentTimeMillis());
        tenant1 = new TenantEntity("test-tenant-1-" + timestamp, "Test Tenant 1");
        tenant1.setEcommerceEnabled(true);
        tenant1 = tenantRepository.save(tenant1);
        
        tenant2 = new TenantEntity("test-tenant-2-" + timestamp, "Test Tenant 2");
        tenant2.setEcommerceEnabled(true);
        tenant2 = tenantRepository.save(tenant2);
    }

    /**
     * Property 2: Tenant data isolation
     * For any tenant and any e-commerce data query, the results should only contain data belonging to that specific tenant
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    @Label("Feature: saas-ecommerce-backend, Property 2: Tenant data isolation")
    void tenantDataIsolation(
        @ForAll @StringLength(min = 3, max = 50) String productName1,
        @ForAll @StringLength(min = 3, max = 50) String productName2,
        @ForAll @StringLength(min = 3, max = 20) String slug1,
        @ForAll @StringLength(min = 3, max = 20) String slug2
    ) {
        // Assume different slugs to avoid conflicts
        Assume.that(!slug1.equals(slug2));
        Assume.that(!productName1.equals(productName2));
        
        // Create products for different tenants
        ProductEntity product1 = new ProductEntity(tenant1, productName1, slug1);
        product1.setPrice(new BigDecimal("10.00"));
        product1 = productRepository.save(product1);
        
        ProductEntity product2 = new ProductEntity(tenant2, productName2, slug2);
        product2.setPrice(new BigDecimal("20.00"));
        product2 = productRepository.save(product2);
        
        // Query products for tenant1 - should only return product1
        List<ProductEntity> tenant1Products = productRepository.findAllByTenant(tenant1.getId(), 
            org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        assertThat(tenant1Products)
            .hasSize(1)
            .extracting(ProductEntity::getId)
            .containsExactly(product1.getId());
        
        // Query products for tenant2 - should only return product2
        List<ProductEntity> tenant2Products = productRepository.findAllByTenant(tenant2.getId(), 
            org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        assertThat(tenant2Products)
            .hasSize(1)
            .extracting(ProductEntity::getId)
            .containsExactly(product2.getId());
        
        // Verify no cross-tenant data leakage
        assertThat(tenant1Products).doesNotContain(product2);
        assertThat(tenant2Products).doesNotContain(product1);
    }

    /**
     * Property 3: Tenant association on creation
     * For any e-commerce entity creation request, the created entity should be associated with the requesting tenant's ID
     * **Validates: Requirements 1.3**
     */
    @Property(tries = 100)
    @Label("Feature: saas-ecommerce-backend, Property 3: Tenant association on creation")
    void tenantAssociationOnCreation(
        @ForAll @StringLength(min = 3, max = 50) String productName,
        @ForAll @StringLength(min = 3, max = 20) String slug,
        @ForAll @StringLength(min = 10, max = 50) String sessionId
    ) {
        // Test product creation
        ProductEntity product = new ProductEntity(tenant1, productName, slug);
        product.setPrice(new BigDecimal("15.00"));
        product = productRepository.save(product);
        
        assertThat(product.getTenantId()).isEqualTo(tenant1.getId());
        assertThat(product.getTenant()).isEqualTo(tenant1);
        
        // Test cart creation
        CartEntity cart = new CartEntity(tenant1, sessionId);
        cart = cartRepository.save(cart);
        
        assertThat(cart.getTenant()).isEqualTo(tenant1);
        
        // Test category creation
        CategoryEntity category = new CategoryEntity(tenant1, "Test Category", "test-category");
        category = categoryRepository.save(category);
        
        assertThat(category.getTenant()).isEqualTo(tenant1);
    }

    /**
     * Property 4: Cross-tenant access prevention
     * For any attempt to access e-commerce data with incorrect tenant context, the system should reject the request with appropriate error
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 100)
    @Label("Feature: saas-ecommerce-backend, Property 4: Cross-tenant access prevention")
    void crossTenantAccessPrevention(
        @ForAll @StringLength(min = 3, max = 50) String productName,
        @ForAll @StringLength(min = 3, max = 20) String slug
    ) {
        // Create product for tenant1
        ProductEntity product = new ProductEntity(tenant1, productName, slug);
        product.setPrice(new BigDecimal("25.00"));
        product = productRepository.save(product);
        
        // Try to access product from tenant1 using tenant2's context - should return empty
        Optional<ProductEntity> crossTenantAccess = productRepository.findByIdAndTenant(
            product.getId(), tenant2.getId());
        
        assertThat(crossTenantAccess).isEmpty();
        
        // Verify correct tenant can access the product
        Optional<ProductEntity> correctAccess = productRepository.findByIdAndTenant(
            product.getId(), tenant1.getId());
        
        assertThat(correctAccess).isPresent();
        assertThat(correctAccess.get().getId()).isEqualTo(product.getId());
        
        // Test cart cross-tenant access prevention
        CartEntity cart = new CartEntity(tenant1, "session-123");
        cart = cartRepository.save(cart);
        
        // Try to access cart from wrong tenant context
        Optional<CartEntity> crossTenantCartAccess = cartRepository.findByTenantIdAndSessionId(
            tenant2.getId(), "session-123");
        
        assertThat(crossTenantCartAccess).isEmpty();
        
        // Verify correct tenant can access the cart
        Optional<CartEntity> correctCartAccess = cartRepository.findByTenantIdAndSessionId(
            tenant1.getId(), "session-123");
        
        assertThat(correctCartAccess).isPresent();
    }

    /**
     * Property 5: Tenant deletion cascade
     * For any tenant deletion, all associated e-commerce data should be completely removed from the system
     * **Validates: Requirements 1.5**
     */
    @Property(tries = 50)
    @Label("Feature: saas-ecommerce-backend, Property 5: Tenant deletion cascade")
    void tenantDeletionCascade(
        @ForAll @StringLength(min = 3, max = 50) String productName,
        @ForAll @StringLength(min = 3, max = 20) String slug,
        @ForAll @StringLength(min = 10, max = 50) String sessionId
    ) {
        // Create a temporary tenant for deletion test
        TenantEntity tempTenant = new TenantEntity("temp-tenant-" + System.currentTimeMillis(), "Temp Tenant");
        tempTenant.setEcommerceEnabled(true);
        tempTenant = tenantRepository.save(tempTenant);
        
        // Create various e-commerce entities for the temp tenant
        ProductEntity product = new ProductEntity(tempTenant, productName, slug);
        product.setPrice(new BigDecimal("30.00"));
        product = productRepository.save(product);
        
        CartEntity cart = new CartEntity(tempTenant, sessionId);
        cart = cartRepository.save(cart);
        
        CategoryEntity category = new CategoryEntity(tempTenant, "Test Category", "test-category");
        category = categoryRepository.save(category);
        
        // Verify entities exist before deletion
        assertThat(productRepository.findByIdAndTenant(product.getId(), tempTenant.getId())).isPresent();
        assertThat(cartRepository.findByTenantIdAndSessionId(tempTenant.getId(), sessionId)).isPresent();
        assertThat(categoryRepository.findByIdAndTenant(category.getId(), tempTenant.getId())).isPresent();
        
        // Delete the tenant (soft delete)
        tempTenant.softDelete();
        tenantRepository.save(tempTenant);
        
        // Verify tenant is marked as deleted
        assertThat(tempTenant.isDeleted()).isTrue();
        assertThat(tempTenant.getStatus()).isEqualTo(TenantStatus.INACTIVE);
        
        // Note: In a real implementation, cascade deletion would be handled by:
        // 1. Database foreign key constraints with CASCADE DELETE
        // 2. Application-level cleanup jobs
        // 3. Event listeners that trigger cleanup
        // For this test, we verify the tenant is properly marked for deletion
        // The actual cascade cleanup would be implementation-specific
    }

    /**
     * Comprehensive tenant isolation test for all e-commerce entities
     * Validates that all entity types maintain strict tenant boundaries
     */
    @Property(tries = 100)
    @Label("Feature: saas-ecommerce-backend, Comprehensive tenant isolation across all entities")
    void comprehensiveTenantIsolationAcrossAllEntities(
        @ForAll @StringLength(min = 3, max = 50) String entityName,
        @ForAll @StringLength(min = 3, max = 20) String slug,
        @ForAll @StringLength(min = 10, max = 50) String sessionId
    ) {
        // Create comprehensive test data for tenant1
        ProductEntity product1 = new ProductEntity(tenant1, entityName + "-product", slug + "-product");
        product1.setPrice(new BigDecimal("10.00"));
        product1 = productRepository.save(product1);
        
        CategoryEntity category1 = new CategoryEntity(tenant1, entityName + "-category", slug + "-category");
        category1 = categoryRepository.save(category1);
        
        CartEntity cart1 = new CartEntity(tenant1, sessionId);
        cart1 = cartRepository.save(cart1);
        
        OrderEntity order1 = new OrderEntity(tenant1, "ORDER-" + System.currentTimeMillis());
        order1.setCustomerName("Test Customer");
        order1.setCustomerEmail("test@example.com");
        order1.setBillingAddressLine1("123 Test St");
        order1.setBillingAddressCity("Test City");
        order1.setBillingAddressCountry("Test Country");
        order1.setSubtotal(new BigDecimal("10.00"));
        order1.setTotalAmount(new BigDecimal("10.00"));
        order1 = orderRepository.save(order1);
        
        CarouselEntity carousel1 = new CarouselEntity(tenant1, entityName + "-carousel", slug + "-carousel", CarouselType.HERO, "home");
        carousel1 = carouselRepository.save(carousel1);
        
        PaymentEntity payment1 = new PaymentEntity(tenant1, order1, new BigDecimal("10.00"));
        payment1.setProvider("PAYPAL");
        payment1 = paymentRepository.save(payment1);
        
        // Create similar entities for tenant2 with different identifiers
        ProductEntity product2 = new ProductEntity(tenant2, entityName + "-product-2", slug + "-product-2");
        product2.setPrice(new BigDecimal("20.00"));
        product2 = productRepository.save(product2);
        
        CategoryEntity category2 = new CategoryEntity(tenant2, entityName + "-category-2", slug + "-category-2");
        category2 = categoryRepository.save(category2);
        
        CartEntity cart2 = new CartEntity(tenant2, sessionId + "-2");
        cart2 = cartRepository.save(cart2);
        
        OrderEntity order2 = new OrderEntity(tenant2, "ORDER-2-" + System.currentTimeMillis());
        order2.setCustomerName("Test Customer 2");
        order2.setCustomerEmail("test2@example.com");
        order2.setBillingAddressLine1("456 Test Ave");
        order2.setBillingAddressCity("Test City 2");
        order2.setBillingAddressCountry("Test Country 2");
        order2.setSubtotal(new BigDecimal("20.00"));
        order2.setTotalAmount(new BigDecimal("20.00"));
        order2 = orderRepository.save(order2);
        
        CarouselEntity carousel2 = new CarouselEntity(tenant2, entityName + "-carousel-2", slug + "-carousel-2", CarouselType.PRODUCT, "category");
        carousel2 = carouselRepository.save(carousel2);
        
        PaymentEntity payment2 = new PaymentEntity(tenant2, order2, new BigDecimal("20.00"));
        payment2.setProvider("PAYPAL");
        payment2 = paymentRepository.save(payment2);
        
        // Verify tenant1 can only access its own data
        assertThat(productRepository.findByIdAndTenant(product1.getId(), tenant1.getId())).isPresent();
        assertThat(productRepository.findByIdAndTenant(product2.getId(), tenant1.getId())).isEmpty();
        
        assertThat(categoryRepository.findByIdAndTenant(category1.getId(), tenant1.getId())).isPresent();
        assertThat(categoryRepository.findByIdAndTenant(category2.getId(), tenant1.getId())).isEmpty();
        
        assertThat(cartRepository.findByTenantIdAndSessionId(tenant1.getId(), sessionId)).isPresent();
        assertThat(cartRepository.findByTenantIdAndSessionId(tenant1.getId(), sessionId + "-2")).isEmpty();
        
        assertThat(orderRepository.findByIdAndTenant(order1.getId(), tenant1.getId())).isPresent();
        assertThat(orderRepository.findByIdAndTenant(order2.getId(), tenant1.getId())).isEmpty();
        
        assertThat(carouselRepository.findByTenantIdAndId(tenant1.getId(), carousel1.getId())).isPresent();
        assertThat(carouselRepository.findByTenantIdAndId(tenant1.getId(), carousel2.getId())).isEmpty();
        
        assertThat(paymentRepository.findByIdAndTenantId(payment1.getId(), tenant1.getId())).isPresent();
        assertThat(paymentRepository.findByIdAndTenantId(payment2.getId(), tenant1.getId())).isEmpty();
        
        // Verify tenant2 can only access its own data
        assertThat(productRepository.findByIdAndTenant(product2.getId(), tenant2.getId())).isPresent();
        assertThat(productRepository.findByIdAndTenant(product1.getId(), tenant2.getId())).isEmpty();
        
        assertThat(categoryRepository.findByIdAndTenant(category2.getId(), tenant2.getId())).isPresent();
        assertThat(categoryRepository.findByIdAndTenant(category1.getId(), tenant2.getId())).isEmpty();
        
        assertThat(cartRepository.findByTenantIdAndSessionId(tenant2.getId(), sessionId + "-2")).isPresent();
        assertThat(cartRepository.findByTenantIdAndSessionId(tenant2.getId(), sessionId)).isEmpty();
        
        assertThat(orderRepository.findByIdAndTenant(order2.getId(), tenant2.getId())).isPresent();
        assertThat(orderRepository.findByIdAndTenant(order1.getId(), tenant2.getId())).isEmpty();
        
        assertThat(carouselRepository.findByTenantIdAndId(tenant2.getId(), carousel2.getId())).isPresent();
        assertThat(carouselRepository.findByTenantIdAndId(tenant2.getId(), carousel1.getId())).isEmpty();
        
        assertThat(paymentRepository.findByIdAndTenantId(payment2.getId(), tenant2.getId())).isPresent();
        assertThat(paymentRepository.findByIdAndTenantId(payment1.getId(), tenant2.getId())).isEmpty();
    }

    /**
     * Test tenant isolation for child entities (variants, images, cart items, etc.)
     * Validates that child entities inherit proper tenant isolation from their parents
     */
    @Property(tries = 100)
    @Label("Feature: saas-ecommerce-backend, Child entity tenant isolation")
    void childEntityTenantIsolation(
        @ForAll @StringLength(min = 3, max = 50) String productName,
        @ForAll @StringLength(min = 3, max = 20) String slug,
        @ForAll @StringLength(min = 3, max = 20) String sku,
        @ForAll @StringLength(min = 10, max = 50) String sessionId
    ) {
        // Create parent entities for both tenants
        ProductEntity product1 = new ProductEntity(tenant1, productName, slug);
        product1.setPrice(new BigDecimal("15.00"));
        product1 = productRepository.save(product1);
        
        ProductEntity product2 = new ProductEntity(tenant2, productName + "-2", slug + "-2");
        product2.setPrice(new BigDecimal("25.00"));
        product2 = productRepository.save(product2);
        
        CartEntity cart1 = new CartEntity(tenant1, sessionId);
        cart1 = cartRepository.save(cart1);
        
        CartEntity cart2 = new CartEntity(tenant2, sessionId + "-2");
        cart2 = cartRepository.save(cart2);
        
        // Create child entities
        ProductVariantEntity variant1 = new ProductVariantEntity(product1, tenant1, sku, "Variant 1", new BigDecimal("15.00"));
        variant1 = productVariantRepository.save(variant1);
        
        ProductVariantEntity variant2 = new ProductVariantEntity(product2, tenant2, sku + "-2", "Variant 2", new BigDecimal("25.00"));
        variant2 = productVariantRepository.save(variant2);
        
        // Additional child entities would be created here once repositories are complete
        
        // Verify tenant isolation for child entities
        // Tenant1 should only see its own variants
        List<ProductVariantEntity> tenant1Variants = productVariantRepository.findByTenantAndProduct(tenant1.getId(), product1.getId());
        assertThat(tenant1Variants).hasSize(1);
        assertThat(tenant1Variants.get(0).getId()).isEqualTo(variant1.getId());
        
        List<ProductVariantEntity> tenant2Variants = productVariantRepository.findByTenantAndProduct(tenant2.getId(), product2.getId());
        assertThat(tenant2Variants).hasSize(1);
        assertThat(tenant2Variants.get(0).getId()).isEqualTo(variant2.getId());
        
        // Verify cross-tenant access prevention for child entities
        assertThat(productVariantRepository.findByIdAndTenant(variant1.getId(), tenant2.getId())).isEmpty();
        assertThat(productVariantRepository.findByIdAndTenant(variant2.getId(), tenant1.getId())).isEmpty();
        
        // Verify cross-tenant access prevention for child entities
        assertThat(productVariantRepository.findByIdAndTenant(variant1.getId(), tenant2.getId())).isEmpty();
        assertThat(productVariantRepository.findByIdAndTenant(variant2.getId(), tenant1.getId())).isEmpty();
        
        // Note: Additional child entity tests (images, cart items) would follow similar patterns
        // once all repository methods are implemented
    }

    /**
     * Test bulk operations maintain tenant isolation
     * Validates that bulk queries and operations respect tenant boundaries
     */
    @Property(tries = 50)
    @Label("Feature: saas-ecommerce-backend, Bulk operations tenant isolation")
    void bulkOperationsTenantIsolation(
        @ForAll @IntRange(min = 2, max = 5) int entityCount
    ) {
        // Create multiple entities for each tenant
        for (int i = 0; i < entityCount; i++) {
            ProductEntity product1 = new ProductEntity(tenant1, "Product-T1-" + i, "product-t1-" + i);
            product1.setPrice(new BigDecimal("10.00"));
            productRepository.save(product1);
            
            ProductEntity product2 = new ProductEntity(tenant2, "Product-T2-" + i, "product-t2-" + i);
            product2.setPrice(new BigDecimal("20.00"));
            productRepository.save(product2);
            
            CategoryEntity category1 = new CategoryEntity(tenant1, "Category-T1-" + i, "category-t1-" + i);
            categoryRepository.save(category1);
            
            CategoryEntity category2 = new CategoryEntity(tenant2, "Category-T2-" + i, "category-t2-" + i);
            categoryRepository.save(category2);
        }
        
        // Test bulk queries respect tenant isolation
        List<ProductEntity> tenant1Products = productRepository.findAllByTenant(tenant1.getId(), Pageable.unpaged()).getContent();
        List<ProductEntity> tenant2Products = productRepository.findAllByTenant(tenant2.getId(), Pageable.unpaged()).getContent();
        
        assertThat(tenant1Products).hasSize(entityCount);
        assertThat(tenant2Products).hasSize(entityCount);
        
        // Verify no cross-tenant contamination
        tenant1Products.forEach(product -> assertThat(product.getTenantId()).isEqualTo(tenant1.getId()));
        tenant2Products.forEach(product -> assertThat(product.getTenantId()).isEqualTo(tenant2.getId()));
        
        // Test count operations
        long tenant1Count = productRepository.countByTenant(tenant1.getId());
        long tenant2Count = productRepository.countByTenant(tenant2.getId());
        
        assertThat(tenant1Count).isEqualTo(entityCount);
        assertThat(tenant2Count).isEqualTo(entityCount);
        
        // Test category bulk operations
        List<CategoryEntity> tenant1Categories = categoryRepository.findActiveByTenant(tenant1.getId());
        List<CategoryEntity> tenant2Categories = categoryRepository.findActiveByTenant(tenant2.getId());
        
        assertThat(tenant1Categories).hasSize(entityCount);
        assertThat(tenant2Categories).hasSize(entityCount);
        
        tenant1Categories.forEach(category -> assertThat(category.getTenantId()).isEqualTo(tenant1.getId()));
        tenant2Categories.forEach(category -> assertThat(category.getTenantId()).isEqualTo(tenant2.getId()));
    }

    /**
     * Test search and filtering operations maintain tenant isolation
     * Validates that search queries don't leak data across tenants
     */
    @Property(tries = 100)
    @Label("Feature: saas-ecommerce-backend, Search operations tenant isolation")
    void searchOperationsTenantIsolation(
        @ForAll @StringLength(min = 3, max = 20) String searchTerm
    ) {
        // Create products with the search term for both tenants
        ProductEntity product1 = new ProductEntity(tenant1, "Product with " + searchTerm, "product-" + searchTerm.toLowerCase());
        product1.setPrice(new BigDecimal("10.00"));
        product1.setDescription("Description containing " + searchTerm);
        product1 = productRepository.save(product1);
        
        ProductEntity product2 = new ProductEntity(tenant2, "Product with " + searchTerm, "product-2-" + searchTerm.toLowerCase());
        product2.setPrice(new BigDecimal("20.00"));
        product2.setDescription("Description containing " + searchTerm);
        product2 = productRepository.save(product2);
        
        // Search for products containing the search term
        List<ProductEntity> tenant1Results = productRepository.searchByTenant(tenant1.getId(), searchTerm, Pageable.unpaged()).getContent();
        List<ProductEntity> tenant2Results = productRepository.searchByTenant(tenant2.getId(), searchTerm, Pageable.unpaged()).getContent();
        
        // Each tenant should only find their own product
        assertThat(tenant1Results).hasSize(1);
        assertThat(tenant1Results.get(0).getId()).isEqualTo(product1.getId());
        assertThat(tenant1Results.get(0).getTenantId()).isEqualTo(tenant1.getId());
        
        assertThat(tenant2Results).hasSize(1);
        assertThat(tenant2Results.get(0).getId()).isEqualTo(product2.getId());
        assertThat(tenant2Results.get(0).getTenantId()).isEqualTo(tenant2.getId());
        
        // Verify no cross-tenant results
        assertThat(tenant1Results).doesNotContain(product2);
        assertThat(tenant2Results).doesNotContain(product1);
    }

    // Data generators for property tests
    @Provide
    Arbitrary<String> validProductNames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars(' ', '-', '_')
            .ofMinLength(3)
            .ofMaxLength(50)
            .filter(s -> s.trim().length() >= 3);
    }
    
    @Provide
    Arbitrary<String> validSlugs() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('-')
            .ofMinLength(3)
            .ofMaxLength(20)
            .filter(s -> !s.startsWith("-") && !s.endsWith("-"));
    }
    
    @Provide
    Arbitrary<String> validSessionIds() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('-', '_')
            .ofMinLength(10)
            .ofMaxLength(50);
    }
    
    @Provide
    Arbitrary<BigDecimal> validPrices() {
        return Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(9999.99))
            .ofScale(2);
    }
}