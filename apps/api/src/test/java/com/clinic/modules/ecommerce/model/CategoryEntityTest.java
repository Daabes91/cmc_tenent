package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CategoryEntity to verify hierarchical functionality.
 */
class CategoryEntityTest {

    private TenantEntity tenant;
    private CategoryEntity rootCategory;
    private CategoryEntity childCategory;

    @BeforeEach
    void setUp() {
        tenant = new TenantEntity("test-clinic", "Test Clinic");
        setId(tenant, 1L);
        
        rootCategory = new CategoryEntity(tenant, "Electronics", "electronics");
        setId(rootCategory, 1L);
        
        childCategory = new CategoryEntity(tenant, rootCategory, "Laptops", "laptops");
        setId(childCategory, 2L);
    }

    @Test
    void testCategoryCreation() {
        assertNotNull(rootCategory);
        assertEquals("Electronics", rootCategory.getName());
        assertEquals("electronics", rootCategory.getSlug());
        assertEquals(tenant, rootCategory.getTenant());
        assertEquals(tenant.getId(), rootCategory.getTenantId());
        assertTrue(rootCategory.isActive());
        assertEquals(0, rootCategory.getSortOrder());
        assertTrue(rootCategory.isRootCategory());
    }

    @Test
    void testHierarchicalRelationships() {
        rootCategory.addChild(childCategory);
        
        assertEquals(1, rootCategory.getChildren().size());
        assertEquals(rootCategory, childCategory.getParent());
        assertEquals(tenant, childCategory.getTenant());
        assertFalse(childCategory.isRootCategory());
        assertTrue(rootCategory.hasChildren());
        
        rootCategory.removeChild(childCategory);
        assertEquals(0, rootCategory.getChildren().size());
        assertNull(childCategory.getParent());
    }

    @Test
    void testCategoryDepth() {
        assertEquals(0, rootCategory.getDepth());
        
        rootCategory.addChild(childCategory);
        assertEquals(1, childCategory.getDepth());
        
        CategoryEntity grandChild = new CategoryEntity(tenant, childCategory, "Gaming Laptops", "gaming-laptops");
        childCategory.addChild(grandChild);
        assertEquals(2, grandChild.getDepth());
    }

    @Test
    void testFullPath() {
        assertEquals("Electronics", rootCategory.getFullPath());
        
        rootCategory.addChild(childCategory);
        assertEquals("Electronics > Laptops", childCategory.getFullPath());
        
        CategoryEntity grandChild = new CategoryEntity(tenant, childCategory, "Gaming Laptops", "gaming-laptops");
        childCategory.addChild(grandChild);
        assertEquals("Electronics > Laptops > Gaming Laptops", grandChild.getFullPath());
    }

    @Test
    void testAncestorsAndDescendants() {
        rootCategory.addChild(childCategory);
        CategoryEntity grandChild = new CategoryEntity(tenant, childCategory, "Gaming Laptops", "gaming-laptops");
        childCategory.addChild(grandChild);
        
        // Test ancestors
        var ancestors = grandChild.getAncestors();
        assertEquals(2, ancestors.size());
        assertEquals(rootCategory, ancestors.get(0));
        assertEquals(childCategory, ancestors.get(1));
        
        // Test descendants
        var descendants = rootCategory.getAllDescendants();
        assertEquals(2, descendants.size());
        assertTrue(descendants.contains(childCategory));
        assertTrue(descendants.contains(grandChild));
    }

    @Test
    void testAncestorDescendantRelationships() {
        rootCategory.addChild(childCategory);
        CategoryEntity grandChild = new CategoryEntity(tenant, childCategory, "Gaming Laptops", "gaming-laptops");
        childCategory.addChild(grandChild);
        
        assertTrue(rootCategory.isAncestorOf(childCategory));
        assertTrue(rootCategory.isAncestorOf(grandChild));
        assertTrue(childCategory.isAncestorOf(grandChild));
        
        assertTrue(childCategory.isDescendantOf(rootCategory));
        assertTrue(grandChild.isDescendantOf(rootCategory));
        assertTrue(grandChild.isDescendantOf(childCategory));
        
        assertFalse(childCategory.isAncestorOf(rootCategory));
        assertFalse(rootCategory.isDescendantOf(childCategory));
    }

    @Test
    void testProductAssociation() {
        ProductEntity product = new ProductEntity(tenant, "Test Product", "test-product");
        setId(product, 1L);
        
        rootCategory.addProduct(product);
        
        assertEquals(1, rootCategory.getProductCategories().size());
        assertTrue(rootCategory.hasProducts());
        
        ProductCategoryEntity productCategory = rootCategory.getProductCategories().get(0);
        assertEquals(product, productCategory.getProduct());
        assertEquals(rootCategory, productCategory.getCategory());
        assertEquals(tenant, productCategory.getTenant());
        
        rootCategory.removeProduct(product);
        assertEquals(0, rootCategory.getProductCategories().size());
        assertFalse(rootCategory.hasProducts());
    }

    @Test
    void testCategoryActiveStatus() {
        assertTrue(rootCategory.isActive());
        
        rootCategory.setIsActive(false);
        assertFalse(rootCategory.isActive());
    }

    @Test
    void testSortOrder() {
        assertEquals(0, rootCategory.getSortOrder());
        
        rootCategory.setSortOrder(5);
        assertEquals(5, rootCategory.getSortOrder());
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