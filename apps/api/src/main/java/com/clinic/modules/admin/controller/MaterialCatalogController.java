package com.clinic.modules.admin.controller;

import com.clinic.modules.admin.dto.MaterialCatalogRequest;
import com.clinic.modules.admin.dto.MaterialCatalogResponse;
import com.clinic.modules.admin.service.MaterialCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing material catalog.
 * Base path: /admin/materials
 */
@RestController
@RequestMapping("/admin/materials")
public class MaterialCatalogController {

    private final MaterialCatalogService materialCatalogService;

    public MaterialCatalogController(MaterialCatalogService materialCatalogService) {
        this.materialCatalogService = materialCatalogService;
    }

    /**
     * Create a new material in the catalog.
     * POST /admin/materials
     */
    @PostMapping
    public ResponseEntity<MaterialCatalogResponse> createMaterial(
            @Valid @RequestBody MaterialCatalogRequest request
    ) {
        MaterialCatalogResponse response = materialCatalogService.createMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get material by ID.
     * GET /admin/materials/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaterialCatalogResponse> getMaterial(@PathVariable Long id) {
        MaterialCatalogResponse response = materialCatalogService.getMaterial(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all materials (active and inactive).
     * GET /admin/materials
     */
    @GetMapping
    public ResponseEntity<List<MaterialCatalogResponse>> getAllMaterials(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<MaterialCatalogResponse> response = activeOnly
                ? materialCatalogService.getActiveMaterials()
                : materialCatalogService.getAllMaterials();
        return ResponseEntity.ok(response);
    }

    /**
     * Update a material.
     * PUT /admin/materials/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<MaterialCatalogResponse> updateMaterial(
            @PathVariable Long id,
            @Valid @RequestBody MaterialCatalogRequest request
    ) {
        MaterialCatalogResponse response = materialCatalogService.updateMaterial(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a material (soft delete).
     * POST /admin/materials/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<MaterialCatalogResponse> deactivateMaterial(@PathVariable Long id) {
        MaterialCatalogResponse response = materialCatalogService.deactivateMaterial(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a material.
     * POST /admin/materials/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<MaterialCatalogResponse> activateMaterial(@PathVariable Long id) {
        MaterialCatalogResponse response = materialCatalogService.activateMaterial(id);
        return ResponseEntity.ok(response);
    }
}
