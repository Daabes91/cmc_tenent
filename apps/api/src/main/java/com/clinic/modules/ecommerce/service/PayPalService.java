package com.clinic.modules.ecommerce.service;

import com.clinic.modules.ecommerce.exception.PaymentProcessingException;
import com.clinic.modules.ecommerce.model.OrderEntity;
import com.clinic.modules.ecommerce.model.PaymentEntity;
import com.clinic.modules.ecommerce.model.PaymentStatus;
import com.clinic.modules.ecommerce.repository.PaymentRepository;
import com.clinic.modules.saas.service.PayPalConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for PayPal payment processing in e-commerce orders.
 * Handles payment creation, capture, and webhook processing.
 */
@Service("ecommercePayPalService")
public class PayPalService {

    private static final Logger logger = LoggerFactory.getLogger(PayPalService.class);

    private final PayPalConfigService payPalConfigService;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PayPalService(PayPalConfigService payPalConfigService,
                        PaymentRepository paymentRepository,
                        RestTemplate restTemplate,
                        ObjectMapper objectMapper) {
        this.payPalConfigService = payPalConfigService;
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a PayPal order for the given e-commerce order.
     *
     * @param order The e-commerce order
     * @param returnUrl URL to redirect after successful payment
     * @param cancelUrl URL to redirect after cancelled payment
     * @return PayPal order creation response with approval URL
     * @throws PaymentProcessingException if payment creation fails
     */
    @Transactional
    public PayPalOrderResponse createPayPalOrder(OrderEntity order, String returnUrl, String cancelUrl) {
        try {
            logger.info("Creating PayPal order for order ID: {}, tenant: {}", order.getId(), order.getTenant().getId());

            // Check if payment already exists for this order
            Optional<PaymentEntity> existingPayment = paymentRepository.findByOrderIdAndTenantId(
                order.getId(), order.getTenant().getId());
            
            if (existingPayment.isPresent() && existingPayment.get().isSuccessful()) {
                throw new PaymentProcessingException("Payment already completed for this order");
            }

            // Create PayPal order request
            Map<String, Object> orderRequest = createPayPalOrderRequest(order, returnUrl, cancelUrl);
            
            // Make API call to PayPal
            Long tenantId = order.getTenant().getId();
            String accessToken = payPalConfigService.getAccessTokenForTenant(tenantId);
            String baseUrl = payPalConfigService.getBaseUrlForTenant(tenantId);
            String url = baseUrl + "/v2/checkout/orders";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(orderRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                String paypalOrderId = responseJson.get("id").asText();
                String approvalUrl = extractApprovalUrl(responseJson);

                // Create or update payment entity
                PaymentEntity payment = existingPayment.orElse(new PaymentEntity(
                    order.getTenant(), order, order.getTotalAmount()));
                
                payment.setProviderOrderId(paypalOrderId);
                payment.setStatus(PaymentStatus.CREATED);
                payment.setRawResponse(response.getBody());
                
                paymentRepository.save(payment);

                logger.info("PayPal order created successfully: {}", paypalOrderId);
                return new PayPalOrderResponse(paypalOrderId, approvalUrl, PaymentStatus.CREATED);
            }

            throw new PaymentProcessingException("Failed to create PayPal order: " + response.getStatusCode());

        } catch (Exception e) {
            logger.error("Failed to create PayPal order for order ID: {}", order.getId(), e);
            throw new PaymentProcessingException("Failed to create PayPal order", e);
        }
    }

    /**
     * Capture a PayPal payment after user approval.
     *
     * @param paypalOrderId PayPal order ID
     * @param tenantId Tenant ID for isolation
     * @return Payment capture response
     * @throws PaymentProcessingException if payment capture fails
     */
    @Transactional
    public PayPalCaptureResponse capturePayPalPayment(String paypalOrderId, Long tenantId) {
        try {
            logger.info("Capturing PayPal payment for order: {}, tenant: {}", paypalOrderId, tenantId);

            // Find payment entity
            PaymentEntity payment = paymentRepository.findByProviderOrderIdAndTenantId(paypalOrderId, tenantId)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found for PayPal order: " + paypalOrderId));

            if (payment.isSuccessful()) {
                logger.warn("Payment already captured for PayPal order: {}", paypalOrderId);
                return new PayPalCaptureResponse(payment.getProviderPaymentId(), PaymentStatus.CAPTURED, true);
            }

            // Make API call to capture payment
            String accessToken = payPalConfigService.getAccessTokenForTenant(tenantId);
            String baseUrl = payPalConfigService.getBaseUrlForTenant(tenantId);
            String url = baseUrl + "/v2/checkout/orders/" + paypalOrderId + "/capture";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                String captureId = extractCaptureId(responseJson);
                String status = responseJson.get("status").asText();

                // Update payment entity
                if ("COMPLETED".equals(status)) {
                    payment.markAsCaptured(captureId, response.getBody());
                    paymentRepository.save(payment);

                    logger.info("PayPal payment captured successfully: {}", captureId);
                    return new PayPalCaptureResponse(captureId, PaymentStatus.CAPTURED, true);
                } else {
                    payment.setRawResponse(response.getBody());
                    paymentRepository.save(payment);
                    
                    logger.warn("PayPal payment capture pending: {}", status);
                    return new PayPalCaptureResponse(captureId, PaymentStatus.PENDING, false);
                }
            }

            throw new PaymentProcessingException("Failed to capture PayPal payment: " + response.getStatusCode());

        } catch (Exception e) {
            logger.error("Failed to capture PayPal payment for order: {}", paypalOrderId, e);
            throw new PaymentProcessingException("Failed to capture PayPal payment", e);
        }
    }

    /**
     * Process PayPal webhook events for payment synchronization.
     *
     * @param webhookPayload Raw webhook payload
     * @param tenantId Tenant ID for isolation
     * @return true if webhook was processed successfully
     */
    @Transactional
    public boolean processWebhook(String webhookPayload, Long tenantId) {
        try {
            logger.info("Processing PayPal webhook for tenant: {}", tenantId);

            JsonNode webhook = objectMapper.readTree(webhookPayload);
            String eventType = webhook.get("event_type").asText();
            JsonNode resource = webhook.get("resource");

            switch (eventType) {
                case "CHECKOUT.ORDER.APPROVED":
                    return handleOrderApproved(resource, tenantId);
                case "PAYMENT.CAPTURE.COMPLETED":
                    return handlePaymentCaptured(resource, tenantId);
                case "PAYMENT.CAPTURE.DENIED":
                case "PAYMENT.CAPTURE.REFUNDED":
                    return handlePaymentFailed(resource, tenantId);
                default:
                    logger.info("Unhandled webhook event type: {}", eventType);
                    return true; // Don't fail for unknown events
            }

        } catch (Exception e) {
            logger.error("Failed to process PayPal webhook for tenant: {}", tenantId, e);
            return false;
        }
    }

    /**
     * Get payment status for an order.
     *
     * @param orderId Order ID
     * @param tenantId Tenant ID for isolation
     * @return Optional PaymentEntity
     */
    public Optional<PaymentEntity> getPaymentForOrder(Long orderId, Long tenantId) {
        return paymentRepository.findByOrderIdAndTenantId(orderId, tenantId);
    }

    /**
     * Get payment by provider order id (e.g., PayPal order id).
     *
     * @param providerOrderId PayPal order id
     * @param tenantId Tenant id for isolation
     * @return Optional PaymentEntity
     */
    public Optional<PaymentEntity> getPaymentByProviderOrderId(String providerOrderId, Long tenantId) {
        return paymentRepository.findByProviderOrderIdAndTenantId(providerOrderId, tenantId);
    }

    /**
     * Verify webhook signature (simplified implementation).
     * In production, implement proper PayPal webhook signature verification.
     *
     * @param payload Webhook payload
     * @param signature Webhook signature
     * @return true if signature is valid
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        // TODO: Implement proper PayPal webhook signature verification
        // For now, return true to allow webhook processing
        logger.warn("Webhook signature verification not implemented - allowing all webhooks");
        return true;
    }

    // Private helper methods

    private Map<String, Object> createPayPalOrderRequest(OrderEntity order, String returnUrl, String cancelUrl) {
        Map<String, Object> request = new HashMap<>();
        request.put("intent", "CAPTURE");

        // Purchase units
        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("reference_id", order.getOrderNumber());
        
        Map<String, Object> amount = new HashMap<>();
        amount.put("currency_code", order.getCurrency());
        amount.put("value", order.getTotalAmount().toString());
        purchaseUnit.put("amount", amount);

        // Shipping address
        Map<String, Object> shipping = new HashMap<>();
        Map<String, Object> address = new HashMap<>();
        String line1 = order.getBillingAddressLine1() != null && !order.getBillingAddressLine1().isBlank()
                ? order.getBillingAddressLine1()
                : "N/A";
        address.put("address_line_1", line1);
        if (order.getBillingAddressLine2() != null && !order.getBillingAddressLine2().isBlank()) {
            address.put("address_line_2", order.getBillingAddressLine2());
        }
        String city = order.getBillingAddressCity() != null && !order.getBillingAddressCity().isBlank()
                ? order.getBillingAddressCity()
                : "N/A";
        address.put("admin_area_2", city);
        if (order.getBillingAddressState() != null && !order.getBillingAddressState().isBlank()) {
            address.put("admin_area_1", order.getBillingAddressState());
        }
        String postal = order.getBillingAddressPostalCode();
        if (postal == null || postal.isBlank()) {
            postal = "00000";
        }
        address.put("postal_code", postal);
        address.put("country_code", getCountryCode(order.getBillingAddressCountry()));
        shipping.put("address", address);
        
        Map<String, Object> name = new HashMap<>();
        name.put("full_name", order.getCustomerName());
        shipping.put("name", name);
        
        purchaseUnit.put("shipping", shipping);

        request.put("purchase_units", List.of(purchaseUnit));

        // Application context
        Map<String, Object> applicationContext = new HashMap<>();
        applicationContext.put("return_url", returnUrl);
        applicationContext.put("cancel_url", cancelUrl);
        applicationContext.put("brand_name", "E-Commerce Store");
        applicationContext.put("landing_page", "BILLING");
        applicationContext.put("user_action", "PAY_NOW");
        request.put("application_context", applicationContext);

        return request;
    }

    private String extractApprovalUrl(JsonNode responseJson) {
        JsonNode links = responseJson.get("links");
        if (links != null && links.isArray()) {
            for (JsonNode link : links) {
                if ("approve".equals(link.get("rel").asText())) {
                    return link.get("href").asText();
                }
            }
        }
        throw new PaymentProcessingException("Approval URL not found in PayPal response");
    }

    private String extractCaptureId(JsonNode responseJson) {
        JsonNode purchaseUnits = responseJson.get("purchase_units");
        if (purchaseUnits != null && purchaseUnits.isArray() && purchaseUnits.size() > 0) {
            JsonNode captures = purchaseUnits.get(0).get("payments").get("captures");
            if (captures != null && captures.isArray() && captures.size() > 0) {
                return captures.get(0).get("id").asText();
            }
        }
        throw new PaymentProcessingException("Capture ID not found in PayPal response");
    }

    private boolean handleOrderApproved(JsonNode resource, Long tenantId) {
        String paypalOrderId = resource.get("id").asText();
        Optional<PaymentEntity> paymentOpt = paymentRepository.findByProviderOrderIdAndTenantId(paypalOrderId, tenantId);
        
        if (paymentOpt.isPresent()) {
            PaymentEntity payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setRawResponse(resource.toString());
            paymentRepository.save(payment);
            logger.info("PayPal order approved: {}", paypalOrderId);
            return true;
        }
        
        logger.warn("Payment not found for approved PayPal order: {}", paypalOrderId);
        return false;
    }

    private boolean handlePaymentCaptured(JsonNode resource, Long tenantId) {
        String captureId = resource.get("id").asText();
        // Try to find by capture ID or order ID
        JsonNode supplementaryData = resource.get("supplementary_data");
        if (supplementaryData != null) {
            JsonNode relatedIds = supplementaryData.get("related_ids");
            if (relatedIds != null) {
                String orderId = relatedIds.get("order_id").asText();
                Optional<PaymentEntity> paymentOpt = paymentRepository.findByProviderOrderIdAndTenantId(orderId, tenantId);
                
                if (paymentOpt.isPresent()) {
                    PaymentEntity payment = paymentOpt.get();
                    payment.markAsCaptured(captureId, resource.toString());
                    paymentRepository.save(payment);
                    logger.info("PayPal payment captured via webhook: {}", captureId);
                    return true;
                }
            }
        }
        
        logger.warn("Payment not found for captured PayPal payment: {}", captureId);
        return false;
    }

    private boolean handlePaymentFailed(JsonNode resource, Long tenantId) {
        String captureId = resource.get("id").asText();
        // Similar logic to handlePaymentCaptured but mark as failed
        JsonNode supplementaryData = resource.get("supplementary_data");
        if (supplementaryData != null) {
            JsonNode relatedIds = supplementaryData.get("related_ids");
            if (relatedIds != null) {
                String orderId = relatedIds.get("order_id").asText();
                Optional<PaymentEntity> paymentOpt = paymentRepository.findByProviderOrderIdAndTenantId(orderId, tenantId);
                
                if (paymentOpt.isPresent()) {
                    PaymentEntity payment = paymentOpt.get();
                    payment.markAsFailed(resource.toString());
                    paymentRepository.save(payment);
                    logger.info("PayPal payment failed via webhook: {}", captureId);
                    return true;
                }
            }
        }
        
        logger.warn("Payment not found for failed PayPal payment: {}", captureId);
        return false;
    }

    private String getCountryCode(String country) {
        // Simple mapping - in production, use a proper country code mapping
        switch (country.toUpperCase()) {
            case "UNITED STATES":
            case "USA":
                return "US";
            case "CANADA":
                return "CA";
            case "UNITED KINGDOM":
            case "UK":
                return "GB";
            default:
                return "US"; // Default fallback
        }
    }

    // Response DTOs
    public static class PayPalOrderResponse {
        private final String orderId;
        private final String approvalUrl;
        private final PaymentStatus status;

        public PayPalOrderResponse(String orderId, String approvalUrl, PaymentStatus status) {
            this.orderId = orderId;
            this.approvalUrl = approvalUrl;
            this.status = status;
        }

        public String getOrderId() { return orderId; }
        public String getApprovalUrl() { return approvalUrl; }
        public PaymentStatus getStatus() { return status; }
    }

    public static class PayPalCaptureResponse {
        private final String captureId;
        private final PaymentStatus status;
        private final boolean success;

        public PayPalCaptureResponse(String captureId, PaymentStatus status, boolean success) {
            this.captureId = captureId;
            this.status = status;
            this.success = success;
        }

        public String getCaptureId() { return captureId; }
        public PaymentStatus getStatus() { return status; }
        public boolean isSuccess() { return success; }
    }
}
