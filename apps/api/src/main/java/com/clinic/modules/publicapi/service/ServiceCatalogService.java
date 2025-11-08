package com.clinic.modules.publicapi.service;

import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.publicapi.dto.ServiceDetailResponse;
import com.clinic.modules.publicapi.dto.ServiceResponse;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class ServiceCatalogService {

    private final ClinicServiceRepository serviceRepository;
    private final TenantContextHolder tenantContextHolder;

    public ServiceCatalogService(ClinicServiceRepository serviceRepository,
                                 TenantContextHolder tenantContextHolder) {
        this.serviceRepository = serviceRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> fetchServices(String locale) {
        Locale resolved = resolveLocale(locale);
        return serviceRepository.findByTenantIdOrderByNameEnAsc(currentTenantId()).stream()
                .map(entity -> toSummaryDto(entity, resolved))
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceDetailResponse fetchServiceDetail(String slug, String locale) {
        Locale resolved = resolveLocale(locale);
        ClinicServiceEntity service = serviceRepository.findBySlugAndTenantId(slug, currentTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        String name = pickLocalized(service.getNameEn(), service.getNameAr(), resolved);
        String summary = pickLocalized(service.getSummaryEn(), service.getSummaryAr(), resolved);

        List<String> doctorIds = service.getDoctors().stream()
                .map(doctor -> doctor.getId().toString())
                .toList();

        return new ServiceDetailResponse(
                service.getSlug(),
                name,
                summary,
                summary,
                doctorIds
        );
    }

    private ServiceResponse toSummaryDto(ClinicServiceEntity service, Locale locale) {
        String name = pickLocalized(service.getNameEn(), service.getNameAr(), locale);
        String summary = pickLocalized(service.getSummaryEn(), service.getSummaryAr(), locale);
        return new ServiceResponse(service.getSlug(), name, summary);
    }

    private String pickLocalized(String en, String ar, Locale locale) {
        if (locale.getLanguage().equalsIgnoreCase("ar") && ar != null && !ar.isBlank()) {
            return ar;
        }
        return en;
    }

    private Locale resolveLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return Locale.ENGLISH;
        }
        return Locale.forLanguageTag(locale);
    }

    private Long currentTenantId() {
        return tenantContextHolder.requireTenantId();
    }
}
