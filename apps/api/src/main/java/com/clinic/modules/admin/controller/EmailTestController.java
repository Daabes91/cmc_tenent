package com.clinic.modules.admin.controller;

import com.clinic.modules.core.email.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/admin/test")
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-email")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestBody Map<String, String> request) {
        String toEmail = request.get("email");
        if (toEmail == null || toEmail.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        try {
            // Send a test confirmation email
            emailService.sendAppointmentConfirmation(
                    toEmail,
                    "Test Patient",
                    "Dr. Test Doctor",
                    "General Consultation",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME),
                    "IN_PERSON",
                    null
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Test email sent successfully to " + toEmail,
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to send email: " + e.getMessage(),
                    "status", "error"
            ));
        }
    }
}
