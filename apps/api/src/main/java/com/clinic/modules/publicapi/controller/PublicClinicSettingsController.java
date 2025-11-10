package com.clinic.modules.publicapi.controller;

import com.clinic.config.ClinicTimezoneConfig;
import com.clinic.modules.admin.dto.ClinicSettingsResponse;
import com.clinic.modules.admin.service.ClinicSettingsService;
import com.clinic.modules.publicapi.dto.TimezoneResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/public/settings")
public class PublicClinicSettingsController {

    private final ClinicSettingsService settingsService;
    private final ClinicTimezoneConfig clinicTimezoneConfig;

    public PublicClinicSettingsController(ClinicSettingsService settingsService,
                                          ClinicTimezoneConfig clinicTimezoneConfig) {
        this.settingsService = settingsService;
        this.clinicTimezoneConfig = clinicTimezoneConfig;
    }

    /**
     * Get clinic settings including hero media configuration.
     * 
     * Performance optimization: Returns Cache-Control headers to enable client-side caching
     * for 5 minutes with stale-while-revalidate for 10 minutes. This reduces server load
     * and improves page load performance for the landing page hero section.
     *
     * @return Clinic settings with caching headers
     */
    @GetMapping
    public ResponseEntity<ClinicSettingsResponse> getSettings() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES)
                        .staleWhileRevalidate(10, TimeUnit.MINUTES)
                        .cachePublic())
                .body(settingsService.getSettings());
    }

    /**
     * Get clinic timezone information for frontend display and conversion.
     *
     * @return Timezone details including zone ID, abbreviation, offset, and current time
     */
    @GetMapping("/timezone")
    public ResponseEntity<TimezoneResponse> getTimezone() {
        return ResponseEntity.ok(TimezoneResponse.from(clinicTimezoneConfig.toZoneId()));
    }
}
