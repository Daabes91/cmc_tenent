package com.clinic.modules.saas;

import com.clinic.modules.saas.dto.PayPalConfigRequest;
import com.clinic.modules.saas.dto.PayPalConfigResponse;
import com.clinic.modules.saas.dto.PayPalPlanConfigDto;
import com.clinic.modules.saas.model.PayPalConfigEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.clinic.modules.saas.repository.PayPalConfigRepository;
import com.clinic.modules.saas.service.PayPalConfigService;
import com.clinic.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PayPalConfigService.
 */
@ExtendWith(MockitoExtension.class)
class PayPalConfigServiceTest {

    @Mock
    private PayPalConfigRepository payPalConfigRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private RestTemplate restTemplate;

    private PayPalConfigService payPalConfigService;

    private PayPalConfigEntity testConfig;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        payPalConfigService = new PayPalConfigService(payPalConfigRepository, encryptionUtil, restTemplate, objectMapper);

        testConfig = new PayPalConfigEntity();
        testConfig.setId(1L);
        testConfig.setClientId("test-client-id");
        testConfig.setClientSecretEncrypted("encrypted-secret");
        testConfig.setPlanId("test-plan-id");
        testConfig.setWebhookId("test-webhook-id");
        testConfig.setSandboxMode(true);
        testConfig.setPlanConfig("[{\"tier\":\"BASIC\",\"monthlyPlanId\":\"test-plan-id\",\"annualPlanId\":null}]");
    }

    @Test
    void testGetConfig_Success() {
        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testConfig));

        PayPalConfigResponse response = payPalConfigService.getConfig();

        assertNotNull(response);
        assertEquals(testConfig.getId(), response.getId());
        assertEquals(testConfig.getClientId(), response.getClientId());
        assertEquals(testConfig.getPlanId(), response.getPlanId());
        assertEquals(testConfig.getWebhookId(), response.getWebhookId());
        assertEquals(testConfig.getSandboxMode(), response.getSandboxMode());
        assertEquals("****", response.getMaskedClientSecret());
        assertEquals(1, response.getPlanConfigs().size());

        verify(payPalConfigRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void testGetConfig_NotFound() {
        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        PayPalConfigResponse response = payPalConfigService.getConfig();

        assertNotNull(response);
        assertEquals("", response.getClientId());
        assertTrue(response.getPlanConfigs().isEmpty());

        verify(payPalConfigRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void testUpdateConfig_Success() {
        PayPalConfigRequest request = new PayPalConfigRequest();
        request.setClientId("new-client-id");
        request.setClientSecret("new-client-secret");
        request.setPlanConfigs(List.of(new PayPalPlanConfigDto("BASIC", "new-plan-id", null)));
        request.setWebhookId("new-webhook-id");
        request.setSandboxMode(false);

        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testConfig));
        when(encryptionUtil.encrypt(anyString())).thenReturn("new-encrypted-secret");

        // Mock successful credential validation
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "test-token");
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class))).thenReturn(mockResponse);

        when(payPalConfigRepository.save(any(PayPalConfigEntity.class))).thenAnswer(invocation -> {
            PayPalConfigEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        PayPalConfigResponse response = payPalConfigService.updateConfig(request);

        assertNotNull(response);
        assertEquals(request.getClientId(), response.getClientId());
        assertEquals("new-plan-id", response.getPlanId());
        assertEquals(request.getWebhookId(), response.getWebhookId());
        assertEquals(request.getSandboxMode(), response.getSandboxMode());
        assertEquals(1, response.getPlanConfigs().size());
        assertEquals("BASIC", response.getPlanConfigs().get(0).getTier());

        verify(encryptionUtil).encrypt("new-client-secret");
        verify(payPalConfigRepository).save(any(PayPalConfigEntity.class));
    }

    @Test
    void testUpdateConfig_InvalidCredentials() {
        PayPalConfigRequest request = new PayPalConfigRequest();
        request.setClientId("invalid-client-id");
        request.setClientSecret("invalid-client-secret");
        request.setPlanConfigs(List.of(new PayPalPlanConfigDto("BASIC", "test-plan-id", null)));
        request.setSandboxMode(true);

        // Mock failed credential validation
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        assertThrows(IllegalArgumentException.class, () -> {
            payPalConfigService.updateConfig(request);
        });

        verify(payPalConfigRepository, never()).save(any());
    }

    @Test
    void testValidateCredentials_Success() {
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "test-token");
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(tokenResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class))).thenReturn(mockResponse);

        boolean result = payPalConfigService.validateCredentials("test-client-id", "test-secret", true);

        assertTrue(result);
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void testValidateCredentials_Failure() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        boolean result = payPalConfigService.validateCredentials("invalid-id", "invalid-secret", true);

        assertFalse(result);
    }

    @Test
    void testGetAccessToken_Success() {
        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testConfig));
        when(encryptionUtil.decrypt("encrypted-secret")).thenReturn("decrypted-secret");

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "test-access-token");
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(tokenResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class))).thenReturn(mockResponse);

        String accessToken = payPalConfigService.getAccessToken();

        assertEquals("test-access-token", accessToken);
        verify(encryptionUtil).decrypt("encrypted-secret");
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void testGetAccessToken_ConfigNotFound() {
        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            payPalConfigService.getAccessToken();
        });
    }

    @Test
    void testGetAccessToken_TokenRetrievalFails() {
        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testConfig));
        when(encryptionUtil.decrypt("encrypted-secret")).thenReturn("decrypted-secret");

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThrows(IllegalStateException.class, () -> {
            payPalConfigService.getAccessToken();
        });
    }

    @Test
    void testGetBaseUrl_Sandbox() {
        testConfig.setSandboxMode(true);
        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testConfig));

        String baseUrl = payPalConfigService.getBaseUrl();

        assertEquals("https://api-m.sandbox.paypal.com", baseUrl);
    }

    @Test
    void testGetBaseUrl_Production() {
        testConfig.setSandboxMode(false);
        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testConfig));

        String baseUrl = payPalConfigService.getBaseUrl();

        assertEquals("https://api-m.paypal.com", baseUrl);
    }

    @Test
    void testGetPlanId() {
        when(payPalConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testConfig));

        String planId = payPalConfigService.getPlanId();

        assertEquals("test-plan-id", planId);
    }
}
