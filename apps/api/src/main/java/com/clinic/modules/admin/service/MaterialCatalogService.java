package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.MaterialCatalogRequest;
import com.clinic.modules.admin.dto.MaterialCatalogResponse;
import com.clinic.modules.core.service.CurrencyConversionService;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.core.treatment.MaterialCatalogEntity;
import com.clinic.modules.core.treatment.MaterialCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing material catalog.
 */
@Service
public class MaterialCatalogService {

    private final MaterialCatalogRepository materialCatalogRepository;
    private final ClinicSettingsRepository clinicSettingsRepository;
    private final CurrencyConversionService currencyConversionService;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;

    public MaterialCatalogService(
            MaterialCatalogRepository materialCatalogRepository,
            ClinicSettingsRepository clinicSettingsRepository,
            CurrencyConversionService currencyConversionService,
            TenantContextHolder tenantContextHolder,
            TenantService tenantService
    ) {
        this.materialCatalogRepository = materialCatalogRepository;
        this.clinicSettingsRepository = clinicSettingsRepository;
        this.currencyConversionService = currencyConversionService;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    /**
     * Create a new material in the catalog.
     */
    @Transactional
    public MaterialCatalogResponse createMaterial(MaterialCatalogRequest request) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        // Check for duplicate name within tenant
        materialCatalogRepository.findByTenantIdAndName(tenantId, request.name())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Material with name '" + request.name() + "' already exists");
                });

        // Use provided currency or default to clinic currency
        String currency = request.currency() != null && !request.currency().isBlank()
                ? request.currency()
                : getClinicCurrency();

        MaterialCatalogEntity material = new MaterialCatalogEntity(
                request.name(),
                request.description(),
                request.unitCost(),
                currency,
                request.unitOfMeasure()
        );

        if (!request.active()) {
            material.deactivate();
        }
        
        // Assign current tenant
        material.setTenant(tenantService.requireTenant(tenantId));

        MaterialCatalogEntity saved = materialCatalogRepository.save(material);
        return toResponse(saved);
    }

    /**
     * Get material by ID.
     */
    @Transactional(readOnly = true)
    public MaterialCatalogResponse getMaterial(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        MaterialCatalogEntity material = materialCatalogRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with ID: " + id));
        return toResponse(material);
    }

    /**
     * Get all active materials.
     */
    @Transactional(readOnly = true)
    public List<MaterialCatalogResponse> getActiveMaterials() {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        return materialCatalogRepository.findAllByTenantId(tenantId).stream()
                .filter(MaterialCatalogEntity::getActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all materials (active and inactive).
     */
    @Transactional(readOnly = true)
    public List<MaterialCatalogResponse> getAllMaterials() {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        return materialCatalogRepository.findAllByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update a material.
     */
    @Transactional
    public MaterialCatalogResponse updateMaterial(Long id, MaterialCatalogRequest request) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        MaterialCatalogEntity material = materialCatalogRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with ID: " + id));

        // Check for duplicate name within tenant if name is being changed
        if (!material.getName().equals(request.name())) {
            materialCatalogRepository.findByTenantIdAndName(tenantId, request.name())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Material with name '" + request.name() + "' already exists");
                    });
        }

        // Use provided currency or default to clinic currency
        String currency = request.currency() != null && !request.currency().isBlank()
                ? request.currency()
                : getClinicCurrency();

        material.updateDetails(
                request.name(),
                request.description(),
                request.unitCost(),
                currency,
                request.unitOfMeasure()
        );

        if (request.active()) {
            material.activate();
        } else {
            material.deactivate();
        }

        return toResponse(material);
    }

    /**
     * Deactivate a material (soft delete).
     */
    @Transactional
    public MaterialCatalogResponse deactivateMaterial(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        MaterialCatalogEntity material = materialCatalogRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with ID: " + id));

        material.deactivate();

        return toResponse(material);
    }

    /**
     * Activate a material.
     */
    @Transactional
    public MaterialCatalogResponse activateMaterial(Long id) {
        Long tenantId = tenantContextHolder.requireTenantId();
        
        MaterialCatalogEntity material = materialCatalogRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with ID: " + id));

        material.activate();

        return toResponse(material);
    }

    /**
     * Convert entity to response DTO.
     * Includes converted price in clinic currency.
     */
    private MaterialCatalogResponse toResponse(MaterialCatalogEntity material) {
        String clinicCurrency = currencyConversionService.getClinicCurrency();
        BigDecimal convertedPrice = currencyConversionService.convert(
                material.getUnitCost(),
                material.getCurrency(),
                clinicCurrency
        );

        return new MaterialCatalogResponse(
                material.getId(),
                material.getName(),
                material.getDescription(),
                material.getUnitCost(),      // Original price
                material.getCurrency(),       // Original currency
                convertedPrice,               // Price in clinic currency
                clinicCurrency,               // Clinic currency
                material.getUnitOfMeasure(),
                material.getActive(),
                material.getCreatedAt(),
                material.getUpdatedAt()
        );
    }

    /**
     * Get the clinic's default currency from settings.
     */
    private String getClinicCurrency() {
        return currencyConversionService.getClinicCurrency();
    }
}
