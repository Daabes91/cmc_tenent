package com.clinic.modules.core.payment;

import com.clinic.config.PayPalConfig;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

@Service
public class PayPalService {

    private static final Logger logger = LoggerFactory.getLogger(PayPalService.class);

    private final PayPalConfig payPalConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ClinicSettingsRepository settingsRepository;
    private final TenantContextHolder tenantContextHolder;

    @Autowired
    public PayPalService(PayPalConfig payPalConfig,
                         RestTemplate paypalRestTemplate,
                         ObjectMapper objectMapper,
                         ClinicSettingsRepository settingsRepository,
                         TenantContextHolder tenantContextHolder) {
        this.payPalConfig = payPalConfig;
        this.restTemplate = paypalRestTemplate;
        this.objectMapper = objectMapper;
        this.settingsRepository = settingsRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    public String createOrder(BigDecimal amount, String currency, Map<String, Object> metadata) {
        try {
            PayPalCredentials credentials = resolveCredentials();
            String baseUrl = resolveBaseUrl(credentials.environment());
            String accessToken = getAccessToken(credentials, baseUrl);
            
            Map<String, Object> orderRequest = createOrderRequest(amount, currency, metadata);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.set("PayPal-Request-Id", generateRequestId());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orderRequest, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders",
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                String orderId = responseJson.get("id").asText();
                
                logger.info("PayPal order created successfully: {}", orderId);
                return orderId;
            } else {
                logger.error("Failed to create PayPal order. Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to create PayPal order");
            }
            
        } catch (Exception e) {
            logger.error("Error creating PayPal order", e);
            throw new RuntimeException("Failed to create PayPal order: " + e.getMessage());
        }
    }

    public PayPalCaptureResult captureOrder(String orderId) {
        try {
            PayPalCredentials credentials = resolveCredentials();
            String baseUrl = resolveBaseUrl(credentials.environment());
            String accessToken = getAccessToken(credentials, baseUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> entity = new HttpEntity<>("{}", headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders/" + orderId + "/capture",
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                
                String status = responseJson.get("status").asText();
                String captureId = null;
                String payerEmail = null;
                String payerName = null;
                
                JsonNode purchaseUnits = responseJson.get("purchase_units");
                if (purchaseUnits != null && purchaseUnits.isArray() && purchaseUnits.size() > 0) {
                    JsonNode captures = purchaseUnits.get(0).get("payments").get("captures");
                    if (captures != null && captures.isArray() && captures.size() > 0) {
                        captureId = captures.get(0).get("id").asText();
                    }
                }
                
                JsonNode payer = responseJson.get("payer");
                if (payer != null) {
                    JsonNode emailAddress = payer.get("email_address");
                    if (emailAddress != null) {
                        payerEmail = emailAddress.asText();
                    }
                    
                    JsonNode name = payer.get("name");
                    if (name != null) {
                        JsonNode givenName = name.get("given_name");
                        JsonNode surname = name.get("surname");
                        if (givenName != null && surname != null) {
                            payerName = givenName.asText() + " " + surname.asText();
                        }
                    }
                }
                
                logger.info("PayPal order captured successfully: {} -> {}", orderId, captureId);
                
                return new PayPalCaptureResult(
                    "COMPLETED".equals(status),
                    captureId,
                    payerEmail,
                    payerName,
                    response.getBody()
                );
            } else {
                logger.error("Failed to capture PayPal order. Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                return new PayPalCaptureResult(false, null, null, null, response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("Error capturing PayPal order: " + orderId, e);
            return new PayPalCaptureResult(false, null, null, null, null);
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature, String webhookId) {
        // Simplified signature verification - in production, implement proper verification
        // using PayPal's webhook verification API
        try {
            // For now, just validate that we have the required components
            return signature != null && !signature.isEmpty() && 
                   payload != null && !payload.isEmpty() &&
                   webhookId != null && webhookId.equals(payPalConfig.getWebhookId());
        } catch (Exception e) {
            logger.error("Error verifying webhook signature", e);
            return false;
        }
    }

    private String getAccessToken(PayPalCredentials credentials, String baseUrl) {
        try {
            String auth = credentials.clientId() + ":" + credentials.clientSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);
            
            String body = "grant_type=client_credentials";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/v1/oauth2/token",
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                return responseJson.get("access_token").asText();
            } else {
                throw new RuntimeException("Failed to get PayPal access token");
            }
            
        } catch (Exception e) {
            logger.error("Error getting PayPal access token", e);
            throw new RuntimeException("Failed to get PayPal access token: " + e.getMessage());
        }
    }

    private Map<String, Object> createOrderRequest(BigDecimal amount, String currency, Map<String, Object> metadata) {
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("intent", "CAPTURE");
        
        Map<String, Object> purchaseUnit = new HashMap<>();
        
        Map<String, Object> amountObj = new HashMap<>();
        amountObj.put("currency_code", currency);
        amountObj.put("value", amount.toString());
        
        purchaseUnit.put("amount", amountObj);
        purchaseUnit.put("description", "Virtual Consultation Fee");
        
        if (metadata != null) {
            purchaseUnit.put("custom_id", metadata.toString());
        }
        
        orderRequest.put("purchase_units", new Object[]{purchaseUnit});
        
        Map<String, Object> applicationContext = new HashMap<>();
        applicationContext.put("return_url", "https://example.com/return");
        applicationContext.put("cancel_url", "https://example.com/cancel");
        applicationContext.put("shipping_preference", "NO_SHIPPING");
        applicationContext.put("user_action", "PAY_NOW");
        applicationContext.put("payment_method", Map.of(
            "payee_preferred", "IMMEDIATE_PAYMENT_REQUIRED"
        ));
        orderRequest.put("application_context", applicationContext);
        
        return orderRequest;
    }

    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private PayPalCredentials resolveCredentials() {
        Long tenantId = tenantContextHolder.requireTenantId();
        var settingsOpt = settingsRepository.findByTenantId(tenantId);
        if (settingsOpt.isPresent()) {
            var settings = settingsOpt.get();
            String dbClientId = settings.getPaypalClientId();
            String dbClientSecret = settings.getPaypalClientSecret();
            String dbEnvironment = settings.getPaypalEnvironment();

            if (StringUtils.hasText(dbClientId) && StringUtils.hasText(dbClientSecret)) {
                String environment = StringUtils.hasText(dbEnvironment) ? dbEnvironment : payPalConfig.getEnvironment();
                logger.debug("Using PayPal credentials from database settings");
                return new PayPalCredentials(dbClientId, dbClientSecret, environment);
            }
        }

        String fallbackClientId = payPalConfig.getClientId();
        String fallbackClientSecret = payPalConfig.getClientSecret();
        String fallbackEnvironment = payPalConfig.getEnvironment();

        if (!StringUtils.hasText(fallbackClientId) || !StringUtils.hasText(fallbackClientSecret)) {
            throw new IllegalStateException("PayPal credentials not configured. Please add them in Settings or environment variables.");
        }

        logger.debug("Using PayPal credentials from environment configuration");
        return new PayPalCredentials(fallbackClientId, fallbackClientSecret, fallbackEnvironment);
    }

    private String resolveBaseUrl(String environment) {
        if (environment == null) {
            return payPalConfig.getBaseUrl();
        }
        if ("live".equalsIgnoreCase(environment) || "production".equalsIgnoreCase(environment)) {
            return "https://api-m.paypal.com";
        }
        return "https://api-m.sandbox.paypal.com";
    }

    private record PayPalCredentials(String clientId, String clientSecret, String environment) {}

    public static class PayPalCaptureResult {
        private final boolean success;
        private final String captureId;
        private final String payerEmail;
        private final String payerName;
        private final String rawResponse;

        public PayPalCaptureResult(boolean success, String captureId, String payerEmail, String payerName, String rawResponse) {
            this.success = success;
            this.captureId = captureId;
            this.payerEmail = payerEmail;
            this.payerName = payerName;
            this.rawResponse = rawResponse;
        }

        public boolean isSuccess() { return success; }
        public String getCaptureId() { return captureId; }
        public String getPayerEmail() { return payerEmail; }
        public String getPayerName() { return payerName; }
        public String getRawResponse() { return rawResponse; }
    }
}
