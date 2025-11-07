package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.DashboardSummaryResponse;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.appointment.AppointmentStatus;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.treatment.PaymentEntity;
import com.clinic.modules.core.treatment.PaymentRepository;
import com.clinic.modules.core.treatment.TreatmentPlanPaymentEntity;
import com.clinic.modules.core.treatment.TreatmentPlanPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Service
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final PaymentRepository paymentRepository;
    private final TreatmentPlanPaymentRepository treatmentPlanPaymentRepository;

    public DashboardService(AppointmentRepository appointmentRepository,
                            PatientRepository patientRepository,
                            PaymentRepository paymentRepository,
                            TreatmentPlanPaymentRepository treatmentPlanPaymentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.paymentRepository = paymentRepository;
        this.treatmentPlanPaymentRepository = treatmentPlanPaymentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);
        Instant startOfToday = today.atStartOfDay(zoneId).toInstant();
        Instant endOfToday = today.plusDays(1).atStartOfDay(zoneId).toInstant();

        long appointmentsToday = appointmentRepository.countByScheduledAtBetween(startOfToday, endOfToday);
        long pendingConfirmations = appointmentRepository.countByStatusAndScheduledAtBetween(
                AppointmentStatus.SCHEDULED, startOfToday, endOfToday);

        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        Instant startOfMonth = firstDayOfMonth.atStartOfDay(zoneId).toInstant();

        // Calculate actual revenue from all payment sources
        // 1. Payments from visits (PaymentEntity)
        List<PaymentEntity> visitPayments = paymentRepository.findByPaymentDateBetween(startOfMonth, endOfToday);
        BigDecimal visitRevenue = visitPayments.stream()
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Direct treatment plan payments (TreatmentPlanPaymentEntity)
        List<TreatmentPlanPaymentEntity> directPayments = treatmentPlanPaymentRepository
                .findByPaymentDateBetween(startOfMonth, endOfToday);
        BigDecimal directRevenue = directPayments.stream()
                .map(TreatmentPlanPaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total actual revenue
        BigDecimal totalRevenue = visitRevenue.add(directRevenue);
        double revenueMonthToDate = totalRevenue.doubleValue();

        Instant sevenDaysAgo = today.minusDays(7).atStartOfDay(zoneId).toInstant();
        long newPatients = patientRepository.countByCreatedAtAfter(sevenDaysAgo);

        return new DashboardSummaryResponse(
                appointmentsToday,
                pendingConfirmations,
                revenueMonthToDate,
                newPatients
        );
    }
}
