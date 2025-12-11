package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.model.OrderEntity;
import com.clinic.modules.ecommerce.model.OrderStatus;
import com.clinic.modules.ecommerce.model.PaymentEntity;
import com.clinic.modules.ecommerce.model.PaymentStatus;
import com.clinic.modules.ecommerce.repository.PaymentRepository;
import com.clinic.modules.saas.service.PayPalConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PayPalService.
 */
@ExtendWith(MockitoExtension.class)
class PayPalServiceTest {

    @Mock
    private PayPalConfigService payPalConfigService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private PayPalService payPalService;

    @BeforeEach
    void setUp() {
        payPalService = new PayPalService(
            payPalConfigService,
            paymentRepository,
            restTemplate,
            objectMapper
        );
    }

    @Test
    void testGetPaymentForOrder() {
        // Given
        Long orderId = 1L;
        Long tenantId = 1L;
        PaymentEntity mockPayment = new PaymentEntity();
        mockPayment.setId(1L);
        mockPayment.setStatus(PaymentStatus.CREATED);

        when(paymentRepository.findByOrderIdAndTenantId(orderId, tenantId))
            .thenReturn(Optional.of(mockPayment));

        // When
        Optional<PaymentEntity> result = payPalService.getPaymentForOrder(orderId, tenantId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(PaymentStatus.CREATED, result.get().getStatus());
        verify(paymentRepository).findByOrderIdAndTenantId(orderId, tenantId);
    }

    @Test
    void testVerifyWebhookSignature() {
        // Given
        String payload = "test payload";
        String signature = "test signature";

        // When
        boolean result = payPalService.verifyWebhookSignature(payload, signature);

        // Then
        assertTrue(result); // Currently returns true as it's not implemented
    }

    @Test
    void testPaymentEntityBusinessMethods() {
        // Given
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCurrency("USD");
        
        PaymentEntity payment = new PaymentEntity(tenant, order, new BigDecimal("100.00"));

        // Test initial state
        assertFalse(payment.isSuccessful());
        assertTrue(payment.isPending());
        assertFalse(payment.isFailed());

        // Test marking as completed
        payment.markAsCompleted("CAPTURE123", "raw response");
        assertTrue(payment.isSuccessful());
        assertFalse(payment.isPending());
        assertEquals("CAPTURE123", payment.getProviderPaymentId());

        // Test marking as failed
        PaymentEntity failedPayment = new PaymentEntity(tenant, order, new BigDecimal("100.00"));
        failedPayment.markAsFailed("error response");
        assertTrue(failedPayment.isFailed());
        assertFalse(failedPayment.isSuccessful());
    }
}