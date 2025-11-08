package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.DashboardSummaryResponse;
import com.clinic.modules.admin.dto.TeamOnCallResponse;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.appointment.AppointmentStatus;
import com.clinic.modules.core.doctor.DoctorAvailabilityEntity;
import com.clinic.modules.core.doctor.DoctorAvailabilityRepository;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.treatment.PaymentEntity;
import com.clinic.modules.core.treatment.PaymentRepository;
import com.clinic.modules.core.treatment.TreatmentPlanPaymentEntity;
import com.clinic.modules.core.treatment.TreatmentPlanPaymentRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final PaymentRepository paymentRepository;
    private final TreatmentPlanPaymentRepository treatmentPlanPaymentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final TenantContextHolder tenantContextHolder;

    public DashboardService(AppointmentRepository appointmentRepository,
                            PatientRepository patientRepository,
                            PaymentRepository paymentRepository,
                            TreatmentPlanPaymentRepository treatmentPlanPaymentRepository,
                            DoctorRepository doctorRepository,
                            DoctorAvailabilityRepository doctorAvailabilityRepository,
                            TenantContextHolder tenantContextHolder) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.paymentRepository = paymentRepository;
        this.treatmentPlanPaymentRepository = treatmentPlanPaymentRepository;
        this.doctorRepository = doctorRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(ZoneId zoneId) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        LocalDate today = LocalDate.now(zoneId);
        Instant startOfToday = today.atStartOfDay(zoneId).toInstant();
        Instant endOfToday = today.plusDays(1).atStartOfDay(zoneId).toInstant();

        long appointmentsToday = appointmentRepository.countByTenantIdAndScheduledAtBetween(tenantId, startOfToday, endOfToday);
        long pendingConfirmations = appointmentRepository.countByTenantIdAndStatusAndScheduledAtBetween(
                tenantId, AppointmentStatus.SCHEDULED, startOfToday, endOfToday);

        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        Instant startOfMonth = firstDayOfMonth.atStartOfDay(zoneId).toInstant();

        // Calculate actual revenue from all payment sources
        // 1. Payments from visits (PaymentEntity)
        List<PaymentEntity> visitPayments = paymentRepository.findByTenantIdAndPaymentDateBetween(tenantId, startOfMonth, endOfToday);
        BigDecimal visitRevenue = visitPayments.stream()
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Direct treatment plan payments (TreatmentPlanPaymentEntity)
        List<TreatmentPlanPaymentEntity> directPayments = treatmentPlanPaymentRepository
                .findByTenantIdAndPaymentDateBetween(tenantId, startOfMonth, endOfToday);
        BigDecimal directRevenue = directPayments.stream()
                .map(TreatmentPlanPaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total actual revenue
        BigDecimal totalRevenue = visitRevenue.add(directRevenue);
        double revenueMonthToDate = totalRevenue.doubleValue();

        Instant sevenDaysAgo = today.minusDays(7).atStartOfDay(zoneId).toInstant();
        long newPatients = patientRepository.countByTenantIdAndCreatedAtAfter(tenantId, sevenDaysAgo);

        return new DashboardSummaryResponse(
                appointmentsToday,
                pendingConfirmations,
                revenueMonthToDate,
                newPatients
        );
    }

    /**
     * Get list of doctors on call (active doctors with their current status)
     */
    @Transactional(readOnly = true)
    public List<TeamOnCallResponse> getTeamOnCall(ZoneId zoneId) {
        Long tenantId = tenantContextHolder.requireTenantId();

        // Get all active doctors for this tenant
        List<DoctorEntity> activeDoctors = doctorRepository.findAllByTenantIdAndIsActiveTrue(tenantId);

        Instant now = Instant.now();
        LocalDate today = LocalDate.now(zoneId);

        return activeDoctors.stream()
                .map(doctor -> {
                    // Determine status and next slot
                    StatusAndSlot statusAndSlot = determineStatusAndNextSlot(doctor, now, today, zoneId);

                    return new TeamOnCallResponse(
                            doctor.getId(),
                            doctor.getFullName(),
                            doctor.getSpecialty(),
                            statusAndSlot.status(),
                            statusAndSlot.nextSlot()
                    );
                })
                .collect(Collectors.toList());
    }

    private record StatusAndSlot(String status, Instant nextSlot) {}

    /**
     * Determine the current status of a doctor and their next available slot
     */
    private StatusAndSlot determineStatusAndNextSlot(DoctorEntity doctor, Instant now, LocalDate today, ZoneId zoneId) {
        Long tenantId = tenantContextHolder.requireTenantId();

        // Check if doctor has an active appointment right now
        Instant nowMinus30Min = now.minus(Duration.ofMinutes(30));
        Instant nowPlus30Min = now.plus(Duration.ofMinutes(30));

        long activeAppointments = appointmentRepository
                .countByTenantIdAndStatusAndScheduledAtBetween(
                        tenantId,
                        AppointmentStatus.CONFIRMED,
                        nowMinus30Min,
                        nowPlus30Min
                );

        if (activeAppointments > 0) {
            // Doctor is in clinic, find next slot after current appointment
            Instant nextSlot = findNextSlot(doctor, nowPlus30Min, today, zoneId);
            return new StatusAndSlot("In clinic", nextSlot);
        }

        // Check if doctor has availability today
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        List<DoctorAvailabilityEntity> availabilitiesToday = doctorAvailabilityRepository
                .findByDoctorIdAndRecurringWeeklyTrueAndDayOfWeek(doctor.getId(), dayOfWeek);

        // Also check for specific date availability
        List<DoctorAvailabilityEntity> specificAvailabilities = doctorAvailabilityRepository
                .findByDoctorIdAndSpecificDate(doctor.getId(), today);

        boolean hasAvailabilityToday = !availabilitiesToday.isEmpty() || !specificAvailabilities.isEmpty();

        if (hasAvailabilityToday) {
            Instant nextSlot = findNextSlot(doctor, now, today, zoneId);
            return new StatusAndSlot("Available", nextSlot);
        }

        // Find next availability in future
        Instant nextSlot = findNextSlot(doctor, now, today, zoneId);
        return new StatusAndSlot("Off duty", nextSlot);
    }

    /**
     * Find the next available slot for a doctor
     */
    private Instant findNextSlot(DoctorEntity doctor, Instant after, LocalDate startDate, ZoneId zoneId) {
        // Check next 7 days for availability
        for (int i = 0; i < 7; i++) {
            LocalDate checkDate = startDate.plusDays(i);
            DayOfWeek dayOfWeek = checkDate.getDayOfWeek();

            // Get recurring weekly availabilities
            List<DoctorAvailabilityEntity> weeklyAvailabilities = doctorAvailabilityRepository
                    .findByDoctorIdAndRecurringWeeklyTrueAndDayOfWeek(doctor.getId(), dayOfWeek);

            // Get specific date availabilities
            List<DoctorAvailabilityEntity> specificAvailabilities = doctorAvailabilityRepository
                    .findByDoctorIdAndSpecificDate(doctor.getId(), checkDate);

            // Combine both lists
            List<DoctorAvailabilityEntity> allAvailabilities = new java.util.ArrayList<>(weeklyAvailabilities);
            allAvailabilities.addAll(specificAvailabilities);

            // Find earliest slot on this date
            Optional<Instant> earliestSlot = allAvailabilities.stream()
                    .map(avail -> avail.getStartTime().atDate(checkDate).atZone(zoneId).toInstant())
                    .filter(instant -> instant.isAfter(after))
                    .min(Instant::compareTo);

            if (earliestSlot.isPresent()) {
                return earliestSlot.get();
            }
        }

        return null;
    }
}
