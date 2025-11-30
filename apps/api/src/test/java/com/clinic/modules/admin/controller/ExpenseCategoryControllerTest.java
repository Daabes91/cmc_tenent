package com.clinic.modules.admin.controller;

import com.clinic.modules.admin.dto.ExpenseCategoryCreateRequest;
import com.clinic.modules.admin.dto.ExpenseCategoryResponse;
import com.clinic.modules.admin.dto.ExpenseCategoryUpdateRequest;
import com.clinic.modules.core.finance.ExpenseCategoryEntity;
import com.clinic.modules.core.finance.ExpenseCategoryService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExpenseCategoryController.
 * Tests all CRUD operations with tenant isolation and validation.
 */
@ExtendWith(MockitoExtension.class)
class ExpenseCategoryControllerTest {

    @Mock
    private ExpenseCategoryService categoryService;

    @Mock
    private TenantContextHolder tenantContextHolder;

    @InjectMocks
    private ExpenseCategoryController controller;

    private Long testTenantId;
    private ExpenseCategoryEntity systemCategory;
    private ExpenseCategoryEntity customCategory;

    @BeforeEach
    void setUp() {
        testTenantId = 123L;
        
        systemCategory = new ExpenseCategoryEntity("Salaries", true, true);
        systemCategory.setTenant(null); // Simplified for unit test
        setEntityId(systemCategory, 1L);
        setEntityTimestamps(systemCategory);

        customCategory = new ExpenseCategoryEntity("Office Supplies", false, true);
        customCategory.setTenant(null); // Simplified for unit test
        setEntityId(customCategory, 2L);
        setEntityTimestamps(customCategory);
    }

    // ========== GET /admin/expense-categories Tests ==========

    @Test
    void getCategories_shouldReturnAllCategories_whenActiveOnlyIsFalse() {
        // Arrange
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.getCategories(testTenantId, false))
                .thenReturn(Arrays.asList(systemCategory, customCategory));

        // Act
        ResponseEntity<List<ExpenseCategoryResponse>> response = controller.getCategories(false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // Verify first category (system)
        ExpenseCategoryResponse firstCategory = response.getBody().get(0);
        assertEquals(1L, firstCategory.id());
        assertEquals("Salaries", firstCategory.name());
        assertTrue(firstCategory.isSystem());
        assertTrue(firstCategory.isActive());

        // Verify second category (custom)
        ExpenseCategoryResponse secondCategory = response.getBody().get(1);
        assertEquals(2L, secondCategory.id());
        assertEquals("Office Supplies", secondCategory.name());
        assertFalse(secondCategory.isSystem());
        assertTrue(secondCategory.isActive());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).getCategories(testTenantId, false);
    }

    @Test
    void getCategories_shouldReturnOnlyActiveCategories_whenActiveOnlyIsTrue() {
        // Arrange
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.getCategories(testTenantId, true))
                .thenReturn(Arrays.asList(systemCategory));

        // Act
        ResponseEntity<List<ExpenseCategoryResponse>> response = controller.getCategories(true);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).isActive());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).getCategories(testTenantId, true);
    }

    @Test
    void getCategories_shouldExtractTenantIdFromContext() {
        // Arrange
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.getCategories(testTenantId, false))
                .thenReturn(Arrays.asList());

        // Act
        controller.getCategories(false);

        // Assert - verify tenant ID was extracted from context
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).getCategories(testTenantId, false);
    }

    // ========== POST /admin/expense-categories Tests ==========

    @Test
    void createCategory_shouldCreateCustomCategory_withCorrectTenantId() {
        // Arrange
        ExpenseCategoryCreateRequest request = new ExpenseCategoryCreateRequest("Marketing Expenses");
        
        ExpenseCategoryEntity createdCategory = new ExpenseCategoryEntity("Marketing Expenses", false, true);
        setEntityId(createdCategory, 3L);
        setEntityTimestamps(createdCategory);

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.createCategory(testTenantId, "Marketing Expenses"))
                .thenReturn(createdCategory);

        // Act
        ResponseEntity<ExpenseCategoryResponse> response = controller.createCategory(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().id());
        assertEquals("Marketing Expenses", response.getBody().name());
        assertFalse(response.getBody().isSystem());
        assertTrue(response.getBody().isActive());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).createCategory(testTenantId, "Marketing Expenses");
    }

    @Test
    void createCategory_shouldThrowException_whenDuplicateName() {
        // Arrange
        ExpenseCategoryCreateRequest request = new ExpenseCategoryCreateRequest("Salaries");
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.createCategory(testTenantId, "Salaries"))
                .thenThrow(new IllegalArgumentException("Category with name 'Salaries' already exists for this tenant"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCategory(request)
        );

        assertTrue(exception.getMessage().contains("already exists"));

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).createCategory(testTenantId, "Salaries");
    }

    @Test
    void createCategory_shouldExtractTenantIdFromContext() {
        // Arrange
        ExpenseCategoryCreateRequest request = new ExpenseCategoryCreateRequest("Test Category");
        
        ExpenseCategoryEntity createdCategory = new ExpenseCategoryEntity("Test Category", false, true);
        setEntityId(createdCategory, 4L);
        setEntityTimestamps(createdCategory);

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.createCategory(testTenantId, "Test Category"))
                .thenReturn(createdCategory);

        // Act
        controller.createCategory(request);

        // Assert - verify tenant ID was extracted from context, not passed in request
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).createCategory(testTenantId, "Test Category");
    }

    // ========== PUT /admin/expense-categories/{id} Tests ==========

    @Test
    void updateCategory_shouldUpdateCategoryName_whenValid() {
        // Arrange
        Long categoryId = 2L;
        ExpenseCategoryUpdateRequest request = new ExpenseCategoryUpdateRequest("Updated Supplies");
        
        ExpenseCategoryEntity updatedCategory = new ExpenseCategoryEntity("Updated Supplies", false, true);
        setEntityId(updatedCategory, categoryId);
        setEntityTimestamps(updatedCategory);

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.updateCategory(testTenantId, categoryId, "Updated Supplies"))
                .thenReturn(updatedCategory);

        // Act
        ResponseEntity<ExpenseCategoryResponse> response = controller.updateCategory(categoryId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(categoryId, response.getBody().id());
        assertEquals("Updated Supplies", response.getBody().name());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).updateCategory(testTenantId, categoryId, "Updated Supplies");
    }

    @Test
    void updateCategory_shouldValidateUniqueness() {
        // Arrange
        Long categoryId = 2L;
        ExpenseCategoryUpdateRequest request = new ExpenseCategoryUpdateRequest("Salaries");
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.updateCategory(testTenantId, categoryId, "Salaries"))
                .thenThrow(new IllegalArgumentException("Category with name 'Salaries' already exists for this tenant"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateCategory(categoryId, request)
        );

        assertTrue(exception.getMessage().contains("already exists"));

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).updateCategory(testTenantId, categoryId, "Salaries");
    }

    @Test
    void updateCategory_shouldThrowException_whenCategoryNotFound() {
        // Arrange
        Long categoryId = 999L;
        ExpenseCategoryUpdateRequest request = new ExpenseCategoryUpdateRequest("New Name");
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.updateCategory(testTenantId, categoryId, "New Name"))
                .thenThrow(new IllegalArgumentException("Category with ID 999 not found for tenant 123"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateCategory(categoryId, request)
        );

        assertTrue(exception.getMessage().contains("not found"));

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).updateCategory(testTenantId, categoryId, "New Name");
    }

    @Test
    void updateCategory_shouldEnforceTenantIsolation() {
        // Arrange
        Long categoryId = 2L;
        ExpenseCategoryUpdateRequest request = new ExpenseCategoryUpdateRequest("Updated Name");
        
        // Simulate cross-tenant access attempt
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.updateCategory(testTenantId, categoryId, "Updated Name"))
                .thenThrow(new IllegalArgumentException("Category with ID 2 not found for tenant 123"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.updateCategory(categoryId, request));

        // Verify service was called with correct tenant ID from context
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).updateCategory(testTenantId, categoryId, "Updated Name");
    }

    // ========== PATCH /admin/expense-categories/{id}/toggle Tests ==========

    @Test
    void toggleActive_shouldDisableActiveCategory() {
        // Arrange
        Long categoryId = 1L;
        
        ExpenseCategoryEntity disabledCategory = new ExpenseCategoryEntity("Salaries", true, false);
        setEntityId(disabledCategory, categoryId);
        setEntityTimestamps(disabledCategory);

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.toggleActive(testTenantId, categoryId))
                .thenReturn(disabledCategory);

        // Act
        ResponseEntity<ExpenseCategoryResponse> response = controller.toggleActive(categoryId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(categoryId, response.getBody().id());
        assertFalse(response.getBody().isActive());
        assertTrue(response.getBody().isSystem());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).toggleActive(testTenantId, categoryId);
    }

    @Test
    void toggleActive_shouldEnableDisabledCategory() {
        // Arrange
        Long categoryId = 1L;
        
        ExpenseCategoryEntity enabledCategory = new ExpenseCategoryEntity("Salaries", true, true);
        setEntityId(enabledCategory, categoryId);
        setEntityTimestamps(enabledCategory);

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.toggleActive(testTenantId, categoryId))
                .thenReturn(enabledCategory);

        // Act
        ResponseEntity<ExpenseCategoryResponse> response = controller.toggleActive(categoryId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isActive());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).toggleActive(testTenantId, categoryId);
    }

    @Test
    void toggleActive_shouldWorkForSystemCategories() {
        // Arrange
        Long categoryId = 1L;
        
        ExpenseCategoryEntity disabledSystemCategory = new ExpenseCategoryEntity("Salaries", true, false);
        setEntityId(disabledSystemCategory, categoryId);
        setEntityTimestamps(disabledSystemCategory);

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.toggleActive(testTenantId, categoryId))
                .thenReturn(disabledSystemCategory);

        // Act
        ResponseEntity<ExpenseCategoryResponse> response = controller.toggleActive(categoryId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSystem());
        assertFalse(response.getBody().isActive());

        // Verify interactions
        verify(categoryService, times(1)).toggleActive(testTenantId, categoryId);
    }

    @Test
    void toggleActive_shouldThrowException_whenCategoryNotFound() {
        // Arrange
        Long categoryId = 999L;
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.toggleActive(testTenantId, categoryId))
                .thenThrow(new IllegalArgumentException("Category with ID 999 not found for tenant 123"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.toggleActive(categoryId)
        );

        assertTrue(exception.getMessage().contains("not found"));

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).toggleActive(testTenantId, categoryId);
    }

    @Test
    void toggleActive_shouldEnforceTenantIsolation() {
        // Arrange
        Long categoryId = 2L;
        
        // Simulate cross-tenant access attempt
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(categoryService.toggleActive(testTenantId, categoryId))
                .thenThrow(new IllegalArgumentException("Category with ID 2 not found for tenant 123"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.toggleActive(categoryId));

        // Verify service was called with correct tenant ID from context
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(categoryService, times(1)).toggleActive(testTenantId, categoryId);
    }

    // ========== Helper Methods ==========

    /**
     * Helper method to set entity ID via reflection (since it's generated).
     */
    private void setEntityId(ExpenseCategoryEntity entity, Long id) {
        try {
            java.lang.reflect.Field idField = ExpenseCategoryEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID", e);
        }
    }

    /**
     * Helper method to set entity timestamps via reflection.
     */
    private void setEntityTimestamps(ExpenseCategoryEntity entity) {
        try {
            Instant now = Instant.now();
            
            java.lang.reflect.Field createdAtField = ExpenseCategoryEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entity, now);
            
            java.lang.reflect.Field updatedAtField = ExpenseCategoryEntity.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, now);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity timestamps", e);
        }
    }
}
