package com.clinic.modules.admin.insurance;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.insurance.dto.InsuranceCompanyRequest;
import com.clinic.modules.admin.insurance.dto.InsuranceCompanyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/insurance-companies")
@PreAuthorize("hasRole('ADMIN')")
public class InsuranceCompanyController {

    private final InsuranceCompanyService insuranceCompanyService;

    public InsuranceCompanyController(InsuranceCompanyService insuranceCompanyService) {
        this.insuranceCompanyService = insuranceCompanyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InsuranceCompanyResponse>>> getAllInsuranceCompanies() {
        List<InsuranceCompanyResponse> companies = insuranceCompanyService.getAllInsuranceCompanies();
        return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Insurance companies retrieved successfully", companies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InsuranceCompanyResponse>> getInsuranceCompanyById(@PathVariable Long id) {
        InsuranceCompanyResponse company = insuranceCompanyService.getInsuranceCompanyById(id);
        return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Insurance company retrieved successfully", company));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InsuranceCompanyResponse>> createInsuranceCompany(
            @Valid @RequestBody InsuranceCompanyRequest request) {
        InsuranceCompanyResponse company = insuranceCompanyService.createInsuranceCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseFactory.success("CREATED", "Insurance company created successfully", company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InsuranceCompanyResponse>> updateInsuranceCompany(
            @PathVariable Long id,
            @Valid @RequestBody InsuranceCompanyRequest request) {
        InsuranceCompanyResponse company = insuranceCompanyService.updateInsuranceCompany(id, request);
        return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Insurance company updated successfully", company));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInsuranceCompany(@PathVariable Long id) {
        insuranceCompanyService.deleteInsuranceCompany(id);
        return ResponseEntity.ok(ApiResponseFactory.success("SUCCESS", "Insurance company deleted successfully"));
    }
}