package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.CategoryBreakdown;
import com.clinic.modules.admin.dto.CategoryExpense;
import com.clinic.modules.admin.dto.ReportMetrics;
import com.clinic.modules.admin.dto.RevenuePoint;
import com.clinic.modules.admin.dto.TimeSeriesPoint;
import com.clinic.modules.admin.util.DateRange;
import com.clinic.modules.core.finance.CategoryExpenseAggregation;
import com.clinic.modules.core.finance.FinanceAggregationService;
import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.appointment.AppointmentStatus;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.treatment.FollowUpVisitRepository;
import com.clinic.modules.core.treatment.PaymentEntity;
import com.clinic.modules.core.treatment.PaymentRepository;
import com.clinic.modules.core.treatment.TreatmentPlanPaymentEntity;
import com.clinic.modules.core.treatment.TreatmentPlanPaymentRepository;
import com.clinic.modules.core.treatment.TreatmentPlanRepository;
import com.clinic.modules.core.treatment.TreatmentPlanStatus;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final FollowUpVisitRepository followUpVisitRepository;
    private final PaymentRepository paymentRepository;
    private final TreatmentPlanPaymentRepository treatmentPlanPaymentRepository;
    private final FinanceAggregationService financeAggregationService;
    private final TenantContextHolder tenantContextHolder;

    public ReportService(AppointmentRepository appointmentRepository,
                         PatientRepository patientRepository,
                         DoctorRepository doctorRepository,
                         TreatmentPlanRepository treatmentPlanRepository,
                         FollowUpVisitRepository followUpVisitRepository,
                         PaymentRepository paymentRepository,
                         TreatmentPlanPaymentRepository treatmentPlanPaymentRepository,
                         FinanceAggregationService financeAggregationService,
                         TenantContextHolder tenantContextHolder) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.followUpVisitRepository = followUpVisitRepository;
        this.paymentRepository = paymentRepository;
        this.treatmentPlanPaymentRepository = treatmentPlanPaymentRepository;
        this.financeAggregationService = financeAggregationService;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public ReportMetrics getOverallMetrics(ZoneId zoneId) {
        // Use current month as default date range
        return getOverallMetrics(DateRange.currentMonth(), zoneId);
    }

    @Transactional(readOnly = true)
    public ReportMetrics getOverallMetrics(DateRange dateRange, ZoneId zoneId) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        Instant now = Instant.now();
        ZonedDateTime zonedNow = now.atZone(zoneId);

        // Convert DateRange to Instants for filtering
        Instant rangeStart = dateRange.getStartDate().atStartOfDay(zoneId).toInstant();
        Instant rangeEnd = dateRange.getEndDate().plusDays(1).atStartOfDay(zoneId).toInstant();

        Instant startOfToday = zonedNow.truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant endOfToday = zonedNow.plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant startOfWeek = zonedNow.minusDays(zonedNow.getDayOfWeek().getValue() - 1)
                .truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant startOfMonth = zonedNow.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant startOfNextMonth = zonedNow.plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant();

        YearMonth currentMonth = YearMonth.from(zonedNow);
        YearMonth baselineMonth = currentMonth.minusMonths(5);
        Instant startOfSixMonthsWindow = baselineMonth.atDay(1).atStartOfDay(zoneId).toInstant();

        LocalDate today = zonedNow.toLocalDate();
        LocalDate trendStartDate = today.minusDays(29);

        // Use optimized database query to filter appointments by date range and tenant
        List<AppointmentEntity> allAppointments = appointmentRepository.findByTenantIdAndDateRange(tenantId, rangeStart, rangeEnd);

        long totalAppointments = allAppointments.size();
        long totalPatients = patientRepository.countByTenantId(tenantId);
        long totalDoctors = doctorRepository.countByTenantId(tenantId);

        long todayAppointments = allAppointments.stream()
                .filter(a -> a.getCreatedAt().isAfter(startOfToday))
                .count();

        long thisWeekAppointments = allAppointments.stream()
                .filter(a -> a.getCreatedAt().isAfter(startOfWeek))
                .count();

        long thisMonthAppointments = allAppointments.stream()
                .filter(a -> a.getCreatedAt().isAfter(startOfMonth))
                .count();

        Map<String, Long> rawStatusCounts = allAppointments.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));

        long scheduledAppointments = rawStatusCounts.getOrDefault(AppointmentStatus.SCHEDULED.name(), 0L)
                + rawStatusCounts.getOrDefault(AppointmentStatus.CONFIRMED.name(), 0L);
        long completedAppointments = rawStatusCounts.getOrDefault(AppointmentStatus.COMPLETED.name(), 0L);
        long cancelledAppointments = rawStatusCounts.getOrDefault(AppointmentStatus.CANCELLED.name(), 0L);

        Map<String, Long> appointmentsByStatus = new LinkedHashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            appointmentsByStatus.put(status.name(), rawStatusCounts.getOrDefault(status.name(), 0L));
        }
        rawStatusCounts.entrySet().stream()
                .filter(entry -> !appointmentsByStatus.containsKey(entry.getKey()))
                .forEach(entry -> appointmentsByStatus.put(entry.getKey(), entry.getValue()));

        List<CategoryBreakdown> doctorBreakdown = allAppointments.stream()
                .filter(appointment -> appointment.getDoctor() != null)
                .collect(Collectors.groupingBy(
                        appointment -> defaultString(appointment.getDoctor().getFullNameEn(), "Unassigned"),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> new CategoryBreakdown(
                        entry.getKey(),
                        entry.getValue(),
                        percentage(entry.getValue(), totalAppointments)
                ))
                .toList();

        List<CategoryBreakdown> serviceBreakdown = allAppointments.stream()
                .filter(appointment -> appointment.getService() != null)
                .collect(Collectors.groupingBy(
                        appointment -> defaultString(appointment.getService().getNameEn(), "Other"),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> new CategoryBreakdown(
                        entry.getKey(),
                        entry.getValue(),
                        percentage(entry.getValue(), totalAppointments)
                ))
                .toList();

        // Use optimized database queries for payment counts
        long paymentsCollectedCount = appointmentRepository.countByTenantIdAndPaymentCollectedByDateRange(tenantId, rangeStart, rangeEnd);
        long paymentsOutstandingCount = appointmentRepository.countByTenantIdAndPaymentOutstandingByDateRange(tenantId, rangeStart, rangeEnd);

        // Use optimized query for new patients within the date range
        long newPatientsThisMonth = patientRepository.countByTenantIdAndCreatedAtBetween(tenantId, rangeStart, rangeEnd);

        List<PaymentEntity> recentPayments = paymentRepository.findByTenantIdAndPaymentDateBetween(
                tenantId,
                startOfSixMonthsWindow,
                endOfToday
        );
        List<AppointmentEntity> standalonePayments = allAppointments.stream()
                .filter(AppointmentEntity::isPaymentCollected)
                .filter(appointment -> appointment.getPaymentAmount() != null)
                .filter(appointment -> {
                    Instant paymentInstant = resolvePaymentInstant(appointment);
                    return !paymentInstant.isBefore(startOfSixMonthsWindow) && paymentInstant.isBefore(endOfToday);
                })
                .toList();

        // Fetch direct treatment plan payments (remaining balance payments)
        List<TreatmentPlanPaymentEntity> directPayments = treatmentPlanPaymentRepository.findByTenantIdAndPaymentDateBetween(
                tenantId,
                startOfSixMonthsWindow,
                endOfToday
        );

        // Multi-currency revenue tracking by month and currency
        Map<String, Map<YearMonth, BigDecimal>> revenueByCurrencyAndMonth = new HashMap<>();

        // Initialize revenue maps for each currency
        recentPayments.forEach(payment -> {
            String currency = payment.getCurrency() != null ? payment.getCurrency() : "USD";
            revenueByCurrencyAndMonth.putIfAbsent(currency, new LinkedHashMap<>());
        });
        standalonePayments.forEach(appointment -> {
            String currency = appointment.getPaymentCurrency() != null ? appointment.getPaymentCurrency() : "USD";
            revenueByCurrencyAndMonth.putIfAbsent(currency, new LinkedHashMap<>());
        });
        directPayments.forEach(payment -> {
            String currency = payment.getCurrency() != null ? payment.getCurrency() : "USD";
            revenueByCurrencyAndMonth.putIfAbsent(currency, new LinkedHashMap<>());
        });

        // Initialize months for each currency
        revenueByCurrencyAndMonth.values().forEach(currencyMap -> {
            for (int i = 0; i < 6; i++) {
                YearMonth month = baselineMonth.plusMonths(i);
                currencyMap.put(month, BigDecimal.ZERO);
            }
        });

        // Aggregate revenue by currency and month from treatment payments
        recentPayments.forEach(payment -> {
            String currency = payment.getCurrency() != null ? payment.getCurrency() : "USD";
            YearMonth paymentMonth = YearMonth.from(payment.getPaymentDate().atZone(zoneId));
            Map<YearMonth, BigDecimal> currencyRevenue = revenueByCurrencyAndMonth.get(currency);
            if (currencyRevenue != null) {
                currencyRevenue.computeIfPresent(paymentMonth, (__, amount) -> amount.add(payment.getAmount()));
            }
        });

        // Aggregate revenue by currency and month from standalone appointment payments
        standalonePayments.forEach(appointment -> {
            String currency = appointment.getPaymentCurrency() != null ? appointment.getPaymentCurrency() : "USD";
            Instant paymentInstant = resolvePaymentInstant(appointment);
            YearMonth paymentMonth = YearMonth.from(paymentInstant.atZone(zoneId));
            Map<YearMonth, BigDecimal> currencyRevenue = revenueByCurrencyAndMonth.get(currency);
            if (currencyRevenue != null) {
                currencyRevenue.computeIfPresent(paymentMonth, (__, amount) -> amount.add(appointment.getPaymentAmount()));
            }
        });

        // Aggregate revenue by currency and month from direct treatment plan payments
        directPayments.forEach(payment -> {
            String currency = payment.getCurrency() != null ? payment.getCurrency() : "USD";
            YearMonth paymentMonth = YearMonth.from(payment.getPaymentDate().atZone(zoneId));
            Map<YearMonth, BigDecimal> currencyRevenue = revenueByCurrencyAndMonth.get(currency);
            if (currencyRevenue != null) {
                currencyRevenue.computeIfPresent(paymentMonth, (__, amount) -> amount.add(payment.getAmount()));
            }
        });

        // Build revenue trend by currency
        Map<String, List<RevenuePoint>> revenueTrendByCurrency = new HashMap<>();
        revenueByCurrencyAndMonth.forEach((currency, monthlyRevenue) -> {
            List<RevenuePoint> currencyTrend = monthlyRevenue.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> new RevenuePoint(
                            entry.getKey().format(MONTH_FORMATTER),
                            entry.getValue()
                    ))
                    .toList();
            revenueTrendByCurrency.put(currency, currencyTrend);
        });

        // Calculate revenue this month and last month by currency
        Map<String, BigDecimal> revenueThisMonthByCurrency = new HashMap<>();
        Map<String, BigDecimal> revenueLastMonthByCurrency = new HashMap<>();

        revenueByCurrencyAndMonth.forEach((currency, monthlyRevenue) -> {
            revenueThisMonthByCurrency.put(currency, monthlyRevenue.getOrDefault(currentMonth, BigDecimal.ZERO));
            revenueLastMonthByCurrency.put(currency, monthlyRevenue.getOrDefault(currentMonth.minusMonths(1), BigDecimal.ZERO));
        });

        // For backwards compatibility, sum all revenue (deprecated - use currency-specific maps)
        Map<YearMonth, BigDecimal> revenueByMonth = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
            YearMonth month = baselineMonth.plusMonths(i);
            revenueByMonth.put(month, BigDecimal.ZERO);
        }
        revenueByCurrencyAndMonth.values().forEach(currencyMap -> {
            currencyMap.forEach((month, amount) -> {
                revenueByMonth.computeIfPresent(month, (__, total) -> total.add(amount));
            });
        });

        List<RevenuePoint> revenueTrend = revenueByMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new RevenuePoint(
                        entry.getKey().format(MONTH_FORMATTER),
                        entry.getValue()
                ))
                .toList();

        BigDecimal revenueThisMonth = revenueByMonth.getOrDefault(currentMonth, BigDecimal.ZERO);
        BigDecimal revenueLastMonth = revenueByMonth.getOrDefault(currentMonth.minusMonths(1), BigDecimal.ZERO);

        Map<String, Long> paymentMethodCounts = new HashMap<>();
        recentPayments.forEach(payment ->
                paymentMethodCounts.merge(payment.getPaymentMethod().name(), 1L, Long::sum)
        );
        long standalonePaymentsWithMethod = 0L;
        for (AppointmentEntity appointment : standalonePayments) {
            if (appointment.getPaymentMethod() != null) {
                paymentMethodCounts.merge(appointment.getPaymentMethod().name(), 1L, Long::sum);
                standalonePaymentsWithMethod++;
            }
        }
        // Include direct treatment plan payments in payment method breakdown
        directPayments.forEach(payment ->
                paymentMethodCounts.merge(payment.getPaymentMethod().name(), 1L, Long::sum)
        );
        long totalPayments = recentPayments.size() + standalonePaymentsWithMethod + directPayments.size();

        List<CategoryBreakdown> paymentMethodBreakdown = paymentMethodCounts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new CategoryBreakdown(
                        entry.getKey(),
                        entry.getValue(),
                        percentage(entry.getValue(), totalPayments)
                ))
                .toList();

        Map<LocalDate, Long> dailyCounts = new LinkedHashMap<>();
        for (LocalDate date = trendStartDate; !date.isAfter(today); date = date.plusDays(1)) {
            dailyCounts.put(date, 0L);
        }

        allAppointments.stream()
                .map(AppointmentEntity::getScheduledAt)
                .filter(instant -> instant != null)
                .map(instant -> instant.atZone(zoneId).toLocalDate())
                .filter(date -> !date.isBefore(trendStartDate) && !date.isAfter(today))
                .forEach(date -> dailyCounts.computeIfPresent(date, (__, count) -> count + 1));

        List<TimeSeriesPoint> appointmentTrend = dailyCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new TimeSeriesPoint(
                        entry.getKey().format(DAY_FORMATTER),
                        entry.getValue()
                ))
                .toList();

        double averageDailyAppointments = dailyCounts.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0d);

        long attendanceRecords = allAppointments.stream()
                .filter(appointment -> appointment.getPatientAttended() != null)
                .count();
        long noShows = allAppointments.stream()
                .filter(appointment -> Boolean.FALSE.equals(appointment.getPatientAttended()))
                .count();

        double noShowRate = attendanceRecords == 0 ? 0d : percentage(noShows, attendanceRecords);
        double collectionRate = percentage(paymentsCollectedCount, totalAppointments);

        long followUpVisitsThisMonth = followUpVisitRepository.countByTenantIdAndVisitDateBetween(tenantId, startOfMonth, startOfNextMonth);
        long activeTreatmentPlans = treatmentPlanRepository.countByTenantIdAndStatus(tenantId, TreatmentPlanStatus.IN_PROGRESS);

        // Get expense data for the date range
        BigDecimal totalExpenses = financeAggregationService.getTotalExpenses(
                tenantId,
                dateRange.getStartDate(),
                dateRange.getEndDate()
        );

        List<CategoryExpenseAggregation> expenseAggregations = financeAggregationService.getExpensesByCategory(
                tenantId,
                dateRange.getStartDate(),
                dateRange.getEndDate()
        );

        // Convert CategoryExpenseAggregation to CategoryExpense DTO
        List<CategoryExpense> expensesByCategory = expenseAggregations.stream()
                .map(agg -> new CategoryExpense(agg.getCategoryName(), agg.getTotalAmount()))
                .toList();

        return new ReportMetrics(
                totalAppointments,
                todayAppointments,
                thisWeekAppointments,
                thisMonthAppointments,
                scheduledAppointments,
                completedAppointments,
                cancelledAppointments,
                totalPatients,
                newPatientsThisMonth,
                totalDoctors,
                paymentsCollectedCount,
                paymentsOutstandingCount,
                appointmentsByStatus,
                doctorBreakdown,
                serviceBreakdown,
                paymentMethodBreakdown,
                appointmentTrend,
                revenueTrend,
                roundTwoDecimals(averageDailyAppointments),
                roundOneDecimal(noShowRate),
                roundOneDecimal(collectionRate),
                followUpVisitsThisMonth,
                activeTreatmentPlans,
                revenueThisMonth,
                revenueLastMonth,
                // Multi-currency support
                revenueThisMonthByCurrency,
                revenueLastMonthByCurrency,
                revenueTrendByCurrency,
                // Expense data
                totalExpenses,
                expensesByCategory
        );
    }

    private static String defaultString(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static Instant resolvePaymentInstant(AppointmentEntity appointment) {
        Instant paymentDate = appointment.getPaymentDate();
        if (paymentDate != null) {
            return paymentDate;
        }
        Instant scheduledAt = appointment.getScheduledAt();
        if (scheduledAt != null) {
            return scheduledAt;
        }
        return appointment.getCreatedAt();
    }

    private static double percentage(long part, long total) {
        if (total <= 0) {
            return 0d;
        }
        double ratio = (double) part / (double) total * 100d;
        return roundOneDecimal(ratio);
    }

    private static double roundOneDecimal(double value) {
        return Math.round(value * 10d) / 10d;
    }

    private static double roundTwoDecimals(double value) {
        return Math.round(value * 100d) / 100d;
    }
}
