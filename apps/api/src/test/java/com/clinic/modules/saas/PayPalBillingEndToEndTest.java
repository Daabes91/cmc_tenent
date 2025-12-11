package com.clinic.modules.saas;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.dto.*;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.PaymentTransactionRepository;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.saas.service.BillingAccessControlService;
import com.clinic.modules.saas.service.SignupService;
import com.clinic.modules.saas.service.SubscriptionService;
import com.clinic.modules.saas.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * End-to-end test for the complete PayPal billing flow.
 * Tests the entire journey: signup → payment → activation → webhook processing → access control.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PayPalBillingEndToEndTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private BillingAccessControlService billingAccessControlService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SubscriptionService subscriptionService;

    private SignupRequest signupRequest;
    private String mockSubscriptionId;

    @BeforeEach
    public void setUp() {
        // Clean up test data
        paymentTransactionRepository.deleteAll();
        subscriptionRepository.deleteAll();
        staffUserRepository.deleteAll();
        tenantRepository.deleteAll();

        // Setup test data
        signupRequest = new SignupRequest(
                "E2E Test Clinic",
                "e2e-test-clinic",
                "Dr. Jane Smith",
                "jane@e2etest.com",
                "SecurePass123!",
                "+1234567890"
        );

        mockSubscriptionId = "I-E2E-TEST-123";

        // Mock PayPal subscription creation
        when(subscriptionService.createSubscription(anyLong(), any(), anyString(), any()))
                .thenReturn("https://www.paypal.com/webapps/billing/subscriptions?ba_token=BA-E2E-TEST");
    }

    @Test
    public void testCompleteSignupToActivationFlow() {
        // ===== STEP 1: Signup =====
        SignupResponse signupResponse = signupService.signup(signupRequest);

        // Verify signup succeeded
        assertThat(signupResponse).isNotNull();
        assertThat(signupResponse.isSuccess()).isTrue();
        // Note: approvalUrl removed in card-fields integration
        // assertThat(signupResponse.getApprovalUrl()).contains("paypal.com");
        assertThat(signupResponse.getTenantId()).isNotNull();

        Long tenantId = signupResponse.getTenantId();

        // Verify tenant was created with pending_payment status
        Optional<TenantEntity> tenantOpt = tenantRepository.findById(tenantId);
        assertThat(tenantOpt).isPresent();
        TenantEntity tenant = tenantOpt.get();
        assertThat(tenant.getName()).isEqualTo("E2E Test Clinic");
        assertThat(tenant.getSlug()).isEqualTo("e2e-test-clinic");
        assertThat(tenant.getBillingStatus()).isEqualTo(BillingStatus.PENDING_PAYMENT);

        // Verify owner user was created
        Optional<StaffUser> ownerOpt = staffUserRepository
                .findByEmailIgnoreCaseAndTenantId("jane@e2etest.com", tenantId);
        assertThat(ownerOpt).isPresent();
        StaffUser owner = ownerOpt.get();
        assertThat(owner.getFullName()).isEqualTo("Dr. Jane Smith");
        assertThat(owner.getRole()).isEqualTo(StaffRole.ADMIN);
        assertThat(owner.getStatus()).isEqualTo(StaffStatus.ACTIVE);

        // Verify access is denied (pending payment)
        boolean canAccess = billingAccessControlService.canAccessTenantAdmin(owner.getId(), tenantId);
        assertThat(canAccess).isFalse();

        // ===== STEP 2: Simulate PayPal Subscription Activation Webhook =====
        // Create subscription record (simulating what happens during payment confirmation)
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setTenant(tenant);
        subscription.setPaypalSubscriptionId(mockSubscriptionId);
        subscription.setStatus("APPROVAL_PENDING");
        subscription.setProvider("paypal");
        subscriptionRepository.save(subscription);

        // Send webhook event for subscription activation
        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId(mockSubscriptionId);
        resource.setStatus("ACTIVE");
        resource.setCustomId("tenant_" + tenantId);

        PayPalWebhookEvent activationEvent = new PayPalWebhookEvent();
        activationEvent.setId("WH-E2E-ACTIVATION");
        activationEvent.setEventType("BILLING.SUBSCRIPTION.ACTIVATED");
        activationEvent.setResource(resource);

        webhookService.processWebhookEvent(activationEvent);

        // Verify tenant billing status was updated to active
        tenant = tenantRepository.findById(tenantId).orElseThrow();
        assertThat(tenant.getBillingStatus()).isEqualTo(BillingStatus.ACTIVE);

        // Verify subscription status was updated
        subscription = subscriptionRepository.findByPaypalSubscriptionId(mockSubscriptionId).orElseThrow();
        assertThat(subscription.getStatus()).isEqualTo("ACTIVE");

        // Verify access is now granted
        canAccess = billingAccessControlService.canAccessTenantAdmin(owner.getId(), tenantId);
        assertThat(canAccess).isTrue();

        // ===== STEP 3: Simulate Payment Completed Webhook =====
        Map<String, Object> amountData = new HashMap<>();
        amountData.put("value", "29.99");
        amountData.put("currency_code", "USD");

        PayPalWebhookResource paymentResource = new PayPalWebhookResource();
        paymentResource.setId("PAYID-E2E-TEST-001");
        paymentResource.setAmount(amountData);

        PayPalWebhookEvent paymentEvent = new PayPalWebhookEvent();
        paymentEvent.setId("WH-E2E-PAYMENT");
        paymentEvent.setEventType("PAYMENT.SALE.COMPLETED");
        paymentEvent.setResource(paymentResource);

        webhookService.processWebhookEvent(paymentEvent);

        // Verify tenant still has active billing
        tenant = tenantRepository.findById(tenantId).orElseThrow();
        assertThat(tenant.getBillingStatus()).isEqualTo(BillingStatus.ACTIVE);

        // ===== STEP 4: Simulate Subscription Suspension (Payment Failed) =====
        PayPalWebhookResource suspendResource = new PayPalWebhookResource();
        suspendResource.setId(mockSubscriptionId);
        suspendResource.setStatus("SUSPENDED");

        PayPalWebhookEvent suspendEvent = new PayPalWebhookEvent();
        suspendEvent.setId("WH-E2E-SUSPEND");
        suspendEvent.setEventType("BILLING.SUBSCRIPTION.SUSPENDED");
        suspendEvent.setResource(suspendResource);

        webhookService.processWebhookEvent(suspendEvent);

        // Verify tenant billing status was updated to past_due
        tenant = tenantRepository.findById(tenantId).orElseThrow();
        assertThat(tenant.getBillingStatus()).isEqualTo(BillingStatus.PAST_DUE);

        // Verify access is now denied
        canAccess = billingAccessControlService.canAccessTenantAdmin(owner.getId(), tenantId);
        assertThat(canAccess).isFalse();

        // ===== STEP 5: Simulate Subscription Cancellation =====
        PayPalWebhookResource cancelResource = new PayPalWebhookResource();
        cancelResource.setId(mockSubscriptionId);
        cancelResource.setStatus("CANCELLED");

        PayPalWebhookEvent cancelEvent = new PayPalWebhookEvent();
        cancelEvent.setId("WH-E2E-CANCEL");
        cancelEvent.setEventType("BILLING.SUBSCRIPTION.CANCELLED");
        cancelEvent.setResource(cancelResource);

        webhookService.processWebhookEvent(cancelEvent);

        // Verify tenant billing status was updated to canceled
        tenant = tenantRepository.findById(tenantId).orElseThrow();
        assertThat(tenant.getBillingStatus()).isEqualTo(BillingStatus.CANCELED);

        // Verify subscription status was updated
        subscription = subscriptionRepository.findByPaypalSubscriptionId(mockSubscriptionId).orElseThrow();
        assertThat(subscription.getStatus()).isEqualTo("CANCELLED");

        // Verify access is still denied
        canAccess = billingAccessControlService.canAccessTenantAdmin(owner.getId(), tenantId);
        assertThat(canAccess).isFalse();

        // Verify tenant data is preserved (not deleted)
        assertThat(tenantRepository.findById(tenantId)).isPresent();
        assertThat(staffUserRepository.findById(owner.getId())).isPresent();
    }

    @Test
    public void testErrorScenario_SubdomainAlreadyExists() {
        // Create existing tenant
        TenantEntity existingTenant = new TenantEntity("e2e-test-clinic", "Existing Clinic");
        existingTenant.setBillingStatus(BillingStatus.ACTIVE);
        tenantRepository.save(existingTenant);

        // Attempt signup with same subdomain
        SignupResponse response = signupService.signup(signupRequest);

        // Verify error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError()).contains("already taken");
        assertThat(response.getTenantId()).isNull();
        // Note: approvalUrl removed in card-fields integration
        // assertThat(response.getApprovalUrl()).isNull();
    }

    @Test
    public void testErrorScenario_PayPalServiceUnavailable() {
        // Mock PayPal failure
        when(subscriptionService.createSubscription(anyLong(), any(), anyString(), any()))
                .thenThrow(new IllegalStateException("PayPal service unavailable"));

        // Attempt signup
        SignupResponse response = signupService.signup(signupRequest);

        // Verify error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError()).contains("temporarily unavailable");

        // Verify tenant was still created (for retry later)
        Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCase("e2e-test-clinic");
        assertThat(tenantOpt).isPresent();
        assertThat(tenantOpt.get().getBillingStatus()).isEqualTo(BillingStatus.PENDING_PAYMENT);
    }

    @Test
    public void testAccessControlScenarios() {
        // Create tenants with different billing statuses
        TenantEntity activeTenant = createTenantWithStatus("active-tenant", BillingStatus.ACTIVE);
        TenantEntity pendingTenant = createTenantWithStatus("pending-tenant", BillingStatus.PENDING_PAYMENT);
        TenantEntity pastDueTenant = createTenantWithStatus("pastdue-tenant", BillingStatus.PAST_DUE);
        TenantEntity canceledTenant = createTenantWithStatus("canceled-tenant", BillingStatus.CANCELED);

        // Test access control for each status
        assertThat(billingAccessControlService.hasActiveBilling(activeTenant.getId())).isTrue();
        assertThat(billingAccessControlService.hasActiveBilling(pendingTenant.getId())).isFalse();
        assertThat(billingAccessControlService.hasActiveBilling(pastDueTenant.getId())).isFalse();
        assertThat(billingAccessControlService.hasActiveBilling(canceledTenant.getId())).isFalse();

        // Test canAccessTenantAdmin
        assertThat(billingAccessControlService.canAccessTenantAdmin(1L, activeTenant.getId())).isTrue();
        assertThat(billingAccessControlService.canAccessTenantAdmin(1L, pendingTenant.getId())).isFalse();
        assertThat(billingAccessControlService.canAccessTenantAdmin(1L, pastDueTenant.getId())).isFalse();
        assertThat(billingAccessControlService.canAccessTenantAdmin(1L, canceledTenant.getId())).isFalse();
    }

    @Test
    public void testWebhookEventProcessing_UnknownSubscription() {
        // Send webhook for non-existent subscription
        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId("I-NONEXISTENT");
        resource.setStatus("ACTIVE");

        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setId("WH-UNKNOWN");
        event.setEventType("BILLING.SUBSCRIPTION.ACTIVATED");
        event.setResource(resource);

        // Should not throw exception
        webhookService.processWebhookEvent(event);

        // Verify no changes were made
        assertThat(subscriptionRepository.findByPaypalSubscriptionId("I-NONEXISTENT")).isEmpty();
    }

    // Helper methods

    private TenantEntity createTenantWithStatus(String slug, BillingStatus status) {
        TenantEntity tenant = new TenantEntity(slug, slug + " name");
        tenant.setBillingStatus(status);
        return tenantRepository.save(tenant);
    }
}
