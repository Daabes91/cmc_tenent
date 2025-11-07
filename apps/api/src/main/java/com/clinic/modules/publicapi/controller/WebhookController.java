package com.clinic.modules.publicapi.controller;

import com.clinic.modules.core.payment.PayPalService;
import com.clinic.modules.core.payment.PayPalPaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/webhooks")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final PayPalPaymentService paymentService;
    private final PayPalService payPalService;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebhookController(PayPalPaymentService paymentService, PayPalService payPalService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.payPalService = payPalService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/paypal")
    public ResponseEntity<String> handlePayPalWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-ID", required = false) String transmissionId,
            @RequestHeader(value = "PAYPAL-CERT-ID", required = false) String certId,
            @RequestHeader(value = "PAYPAL-AUTH-ALGO", required = false) String authAlgo,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-SIG", required = false) String signature) {
        
        try {
            logger.info("Received PayPal webhook with transmission ID: {}", transmissionId);
            
            // Verify webhook signature (simplified for now)
            if (!payPalService.verifyWebhookSignature(payload, signature, transmissionId)) {
                logger.warn("Invalid webhook signature for transmission ID: {}", transmissionId);
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            // Parse webhook payload
            JsonNode webhookEvent = objectMapper.readTree(payload);
            String eventType = webhookEvent.get("event_type").asText();
            
            logger.info("Processing PayPal webhook event: {}", eventType);

            // Handle different event types
            switch (eventType) {
                case "PAYMENT.CAPTURE.COMPLETED":
                    return handlePaymentCaptureCompleted(webhookEvent, payload);
                case "CHECKOUT.ORDER.APPROVED":
                    // For now, we'll handle capture completion instead of approval
                    logger.info("Order approved, waiting for capture completion");
                    return ResponseEntity.ok("OK");
                default:
                    logger.info("Unhandled webhook event type: {}", eventType);
                    return ResponseEntity.ok("OK");
            }

        } catch (Exception e) {
            logger.error("Error processing PayPal webhook", e);
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }

    private ResponseEntity<String> handlePaymentCaptureCompleted(JsonNode webhookEvent, String rawPayload) {
        try {
            JsonNode resource = webhookEvent.get("resource");
            if (resource == null) {
                logger.error("No resource found in webhook payload");
                return ResponseEntity.badRequest().body("Invalid payload");
            }

            String captureId = resource.get("id").asText();
            String status = resource.get("status").asText();
            
            // Get order ID from supplementary data or custom ID
            JsonNode supplementaryData = resource.get("supplementary_data");
            JsonNode relatedIds = supplementaryData != null ? supplementaryData.get("related_ids") : null;
            String orderId = null;
            
            if (relatedIds != null && relatedIds.get("order_id") != null) {
                orderId = relatedIds.get("order_id").asText();
            }

            if (orderId == null) {
                logger.error("Could not extract order ID from webhook payload");
                return ResponseEntity.badRequest().body("Order ID not found");
            }

            if (!"COMPLETED".equals(status)) {
                logger.warn("Payment capture not completed. Status: {}, Order: {}", status, orderId);
                return ResponseEntity.ok("OK");
            }

            // Extract payer information
            String payerEmail = null;
            String payerName = null;
            
            // Note: Payer info might be in different locations depending on webhook structure
            // This is a simplified extraction - adjust based on actual PayPal webhook structure

            // Process the payment
            boolean success = paymentService.processWebhookPayment(orderId, captureId, payerEmail, payerName, rawPayload);
            
            if (success) {
                logger.info("Successfully processed webhook for order: {} capture: {}", orderId, captureId);
                return ResponseEntity.ok("OK");
            } else {
                logger.error("Failed to process webhook for order: {}", orderId);
                return ResponseEntity.badRequest().body("Processing failed");
            }

        } catch (Exception e) {
            logger.error("Error handling payment capture completed webhook", e);
            return ResponseEntity.badRequest().body("Processing failed");
        }
    }
}