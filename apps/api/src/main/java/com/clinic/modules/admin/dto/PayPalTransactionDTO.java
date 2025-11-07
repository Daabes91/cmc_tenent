package com.clinic.modules.admin.dto;

import com.clinic.modules.core.payment.PaymentStatus;
import com.clinic.modules.core.payment.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;

public record PayPalTransactionDTO(
        Long id,
        String orderId,
        String captureId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        PaymentType type,
        String patientName,
        String doctorName,
        String payerEmail,
        String payerName,
        Long appointmentId,
        Instant createdAt,
        Instant updatedAt
) {
}