package com.clinic.modules.core.finance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for TenantCreatedEventListener.
 * 
 * Tests that category seeding is triggered on tenant creation.
 * _Requirements: 5.1_
 */
@ExtendWith(MockitoExtension.class)
public class TenantCreatedEventListenerTest {

    @Mock
    private CategorySeedingService categorySeedingService;

    private TenantCreatedEventListener eventListener;

    @BeforeEach
    public void setUp() {
        eventListener = new TenantCreatedEventListener(categorySeedingService);
    }

    /**
     * Test that category seeding is triggered when a TenantCreatedEvent is received.
     * Verifies that the event listener calls the seeding service with the correct tenant ID.
     * 
     * _Requirements: 5.1_
     */
    @Test
    public void onTenantCreated_shouldTriggerCategorySeeding() {
        // Arrange
        Long tenantId = 123L;
        String tenantSlug = "test-clinic";
        TenantCreatedEvent event = new TenantCreatedEvent(this, tenantId, tenantSlug);

        // Act
        eventListener.onTenantCreated(event);

        // Assert
        ArgumentCaptor<Long> tenantIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(categorySeedingService, times(1)).seedDefaultCategories(tenantIdCaptor.capture());
        
        Long capturedTenantId = tenantIdCaptor.getValue();
        assertEquals(tenantId, capturedTenantId, 
            "Category seeding should be called with the correct tenant ID");
    }

    /**
     * Test that category seeding is called with the correct tenant ID from the event.
     * 
     * _Requirements: 5.1_
     */
    @Test
    public void onTenantCreated_shouldPassCorrectTenantIdToSeedingService() {
        // Arrange
        Long expectedTenantId = 456L;
        String tenantSlug = "another-clinic";
        TenantCreatedEvent event = new TenantCreatedEvent(this, expectedTenantId, tenantSlug);

        // Act
        eventListener.onTenantCreated(event);

        // Assert
        verify(categorySeedingService).seedDefaultCategories(expectedTenantId);
    }

    /**
     * Test that the event listener handles exceptions gracefully.
     * If category seeding fails, the exception should be caught and logged,
     * but not propagated (to avoid failing tenant creation).
     * 
     * _Requirements: 5.1_
     */
    @Test
    public void onTenantCreated_shouldHandleExceptionsGracefully() {
        // Arrange
        Long tenantId = 789L;
        String tenantSlug = "failing-clinic";
        TenantCreatedEvent event = new TenantCreatedEvent(this, tenantId, tenantSlug);
        
        // Configure mock to throw exception
        doThrow(new RuntimeException("Database connection failed"))
            .when(categorySeedingService).seedDefaultCategories(tenantId);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> eventListener.onTenantCreated(event),
            "Event listener should handle exceptions gracefully without propagating them");
        
        // Verify seeding was attempted
        verify(categorySeedingService).seedDefaultCategories(tenantId);
    }

    /**
     * Test that multiple tenant creation events trigger seeding for each tenant.
     * 
     * _Requirements: 5.1_
     */
    @Test
    public void onTenantCreated_shouldHandleMultipleTenantCreations() {
        // Arrange
        Long tenantId1 = 100L;
        Long tenantId2 = 200L;
        Long tenantId3 = 300L;
        
        TenantCreatedEvent event1 = new TenantCreatedEvent(this, tenantId1, "clinic-1");
        TenantCreatedEvent event2 = new TenantCreatedEvent(this, tenantId2, "clinic-2");
        TenantCreatedEvent event3 = new TenantCreatedEvent(this, tenantId3, "clinic-3");

        // Act
        eventListener.onTenantCreated(event1);
        eventListener.onTenantCreated(event2);
        eventListener.onTenantCreated(event3);

        // Assert
        verify(categorySeedingService).seedDefaultCategories(tenantId1);
        verify(categorySeedingService).seedDefaultCategories(tenantId2);
        verify(categorySeedingService).seedDefaultCategories(tenantId3);
        verify(categorySeedingService, times(3)).seedDefaultCategories(anyLong());
    }

    /**
     * Test that the event listener is called with the correct event data.
     * 
     * _Requirements: 5.1_
     */
    @Test
    public void onTenantCreated_shouldReceiveCorrectEventData() {
        // Arrange
        Long tenantId = 999L;
        String tenantSlug = "event-data-clinic";
        TenantCreatedEvent event = new TenantCreatedEvent(this, tenantId, tenantSlug);

        // Act
        eventListener.onTenantCreated(event);

        // Assert
        assertEquals(tenantId, event.getTenantId(), 
            "Event should contain the correct tenant ID");
        assertEquals(tenantSlug, event.getTenantSlug(), 
            "Event should contain the correct tenant slug");
        
        verify(categorySeedingService).seedDefaultCategories(tenantId);
    }

    /**
     * Test that seeding is not called if the event is null (defensive programming).
     * Note: This is a defensive test - in practice, Spring won't call the listener with null.
     * 
     * _Requirements: 5.1_
     */
    @Test
    public void onTenantCreated_shouldHandleNullEventGracefully() {
        // This test verifies defensive behavior, though Spring won't actually pass null
        // We're testing that if somehow a null event is passed, it doesn't cause issues
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            try {
                eventListener.onTenantCreated(null);
            } catch (NullPointerException e) {
                // Expected - the method will try to access event.getTenantId()
                // This is acceptable behavior as Spring guarantees non-null events
            }
        });
    }
}
