package com.clinic.modules.admin;

import com.clinic.modules.admin.controller.AdminBillingController;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.saas.dto.TenantPlanResponse;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminBillingController.
 * Tests the read-only endpoint for tenant administrators to view their subscription plan.
 */
@ExtendWith(MockitoExtension.class)
class AdminBillingControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private TenantContextHolder tenantContextHolder;

    @InjectMocks
    private AdminBillingController controller;

    private Long testTenantId;
    private TenantPlanResponse mockPlanResponse;

    @BeforeEach
    void setUp() {
        testTenantId = 123L;
        
        mockPlanResponse = new TenantPlanResponse(
                testTenantId,
                PlanTier.PROFESSIONAL,
                "Professional Plan",
                new BigDecimal("99.00"),
                "USD",
                "monthly",
                LocalDateTime.now().plusMonths(1),
                "Visa ****1234",
                "CREDIT_CARD",
                "ACTIVE",
                null,
                null,
                null,
                null,
                List.of("Unlimited doctors", "Priority support", "Custom branding"),
                "SUB-123456789"
        );
    }

    @Test
    void getCurrentPlan_shouldReturnPlanDetails_whenTenantHasSubscription() {
        // Arrange
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(subscriptionService.getPlanDetails(testTenantId)).thenReturn(mockPlanResponse);

        // Act
        ResponseEntity<TenantPlanResponse> response = controller.getCurrentPlan();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testTenantId, response.getBody().tenantId());
        assertEquals(PlanTier.PROFESSIONAL, response.getBody().planTier());
        assertEquals("Professional Plan", response.getBody().planTierName());
        assertEquals(new BigDecimal("99.00"), response.getBody().price());
        assertEquals("USD", response.getBody().currency());
        assertEquals("monthly", response.getBody().billingCycle());
        assertEquals("Visa ****1234", response.getBody().paymentMethodMask());
        assertEquals("ACTIVE", response.getBody().status());

        // Verify interactions
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(subscriptionService, times(1)).getPlanDetails(testTenantId);
    }

    @Test
    void getCurrentPlan_shouldExtractTenantIdFromContext() {
        // Arrange
        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(subscriptionService.getPlanDetails(testTenantId)).thenReturn(mockPlanResponse);

        // Act
        controller.getCurrentPlan();

        // Assert - verify tenant ID was extracted from context, not passed as parameter
        verify(tenantContextHolder, times(1)).requireTenantId();
        verify(subscriptionService, times(1)).getPlanDetails(testTenantId);
    }

    @Test
    void getCurrentPlan_shouldReturnPendingChanges_whenDowngradeScheduled() {
        // Arrange
        LocalDateTime effectiveDate = LocalDateTime.now().plusMonths(1);
        TenantPlanResponse planWithPendingChange = new TenantPlanResponse(
                testTenantId,
                PlanTier.PROFESSIONAL,
                "Professional Plan",
                new BigDecimal("99.00"),
                "USD",
                "monthly",
                LocalDateTime.now().plusMonths(1),
                "Visa ****1234",
                "CREDIT_CARD",
                "ACTIVE",
                null,
                null,
                PlanTier.BASIC,
                effectiveDate,
                List.of("Unlimited doctors", "Priority support"),
                "SUB-123456789"
        );

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(subscriptionService.getPlanDetails(testTenantId)).thenReturn(planWithPendingChange);

        // Act
        ResponseEntity<TenantPlanResponse> response = controller.getCurrentPlan();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(PlanTier.BASIC, response.getBody().pendingPlanTier());
        assertEquals(effectiveDate, response.getBody().pendingPlanEffectiveDate());
    }

    @Test
    void getCurrentPlan_shouldReturnCancellationDetails_whenSubscriptionCancelled() {
        // Arrange
        LocalDateTime cancellationDate = LocalDateTime.now();
        LocalDateTime effectiveDate = LocalDateTime.now().plusMonths(1);
        TenantPlanResponse cancelledPlan = new TenantPlanResponse(
                testTenantId,
                PlanTier.PROFESSIONAL,
                "Professional Plan",
                new BigDecimal("99.00"),
                "USD",
                "monthly",
                effectiveDate,
                "Visa ****1234",
                "CREDIT_CARD",
                "ACTIVE",
                cancellationDate,
                effectiveDate,
                null,
                null,
                List.of("Unlimited doctors", "Priority support"),
                "SUB-123456789"
        );

        when(tenantContextHolder.requireTenantId()).thenReturn(testTenantId);
        when(subscriptionService.getPlanDetails(testTenantId)).thenReturn(cancelledPlan);

        // Act
        ResponseEntity<TenantPlanResponse> response = controller.getCurrentPlan();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(cancellationDate, response.getBody().cancellationDate());
        assertEquals(effectiveDate, response.getBody().cancellationEffectiveDate());
    }
}
