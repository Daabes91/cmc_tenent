package com.clinic.modules.publicapi.controller;

import com.clinic.modules.core.translation.TenantTranslationService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/public/translations")
public class TenantTranslationPublicController {

    private final TenantTranslationService translationService;
    private final TenantContextHolder tenantContextHolder;

    public TenantTranslationPublicController(
            TenantTranslationService translationService,
            TenantContextHolder tenantContextHolder
    ) {
        this.translationService = translationService;
        this.tenantContextHolder = tenantContextHolder;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getTranslations(
            @RequestParam("locale") String locale,
            @RequestParam(value = "namespace", required = false) String namespace
    ) {
        Long tenantId = tenantContextHolder.requireTenantId();
        Map<String, String> translations = translationService.getTranslationsForLocale(tenantId, locale, namespace);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(translations);
    }
}
