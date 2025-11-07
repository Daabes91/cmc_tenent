package com.clinic.modules.admin.insurance;

import com.clinic.modules.admin.insurance.dto.InsuranceCompanyRequest;
import com.clinic.modules.admin.insurance.dto.InsuranceCompanyResponse;
import com.clinic.modules.core.insurance.InsuranceCompanyEntity;
import com.clinic.modules.core.insurance.InsuranceCompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InsuranceCompanyService {

    private final InsuranceCompanyRepository insuranceCompanyRepository;

    public InsuranceCompanyService(InsuranceCompanyRepository insuranceCompanyRepository) {
        this.insuranceCompanyRepository = insuranceCompanyRepository;
    }

    @Transactional(readOnly = true)
    public List<InsuranceCompanyResponse> getAllInsuranceCompanies() {
        return insuranceCompanyRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InsuranceCompanyResponse getInsuranceCompanyById(Long id) {
        InsuranceCompanyEntity company = insuranceCompanyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance company not found with ID: " + id));
        return mapToResponse(company);
    }

    @Transactional
    public InsuranceCompanyResponse createInsuranceCompany(InsuranceCompanyRequest request) {
        // Check if company name already exists
        if (insuranceCompanyRepository.existsByNameIgnoreCase(request.nameEn())) {
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

        InsuranceCompanyEntity savedCompany = insuranceCompanyRepository.save(company);
        return mapToResponse(savedCompany);
    }

    @Transactional
    public InsuranceCompanyResponse updateInsuranceCompany(Long id, InsuranceCompanyRequest request) {
        InsuranceCompanyEntity company = insuranceCompanyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance company not found with ID: " + id));

        // Check if company name already exists (excluding current company)
        if (insuranceCompanyRepository.existsByNameIgnoreCaseAndIdNot(request.nameEn(), id)) {
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
        if (!insuranceCompanyRepository.existsById(id)) {
            throw new RuntimeException("Insurance company not found with ID: " + id);
        }
        insuranceCompanyRepository.deleteById(id);
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