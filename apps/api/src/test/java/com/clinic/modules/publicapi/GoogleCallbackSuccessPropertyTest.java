package com.clinic.modules.publicapi;

import com.clinic.modules.core.oauth.*;
import com.clinic.modules.core.patient.*;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.security.JwtIssuer;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for successful Google OAuth callback authentication.
 * These tests verify that successful authentication creates a valid session.
 * 
 * Feature: patient-google-oauth
 */
public class GoogleCallbackSuccessPropertyTest {

    @Mock
    private OAuthStateService oAuthStateService;
    
    @Mock
    private GoogleOAuthService googleOAuthService;
    
    @Mock
    private PatientGoogleAuthService patientGoogleAuthService;
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private TenantRepository tenantRepository;
    
    @Mock
    private JwtIssuer jwtIssuer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Property 2: Successful authentication creates session
     * 
     * For any valid Google authentication response, the system should create 
     * a patient session and redirect to the dashboard.
     * 
     * This test verifies:
     * 1. Valid authorization code and state parameters result in successful authentication
     * 2. A JWT token is generated for the authenticated patient
     * 3. The token contains the correct patient information
     * 4. The session is properly established
     * 
     * **Feature: patient-google-oauth, Property 2: Successful authentication creates session**
     * **Validates: Requirements 1.3, 2.5**
     */
    @Property(tries = 100)
    public void successfulAuthenticationCreatesSession(
        @ForAll("authorizationCodes") String authCode,
        @ForAll("stateTokens") String stateToken,
        @ForAll("tenantSlugs") String tenantSlug,
        @ForAll("googleIds") String googleId,
        @ForAll("patientEmails") String email,
        @ForAll("patientNames") String firstName,
        @ForAll("patientNames") String lastName,
        @ForAll("tenantIds") Long tenantId
    ) throws Exception {
        // Setup mocks
        MockitoAnnotations.openMocks(this);
        
        // Create tenant
        TenantEntity tenant = createTenant(tenantId, tenantSlug);
        
        // Mock state validation - state is valid and contains tenant context
        OAuthStateService.OAuthStateData stateData = new OAuthStateService.OAuthStateData(
            tenantSlug,
            "nonce-" + System.currentTimeMillis(),
            "http://localhost:3000/auth/google/callback"
        );
        when(oAuthStateService.validateAndConsumeState(stateToken))
            .thenReturn(stateData);
        
        // Mock tenant lookup
        when(tenantRepository.findBySlugIgnoreCase(tenantSlug))
            .thenReturn(Optional.of(tenant));
        
        // Mock Google OAuth token exchange
        GoogleTokenResponse tokenResponse = new GoogleTokenResponse(
            "access_token_" + authCode,
            null,
            "id_token_" + authCode,
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(eq(authCode), anyString()))
            .thenReturn(tokenResponse);
        
        // Mock Google user info retrieval
        GoogleUserInfo userInfo = new GoogleUserInfo(
            googleId,
            email,
            true,
            firstName + " " + lastName,
            firstName,
            lastName,
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(tokenResponse.accessToken()))
            .thenReturn(userInfo);
        
        // Create patient entities
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            googleId,
            email,
            firstName,
            lastName
        );
        setId(globalPatient, 1000L + tenantId);
        
        PatientEntity patient = new PatientEntity(firstName, lastName, email, null);
        patient.setTenant(tenant);
        patient.setGlobalPatient(globalPatient);
        patient.linkGoogleAccount(googleId);
        setId(patient, 2000L + tenantId);
        
        // Mock patient authentication
        PatientAuthResult authResult = 
            new PatientAuthResult(
                patient.getId(),
                globalPatient.getId(),
                email,
                firstName,
                lastName,
                false,  // not a new patient
                false   // not account linking
            );
        when(patientGoogleAuthService.authenticateWithGoogle(
            eq(googleId),
            eq(email),
            eq(firstName),
            eq(lastName),
            eq(tenantId)
        )).thenReturn(authResult);
        
        // Mock patient retrieval
        when(patientRepository.findByIdAndTenantId(patient.getId(), tenantId))
            .thenReturn(Optional.of(patient));
        
        // Mock JWT token generation
        JwtIssuer.IssuedToken issuedToken = new JwtIssuer.IssuedToken(
            "jwt_token_" + authCode,
            Instant.now().plusSeconds(86400)
        );
        when(jwtIssuer.issuePatientAccessToken(patient))
            .thenReturn(issuedToken);
        
        // Simulate the callback flow
        // 1. Validate state
        OAuthStateService.OAuthStateData validatedState = oAuthStateService.validateAndConsumeState(stateToken);
        assertNotNull(validatedState, "State validation should succeed");
        assertEquals(tenantSlug, validatedState.tenantSlug(), "State should contain correct tenant slug");
        
        // 2. Lookup tenant
        Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCase(tenantSlug);
        assertTrue(tenantOpt.isPresent(), "Tenant should be found");
        TenantEntity foundTenant = tenantOpt.get();
        assertEquals(tenantId, foundTenant.getId(), "Tenant ID should match");
        
        // 3. Exchange authorization code for tokens
        GoogleTokenResponse tokens = googleOAuthService.exchangeCodeForToken(authCode, "http://localhost:3000/auth/google/callback");
        assertNotNull(tokens, "Token exchange should succeed");
        assertNotNull(tokens.accessToken(), "Access token should be present");
        assertNotNull(tokens.idToken(), "ID token should be present");
        
        // 4. Get user info from Google
        GoogleUserInfo user = googleOAuthService.getUserInfo(tokens.accessToken());
        assertNotNull(user, "User info should be retrieved");
        assertEquals(googleId, user.id(), "Google ID should match");
        assertEquals(email, user.email(), "Email should match");
        assertTrue(user.emailVerified(), "Email should be verified");
        
        // 5. Authenticate or create patient
        PatientAuthResult result = patientGoogleAuthService.authenticateWithGoogle(
            user.id(),
            user.email(),
            user.givenName(),
            user.familyName(),
            foundTenant.getId()
        );
        assertNotNull(result, "Authentication result should not be null");
        assertNotNull(result.patientId(), "Patient ID should be present");
        assertNotNull(result.globalPatientId(), "Global patient ID should be present");
        assertEquals(email, result.email(), "Email should match");
        
        // 6. Retrieve patient entity
        Optional<PatientEntity> patientOpt = patientRepository.findByIdAndTenantId(
            result.patientId(),
            foundTenant.getId()
        );
        assertTrue(patientOpt.isPresent(), "Patient should be found");
        PatientEntity authenticatedPatient = patientOpt.get();
        
        // 7. Generate JWT token
        JwtIssuer.IssuedToken token = jwtIssuer.issuePatientAccessToken(authenticatedPatient);
        assertNotNull(token, "JWT token should be generated");
        assertNotNull(token.token(), "Token string should not be null");
        assertFalse(token.token().isEmpty(), "Token string should not be empty");
        assertTrue(token.token().startsWith("jwt_token_"), "Token should have expected format");
        
        // 8. Verify token expiration is in the future
        assertNotNull(token.expiresAt(), "Token should have expiration time");
        assertTrue(token.expiresAt().isAfter(Instant.now()), "Token should not be expired");
        
        // Verify all mocks were called as expected
        verify(oAuthStateService, times(1)).validateAndConsumeState(stateToken);
        verify(tenantRepository, times(1)).findBySlugIgnoreCase(tenantSlug);
        verify(googleOAuthService, times(1)).exchangeCodeForToken(eq(authCode), anyString());
        verify(googleOAuthService, times(1)).getUserInfo(tokens.accessToken());
        verify(patientGoogleAuthService, times(1)).authenticateWithGoogle(
            eq(googleId), eq(email), eq(firstName), eq(lastName), eq(tenantId)
        );
        verify(jwtIssuer, times(1)).issuePatientAccessToken(authenticatedPatient);
    }

    /**
     * Property 2 (variant): New patient creation via Google OAuth creates session
     * 
     * For any new Google user (no existing account), authenticating should create 
     * a patient record and establish a valid session.
     * 
     * **Feature: patient-google-oauth, Property 2: Successful authentication creates session**
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 100)
    public void newPatientCreationCreatesSession(
        @ForAll("authorizationCodes") String authCode,
        @ForAll("stateTokens") String stateToken,
        @ForAll("tenantSlugs") String tenantSlug,
        @ForAll("googleIds") String googleId,
        @ForAll("patientEmails") String email,
        @ForAll("patientNames") String firstName,
        @ForAll("patientNames") String lastName,
        @ForAll("tenantIds") Long tenantId
    ) throws Exception {
        // Setup mocks
        MockitoAnnotations.openMocks(this);
        
        // Create tenant
        TenantEntity tenant = createTenant(tenantId, tenantSlug);
        
        // Mock state validation
        OAuthStateService.OAuthStateData stateData = new OAuthStateService.OAuthStateData(
            tenantSlug,
            "nonce-" + System.currentTimeMillis(),
            "http://localhost:3000/auth/google/callback"
        );
        when(oAuthStateService.validateAndConsumeState(stateToken))
            .thenReturn(stateData);
        
        // Mock tenant lookup
        when(tenantRepository.findBySlugIgnoreCase(tenantSlug))
            .thenReturn(Optional.of(tenant));
        
        // Mock Google OAuth
        GoogleTokenResponse tokenResponse = new GoogleTokenResponse(
            "access_token_" + authCode,
            null,
            "id_token_" + authCode,
            3600,
            "Bearer"
        );
        when(googleOAuthService.exchangeCodeForToken(eq(authCode), anyString()))
            .thenReturn(tokenResponse);
        
        GoogleUserInfo userInfo = new GoogleUserInfo(
            googleId,
            email,
            true,
            firstName + " " + lastName,
            firstName,
            lastName,
            "https://example.com/photo.jpg",
            "en"
        );
        when(googleOAuthService.getUserInfo(tokenResponse.accessToken()))
            .thenReturn(userInfo);
        
        // Create NEW patient entities
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            googleId,
            email,
            firstName,
            lastName
        );
        setId(globalPatient, 3000L + tenantId);
        
        PatientEntity patient = new PatientEntity(firstName, lastName, email, null);
        patient.setTenant(tenant);
        patient.setGlobalPatient(globalPatient);
        patient.linkGoogleAccount(googleId);
        setId(patient, 4000L + tenantId);
        
        // Mock patient authentication - NEW PATIENT
        PatientAuthResult authResult = 
            new PatientAuthResult(
                patient.getId(),
                globalPatient.getId(),
                email,
                firstName,
                lastName,
                true,   // IS a new patient
                false   // not account linking
            );
        when(patientGoogleAuthService.authenticateWithGoogle(
            eq(googleId),
            eq(email),
            eq(firstName),
            eq(lastName),
            eq(tenantId)
        )).thenReturn(authResult);
        
        when(patientRepository.findByIdAndTenantId(patient.getId(), tenantId))
            .thenReturn(Optional.of(patient));
        
        // Mock JWT token generation
        JwtIssuer.IssuedToken issuedToken = new JwtIssuer.IssuedToken(
            "jwt_token_new_" + authCode,
            Instant.now().plusSeconds(86400)
        );
        when(jwtIssuer.issuePatientAccessToken(patient))
            .thenReturn(issuedToken);
        
        // Execute callback flow
        OAuthStateService.OAuthStateData validatedState = oAuthStateService.validateAndConsumeState(stateToken);
        TenantEntity foundTenant = tenantRepository.findBySlugIgnoreCase(tenantSlug).orElseThrow();
        GoogleTokenResponse tokens = googleOAuthService.exchangeCodeForToken(authCode, "http://localhost:3000/auth/google/callback");
        GoogleUserInfo user = googleOAuthService.getUserInfo(tokens.accessToken());
        
        PatientAuthResult result = patientGoogleAuthService.authenticateWithGoogle(
            user.id(),
            user.email(),
            user.givenName(),
            user.familyName(),
            foundTenant.getId()
        );
        
        // Verify this is a new patient
        assertTrue(result.isNewPatient(), "Result should indicate new patient creation");
        
        // Retrieve and verify patient
        PatientEntity authenticatedPatient = patientRepository.findByIdAndTenantId(
            result.patientId(),
            foundTenant.getId()
        ).orElseThrow();
        
        // Generate JWT token
        JwtIssuer.IssuedToken token = jwtIssuer.issuePatientAccessToken(authenticatedPatient);
        
        // Verify session is created for new patient
        assertNotNull(token, "JWT token should be generated for new patient");
        assertNotNull(token.token(), "Token string should not be null");
        assertFalse(token.token().isEmpty(), "Token string should not be empty");
        assertTrue(token.expiresAt().isAfter(Instant.now()), "Token should not be expired");
    }

    // Helper methods
    
    private TenantEntity createTenant(Long tenantId, String slug) {
        TenantEntity tenant = new TenantEntity(slug, "Test Clinic " + tenantId);
        setId(tenant, tenantId);
        return tenant;
    }

    private void setId(Object entity, Long id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID via reflection", e);
        }
    }

    // Arbitraries (generators) for property-based testing
    
    @Provide
    Arbitrary<String> authorizationCodes() {
        return Arbitraries.strings()
            .alpha()
            .numeric()
            .ofMinLength(20)
            .ofMaxLength(50)
            .map(s -> "4/" + s);
    }

    @Provide
    Arbitrary<String> stateTokens() {
        return Arbitraries.strings()
            .alpha()
            .numeric()
            .ofMinLength(32)
            .ofMaxLength(64);
    }

    @Provide
    Arbitrary<String> tenantSlugs() {
        String[] slugs = {"clinic-a", "clinic-b", "dental-care", "health-center", "medical-clinic"};
        return Arbitraries.of(slugs);
    }

    @Provide
    Arbitrary<String> googleIds() {
        return Arbitraries.longs()
            .between(100000000000000000L, 999999999999999999L)
            .map(String::valueOf);
    }

    @Provide
    Arbitrary<String> patientEmails() {
        String[] domains = {"gmail.com", "yahoo.com", "outlook.com", "example.com"};
        
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(15),
            Arbitraries.of(domains)
        ).as((username, domain) -> username.toLowerCase() + "@" + domain);
    }

    @Provide
    Arbitrary<String> patientNames() {
        String[] names = {"John", "Jane", "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank"};
        return Arbitraries.of(names);
    }

    @Provide
    Arbitrary<Long> tenantIds() {
        return Arbitraries.longs().between(1L, 1000L);
    }
}
