package com.clinic.modules.admin.service;

import com.clinic.config.ClinicTimezoneConfig;
import com.clinic.modules.admin.dto.ClinicSettingsResponse;
import com.clinic.modules.admin.dto.ClinicSettingsUpdateRequest;
import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.Locale;

@Service
public class ClinicSettingsService {

    private final ClinicSettingsRepository settingsRepository;
    private final ClinicTimezoneConfig clinicTimezoneConfig;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;

    @Value("${paypal.client-id:}")
    private String paypalClientId;

    @Value("${paypal.client-secret:}")
    private String paypalClientSecret;

    @Value("${paypal.environment:sandbox}")
    private String paypalEnvironment;

    public ClinicSettingsService(ClinicSettingsRepository settingsRepository,
                                 ClinicTimezoneConfig clinicTimezoneConfig,
                                 TenantContextHolder tenantContextHolder,
                                 TenantService tenantService) {
        this.settingsRepository = settingsRepository;
        this.clinicTimezoneConfig = clinicTimezoneConfig;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    public ClinicSettingsResponse getSettings() {
        ClinicSettingsEntity settings = loadOrCreateSettings();

        return mapToResponse(settings);
    }

    @Transactional
    public ClinicSettingsResponse updateSettings(ClinicSettingsUpdateRequest request) {
        ClinicSettingsEntity settings = loadOrCreateSettings();

        // Update basic info
        if (request.clinicName() != null) settings.setClinicName(request.clinicName());
        if (request.phone() != null) settings.setPhone(request.phone());
        if (request.email() != null) settings.setEmail(request.email());
        if (request.address() != null) settings.setAddress(request.address());
        if (request.city() != null) settings.setCity(request.city());
        if (request.state() != null) settings.setState(request.state());
        if (request.zipCode() != null) settings.setZipCode(request.zipCode());
        if (request.country() != null) settings.setCountry(request.country());
        if (request.currency() != null && !request.currency().isBlank()) {
            settings.setCurrency(request.currency().trim().toUpperCase());
        }
        if (request.locale() != null && !request.locale().isBlank()) {
            settings.setLocale(request.locale().trim());
        }

        // Update working hours
        if (request.workingHours() != null) {
            var hours = request.workingHours();
            if (hours.monday() != null) settings.setMondayHours(hours.monday());
            if (hours.tuesday() != null) settings.setTuesdayHours(hours.tuesday());
            if (hours.wednesday() != null) settings.setWednesdayHours(hours.wednesday());
            if (hours.thursday() != null) settings.setThursdayHours(hours.thursday());
            if (hours.friday() != null) settings.setFridayHours(hours.friday());
            if (hours.saturday() != null) settings.setSaturdayHours(hours.saturday());
            if (hours.sunday() != null) settings.setSundayHours(hours.sunday());
        }

        // Update logo fields
        if (request.logoUrl() != null) settings.setLogoUrl(request.logoUrl());
        if (request.logoImageId() != null) settings.setLogoImageId(request.logoImageId());

        // Update social media
        if (request.socialMedia() != null) {
            var social = request.socialMedia();
            if (social.facebook() != null) settings.setFacebookUrl(social.facebook());
            if (social.instagram() != null) settings.setInstagramUrl(social.instagram());
            if (social.twitter() != null) settings.setTwitterUrl(social.twitter());
            if (social.linkedin() != null) settings.setLinkedinUrl(social.linkedin());
        }

        // Update PayPal settings
        if (request.virtualConsultationFee() != null) {
            settings.setVirtualConsultationFee(request.virtualConsultationFee());
        }

        if (request.virtualConsultationMeetingLink() != null) {
            settings.setVirtualConsultationMeetingLink(normalize(request.virtualConsultationMeetingLink()));
        }

        if (request.slotDurationMinutes() != null) {
            settings.setSlotDurationMinutes(request.slotDurationMinutes());
        }

        // Update exchange rates
        if (request.exchangeRates() != null) {
            settings.setExchangeRates(request.exchangeRates());
        }

        // Update PayPal credentials (trim empty inputs to null)
        if (request.paypalEnvironment() != null) {
            settings.setPaypalEnvironment(normalize(request.paypalEnvironment()));
        }
        if (request.paypalClientId() != null) {
            settings.setPaypalClientId(normalize(request.paypalClientId()));
        }
        if (request.paypalClientSecret() != null) {
            settings.setPaypalClientSecret(normalize(request.paypalClientSecret()));
        }

        // Update Cloudflare settings
        if (request.cloudflareAccountId() != null) {
            settings.setCloudflareAccountId(normalize(request.cloudflareAccountId()));
        }
        if (request.cloudflareApiToken() != null) {
            settings.setCloudflareApiToken(normalize(request.cloudflareApiToken()));
        }

        ClinicSettingsEntity saved = settingsRepository.save(settings);
        return mapToResponse(saved);
    }

    private ClinicSettingsEntity loadOrCreateSettings() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultSettings(tenantId));
    }

    private ClinicSettingsEntity createDefaultSettings(Long tenantId) {
        ClinicSettingsEntity settings = new ClinicSettingsEntity("Qadri's Clinic");
        settings.setTenant(tenantService.requireTenant(tenantId));
        settings.setClinicName("Qadri's Clinic");
        settings.setPhone("+1 (555) 123-4567");
        settings.setEmail("info@qadrisclinic.com");
        settings.setMondayHours("9:00 AM - 5:00 PM");
        settings.setTuesdayHours("9:00 AM - 5:00 PM");
        settings.setWednesdayHours("9:00 AM - 5:00 PM");
        settings.setThursdayHours("9:00 AM - 5:00 PM");
        settings.setFridayHours("9:00 AM - 5:00 PM");
        settings.setSaturdayHours("10:00 AM - 2:00 PM");
        settings.setSundayHours("Closed");
        settings.setCurrency("AED");
        settings.setLocale("en-AE");
        settings.setSlotDurationMinutes(30);
        return settingsRepository.save(settings);
    }

    private ClinicSettingsResponse mapToResponse(ClinicSettingsEntity entity) {
        var workingHours = new ClinicSettingsResponse.WorkingHours(
                entity.getMondayHours(),
                entity.getTuesdayHours(),
                entity.getWednesdayHours(),
                entity.getThursdayHours(),
                entity.getFridayHours(),
                entity.getSaturdayHours(),
                entity.getSundayHours()
        );

        var socialMedia = new ClinicSettingsResponse.SocialMedia(
                entity.getFacebookUrl(),
                entity.getInstagramUrl(),
                entity.getTwitterUrl(),
                entity.getLinkedinUrl()
        );

        String resolvedPaypalEnvironment = resolveOrDefault(entity.getPaypalEnvironment(), paypalEnvironment);
        String resolvedPaypalClientId = resolveOrDefault(entity.getPaypalClientId(), paypalClientId);
        String resolvedPaypalClientSecret = resolveOrDefault(entity.getPaypalClientSecret(), paypalClientSecret);

        return new ClinicSettingsResponse(
                entity.getId(),
                entity.getClinicName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getCity(),
                entity.getState(),
                entity.getZipCode(),
                entity.getCountry(),
                entity.getCurrency(),
                entity.getLocale(),
                resolveCurrencySymbol(entity.getCurrency(), entity.getLocale()),
                entity.getLogoUrl(),
                entity.getLogoImageId(),
                workingHours,
                socialMedia,
                entity.getVirtualConsultationFee(),
                entity.getVirtualConsultationMeetingLink(),
                entity.getSlotDurationMinutes(),
                resolvedPaypalEnvironment,
                resolvedPaypalClientId,
                resolvedPaypalClientSecret,
                entity.getExchangeRates(),
                clinicTimezoneConfig.getZoneId(),
                entity.getCloudflareAccountId(),
                entity.getCloudflareApiToken()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveOrDefault(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return (fallback != null && !fallback.isBlank()) ? fallback : null;
    }

    private String resolveCurrencySymbol(String currencyCode, String localeTag) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return "";
        }
        try {
            Currency currency = Currency.getInstance(currencyCode);
            if (localeTag != null && !localeTag.isBlank()) {
                Locale locale = Locale.forLanguageTag(localeTag);
                String symbol = currency.getSymbol(locale);
                if (symbol != null && !symbol.isBlank()) {
                    return symbol;
                }
            }
            return currency.getSymbol();
        } catch (IllegalArgumentException ex) {
            return currencyCode;
        }
    }
}
