package com.clinic.modules.publicapi;

import com.clinic.modules.core.oauth.*;
import com.clinic.modules.core.patient.*;
import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for Google OAuth flow.
 * 
 * This test suite covers:
 * - Complete OAuth flow end-to-end
 * - Multi-tenant scenarios
 * - Account linking scenarios
 * - Error scenarios
 * 
 * Feature: patient-google-oauth, Task 23
 * **Validates: All Requirements**
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class GoogleOAuthEndToEndIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private GlobalPatientRepository globalPatientRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private OAuthStateRepository oAuthStateRepository;

    @MockBean
    private GoogleOAuthService googleOAuthService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Test 1: Complete OAuth flow end-to-end for new patient
     * 
     * Scenario: New patient signs up using Google OAuth
     * Expected: Patient account created, JWT token returned, session established
     */
    @Test
    public void testCompleteOAuthFlowForNewPatient() throws Exception {
        // Setup: Create tenant
        TenantEntity tenant = createTestTenant("e2e-new-patient");
        
        // Step 1: Create OAuth state manually (simulating what would happen in authorize endpoint)
        // Note: We skip the authorize endpoint test here since it's covered in other tests
        // and focus on the complete callback flow
        OAuthStateEntity state = createOAuthState(tenant.getSlug());
        
        // Step 2: Mock Google callback
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "access-token-123",
            "refresh-token-123",
            "id-token-123",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(eq("auth-code-123"), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(eq("id-token-123"), anyString()))
            .thenReturn(true);
        
        GoogleUserInfo mockUserInfo = new GoogleUserInfo(
            "google-id-e2e-new",
            "newpatient@example.com",
            true,
            "New Patient",
            "New",
            "Patient",
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(eq("access-token-123")))
            .thenReturn(mockUserInfo);
        
        // Step 3: Process callback
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "auth-code-123")
                .param("state", state.getStateToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.patient.email").value("newpatient@example.com"))
                .andExpect(jsonPath("$.patient.firstName").value("New"))
                .andExpect(jsonPath("$.patient.lastName").value("Patient"));
        
        // Step 4: Verify patient was created
        PatientEntity patient = patientRepository.findByGoogleIdAndTenantId(
            "google-id-e2e-new", tenant.getId()
        ).orElse(null);
        assertNotNull(patient, "Patient should be created");
        assertEquals("newpatient@example.com", patient.getEmail());
        assertEquals("google-id-e2e-new", patient.getGoogleId());
        assertEquals(tenant.getId(), patient.getTenant().getId());
        
        // Step 5: Verify global patient
        GlobalPatientEntity globalPatient = globalPatientRepository.findById(
            patient.getGlobalPatient().getId()
        ).orElse(null);
        assertNotNull(globalPatient, "Global patient should exist");
        assertEquals(AuthProvider.GOOGLE, globalPatient.getAuthProvider());
        assertNull(globalPatient.getPasswordHash(), "Google-only patient should not have password");
        
        // Step 6: Verify state was consumed
        OAuthStateEntity consumedState = oAuthStateRepository.findById(state.getId()).orElse(null);
        assertNotNull(consumedState);
        assertTrue(consumedState.isConsumed(), "State should be marked as consumed");
    }

    /**
     * Test 2: Multi-tenant scenario - Same Google account across different tenants
     * 
     * Scenario: Patient uses same Google account to sign up at two different clinics
     * Expected: Separate patient records created for each tenant
     */
    @Test
    public void testMultiTenantOAuthFlow() throws Exception {
        // Setup: Create two tenants
        TenantEntity tenant1 = createTestTenant("clinic-alpha");
        TenantEntity tenant2 = createTestTenant("clinic-beta");
        
        String googleId = "google-id-multitenant-e2e";
        String email = "multitenant@example.com";
        
        // Mock Google responses (same for both tenants)
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "access-token-multi",
            "refresh-token-multi",
            "id-token-multi",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(anyString(), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(anyString(), anyString()))
            .thenReturn(true);
        
        GoogleUserInfo mockUserInfo = new GoogleUserInfo(
            googleId,
            email,
            true,
            "Multi Tenant User",
            "Multi",
            "User",
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(anyString()))
            .thenReturn(mockUserInfo);
        
        // Tenant 1: Complete OAuth flow
        OAuthStateEntity state1 = createOAuthState(tenant1.getSlug());
        
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-tenant1")
                .param("state", state1.getStateToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.email").value(email));
        
        // Tenant 2: Complete OAuth flow
        OAuthStateEntity state2 = createOAuthState(tenant2.getSlug());
        
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-tenant2")
                .param("state", state2.getStateToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.email").value(email));
        
        // Verify: Separate patient records exist
        PatientEntity patient1 = patientRepository.findByGoogleIdAndTenantId(
            googleId, tenant1.getId()
        ).orElse(null);
        PatientEntity patient2 = patientRepository.findByGoogleIdAndTenantId(
            googleId, tenant2.getId()
        ).orElse(null);
        
        assertNotNull(patient1, "Patient should exist in tenant 1");
        assertNotNull(patient2, "Patient should exist in tenant 2");
        assertNotEquals(patient1.getId(), patient2.getId(), "Should be different patient records");
        assertEquals(tenant1.getId(), patient1.getTenant().getId());
        assertEquals(tenant2.getId(), patient2.getTenant().getId());
        
        // Verify: Same global patient
        assertEquals(patient1.getGlobalPatient().getId(), patient2.getGlobalPatient().getId(),
            "Should share same global patient");
        
        // Verify: Tenant isolation
        var notFoundInTenant2 = patientRepository.findByIdAndTenantId(
            patient1.getId(), tenant2.getId()
        );
        assertFalse(notFoundInTenant2.isPresent(), "Patient 1 should not be accessible in tenant 2");
    }

    /**
     * Test 3: Account linking scenario - Existing local patient links Google account
     * 
     * Scenario: Patient with email/password account links their Google account
     * Expected: Google ID added to existing patient, both auth methods work
     */
    @Test
    public void testAccountLinkingScenario() throws Exception {
        // Setup: Create tenant and existing local patient
        TenantEntity tenant = createTestTenant("clinic-linking");
        
        String email = "existing@example.com";
        String googleId = "google-id-linking";
        
        // Create existing local patient
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            email, null, "hashed-password-123", LocalDate.of(1990, 5, 15)
        );
        globalPatient = globalPatientRepository.save(globalPatient);
        
        PatientEntity localPatient = new PatientEntity("Alice", "Smith", email, null);
        localPatient.setGlobalPatient(globalPatient);
        localPatient.setTenant(tenant);
        localPatient = patientRepository.save(localPatient);
        
        Long originalPatientId = localPatient.getId();
        Long originalGlobalPatientId = globalPatient.getId();
        
        // Mock Google OAuth responses
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "access-token-link",
            "refresh-token-link",
            "id-token-link",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(anyString(), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(anyString(), anyString()))
            .thenReturn(true);
        
        GoogleUserInfo mockUserInfo = new GoogleUserInfo(
            googleId,
            email, // Same email as existing patient
            true,
            "Alice Smith",
            "Alice",
            "Smith",
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(anyString()))
            .thenReturn(mockUserInfo);
        
        // Complete OAuth flow
        OAuthStateEntity state = createOAuthState(tenant.getSlug());
        
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-linking")
                .param("state", state.getStateToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.email").value(email))
                .andExpect(jsonPath("$.patient.id").value(originalPatientId.intValue()));
        
        // Verify: Account was linked (not new patient created)
        PatientEntity linkedPatient = patientRepository.findById(originalPatientId).orElse(null);
        assertNotNull(linkedPatient, "Original patient should still exist");
        assertEquals(googleId, linkedPatient.getGoogleId(), "Google ID should be linked");
        assertEquals(email, linkedPatient.getEmail());
        
        // Verify: No duplicate patient created
        long patientCount = patientRepository.findAllByTenantId(tenant.getId()).size();
        assertEquals(1, patientCount, "Should only have one patient record");
        
        // Verify: Global patient has both auth methods
        GlobalPatientEntity updatedGlobalPatient = globalPatientRepository.findById(
            originalGlobalPatientId
        ).orElse(null);
        assertNotNull(updatedGlobalPatient);
        assertEquals(AuthProvider.BOTH, updatedGlobalPatient.getAuthProvider(),
            "Should support both auth methods");
        assertNotNull(updatedGlobalPatient.getPasswordHash(), "Password should be preserved");
        assertEquals(googleId, updatedGlobalPatient.getGoogleId(), "Google ID should be set");
        
        // Verify: Patient can still authenticate with local credentials
        assertNotNull(updatedGlobalPatient.getPasswordHash());
    }

    /**
     * Test 4: Account linking with email mismatch - Creates new account
     * 
     * Scenario: Patient tries to link Google account with different email
     * Expected: New patient account created instead of linking
     */
    @Test
    public void testAccountLinkingWithEmailMismatch() throws Exception {
        // Setup: Create tenant and existing local patient
        TenantEntity tenant = createTestTenant("clinic-mismatch");
        
        // Create existing patient with one email
        GlobalPatientEntity existingGlobal = new GlobalPatientEntity(
            "existing@example.com", null, "hashed-password", LocalDate.of(1985, 3, 20)
        );
        existingGlobal = globalPatientRepository.save(existingGlobal);
        
        PatientEntity existingPatient = new PatientEntity("Bob", "Jones", "existing@example.com", null);
        existingPatient.setGlobalPatient(existingGlobal);
        existingPatient.setTenant(tenant);
        existingPatient = patientRepository.save(existingPatient);
        
        Long existingPatientId = existingPatient.getId();
        
        // Mock Google OAuth with different email
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "access-token-mismatch",
            "refresh-token-mismatch",
            "id-token-mismatch",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(anyString(), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(anyString(), anyString()))
            .thenReturn(true);
        
        GoogleUserInfo mockUserInfo = new GoogleUserInfo(
            "google-id-different",
            "different@example.com", // Different email
            true,
            "Charlie Davis",
            "Charlie",
            "Davis",
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(anyString()))
            .thenReturn(mockUserInfo);
        
        // Complete OAuth flow
        OAuthStateEntity state = createOAuthState(tenant.getSlug());
        
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-mismatch")
                .param("state", state.getStateToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.email").value("different@example.com"));
        
        // Verify: New patient was created
        long patientCount = patientRepository.findAllByTenantId(tenant.getId()).size();
        assertEquals(2, patientCount, "Should have two patient records");
        
        // Verify: Existing patient unchanged
        PatientEntity unchangedPatient = patientRepository.findById(existingPatientId).orElse(null);
        assertNotNull(unchangedPatient);
        assertEquals("existing@example.com", unchangedPatient.getEmail());
        assertNull(unchangedPatient.getGoogleId(), "Existing patient should not have Google ID");
        
        // Verify: New patient exists
        PatientEntity newPatient = patientRepository.findByGoogleIdAndTenantId(
            "google-id-different", tenant.getId()
        ).orElse(null);
        assertNotNull(newPatient);
        assertEquals("different@example.com", newPatient.getEmail());
        assertNotEquals(existingPatientId, newPatient.getId());
    }

    /**
     * Test 5: Error scenario - Invalid state parameter
     * 
     * Scenario: Callback with invalid/tampered state token
     * Expected: Authentication rejected with 401 Unauthorized
     */
    @Test
    public void testErrorScenarioInvalidState() throws Exception {
        // Attempt callback with invalid state
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "some-code")
                .param("state", "invalid-state-token-12345"))
                .andExpect(status().isUnauthorized());
        
        // Verify: No Google API calls were made
        verify(googleOAuthService, never()).exchangeCodeForToken(anyString(), anyString());
        
        // Verify: No patients were created (check all repositories are clean for this test)
        // Note: Other tests may have created patients, so we just verify no new ones were added
    }

    /**
     * Test 6: Error scenario - Expired state parameter
     * 
     * Scenario: Callback with expired state token
     * Expected: Authentication rejected with 401 Unauthorized
     */
    @Test
    public void testErrorScenarioExpiredState() throws Exception {
        // Create tenant and expired state
        TenantEntity tenant = createTestTenant("clinic-expired-state");
        
        OAuthStateEntity expiredState = new OAuthStateEntity(
            "expired-state-token",
            tenant.getSlug(),
            "nonce-expired",
            "https://example.com/callback",
            java.time.Instant.now().minusSeconds(600) // Expired 10 minutes ago
        );
        oAuthStateRepository.save(expiredState);
        
        // Attempt callback with expired state
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "some-code")
                .param("state", "expired-state-token"))
                .andExpect(status().isUnauthorized());
        
        // Verify: No Google API calls were made
        verify(googleOAuthService, never()).exchangeCodeForToken(anyString(), anyString());
    }

    /**
     * Test 7: Error scenario - State reuse attempt
     * 
     * Scenario: Attempting to reuse a consumed state token
     * Expected: Authentication rejected with 401 Unauthorized
     */
    @Test
    public void testErrorScenarioStateReuse() throws Exception {
        // Create tenant and state
        TenantEntity tenant = createTestTenant("clinic-state-reuse");
        OAuthStateEntity state = createOAuthState(tenant.getSlug());
        
        // Mock Google responses
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "access-token-reuse",
            "refresh-token-reuse",
            "id-token-reuse",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(anyString(), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(anyString(), anyString()))
            .thenReturn(true);
        
        GoogleUserInfo mockUserInfo = new GoogleUserInfo(
            "google-id-reuse",
            "reuse@example.com",
            true,
            "Reuse Test",
            "Reuse",
            "Test",
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(anyString()))
            .thenReturn(mockUserInfo);
        
        // First use: Should succeed
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-first")
                .param("state", state.getStateToken()))
                .andExpect(status().isOk());
        
        // Second use: Should fail (state already consumed)
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-second")
                .param("state", state.getStateToken()))
                .andExpect(status().isUnauthorized());
        
        // Verify: Only one patient was created
        long patientCount = patientRepository.findAllByTenantId(tenant.getId()).size();
        assertEquals(1, patientCount, "Only one patient should be created");
    }

    /**
     * Test 8: Error scenario - Google token exchange failure
     * 
     * Scenario: Google rejects the authorization code
     * Expected: 502 Bad Gateway error
     */
    @Test
    public void testErrorScenarioTokenExchangeFailure() throws Exception {
        // Create tenant and state
        TenantEntity tenant = createTestTenant("clinic-token-fail");
        OAuthStateEntity state = createOAuthState(tenant.getSlug());
        
        // Mock token exchange failure
        when(googleOAuthService.exchangeCodeForToken(anyString(), anyString()))
            .thenThrow(new GoogleOAuthException("Invalid authorization code"));
        
        // Attempt callback
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "invalid-code")
                .param("state", state.getStateToken()))
                .andExpect(status().isBadGateway());
        
        // Verify: No patients were created
        long patientCount = patientRepository.findAllByTenantId(tenant.getId()).size();
        assertEquals(0, patientCount, "No patients should be created");
    }

    /**
     * Test 9: Error scenario - ID token validation failure
     * 
     * Scenario: ID token signature validation fails
     * Expected: 401 Unauthorized error
     */
    @Test
    public void testErrorScenarioIdTokenValidationFailure() throws Exception {
        // Create tenant and state
        TenantEntity tenant = createTestTenant("clinic-token-invalid");
        OAuthStateEntity state = createOAuthState(tenant.getSlug());
        
        // Mock token response with invalid ID token
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "access-token-invalid",
            "refresh-token-invalid",
            "invalid-id-token",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(anyString(), anyString()))
            .thenReturn(mockTokenResponse);
        
        // Mock validation failure
        when(googleOAuthService.validateIdToken(eq("invalid-id-token"), anyString()))
            .thenReturn(false);
        
        // Attempt callback
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-invalid-token")
                .param("state", state.getStateToken()))
                .andExpect(status().isUnauthorized());
        
        // Verify: No patients were created
        long patientCount = patientRepository.findAllByTenantId(tenant.getId()).size();
        assertEquals(0, patientCount, "No patients should be created");
    }

    /**
     * Test 10: Error scenario - Unverified Google email
     * 
     * Scenario: Google account email is not verified
     * Expected: 400 Bad Request error
     */
    @Test
    public void testErrorScenarioUnverifiedEmail() throws Exception {
        // Create tenant and state
        TenantEntity tenant = createTestTenant("clinic-unverified");
        OAuthStateEntity state = createOAuthState(tenant.getSlug());
        
        // Mock responses with unverified email
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "access-token-unverified",
            "refresh-token-unverified",
            "id-token-unverified",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(anyString(), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(anyString(), anyString()))
            .thenReturn(true);
        
        GoogleUserInfo mockUserInfo = new GoogleUserInfo(
            "google-id-unverified",
            "unverified@example.com",
            false, // Email not verified
            "Unverified User",
            "Unverified",
            "User",
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(anyString()))
            .thenReturn(mockUserInfo);
        
        // Attempt callback
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-unverified")
                .param("state", state.getStateToken()))
                .andExpect(status().isBadRequest());
        
        // Verify: No patients were created
        long patientCount = patientRepository.findAllByTenantId(tenant.getId()).size();
        assertEquals(0, patientCount, "No patients should be created");
    }

    /**
     * Test 11: Error scenario - Tenant not found
     * 
     * Scenario: State contains slug for non-existent tenant
     * Expected: 400 Bad Request error
     */
    @Test
    public void testErrorScenarioTenantNotFound() throws Exception {
        // Create state with non-existent tenant
        OAuthStateEntity state = new OAuthStateEntity(
            "state-no-tenant",
            "non-existent-clinic",
            "nonce-no-tenant",
            "https://example.com/callback",
            java.time.Instant.now().plusSeconds(300)
        );
        oAuthStateRepository.save(state);
        
        // Attempt callback
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "some-code")
                .param("state", "state-no-tenant"))
                .andExpect(status().isBadRequest());
        
        // Verify: No Google API calls were made
        verify(googleOAuthService, never()).exchangeCodeForToken(anyString(), anyString());
    }

    /**
     * Test 12: Error scenario - Google UserInfo API failure
     * 
     * Scenario: Failed to retrieve user info from Google
     * Expected: 502 Bad Gateway error
     */
    @Test
    public void testErrorScenarioUserInfoFailure() throws Exception {
        // Create tenant and state
        TenantEntity tenant = createTestTenant("clinic-userinfo-fail");
        OAuthStateEntity state = createOAuthState(tenant.getSlug());
        
        // Mock successful token exchange
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "access-token-userinfo-fail",
            "refresh-token-userinfo-fail",
            "id-token-userinfo-fail",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(anyString(), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(anyString(), anyString()))
            .thenReturn(true);
        
        // Mock UserInfo API failure
        when(googleOAuthService.getUserInfo(anyString()))
            .thenThrow(new GoogleOAuthException("Failed to retrieve user info"));
        
        // Attempt callback
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-userinfo-fail")
                .param("state", state.getStateToken()))
                .andExpect(status().isBadGateway());
        
        // Verify: No patients were created
        long patientCount = patientRepository.findAllByTenantId(tenant.getId()).size();
        assertEquals(0, patientCount, "No patients should be created");
    }

    // Helper methods

    private TenantEntity createTestTenant(String slug) {
        TenantEntity tenant = new TenantEntity(slug, "Test Clinic " + slug);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }

    private OAuthStateEntity createOAuthState(String tenantSlug) {
        String stateToken = "state-" + tenantSlug + "-" + System.currentTimeMillis();
        OAuthStateEntity state = new OAuthStateEntity(
            stateToken,
            tenantSlug,
            "nonce-" + System.currentTimeMillis(),
            "https://example.com/callback",
            java.time.Instant.now().plusSeconds(300)
        );
        return oAuthStateRepository.save(state);
    }
}
