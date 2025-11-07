package com.clinic.modules.publicapi.controller;

import com.clinic.modules.publicapi.dto.InsuranceCompanyPublicResponse;
import com.clinic.modules.publicapi.service.InsuranceCompanyPublicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/insurance-companies")
public class InsuranceCompanyPublicController {

    private final InsuranceCompanyPublicService insuranceCompanyPublicService;

    public InsuranceCompanyPublicController(InsuranceCompanyPublicService insuranceCompanyPublicService) {
        this.insuranceCompanyPublicService = insuranceCompanyPublicService;
    }

    @GetMapping
    public ResponseEntity<List<InsuranceCompanyPublicResponse>> getActiveInsuranceCompanies(
            @RequestParam(name = "locale", required = false) String locale) {
        List<InsuranceCompanyPublicResponse> companies = insuranceCompanyPublicService.getActiveInsuranceCompanies(locale);
        return ResponseEntity.ok(companies);
    }
}