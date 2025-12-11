package com.clinic.modules.ecommerce.controller.publicapi;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.ecommerce.config.RequiresEcommerceFeature;
import com.clinic.modules.ecommerce.dto.*;
import com.clinic.modules.ecommerce.exception.PaymentProcessingException;
import com.clinic.modules.ecommerce.model.OrderEntity;
import com.clinic.modules.ecommerce.model.OrderStatus;
import com.clinic.modules.ecommerce.model.PaymentEntity;
import com.clinic.modules.ecommerce.repository.OrderRepository;
import com.clinic.modules.ecommerce.service.PayPalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Public API controller for PayPal payment processing.
 * Handles payment initiation, capture, and webhook processing for e-commerce orders.
 */
@RestController
@RequestMapping("/public/payments/paypal")
@RequiresEcommerceFeature
public class EcommercePaymentController {

    private static final Logger logger = LoggerFactory.getLogger(EcommercePaymentController.class);

    private final PayPalService payPalService;
    private final OrderRepository orderRepository;
    private final TenantRepository tenantRepository;

    public EcommercePaymentController(@Qualifier("ecommercePayPalService") PayPalService payPalService,
                           OrderRepository orderRepository,
                           TenantRepository tenantRepository) {
        this.payPalService = payPalService;
        this.orderRepository = orderRepository;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Initiate PayPal payment for an order.
     * Creates a PayPal order and returns approval URL for user to complete payment.
     *
     * @param request Payment initiation request
     * @param tenantSlug Tenant slug from path parameter
     * @return PaymentInitiationResponse with approval URL
     */
    @PostMapping("/{tenantSlug}/initiate")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiationRequest request,
            @PathVariable String tenantSlug) {
        
        try {
            logger.info("Initiating PayPal payment for order: {}, tenant: {}", request.getOrderId(), tenantSlug);

            // Resolve tenant
            TenantEntity tenant = tenantRepository.findBySlugIgnoreCaseAndNotDeleted(tenantSlug)
                .orElseThrow(() -> new PaymentProcessingException("Tenant not found: " + tenantSlug));

            // Find order with tenant isolation
            OrderEntity order = orderRepository.findByIdAndTenant(request.getOrderId(), tenant.getId())
                .orElseThrow(() -> new PaymentProcessingException("Order not found: " + request.getOrderId()));

            // Validate order status
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                throw new PaymentProcessingException("Order is not in PENDING_PAYMENT status");
            }

            // Create PayPal order
            PayPalService.PayPalOrderResponse paypalResponse = payPalService.createPayPalOrder(
                order, request.getReturnUrl(), request.getCancelUrl());

            PaymentInitiationResponse response = PaymentInitiationResponse.success(
                paypalResponse.getOrderId(),
                paypalResponse.getOrderId(),
                paypalResponse.getApprovalUrl(),
                paypalResponse.getStatus()
            );

            logger.info("PayPal payment initiated successfully for order: {}", request.getOrderId());
            return ResponseEntity.ok(response);

        } catch (PaymentProcessingException e) {
            logger.error("Payment initiation failed for order: {}", request.getOrderId(), e);
            return ResponseEntity.badRequest()
                .body(PaymentInitiationResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during payment initiation for order: {}", request.getOrderId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentInitiationResponse.error("Payment initiation failed"));
        }
    }

    /**
     * Capture PayPal payment after user approval.
     * Completes the payment process and updates order status.
     *
     * @param request Payment capture request
     * @param tenantSlug Tenant slug from path parameter
     * @return PaymentCaptureResponse with capture details
     */
    @PostMapping("/{tenantSlug}/capture")
    public ResponseEntity<PaymentCaptureResponse> capturePayment(
            @Valid @RequestBody PaymentCaptureRequest request,
            @PathVariable String tenantSlug) {
        
        try {
            logger.info("Capturing PayPal payment for order: {}, tenant: {}", request.getPaypalOrderId(), tenantSlug);

            // Resolve tenant
            TenantEntity tenant = tenantRepository.findBySlugIgnoreCaseAndNotDeleted(tenantSlug)
                .orElseThrow(() -> new PaymentProcessingException("Tenant not found: " + tenantSlug));

            // Capture payment
            PayPalService.PayPalCaptureResponse paypalResponse = payPalService.capturePayPalPayment(
                request.getPaypalOrderId(), tenant.getId());

            if (paypalResponse.isSuccess()) {
                // Find payment to get order ID
                Optional<PaymentEntity> paymentOpt = payPalService.getPaymentForOrder(null, tenant.getId());
                if (paymentOpt.isPresent()) {
                    OrderEntity order = paymentOpt.get().getOrder();
                    order.setStatus(OrderStatus.PAID);
                    orderRepository.save(order);

                    PaymentCaptureResponse response = PaymentCaptureResponse.success(
                        request.getPaypalOrderId(),
                        paypalResponse.getCaptureId(),
                        paypalResponse.getStatus(),
                        order.getId()
                    );

                    logger.info("PayPal payment captured successfully for order: {}", request.getPaypalOrderId());
                    return ResponseEntity.ok(response);
                }
            }

            return ResponseEntity.badRequest()
                .body(PaymentCaptureResponse.error("Payment capture failed"));

        } catch (PaymentProcessingException e) {
            logger.error("Payment capture failed for order: {}", request.getPaypalOrderId(), e);
            return ResponseEntity.badRequest()
                .body(PaymentCaptureResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during payment capture for order: {}", request.getPaypalOrderId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentCaptureResponse.error("Payment capture failed"));
        }
    }

    /**
     * Get payment status for an order.
     *
     * @param orderId Order ID
     * @param tenantSlug Tenant slug from path parameter
     * @return PaymentResponse with payment details
     */
    @GetMapping("/{tenantSlug}/orders/{orderId}/payment")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @PathVariable Long orderId,
            @PathVariable String tenantSlug) {
        
        try {
            logger.info("Getting payment status for order: {}, tenant: {}", orderId, tenantSlug);

            // Resolve tenant
            TenantEntity tenant = tenantRepository.findBySlugIgnoreCaseAndNotDeleted(tenantSlug)
                .orElseThrow(() -> new PaymentProcessingException("Tenant not found: " + tenantSlug));

            // Find payment
            Optional<PaymentEntity> paymentOpt = payPalService.getPaymentForOrder(orderId, tenant.getId());
            
            if (paymentOpt.isPresent()) {
                PaymentEntity payment = paymentOpt.get();
                PaymentResponse response = new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getProvider(),
                    payment.getProviderOrderId(),
                    payment.getProviderPaymentId(),
                    payment.getStatus(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getCreatedAt(),
                    payment.getUpdatedAt()
                );

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error getting payment status for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handle PayPal webhooks for payment synchronization.
     * Processes webhook events to keep payment status in sync with PayPal.
     *
     * @param request HTTP request containing webhook payload
     * @param tenantSlug Tenant slug from path parameter
     * @return Success or error response
     */
    @PostMapping("/{tenantSlug}/webhook")
    public ResponseEntity<String> handleWebhook(
            HttpServletRequest request,
            @PathVariable String tenantSlug) {
        
        try {
            logger.info("Processing PayPal webhook for tenant: {}", tenantSlug);

            // Resolve tenant
            TenantEntity tenant = tenantRepository.findBySlugIgnoreCaseAndNotDeleted(tenantSlug)
                .orElseThrow(() -> new PaymentProcessingException("Tenant not found: " + tenantSlug));

            // Read webhook payload
            StringBuilder payload = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                payload.append(line);
            }

            // Verify webhook signature (simplified)
            String signature = request.getHeader("PAYPAL-TRANSMISSION-SIG");
            if (!payPalService.verifyWebhookSignature(payload.toString(), signature)) {
                logger.warn("Invalid webhook signature for tenant: {}", tenantSlug);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            // Process webhook
            boolean processed = payPalService.processWebhook(payload.toString(), tenant.getId());
            
            if (processed) {
                logger.info("PayPal webhook processed successfully for tenant: {}", tenantSlug);
                return ResponseEntity.ok("Webhook processed successfully");
            } else {
                logger.warn("Failed to process PayPal webhook for tenant: {}", tenantSlug);
                return ResponseEntity.badRequest().body("Webhook processing failed");
            }

        } catch (Exception e) {
            logger.error("Error processing PayPal webhook for tenant: {}", tenantSlug, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing error");
        }
    }
}