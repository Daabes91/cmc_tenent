package com.clinic.modules.admin.controller;

import com.clinic.modules.core.email.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/test")
public class VirtualConsultationTestController {

    private final EmailService emailService;

    public VirtualConsultationTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Test endpoint to send a virtual consultation email with calendar attachment.
     *
     * Example usage:
     * POST /admin/test/send-virtual-email
     * {
     *   "email": "mohammad.daabes3@gmail.com"
     * }
     */
    @PostMapping("/send-virtual-email")
    public ResponseEntity<Map<String, String>> sendVirtualConsultationTestEmail(
            @RequestBody Map<String, String> request
    ) {
        String toEmail = request.get("email");
        if (toEmail == null || toEmail.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        try {
            // Create a test appointment for tomorrow at 2:00 PM Amman time
            ZoneId ammanZone = ZoneId.of("Asia/Amman");
            ZonedDateTime now = ZonedDateTime.now(ammanZone);
            ZonedDateTime appointmentStart = now.plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
            ZonedDateTime appointmentEnd = appointmentStart.plusMinutes(30);

            emailService.sendVirtualConsultationConfirmation(
                    toEmail,
                    "Test Patient",
                    "Dr. Ahmad Qadri",
                    "General Consultation",
                    appointmentStart,
                    appointmentEnd,
                    request.getOrDefault("meetingLink", null)
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Virtual consultation email sent to " + toEmail,
                    "appointmentTime", appointmentStart.toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send email: " + e.getMessage()
            ));
        }
    }
}
