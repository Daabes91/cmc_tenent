package com.clinic.modules.saas;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.dto.SignupRequest;
import com.clinic.modules.saas.dto.SignupResponse;
import com.clinic.modules.saas.service.SignupService;
import com.clinic.modules.saas.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test for the signup flow.
 * Tests tenant creation, owner user creation, and PayPal subscription initiation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SignupFlowIntegrationTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SubscriptionService subscriptionService;

    private SignupRequest validRequest;

    @BeforeEach
    public void setUp() {
        // Clean up test data
        staffUserRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create a valid signup request
        validRequest = new SignupRequest(
                "Test Clinic",
                "test-clinic",
                "Dr. John Doe",
                "john.doe@testclinic.com",
                "SecurePass123",
                "+1234567890"
        );

        // Mock PayPal subscription creation
        when(subscriptionService.createSubscription(anyLong(), any(), anyString(), any()))
                .thenReturn("https://www.paypal.com/webapps/billing/subscriptions?ba_token=BA-TEST123");
    }

    @Test
    public void testCompleteSignupFlow() {
        // Act
        SignupResponse response = signupService.signup(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getApprovalUrl()).isNotNull();
        assertThat(response.getApprovalUrl()).contains("paypal.com");
        assertThat(response.getTenantId()).isNotNull();

        // Verify tenant was created
        Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCase("test-clinic");
        assertThat(tenantOpt).isPresent();

        TenantEntity tenant = tenantOpt.get();
        assertThat(tenant.getName()).isEqualTo("Test Clinic");
        assertThat(tenant.getSlug()).isEqualTo("test-clinic");
        assertThat(tenant.getBillingStatus()).isEqualTo(BillingStatus.PENDING_PAYMENT);

        // Verify owner user was created
        Optional<StaffUser> ownerOpt = staffUserRepository
                .findByEmailIgnoreCaseAndTenantId("john.doe@testclinic.com", tenant.getId());
        assertThat(ownerOpt).isPresent();

        StaffUser owner = ownerOpt.get();
        assertThat(owner.getFullName()).isEqualTo("Dr. John Doe");
        assertThat(owner.getEmail()).isEqualTo("john.doe@testclinic.com");
        assertThat(owner.getRole()).isEqualTo(StaffRole.ADMIN);
        assertThat(owner.getStatus()).isEqualTo(StaffStatus.ACTIVE);
        assertThat(passwordEncoder.matches("SecurePass123", owner.getPasswordHash())).isTrue();
    }

    @Test
    public void testSubdomainValidation() {
        // Create a tenant with the same subdomain
        TenantEntity existingTenant = new TenantEntity("test-clinic", "Existing Clinic");
        existingTenant.setBillingStatus(BillingStatus.ACTIVE);
        tenantRepository.save(existingTenant);

        // Act
        SignupResponse response = signupService.signup(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError()).contains("already taken");
    }

    @Test
    public void testSubdomainAvailabilityCheck() {
        // Test available subdomain
        boolean available = signupService.isSubdomainAvailable("new-clinic");
        assertThat(available).isTrue();

        // Create a tenant
        TenantEntity tenant = new TenantEntity("existing-clinic", "Existing Clinic");
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        tenantRepository.save(tenant);

        // Test unavailable subdomain
        available = signupService.isSubdomainAvailable("existing-clinic");
        assertThat(available).isFalse();

        // Test case-insensitive check
        available = signupService.isSubdomainAvailable("EXISTING-CLINIC");
        assertThat(available).isFalse();
    }

    @Test
    public void testPayPalSubscriptionCreationFailure() {
        // Mock PayPal failure
        when(subscriptionService.createSubscription(anyLong(), any(), anyString(), any()))
                .thenThrow(new IllegalStateException("PayPal service unavailable"));

        // Act
        SignupResponse response = signupService.signup(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError()).contains("temporarily unavailable");

        // Verify tenant and user were still created (transaction should not rollback)
        Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCase("test-clinic");
        assertThat(tenantOpt).isPresent();
    }

    @Test
    public void testPasswordHashing() {
        // Act
        SignupResponse response = signupService.signup(validRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();

        // Verify password is hashed
        Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCase("test-clinic");
        assertThat(tenantOpt).isPresent();

        Optional<StaffUser> ownerOpt = staffUserRepository
                .findByEmailIgnoreCaseAndTenantId("john.doe@testclinic.com", tenantOpt.get().getId());
        assertThat(ownerOpt).isPresent();

        StaffUser owner = ownerOpt.get();
        // Password should be hashed, not plain text
        assertThat(owner.getPasswordHash()).isNotEqualTo("SecurePass123");
        // But should match when verified
        assertThat(passwordEncoder.matches("SecurePass123", owner.getPasswordHash())).isTrue();
    }

    @Test
    public void testEmailNormalization() {
        // Use uppercase email
        validRequest.setEmail("JOHN.DOE@TESTCLINIC.COM");

        // Act
        SignupResponse response = signupService.signup(validRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();

        // Verify email is stored in lowercase
        Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCase("test-clinic");
        assertThat(tenantOpt).isPresent();

        Optional<StaffUser> ownerOpt = staffUserRepository
                .findByEmailIgnoreCaseAndTenantId("john.doe@testclinic.com", tenantOpt.get().getId());
        assertThat(ownerOpt).isPresent();
        assertThat(ownerOpt.get().getEmail()).isEqualTo("john.doe@testclinic.com");
    }

    @Test
    public void testSubdomainNormalization() {
        // Use uppercase subdomain
        validRequest.setSubdomain("TEST-CLINIC");

        // Act
        SignupResponse response = signupService.signup(validRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();

        // Verify subdomain is stored in lowercase
        Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCase("test-clinic");
        assertThat(tenantOpt).isPresent();
        assertThat(tenantOpt.get().getSlug()).isEqualTo("test-clinic");
    }
}
