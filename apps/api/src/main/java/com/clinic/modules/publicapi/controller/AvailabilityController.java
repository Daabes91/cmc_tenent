package com.clinic.modules.publicapi.controller;

import com.clinic.modules.publicapi.dto.AvailabilityRequest;
import com.clinic.modules.publicapi.dto.AvailabilitySlotResponse;
import com.clinic.modules.publicapi.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<List<AvailabilitySlotResponse>> availability(@Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.computeAvailability(request));
    }
}
