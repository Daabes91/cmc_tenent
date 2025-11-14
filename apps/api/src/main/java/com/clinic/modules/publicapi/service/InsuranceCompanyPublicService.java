package com.clinic.modules.publicapi.service;

import com.clinic.modules.core.insurance.InsuranceCompanyEntity;
import com.clinic.modules.core.insurance.InsuranceCompanyRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.publicapi.dto.InsuranceCompanyPublicResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InsuranceCompanyPublicService {

    private final InsuranceCompanyRepository insuranceCompanyRepository;
    private final TenantContextHolder tenantContextHolder;

    public InsuranceCompanyPublicService(InsuranceCompanyRepository insuranceCompanyRepository,
                                         TenantContextHolder tenantContextHolder) {
        this.insuranceCompanyRepository = insuranceCompanyRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public List<InsuranceCompanyPublicResponse> getActiveInsuranceCompanies(String locale) {
        Long tenantId = tenantContextHolder.requireTenantId();
        return insuranceCompanyRepository.findByTenantIdAndIsActiveTrueOrderByDisplayOrderAsc(tenantId)
                .stream()
                .map(company -> mapToPublicResponse(company, locale))
                .toList();
    }

    private InsuranceCompanyPublicResponse mapToPublicResponse(InsuranceCompanyEntity company, String locale) {
        String name = getLocalizedName(company, locale);
        String description = getLocalizedDescription(company, locale);
        
        return new InsuranceCompanyPublicResponse(
                company.getId(),
                name,
                company.getLogoUrl(),
                company.getWebsiteUrl(),
                description
        );
    }

    private String getLocalizedName(InsuranceCompanyEntity company, String locale) {
        if ("ar".equals(locale) && company.getNameAr() != null && !company.getNameAr().isBlank()) {
            return company.getNameAr();
        }
        return company.getNameEn();
    }

    private String getLocalizedDescription(InsuranceCompanyEntity company, String locale) {
        if ("ar".equals(locale) && company.getDescriptionAr() != null && !company.getDescriptionAr().isBlank()) {
            return company.getDescriptionAr();
        }
        return company.getDescriptionEn();
    }
}
