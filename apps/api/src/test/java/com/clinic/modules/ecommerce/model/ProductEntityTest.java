package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductEntity to verify basic functionality and relationships.
 */
class ProductEntityTest {

    private TenantEntity tenant;
    private ProductEntity product;

    @BeforeEach
    void setUp() {
        tenant = new TenantEntity("test-clinic", "Test Clinic");
        // Simulate setting ID for tenant (normally done by JPA)
        setId(tenant, 1L);
        
        product = new ProductEntity(tenant, "Test Product", "test-product");
    }

    @Test
    void testProductCreation() {
        assertNotNull(product);
        assertEquals("Test Product", product.getName());
        assertEquals("test-product", product.getSlug());
        assertEquals(tenant, product.getTenant());
        assertEquals(tenant.getId(), product.getTenantId());
        assertEquals(ProductStatus.DRAFT, product.getStatus());
        assertEquals(ProductType.SIMPLE, product.getProductType());
        assertEquals("USD", product.getCurrency());
        assertFalse(product.hasVariants());
        assertTrue(product.isTaxable());
        assertTrue(product.isVisible());
    }

    @Test
    void testProductVariantManagement() {
        ProductVariantEntity variant = new ProductVariantEntity(
            product, tenant, "TEST-001", "Small Size", new BigDecimal("19.99")
        );
        
        product.addVariant(variant);
        
        assertEquals(1, product.getVariants().size());
        assertEquals(product, variant.getProduct());
        assertEquals(tenant, variant.getTenant());
        
        product.removeVariant(variant);
        assertEquals(0, product.getVariants().size());
        assertNull(variant.getProduct());
    }

    @Test
    void testProductImageManagement() {
        ProductImageEntity image = new ProductImageEntity(
            product, tenant, "https://example.com/image.jpg", "Test Image"
        );
        
        product.addImage(image);
        
        assertEquals(1, product.getImages().size());
        assertEquals(product, image.getProduct());
        assertEquals(tenant, image.getTenant());
        
        product.removeImage(image);
        assertEquals(0, product.getImages().size());
        assertNull(image.getProduct());
    }

    @Test
    void testProductCategoryManagement() {
        CategoryEntity category = new CategoryEntity(tenant, "Electronics", "electronics");
        setId(category, 1L);
        
        product.addCategory(category);
        
        assertEquals(1, product.getProductCategories().size());
        ProductCategoryEntity productCategory = product.getProductCategories().get(0);
        assertEquals(product, productCategory.getProduct());
        assertEquals(category, productCategory.getCategory());
        assertEquals(tenant, productCategory.getTenant());
        
        product.removeCategory(category);
        assertEquals(0, product.getProductCategories().size());
    }

    @Test
    void testProductStatusTransitions() {
        assertEquals(ProductStatus.DRAFT, product.getStatus());
        
        product.setStatus(ProductStatus.ACTIVE);
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        
        product.setStatus(ProductStatus.ARCHIVED);
        assertEquals(ProductStatus.ARCHIVED, product.getStatus());
    }

    @Test
    void testProductPricing() {
        BigDecimal price = new BigDecimal("29.99");
        BigDecimal comparePrice = new BigDecimal("39.99");
        
        product.setPrice(price);
        product.setCompareAtPrice(comparePrice);
        
        assertEquals(price, product.getPrice());
        assertEquals(comparePrice, product.getCompareAtPrice());
    }

    @Test
    void testProductVariantFlags() {
        assertFalse(product.hasVariants());
        
        product.setHasVariants(true);
        assertTrue(product.hasVariants());
        
        product.setProductType(ProductType.VARIABLE);
        assertEquals(ProductType.VARIABLE, product.getProductType());
    }

    @Test
    void testProductVisibilityAndTaxable() {
        assertTrue(product.isVisible());
        assertTrue(product.isTaxable());
        
        product.setIsVisible(false);
        product.setIsTaxable(false);
        
        assertFalse(product.isVisible());
        assertFalse(product.isTaxable());
    }

    // Helper method to simulate JPA ID setting
    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            // Ignore for test purposes
        }
    }
}