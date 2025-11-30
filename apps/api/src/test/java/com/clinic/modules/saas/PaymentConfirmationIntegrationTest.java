package com.clinic.modules.saas;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantStatus;
import com.clinic.modules.saas.dto.PaymentConfirmationResponse;
import com.clinic.modules.saas.exception.InvalidSubscriptionStatusException;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.saas.service.PayPalConfigService;
import com.clinic.modules.saas.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Integration tests for payment confirmation flow.
 * Tests successful confirmation, invalid subscription, and already activated scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class PaymentConfirmationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PayPalConfigService payPalConfigService;

    @MockBean
    private SubscriptionService subscriptionService;

    private TenantEntity testTenant;
    private StaffUser ownerUser;

    @BeforeEach
    void setUp() {
        // Clean up
        subscriptionRepository.deleteAll();
        staffUserRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = new TenantEntity("test-clinic", "Test Clinic");
        testTenant.setStatus(TenantStatus.ACTIVE);
        testTenant.setBillingStatus(BillingStatus.PENDING_PAYMENT);
        testTenant = tenantRepository.save(testTenant);

        // Create owner user
        ownerUser = new StaffUser(
                "owner@test.com",
                "Test Owner",
                StaffRole.ADMIN,
                passwordEncoder.encode("password123"),
                StaffStatus.ACTIVE
        );
        ownerUser.setTenant(testTenant);
        ownerUser = staffUserRepository.save(ownerUser);

        // Mock PayPal config
        when(payPalConfigService.getAccessToken()).thenReturn("mock-access-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api.sandbox.paypal.com");
    }

    @Test
    void testSuccessfulPaymentConfirmation() throws Exception {
        // Arrange
        String subscriptionId = "I-TEST123";
        Map<String, Object> mockSubscriptionData = createMockSubscriptionData(subscriptionId, "ACTIVE", testTenant.getId());

        when(subscriptionService.verifySubscription(subscriptionId)).thenReturn(mockSubscriptionData);

        // Act
        String url = "/api/public/payment-confirmation?subscription_id=" + subscriptionId;
        ResponseEntity<PaymentConfirmationResponse> response = restTemplate.getForEntity(
                url,
                PaymentConfirmationResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getSessionToken()).isNotNull();
        assertThat(response.getBody().getRedirectUrl()).contains("/onboarding");
        assertThat(response.getBody().getError()).isNull();
    }

    @Test
    void testPaymentConfirmationWithMissingSubscriptionId() {
        // Act
        String url = "/api/public/payment-confirmation";
        ResponseEntity<PaymentConfirmationResponse> response = restTemplate.getForEntity(
                url,
                PaymentConfirmationResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).contains("Missing subscription_id");
    }

    @Test
    void testPaymentConfirmationWithInvalidSubscriptionId() throws Exception {
        // Arrange
        String subscriptionId = "I-INVALID";
        when(subscriptionService.verifySubscription(subscriptionId))
                .thenThrow(new IllegalStateException("Subscription not found"));

        // Act
        String url = "/api/public/payment-confirmation?subscription_id=" + subscriptionId;
        ResponseEntity<PaymentConfirmationResponse> response = restTemplate.getForEntity(
                url,
                PaymentConfirmationResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isNotNull();
    }

    @Test
    void testPaymentConfirmationWithAlreadyActivatedSubscription() throws Exception {
        // Arrange
        String subscriptionId = "I-ALREADY-ACTIVE";
        
        // Create existing subscription
        SubscriptionEntity existingSubscription = new SubscriptionEntity();
        existingSubscription.setTenant(testTenant);
        existingSubscription.setPaypalSubscriptionId(subscriptionId);
        existingSubscription.setStatus("ACTIVE");
        existingSubscription.setProvider("paypal");
        subscriptionRepository.save(existingSubscription);

        // Update tenant billing status
        testTenant.setBillingStatus(BillingStatus.ACTIVE);
        tenantRepository.save(testTenant);

        Map<String, Object> mockSubscriptionData = createMockSubscriptionData(subscriptionId, "ACTIVE", testTenant.getId());
        when(subscriptionService.verifySubscription(subscriptionId)).thenReturn(mockSubscriptionData);

        // Act
        String url = "/api/public/payment-confirmation?subscription_id=" + subscriptionId;
        ResponseEntity<PaymentConfirmationResponse> response = restTemplate.getForEntity(
                url,
                PaymentConfirmationResponse.class
        );

        // Assert - Should still succeed (idempotent)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    void testPaymentConfirmationWithInactiveSubscription() throws Exception {
        // Arrange
        String subscriptionId = "I-INACTIVE";
        Map<String, Object> mockSubscriptionData = createMockSubscriptionData(subscriptionId, "SUSPENDED", testTenant.getId());

        when(subscriptionService.verifySubscription(subscriptionId)).thenReturn(mockSubscriptionData);
        
        // Use doThrow for void methods
        org.mockito.Mockito.doThrow(new InvalidSubscriptionStatusException(
                        subscriptionId,
                        "SUSPENDED",
                        "ACTIVE, APPROVAL_PENDING, or APPROVED"
                ))
                .when(subscriptionService).activateTenant(subscriptionId);

        // Act
        String url = "/api/public/payment-confirmation?subscription_id=" + subscriptionId;
        ResponseEntity<PaymentConfirmationResponse> response = restTemplate.getForEntity(
                url,
                PaymentConfirmationResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).contains("invalid status");
    }

    /**
     * Helper method to create mock PayPal subscription data.
     */
    private Map<String, Object> createMockSubscriptionData(String subscriptionId, String status, Long tenantId) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", subscriptionId);
        data.put("status", status);
        data.put("custom_id", "tenant_" + tenantId);

        Map<String, Object> billingInfo = new HashMap<>();
        billingInfo.put("next_billing_time", "2024-02-01T00:00:00Z");
        
        Map<String, Object> lastPayment = new HashMap<>();
        lastPayment.put("time", "2024-01-01T00:00:00Z");
        billingInfo.put("last_payment", lastPayment);
        
        data.put("billing_info", billingInfo);

        return data;
    }
}
