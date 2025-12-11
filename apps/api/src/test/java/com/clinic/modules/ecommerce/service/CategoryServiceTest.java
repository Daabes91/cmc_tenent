package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.CategoryEntity;
import com.clinic.modules.ecommerce.repository.CategoryRepository;
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
 * Unit tests for CategoryService.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EcommerceFeatureService ecommerceFeatureService;

    private CategoryService categoryService;

    private TenantEntity testTenant;
    private CategoryEntity testCategory;
    private CategoryEntity testParentCategory;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(
                categoryRepository,
                tenantRepository,
                ecommerceFeatureService
        );

        testTenant = new TenantEntity("test-tenant", "Test Tenant");
        testTenant.setEcommerceEnabled(true);

        testCategory = new CategoryEntity(testTenant, "Test Category", "test-category");
        testParentCategory = new CategoryEntity(testTenant, "Parent Category", "parent-category");
    }

    @Test
    void createRootCategory_Success() {
        // Arrange
        Long tenantId = 1L;
        String name = "Test Category";
        String slug = "test-category";

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(categoryRepository.existsBySlugAndTenant(slug, tenantId)).thenReturn(false);
        when(categoryRepository.findMaxSortOrderByTenantForRootCategories(tenantId)).thenReturn(Optional.of(5));
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(testCategory);

        // Act
        CategoryEntity result = categoryService.createRootCategory(tenantId, name, slug);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(slug, result.getSlug());
        verify(ecommerceFeatureService).validateEcommerceEnabled(tenantId);
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void createRootCategory_DuplicateSlug_ThrowsException() {
        // Arrange
        Long tenantId = 1L;
        String name = "Test Category";
        String slug = "test-category";

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(categoryRepository.existsBySlugAndTenant(slug, tenantId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createRootCategory(tenantId, name, slug)
        );
        assertEquals("Category slug already exists: " + slug, exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createChildCategory_Success() {
        // Arrange
        Long tenantId = 1L;
        Long parentId = 1L;
        String name = "Child Category";
        String slug = "child-category";

        CategoryEntity childCategory = new CategoryEntity(testTenant, testParentCategory, name, slug);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(categoryRepository.findByIdAndTenant(parentId, tenantId)).thenReturn(Optional.of(testParentCategory));
        when(categoryRepository.existsBySlugAndTenant(slug, tenantId)).thenReturn(false);
        when(categoryRepository.findMaxSortOrderByTenantAndParent(tenantId, parentId)).thenReturn(Optional.of(3));
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(childCategory);

        // Act
        CategoryEntity result = categoryService.createChildCategory(tenantId, parentId, name, slug);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(slug, result.getSlug());
        verify(ecommerceFeatureService, times(2)).validateEcommerceEnabled(tenantId); // Called in getCategory and createChildCategory
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void getCategory_Success() {
        // Arrange
        Long categoryId = 1L;
        Long tenantId = 1L;

        when(categoryRepository.findByIdAndTenant(categoryId, tenantId)).thenReturn(Optional.of(testCategory));

        // Act
        CategoryEntity result = categoryService.getCategory(categoryId, tenantId);

        // Assert
        assertNotNull(result);
        assertEquals(testCategory, result);
        verify(ecommerceFeatureService).validateEcommerceEnabled(tenantId);
    }

    @Test
    void getCategory_NotFound_ThrowsException() {
        // Arrange
        Long categoryId = 1L;
        Long tenantId = 1L;

        when(categoryRepository.findByIdAndTenant(categoryId, tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> categoryService.getCategory(categoryId, tenantId)
        );
        assertTrue(exception.getMessage().contains("Category with ID " + categoryId + " not found"));
    }

    @Test
    void updateCategoryStatus_Success() {
        // Arrange
        Long categoryId = 1L;
        Long tenantId = 1L;
        boolean active = false;

        when(categoryRepository.findByIdAndTenant(categoryId, tenantId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(testCategory);

        // Act
        CategoryEntity result = categoryService.updateCategoryStatus(categoryId, tenantId, active);

        // Assert
        assertNotNull(result);
        verify(ecommerceFeatureService, times(2)).validateEcommerceEnabled(tenantId); // Called in getCategory and updateCategoryStatus
        verify(categoryRepository).findByIdAndTenant(categoryId, tenantId);
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void deleteCategory_WithChildren_ThrowsException() {
        // Arrange
        Long categoryId = 1L;
        Long tenantId = 1L;

        when(categoryRepository.findByIdAndTenant(categoryId, tenantId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.hasChildrenByTenantAndParent(tenantId, categoryId)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> categoryService.deleteCategory(categoryId, tenantId)
        );
        assertEquals("Cannot delete category with child categories. Delete children first.", exception.getMessage());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_WithProducts_ThrowsException() {
        // Arrange
        Long categoryId = 1L;
        Long tenantId = 1L;

        when(categoryRepository.findByIdAndTenant(categoryId, tenantId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.hasChildrenByTenantAndParent(tenantId, categoryId)).thenReturn(false);
        when(categoryRepository.hasProductsByTenantAndCategory(tenantId, categoryId)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> categoryService.deleteCategory(categoryId, tenantId)
        );
        assertEquals("Cannot delete category with associated products. Remove products first.", exception.getMessage());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void validateCategoryName_EmptyName_ThrowsException() {
        // Arrange
        Long tenantId = 1L;
        String emptyName = "";
        String slug = "test-category";

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createRootCategory(tenantId, emptyName, slug)
        );
        assertEquals("Category name is required", exception.getMessage());
    }

    @Test
    void validateCategorySlug_InvalidSlug_ThrowsException() {
        // Arrange
        Long tenantId = 1L;
        String name = "Test Category";
        String invalidSlug = "Test Category!"; // Contains invalid characters

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createRootCategory(tenantId, name, invalidSlug)
        );
        assertEquals("Category slug must contain only lowercase letters, numbers, and hyphens", exception.getMessage());
    }
}