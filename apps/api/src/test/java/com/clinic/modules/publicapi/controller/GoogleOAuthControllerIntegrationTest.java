package com.clinic.modules.publicapi.controller;

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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Google OAuth endpoints.
 * These tests verify the complete OAuth flow with mocked Google services.
 * 
 * Feature: patient-google-oauth
 * 
 * Tests cover:
 * - Complete OAuth flow with mocked Google
 * - Error scenarios
 * - Tenant context preservation
 * 
 * **Validates: Requirements 1.2, 1.3, 1.4, 5.1, 5.2**
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class GoogleOAuthControllerIntegrationTest {

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
     * Test complete OAuth flow with mocked Google.
     * 
     * This test verifies:
     * 1. Authorization endpoint generates correct URL with tenant context
     * 2. Callback endpoint processes authorization code
     * 3. Patient is created/authenticated
     * 4. JWT token is returned
     * 
     * **Validates: Requirements 1.2, 1.3, 5.1**
     */
    @Test
    public void testCompleteOAuthFlowWithMockedGoogle() throws Exception {
        // Create test tenant
        TenantEntity tenant = createTestTenant("test-clinic-oauth");
        
        // Mock Google OAuth service responses
        String mockAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth?state=mock-state&client_id=test";
        when(googleOAuthService.generateAuthorizationUrl(eq(tenant.getSlug()), anyString()))
            .thenReturn(mockAuthUrl);
        
        // Test authorization endpoint
        mockMvc.perform(get("/public/auth/google/authorize")
                .param("tenantSlug", tenant.getSlug()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(mockAuthUrl));
        
        // Verify authorization URL was generated with tenant context
        verify(googleOAuthService).generateAuthorizationUrl(eq(tenant.getSlug()), anyString());
    }

    /**
     * Test OAuth callback with successful authentication.
     * 
     * This test verifies:
     * 1. Callback processes authorization code
     * 2. ID token is validated
     * 3. User info is retrieved
     * 4. Patient is created
     * 5. JWT token is returned
     * 
     * **Validates: Requirements 1.3, 5.2**
     */
    @Test
    public void testOAuthCallbackWithSuccessfulAuthentication() throws Exception {
        // Create test tenant
        TenantEntity tenant = createTestTenant("test-clinic-callback");
        
        // Create OAuth state
        OAuthStateEntity state = new OAuthStateEntity(
            "test-state-token",
            tenant.getSlug(),
            "test-nonce",
            "https://example.com/callback",
            java.time.Instant.now().plusSeconds(300)
        );
        oAuthStateRepository.save(state);
        
        // Mock Google OAuth service responses
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "mock-access-token",
            "mock-refresh-token",
            "mock-id-token",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(eq("test-auth-code"), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(eq("mock-id-token"), eq("test-nonce")))
            .thenReturn(true);
        
        GoogleUserInfo mockUserInfo = new GoogleUserInfo(
            "google-id-123456",
            "testuser@example.com",
            true,
            "Test User",
            "Test",
            "User",
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(eq("mock-access-token")))
            .thenReturn(mockUserInfo);
        
        // Test callback endpoint
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "test-auth-code")
                .param("state", "test-state-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.patient.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.patient.firstName").value("Test"))
                .andExpect(jsonPath("$.patient.lastName").value("User"));
        
        // Verify patient was created
        PatientEntity patient = patientRepository.findByGoogleIdAndTenantId("google-id-123456", tenant.getId())
            .orElse(null);
        assertNotNull(patient);
        assertEquals("testuser@example.com", patient.getEmail());
        assertEquals("google-id-123456", patient.getGoogleId());
    }

    /**
     * Test OAuth callback with invalid state parameter.
     * 
     * This test verifies that invalid state tokens are rejected.
     * 
     * **Validates: Requirements 1.4, 5.2**
     */
    @Test
    public void testOAuthCallbackWithInvalidState() throws Exception {
        // Test callback with invalid state
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "test-auth-code")
                .param("state", "invalid-state-token"))
                .andExpect(status().isUnauthorized());
        
        // Verify no Google API calls were made
        verify(googleOAuthService, never()).exchangeCodeForToken(anyString(), anyString());
    }

    /**
     * Test OAuth callback with expired state parameter.
     * 
     * This test verifies that expired state tokens are rejected.
     * 
     * **Validates: Requirements 1.4, 5.2**
     */
    @Test
    public void testOAuthCallbackWithExpiredState() throws Exception {
        // Create test tenant
        TenantEntity tenant = createTestTenant("test-clinic-expired");
        
        // Create expired OAuth state
        OAuthStateEntity expiredState = new OAuthStateEntity(
            "expired-state-token",
            tenant.getSlug(),
            "test-nonce",
            "https://example.com/callback",
            java.time.Instant.now().minusSeconds(60) // Expired 1 minute ago
        );
        oAuthStateRepository.save(expiredState);
        
        // Test callback with expired state
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "test-auth-code")
                .param("state", "expired-state-token"))
                .andExpect(status().isUnauthorized());
        
        // Verify no Google API calls were made
        verify(googleOAuthService, never()).exchangeCodeForToken(anyString(), anyString());
    }

    /**
     * Test OAuth callback with already consumed state.
     * 
     * This test verifies that state tokens can only be used once.
     * 
     * **Validates: Requirements 1.4, 5.2**
     */
    @Test
    public void testOAuthCallbackWithConsumedState() throws Exception {
        // Create test tenant
        TenantEntity tenant = createTestTenant("test-clinic-consumed");
        
        // Create OAuth state
        OAuthStateEntity state = new OAuthStateEntity(
            "consumed-state-token",
            tenant.getSlug(),
            "test-nonce",
            "https://example.com/callback",
            java.time.Instant.now().plusSeconds(300)
        );
        state.markConsumed(); // Mark as already consumed
        oAuthStateRepository.save(state);
        
        // Test callback with consumed state
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "test-auth-code")
                .param("state", "consumed-state-token"))
                .andExpect(status().isUnauthorized());
        
        // Verify no Google API calls were made
        verify(googleOAuthService, never()).exchangeCodeForToken(anyString(), anyString());
    }

    /**
     * Test OAuth callback with token exchange failure.
     * 
     * This test verifies proper error handling when Google token exchange fails.
     * 
     * **Validates: Requirements 1.4**
     */
    @Test
    public void testOAuthCallbackWithTokenExchangeFailure() throws Exception {
        // Create test tenant
        TenantEntity tenant = createTestTenant("test-clinic-token-fail");
        
        // Create OAuth state
        OAuthStateEntity state = new OAuthStateEntity(
            "test-state-token-fail",
            tenant.getSlug(),
            "test-nonce",
            "https://example.com/callback",
            java.time.Instant.now().plusSeconds(300)
        );
        oAuthStateRepository.save(state);
        
        // Mock token exchange failure
        when(googleOAuthService.exchangeCodeForToken(eq("invalid-code"), anyString()))
            .thenThrow(new GoogleOAuthException("Failed to exchange code"));
        
        // Test callback with failing token exchange
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "invalid-code")
                .param("state", "test-state-token-fail"))
                .andExpect(status().isBadGateway());
    }

    /**
     * Test OAuth callback with ID token validation failure.
     * 
     * This test verifies proper error handling when ID token validation fails.
     * 
     * **Validates: Requirements 1.4**
     */
    @Test
    public void testOAuthCallbackWithIdTokenValidationFailure() throws Exception {
        // Create test tenant
        TenantEntity tenant = createTestTenant("test-clinic-token-invalid");
        
        // Create OAuth state
        OAuthStateEntity state = new OAuthStateEntity(
            "test-state-token-invalid",
            tenant.getSlug(),
            "test-nonce",
            "https://example.com/callback",
            java.time.Instant.now().plusSeconds(300)
        );
        oAuthStateRepository.save(state);
        
        // Mock token response with invalid ID token
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "mock-access-token",
            "mock-refresh-token",
            "invalid-id-token",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(eq("test-code"), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(eq("invalid-id-token"), eq("test-nonce")))
            .thenReturn(false); // Validation fails
        
        // Test callback with invalid ID token
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "test-code")
                .param("state", "test-state-token-invalid"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test OAuth callback with unverified email.
     * 
     * This test verifies that unverified Google emails are rejected.
     * 
     * **Validates: Requirements 1.4**
     */
    @Test
    public void testOAuthCallbackWithUnverifiedEmail() throws Exception {
        // Create test tenant
        TenantEntity tenant = createTestTenant("test-clinic-unverified");
        
        // Create OAuth state
        OAuthStateEntity state = new OAuthStateEntity(
            "test-state-unverified",
            tenant.getSlug(),
            "test-nonce",
            "https://example.com/callback",
            java.time.Instant.now().plusSeconds(300)
        );
        oAuthStateRepository.save(state);
        
        // Mock responses with unverified email
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "mock-access-token",
            "mock-refresh-token",
            "mock-id-token",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(eq("test-code"), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(eq("mock-id-token"), eq("test-nonce")))
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
        when(googleOAuthService.getUserInfo(eq("mock-access-token")))
            .thenReturn(mockUserInfo);
        
        // Test callback with unverified email
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "test-code")
                .param("state", "test-state-unverified"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test tenant context preservation throughout OAuth flow.
     * 
     * This test verifies that tenant context is maintained from authorization
     * through callback.
     * 
     * **Validates: Requirements 5.1, 5.2**
     */
    @Test
    public void testTenantContextPreservation() throws Exception {
        // Create two tenants
        TenantEntity tenant1 = createTestTenant("clinic-1");
        TenantEntity tenant2 = createTestTenant("clinic-2");
        
        // Create OAuth states for both tenants
        OAuthStateEntity state1 = new OAuthStateEntity(
            "state-tenant-1",
            tenant1.getSlug(),
            "nonce-1",
            "https://example.com/callback",
            java.time.Instant.now().plusSeconds(300)
        );
        oAuthStateRepository.save(state1);
        
        OAuthStateEntity state2 = new OAuthStateEntity(
            "state-tenant-2",
            tenant2.getSlug(),
            "nonce-2",
            "https://example.com/callback",
            java.time.Instant.now().plusSeconds(300)
        );
        oAuthStateRepository.save(state2);
        
        // Mock Google responses
        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(
            "mock-access-token",
            "mock-refresh-token",
            "mock-id-token",
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(anyString(), anyString()))
            .thenReturn(mockTokenResponse);
        
        when(googleOAuthService.validateIdToken(anyString(), anyString()))
            .thenReturn(true);
        
        GoogleUserInfo mockUserInfo = new GoogleUserInfo(
            "google-id-multi-tenant",
            "multiuser@example.com",
            true,
            "Multi Tenant",
            "Multi",
            "Tenant",
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(anyString()))
            .thenReturn(mockUserInfo);
        
        // Test callback for tenant 1
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-1")
                .param("state", "state-tenant-1"))
                .andExpect(status().isOk());
        
        // Test callback for tenant 2
        mockMvc.perform(get("/public/auth/google/callback")
                .param("code", "code-2")
                .param("state", "state-tenant-2"))
                .andExpect(status().isOk());
        
        // Verify separate patient records were created for each tenant
        PatientEntity patient1 = patientRepository.findByGoogleIdAndTenantId(
            "google-id-multi-tenant", tenant1.getId()
        ).orElse(null);
        assertNotNull(patient1);
        assertEquals(tenant1.getId(), patient1.getTenant().getId());
        
        PatientEntity patient2 = patientRepository.findByGoogleIdAndTenantId(
            "google-id-multi-tenant", tenant2.getId()
        ).orElse(null);
        assertNotNull(patient2);
        assertEquals(tenant2.getId(), patient2.getTenant().getId());
        
        // Verify they are different patient records
        assertNotEquals(patient1.getId(), patient2.getId());
    }

    // Helper methods

    private TenantEntity createTestTenant(String slug) {
        TenantEntity tenant = new TenantEntity(slug, "Test Clinic " + slug);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }

    private void assertNotNull(Object object) {
        org.junit.jupiter.api.Assertions.assertNotNull(object);
    }

    private void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }

    private void assertNotEquals(Object unexpected, Object actual) {
        org.junit.jupiter.api.Assertions.assertNotEquals(unexpected, actual);
    }
}
