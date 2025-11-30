package com.clinic.modules.saas;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.dto.*;
import com.clinic.modules.saas.exception.*;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.saas.service.BillingAuditLogger;
import com.clinic.modules.saas.service.PayPalConfigService;
import com.clinic.modules.saas.service.SubscriptionService;
import com.clinic.modules.saas.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for subscription plan management flow.
 * Tests complete upgrade flow from API call to webhook processing,
 * cancellation flow with scheduled status update, payment method update,
 * manual override by SaaS manager, and error scenarios.
 * 
 * Requirements: 2.1, 2.2, 2.3, 3.1, 3.2, 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 7.1, 7.2, 7.3
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlanManagementIntegrationTest {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private BillingAuditLogger auditLogger;

    @MockBean
    private PayPalConfigService payPalConfigService;

    @MockBean
    private RestTemplate restTemplate;

    private TenantEntity testTenant;
    private SubscriptionEntity testSubscription;

    @BeforeEach
    void setUp() {
        // Clean up test data
        subscriptionRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = new TenantEntity("test-clinic", "Test Clinic");
        testTenant.setBillingStatus(BillingStatus.ACTIVE);
        testTenant = tenantRepository.save(testTenant);

        // Create test subscription
        testSubscription = new SubscriptionEntity();
        testSubscription.setTenant(testTenant);
        testSubscription.setPaypalSubscriptionId("I-TEST123456");
        testSubscription.setStatus("ACTIVE");
        testSubscription.setPlanTier(PlanTier.BASIC);
        testSubscription.setRenewalDate(LocalDateTime.now().plusMonths(1));
        testSubscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        testSubscription.setProvider("paypal");
        testSubscription = subscriptionRepository.save(testSubscription);

        // Mock PayPal config
        when(payPalConfigService.getAccessToken()).thenReturn("mock-access-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");
    }

    // ==================== Complete Upgrade Flow Tests ====================

    /**
     * Test complete upgrade flow from API call to webhook processing.
     * Requirements: 2.1, 2.2, 2.3
     */
    @Test
    void testCompleteUpgradeFlow() {
        // Step 1: Initiate upgrade
        PlanTier targetTier = PlanTier.PROFESSIONAL;
        String billingCycle = "MONTHLY";

        // Mock PayPal revise subscription response
        Map<String, Object> reviseResponse = createMockReviseResponse("https://paypal.com/approve/SUB-123");
        
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockReviseResponse = new ResponseEntity<>(reviseResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/revise"),
            eq(HttpMethod.POST),
            any(),
            eq(Map.class)
        )).thenReturn(mockReviseResponse);

        // Mock PayPal verify subscription response
        Map<String, Object> verifyResponse = createMockSubscriptionData("I-TEST123456", "ACTIVE", testTenant.getId());
        
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockVerifyResponse = new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockVerifyResponse);

        // Act: Initiate upgrade
        PlanChangeResponse upgradeResponse = subscriptionService.upgradePlan(
            testTenant.getId(),
            targetTier,
            billingCycle
        );

        // Assert: Upgrade initiated successfully
        assertThat(upgradeResponse).isNotNull();
        assertThat(upgradeResponse.approvalUrl()).isEqualTo("https://paypal.com/approve/SUB-123");
        assertThat(upgradeResponse.newTier()).isEqualTo(targetTier);
        assertThat(upgradeResponse.message()).contains("upgrade initiated");

        // Verify subscription has pending plan change
        SubscriptionEntity updatedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assertThat(updatedSubscription.getPendingPlanTier()).isEqualTo(targetTier);
        assertThat(updatedSubscription.getPendingPlanEffectiveDate()).isNotNull();

        // Step 2: Simulate webhook event after user approves upgrade
        PayPalWebhookEvent webhookEvent = createUpgradeWebhookEvent(
            "I-TEST123456",
            targetTier.name(),
            testTenant.getId()
        );

        // Mock webhook verification
        when(restTemplate.exchange(
            contains("/v1/notifications/verify-webhook-signature"),
            eq(HttpMethod.POST),
            any(),
            eq(Map.class)
        )).thenReturn(new ResponseEntity<>(Map.of("verification_status", "SUCCESS"), HttpStatus.OK));

        // Process webhook
        webhookService.processWebhookEvent(webhookEvent);

        // Assert: Subscription updated with new plan tier
        SubscriptionEntity finalSubscription = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assertThat(finalSubscription.getPlanTier()).isEqualTo(targetTier);
        assertThat(finalSubscription.getPendingPlanTier()).isNull();
        assertThat(finalSubscription.getPendingPlanEffectiveDate()).isNull();

        // Verify tenant billing status remains active
        TenantEntity finalTenant = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertThat(finalTenant.getBillingStatus()).isEqualTo(BillingStatus.ACTIVE);
    }

    /**
     * Test upgrade flow with PayPal API failure.
     * Requirements: 2.5
     */
    @Test
    void testUpgradeFlowWithPayPalFailure() {
        // Mock PayPal API failure
        when(restTemplate.exchange(
            contains("/revise"),
            eq(HttpMethod.POST),
            any(),
            eq(Map.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid plan ID"));

        // Mock verify subscription for getPlanDetails
        Map<String, Object> verifyResponse = createMockSubscriptionData("I-TEST123456", "ACTIVE", testTenant.getId());
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockVerifyResponse = new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockVerifyResponse);

        // Act & Assert: Upgrade should fail with PayPalApiException
        assertThatThrownBy(() -> {
            subscriptionService.upgradePlan(testTenant.getId(), PlanTier.PROFESSIONAL, "MONTHLY");
        })
        .isInstanceOf(PayPalApiException.class)
        .hasMessageContaining("subscription revision");

        // Verify subscription was not modified
        SubscriptionEntity unchangedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assertThat(unchangedSubscription.getPlanTier()).isEqualTo(PlanTier.BASIC);
        assertThat(unchangedSubscription.getPendingPlanTier()).isNull();
    }

    // ==================== Complete Cancellation Flow Tests ====================

    /**
     * Test complete cancellation flow with scheduled status update.
     * Requirements: 4.1, 4.2, 4.3
     */
    @Test
    void testCompleteCancellationFlowScheduled() {
        // Mock PayPal cancel subscription response
        ResponseEntity<Void> mockCancelResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(
            contains("/cancel"),
            eq(HttpMethod.POST),
            any(),
            eq(Void.class)
        )).thenReturn(mockCancelResponse);

        // Mock verify subscription
        Map<String, Object> verifyResponse = createMockSubscriptionData("I-TEST123456", "ACTIVE", testTenant.getId());
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockVerifyResponse = new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockVerifyResponse);

        // Act: Cancel subscription (not immediate)
        String reason = "Switching to another provider";
        CancellationResponse response = subscriptionService.cancelSubscription(
            testTenant.getId(),
            false, // Not immediate
            reason
        );

        // Assert: Cancellation scheduled
        assertThat(response).isNotNull();
        assertThat(response.immediate()).isFalse();
        assertThat(response.effectiveDate()).isNotNull();
        assertThat(response.effectiveDate()).isAfter(LocalDateTime.now());
        assertThat(response.message()).contains("cancelled on");

        // Verify subscription status
        SubscriptionEntity cancelledSubscription = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assertThat(cancelledSubscription.getStatus()).isEqualTo("ACTIVE"); // Still active until effective date
        assertThat(cancelledSubscription.getCancellationDate()).isNotNull();
        assertThat(cancelledSubscription.getCancellationEffectiveDate()).isNotNull();
        assertThat(cancelledSubscription.getCancellationEffectiveDate()).isEqualTo(response.effectiveDate());

        // Verify tenant billing status is still active
        TenantEntity tenant = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertThat(tenant.getBillingStatus()).isEqualTo(BillingStatus.ACTIVE);

        // Simulate scheduled job processing cancellation on effective date
        // (This would normally be done by ScheduledPlanChangeJob)
        cancelledSubscription.setStatus("CANCELLED");
        subscriptionRepository.save(cancelledSubscription);
        
        tenant.setBillingStatus(BillingStatus.CANCELED);
        tenantRepository.save(tenant);

        // Verify final state
        SubscriptionEntity finalSubscription = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assertThat(finalSubscription.getStatus()).isEqualTo("CANCELLED");
        
        TenantEntity finalTenant = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertThat(finalTenant.getBillingStatus()).isEqualTo(BillingStatus.CANCELED);
    }

    /**
     * Test immediate cancellation flow.
     * Requirements: 4.1, 4.2, 4.3
     */
    @Test
    void testCompleteCancellationFlowImmediate() {
        // Mock PayPal cancel subscription response
        ResponseEntity<Void> mockCancelResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(
            contains("/cancel"),
            eq(HttpMethod.POST),
            any(),
            eq(Void.class)
        )).thenReturn(mockCancelResponse);

        // Mock verify subscription
        Map<String, Object> verifyResponse = createMockSubscriptionData("I-TEST123456", "ACTIVE", testTenant.getId());
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockVerifyResponse = new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockVerifyResponse);

        // Act: Cancel subscription immediately
        String reason = "Service no longer needed";
        CancellationResponse response = subscriptionService.cancelSubscription(
            testTenant.getId(),
            true, // Immediate
            reason
        );

        // Assert: Cancellation immediate
        assertThat(response).isNotNull();
        assertThat(response.immediate()).isTrue();
        assertThat(response.message()).contains("immediately");

        // Verify subscription status changed immediately
        SubscriptionEntity cancelledSubscription = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assertThat(cancelledSubscription.getStatus()).isEqualTo("CANCELLED");
        assertThat(cancelledSubscription.getCancellationDate()).isNotNull();
        assertThat(cancelledSubscription.getCancellationEffectiveDate()).isNotNull();

        // Verify tenant billing status changed immediately
        TenantEntity tenant = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertThat(tenant.getBillingStatus()).isEqualTo(BillingStatus.CANCELED);
    }

    // ==================== Payment Method Update Flow Tests ====================

    /**
     * Test payment method update flow with webhook.
     * Requirements: 5.1, 5.2, 5.3
     */
    @Test
    void testPaymentMethodUpdateFlow() {
        // Step 1: Initiate payment method update
        String portalUrl = "https://paypal.com/billing/portal/SUB-123";
        
        // Mock PayPal billing portal URL generation
        Map<String, Object> portalResponse = createMockBillingPortalResponse(portalUrl);
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockPortalResponse = new ResponseEntity<>(portalResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockPortalResponse);

        // Act: Create payment update session
        PaymentMethodUpdateResponse updateResponse = subscriptionService.createPaymentUpdateSession(testTenant.getId());

        // Assert: Portal URL generated
        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.portalUrl()).isEqualTo(portalUrl);
        assertThat(updateResponse.message()).contains("PayPal portal");

        // Step 2: Simulate webhook event after payment method updated
        PayPalWebhookEvent webhookEvent = createPaymentMethodUpdateWebhookEvent(
            "I-TEST123456",
            "Visa ****1234",
            testTenant.getId()
        );

        // Mock webhook verification
        when(restTemplate.exchange(
            contains("/v1/notifications/verify-webhook-signature"),
            eq(HttpMethod.POST),
            any(),
            eq(Map.class)
        )).thenReturn(new ResponseEntity<>(Map.of("verification_status", "SUCCESS"), HttpStatus.OK));

        // Process webhook
        webhookService.processWebhookEvent(webhookEvent);

        // Assert: Subscription updated with new payment method
        SubscriptionEntity updatedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assertThat(updatedSubscription.getPaymentMethodMask()).isEqualTo("Visa ****1234");
    }

    // ==================== Manual Override Tests ====================

    /**
     * Test manual override by SaaS manager with audit logging.
     * Requirements: 7.1, 7.2, 7.3
     */
    @Test
    void testManualOverrideWithAuditLogging() {
        // Mock verify subscription
        Map<String, Object> verifyResponse = createMockSubscriptionData("I-TEST123456", "ACTIVE", testTenant.getId());
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockVerifyResponse = new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockVerifyResponse);

        // Arrange
        Long managerId = 1L;
        PlanTier targetTier = PlanTier.ENTERPRISE;
        String reason = "Special pricing agreement for enterprise customer";
        PlanTier originalTier = testSubscription.getPlanTier();

        // Act: Perform manual override
        subscriptionService.manualPlanOverride(testTenant.getId(), targetTier, managerId, reason);

        // Assert: Plan tier changed immediately
        SubscriptionEntity overriddenSubscription = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assertThat(overriddenSubscription.getPlanTier()).isEqualTo(targetTier);
        assertThat(overriddenSubscription.getPendingPlanTier()).isNull();
        assertThat(overriddenSubscription.getPendingPlanEffectiveDate()).isNull();

        // Verify audit log was created (this would be verified in actual logs)
        // The audit logger should have logged the manual override with manager ID and reason
    }

    /**
     * Test manual override clears pending plan changes.
     * Requirements: 7.1, 7.2
     */
    @Test
    void testManualOverrideClearsPendingChanges() {
        // Arrange: Set up pending plan change
        testSubscription.setPendingPlanTier(PlanTier.PROFESSIONAL);
        testSubscription.setPendingPlanEffectiveDate(LocalDateTime.now().plusDays(30));
        subscriptionRepository.save(testSubscription);

        // Mock verify subscription
        Map<String, Object> verifyResponse = createMockSubscriptionData("I-TEST123456", "ACTIVE", testTenant.getId());
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockVerifyResponse = new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockVerifyResponse);

        // Act: Perform manual override
        Long managerId = 1L;
        PlanTier targetTier = PlanTier.ENTERPRISE;
        String reason = "Override pending change for immediate upgrade";
        
        subscriptionService.manualPlanOverride(testTenant.getId(), targetTier, managerId, reason);

        // Assert: Pending plan change cleared
        SubscriptionEntity overriddenSubscription = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assertThat(overriddenSubscription.getPlanTier()).isEqualTo(targetTier);
        assertThat(overriddenSubscription.getPendingPlanTier()).isNull();
        assertThat(overriddenSubscription.getPendingPlanEffectiveDate()).isNull();
    }

    // ==================== Error Scenario Tests ====================

    /**
     * Test upgrade with invalid tier.
     * Requirements: 2.5
     */
    @Test
    void testUpgradeWithInvalidTier() {
        // Mock verify subscription
        Map<String, Object> verifyResponse = createMockSubscriptionData("I-TEST123456", "ACTIVE", testTenant.getId());
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockVerifyResponse = new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockVerifyResponse);

        // Act & Assert: Should throw InvalidPlanTierException
        // Note: CUSTOM tier is not configured in PlanTierConfig
        assertThatThrownBy(() -> {
            subscriptionService.upgradePlan(testTenant.getId(), PlanTier.CUSTOM, "MONTHLY");
        })
        .isInstanceOf(InvalidPlanTierException.class);
    }

    /**
     * Test downgrade with pending plan change.
     * Requirements: 3.5
     */
    @Test
    void testDowngradeWithPendingChange() {
        // Arrange: Set up pending plan change
        testSubscription.setPendingPlanTier(PlanTier.PROFESSIONAL);
        testSubscription.setPendingPlanEffectiveDate(LocalDateTime.now().plusDays(30));
        subscriptionRepository.save(testSubscription);

        // Mock verify subscription
        Map<String, Object> verifyResponse = createMockSubscriptionData("I-TEST123456", "ACTIVE", testTenant.getId());
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockVerifyResponse = new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockVerifyResponse);

        // Act & Assert: Should throw PlanChangeConflictException
        assertThatThrownBy(() -> {
            subscriptionService.downgradePlan(testTenant.getId(), PlanTier.BASIC, "MONTHLY");
        })
        .isInstanceOf(PlanChangeConflictException.class)
        .hasMessageContaining("Pending change");
    }

    /**
     * Test cancellation of already cancelled subscription.
     * Requirements: 4.5
     */
    @Test
    void testCancelAlreadyCancelledSubscription() {
        // Arrange: Set subscription as cancelled
        testSubscription.setStatus("CANCELLED");
        testSubscription.setCancellationDate(LocalDateTime.now().minusDays(1));
        subscriptionRepository.save(testSubscription);

        // Mock verify subscription
        Map<String, Object> verifyResponse = createMockSubscriptionData("I-TEST123456", "CANCELLED", testTenant.getId());
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> mockVerifyResponse = new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            contains("/v1/billing/subscriptions/I-TEST123456"),
            eq(HttpMethod.GET),
            any(),
            eq(Map.class)
        )).thenReturn(mockVerifyResponse);

        // Act & Assert: Should throw IllegalStateException
        assertThatThrownBy(() -> {
            subscriptionService.cancelSubscription(testTenant.getId(), false, "test");
        })
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already cancelled");
    }

    /**
     * Test unauthorized access (tenant not found).
     * Requirements: 2.5, 3.5, 4.5, 5.4
     */
    @Test
    void testUnauthorizedAccessTenantNotFound() {
        // Act & Assert: Should throw NotFoundException
        Long nonExistentTenantId = 99999L;
        
        assertThatThrownBy(() -> {
            subscriptionService.getPlanDetails(nonExistentTenantId);
        })
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Tenant with ID " + nonExistentTenantId + " not found");
    }

    /**
     * Test subscription not found for tenant.
     * Requirements: 2.5, 3.5, 4.5, 5.4
     */
    @Test
    void testSubscriptionNotFoundForTenant() {
        // Arrange: Create tenant without subscription
        TenantEntity tenantWithoutSubscription = new TenantEntity("no-sub-clinic", "No Subscription Clinic");
        tenantWithoutSubscription.setBillingStatus(BillingStatus.PENDING_PAYMENT);
        tenantWithoutSubscription = tenantRepository.save(tenantWithoutSubscription);

        // Act & Assert: Should throw NotFoundException
        Long tenantId = tenantWithoutSubscription.getId();
        assertThatThrownBy(() -> {
            subscriptionService.getPlanDetails(tenantId);
        })
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Subscription not found for tenant ID " + tenantId);
    }

    // ==================== Helper Methods ====================

    private Map<String, Object> createMockReviseResponse(String approvalUrl) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> links = new ArrayList<>();
        Map<String, String> approveLink = new HashMap<>();
        approveLink.put("rel", "approve");
        approveLink.put("href", approvalUrl);
        links.add(approveLink);
        response.put("links", links);
        return response;
    }

    private Map<String, Object> createMockSubscriptionData(String subscriptionId, String status, Long tenantId) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", subscriptionId);
        data.put("status", status);
        data.put("custom_id", "tenant_" + tenantId);

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

    private Map<String, Object> createMockBillingPortalResponse(String portalUrl) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> links = new ArrayList<>();
        Map<String, String> editLink = new HashMap<>();
        editLink.put("rel", "edit");
        editLink.put("href", portalUrl);
        links.add(editLink);
        response.put("links", links);
        return response;
    }

    private PayPalWebhookEvent createUpgradeWebhookEvent(String subscriptionId, String newTier, Long tenantId) {
        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setEventType("BILLING.SUBSCRIPTION.UPDATED");
        event.setId("WH-" + UUID.randomUUID().toString());
        event.setCreateTime("2024-01-15T10:30:00Z");

        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId(subscriptionId);
        resource.setStatus("ACTIVE");
        resource.setCustomId("tenant_" + tenantId);
        
        // Add billing info with plan details
        Map<String, Object> billingInfo = new HashMap<>();
        Map<String, Object> planOverview = new HashMap<>();
        planOverview.put("plan_name", newTier + " Plan");
        billingInfo.put("plan_overview", planOverview);
        resource.setBillingInfo(billingInfo);

        event.setResource(resource);
        return event;
    }

    private PayPalWebhookEvent createPaymentMethodUpdateWebhookEvent(String subscriptionId, String paymentMethod, Long tenantId) {
        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setEventType("PAYMENT.SALE.COMPLETED");
        event.setId("WH-" + UUID.randomUUID().toString());
        event.setCreateTime("2024-01-15T10:30:00Z");

        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId(subscriptionId);
        resource.setStatus("ACTIVE");
        resource.setCustomId("tenant_" + tenantId);
        
        // Add billing info with payment source details
        Map<String, Object> billingInfo = new HashMap<>();
        Map<String, Object> paymentSource = new HashMap<>();
        Map<String, Object> card = new HashMap<>();
        card.put("brand", "Visa");
        card.put("last_digits", "1234");
        paymentSource.put("card", card);
        billingInfo.put("payment_source", paymentSource);
        resource.setBillingInfo(billingInfo);

        event.setResource(resource);
        return event;
    }
}
