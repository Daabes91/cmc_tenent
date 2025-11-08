package com.clinic.modules.admin.insurance;

import com.clinic.modules.admin.insurance.dto.InsuranceCompanyRequest;
import com.clinic.modules.admin.insurance.dto.InsuranceCompanyResponse;
import com.clinic.modules.core.insurance.InsuranceCompanyEntity;
import com.clinic.modules.core.insurance.InsuranceCompanyRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InsuranceCompanyService {

    private final InsuranceCompanyRepository insuranceCompanyRepository;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;

    public InsuranceCompanyService(
            InsuranceCompanyRepository insuranceCompanyRepository,
            TenantContextHolder tenantContextHolder,
            TenantService tenantService
    ) {
        this.insuranceCompanyRepository = insuranceCompanyRepository;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public List<InsuranceCompanyResponse> getAllInsuranceCompanies() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return insuranceCompanyRepository.findAllByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InsuranceCompanyResponse getInsuranceCompanyById(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        InsuranceCompanyEntity company = insuranceCompanyRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Insurance company not found with ID: " + id));
        return mapToResponse(company);
    }

    @Transactional
    public InsuranceCompanyResponse createInsuranceCompany(InsuranceCompanyRequest request) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        // Check if company name already exists within tenant
        if (insuranceCompanyRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.nameEn())) {
            throw new RuntimeException("Insurance company with name '" + request.nameEn() + "' already exists");
        }

        InsuranceCompanyEntity company = new InsuranceCompanyEntity(
                request.nameEn(),
                request.nameAr(),
                request.logoUrl(),
                request.websiteUrl(),
                request.phone(),
                request.email(),
                request.descriptionEn(),
                request.descriptionAr(),
                request.isActive(),
                request.displayOrder()
        );
        
        // Assign current tenant
        company.setTenant(tenantService.requireTenant(tenantId));

        InsuranceCompanyEntity savedCompany = insuranceCompanyRepository.save(company);
        return mapToResponse(savedCompany);
    }

    @Transactional
    public InsuranceCompanyResponse updateInsuranceCompany(Long id, InsuranceCompanyRequest request) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        InsuranceCompanyEntity company = insuranceCompanyRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Insurance company not found with ID: " + id));

        // Check if company name already exists within tenant (excluding current company)
        if (insuranceCompanyRepository.existsByTenantIdAndNameIgnoreCaseAndIdNot(tenantId, request.nameEn(), id)) {
            throw new RuntimeException("Insurance company with name '" + request.nameEn() + "' already exists");
        }

        company.updateDetails(
                request.nameEn(),
                request.nameAr(),
                request.logoUrl(),
                request.websiteUrl(),
                request.phone(),
                request.email(),
                request.descriptionEn(),
                request.descriptionAr(),
                request.isActive(),
                request.displayOrder()
        );

        InsuranceCompanyEntity updatedCompany = insuranceCompanyRepository.save(company);
        return mapToResponse(updatedCompany);
    }

    @Transactional
    public void deleteInsuranceCompany(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        InsuranceCompanyEntity company = insuranceCompanyRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Insurance company not found with ID: " + id));
        
        insuranceCompanyRepository.delete(company);
    }

    private InsuranceCompanyResponse mapToResponse(InsuranceCompanyEntity company) {
        return new InsuranceCompanyResponse(
                company.getId(),
                company.getNameEn(),
                company.getNameAr(),
                company.getLogoUrl(),
                company.getWebsiteUrl(),
                company.getPhone(),
                company.getEmail(),
                company.getDescriptionEn(),
                company.getDescriptionAr(),
                company.getIsActive(),
                company.getDisplayOrder(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}