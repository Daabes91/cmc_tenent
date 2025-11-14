package com.clinic.modules.admin.translation.controller;

import com.clinic.modules.admin.translation.dto.TranslationResponse;
import com.clinic.modules.admin.translation.dto.TranslationUpsertRequest;
import com.clinic.modules.core.translation.TenantTranslationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/translations")
public class TenantTranslationAdminController {

    private final TenantTranslationService translationService;

    public TenantTranslationAdminController(TenantTranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping
    public ResponseEntity<List<TranslationResponse>> listTranslations(
            @RequestParam(name = "namespace", required = false) String namespace,
            @RequestParam(name = "locale", required = false) String locale
    ) {
        return ResponseEntity.ok(translationService.getTranslations(namespace, locale));
    }

    @PutMapping
    public ResponseEntity<Void> upsertTranslations(
            @Valid @RequestBody List<TranslationUpsertRequest> requests
    ) {
        translationService.upsertTranslations(requests);
        return ResponseEntity.noContent().build();
    }
}
