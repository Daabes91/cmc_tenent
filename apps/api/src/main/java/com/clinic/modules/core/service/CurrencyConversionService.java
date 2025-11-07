package com.clinic.modules.core.service;

import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Service for converting between currencies using exchange rates.
 * Rates are stored relative to USD as the base currency.
 */
@Service
public class CurrencyConversionService {

    private final ClinicSettingsRepository clinicSettingsRepository;
    private final TenantContextHolder tenantContextHolder;

    public CurrencyConversionService(ClinicSettingsRepository clinicSettingsRepository,
                                     TenantContextHolder tenantContextHolder) {
        this.clinicSettingsRepository = clinicSettingsRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }

        Map<String, BigDecimal> rates = getExchangeRates();
        BigDecimal fromRate = rates.getOrDefault(fromCurrency, BigDecimal.ONE);
        BigDecimal toRate = rates.getOrDefault(toCurrency, BigDecimal.ONE);

        BigDecimal converted = amount
                .multiply(fromRate)
                .divide(toRate, 4, RoundingMode.HALF_UP);
        return converted.setScale(2, RoundingMode.HALF_UP);
    }

    public String getClinicCurrency() {
        return requireSettings().getCurrency();
    }

    public Map<String, BigDecimal> getExchangeRates() {
        Map<String, BigDecimal> rates = requireSettings().getExchangeRates();
        if (rates == null || rates.isEmpty()) {
            return getDefaultRates();
        }
        return rates;
    }

    public void updateExchangeRates(Map<String, BigDecimal> rates) {
        ClinicSettingsEntity settings = requireSettings();
        settings.setExchangeRates(rates);
        clinicSettingsRepository.save(settings);
    }

    public BigDecimal getRate(String currencyCode) {
        return getExchangeRates().getOrDefault(currencyCode, BigDecimal.ONE);
    }

    private ClinicSettingsEntity requireSettings() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return clinicSettingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic settings not configured"));
    }

    private Map<String, BigDecimal> getDefaultRates() {
        return Map.of(
                "USD", new BigDecimal("1.0"),
                "JOD", new BigDecimal("0.709"),
                "AED", new BigDecimal("0.272"),
                "SAR", new BigDecimal("0.267"),
                "QAR", new BigDecimal("0.275"),
                "OMR", new BigDecimal("2.597"),
                "KWD", new BigDecimal("3.261"),
                "EUR", new BigDecimal("1.085"),
                "GBP", new BigDecimal("1.267")
        );
    }
}
