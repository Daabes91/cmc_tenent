package com.clinic.modules.publicapi.controller;

import com.clinic.modules.publicapi.dto.ServiceDetailResponse;
import com.clinic.modules.publicapi.dto.ServiceResponse;
import com.clinic.modules.publicapi.service.ServiceCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/services")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> listServices(
            @RequestParam(name = "locale", required = false) String locale) {
        return ResponseEntity.ok(serviceCatalogService.fetchServices(locale));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ServiceDetailResponse> getService(
            @PathVariable String slug,
            @RequestParam(name = "locale", required = false) String locale) {
        return ResponseEntity.ok(serviceCatalogService.fetchServiceDetail(slug, locale));
    }
}
