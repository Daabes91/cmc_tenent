package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.dto.PayPalWebhookEvent;
import com.clinic.modules.saas.service.AsyncWebhookProcessor;
import com.clinic.modules.saas.service.PayPalConfigService;
import com.clinic.modules.saas.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling PayPal webhook notifications.
 * Verifies webhook signatures and processes subscription events.
 */
@RestController
@RequestMapping("/api/webhooks")
public class PayPalSubscriptionWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(PayPalSubscriptionWebhookController.class);

    private final WebhookService webhookService;
    private final AsyncWebhookProcessor asyncWebhookProcessor;
    private final PayPalConfigService payPalConfigService;

    public PayPalSubscriptionWebhookController(
            WebhookService webhookService,
            AsyncWebhookProcessor asyncWebhookProcessor,
            PayPalConfigService payPalConfigService) {
        this.webhookService = webhookService;
        this.asyncWebhookProcessor = asyncWebhookProcessor;
        this.payPalConfigService = payPalConfigService;
    }

    /**
     * Handle PayPal webhook notifications.
     * Verifies signature and processes the event.
     *
     * @param transmissionId PayPal transmission ID header
     * @param transmissionTime PayPal transmission time header
     * @param transmissionSig PayPal transmission signature header
     * @param certUrl PayPal certificate URL header
     * @param authAlgo PayPal auth algorithm header
     * @param webhookEvent The webhook event payload
     * @param rawBody Raw request body for signature verification
     * @return ResponseEntity with status
     */
    @PostMapping("/paypal")
    public ResponseEntity<Map<String, Object>> handlePayPalWebhook(
            @RequestHeader(value = "PAYPAL-TRANSMISSION-ID", required = false) String transmissionId,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-TIME", required = false) String transmissionTime,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-SIG", required = false) String transmissionSig,
            @RequestHeader(value = "PAYPAL-CERT-URL", required = false) String certUrl,
            @RequestHeader(value = "PAYPAL-AUTH-ALGO", required = false) String authAlgo,
            @RequestBody PayPalWebhookEvent webhookEvent,
            @RequestBody String rawBody) {

        logger.info("Received PayPal webhook: {} (ID: {})", webhookEvent.getEventType(), webhookEvent.getId());

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate required headers
            if (transmissionId == null || transmissionTime == null || transmissionSig == null) {
                logger.warn("Missing required webhook headers");
                response.put("received", false);
                response.put("error", "Missing required headers");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Get webhook ID from configuration
            String webhookId;
            try {
                webhookId = payPalConfigService.getConfig().getWebhookId();
                if (webhookId == null || webhookId.isEmpty()) {
                    logger.warn("Webhook ID not configured. Skipping signature verification.");
                    // Process webhook anyway in development/testing
                    webhookService.processWebhookEvent(webhookEvent);
                    response.put("received", true);
                    response.put("warning", "Webhook ID not configured");
                    return ResponseEntity.ok(response);
                }
            } catch (Exception e) {
                logger.error("Failed to get webhook configuration", e);
                response.put("received", false);
                response.put("error", "Configuration error");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // Verify webhook signature
            boolean isValid = webhookService.verifyWebhookSignature(
                    transmissionId,
                    transmissionTime,
                    transmissionSig,
                    certUrl,
                    authAlgo,
                    webhookId,
                    rawBody
            );

            if (!isValid) {
                logger.error("Webhook signature verification failed for transmission ID: {}", transmissionId);
                response.put("received", false);
                response.put("error", "Invalid signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Process the webhook event asynchronously
            asyncWebhookProcessor.processWebhookAsync(webhookEvent);

            // Return 200 OK immediately to PayPal
            response.put("received", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing webhook: {} (ID: {})", webhookEvent.getEventType(), webhookEvent.getId(), e);
            response.put("received", false);
            response.put("error", "Processing error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
