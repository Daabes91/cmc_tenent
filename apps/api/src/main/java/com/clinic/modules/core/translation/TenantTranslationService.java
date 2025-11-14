package com.clinic.modules.core.translation;

import com.clinic.modules.admin.translation.dto.TranslationResponse;
import com.clinic.modules.admin.translation.dto.TranslationUpsertRequest;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TenantTranslationService {

    private final TenantTranslationRepository translationRepository;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;
    private final DefaultTranslationProvider defaultTranslationProvider;

    public TenantTranslationService(
            TenantTranslationRepository translationRepository,
            TenantContextHolder tenantContextHolder,
            TenantService tenantService,
            DefaultTranslationProvider defaultTranslationProvider
    ) {
        this.translationRepository = translationRepository;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
        this.defaultTranslationProvider = defaultTranslationProvider;
    }

    @Transactional(readOnly = true)
    public List<TranslationResponse> getTranslations(String namespace, String locale) {
        Long tenantId = tenantContextHolder.requireTenantId();
        String normalizedLocale = locale != null ? locale.toLowerCase(Locale.ROOT) : "en";
        String normalizedNamespace = normalize(namespace);

        Map<String, String> defaults = defaultTranslationProvider.getDefaults(normalizedLocale);
        Map<String, TranslationResponse> result = new LinkedHashMap<>();

        defaults.forEach((path, value) -> {
            NamespaceKey nk = splitPath(path);
            if (normalizedNamespace != null && !normalizedNamespace.equals(nk.namespace())) {
                return;
            }
            result.put(path, new TranslationResponse(
                    null,
                    nk.namespace(),
                    nk.key(),
                    normalizedLocale,
                    value,
                    null
            ));
        });

        List<TenantTranslationEntity> overrides = translationRepository.findByTenantAndFilters(
                tenantId,
                normalizedNamespace,
                normalizedLocale
        );

        for (TenantTranslationEntity entity : overrides) {
            String path = entity.getNamespace() + "." + entity.getTranslationKey();
            result.put(path, new TranslationResponse(
                    entity.getId(),
                    entity.getNamespace(),
                    entity.getTranslationKey(),
                    entity.getLocale(),
                    entity.getValue(),
                    entity.getUpdatedAt()
            ));
        }

        return new ArrayList<>(result.values());
    }

    @Transactional
    public void upsertTranslations(List<TranslationUpsertRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        Long tenantId = tenantContextHolder.requireTenantId();
        var tenant = tenantService.requireTenant(tenantId);

        for (TranslationUpsertRequest request : requests) {
            String namespace = requireNonBlank(request.namespace(), "namespace");
            String key = requireNonBlank(request.key(), "key");
            String locale = requireNonBlank(request.locale(), "locale").toLowerCase(Locale.ROOT);
            String value = Optional.ofNullable(request.value()).orElse("").trim();

            TenantTranslationEntity entity = translationRepository
                    .findByTenantIdAndNamespaceAndTranslationKeyAndLocale(tenantId, namespace, key, locale)
                    .orElseGet(() -> {
                        TenantTranslationEntity created = new TenantTranslationEntity();
                        created.setTenant(tenant);
                        created.setNamespace(namespace);
                        created.setTranslationKey(key);
                        created.setLocale(locale);
                        return created;
                    });

            entity.setValue(value);
            translationRepository.save(entity);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, String> getTranslationsForLocale(Long tenantId, String locale, String namespace) {
        String normalizedLocale = locale.toLowerCase(Locale.ROOT);
        List<TenantTranslationEntity> entities;

        if (namespace != null && !namespace.isBlank()) {
            entities = translationRepository.findByTenantIdAndNamespaceAndLocale(tenantId, namespace, normalizedLocale);
        } else {
            entities = translationRepository.findByTenantIdAndLocale(tenantId, normalizedLocale);
        }

        return entities.stream().collect(Collectors.toMap(
                entity -> entity.getNamespace() + "." + entity.getTranslationKey(),
                TenantTranslationEntity::getValue
        ));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private NamespaceKey splitPath(String path) {
        int dotIndex = path.indexOf('.');
        if (dotIndex == -1) {
            return new NamespaceKey(path, "");
        }
        String namespace = path.substring(0, dotIndex);
        String key = path.substring(dotIndex + 1);
        return new NamespaceKey(namespace, key);
    }

    private record NamespaceKey(String namespace, String key) {}
}
