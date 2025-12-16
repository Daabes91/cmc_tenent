package com.clinic.modules.publicapi.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.core.payment.PayPalPaymentService;
import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/public/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PayPalPaymentService paymentService;
    private final ClinicSettingsRepository settingsRepository;
    private final TenantContextHolder tenantContextHolder;
    private final TenantRepository tenantRepository;

    @Value("${paypal.client-id:}")
    private String paypalClientIdFallback;

    @Value("${paypal.environment:sandbox}")
    private String paypalEnvironmentFallback;

    @Autowired
    public PaymentController(PayPalPaymentService paymentService,
                             ClinicSettingsRepository settingsRepository,
                             TenantContextHolder tenantContextHolder,
                             TenantRepository tenantRepository) {
        this.paymentService = paymentService;
        this.settingsRepository = settingsRepository;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping("/paypal/create-order")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPayPalOrder(@RequestBody CreateOrderRequest request) {
        try {
            // Ensure tenant context is set from header before downstream services fetch credentials
            ensureTenantContext();
            logger.info("Creating PayPal order for patient {} with doctor {}", request.patientId(), request.doctorId());
            
            String orderId = paymentService.createPayPalOrder(
                request.patientId(),
                request.doctorId(),
                request.serviceId(),
                request.slotId()
            );

            Map<String, String> response = Map.of("orderID", orderId);
            return ResponseEntity.ok(ApiResponseFactory.success("ORDER_CREATED", "PayPal order created successfully", response));

        } catch (Exception e) {
            logger.error("Error creating PayPal order", e);
            return ResponseEntity.badRequest()
                .body(ApiResponseFactory.errorWithType("ORDER_CREATION_FAILED", "Failed to create payment order: " + e.getMessage(), List.of()));
        }
    }

    @PostMapping("/paypal/capture")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capturePayPalOrder(@RequestBody CaptureOrderRequest request) {
        try {
            // Ensure tenant context is set from header before downstream services fetch credentials
            ensureTenantContext();
            logger.info("Capturing PayPal order: {}", request.orderID());
            
            boolean success = paymentService.capturePayPalOrder(request.orderID());
            
            if (success) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Payment captured successfully"
                );
                return ResponseEntity.ok(ApiResponseFactory.success("PAYMENT_CAPTURED", "Payment captured successfully", response));
            } else {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "Failed to capture payment"
                );
                return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.errorWithType("PAYMENT_CAPTURE_FAILED", "Payment capture failed", List.of()));
            }

        } catch (Exception e) {
            logger.error("Error capturing PayPal order: " + request.orderID(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponseFactory.errorWithType("PAYMENT_CAPTURE_ERROR", "Failed to capture payment: " + e.getMessage(), List.of()));
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<PaymentSettingsResponse>> getPaymentSettings(HttpServletRequest request) {
        try {
            ClinicSettingsEntity settings = resolveTenantSettings(request);

            BigDecimal fee = settings.getVirtualConsultationFee();

            if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.errorWithType("FEE_NOT_CONFIGURED", "Virtual consultation fee not configured", List.of()));
            }

            PayPalSettings payPalSettings = resolvePayPalSettings(settings);
            if (payPalSettings == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseFactory.errorWithType("PAYPAL_NOT_CONFIGURED", "PayPal configuration not set up in settings or environment", List.of()));
            }

            // PayPal always uses USD regardless of clinic currency
            PaymentSettingsResponse response = new PaymentSettingsResponse(
                fee,
                "USD",
                payPalSettings.clientId(),
                payPalSettings.environment()
            );

            return ResponseEntity.ok(ApiResponseFactory.success("PAYMENT_SETTINGS", "Payment settings retrieved successfully", response));

        } catch (Exception e) {
            logger.error("Error getting payment settings", e);
            return ResponseEntity.badRequest()
                .body(ApiResponseFactory.errorWithType("SETTINGS_ERROR", "Failed to get payment settings: " + e.getMessage(), List.of()));
        }
    }

    public record CreateOrderRequest(
        Long patientId,
        Long doctorId,
        Long serviceId,
        String slotId
    ) {}

    public record CaptureOrderRequest(String orderID) {}

    public record PaymentSettingsResponse(
        BigDecimal virtualConsultationFee,
        String currency,
        String paypalClientId,
        String paypalEnvironment
    ) {}

    private ClinicSettingsEntity resolveTenantSettings(HttpServletRequest request) {
        Long tenantId = null;

        // Prefer explicit slug header
        String slugHeader = request.getHeader("x-tenant-slug");
        if (StringUtils.hasText(slugHeader)) {
            Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCaseAndNotDeleted(slugHeader.trim());
            if (tenantOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found for slug: " + slugHeader);
            }
            tenantId = tenantOpt.get().getId();
        }

        // Fallback to existing tenant context if not resolved from header
        if (tenantId == null) {
            try {
                tenantId = tenantContextHolder.requireTenantId();
            } catch (Exception ignored) {
                // will fallback below
            }
        }

        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant context not provided");
        }

        return settingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic settings not configured"));
    }

    private PayPalSettings resolvePayPalSettings(ClinicSettingsEntity settings) {
        String dbClientId = settings.getPaypalClientId();
        if (StringUtils.hasText(dbClientId)) {
            String dbEnvironment = settings.getPaypalEnvironment();
            String resolvedEnvironment = StringUtils.hasText(dbEnvironment)
                    ? dbEnvironment
                    : paypalEnvironmentFallback;
            return new PayPalSettings(dbClientId, resolvedEnvironment);
        }

        if (!StringUtils.hasText(paypalClientIdFallback)) {
            return null;
        }

        String resolvedEnvironment = StringUtils.hasText(paypalEnvironmentFallback)
                ? paypalEnvironmentFallback
                : "sandbox";
        return new PayPalSettings(paypalClientIdFallback, resolvedEnvironment);
    }

    private record PayPalSettings(String clientId, String environment) {}

    private void ensureTenantContext() {
        // Try to set from slug header first
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest req = attributes.getRequest();
            String slugHeader = req.getHeader("x-tenant-slug");
            if (StringUtils.hasText(slugHeader)) {
                Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCaseAndNotDeleted(slugHeader.trim());
                tenantOpt.ifPresent(tenant ->
                    tenantContextHolder.setTenant(new com.clinic.modules.core.tenant.TenantContext(tenant.getId(), tenant.getSlug()))
                );
            }
        }
    }
}
