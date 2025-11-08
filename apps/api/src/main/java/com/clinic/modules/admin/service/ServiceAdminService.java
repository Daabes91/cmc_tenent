package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.ServiceSummaryResponse;
import com.clinic.modules.admin.dto.ServiceUpsertRequest;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ServiceAdminService {

    private final ClinicServiceRepository serviceRepository;
    private final TenantService tenantService;
    private final TenantContextHolder tenantContextHolder;

    public ServiceAdminService(ClinicServiceRepository serviceRepository,
                               TenantService tenantService,
                               TenantContextHolder tenantContextHolder) {
        this.serviceRepository = serviceRepository;
        this.tenantService = tenantService;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public List<ServiceSummaryResponse> listServices() {
        return serviceRepository.findByTenantIdOrderByNameEnAsc(currentTenantId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceSummaryResponse getService(Long id) {
        return toResponse(requireService(id));
    }

    @Transactional
    public ServiceSummaryResponse createService(ServiceUpsertRequest request) {
        String nameEn = requireName(request.nameEn());
        String slug = resolveSlug(request.slug(), nameEn);
        ensureSlugAvailable(slug, null);

        ClinicServiceEntity entity = new ClinicServiceEntity(
                slug,
                tenantService.requireTenant(currentTenantId()),
                nameEn,
                normalize(request.nameAr()),
                normalize(request.summaryEn()),
                normalize(request.summaryAr())
        );

        ClinicServiceEntity saved = serviceRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public ServiceSummaryResponse updateService(Long id, ServiceUpsertRequest request) {
        ClinicServiceEntity entity = requireService(id);

        String nameEn = requireName(request.nameEn());
        String slug = resolveSlug(request.slug(), nameEn);
        ensureSlugAvailable(slug, id);

        entity.updateDetails(
                slug,
                nameEn,
                normalize(request.nameAr()),
                normalize(request.summaryEn()),
                normalize(request.summaryAr())
        );

        return toResponse(entity);
    }

    @Transactional
    public void deleteService(Long id) {
        ClinicServiceEntity service = requireService(id);

        Set<com.clinic.modules.core.doctor.DoctorEntity> linkedDoctors = new HashSet<>(service.getDoctors());
        linkedDoctors.forEach(doctor -> doctor.getServices().remove(service));

        serviceRepository.delete(service);
    }

    private ServiceSummaryResponse toResponse(ClinicServiceEntity entity) {
        return new ServiceSummaryResponse(
                entity.getId(),
                entity.getSlug(),
                entity.getNameEn(),
                entity.getNameAr(),
                entity.getSummaryEn(),
                entity.getSummaryAr(),
                entity.getCreatedAt(),
                entity.getDoctors().size()
        );
    }

    private String resolveSlug(String provided, String fallbackName) {
        String candidate = normalize(provided);
        if (candidate == null || candidate.isBlank()) {
            candidate = fallbackName;
        }

        String slug = slugify(candidate);
        if (slug.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to derive slug from provided data");
        }
        return slug;
    }

    private void ensureSlugAvailable(String slug, Long currentId) {
        serviceRepository.findBySlugAndTenantId(slug, currentTenantId())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already in use");
                });
    }

    private String requireName(String name) {
        String normalized = normalize(name);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "English name is required");
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        return normalized;
    }

    private ClinicServiceEntity requireService(Long id) {
        return serviceRepository.findByIdAndTenantId(id, currentTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    }

    private Long currentTenantId() {
        return tenantContextHolder.requireTenantId();
    }
}
