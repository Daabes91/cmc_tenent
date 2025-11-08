package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.GlobalSearchResponse;
import com.clinic.modules.admin.dto.GlobalSearchResultItem;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.admin.staff.service.PermissionService;
import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.insurance.InsuranceCompanyEntity;
import com.clinic.modules.core.insurance.InsuranceCompanyRepository;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.core.treatment.TreatmentPlanEntity;
import com.clinic.modules.core.treatment.TreatmentPlanRepository;
import com.clinic.modules.core.treatment.MaterialCatalogEntity;
import com.clinic.modules.core.treatment.MaterialCatalogRepository;
import com.clinic.modules.core.blog.BlogEntity;
import com.clinic.modules.core.blog.BlogRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class GlobalSearchService {

    private static final DateTimeFormatter APPOINTMENT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d • HH:mm").withLocale(Locale.ENGLISH);

    private final PermissionService permissionService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final StaffUserRepository staffUserRepository;
    private final InsuranceCompanyRepository insuranceCompanyRepository;
    private final MaterialCatalogRepository materialCatalogRepository;
    private final BlogRepository blogRepository;
    private final TenantContextHolder tenantContextHolder;

    public GlobalSearchService(PermissionService permissionService,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository,
                               ClinicServiceRepository clinicServiceRepository,
                               AppointmentRepository appointmentRepository,
                               TreatmentPlanRepository treatmentPlanRepository,
                               StaffUserRepository staffUserRepository,
                               InsuranceCompanyRepository insuranceCompanyRepository,
                               MaterialCatalogRepository materialCatalogRepository,
                               BlogRepository blogRepository,
                               TenantContextHolder tenantContextHolder) {
        this.permissionService = permissionService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.clinicServiceRepository = clinicServiceRepository;
        this.appointmentRepository = appointmentRepository;
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.staffUserRepository = staffUserRepository;
        this.insuranceCompanyRepository = insuranceCompanyRepository;
        this.materialCatalogRepository = materialCatalogRepository;
        this.blogRepository = blogRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public GlobalSearchResponse search(String query, int limitPerType, ZoneId zoneId) {
        if (query == null) {
            return new GlobalSearchResponse(List.of());
        }

        String term = query.trim();
        if (term.length() < 2) {
            return new GlobalSearchResponse(List.of());
        }

        int limit = Math.max(1, limitPerType);
        Pageable pageable = PageRequest.of(0, limit);
        Long tenantId = tenantContextHolder.requireTenantId();

        List<GlobalSearchResultItem> results = new ArrayList<>();

        if (permissionService.canView("patients")) {
            patientRepository.searchPatientsByTenantId(tenantId, term, pageable).forEach(patient ->
                    results.add(mapPatient(patient))
            );
        }

        if (permissionService.canView("doctors")) {
            doctorRepository.searchDoctors(tenantId, term, pageable).forEach(doctor ->
                    results.add(mapDoctor(doctor))
            );
        }

        if (permissionService.canView("appointments")) {
            appointmentRepository.searchByTenantIdAndTerm(tenantId, term, pageable).forEach(appointment ->
                    results.add(mapAppointment(appointment, zoneId))
            );
        }

        if (permissionService.canView("services")) {
            clinicServiceRepository.searchServices(tenantId, term, pageable).forEach(service ->
                    results.add(mapService(service))
            );
        }

        if (permissionService.canView("treatmentPlans")) {
            treatmentPlanRepository.searchTreatmentPlans(term, pageable).forEach(plan ->
                    results.add(mapTreatmentPlan(plan))
            );
        }

        if (permissionService.canView("staff")) {
            staffUserRepository.searchStaff(tenantId, term, pageable).forEach(staff ->
                    results.add(mapStaff(staff))
            );
        }

        if (permissionService.isAdmin()) {
            insuranceCompanyRepository.searchInsuranceCompanies(term, pageable).forEach(company ->
                    results.add(mapInsurance(company))
            );
        }

        if (permissionService.canView("services") || permissionService.isAdmin()) {
            materialCatalogRepository.searchMaterialsByTenantId(tenantId, term, pageable).forEach(material ->
                    results.add(mapMaterial(material))
            );
        }

        if (permissionService.canView("blogs")) {
            blogRepository.searchBlogs(term, pageable).forEach(blog ->
                    results.add(mapBlog(blog))
            );
        }

        return new GlobalSearchResponse(results);
    }

    private GlobalSearchResultItem mapPatient(PatientEntity patient) {
        String fullName = joinNonBlank(" ", patient.getFirstName(), patient.getLastName());
        String subtitle = joinNonBlank(" • ",
                nullToEmpty(patient.getEmail()),
                patient.getPhone()
        );

        return new GlobalSearchResultItem(
                "patients",
                String.valueOf(patient.getId()),
                fullName.isBlank() ? "Unnamed patient" : fullName,
                subtitle,
                "/patients/" + patient.getId(),
                "i-lucide-user"
        );
    }

    private GlobalSearchResultItem mapDoctor(DoctorEntity doctor) {
        String subtitle = joinNonBlank(" • ",
                doctor.getSpecialtyEn(),
                doctor.getSpecialtyAr()
        );

        return new GlobalSearchResultItem(
                "doctors",
                String.valueOf(doctor.getId()),
                defaultString(doctor.getFullNameEn(), doctor.getFullNameAr(), "Doctor"),
                subtitle,
                "/doctors/" + doctor.getId(),
                "i-lucide-stethoscope"
        );
    }

    private GlobalSearchResultItem mapService(ClinicServiceEntity service) {
        String subtitle = joinNonBlank(" • ",
                service.getSummaryEn(),
                service.getSummaryAr()
        );

        return new GlobalSearchResultItem(
                "services",
                String.valueOf(service.getId()),
                defaultString(service.getNameEn(), service.getNameAr(), "Service"),
                subtitle,
                "/services/" + service.getId(),
                "i-lucide-layers"
        );
    }

    private GlobalSearchResultItem mapAppointment(AppointmentEntity appointment, ZoneId zoneId) {
        String patientName = appointment.getPatient() != null
                ? joinNonBlank(" ", appointment.getPatient().getFirstName(), appointment.getPatient().getLastName())
                : "Patient";
        String doctorName = appointment.getDoctor() != null
                ? defaultString(appointment.getDoctor().getFullNameEn(), appointment.getDoctor().getFullNameAr(), "Doctor")
                : "Doctor";

        ZonedDateTime scheduledAt = appointment.getScheduledAt() != null
                ? appointment.getScheduledAt().atZone(zoneId)
                : null;

        String timeLabel = scheduledAt != null ? APPOINTMENT_TIME_FORMATTER.format(scheduledAt) : "";

        String subtitle = joinNonBlank(" • ",
                doctorName,
                appointment.getService() != null ? appointment.getService().getNameEn() : null,
                timeLabel,
                appointment.getStatus() != null ? appointment.getStatus().name() : null
        );

        return new GlobalSearchResultItem(
                "appointments",
                String.valueOf(appointment.getId()),
                patientName,
                subtitle,
                "/appointments/" + appointment.getId(),
                "i-lucide-calendar-clock"
        );
    }

    private GlobalSearchResultItem mapTreatmentPlan(TreatmentPlanEntity plan) {
        String patientName = plan.getPatient() != null
                ? joinNonBlank(" ", plan.getPatient().getFirstName(), plan.getPatient().getLastName())
                : "Patient";
        String doctorName = plan.getDoctor() != null
                ? defaultString(plan.getDoctor().getFullNameEn(), plan.getDoctor().getFullNameAr(), null)
                : null;
        String serviceName = plan.getTreatmentType() != null
                ? defaultString(plan.getTreatmentType().getNameEn(), plan.getTreatmentType().getNameAr(), null)
                : null;

        String subtitle = joinNonBlank(" • ",
                doctorName,
                serviceName,
                plan.getStatus() != null ? plan.getStatus().name() : null
        );

        return new GlobalSearchResultItem(
                "treatmentPlans",
                String.valueOf(plan.getId()),
                patientName,
                subtitle,
                "/treatment-plans/" + plan.getId(),
                "i-lucide-clipboard-list"
        );
    }

    private GlobalSearchResultItem mapStaff(StaffUser staff) {
        String subtitle = joinNonBlank(" • ",
                staff.getEmail(),
                staff.getRole() != null ? staff.getRole().name() : null,
                staff.getStatus() != null ? staff.getStatus().name() : null
        );

        return new GlobalSearchResultItem(
                "staff",
                String.valueOf(staff.getId()),
                defaultString(staff.getFullName(), staff.getEmail(), "Staff"),
                subtitle,
                "/staff/" + staff.getId(),
                "i-lucide-shield-check"
        );
    }

    private GlobalSearchResultItem mapInsurance(InsuranceCompanyEntity company) {
        String subtitle = joinNonBlank(" • ",
                company.getEmail(),
                company.getPhone(),
                company.getWebsiteUrl()
        );

        return new GlobalSearchResultItem(
                "insurance",
                String.valueOf(company.getId()),
                defaultString(company.getNameEn(), company.getNameAr(), "Insurance"),
                subtitle,
                "/insurance-companies/" + company.getId(),
                "i-lucide-building-2"
        );
    }

    private GlobalSearchResultItem mapMaterial(MaterialCatalogEntity material) {
        String subtitle = joinNonBlank(" • ",
                material.getDescription(),
                material.getUnitOfMeasure(),
                material.getActive() ? "Active" : "Inactive"
        );

        return new GlobalSearchResultItem(
                "materials",
                String.valueOf(material.getId()),
                material.getName(),
                subtitle,
                "/materials",
                "i-lucide-package"
        );
    }

    private GlobalSearchResultItem mapBlog(BlogEntity blog) {
        String subtitle = joinNonBlank(" • ",
                blog.getAuthorName(),
                blog.getStatus() != null ? blog.getStatus().name() : null,
                blog.getLocale()
        );

        return new GlobalSearchResultItem(
                "blogs",
                String.valueOf(blog.getId()),
                blog.getTitle(),
                subtitle,
                "/blogs/" + blog.getId(),
                "i-lucide-newspaper"
        );
    }

    private static String joinNonBlank(String delimiter, String... parts) {
        return Stream.of(parts)
                .filter(part -> part != null && !part.trim().isEmpty())
                .reduce((left, right) -> left + delimiter + right)
                .orElse("");
    }

    private static String defaultString(String primary, String fallback, String defaultValue) {
        if (primary != null && !primary.trim().isEmpty()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.trim().isEmpty()) {
            return fallback.trim();
        }
        return defaultValue != null ? defaultValue : "";
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
