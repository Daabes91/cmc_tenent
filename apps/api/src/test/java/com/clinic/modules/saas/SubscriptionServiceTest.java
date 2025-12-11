package com.clinic.modules.saas;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.config.PlanTierConfig;
import com.clinic.modules.saas.dto.*;
import com.clinic.modules.saas.exception.*;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.monitoring.BillingAlertService;
import com.clinic.modules.saas.monitoring.BillingMetricsService;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.saas.service.BillingAuditLogger;
import com.clinic.modules.saas.service.PayPalCircuitBreaker;
import com.clinic.modules.saas.service.PayPalConfigService;
import com.clinic.modules.saas.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionService plan management methods.
 * Tests getPlanDetails, upgradePlan, downgradePlan, cancelSubscription, 
 * createPaymentUpdateSession, and plan tier validation logic.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private PayPalConfigService payPalConfigService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private BillingAuditLogger auditLogger;

    @Mock
    private BillingMetricsService metricsService;

    @Mock
    private BillingAlertService alertService;

    @Mock
    private PlanTierConfig planTierConfig;

    @Mock
    private PayPalCircuitBreaker circuitBreaker;

    @Mock
    private com.clinic.modules.admin.staff.repository.StaffUserRepository staffUserRepository;

    @Mock
    private com.clinic.modules.core.doctor.DoctorRepository doctorRepository;

    private SubscriptionService subscriptionService;

    private TenantEntity testTenant;
    private SubscriptionEntity testSubscription;

    @BeforeEach
    void setUp() throws Exception {
        subscriptionService = new SubscriptionService(
                payPalConfigService,
                restTemplate,
                subscriptionRepository,
                tenantRepository,
                auditLogger,
                metricsService,
                alertService,
                planTierConfig,
                circuitBreaker,
                staffUserRepository,
                doctorRepository
        );

        // Setup test tenant using constructor
        testTenant = new TenantEntity("test-clinic", "Test Clinic");
        testTenant.setBillingStatus(BillingStatus.ACTIVE);
        
        // Use reflection to set the ID for testing purposes
        java.lang.reflect.Field idField = TenantEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testTenant, 1L);

        // Setup test subscription
        testSubscription = new SubscriptionEntity();
        testSubscription.setTenant(testTenant);
        testSubscription.setPaypalSubscriptionId("SUB-123456");
        testSubscription.setStatus("ACTIVE");
        testSubscription.setPlanTier(PlanTier.BASIC);
        testSubscription.setRenewalDate(LocalDateTime.now().plusMonths(1));
        testSubscription.setProvider("paypal");
    }

    // ==================== getPlanDetails Tests ====================

    @Test
    void testGetPlanDetails_Success() {
        // Arrange
        Long tenantId = 1L;
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        
        // Mock PayPal verification response
        Map<String, Object> subscriptionData = createMockPayPalSubscriptionData();
        when(payPalConfigService.getAccessToken()).thenReturn("test-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");
        
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(subscriptionData, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(mockResponse);
        
        // Mock plan tier config
        when(planTierConfig.getFeatures(PlanTier.BASIC))
                .thenReturn(List.of("Feature 1", "Feature 2"));

        // Act
        TenantPlanResponse response = subscriptionService.getPlanDetails(tenantId);

        // Assert
        assertNotNull(response);
        assertEquals(tenantId, response.tenantId());
        assertEquals(PlanTier.BASIC, response.planTier());
        assertEquals("ACTIVE", response.status());
        assertNotNull(response.renewalDate());
        
        verify(tenantRepository).findById(tenantId);
        verify(subscriptionRepository).findByTenantId(tenantId);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class));
    }

    @Test
    void testGetPlanDetails_TenantNotFound() {
        // Arrange
        Long tenantId = 999L;
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            subscriptionService.getPlanDetails(tenantId);
        });

        verify(tenantRepository).findById(tenantId);
        verify(subscriptionRepository, never()).findByTenantId(anyLong());
    }

    @Test
    void testGetPlanDetails_SubscriptionNotFound() {
        // Arrange
        Long tenantId = 1L;
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            subscriptionService.getPlanDetails(tenantId);
        });

        verify(tenantRepository).findById(tenantId);
        verify(subscriptionRepository).findByTenantId(tenantId);
    }

    // ==================== upgradePlan Tests ====================

    @Test
    void testUpgradePlan_Success() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.PROFESSIONAL;
        String billingCycle = "MONTHLY";
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(true);
        when(planTierConfig.getPayPalPlanId(targetTier, billingCycle)).thenReturn("PLAN-PRO-123");
        when(planTierConfig.getPrice(targetTier, "USD", billingCycle)).thenReturn(new BigDecimal("99.00"));
        
        // Mock PayPal revise subscription response
        Map<String, Object> reviseResponse = new HashMap<>();
        List<Map<String, String>> links = new ArrayList<>();
        Map<String, String> approveLink = new HashMap<>();
        approveLink.put("rel", "approve");
        approveLink.put("href", "https://paypal.com/approve/SUB-123456");
        links.add(approveLink);
        reviseResponse.put("links", links);
        
        when(payPalConfigService.getAccessToken()).thenReturn("test-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");
        
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(reviseResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        PlanChangeResponse response = subscriptionService.upgradePlan(tenantId, targetTier, billingCycle);

        // Assert
        assertNotNull(response);
        assertEquals("https://paypal.com/approve/SUB-123456", response.approvalUrl());
        assertEquals(targetTier, response.newTier());
        assertEquals(new BigDecimal("99.00"), response.newPrice());
        assertNotNull(response.effectiveDate());
        
        verify(subscriptionRepository).save(any(SubscriptionEntity.class));
        verify(auditLogger).logPlanUpgrade(eq(tenantId), isNull(), eq("BASIC"), eq("PROFESSIONAL"), any());
    }

    @Test
    void testUpgradePlan_NullTenantId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.upgradePlan(null, PlanTier.PROFESSIONAL, "MONTHLY");
        });
    }

    @Test
    void testUpgradePlan_NullTargetTier() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.upgradePlan(1L, null, "MONTHLY");
        });
    }

    @Test
    void testUpgradePlan_InvalidTier() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.ENTERPRISE;
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidPlanTierException.class, () -> {
            subscriptionService.upgradePlan(tenantId, targetTier, "MONTHLY");
        });
    }

    @Test
    void testUpgradePlan_SubscriptionNotActive() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.PROFESSIONAL;
        
        testSubscription.setStatus("CANCELLED");
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidSubscriptionStatusException.class, () -> {
            subscriptionService.upgradePlan(tenantId, targetTier, "MONTHLY");
        });
    }

    @Test
    void testUpgradePlan_PendingChangeExists() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.PROFESSIONAL;
        
        testSubscription.setPendingPlanTier(PlanTier.ENTERPRISE);
        testSubscription.setPendingPlanEffectiveDate(LocalDateTime.now().plusDays(30));
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(true);

        // Act & Assert
        assertThrows(PlanChangeConflictException.class, () -> {
            subscriptionService.upgradePlan(tenantId, targetTier, "MONTHLY");
        });
    }

    @Test
    void testUpgradePlan_SameTier() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.BASIC; // Same as current
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.upgradePlan(tenantId, targetTier, "MONTHLY");
        });
    }

    // ==================== downgradePlan Tests ====================

    @Test
    void testDowngradePlan_Success() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.BASIC;
        String billingCycle = "MONTHLY";
        
        testSubscription.setPlanTier(PlanTier.PROFESSIONAL); // Current tier
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(true);
        when(planTierConfig.getPayPalPlanId(targetTier, billingCycle)).thenReturn("PLAN-BASIC-123");
        when(planTierConfig.getPrice(targetTier, "USD", billingCycle)).thenReturn(new BigDecimal("49.00"));

        // Act
        PlanChangeResponse response = subscriptionService.downgradePlan(tenantId, targetTier, billingCycle);

        // Assert
        assertNotNull(response);
        assertNull(response.approvalUrl()); // No approval URL for downgrades
        assertEquals(targetTier, response.newTier());
        assertEquals(new BigDecimal("49.00"), response.newPrice());
        assertNotNull(response.effectiveDate());
        assertTrue(response.message().contains("scheduled"));
        
        verify(subscriptionRepository).save(any(SubscriptionEntity.class));
        verify(auditLogger).logPlanDowngrade(eq(tenantId), isNull(), eq("PROFESSIONAL"), eq("BASIC"), any());
    }

    @Test
    void testDowngradePlan_SchedulesForNextBillingCycle() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.BASIC;
        LocalDateTime expectedEffectiveDate = LocalDateTime.now().plusMonths(1);
        
        testSubscription.setPlanTier(PlanTier.PROFESSIONAL);
        testSubscription.setRenewalDate(expectedEffectiveDate);
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(true);
        when(planTierConfig.getPayPalPlanId(targetTier, "MONTHLY")).thenReturn("PLAN-BASIC-123");
        when(planTierConfig.getPrice(targetTier, "USD", "MONTHLY")).thenReturn(new BigDecimal("49.00"));

        // Act
        PlanChangeResponse response = subscriptionService.downgradePlan(tenantId, targetTier, "MONTHLY");

        // Assert
        assertNotNull(response);
        assertEquals(expectedEffectiveDate, response.effectiveDate());
        
        // Verify subscription was updated with pending change
        verify(subscriptionRepository).save(argThat(sub -> 
            sub.getPendingPlanTier() == targetTier &&
            sub.getPendingPlanEffectiveDate().equals(expectedEffectiveDate)
        ));
    }

    // ==================== cancelSubscription Tests ====================

    @Test
    void testCancelSubscription_ImmediateSuccess() {
        // Arrange
        Long tenantId = 1L;
        boolean immediate = true;
        String reason = "No longer needed";
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        
        // Mock PayPal cancel API
        when(payPalConfigService.getAccessToken()).thenReturn("test-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");
        
        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(mockResponse);

        // Act
        CancellationResponse response = subscriptionService.cancelSubscription(tenantId, immediate, reason);

        // Assert
        assertNotNull(response);
        assertTrue(response.immediate());
        assertNotNull(response.effectiveDate());
        assertTrue(response.message().contains("immediately"));
        
        verify(subscriptionRepository).save(argThat(sub -> 
            "CANCELLED".equals(sub.getStatus()) &&
            sub.getCancellationDate() != null &&
            sub.getCancellationEffectiveDate() != null
        ));
        verify(tenantRepository).save(argThat(tenant -> 
            tenant.getBillingStatus() == BillingStatus.CANCELED
        ));
        verify(auditLogger).logCancellation(eq(tenantId), isNull(), any(), any(), eq(reason));
    }

    @Test
    void testCancelSubscription_ScheduledSuccess() {
        // Arrange
        Long tenantId = 1L;
        boolean immediate = false;
        String reason = "Switching providers";
        LocalDateTime renewalDate = LocalDateTime.now().plusMonths(1);
        
        testSubscription.setRenewalDate(renewalDate);
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        
        // Mock PayPal cancel API
        when(payPalConfigService.getAccessToken()).thenReturn("test-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");
        
        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(mockResponse);

        // Act
        CancellationResponse response = subscriptionService.cancelSubscription(tenantId, immediate, reason);

        // Assert
        assertNotNull(response);
        assertFalse(response.immediate());
        assertEquals(renewalDate, response.effectiveDate());
        assertTrue(response.message().contains("cancelled on"));
        
        verify(subscriptionRepository).save(argThat(sub -> 
            "ACTIVE".equals(sub.getStatus()) && // Remains active until effective date
            sub.getCancellationEffectiveDate().equals(renewalDate)
        ));
    }

    @Test
    void testCancelSubscription_AlreadyCancelled() {
        // Arrange
        Long tenantId = 1L;
        testSubscription.setStatus("CANCELLED");
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            subscriptionService.cancelSubscription(tenantId, false, "test");
        });
    }

    @Test
    void testCancelSubscription_CancellationAlreadyScheduled() {
        // Arrange
        Long tenantId = 1L;
        testSubscription.setCancellationEffectiveDate(LocalDateTime.now().plusDays(30));
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            subscriptionService.cancelSubscription(tenantId, false, "test");
        });
    }

    @Test
    void testCancelSubscription_CallsPayPalAPI() {
        // Arrange
        Long tenantId = 1L;
        String reason = "Test cancellation";
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        
        when(payPalConfigService.getAccessToken()).thenReturn("test-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");
        
        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(mockResponse);

        // Act
        subscriptionService.cancelSubscription(tenantId, true, reason);

        // Assert
        verify(restTemplate).exchange(
            contains("/v1/billing/subscriptions/SUB-123456/cancel"),
            eq(HttpMethod.POST),
            any(),
            eq(Void.class)
        );
    }

    // ==================== createPaymentUpdateSession Tests ====================

    @Test
    void testCreatePaymentUpdateSession_Success() {
        // Arrange
        Long tenantId = 1L;
        String expectedPortalUrl = "https://paypal.com/billing/SUB-123456";
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        
        // Mock PayPal billing portal URL generation
        when(payPalConfigService.getAccessToken()).thenReturn("test-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");
        
        Map<String, Object> portalResponse = new HashMap<>();
        List<Map<String, String>> links = new ArrayList<>();
        Map<String, String> portalLink = new HashMap<>();
        portalLink.put("rel", "edit");
        portalLink.put("href", expectedPortalUrl);
        links.add(portalLink);
        portalResponse.put("links", links);
        
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(portalResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        PaymentMethodUpdateResponse response = subscriptionService.createPaymentUpdateSession(tenantId);

        // Assert
        assertNotNull(response);
        assertEquals(expectedPortalUrl, response.portalUrl());
        assertTrue(response.message().contains("PayPal portal"));
        
        verify(tenantRepository).findById(tenantId);
        verify(subscriptionRepository).findByTenantId(tenantId);
    }

    @Test
    void testCreatePaymentUpdateSession_TenantNotFound() {
        // Arrange
        Long tenantId = 999L;
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            subscriptionService.createPaymentUpdateSession(tenantId);
        });
    }

    @Test
    void testCreatePaymentUpdateSession_SubscriptionNotFound() {
        // Arrange
        Long tenantId = 1L;
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            subscriptionService.createPaymentUpdateSession(tenantId);
        });
    }

    // ==================== Plan Tier Validation Tests ====================

    @Test
    void testPlanTierValidation_ValidTier() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.PROFESSIONAL;
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(true);
        when(planTierConfig.getPayPalPlanId(targetTier, "MONTHLY")).thenReturn("PLAN-PRO-123");
        when(planTierConfig.getPrice(targetTier, "USD", "MONTHLY")).thenReturn(new BigDecimal("99.00"));
        
        // Mock PayPal response
        Map<String, Object> reviseResponse = new HashMap<>();
        List<Map<String, String>> links = new ArrayList<>();
        Map<String, String> approveLink = new HashMap<>();
        approveLink.put("rel", "approve");
        approveLink.put("href", "https://paypal.com/approve");
        links.add(approveLink);
        reviseResponse.put("links", links);
        
        when(payPalConfigService.getAccessToken()).thenReturn("test-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");
        
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(reviseResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            subscriptionService.upgradePlan(tenantId, targetTier, "MONTHLY");
        });
        
        verify(planTierConfig).tierExists(targetTier);
    }

    @Test
    void testPlanTierValidation_InvalidTierThrowsException() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.CUSTOM;
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(false);

        // Act & Assert
        InvalidPlanTierException exception = assertThrows(InvalidPlanTierException.class, () -> {
            subscriptionService.upgradePlan(tenantId, targetTier, "MONTHLY");
        });
        
        assertEquals("CUSTOM", exception.getRequestedTier());
        verify(planTierConfig).tierExists(targetTier);
    }

    @Test
    void testPlanTierValidation_PayPalPlanIdNotConfigured() {
        // Arrange
        Long tenantId = 1L;
        PlanTier targetTier = PlanTier.ENTERPRISE;
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(testSubscription));
        when(planTierConfig.tierExists(targetTier)).thenReturn(true);
        when(planTierConfig.getPayPalPlanId(targetTier, "MONTHLY")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.upgradePlan(tenantId, targetTier, "MONTHLY");
        });
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a mock PayPal subscription data response for testing.
     */
    private Map<String, Object> createMockPayPalSubscriptionData() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", "SUB-123456");
        data.put("status", "ACTIVE");
        
        // Billing info
        Map<String, Object> billingInfo = new HashMap<>();
        billingInfo.put("next_billing_time", "2024-12-31T00:00:00Z");
        
        Map<String, Object> lastPayment = new HashMap<>();
        Map<String, Object> amount = new HashMap<>();
        amount.put("value", "49.00");
        amount.put("currency_code", "USD");
        lastPayment.put("amount", amount);
        lastPayment.put("time", "2024-11-01T00:00:00Z");
        billingInfo.put("last_payment", lastPayment);
        
        List<Map<String, Object>> cycleExecutions = new ArrayList<>();
        Map<String, Object> cycle = new HashMap<>();
        cycle.put("tenure_type", "REGULAR");
        cycleExecutions.add(cycle);
        billingInfo.put("cycle_executions", cycleExecutions);
        
        data.put("billing_info", billingInfo);
        
        // Plan overview
        Map<String, Object> planOverview = new HashMap<>();
        planOverview.put("plan_name", "Basic Plan");
        data.put("plan_overview", planOverview);
        
        // Payment source
        Map<String, Object> paymentSource = new HashMap<>();
        Map<String, Object> card = new HashMap<>();
        card.put("brand", "Visa");
        card.put("last_digits", "1234");
        paymentSource.put("card", card);
        data.put("payment_source", paymentSource);
        
        return data;
    }
}
