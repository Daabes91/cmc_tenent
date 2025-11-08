package com.clinic.multitenant;

import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentMode;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.appointment.AppointmentStatus;
import com.clinic.modules.core.blog.BlogEntity;
import com.clinic.modules.core.blog.BlogRepository;
import com.clinic.modules.core.blog.BlogStatus;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.patient.GlobalPatientEntity;
import com.clinic.modules.core.patient.GlobalPatientRepository;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.service.ClinicServiceEntity;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test to verify tenant isolation across the system.
 * Tests that:
 * - Multiple tenants can exist with isolated data
 * - Doctors in different tenants can have the same email
 * - Queries filtered by tenant return only that tenant's data
 * - Cross-tenant access attempts return empty results (404 equivalent)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantIsolationIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private GlobalPatientRepository globalPatientRepository;

    @Autowired
    private ClinicServiceRepository serviceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private TenantEntity tenant1;
    private TenantEntity tenant2;
    private DoctorEntity doctorInTenant1;
    private DoctorEntity doctorInTenant2;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data (order matters due to foreign keys)
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        globalPatientRepository.deleteAll();
        serviceRepository.deleteAll();
        doctorRepository.deleteAll();
        blogRepository.deleteAll();
        
        // Get or create tenant 1 (default tenant should exist from migrations)
        tenant1 = tenantRepository.findBySlugIgnoreCase("default")
                .orElseGet(() -> {
                    TenantEntity t = new TenantEntity("default", "Default Clinic");
                    return tenantRepository.save(t);
                });

        // Delete and recreate tenant 2 to ensure clean state
        tenantRepository.findBySlugIgnoreCase("clinic-b").ifPresent(tenantRepository::delete);
        
        // Create tenant 2
        tenant2 = new TenantEntity("clinic-b", "Clinic B");
        tenant2 = tenantRepository.save(tenant2);

        // Create doctor in tenant 1
        doctorInTenant1 = new DoctorEntity(
                "Dr. John Smith",
                "د. جون سميث",
                "General Dentist",
                "طبيب أسنان عام",
                "Experienced dentist",
                "طبيب أسنان ذو خبرة",
                "en,ar"
        );
        doctorInTenant1.updateContactInfo("doctor@example.com", "+1234567890");
        doctorInTenant1.setTenant(tenant1);
        doctorInTenant1 = doctorRepository.save(doctorInTenant1);

        // Create doctor in tenant 2 with SAME email (should be allowed due to tenant scoping)
        doctorInTenant2 = new DoctorEntity(
                "Dr. Jane Doe",
                "د. جين دو",
                "Orthodontist",
                "أخصائي تقويم الأسنان",
                "Specialist in braces",
                "متخصص في التقويم",
                "en,ar"
        );
        doctorInTenant2.updateContactInfo("doctor@example.com", "+0987654321");
        doctorInTenant2.setTenant(tenant2);
        doctorInTenant2 = doctorRepository.save(doctorInTenant2);
    }

    @Test
    void shouldCreateMultipleTenants() {
        // Verify both tenants exist
        assertThat(tenant1).isNotNull();
        assertThat(tenant1.getId()).isNotNull();
        assertThat(tenant1.getSlug()).isEqualTo("default");

        assertThat(tenant2).isNotNull();
        assertThat(tenant2.getId()).isNotNull();
        assertThat(tenant2.getSlug()).isEqualTo("clinic-b");
    }

    @Test
    void shouldAllowSameEmailInDifferentTenants() {
        // Verify both doctors were created successfully with the same email
        assertThat(doctorInTenant1.getEmail()).isEqualTo("doctor@example.com");
        assertThat(doctorInTenant2.getEmail()).isEqualTo("doctor@example.com");
        
        // Verify they are different doctors
        assertThat(doctorInTenant1.getId()).isNotEqualTo(doctorInTenant2.getId());
        assertThat(doctorInTenant1.getFullNameEn()).isEqualTo("Dr. John Smith");
        assertThat(doctorInTenant2.getFullNameEn()).isEqualTo("Dr. Jane Doe");
    }

    @Test
    void shouldReturnOnlyTenant1DoctorsWhenQueryingWithTenant1Context() {
        // Query doctors for tenant 1
        List<DoctorEntity> tenant1Doctors = doctorRepository.findAllByTenantIdAndIsActiveTrue(tenant1.getId());

        // Should return only tenant 1's doctor
        assertThat(tenant1Doctors).hasSize(1);
        assertThat(tenant1Doctors.get(0).getId()).isEqualTo(doctorInTenant1.getId());
        assertThat(tenant1Doctors.get(0).getFullNameEn()).isEqualTo("Dr. John Smith");
        assertThat(tenant1Doctors.get(0).getTenant().getId()).isEqualTo(tenant1.getId());
    }

    @Test
    void shouldReturnOnlyTenant2DoctorsWhenQueryingWithTenant2Context() {
        // Query doctors for tenant 2
        List<DoctorEntity> tenant2Doctors = doctorRepository.findAllByTenantIdAndIsActiveTrue(tenant2.getId());

        // Should return only tenant 2's doctor
        assertThat(tenant2Doctors).hasSize(1);
        assertThat(tenant2Doctors.get(0).getId()).isEqualTo(doctorInTenant2.getId());
        assertThat(tenant2Doctors.get(0).getFullNameEn()).isEqualTo("Dr. Jane Doe");
        assertThat(tenant2Doctors.get(0).getTenant().getId()).isEqualTo(tenant2.getId());
    }

    @Test
    void shouldReturnEmptyWhenAccessingTenant1DoctorFromTenant2Context() {
        // Try to access tenant 1's doctor using tenant 2's context
        Optional<DoctorEntity> result = doctorRepository.findByIdAndTenantId(
                doctorInTenant1.getId(),
                tenant2.getId()
        );

        // Should return empty (equivalent to 404)
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenAccessingTenant2DoctorFromTenant1Context() {
        // Try to access tenant 2's doctor using tenant 1's context
        Optional<DoctorEntity> result = doctorRepository.findByIdAndTenantId(
                doctorInTenant2.getId(),
                tenant1.getId()
        );

        // Should return empty (equivalent to 404)
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindDoctorByEmailOnlyWithinCorrectTenant() {
        // Find doctor by email in tenant 1
        Optional<DoctorEntity> tenant1Result = doctorRepository.findByTenantIdAndEmail(
                tenant1.getId(),
                "doctor@example.com"
        );

        // Should find tenant 1's doctor
        assertThat(tenant1Result).isPresent();
        assertThat(tenant1Result.get().getId()).isEqualTo(doctorInTenant1.getId());
        assertThat(tenant1Result.get().getFullNameEn()).isEqualTo("Dr. John Smith");

        // Find doctor by email in tenant 2
        Optional<DoctorEntity> tenant2Result = doctorRepository.findByTenantIdAndEmail(
                tenant2.getId(),
                "doctor@example.com"
        );

        // Should find tenant 2's doctor
        assertThat(tenant2Result).isPresent();
        assertThat(tenant2Result.get().getId()).isEqualTo(doctorInTenant2.getId());
        assertThat(tenant2Result.get().getFullNameEn()).isEqualTo("Dr. Jane Doe");
    }

    @Test
    void shouldIsolateDoctorsBetweenTenants() {
        // Get all doctors for tenant 1
        List<DoctorEntity> tenant1Doctors = doctorRepository.findAllByTenantIdAndIsActiveTrue(tenant1.getId());
        
        // Get all doctors for tenant 2
        List<DoctorEntity> tenant2Doctors = doctorRepository.findAllByTenantIdAndIsActiveTrue(tenant2.getId());

        // Verify complete isolation
        assertThat(tenant1Doctors).hasSize(1);
        assertThat(tenant2Doctors).hasSize(1);
        
        // Verify no overlap
        assertThat(tenant1Doctors.get(0).getId()).isNotEqualTo(tenant2Doctors.get(0).getId());
        
        // Verify each doctor belongs to correct tenant
        assertThat(tenant1Doctors.get(0).getTenant().getId()).isEqualTo(tenant1.getId());
        assertThat(tenant2Doctors.get(0).getTenant().getId()).isEqualTo(tenant2.getId());
    }

    @Test
    void shouldPreventCrossTenantAppointmentCreation_PatientFromDifferentTenant() {
        // Create a service in tenant 1
        ClinicServiceEntity serviceInTenant1 = new ClinicServiceEntity(
                "general-checkup-t1",
                tenant1,
                "General Checkup",
                "فحص عام",
                "Routine dental checkup",
                "فحص أسنان روتيني"
        );
        serviceInTenant1 = serviceRepository.save(serviceInTenant1);

        // Associate service with doctor in tenant 1
        
        doctorInTenant1.getServices().add(serviceInTenant1);
        doctorInTenant1 = doctorRepository.save(doctorInTenant1);

        // Create a global patient
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
                "patient@example.com",
                "+1234567890",
                "hashedPassword123",
                LocalDate.of(1990, 1, 15)
        );
        globalPatient = globalPatientRepository.save(globalPatient);

        // Create patient profile in tenant 2
        PatientEntity patientInTenant2 = new PatientEntity(
                "Jane",
                "Doe",
                "patient@example.com",
                "+1234567890"
        );
        patientInTenant2.setTenant(tenant2);
        patientInTenant2.setGlobalPatient(globalPatient);
        patientInTenant2.setDateOfBirth(LocalDate.of(1990, 1, 15));
        patientInTenant2 = patientRepository.save(patientInTenant2);

        // Attempt to query patient from tenant 1 context (should return empty)
        Optional<PatientEntity> patientFromTenant1Context = patientRepository.findByIdAndTenantId(
                patientInTenant2.getId(),
                tenant1.getId()
        );

        // Verify patient is not accessible from tenant 1 context
        assertThat(patientFromTenant1Context).isEmpty();

        // This simulates what would happen in the service layer:
        // When trying to create an appointment in tenant 1 with a patient from tenant 2,
        // the findByIdAndTenantId query would return empty, causing a 404 error
        
        // Verify we can access the patient from tenant 2 context
        Optional<PatientEntity> patientFromTenant2Context = patientRepository.findByIdAndTenantId(
                patientInTenant2.getId(),
                tenant2.getId()
        );
        assertThat(patientFromTenant2Context).isPresent();
    }

    @Test
    void shouldPreventCrossTenantAppointmentCreation_DoctorFromDifferentTenant() {
        // Create a service in tenant 2
        ClinicServiceEntity serviceInTenant2 = new ClinicServiceEntity(
                "general-checkup-t2",
                tenant2,
                "General Checkup",
                "فحص عام",
                "Routine dental checkup",
                "فحص أسنان روتيني"
        );
        serviceInTenant2 = serviceRepository.save(serviceInTenant2);

        // Associate service with doctor in tenant 2
        
        doctorInTenant2.getServices().add(serviceInTenant2);
        doctorInTenant2 = doctorRepository.save(doctorInTenant2);

        // Create a global patient
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
                "patient2@example.com",
                "+0987654321",
                "hashedPassword456",
                LocalDate.of(1985, 5, 20)
        );
        globalPatient = globalPatientRepository.save(globalPatient);

        // Create patient profile in tenant 1
        PatientEntity patientInTenant1 = new PatientEntity(
                "John",
                "Smith",
                "patient2@example.com",
                "+0987654321"
        );
        patientInTenant1.setTenant(tenant1);
        patientInTenant1.setGlobalPatient(globalPatient);
        patientInTenant1.setDateOfBirth(LocalDate.of(1985, 5, 20));
        patientInTenant1 = patientRepository.save(patientInTenant1);

        // Attempt to query doctor from tenant 1 context (should return empty)
        Optional<DoctorEntity> doctorFromTenant1Context = doctorRepository.findByIdAndTenantId(
                doctorInTenant2.getId(),
                tenant1.getId()
        );

        // Verify doctor is not accessible from tenant 1 context
        assertThat(doctorFromTenant1Context).isEmpty();

        // This simulates what would happen in the service layer:
        // When trying to create an appointment in tenant 1 with a doctor from tenant 2,
        // the findByIdAndTenantId query would return empty, causing a 404 error
        
        // Verify we can access the doctor from tenant 2 context
        Optional<DoctorEntity> doctorFromTenant2Context = doctorRepository.findByIdAndTenantId(
                doctorInTenant2.getId(),
                tenant2.getId()
        );
        assertThat(doctorFromTenant2Context).isPresent();
    }

    @Test
    void shouldPreventCrossTenantAppointmentCreation_ServiceFromDifferentTenant() {
        // Create services in both tenants
        ClinicServiceEntity serviceInTenant1 = new ClinicServiceEntity(
                "cleaning-t1",
                tenant1,
                "Teeth Cleaning",
                "تنظيف الأسنان",
                "Professional teeth cleaning",
                "تنظيف أسنان احترافي"
        );
        serviceInTenant1 = serviceRepository.save(serviceInTenant1);

        ClinicServiceEntity serviceInTenant2 = new ClinicServiceEntity(
                "cleaning-t2",
                tenant2,
                "Teeth Cleaning",
                "تنظيف الأسنان",
                "Professional teeth cleaning",
                "تنظيف أسنان احترافي"
        );
        serviceInTenant2 = serviceRepository.save(serviceInTenant2);

        // Attempt to query service from tenant 1 context
        Optional<ClinicServiceEntity> serviceFromTenant1Context = serviceRepository.findByIdAndTenantId(
                serviceInTenant2.getId(),
                tenant1.getId()
        );

        // Verify service is not accessible from tenant 1 context
        assertThat(serviceFromTenant1Context).isEmpty();

        // Verify we can access the service from tenant 2 context
        Optional<ClinicServiceEntity> serviceFromTenant2Context = serviceRepository.findByIdAndTenantId(
                serviceInTenant2.getId(),
                tenant2.getId()
        );
        assertThat(serviceFromTenant2Context).isPresent();
    }

    @Test
    void shouldSuccessfullyCreateAppointmentWithinSameTenant() {
        // Create a service in tenant 1
        ClinicServiceEntity serviceInTenant1 = new ClinicServiceEntity(
                "consultation-t1",
                tenant1,
                "Consultation",
                "استشارة",
                "Initial consultation",
                "استشارة أولية"
        );
        serviceInTenant1 = serviceRepository.save(serviceInTenant1);

        // Associate service with doctor in tenant 1
        
        doctorInTenant1.getServices().add(serviceInTenant1);
        doctorInTenant1 = doctorRepository.save(doctorInTenant1);

        // Create a global patient
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
                "patient3@example.com",
                "+1122334455",
                "hashedPassword789",
                LocalDate.of(1992, 8, 10)
        );
        globalPatient = globalPatientRepository.save(globalPatient);

        // Create patient profile in tenant 1
        PatientEntity patientInTenant1 = new PatientEntity(
                "Alice",
                "Johnson",
                "patient3@example.com",
                "+1122334455"
        );
        patientInTenant1.setTenant(tenant1);
        patientInTenant1.setGlobalPatient(globalPatient);
        patientInTenant1.setDateOfBirth(LocalDate.of(1992, 8, 10));
        patientInTenant1 = patientRepository.save(patientInTenant1);

        // Verify all entities belong to tenant 1
        assertThat(doctorInTenant1.getTenant().getId()).isEqualTo(tenant1.getId());
        assertThat(patientInTenant1.getTenant().getId()).isEqualTo(tenant1.getId());
        assertThat(serviceInTenant1.getTenant().getId()).isEqualTo(tenant1.getId());

        // Create appointment in tenant 1 with all entities from tenant 1
        Instant scheduledAt = Instant.now().plus(1, ChronoUnit.DAYS);
        AppointmentEntity appointment = new AppointmentEntity(
                patientInTenant1,
                doctorInTenant1,
                serviceInTenant1,
                scheduledAt,
                AppointmentStatus.SCHEDULED,
                AppointmentMode.CLINIC_VISIT,
                "Initial consultation appointment"
        );
        appointment.setTenant(tenant1);
        appointment = appointmentRepository.save(appointment);

        // Verify appointment was created successfully
        assertThat(appointment.getId()).isNotNull();
        assertThat(appointment.getTenant().getId()).isEqualTo(tenant1.getId());
        assertThat(appointment.getPatient().getId()).isEqualTo(patientInTenant1.getId());
        assertThat(appointment.getDoctor().getId()).isEqualTo(doctorInTenant1.getId());
        assertThat(appointment.getService().getId()).isEqualTo(serviceInTenant1.getId());

        // Verify appointment can be queried from tenant 1 context
        Optional<AppointmentEntity> foundAppointment = appointmentRepository.findByIdAndTenantId(
                appointment.getId(),
                tenant1.getId()
        );
        assertThat(foundAppointment).isPresent();
        assertThat(foundAppointment.get().getId()).isEqualTo(appointment.getId());

        // Verify appointment cannot be queried from tenant 2 context
        Optional<AppointmentEntity> appointmentFromTenant2 = appointmentRepository.findByIdAndTenantId(
                appointment.getId(),
                tenant2.getId()
        );
        assertThat(appointmentFromTenant2).isEmpty();
    }

    @Test
    void shouldIsolateAppointmentsBetweenTenants() {
        // Create services in both tenants
        ClinicServiceEntity serviceInTenant1 = new ClinicServiceEntity(
                "service-t1",
                tenant1,
                "Service T1",
                "خدمة ت1",
                "Service for tenant 1",
                "خدمة للمستأجر 1"
        );
        serviceInTenant1 = serviceRepository.save(serviceInTenant1);

        ClinicServiceEntity serviceInTenant2 = new ClinicServiceEntity(
                "service-t2",
                tenant2,
                "Service T2",
                "خدمة ت2",
                "Service for tenant 2",
                "خدمة للمستأجر 2"
        );
        serviceInTenant2 = serviceRepository.save(serviceInTenant2);

        // Associate services with doctors
        
        doctorInTenant1.getServices().add(serviceInTenant1);
        doctorInTenant1 = doctorRepository.save(doctorInTenant1);

        
        doctorInTenant2.getServices().add(serviceInTenant2);
        doctorInTenant2 = doctorRepository.save(doctorInTenant2);

        // Create global patients
        GlobalPatientEntity globalPatient1 = new GlobalPatientEntity(
                "patient4@example.com",
                "+2233445566",
                "hashedPassword111",
                LocalDate.of(1988, 3, 15)
        );
        globalPatient1 = globalPatientRepository.save(globalPatient1);

        GlobalPatientEntity globalPatient2 = new GlobalPatientEntity(
                "patient5@example.com",
                "+3344556677",
                "hashedPassword222",
                LocalDate.of(1995, 7, 25)
        );
        globalPatient2 = globalPatientRepository.save(globalPatient2);

        // Create patient profiles in both tenants
        PatientEntity patientInTenant1 = new PatientEntity(
                "Bob",
                "Williams",
                "patient4@example.com",
                "+2233445566"
        );
        patientInTenant1.setTenant(tenant1);
        patientInTenant1.setGlobalPatient(globalPatient1);
        patientInTenant1 = patientRepository.save(patientInTenant1);

        PatientEntity patientInTenant2 = new PatientEntity(
                "Carol",
                "Davis",
                "patient5@example.com",
                "+3344556677"
        );
        patientInTenant2.setTenant(tenant2);
        patientInTenant2.setGlobalPatient(globalPatient2);
        patientInTenant2 = patientRepository.save(patientInTenant2);

        // Create appointments in both tenants
        Instant scheduledAt1 = Instant.now().plus(2, ChronoUnit.DAYS);
        AppointmentEntity appointmentInTenant1 = new AppointmentEntity(
                patientInTenant1,
                doctorInTenant1,
                serviceInTenant1,
                scheduledAt1,
                AppointmentStatus.SCHEDULED,
                AppointmentMode.CLINIC_VISIT,
                "Appointment in tenant 1"
        );
        appointmentInTenant1.setTenant(tenant1);
        appointmentInTenant1 = appointmentRepository.save(appointmentInTenant1);

        Instant scheduledAt2 = Instant.now().plus(3, ChronoUnit.DAYS);
        AppointmentEntity appointmentInTenant2 = new AppointmentEntity(
                patientInTenant2,
                doctorInTenant2,
                serviceInTenant2,
                scheduledAt2,
                AppointmentStatus.SCHEDULED,
                AppointmentMode.CLINIC_VISIT,
                "Appointment in tenant 2"
        );
        appointmentInTenant2.setTenant(tenant2);
        appointmentInTenant2 = appointmentRepository.save(appointmentInTenant2);

        // Query appointments for tenant 1
        List<AppointmentEntity> tenant1Appointments = appointmentRepository.findAllByTenantId(tenant1.getId());

        // Query appointments for tenant 2
        List<AppointmentEntity> tenant2Appointments = appointmentRepository.findAllByTenantId(tenant2.getId());

        // Verify complete isolation
        assertThat(tenant1Appointments).hasSize(1);
        assertThat(tenant2Appointments).hasSize(1);

        // Verify tenant 1 only sees its appointment
        assertThat(tenant1Appointments.get(0).getId()).isEqualTo(appointmentInTenant1.getId());
        assertThat(tenant1Appointments.get(0).getTenant().getId()).isEqualTo(tenant1.getId());

        // Verify tenant 2 only sees its appointment
        assertThat(tenant2Appointments.get(0).getId()).isEqualTo(appointmentInTenant2.getId());
        assertThat(tenant2Appointments.get(0).getTenant().getId()).isEqualTo(tenant2.getId());

        // Verify no overlap
        assertThat(tenant1Appointments.get(0).getId()).isNotEqualTo(tenant2Appointments.get(0).getId());
    }

    @Test
    void shouldAllowSameSlugInDifferentTenants_AndEnforceUniqueConstraintWithinTenant() {
        // Verify tenants have different IDs
        assertThat(tenant1.getId()).isNotNull();
        assertThat(tenant2.getId()).isNotNull();
        assertThat(tenant1.getId()).isNotEqualTo(tenant2.getId());
        
        // Create blog with slug "test-blog" in tenant 1
        BlogEntity blogInTenant1 = new BlogEntity();
        blogInTenant1.setTenant(tenant1);
        blogInTenant1.setTitle("Test Blog Tenant 1");
        blogInTenant1.setSlug("test-blog");
        blogInTenant1.setContent("This is a test blog in tenant 1");
        blogInTenant1.setStatus(BlogStatus.PUBLISHED);
        blogInTenant1.setLocale("en");
        blogInTenant1 = blogRepository.save(blogInTenant1);

        // Verify blog was created successfully in tenant 1
        assertThat(blogInTenant1.getId()).isNotNull();
        assertThat(blogInTenant1.getSlug()).isEqualTo("test-blog");
        assertThat(blogInTenant1.getTenant().getId()).isEqualTo(tenant1.getId());

        // Create blog with SAME slug "test-blog" in tenant 2 (should succeed)
        BlogEntity blogInTenant2 = new BlogEntity();
        blogInTenant2.setTenant(tenant2);
        blogInTenant2.setTitle("Test Blog Tenant 2");
        blogInTenant2.setSlug("test-blog");
        blogInTenant2.setContent("This is a test blog in tenant 2");
        blogInTenant2.setStatus(BlogStatus.PUBLISHED);
        blogInTenant2.setLocale("en");
        blogInTenant2 = blogRepository.save(blogInTenant2);

        // Verify blog was created successfully in tenant 2 with same slug
        assertThat(blogInTenant2.getId()).isNotNull();
        assertThat(blogInTenant2.getSlug()).isEqualTo("test-blog");
        assertThat(blogInTenant2.getTenant().getId()).isEqualTo(tenant2.getId());

        // Verify they are different blogs
        assertThat(blogInTenant1.getId()).isNotEqualTo(blogInTenant2.getId());

        // Verify tenant 1 can query its blog by slug
        Optional<BlogEntity> foundInTenant1 = blogRepository.findByTenantIdAndSlug(tenant1.getId(), "test-blog");
        assertThat(foundInTenant1).isPresent();
        assertThat(foundInTenant1.get().getId()).isEqualTo(blogInTenant1.getId());
        assertThat(foundInTenant1.get().getTitle()).isEqualTo("Test Blog Tenant 1");

        // Verify tenant 2 can query its blog by slug
        Optional<BlogEntity> foundInTenant2 = blogRepository.findByTenantIdAndSlug(tenant2.getId(), "test-blog");
        assertThat(foundInTenant2).isPresent();
        assertThat(foundInTenant2.get().getId()).isEqualTo(blogInTenant2.getId());
        assertThat(foundInTenant2.get().getTitle()).isEqualTo("Test Blog Tenant 2");

        // Flush to ensure blogs are persisted before testing constraint
        entityManager.flush();
        
        // Verify only 2 blogs exist so far (one in each tenant)
        List<BlogEntity> allBlogsInTenant1 = blogRepository.findAllByTenantId(tenant1.getId());
        List<BlogEntity> allBlogsInTenant2 = blogRepository.findAllByTenantId(tenant2.getId());

        assertThat(allBlogsInTenant1).hasSize(1);
        assertThat(allBlogsInTenant2).hasSize(1);
        
        // Attempt to create another blog with slug "test-blog" in tenant 1 (should fail)
        BlogEntity duplicateBlogInTenant1 = new BlogEntity();
        duplicateBlogInTenant1.setTenant(tenant1);
        duplicateBlogInTenant1.setTitle("Duplicate Test Blog Tenant 1");
        duplicateBlogInTenant1.setSlug("test-blog");
        duplicateBlogInTenant1.setContent("This should fail due to unique constraint");
        duplicateBlogInTenant1.setStatus(BlogStatus.DRAFT);
        duplicateBlogInTenant1.setLocale("en");

        // Verify operation fails with unique constraint violation
        assertThatThrownBy(() -> {
            blogRepository.saveAndFlush(duplicateBlogInTenant1);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRespectTenantBoundariesForUpdateAndDeleteOperations() {
        // Create a doctor in tenant 1
        DoctorEntity doctorForUpdateTest = new DoctorEntity(
                "Dr. Update Test",
                "د. اختبار التحديث",
                "Test Specialist",
                "أخصائي اختبار",
                "Test bio",
                "سيرة ذاتية اختبارية",
                "en,ar"
        );
        doctorForUpdateTest.updateContactInfo("update.test@example.com", "+1111111111");
        doctorForUpdateTest.setTenant(tenant1);
        doctorForUpdateTest = doctorRepository.save(doctorForUpdateTest);

        Long doctorId = doctorForUpdateTest.getId();
        String originalName = doctorForUpdateTest.getFullNameEn();

        // Attempt to update doctor from tenant 2 context (should return empty/404)
        Optional<DoctorEntity> doctorFromTenant2ForUpdate = doctorRepository.findByIdAndTenantId(
                doctorId,
                tenant2.getId()
        );

        // Verify operation fails with 404 (empty result)
        assertThat(doctorFromTenant2ForUpdate).isEmpty();

        // Attempt to delete doctor from tenant 2 context (should return empty/404)
        Optional<DoctorEntity> doctorFromTenant2ForDelete = doctorRepository.findByIdAndTenantId(
                doctorId,
                tenant2.getId()
        );

        // Verify operation fails with 404 (empty result)
        assertThat(doctorFromTenant2ForDelete).isEmpty();

        // Update from tenant 1 context (should succeed)
        Optional<DoctorEntity> doctorFromTenant1ForUpdate = doctorRepository.findByIdAndTenantId(
                doctorId,
                tenant1.getId()
        );

        // Verify operation succeeds
        assertThat(doctorFromTenant1ForUpdate).isPresent();
        DoctorEntity doctorToUpdate = doctorFromTenant1ForUpdate.get();
        
        // Perform update
        String updatedName = "Dr. Updated Name";
        doctorToUpdate.updateDetails(
                updatedName,
                "د. اسم محدث",
                "Updated Specialty",
                "تخصص محدث",
                "Updated bio",
                "سيرة ذاتية محدثة",
                "en,ar"
        );
        doctorRepository.save(doctorToUpdate);

        // Verify update was successful
        Optional<DoctorEntity> updatedDoctor = doctorRepository.findByIdAndTenantId(
                doctorId,
                tenant1.getId()
        );
        assertThat(updatedDoctor).isPresent();
        assertThat(updatedDoctor.get().getFullNameEn()).isEqualTo(updatedName);
        assertThat(updatedDoctor.get().getFullNameEn()).isNotEqualTo(originalName);

        // Delete from tenant 1 context (should succeed)
        Optional<DoctorEntity> doctorFromTenant1ForDelete = doctorRepository.findByIdAndTenantId(
                doctorId,
                tenant1.getId()
        );

        // Verify operation succeeds
        assertThat(doctorFromTenant1ForDelete).isPresent();
        DoctorEntity doctorToDelete = doctorFromTenant1ForDelete.get();
        
        // Perform delete
        doctorRepository.delete(doctorToDelete);

        // Verify delete was successful
        Optional<DoctorEntity> deletedDoctor = doctorRepository.findByIdAndTenantId(
                doctorId,
                tenant1.getId()
        );
        assertThat(deletedDoctor).isEmpty();

        // Verify doctor is also not accessible from tenant 2 context
        Optional<DoctorEntity> deletedDoctorFromTenant2 = doctorRepository.findByIdAndTenantId(
                doctorId,
                tenant2.getId()
        );
        assertThat(deletedDoctorFromTenant2).isEmpty();
    }
}
