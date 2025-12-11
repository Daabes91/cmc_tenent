package com.clinic.modules.admin.service;

import com.clinic.config.ClinicTimezoneConfig;
import com.clinic.modules.admin.dto.ClinicSettingsResponse;
import com.clinic.modules.admin.dto.ClinicSettingsUpdateRequest;
import com.clinic.modules.core.settings.ClinicSettingsEntity;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.ecommerce.service.EcommerceFeatureService;
import com.clinic.util.YouTubeUrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

@Service
public class ClinicSettingsService {

    private final ClinicSettingsRepository settingsRepository;
    private final ClinicTimezoneConfig clinicTimezoneConfig;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;
    private final EcommerceFeatureService ecommerceFeatureService;

    @Value("${paypal.client-id:}")
    private String paypalClientId;

    @Value("${paypal.client-secret:}")
    private String paypalClientSecret;

    @Value("${paypal.environment:sandbox}")
    private String paypalEnvironment;

    @Value("${security.email.sendgrid-api-key:}")
    private String defaultSendgridApiKey;

    @Value("${security.email.from-email:}")
    private String defaultEmailFrom;

    @Value("${security.email.from-name:Clinic}")
    private String defaultEmailFromName;

    @Value("${security.email.enabled:true}")
    private Boolean defaultEmailEnabled;

    public ClinicSettingsService(ClinicSettingsRepository settingsRepository,
                                 ClinicTimezoneConfig clinicTimezoneConfig,
                                 TenantContextHolder tenantContextHolder,
                                 TenantService tenantService,
                                 EcommerceFeatureService ecommerceFeatureService) {
        this.settingsRepository = settingsRepository;
        this.clinicTimezoneConfig = clinicTimezoneConfig;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
        this.ecommerceFeatureService = ecommerceFeatureService;
    }

    public ClinicSettingsResponse getSettings() {
        ClinicSettingsEntity settings = loadOrCreateSettings();
        Long tenantId = tenantContextHolder.requireTenantId();
        boolean ecommerceEnabled = ecommerceFeatureService.isEcommerceEnabled(tenantId);

        return mapToResponse(settings, ecommerceEnabled);
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
        if (request.faviconUrl() != null) settings.setFaviconUrl(request.faviconUrl());
        if (request.faviconImageId() != null) settings.setFaviconImageId(request.faviconImageId());

        // Update social media
        if (request.socialMedia() != null) {
            var social = request.socialMedia();
            if (social.facebook() != null) settings.setFacebookUrl(social.facebook());
            if (social.instagram() != null) settings.setInstagramUrl(social.instagram());
            if (social.twitter() != null) settings.setTwitterUrl(social.twitter());
            if (social.linkedin() != null) settings.setLinkedinUrl(social.linkedin());
        }

        // Update SendGrid / email settings
        if (request.sendgridApiKey() != null) {
            settings.setSendgridApiKey(normalize(request.sendgridApiKey()));
        }
        if (request.emailFrom() != null) {
            settings.setEmailFrom(normalize(request.emailFrom()));
        }
        if (request.emailFromName() != null) {
            settings.setEmailFromName(normalize(request.emailFromName()));
        }
        if (request.emailEnabled() != null) {
            settings.setEmailEnabled(request.emailEnabled());
        }
        if (request.reminderEnabled() != null) {
            settings.setReminderEnabled(request.reminderEnabled());
        }
        if (request.reminderHoursBefore() != null) {
            settings.setReminderHoursBefore(request.reminderHoursBefore());
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

        // Update hero media settings
        if (request.heroMediaType() != null) {
            String mediaType = normalize(request.heroMediaType());
            if (mediaType != null && (mediaType.equals("image") || mediaType.equals("video"))) {
                settings.setHeroMediaType(mediaType);
            }
        }
        if (request.heroImageUrl() != null) {
            settings.setHeroImageUrl(normalize(request.heroImageUrl()));
        }
        if (request.heroVideoId() != null) {
            String videoId = normalize(request.heroVideoId());
            // Validate if it's a URL and extract video ID, or use as-is if already an ID
            if (videoId != null && YouTubeUrlValidator.isValidYouTubeUrl(videoId)) {
                videoId = YouTubeUrlValidator.extractVideoId(videoId);
            }
            settings.setHeroVideoId(videoId);
        }

        if (request.whyChoose() != null) {
            var why = request.whyChoose();
            if (why.title() != null) {
                if (why.title().en() != null) {
                    settings.setWhyChooseTitleEn(normalize(why.title().en()));
                }
                if (why.title().ar() != null) {
                    settings.setWhyChooseTitleAr(normalize(why.title().ar()));
                }
            }
            if (why.subtitle() != null) {
                if (why.subtitle().en() != null) {
                    settings.setWhyChooseSubtitleEn(normalize(why.subtitle().en()));
                }
                if (why.subtitle().ar() != null) {
                    settings.setWhyChooseSubtitleAr(normalize(why.subtitle().ar()));
                }
            }
            if (why.features() != null) {
                List<ClinicSettingsEntity.WhyChooseFeatureConfig> featureConfigs = why.features().stream().map(feature -> {
                    ClinicSettingsEntity.WhyChooseFeatureConfig config = new ClinicSettingsEntity.WhyChooseFeatureConfig();
                    config.setKey(normalize(feature.key()));
                    config.setIcon(normalize(feature.icon()));
                    if (feature.title() != null) {
                        if (feature.title().en() != null) {
                            config.setTitleEn(normalize(feature.title().en()));
                        }
                        if (feature.title().ar() != null) {
                            config.setTitleAr(normalize(feature.title().ar()));
                        }
                    }
                    if (feature.description() != null) {
                        if (feature.description().en() != null) {
                            config.setDescriptionEn(normalize(feature.description().en()));
                        }
                        if (feature.description().ar() != null) {
                            config.setDescriptionAr(normalize(feature.description().ar()));
                        }
                    }
                    return config;
                }).toList();
                settings.setWhyChooseFeatures(featureConfigs);
            }
        }

        // Handle ecommerce feature toggle
        Long tenantId = tenantContextHolder.requireTenantId();
        boolean currentEcommerceEnabled = ecommerceFeatureService.isEcommerceEnabled(tenantId);
        
        if (request.ecommerceEnabled() != null && request.ecommerceEnabled() != currentEcommerceEnabled) {
            if (request.ecommerceEnabled()) {
                ecommerceFeatureService.enableEcommerce(tenantId);
            } else {
                ecommerceFeatureService.disableEcommerce(tenantId);
            }
        }

        ClinicSettingsEntity saved = settingsRepository.save(settings);
        boolean finalEcommerceEnabled = ecommerceFeatureService.isEcommerceEnabled(tenantId);
        return mapToResponse(saved, finalEcommerceEnabled);
    }

    private ClinicSettingsEntity loadOrCreateSettings() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultSettings(tenantId));
    }

    private ClinicSettingsEntity createDefaultSettings(Long tenantId) {
        ClinicSettingsEntity settings = new ClinicSettingsEntity("Cliniqax's Clinic");
        settings.setTenant(tenantService.requireTenant(tenantId));
        settings.setClinicName("Cliniqax's Clinic");
        settings.setPhone("+1 (555) 123-4567");
        settings.setEmail("info@Cliniqaxsclinic.com");
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
        settings.setTimezone(clinicTimezoneConfig.getZoneId());
        // Do not prefill email delivery credentials; leave empty/disabled by default
        settings.setSendgridApiKey(null);
        settings.setEmailFrom(null);
        settings.setEmailFromName(null);
        // Let global config decide email enablement (null = fallback to security.email.enabled)
        settings.setEmailEnabled(null);
        settings.setReminderEnabled(Boolean.FALSE);
        settings.setReminderHoursBefore(24);
        settings.setWhyChooseTitleEn("Why Choose Cliniqax's Clinic?");
        settings.setWhyChooseTitleAr("لماذا تختار عيادة قدري؟");
        settings.setWhyChooseSubtitleEn("We combine cutting-edge technology with compassionate care to deliver exceptional dental experiences.");
        settings.setWhyChooseSubtitleAr("نجمع بين أحدث التقنيات والرعاية الإنسانية لنقدم تجربة أسنان استثنائية.");
        settings.setWhyChooseFeatures(defaultWhyChooseFeatures());
        settings.setFaviconUrl("/favicon.ico");
        return settingsRepository.save(settings);
    }

    private ClinicSettingsResponse mapToResponse(ClinicSettingsEntity entity, boolean ecommerceEnabled) {
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
        // Do not expose env defaults for email credentials; return only tenant-set values or null
        String resolvedSendgridKey = StringUtils.hasText(entity.getSendgridApiKey()) ? entity.getSendgridApiKey() : null;
        String resolvedEmailFrom = StringUtils.hasText(entity.getEmailFrom()) ? entity.getEmailFrom() : null;
        String resolvedEmailFromName = StringUtils.hasText(entity.getEmailFromName()) ? entity.getEmailFromName() : null;
        Boolean resolvedEmailEnabled = entity.getEmailEnabled();
        String resolvedTimezone = resolveOrDefault(entity.getTimezone(), clinicTimezoneConfig.getZoneId());
        Boolean resolvedReminderEnabled = entity.getReminderEnabled();
        Integer resolvedReminderHours = entity.getReminderHoursBefore();

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
                entity.getFaviconUrl(),
                entity.getFaviconImageId(),
                workingHours,
                socialMedia,
                entity.getVirtualConsultationFee(),
                entity.getVirtualConsultationMeetingLink(),
                entity.getSlotDurationMinutes(),
                resolvedPaypalEnvironment,
                resolvedPaypalClientId,
                resolvedPaypalClientSecret,
                entity.getExchangeRates(),
                resolvedTimezone,
                resolvedSendgridKey,
                resolvedEmailFrom,
                resolvedEmailFromName,
                resolvedEmailEnabled,
                resolvedReminderEnabled,
                resolvedReminderHours,
                entity.getHeroMediaType(),
                entity.getHeroImageUrl(),
                entity.getHeroVideoId(),
                new ClinicSettingsResponse.WhyChoose(
                        localizedText(entity.getWhyChooseTitleEn(), entity.getWhyChooseTitleAr()),
                        localizedText(entity.getWhyChooseSubtitleEn(), entity.getWhyChooseSubtitleAr()),
                        entity.getWhyChooseFeatures() == null
                                ? List.of()
                                : entity.getWhyChooseFeatures().stream()
                                .map(feature -> new ClinicSettingsResponse.WhyChooseFeature(
                                        feature.getKey(),
                                        localizedText(feature.getTitleEn(), feature.getTitleAr()),
                                        localizedText(feature.getDescriptionEn(), feature.getDescriptionAr()),
                                        feature.getIcon()
                                ))
                                .toList()
                ),
                ecommerceEnabled
        );
    }

    private ClinicSettingsResponse.LocalizedText localizedText(String en, String ar) {
        return new ClinicSettingsResponse.LocalizedText(en, ar);
    }

    private List<ClinicSettingsEntity.WhyChooseFeatureConfig> defaultWhyChooseFeatures() {
        List<ClinicSettingsEntity.WhyChooseFeatureConfig> defaults = new ArrayList<>();
        defaults.add(createFeatureConfig(
                "experts",
                "shield-check",
                "Expert Professionals",
                "أطباء محترفون",
                "Highly qualified dentists with years of specialized experience",
                "أطباء أسنان مؤهلون يتمتعون بسنوات من الخبرة المتخصصة"
        ));
        defaults.add(createFeatureConfig(
                "technology",
                "beaker",
                "Advanced Technology",
                "تقنيات متقدمة",
                "State-of-the-art equipment for precise diagnosis and treatment",
                "أحدث الأجهزة للحصول على تشخيص وعلاج دقيق"
        ));
        defaults.add(createFeatureConfig(
                "comfort",
                "smile",
                "Patient Comfort",
                "راحة المرضى",
                "Relaxing environment designed to ease dental anxiety",
                "بيئة مريحة تساعد على تقليل القلق من علاج الأسنان"
        ));
        defaults.add(createFeatureConfig(
                "affordable",
                "wallet",
                "Affordable Care",
                "رعاية ميسورة",
                "Flexible payment plans and insurance options available",
                "خيارات دفع مرنة وتغطية تأمينية متاحة"
        ));
        return defaults;
    }

    private ClinicSettingsEntity.WhyChooseFeatureConfig createFeatureConfig(
            String key,
            String icon,
            String titleEn,
            String titleAr,
            String descriptionEn,
            String descriptionAr
    ) {
        ClinicSettingsEntity.WhyChooseFeatureConfig config = new ClinicSettingsEntity.WhyChooseFeatureConfig();
        config.setKey(key);
        config.setIcon(icon);
        config.setTitleEn(titleEn);
        config.setTitleAr(titleAr);
        config.setDescriptionEn(descriptionEn);
        config.setDescriptionAr(descriptionAr);
        return config;
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
