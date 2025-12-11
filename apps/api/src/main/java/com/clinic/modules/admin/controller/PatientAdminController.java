package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.PatientAdminResponse;
import com.clinic.modules.admin.dto.PatientUpsertRequest;
import com.clinic.modules.admin.service.PatientAdminService;
import com.clinic.modules.core.email.EmailService;
import com.clinic.modules.core.tag.TagEntity;
import com.clinic.modules.core.tag.TagService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/patients")
public class PatientAdminController {

        private final PatientAdminService patientAdminService;
        private final TagService tagService;
        private final EmailService emailService;

        public PatientAdminController(PatientAdminService patientAdminService, TagService tagService, EmailService emailService) {
                this.patientAdminService = patientAdminService;
                this.tagService = tagService;
                this.emailService = emailService;
        }

        @GetMapping
        @PreAuthorize("@permissionService.canView('patients')")
        public ResponseEntity<ApiResponse<Page<PatientAdminResponse>>> listPatients(
                        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                        @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                        @RequestParam(name = "sort", required = false, defaultValue = "lastName") String sortBy,
                        @RequestParam(name = "direction", required = false, defaultValue = "ASC") String direction,
                        @RequestParam(name = "tags", required = false) List<Long> tagIds) {

                // Create pageable with sorting
                Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC
                                : Sort.Direction.ASC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

                Page<PatientAdminResponse> patients = patientAdminService.listPatients(pageable, tagIds);
                return ResponseEntity.ok(
                                ApiResponseFactory.success(
                                                "PATIENTS_LISTED",
                                                "Patients fetched successfully.",
                                                patients,
                                                Map.of(
                                                                "totalElements", patients.getTotalElements(),
                                                                "totalPages", patients.getTotalPages(),
                                                                "currentPage", patients.getNumber(),
                                                                "pageSize", patients.getSize()),
                                                null));
        }

        @GetMapping("/{id}")
        @PreAuthorize("@permissionService.canView('patients')")
        public ResponseEntity<ApiResponse<PatientAdminResponse>> getPatient(@PathVariable Long id) {
                PatientAdminResponse response = patientAdminService.getPatient(id);
                return ResponseEntity.ok(
                                ApiResponseFactory.success(
                                                "PATIENT_RETRIEVED",
                                                "Patient fetched successfully.",
                                                response));
        }

        @PostMapping
        @PreAuthorize("@permissionService.canCreate('patients')")
        public ResponseEntity<ApiResponse<PatientAdminResponse>> createPatient(
                        @Valid @RequestBody PatientUpsertRequest request) {
                PatientAdminResponse response = patientAdminService.createPatient(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponseFactory.success(
                                                "PATIENT_CREATED",
                                                "Patient created successfully.",
                                                response));
        }

        @PutMapping("/{id}")
        @PreAuthorize("@permissionService.canEdit('patients')")
        public ResponseEntity<ApiResponse<PatientAdminResponse>> updatePatient(@PathVariable Long id,
                        @Valid @RequestBody PatientUpsertRequest request) {
                PatientAdminResponse response = patientAdminService.updatePatient(id, request);
                return ResponseEntity.ok(
                                ApiResponseFactory.success(
                                                "PATIENT_UPDATED",
                                                "Patient updated successfully.",
                                                response));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("@permissionService.canDelete('patients')")
        public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
                patientAdminService.deletePatient(id);
                return ResponseEntity.ok(
                                ApiResponseFactory.success(
                                                "PATIENT_DELETED",
                                                "Patient deleted successfully."));
        }

        @PostMapping("/{id}/reminder-email")
        @PreAuthorize("@permissionService.canView('patients')")
        public ResponseEntity<ApiResponse<String>> sendReminderEmail(
                        @PathVariable Long id,
                        @RequestBody ReminderEmailRequest request) {
                PatientAdminResponse patient = patientAdminService.getPatient(id);
                if (patient.email() == null || patient.email().isBlank()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                        ApiResponseFactory.errorWithType("PATIENT_EMAIL_MISSING",
                                                        "Patient has no email on file.", List.of()));
                }

                String subject = (request.subject() != null && !request.subject().isBlank())
                                ? request.subject().trim()
                                : "Appointment Reminder";
                String body = (request.body() != null && !request.body().isBlank())
                                ? request.body().trim()
                                : "This is a reminder for your upcoming appointment.";

                String patientName = ((patient.firstName() != null ? patient.firstName() : "") + " " +
                                (patient.lastName() != null ? patient.lastName() : "")).trim();
                if (patientName.isBlank()) {
                        patientName = "Patient";
                }

                String html = "<p>Dear " + patientName + ",</p>"
                                + "<p>" + body + "</p>"
                                + "<p>Thank you,<br/>" + "Clinic Team" + "</p>";

                emailService.sendCustomEmail(patient.email(), subject, html);

                return ResponseEntity.ok(
                                ApiResponseFactory.success(
                                                "REMINDER_SENT",
                                                "Reminder email sent.",
                                                "sent"));
        }

        public record ReminderEmailRequest(
                        String subject,
                        String body) {
        }

        @GetMapping("/tags")
        @PreAuthorize("@permissionService.canView('patients')")
        public ResponseEntity<ApiResponse<List<TagEntity>>> listTags(@RequestParam(required = false) String search) {
                List<TagEntity> tags = tagService.searchTags(search);
                return ResponseEntity.ok(
                                ApiResponseFactory.success(
                                                "TAGS_LISTED",
                                                "Tags fetched successfully.",
                                                tags));
        }

        @PostMapping("/tags")
        @PreAuthorize("@permissionService.canCreate('patients')")
        public ResponseEntity<ApiResponse<TagEntity>> createTag(@RequestBody Map<String, String> request) {
                String name = request.get("name");
                String color = request.get("color");

                if (name == null || name.isBlank()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag name is required");
                }

                TagEntity tag = tagService.createOrGetTag(name, color);
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponseFactory.success(
                                                "TAG_CREATED",
                                                "Tag created successfully.",
                                                tag));
        }
}
