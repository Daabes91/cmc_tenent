package com.clinic.modules.publicapi.service;

import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.publicapi.dto.DoctorResponse;
import com.clinic.modules.publicapi.dto.ServiceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class DoctorDirectoryService {

    private final DoctorRepository doctorRepository;
    private final TenantContextHolder tenantContextHolder;

    public DoctorDirectoryService(DoctorRepository doctorRepository,
                                  TenantContextHolder tenantContextHolder) {
        this.doctorRepository = doctorRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> listDoctors(String locale, String serviceSlug) {
        Long tenantId = tenantContextHolder.requireTenantId();
        List<DoctorEntity> doctors = serviceSlug != null && !serviceSlug.isBlank()
                ? doctorRepository.findAllByServiceSlug(serviceSlug, tenantId)
                : doctorRepository.findAllWithServices(tenantId);

        Locale resolved = locale != null ? Locale.forLanguageTag(locale) : Locale.ENGLISH;

        return doctors.stream()
                .map(doctor -> new DoctorResponse(
                        doctor.getId(),
                        doctor.getFullName(),
                        doctor.getSpecialty(),
                        doctor.getBio(),
                        doctor.getImageUrl(),
                        parseLanguages(doctor.getLocaleCodes(), resolved),
                        mapServices(doctor.getServices(), resolved)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long doctorId, String locale) {
        Long tenantId = tenantContextHolder.requireTenantId();
        DoctorEntity doctor = doctorRepository.findByIdAndTenantId(doctorId, tenantId)
                .orElseThrow(() -> new com.clinic.modules.publicapi.exception.DoctorNotFoundException(doctorId));

        Locale resolved = locale != null ? Locale.forLanguageTag(locale) : Locale.ENGLISH;

        return new DoctorResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getSpecialty(),
                doctor.getBio(),
                doctor.getImageUrl(),
                parseLanguages(doctor.getLocaleCodes(), resolved),
                mapServices(doctor.getServices(), resolved)
        );
    }

    private List<String> parseLanguages(String localeCodes, Locale fallback) {
        if (localeCodes == null || localeCodes.isBlank()) {
            return List.of(fallback.getLanguage());
        }
        return Arrays.stream(localeCodes.split(","))
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .toList();
    }

    private List<ServiceResponse> mapServices(java.util.Set<ClinicServiceEntity> services, Locale locale) {
        if (services == null || services.isEmpty()) {
            return List.of();
        }
        return services.stream()
                .map(service -> new ServiceResponse(
                        service.getSlug(),
                        getLocalizedName(service, locale),
                        getLocalizedSummary(service, locale)
                ))
                .toList();
    }

    private String getLocalizedName(ClinicServiceEntity service, Locale locale) {
        if ("ar".equals(locale.getLanguage()) && service.getNameAr() != null && !service.getNameAr().isBlank()) {
            return service.getNameAr();
        }
        return service.getNameEn();
    }

    private String getLocalizedSummary(ClinicServiceEntity service, Locale locale) {
        if ("ar".equals(locale.getLanguage()) && service.getSummaryAr() != null && !service.getSummaryAr().isBlank()) {
            return service.getSummaryAr();
        }
        return service.getSummaryEn();
    }
}
