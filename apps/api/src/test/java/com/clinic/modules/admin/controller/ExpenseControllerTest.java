package com.clinic.modules.admin.controller;

import com.clinic.modules.admin.dto.ExpenseCreateRequest;
import com.clinic.modules.admin.dto.ExpenseResponse;
import com.clinic.modules.admin.dto.ExpenseUpdateRequest;
import com.clinic.modules.core.finance.ExpenseCategoryEntity;
import com.clinic.modules.core.finance.ExpenseEntity;
import com.clinic.modules.core.finance.ExpenseService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExpenseController.
 * Tests all CRUD operations with tenant isolation and validation.
 */
@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private TenantContextHolder tenantContextHolder;

    @InjectMocks
    private ExpenseController controller;

    private Long testTenantId;
    private Long otherTenantId;
    private ExpenseCategoryEntity testCategory;
    private ExpenseEntity testExpense1;
    private ExpenseEntity testExpense2;

    @BeforeEach
    void setUp() {
        testTenantId = 123L;
        otherTenantId = 456L;
        
        // Create test category
        testCategory = new ExpenseCategoryEntity("Salaries", false, true);
        setEntityId(testCategory, 1L);
        setEntityTimestamps(testCategory);

        // Create test expenses
        testExpense1 = new ExpenseEntity(
                testCategory,
                new BigDecimal("1500.00"),
                LocalDate.of(2024, 1, 15),
                "January salary payment"
        );
        setEntityId(testExpense1, 100L);
        setEntityTimestamps(testExpense1);

        testExpense2 = new ExpenseEntity(
                testCategory,
                new BigDecimal("2500.50"),
                LocalDate.of(2024, 1, 20),
                "Bonus payment"
        );
        setEntityId(testExpense2, 101L);
        setEntityTimestamps(testExpense2);
    }

    // ========== GET /admin/expenses Tests ==========

    @Test
    void getExpenses_shouldReturnAllExpenses_whenNoFiltersProvided() {
        // Arrange
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(expenseService.getExpenses(testTenantId, null, null, null))
                .thenReturn(Arrays.asList(testExpense1, testExpense2));

        // Act
        ResponseEntity<List<ExpenseResponse>> response = controller.getExpenses(null, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // Verify first expense
        ExpenseResponse firstExpense = response.getBody().get(0);
        assertEquals(100L, firstExpense.id());
        assertEquals(1L, firstExpense.categoryId());
        assertEquals("Salaries", firstExpense.categoryName());
        assertEquals(new BigDecimal("1500.00"), firstExpense.amount());
        assertEquals(LocalDate.of(2024, 1, 15), firstExpense.expenseDate());
        assertEquals("January salary payment", firstExpense.notes());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).getExpenses(testTenantId, null, null, null);
    }

    @Test
    void getExpenses_shouldReturnFilteredExpenses_whenDateRangeProvided() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(expenseService.getExpenses(testTenantId, startDate, endDate, null))
                .thenReturn(Collections.singletonList(testExpense1));

        // Act
        ResponseEntity<List<ExpenseResponse>> response = controller.getExpenses(startDate, endDate, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(100L, response.getBody().get(0).id());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).getExpenses(testTenantId, startDate, endDate, null);
    }

    @Test
    void getExpenses_shouldReturnFilteredExpenses_whenCategoryIdProvided() {
        // Arrange
        Long categoryId = 1L;
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(expenseService.getExpenses(testTenantId, null, null, categoryId))
                .thenReturn(Arrays.asList(testExpense1, testExpense2));

        // Act
        ResponseEntity<List<ExpenseResponse>> response = controller.getExpenses(null, null, categoryId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).getExpenses(testTenantId, null, null, categoryId);
    }

    @Test
    void getExpenses_shouldReturnFilteredExpenses_whenAllFiltersProvided() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        Long categoryId = 1L;
        
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(expenseService.getExpenses(testTenantId, startDate, endDate, categoryId))
                .thenReturn(Collections.singletonList(testExpense1));

        // Act
        ResponseEntity<List<ExpenseResponse>> response = controller.getExpenses(startDate, endDate, categoryId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).getExpenses(testTenantId, startDate, endDate, categoryId);
    }

    @Test
    void getExpenses_shouldReturnEmptyList_whenNoExpensesFound() {
        // Arrange
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(expenseService.getExpenses(testTenantId, null, null, null))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ExpenseResponse>> response = controller.getExpenses(null, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).getExpenses(testTenantId, null, null, null);
    }

    // ========== POST /admin/expenses Tests ==========

    @Test
    void createExpense_shouldCreateExpense_whenValidRequest() {
        // Arrange
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                1L,
                new BigDecimal("1500.00"),
                LocalDate.of(2024, 1, 15),
                "Test expense"
        );

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(expenseService.createExpense(
                testTenantId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        )).thenReturn(testExpense1);

        // Act
        ResponseEntity<ExpenseResponse> response = controller.createExpense(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().id());
        assertEquals(new BigDecimal("1500.00"), response.getBody().amount());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).createExpense(
                testTenantId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        );
    }

    @Test
    void createExpense_shouldThrowException_whenServiceThrowsException() {
        // Arrange
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                1L,
                new BigDecimal("1500.00"),
                LocalDate.of(2024, 1, 15),
                "Test expense"
        );

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(expenseService.createExpense(
                testTenantId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        )).thenThrow(new IllegalArgumentException("Category not found"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            controller.createExpense(request);
        });

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).createExpense(
                testTenantId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        );
    }

    // ========== PUT /admin/expenses/{id} Tests ==========

    @Test
    void updateExpense_shouldUpdateExpense_whenValidRequest() {
        // Arrange
        Long expenseId = 100L;
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
                1L,
                new BigDecimal("2000.00"),
                LocalDate.of(2024, 1, 20),
                "Updated expense"
        );

        ExpenseEntity updatedExpense = new ExpenseEntity(
                testCategory,
                request.amount(),
                request.expenseDate(),
                request.notes()
        );
        setEntityId(updatedExpense, expenseId);
        setEntityTimestamps(updatedExpense);

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(expenseService.updateExpense(
                testTenantId,
                expenseId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        )).thenReturn(updatedExpense);

        // Act
        ResponseEntity<ExpenseResponse> response = controller.updateExpense(expenseId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expenseId, response.getBody().id());
        assertEquals(new BigDecimal("2000.00"), response.getBody().amount());
        assertEquals("Updated expense", response.getBody().notes());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).updateExpense(
                testTenantId,
                expenseId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        );
    }

    @Test
    void updateExpense_shouldThrowException_whenExpenseNotFound() {
        // Arrange
        Long expenseId = 999L;
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
                1L,
                new BigDecimal("2000.00"),
                LocalDate.of(2024, 1, 20),
                "Updated expense"
        );

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(expenseService.updateExpense(
                testTenantId,
                expenseId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        )).thenThrow(new IllegalArgumentException("Expense not found"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            controller.updateExpense(expenseId, request);
        });

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).updateExpense(
                testTenantId,
                expenseId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        );
    }

    @Test
    void updateExpense_shouldThrowException_whenCrossTenantUpdate() {
        // Arrange
        Long expenseId = 100L;
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
                1L,
                new BigDecimal("2000.00"),
                LocalDate.of(2024, 1, 20),
                "Updated expense"
        );

        when(tenantContextHolder.requireTenantId()).thenReturn(otherTenantId);
        when(expenseService.updateExpense(
                otherTenantId,
                expenseId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        )).thenThrow(new IllegalArgumentException("Expense not found for tenant"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            controller.updateExpense(expenseId, request);
        });

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).updateExpense(
                otherTenantId,
                expenseId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.notes()
        );
    }

    // ========== DELETE /admin/expenses/{id} Tests ==========

    @Test
    void deleteExpense_shouldDeleteExpense_whenValidRequest() {
        // Arrange
        Long expenseId = 100L;

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        doNothing().when(expenseService).deleteExpense(testTenantId, expenseId);

        // Act
        ResponseEntity<Void> response = controller.deleteExpense(expenseId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).deleteExpense(testTenantId, expenseId);
    }

    @Test
    void deleteExpense_shouldThrowException_whenExpenseNotFound() {
        // Arrange
        Long expenseId = 999L;

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        doThrow(new IllegalArgumentException("Expense not found"))
                .when(expenseService).deleteExpense(testTenantId, expenseId);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            controller.deleteExpense(expenseId);
        });

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).deleteExpense(testTenantId, expenseId);
    }

    @Test
    void deleteExpense_shouldThrowException_whenCrossTenantDeletion() {
        // Arrange
        Long expenseId = 100L;

        when(tenantContextHolder.requireTenantId()).thenReturn(otherTenantId);
        doThrow(new IllegalArgumentException("Expense not found for tenant"))
                .when(expenseService).deleteExpense(otherTenantId, expenseId);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            controller.deleteExpense(expenseId);
        });

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(expenseService, times(1)).deleteExpense(otherTenantId, expenseId);
    }

    // ========== Helper Methods ==========

    private void setEntityId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID", e);
        }
    }

    private void setEntityTimestamps(Object entity) {
        try {
            Instant now = Instant.now();
            Field createdAtField = entity.getClass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entity, now);

            Field updatedAtField = entity.getClass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, now);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity timestamps", e);
        }
    }
}
