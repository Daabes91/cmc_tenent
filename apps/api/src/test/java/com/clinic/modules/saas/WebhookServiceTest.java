package com.clinic.modules.saas;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.dto.PayPalWebhookEvent;
import com.clinic.modules.saas.dto.PayPalWebhookResource;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.PaymentTransactionRepository;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.saas.service.PayPalConfigService;
import com.clinic.modules.saas.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebhookService.
 */
@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private PayPalConfigService payPalConfigService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private TenantRepository tenantRepository;

    private WebhookService webhookService;

    private TenantEntity testTenant;
    private SubscriptionEntity testSubscription;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(
                payPalConfigService,
                restTemplate,
                subscriptionRepository,
                paymentTransactionRepository,
                tenantRepository,
                mock(com.clinic.modules.saas.service.BillingAuditLogger.class),
                mock(com.clinic.modules.saas.monitoring.BillingMetricsService.class),
                mock(com.clinic.modules.saas.monitoring.BillingAlertService.class));

        // Setup test tenant
        testTenant = new TenantEntity("test-clinic", "Test Clinic");
        testTenant.setBillingStatus(BillingStatus.PENDING_PAYMENT);

        // Setup test subscription
        testSubscription = new SubscriptionEntity();
        testSubscription.setId(1L);
        testSubscription.setTenant(testTenant);
        testSubscription.setPaypalSubscriptionId("I-TEST123");
        testSubscription.setStatus("APPROVAL_PENDING");
        testSubscription.setProvider("paypal");
    }

    @Test
    void testVerifyWebhookSignature_Success() {
        // Arrange
        String transmissionId = "test-transmission-id";
        String transmissionTime = "2024-01-01T00:00:00Z";
        String transmissionSig = "test-signature";
        String certUrl = "https://api.paypal.com/cert";
        String authAlgo = "SHA256withRSA";
        String webhookId = "test-webhook-id";
        String webhookBody = "{\"event_type\":\"TEST\"}";

        when(payPalConfigService.getAccessToken()).thenReturn("test-access-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("verification_status", "SUCCESS");

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(Map.class))).thenReturn(response);

        // Act
        boolean result = webhookService.verifyWebhookSignature(
                transmissionId,
                transmissionTime,
                transmissionSig,
                certUrl,
                authAlgo,
                webhookId,
                webhookBody);

        // Assert
        assertTrue(result);
        verify(payPalConfigService).getAccessToken();
        verify(payPalConfigService).getBaseUrl();
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void testVerifyWebhookSignature_Failure() {
        // Arrange
        String transmissionId = "test-transmission-id";
        String transmissionTime = "2024-01-01T00:00:00Z";
        String transmissionSig = "invalid-signature";
        String certUrl = "https://api.paypal.com/cert";
        String authAlgo = "SHA256withRSA";
        String webhookId = "test-webhook-id";
        String webhookBody = "{\"event_type\":\"TEST\"}";

        when(payPalConfigService.getAccessToken()).thenReturn("test-access-token");
        when(payPalConfigService.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("verification_status", "FAILURE");

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(Map.class))).thenReturn(response);

        // Act
        boolean result = webhookService.verifyWebhookSignature(
                transmissionId,
                transmissionTime,
                transmissionSig,
                certUrl,
                authAlgo,
                webhookId,
                webhookBody);

        // Assert
        assertFalse(result);
    }

    @Test
    void testProcessWebhookEvent_SubscriptionActivated() {
        // Arrange
        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId("I-TEST123");
        resource.setStatus("ACTIVE");
        resource.setCustomId("tenant_1");

        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setId("WH-TEST123");
        event.setEventType("BILLING.SUBSCRIPTION.ACTIVATED");
        event.setResource(resource);

        when(subscriptionRepository.findByPaypalSubscriptionId("I-TEST123"))
                .thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(SubscriptionEntity.class)))
                .thenReturn(testSubscription);
        when(tenantRepository.save(any(TenantEntity.class)))
                .thenReturn(testTenant);

        // Act
        webhookService.processWebhookEvent(event);

        // Assert
        verify(subscriptionRepository).findByPaypalSubscriptionId("I-TEST123");
        verify(subscriptionRepository).save(argThat(sub -> "ACTIVE".equals(sub.getStatus())));
        verify(tenantRepository).save(argThat(tenant -> BillingStatus.ACTIVE.equals(tenant.getBillingStatus())));
    }

    @Test
    void testProcessWebhookEvent_SubscriptionCancelled() {
        // Arrange
        testSubscription.setStatus("ACTIVE");
        testTenant.setBillingStatus(BillingStatus.ACTIVE);

        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId("I-TEST123");
        resource.setStatus("CANCELLED");

        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setId("WH-TEST123");
        event.setEventType("BILLING.SUBSCRIPTION.CANCELLED");
        event.setResource(resource);

        when(subscriptionRepository.findByPaypalSubscriptionId("I-TEST123"))
                .thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(SubscriptionEntity.class)))
                .thenReturn(testSubscription);
        when(tenantRepository.save(any(TenantEntity.class)))
                .thenReturn(testTenant);

        // Act
        webhookService.processWebhookEvent(event);

        // Assert
        verify(subscriptionRepository).findByPaypalSubscriptionId("I-TEST123");
        verify(subscriptionRepository).save(argThat(sub -> "CANCELLED".equals(sub.getStatus())));
        verify(tenantRepository).save(argThat(tenant -> BillingStatus.CANCELED.equals(tenant.getBillingStatus())));
    }

    @Test
    void testProcessWebhookEvent_SubscriptionSuspended() {
        // Arrange
        testSubscription.setStatus("ACTIVE");
        testTenant.setBillingStatus(BillingStatus.ACTIVE);

        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId("I-TEST123");
        resource.setStatus("SUSPENDED");

        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setId("WH-TEST123");
        event.setEventType("BILLING.SUBSCRIPTION.SUSPENDED");
        event.setResource(resource);

        when(subscriptionRepository.findByPaypalSubscriptionId("I-TEST123"))
                .thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(SubscriptionEntity.class)))
                .thenReturn(testSubscription);
        when(tenantRepository.save(any(TenantEntity.class)))
                .thenReturn(testTenant);

        // Act
        webhookService.processWebhookEvent(event);

        // Assert
        verify(subscriptionRepository).findByPaypalSubscriptionId("I-TEST123");
        verify(subscriptionRepository).save(argThat(sub -> "SUSPENDED".equals(sub.getStatus())));
        verify(tenantRepository).save(argThat(tenant -> BillingStatus.PAST_DUE.equals(tenant.getBillingStatus())));
    }

    @Test
    void testProcessWebhookEvent_PaymentCompleted() {
        // Arrange
        Map<String, Object> amountData = new HashMap<>();
        amountData.put("value", "29.99");
        amountData.put("currency_code", "USD");

        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId("PAYID-TEST123");
        resource.setAmount(amountData);

        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setId("WH-TEST123");
        event.setEventType("PAYMENT.SALE.COMPLETED");
        event.setResource(resource);

        when(paymentTransactionRepository.findByPaypalTransactionId("PAYID-TEST123"))
                .thenReturn(Optional.empty());

        // Act
        webhookService.processWebhookEvent(event);

        // Assert
        verify(paymentTransactionRepository).findByPaypalTransactionId("PAYID-TEST123");
        // Note: Full payment recording requires subscription linking which is not
        // implemented yet
    }

    @Test
    void testProcessWebhookEvent_UnhandledEventType() {
        // Arrange
        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId("I-TEST123");

        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setId("WH-TEST123");
        event.setEventType("UNKNOWN.EVENT.TYPE");
        event.setResource(resource);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> webhookService.processWebhookEvent(event));
    }

    @Test
    void testProcessWebhookEvent_SubscriptionNotFound() {
        // Arrange
        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId("I-NONEXISTENT");
        resource.setStatus("ACTIVE");

        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setId("WH-TEST123");
        event.setEventType("BILLING.SUBSCRIPTION.ACTIVATED");
        event.setResource(resource);

        when(subscriptionRepository.findByPaypalSubscriptionId("I-NONEXISTENT"))
                .thenReturn(Optional.empty());

        // Act & Assert - should not throw exception, just log warning
        assertDoesNotThrow(() -> webhookService.processWebhookEvent(event));
        verify(subscriptionRepository).findByPaypalSubscriptionId("I-NONEXISTENT");
        verify(subscriptionRepository, never()).save(any());
        verify(tenantRepository, never()).save(any());
    }
}
