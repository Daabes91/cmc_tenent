package com.clinic.modules.core.settings;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "clinic_settings")
public class ClinicSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "clinic_name", length = 200)
    private String clinicName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "AED";

    @Column(name = "locale", nullable = false, length = 20)
    private String locale = "en-AE";

    // Working Hours (stored as JSON or individual fields)
    @Column(name = "monday_hours", length = 50)
    private String mondayHours;

    @Column(name = "tuesday_hours", length = 50)
    private String tuesdayHours;

    @Column(name = "wednesday_hours", length = 50)
    private String wednesdayHours;

    @Column(name = "thursday_hours", length = 50)
    private String thursdayHours;

    @Column(name = "friday_hours", length = 50)
    private String fridayHours;

    @Column(name = "saturday_hours", length = 50)
    private String saturdayHours;

    @Column(name = "sunday_hours", length = 50)
    private String sundayHours;

    // Social Media Links
    @Column(name = "facebook_url", length = 300)
    private String facebookUrl;

    @Column(name = "instagram_url", length = 300)
    private String instagramUrl;

    @Column(name = "twitter_url", length = 300)
    private String twitterUrl;

    @Column(name = "linkedin_url", length = 300)
    private String linkedinUrl;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "logo_image_id", length = 100)
    private String logoImageId;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    @Column(name = "favicon_image_id", length = 100)
    private String faviconImageId;

    // PayPal Settings - Only business settings, not credentials
    @Column(name = "virtual_consultation_fee", precision = 10, scale = 2)
    private java.math.BigDecimal virtualConsultationFee;

    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes = 30;

    @Column(name = "virtual_consultation_meeting_link", length = 500)
    private String virtualConsultationMeetingLink;

    // Exchange rates to USD (e.g., {"JOD": 0.709, "AED": 0.272, "USD": 1.0})
    // Multiply by this rate to convert TO USD
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exchange_rates", columnDefinition = "jsonb")
    private Map<String, BigDecimal> exchangeRates = new HashMap<>();

    // Clinic timezone (IANA identifier, e.g., "Asia/Amman")
    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Amman";

    // Cloudflare Images Configuration
    // SendGrid Email Configuration
    @Column(name = "sendgrid_api_key", length = 500)
    private String sendgridApiKey;

    @Column(name = "email_from", length = 150)
    private String emailFrom;

    @Column(name = "email_from_name", length = 150)
    private String emailFromName;

    @Column(name = "email_enabled")
    private Boolean emailEnabled = Boolean.TRUE;

    @Column(name = "reminder_enabled")
    private Boolean reminderEnabled = Boolean.FALSE;

    @Column(name = "reminder_hours_before")
    private Integer reminderHoursBefore = 24;

    // PayPal Credentials Configuration
    @Column(name = "paypal_client_id", length = 200)
    private String paypalClientId;

    @Column(name = "paypal_client_secret", length = 500)
    private String paypalClientSecret;

    @Column(name = "paypal_environment", length = 20)
    private String paypalEnvironment;

    // Hero Media Configuration
    @Column(name = "hero_media_type", length = 10)
    private String heroMediaType = "image";

    @Column(name = "hero_image_url", columnDefinition = "TEXT")
    private String heroImageUrl;

    @Column(name = "hero_video_id", length = 20)
    private String heroVideoId;

    @Column(name = "why_choose_title_en", columnDefinition = "TEXT")
    private String whyChooseTitleEn;

    @Column(name = "why_choose_title_ar", columnDefinition = "TEXT")
    private String whyChooseTitleAr;

    @Column(name = "why_choose_subtitle_en", columnDefinition = "TEXT")
    private String whyChooseSubtitleEn;

    @Column(name = "why_choose_subtitle_ar", columnDefinition = "TEXT")
    private String whyChooseSubtitleAr;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "why_choose_features", columnDefinition = "jsonb")
    private List<WhyChooseFeatureConfig> whyChooseFeatures = new ArrayList<>();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ClinicSettingsEntity() {
    }

    public ClinicSettingsEntity(String clinicName) {
        this.clinicName = clinicName;
    }

    @PrePersist
    public void onCreate() {
        if (this.slotDurationMinutes == null) {
            this.slotDurationMinutes = 30;
        }
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        if (this.slotDurationMinutes == null) {
            this.slotDurationMinutes = 30;
        }
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMondayHours() {
        return mondayHours;
    }

    public void setMondayHours(String mondayHours) {
        this.mondayHours = mondayHours;
    }

    public String getTuesdayHours() {
        return tuesdayHours;
    }

    public void setTuesdayHours(String tuesdayHours) {
        this.tuesdayHours = tuesdayHours;
    }

    public String getWednesdayHours() {
        return wednesdayHours;
    }

    public void setWednesdayHours(String wednesdayHours) {
        this.wednesdayHours = wednesdayHours;
    }

    public String getThursdayHours() {
        return thursdayHours;
    }

    public void setThursdayHours(String thursdayHours) {
        this.thursdayHours = thursdayHours;
    }

    public String getFridayHours() {
        return fridayHours;
    }

    public void setFridayHours(String fridayHours) {
        this.fridayHours = fridayHours;
    }

    public String getSaturdayHours() {
        return saturdayHours;
    }

    public void setSaturdayHours(String saturdayHours) {
        this.saturdayHours = saturdayHours;
    }

    public String getSundayHours() {
        return sundayHours;
    }

    public void setSundayHours(String sundayHours) {
        this.sundayHours = sundayHours;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLogoImageId() {
        return logoImageId;
    }

    public void setLogoImageId(String logoImageId) {
        this.logoImageId = logoImageId;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public String getFaviconImageId() {
        return faviconImageId;
    }

    public void setFaviconImageId(String faviconImageId) {
        this.faviconImageId = faviconImageId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public java.math.BigDecimal getVirtualConsultationFee() {
        return virtualConsultationFee;
    }

    public void setVirtualConsultationFee(java.math.BigDecimal virtualConsultationFee) {
        this.virtualConsultationFee = virtualConsultationFee;
    }

    public Integer getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public String getVirtualConsultationMeetingLink() {
        return virtualConsultationMeetingLink;
    }

    public void setVirtualConsultationMeetingLink(String virtualConsultationMeetingLink) {
        this.virtualConsultationMeetingLink = virtualConsultationMeetingLink;
    }

    public Map<String, BigDecimal> getExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(Map<String, BigDecimal> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getSendgridApiKey() {
        return sendgridApiKey;
    }

    public void setSendgridApiKey(String sendgridApiKey) {
        this.sendgridApiKey = sendgridApiKey;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailFromName() {
        return emailFromName;
    }

    public void setEmailFromName(String emailFromName) {
        this.emailFromName = emailFromName;
    }

    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public Boolean getReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(Boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public Integer getReminderHoursBefore() {
        return reminderHoursBefore;
    }

    public void setReminderHoursBefore(Integer reminderHoursBefore) {
        this.reminderHoursBefore = reminderHoursBefore;
    }

    public String getPaypalClientId() {
        return paypalClientId;
    }

    public void setPaypalClientId(String paypalClientId) {
        this.paypalClientId = paypalClientId;
    }

    public String getPaypalClientSecret() {
        return paypalClientSecret;
    }

    public void setPaypalClientSecret(String paypalClientSecret) {
        this.paypalClientSecret = paypalClientSecret;
    }

    public String getPaypalEnvironment() {
        return paypalEnvironment;
    }

    public void setPaypalEnvironment(String paypalEnvironment) {
        this.paypalEnvironment = paypalEnvironment;
    }

    public String getHeroMediaType() {
        return heroMediaType;
    }

    public void setHeroMediaType(String heroMediaType) {
        this.heroMediaType = heroMediaType;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }

    public String getHeroVideoId() {
        return heroVideoId;
    }

    public void setHeroVideoId(String heroVideoId) {
        this.heroVideoId = heroVideoId;
    }

    public String getWhyChooseTitleEn() {
        return whyChooseTitleEn;
    }

    public void setWhyChooseTitleEn(String whyChooseTitleEn) {
        this.whyChooseTitleEn = whyChooseTitleEn;
    }

    public String getWhyChooseTitleAr() {
        return whyChooseTitleAr;
    }

    public void setWhyChooseTitleAr(String whyChooseTitleAr) {
        this.whyChooseTitleAr = whyChooseTitleAr;
    }

    public String getWhyChooseSubtitleEn() {
        return whyChooseSubtitleEn;
    }

    public void setWhyChooseSubtitleEn(String whyChooseSubtitleEn) {
        this.whyChooseSubtitleEn = whyChooseSubtitleEn;
    }

    public String getWhyChooseSubtitleAr() {
        return whyChooseSubtitleAr;
    }

    public void setWhyChooseSubtitleAr(String whyChooseSubtitleAr) {
        this.whyChooseSubtitleAr = whyChooseSubtitleAr;
    }

    public List<WhyChooseFeatureConfig> getWhyChooseFeatures() {
        return whyChooseFeatures;
    }

    public void setWhyChooseFeatures(List<WhyChooseFeatureConfig> whyChooseFeatures) {
        this.whyChooseFeatures = whyChooseFeatures;
    }

    public static class WhyChooseFeatureConfig {
        private String key;
        private String icon;
        private String titleEn;
        private String titleAr;
        private String descriptionEn;
        private String descriptionAr;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getTitleEn() {
            return titleEn;
        }

        public void setTitleEn(String titleEn) {
            this.titleEn = titleEn;
        }

        public String getTitleAr() {
            return titleAr;
        }

        public void setTitleAr(String titleAr) {
            this.titleAr = titleAr;
        }

        public String getDescriptionEn() {
            return descriptionEn;
        }

        public void setDescriptionEn(String descriptionEn) {
            this.descriptionEn = descriptionEn;
        }

        public String getDescriptionAr() {
            return descriptionAr;
        }

        public void setDescriptionAr(String descriptionAr) {
            this.descriptionAr = descriptionAr;
        }
    }
}
