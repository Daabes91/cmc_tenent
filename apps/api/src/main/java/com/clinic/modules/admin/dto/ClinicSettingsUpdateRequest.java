package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.Map;

public record ClinicSettingsUpdateRequest(
        String clinicName,
        String phone,
        @Email String email,
        String address,
        String city,
        String state,
        String zipCode,
        String country,
        String currency,
        String locale,
        String logoUrl,
        String logoImageId,
        WorkingHoursUpdate workingHours,
        SocialMediaUpdate socialMedia,
        BigDecimal virtualConsultationFee,
        String virtualConsultationMeetingLink,
        @Min(5) @Max(240) Integer slotDurationMinutes,
        Map<String, BigDecimal> exchangeRates,
        String paypalEnvironment,
        String paypalClientId,
        String paypalClientSecret,
        String cloudflareAccountId,
        String cloudflareApiToken,
        String sendgridApiKey,
        @Email String emailFrom,
        String emailFromName,
        Boolean emailEnabled
) {
    public record WorkingHoursUpdate(
            String monday,
            String tuesday,
            String wednesday,
            String thursday,
            String friday,
            String saturday,
            String sunday
    ) {}

    public record SocialMediaUpdate(
            String facebook,
            String instagram,
            String twitter,
            String linkedin
    ) {}
}
