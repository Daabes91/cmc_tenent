package com.clinic.multitenant;

import com.clinic.modules.core.patient.GlobalPatientEntity;
import com.clinic.modules.core.patient.GlobalPatientRepository;
import com.clinic.modules.core.patient.GlobalPatientService;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify the global patient model implementation.
 * Tests that:
 * - A global patient record is created when a patient registers
 * - The same global patient is reused when registering at a different tenant
 * - Each tenant has its own patient profile linked to the global patient
 * - Patients can authenticate from any tenant subdomain
 * - Each tenant only sees their own patient profile
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class GlobalPatientModelIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private GlobalPatientRepository globalPatientRepository;

    @Autowired
    private GlobalPatientService globalPatientService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TenantService tenantService;

    private TenantEntity tenant1;
    private TenantEntity tenant2;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        patientRepository.deleteAll();
        globalPatientRepository.deleteAll();
        
        // Get or create tenant 1 (default tenant should exist from migrations)
        tenant1 = tenantRepository.findBySlugIgnoreCase("default")
                .orElseGet(() -> {
                    TenantEntity t = new TenantEntity("default", "Default Clinic");
                    return tenantRepository.save(t);
                });

        // Create tenant 2
        tenant2 = new TenantEntity("clinic-b", "Clinic B");
        tenant2 = tenantRepository.save(tenant2);
    }

    @Test
    void shouldCreateGlobalPatientWhenPatientRegistersInTenant1() {
        // Given: Patient registration data
        String email = "test@example.com";
        String phone = "+1234567890";
        LocalDate dob = LocalDate.of(1990, 1, 15);
        String password = "SecurePassword123!";

        // When: Patient registers in tenant 1
        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email, phone, dob, password
        );

        // Create tenant profile
        PatientEntity patientProfile = new PatientEntity("John", "Smith", email, phone);
        patientProfile.setTenant(tenant1);
        patientProfile.setGlobalPatient(globalPatient);
        patientProfile.setDateOfBirth(dob);
        patientRepository.save(patientProfile);

        // Then: Global patient record should be created
        assertThat(globalPatient).isNotNull();
        assertThat(globalPatient.getId()).isNotNull();
        assertThat(globalPatient.getEmail()).isEqualTo(email);
        assertThat(globalPatient.getPhone()).isEqualTo(phone);
        assertThat(globalPatient.getDateOfBirth()).isEqualTo(dob);
        assertThat(globalPatient.getExternalId()).startsWith("PAT-");
        assertThat(globalPatient.getPasswordHash()).isNotNull();
        assertThat(globalPatient.getPasswordHash()).isNotEqualTo(password); // Should be hashed

        // Verify global patient can be found by email
        Optional<GlobalPatientEntity> found = globalPatientRepository.findByEmail(email);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(globalPatient.getId());

        // Verify tenant profile is created
        assertThat(patientProfile.getId()).isNotNull();
        assertThat(patientProfile.getTenant().getId()).isEqualTo(tenant1.getId());
        assertThat(patientProfile.getGlobalPatient().getId()).isEqualTo(globalPatient.getId());
    }

    @Test
    void shouldReuseSameGlobalPatientWhenRegisteringInTenant2() {
        // Given: Patient already registered in tenant 1
        String email = "test@example.com";
        String phone = "+1234567890";
        LocalDate dob = LocalDate.of(1990, 1, 15);
        String password = "SecurePassword123!";

        // Create patient in tenant 1
        GlobalPatientEntity globalPatient1 = globalPatientService.findOrCreateGlobalPatient(
                email, phone, dob, password
        );
        PatientEntity tenant1Profile = new PatientEntity("John", "Smith", email, phone);
        tenant1Profile.setTenant(tenant1);
        tenant1Profile.setGlobalPatient(globalPatient1);
        tenant1Profile.setDateOfBirth(dob);
        patientRepository.save(tenant1Profile);

        // When: Same patient registers in tenant 2
        GlobalPatientEntity globalPatient2 = globalPatientService.findOrCreateGlobalPatient(
                email, phone, dob, password
        );
        PatientEntity tenant2Profile = new PatientEntity("جون", "سميث", email, phone);
        tenant2Profile.setTenant(tenant2);
        tenant2Profile.setGlobalPatient(globalPatient2);
        tenant2Profile.setDateOfBirth(dob);
        patientRepository.save(tenant2Profile);

        // Then: Same global patient should be reused
        assertThat(globalPatient2.getId()).isEqualTo(globalPatient1.getId());
        assertThat(globalPatient2.getEmail()).isEqualTo(email);

        // Verify only one global patient exists
        List<GlobalPatientEntity> allGlobalPatients = globalPatientRepository.findAll();
        assertThat(allGlobalPatients).hasSize(1);
        assertThat(allGlobalPatients.get(0).getId()).isEqualTo(globalPatient1.getId());

        // Verify two tenant profiles exist
        List<PatientEntity> allPatientProfiles = patientRepository.findAll();
        assertThat(allPatientProfiles).hasSize(2);

        // Verify tenant profiles are different but linked to same global patient
        assertThat(tenant1Profile.getId()).isNotEqualTo(tenant2Profile.getId());
        assertThat(tenant1Profile.getGlobalPatient().getId()).isEqualTo(globalPatient1.getId());
        assertThat(tenant2Profile.getGlobalPatient().getId()).isEqualTo(globalPatient1.getId());
    }

    @Test
    void shouldAllowDifferentNamesPerTenantForSameGlobalPatient() {
        // Given: Patient registered in both tenants
        String email = "test@example.com";
        String phone = "+1234567890";
        LocalDate dob = LocalDate.of(1990, 1, 15);
        String password = "SecurePassword123!";

        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email, phone, dob, password
        );

        // When: Create profiles with different names in each tenant
        PatientEntity tenant1Profile = new PatientEntity("John", "Smith", email, phone);
        tenant1Profile.setTenant(tenant1);
        tenant1Profile.setGlobalPatient(globalPatient);
        tenant1Profile.setDateOfBirth(dob);
        patientRepository.save(tenant1Profile);

        PatientEntity tenant2Profile = new PatientEntity("جون", "سميث", email, phone);
        tenant2Profile.setTenant(tenant2);
        tenant2Profile.setGlobalPatient(globalPatient);
        tenant2Profile.setDateOfBirth(dob);
        patientRepository.save(tenant2Profile);

        // Then: Each tenant should see their own name representation
        Optional<PatientEntity> tenant1Result = patientRepository.findByTenantIdAndGlobalPatient_Email(
                tenant1.getId(), email
        );
        assertThat(tenant1Result).isPresent();
        assertThat(tenant1Result.get().getFirstName()).isEqualTo("John");
        assertThat(tenant1Result.get().getLastName()).isEqualTo("Smith");

        Optional<PatientEntity> tenant2Result = patientRepository.findByTenantIdAndGlobalPatient_Email(
                tenant2.getId(), email
        );
        assertThat(tenant2Result).isPresent();
        assertThat(tenant2Result.get().getFirstName()).isEqualTo("جون");
        assertThat(tenant2Result.get().getLastName()).isEqualTo("سميث");
    }

    @Test
    void shouldValidatePasswordAgainstGlobalPatientRecord() {
        // Given: Patient registered with a password
        String email = "test@example.com";
        String phone = "+1234567890";
        LocalDate dob = LocalDate.of(1990, 1, 15);
        String correctPassword = "SecurePassword123!";
        String wrongPassword = "WrongPassword456!";

        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email, phone, dob, correctPassword
        );

        // When: Validate passwords
        boolean correctValidation = globalPatientService.validatePassword(globalPatient, correctPassword);
        boolean wrongValidation = globalPatientService.validatePassword(globalPatient, wrongPassword);

        // Then: Correct password should validate, wrong password should not
        assertThat(correctValidation).isTrue();
        assertThat(wrongValidation).isFalse();
    }

    @Test
    void shouldAuthenticateFromEitherTenantSubdomain() {
        // Given: Patient registered in both tenants
        String email = "test@example.com";
        String phone = "+1234567890";
        LocalDate dob = LocalDate.of(1990, 1, 15);
        String password = "SecurePassword123!";

        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email, phone, dob, password
        );

        PatientEntity tenant1Profile = new PatientEntity("John", "Smith", email, phone);
        tenant1Profile.setTenant(tenant1);
        tenant1Profile.setGlobalPatient(globalPatient);
        patientRepository.save(tenant1Profile);

        PatientEntity tenant2Profile = new PatientEntity("John", "Smith", email, phone);
        tenant2Profile.setTenant(tenant2);
        tenant2Profile.setGlobalPatient(globalPatient);
        patientRepository.save(tenant2Profile);

        // When: Patient attempts to login from tenant 1 subdomain
        Optional<GlobalPatientEntity> authFromTenant1 = globalPatientService.findByEmail(email);
        
        // When: Patient attempts to login from tenant 2 subdomain
        Optional<GlobalPatientEntity> authFromTenant2 = globalPatientService.findByEmail(email);

        // Then: Authentication should succeed from both subdomains using same global patient
        assertThat(authFromTenant1).isPresent();
        assertThat(authFromTenant2).isPresent();
        assertThat(authFromTenant1.get().getId()).isEqualTo(globalPatient.getId());
        assertThat(authFromTenant2.get().getId()).isEqualTo(globalPatient.getId());

        // Verify password validation works from both contexts
        assertThat(globalPatientService.validatePassword(authFromTenant1.get(), password)).isTrue();
        assertThat(globalPatientService.validatePassword(authFromTenant2.get(), password)).isTrue();
    }

    @Test
    void shouldOnlySeeTenantSpecificProfileWhenQuerying() {
        // Given: Patient registered in both tenants
        String email = "test@example.com";
        String phone = "+1234567890";
        LocalDate dob = LocalDate.of(1990, 1, 15);
        String password = "SecurePassword123!";

        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email, phone, dob, password
        );

        PatientEntity tenant1Profile = new PatientEntity("John", "Smith", email, phone);
        tenant1Profile.setTenant(tenant1);
        tenant1Profile.setGlobalPatient(globalPatient);
        patientRepository.save(tenant1Profile);

        PatientEntity tenant2Profile = new PatientEntity("Jane", "Doe", email, phone);
        tenant2Profile.setTenant(tenant2);
        tenant2Profile.setGlobalPatient(globalPatient);
        patientRepository.save(tenant2Profile);

        // When: Query patients for tenant 1
        List<PatientEntity> tenant1Patients = patientRepository.findAllByTenantId(tenant1.getId());

        // When: Query patients for tenant 2
        List<PatientEntity> tenant2Patients = patientRepository.findAllByTenantId(tenant2.getId());

        // Then: Each tenant should only see their own profile
        assertThat(tenant1Patients).hasSize(1);
        assertThat(tenant1Patients.get(0).getId()).isEqualTo(tenant1Profile.getId());
        assertThat(tenant1Patients.get(0).getFirstName()).isEqualTo("John");
        assertThat(tenant1Patients.get(0).getTenant().getId()).isEqualTo(tenant1.getId());

        assertThat(tenant2Patients).hasSize(1);
        assertThat(tenant2Patients.get(0).getId()).isEqualTo(tenant2Profile.getId());
        assertThat(tenant2Patients.get(0).getFirstName()).isEqualTo("Jane");
        assertThat(tenant2Patients.get(0).getTenant().getId()).isEqualTo(tenant2.getId());

        // Verify both profiles link to same global patient
        assertThat(tenant1Patients.get(0).getGlobalPatient().getId()).isEqualTo(globalPatient.getId());
        assertThat(tenant2Patients.get(0).getGlobalPatient().getId()).isEqualTo(globalPatient.getId());
    }

    @Test
    void shouldNotAccessTenant2ProfileFromTenant1Context() {
        // Given: Patient registered in both tenants
        String email = "test@example.com";
        String phone = "+1234567890";
        LocalDate dob = LocalDate.of(1990, 1, 15);
        String password = "SecurePassword123!";

        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email, phone, dob, password
        );

        PatientEntity tenant1Profile = new PatientEntity("John", "Smith", email, phone);
        tenant1Profile.setTenant(tenant1);
        tenant1Profile.setGlobalPatient(globalPatient);
        patientRepository.save(tenant1Profile);

        PatientEntity tenant2Profile = new PatientEntity("Jane", "Doe", email, phone);
        tenant2Profile.setTenant(tenant2);
        tenant2Profile.setGlobalPatient(globalPatient);
        patientRepository.save(tenant2Profile);

        // When: Try to access tenant 2's profile from tenant 1 context
        Optional<PatientEntity> result = patientRepository.findByIdAndTenantId(
                tenant2Profile.getId(),
                tenant1.getId()
        );

        // Then: Should return empty (equivalent to 404)
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindPatientByGlobalPatientIdWithinTenant() {
        // Given: Patient registered in both tenants
        String email = "test@example.com";
        String phone = "+1234567890";
        LocalDate dob = LocalDate.of(1990, 1, 15);
        String password = "SecurePassword123!";

        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
                email, phone, dob, password
        );

        PatientEntity tenant1Profile = new PatientEntity("John", "Smith", email, phone);
        tenant1Profile.setTenant(tenant1);
        tenant1Profile.setGlobalPatient(globalPatient);
        patientRepository.save(tenant1Profile);

        PatientEntity tenant2Profile = new PatientEntity("Jane", "Doe", email, phone);
        tenant2Profile.setTenant(tenant2);
        tenant2Profile.setGlobalPatient(globalPatient);
        patientRepository.save(tenant2Profile);

        // When: Find patient by global patient ID in tenant 1
        Optional<PatientEntity> tenant1Result = patientRepository.findByTenantIdAndGlobalPatientId(
                tenant1.getId(),
                globalPatient.getId()
        );

        // When: Find patient by global patient ID in tenant 2
        Optional<PatientEntity> tenant2Result = patientRepository.findByTenantIdAndGlobalPatientId(
                tenant2.getId(),
                globalPatient.getId()
        );

        // Then: Should find correct profile for each tenant
        assertThat(tenant1Result).isPresent();
        assertThat(tenant1Result.get().getId()).isEqualTo(tenant1Profile.getId());
        assertThat(tenant1Result.get().getFirstName()).isEqualTo("John");

        assertThat(tenant2Result).isPresent();
        assertThat(tenant2Result.get().getId()).isEqualTo(tenant2Profile.getId());
        assertThat(tenant2Result.get().getFirstName()).isEqualTo("Jane");
    }

    @Test
    void shouldEnforceGlobalEmailUniqueness() {
        // Given: First patient with email
        String email = "test@example.com";
        String phone1 = "+1234567890";
        String phone2 = "+0987654321";
        LocalDate dob = LocalDate.of(1990, 1, 15);
        String password = "SecurePassword123!";

        // When: Create first global patient
        GlobalPatientEntity globalPatient1 = globalPatientService.findOrCreateGlobalPatient(
                email, phone1, dob, password
        );

        // When: Try to create another global patient with same email
        GlobalPatientEntity globalPatient2 = globalPatientService.findOrCreateGlobalPatient(
                email, phone2, dob, password
        );

        // Then: Should return the same global patient (not create a new one)
        assertThat(globalPatient2.getId()).isEqualTo(globalPatient1.getId());
        assertThat(globalPatient2.getEmail()).isEqualTo(email);

        // Verify only one global patient exists
        List<GlobalPatientEntity> allGlobalPatients = globalPatientRepository.findAll();
        assertThat(allGlobalPatients).hasSize(1);
    }
}
