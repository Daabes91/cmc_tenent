package com.clinic.modules.publicapi.controller;

import com.clinic.modules.admin.service.AppointmentConfirmationService;
import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.tenant.TenantEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/public/appointments")
public class AppointmentConfirmationController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentConfirmationController.class);
    private final AppointmentConfirmationService confirmationService;

    public AppointmentConfirmationController(AppointmentConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmAppointment(
            @RequestParam("token") String token,
            HttpServletRequest request) {
        Optional<AppointmentEntity> appointment = confirmationService.confirmFromToken(token);
        if (appointment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // If client prefers JSON, return success payload instead of redirect
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains("application/json")) {
            return ResponseEntity.ok().body(java.util.Map.of(
                    "status", "confirmed",
                    "appointmentId", appointment.get().getId()
            ));
        }
        TenantEntity tenant = appointment.get().getTenant();
        String redirectUrl = confirmationService.buildSuccessRedirect(tenant);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
