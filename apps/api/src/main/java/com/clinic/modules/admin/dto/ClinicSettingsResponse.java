package com.clinic.modules.admin.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ClinicSettingsResponse(
        Long id,
        String clinicName,
        String phone,
        String email,
        String address,
        String city,
        String state,
        String zipCode,
        String country,
        String currency,
        String locale,
        String currencySymbol,
        String logoUrl,
        String logoImageId,
        WorkingHours workingHours,
        SocialMedia socialMedia,
        BigDecimal virtualConsultationFee,
        String virtualConsultationMeetingLink,
        Integer slotDurationMinutes,
        String paypalEnvironment,
        String paypalClientId,
        String paypalClientSecret,
        Map<String, BigDecimal> exchangeRates,
        String timezone,
        String cloudflareAccountId,
        String cloudflareApiToken,
        String sendgridApiKey,
        String emailFrom,
        String emailFromName,
        Boolean emailEnabled
) {
    public record WorkingHours(
            String monday,
            String tuesday,
            String wednesday,
            String thursday,
            String friday,
            String saturday,
            String sunday
    ) {}

    public record SocialMedia(
            String facebook,
            String instagram,
            String twitter,
            String linkedin
    ) {}
}
