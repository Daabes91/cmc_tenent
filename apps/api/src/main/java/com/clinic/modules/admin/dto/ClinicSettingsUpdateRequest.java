package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;
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
        String faviconUrl,
        String faviconImageId,
        WorkingHoursUpdate workingHours,
        SocialMediaUpdate socialMedia,
        BigDecimal virtualConsultationFee,
        String virtualConsultationMeetingLink,
        @Min(5) @Max(240) Integer slotDurationMinutes,
        Map<String, BigDecimal> exchangeRates,
        String paypalEnvironment,
        String paypalClientId,
        String paypalClientSecret,
        String sendgridApiKey,
        String emailFrom,
        String emailFromName,
        Boolean emailEnabled,
        Boolean reminderEnabled,
        Integer reminderHoursBefore,
        String heroMediaType,
        String heroImageUrl,
        String heroVideoId,
        WhyChooseUpdate whyChoose,
        Boolean ecommerceEnabled
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

    public record LocalizedTextUpdate(
            String en,
            String ar
    ) {}

    public record WhyChooseFeatureUpdate(
            String key,
            String icon,
            LocalizedTextUpdate title,
            LocalizedTextUpdate description
    ) {}

    public record WhyChooseUpdate(
            LocalizedTextUpdate title,
            LocalizedTextUpdate subtitle,
            List<WhyChooseFeatureUpdate> features
    ) {}
}
