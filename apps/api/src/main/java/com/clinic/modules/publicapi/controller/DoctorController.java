package com.clinic.modules.publicapi.controller;

import com.clinic.modules.publicapi.dto.DoctorResponse;
import com.clinic.modules.publicapi.service.DoctorDirectoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/doctors")
public class DoctorController {

    private final DoctorDirectoryService doctorDirectoryService;

    public DoctorController(DoctorDirectoryService doctorDirectoryService) {
        this.doctorDirectoryService = doctorDirectoryService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> listDoctors(
            @RequestParam(name = "locale", required = false) String locale,
            @RequestParam(name = "service", required = false) String serviceSlug) {
        return ResponseEntity.ok(doctorDirectoryService.listDoctors(locale, serviceSlug));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(
            @PathVariable Long id,
            @RequestParam(name = "locale", required = false) String locale) {
        return ResponseEntity.ok(doctorDirectoryService.getDoctorById(id, locale));
    }
}
