package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.DoctorAdminResponse;
import com.clinic.modules.admin.dto.DoctorUpsertRequest;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class DoctorAdminService {

    private final DoctorRepository doctorRepository;
    private final ClinicServiceRepository serviceRepository;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;

    public DoctorAdminService(DoctorRepository doctorRepository,
                              ClinicServiceRepository serviceRepository,
                              TenantContextHolder tenantContextHolder,
                              TenantService tenantService) {
        this.doctorRepository = doctorRepository;
        this.serviceRepository = serviceRepository;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public List<DoctorAdminResponse> listDoctors() {
        Long tenantId = currentTenantId();
        return doctorRepository.findAllWithServices(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorAdminResponse getDoctor(Long id) {
        Long tenantId = currentTenantId();
        DoctorEntity entity = doctorRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        return toResponse(entity);
    }

    @Transactional
    public DoctorAdminResponse createDoctor(DoctorUpsertRequest request) {
        Long tenantId = currentTenantId();
        String fullNameEn = requireName(request.fullNameEn());
        DoctorEntity doctor = new DoctorEntity(
                fullNameEn,
                normalize(request.fullNameAr()),
                normalize(request.specialtyEn()),
                normalize(request.specialtyAr()),
                normalize(request.bioEn()),
                normalize(request.bioAr()),
                encodeLocales(request.locales())
        );

        // Assign current tenant before save
        doctor.setTenant(tenantService.requireTenant(tenantId));

        Set<ClinicServiceEntity> services = resolveServices(request.serviceIds());
        doctor.assignServices(services);
        doctor.setImageUrl(normalize(request.imageUrl()));
        doctor.updateContactInfo(normalize(request.email()), normalize(request.phone()));
        doctor.updateDisplaySettings(request.displayOrder(), request.isActive());

        DoctorEntity saved = doctorRepository.save(doctor);
        return toResponse(saved);
    }

    @Transactional
    public DoctorAdminResponse updateDoctor(Long id, DoctorUpsertRequest request) {
        Long tenantId = currentTenantId();
        DoctorEntity doctor = doctorRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        String fullNameEn = requireName(request.fullNameEn());
        Set<ClinicServiceEntity> services = resolveServices(request.serviceIds());

        doctor.updateDetails(
                fullNameEn,
                normalize(request.fullNameAr()),
                normalize(request.specialtyEn()),
                normalize(request.specialtyAr()),
                normalize(request.bioEn()),
                normalize(request.bioAr()),
                encodeLocales(request.locales())
        );
        doctor.assignServices(services);
        doctor.setImageUrl(normalize(request.imageUrl()));
        doctor.updateContactInfo(normalize(request.email()), normalize(request.phone()));
        doctor.updateDisplaySettings(request.displayOrder(), request.isActive());

        return toResponse(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Long tenantId = currentTenantId();
        DoctorEntity doctor = doctorRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        Set<ClinicServiceEntity> linkedServices = new HashSet<>(doctor.getServices());
        linkedServices.forEach(service -> service.getDoctors().remove(doctor));
        doctor.assignServices(Collections.emptySet());

        doctorRepository.delete(doctor);
    }

    private Set<ClinicServiceEntity> resolveServices(List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> distinctIds = serviceIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Long tenantId = currentTenantId();
        List<ClinicServiceEntity> services = serviceRepository.findAllByTenantIdAndIdIn(tenantId, distinctIds);
        if (services.size() != distinctIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more services could not be found");
        }
        
        // Validate all services belong to the current tenant
        for (ClinicServiceEntity service : services) {
            if (service.getTenant() == null || !service.getTenant().getId().equals(tenantId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Cannot assign services from different tenants to doctor");
            }
        }
        
        return new HashSet<>(services);
    }

    private DoctorAdminResponse toResponse(DoctorEntity entity) {
        List<String> locales = decodeLocales(entity.getLocaleCodes());
        List<DoctorAdminResponse.ServiceReference> services = entity.getServices().stream()
                .sorted(Comparator.comparing(ClinicServiceEntity::getNameEn, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(service -> new DoctorAdminResponse.ServiceReference(
                        service.getId(),
                        service.getSlug(),
                        service.getNameEn()
                ))
                .toList();

        return new DoctorAdminResponse(
                entity.getId(),
                entity.getFullNameEn(),
                entity.getFullNameAr(),
                entity.getSpecialtyEn(),
                entity.getSpecialtyAr(),
                entity.getBioEn(),
                entity.getBioAr(),
                entity.getImageUrl(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getDisplayOrder(),
                entity.getIsActive(),
                locales,
                services,
                entity.getCreatedAt()
        );
    }

    private List<String> decodeLocales(String localeCodes) {
        if (localeCodes == null || localeCodes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(localeCodes.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .toList();
    }

    private String encodeLocales(List<String> locales) {
        if (locales == null || locales.isEmpty()) {
            return null;
        }
        List<String> sanitized = locales.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .toList();
        if (sanitized.isEmpty()) {
            return null;
        }
        return String.join(",", sanitized);
    }

    private String requireName(String input) {
        String normalized = normalize(input);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor name is required");
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

    private Long currentTenantId() {
        return tenantContextHolder.requireTenantId();
    }
}
