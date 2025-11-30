package com.clinic.modules.saas.dto;

/**
 * Response DTO for payment method update operations.
 * Contains PayPal billing portal URL.
 */
public record PaymentMethodUpdateResponse(
        String portalUrl,
        String message
) {
}
