package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.PayPalSummaryDTO;
import com.clinic.modules.admin.dto.PayPalTransactionDTO;
import com.clinic.modules.admin.util.DateRange;
import com.clinic.modules.core.payment.PayPalPaymentEntity;
import com.clinic.modules.core.payment.PayPalPaymentRepository;
import com.clinic.modules.core.payment.PaymentStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Service
public class PayPalReportsService {

    private final PayPalPaymentRepository payPalPaymentRepository;

    // PayPal fee rates (approximate)
    private static final BigDecimal PAYPAL_FIXED_FEE = new BigDecimal("0.30");
    private static final BigDecimal PAYPAL_PERCENTAGE_FEE = new BigDecimal("0.029"); // 2.9%

    public PayPalReportsService(PayPalPaymentRepository payPalPaymentRepository) {
        this.payPalPaymentRepository = payPalPaymentRepository;
    }

    @Transactional(readOnly = true)
    public PayPalSummaryDTO getPayPalSummary(int page, int size) {
        // Use current month as default date range
        return getPayPalSummary(page, size, DateRange.currentMonth());
    }

    @Transactional(readOnly = true)
    public PayPalSummaryDTO getPayPalSummary(int page, int size, DateRange dateRange) {
        // Convert DateRange to Instants for filtering
        ZoneId systemZone = ZoneId.systemDefault();
        Instant rangeStart = dateRange.getStartDate().atStartOfDay(systemZone).toInstant();
        Instant rangeEnd = dateRange.getEndDate().plusDays(1).atStartOfDay(systemZone).toInstant();

        // Use optimized database query to filter payments by date range
        List<PayPalPaymentEntity> allPayments = payPalPaymentRepository.findByCreatedAtBetween(rangeStart, rangeEnd);
        
        if (allPayments.isEmpty()) {
            PayPalSummaryDTO.PaginationInfo emptyPagination = new PayPalSummaryDTO.PaginationInfo(
                    page, size, 0L, 0, false, false
            );
            return new PayPalSummaryDTO(
                    BigDecimal.ZERO,
                    0L,
                    0L,
                    0L,
                    0L,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0.0,
                    List.of(),
                    emptyPagination
            );
        }

        // Calculate totals
        BigDecimal totalRevenue = allPayments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .map(PayPalPaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTransactions = allPayments.size();
        long completedTransactions = allPayments.stream()
                .mapToLong(payment -> payment.getStatus() == PaymentStatus.COMPLETED ? 1 : 0)
                .sum();
        long pendingTransactions = allPayments.stream()
                .mapToLong(payment -> payment.getStatus() == PaymentStatus.PENDING ? 1 : 0)
                .sum();
        long failedTransactions = allPayments.stream()
                .mapToLong(payment -> payment.getStatus() == PaymentStatus.FAILED ? 1 : 0)
                .sum();

        BigDecimal averageTransactionValue = completedTransactions > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(completedTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Estimate PayPal fees (fixed fee + percentage)
        BigDecimal estimatedPayPalFees = allPayments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .map(payment -> calculatePayPalFee(payment.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netRevenue = totalRevenue.subtract(estimatedPayPalFees);

        double successRate = totalTransactions > 0 
                ? (double) completedTransactions / totalTransactions * 100.0
                : 0.0;

        // For pagination, we need to manually paginate the filtered results
        int start = page * size;
        int end = Math.min(start + size, allPayments.size());
        List<PayPalPaymentEntity> paginatedPayments = start < allPayments.size() 
                ? allPayments.subList(start, end) 
                : List.of();
        
        // Calculate pagination info
        long totalElements = allPayments.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        List<PayPalTransactionDTO> transactions = paginatedPayments.stream()
                .map(this::mapToTransactionDTO)
                .toList();

        // Create pagination info
        PayPalSummaryDTO.PaginationInfo pagination = new PayPalSummaryDTO.PaginationInfo(
                page,
                size,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious
        );

        return new PayPalSummaryDTO(
                totalRevenue,
                totalTransactions,
                completedTransactions,
                pendingTransactions,
                failedTransactions,
                averageTransactionValue,
                estimatedPayPalFees,
                netRevenue,
                Math.round(successRate * 10.0) / 10.0, // Round to 1 decimal place
                transactions,
                pagination
        );
    }

    private BigDecimal calculatePayPalFee(BigDecimal amount) {
        // PayPal fee = fixed fee + (amount * percentage)
        BigDecimal percentageFee = amount.multiply(PAYPAL_PERCENTAGE_FEE);
        return PAYPAL_FIXED_FEE.add(percentageFee).setScale(2, RoundingMode.HALF_UP);
    }

    private PayPalTransactionDTO mapToTransactionDTO(PayPalPaymentEntity payment) {
        String patientName = null;
        if (payment.getPatient() != null) {
            patientName = payment.getPatient().getFirstName() + " " + payment.getPatient().getLastName();
        }
        
        return new PayPalTransactionDTO(
                payment.getId(),
                payment.getOrderId(),
                payment.getCaptureId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getType(),
                patientName,
                payment.getDoctor() != null ? payment.getDoctor().getFullNameEn() : null,
                payment.getPayerEmail(),
                payment.getPayerName(),
                payment.getAppointment() != null ? payment.getAppointment().getId() : null,
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}