package com.clinic.modules.publicapi;

import com.clinic.config.SecurityProperties;
import com.clinic.modules.core.patient.AuthProvider;
import com.clinic.modules.core.patient.GlobalPatientEntity;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.security.JwtIssuer;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.jqwik.api.*;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for JWT token generation and session equivalence.
 * These tests verify that Google and local authentication produce equivalent sessions.
 * 
 * Feature: patient-google-oauth
 */
public class SessionEquivalencePropertyTest {

    private JwtIssuer createJwtIssuer() {
        // Create mock security properties
        SecurityProperties.Token staffToken = new SecurityProperties.Token(
            "https://api.example-clinic.com",  // issuer
            "staff",                            // audience
            "classpath:keys/staff_public.pem",  // publicKey
            "classpath:keys/staff_private.pem", // privateKey
            Duration.ofHours(1),                // accessTtl
            Duration.ofSeconds(30)              // clockSkew
        );
        
        SecurityProperties.Token patientToken = new SecurityProperties.Token(
            "https://api.example-clinic.com",    // issuer
            "patient",                            // audience
            "classpath:keys/patient_public.pem",  // publicKey
            "classpath:keys/patient_private.pem", // privateKey
            Duration.ofHours(24),                 // accessTtl
            Duration.ofSeconds(30)                // clockSkew
        );
        
        SecurityProperties.Token saasManagerToken = new SecurityProperties.Token();
        SecurityProperties.Refresh refresh = new SecurityProperties.Refresh();
        
        SecurityProperties.Jwt jwt = new SecurityProperties.Jwt(patientToken, staffToken, saasManagerToken, refresh);
        SecurityProperties securityProperties = new SecurityProperties();
        securityProperties.setJwt(jwt);
        
        Environment environment = new MockEnvironment();
        
        return new JwtIssuer(securityProperties, environment);
    }

    /**
     * Property 4: Google and local sessions have equivalent privileges
     * 
     * For any patient authenticated via Google or local credentials, 
     * both session types should grant identical permissions and access rights.
     * 
     * This test verifies:
     * 1. Both authentication methods produce valid JWT tokens
     * 2. Both tokens contain the same essential claims (roles, tenantProfileId, globalPatientId)
     * 3. Both tokens have the same structure and expiration behavior
     * 4. The only difference is the auth_provider claim
     * 
     * **Feature: patient-google-oauth, Property 4: Google and local sessions have equivalent privileges**
     * **Validates: Requirements 1.5**
     */
    @Property(tries = 100)
    public void googleAndLocalSessionsHaveEquivalentPrivileges(
        @ForAll("patientEmails") String email,
        @ForAll("patientNames") String firstName,
        @ForAll("patientNames") String lastName,
        @ForAll("tenantIds") Long tenantId,
        @ForAll("googleIds") String googleId
    ) throws Exception {
        // Create JWT issuer
        JwtIssuer jwtIssuer = createJwtIssuer();
        
        // Create a tenant
        TenantEntity tenant = createTenant(tenantId);
        
        // Create a global patient with LOCAL auth
        GlobalPatientEntity localGlobalPatient = new GlobalPatientEntity(
            email,
            "+1234567890",
            "$2a$10$hashedpassword",
            LocalDate.of(1990, 1, 1)
        );
        localGlobalPatient.setAuthProvider(AuthProvider.LOCAL);
        // Simulate persisted entity
        setId(localGlobalPatient, 1000L + tenantId);
        
        // Create a tenant-scoped patient for local auth
        PatientEntity localPatient = new PatientEntity(firstName, lastName, email, "+1234567890");
        localPatient.setTenant(tenant);
        localPatient.setGlobalPatient(localGlobalPatient);
        setId(localPatient, 2000L + tenantId);
        
        // Create a global patient with GOOGLE auth
        GlobalPatientEntity googleGlobalPatient = new GlobalPatientEntity(
            googleId,
            email,
            firstName,
            lastName
        );
        googleGlobalPatient.setAuthProvider(AuthProvider.GOOGLE);
        // Simulate persisted entity
        setId(googleGlobalPatient, 3000L + tenantId);
        
        // Create a tenant-scoped patient for Google auth
        PatientEntity googlePatient = new PatientEntity(firstName, lastName, email, "+1234567890");
        googlePatient.setTenant(tenant);
        googlePatient.setGlobalPatient(googleGlobalPatient);
        setId(googlePatient, 4000L + tenantId);
        
        // Generate JWT tokens for both authentication methods
        JwtIssuer.IssuedToken localToken = jwtIssuer.issuePatientAccessToken(localPatient);
        JwtIssuer.IssuedToken googleToken = jwtIssuer.issuePatientAccessToken(googlePatient);
        
        // Verify both tokens are valid
        assertNotNull(localToken, "Local auth token should not be null");
        assertNotNull(googleToken, "Google auth token should not be null");
        assertNotNull(localToken.token(), "Local auth token string should not be null");
        assertNotNull(googleToken.token(), "Google auth token string should not be null");
        assertFalse(localToken.token().isEmpty(), "Local auth token should not be empty");
        assertFalse(googleToken.token().isEmpty(), "Google auth token should not be empty");
        
        // Parse JWT tokens
        SignedJWT localJwt = SignedJWT.parse(localToken.token());
        SignedJWT googleJwt = SignedJWT.parse(googleToken.token());
        
        JWTClaimsSet localClaims = localJwt.getJWTClaimsSet();
        JWTClaimsSet googleClaims = googleJwt.getJWTClaimsSet();
        
        // Verify both tokens have the same essential structure
        assertNotNull(localClaims, "Local token claims should not be null");
        assertNotNull(googleClaims, "Google token claims should not be null");
        
        // Verify both tokens have ROLE_PATIENT
        @SuppressWarnings("unchecked")
        List<String> localRoles = (List<String>) localClaims.getClaim("roles");
        @SuppressWarnings("unchecked")
        List<String> googleRoles = (List<String>) googleClaims.getClaim("roles");
        
        assertNotNull(localRoles, "Local token should have roles claim");
        assertNotNull(googleRoles, "Google token should have roles claim");
        assertTrue(localRoles.contains("ROLE_PATIENT"), "Local token should have ROLE_PATIENT");
        assertTrue(googleRoles.contains("ROLE_PATIENT"), "Google token should have ROLE_PATIENT");
        assertEquals(localRoles, googleRoles, "Both tokens should have identical roles");
        
        // Verify both tokens have the same essential claims structure
        assertNotNull(localClaims.getClaim("email"), "Local token should have email claim");
        assertNotNull(googleClaims.getClaim("email"), "Google token should have email claim");
        assertNotNull(localClaims.getClaim("name"), "Local token should have name claim");
        assertNotNull(googleClaims.getClaim("name"), "Google token should have name claim");
        assertNotNull(localClaims.getClaim("tenantProfileId"), "Local token should have tenantProfileId claim");
        assertNotNull(googleClaims.getClaim("tenantProfileId"), "Google token should have tenantProfileId claim");
        assertNotNull(localClaims.getClaim("globalPatientId"), "Local token should have globalPatientId claim");
        assertNotNull(googleClaims.getClaim("globalPatientId"), "Google token should have globalPatientId claim");
        
        // Verify both tokens have auth_provider claim
        String localAuthProvider = (String) localClaims.getClaim("auth_provider");
        String googleAuthProvider = (String) googleClaims.getClaim("auth_provider");
        
        assertNotNull(localAuthProvider, "Local token should have auth_provider claim");
        assertNotNull(googleAuthProvider, "Google token should have auth_provider claim");
        assertEquals("LOCAL", localAuthProvider, "Local token should have LOCAL auth_provider");
        assertEquals("GOOGLE", googleAuthProvider, "Google token should have GOOGLE auth_provider");
        
        // Verify both tokens have the same issuer and audience
        assertEquals(localClaims.getIssuer(), googleClaims.getIssuer(), 
            "Both tokens should have the same issuer");
        assertEquals(localClaims.getAudience(), googleClaims.getAudience(), 
            "Both tokens should have the same audience");
        
        // Verify both tokens have expiration times
        assertNotNull(localClaims.getExpirationTime(), "Local token should have expiration time");
        assertNotNull(googleClaims.getExpirationTime(), "Google token should have expiration time");
        
        // Verify expiration times are in the future
        assertTrue(localClaims.getExpirationTime().toInstant().isAfter(java.time.Instant.now()),
            "Local token should not be expired");
        assertTrue(googleClaims.getExpirationTime().toInstant().isAfter(java.time.Instant.now()),
            "Google token should not be expired");
        
        // Verify both tokens have similar expiration duration (within 1 second tolerance)
        long localExpDuration = localClaims.getExpirationTime().getTime() - localClaims.getIssueTime().getTime();
        long googleExpDuration = googleClaims.getExpirationTime().getTime() - googleClaims.getIssueTime().getTime();
        
        assertTrue(Math.abs(localExpDuration - googleExpDuration) < 1000,
            "Both tokens should have similar expiration duration");
    }

    /**
     * Property 4 (variant): Linked accounts (BOTH auth provider) produce valid sessions
     * 
     * For any patient with both local and Google credentials linked,
     * the session should indicate BOTH as the auth provider.
     * 
     * **Feature: patient-google-oauth, Property 4: Google and local sessions have equivalent privileges**
     * **Validates: Requirements 1.5**
     */
    @Property(tries = 100)
    public void linkedAccountsProduceValidSessions(
        @ForAll("patientEmails") String email,
        @ForAll("patientNames") String firstName,
        @ForAll("patientNames") String lastName,
        @ForAll("tenantIds") Long tenantId,
        @ForAll("googleIds") String googleId
    ) throws Exception {
        // Create JWT issuer
        JwtIssuer jwtIssuer = createJwtIssuer();
        
        // Create a tenant
        TenantEntity tenant = createTenant(tenantId);
        
        // Create a global patient with BOTH auth methods
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            email,
            "+1234567890",
            "$2a$10$hashedpassword",
            LocalDate.of(1990, 1, 1)
        );
        globalPatient.linkGoogleAccount(googleId, email);
        // After linking, auth provider should be BOTH
        assertEquals(AuthProvider.BOTH, globalPatient.getAuthProvider(),
            "Linked account should have BOTH auth provider");
        
        // Simulate persisted entity
        setId(globalPatient, 5000L + tenantId);
        
        // Create a tenant-scoped patient
        PatientEntity patient = new PatientEntity(firstName, lastName, email, "+1234567890");
        patient.setTenant(tenant);
        patient.setGlobalPatient(globalPatient);
        setId(patient, 6000L + tenantId);
        
        // Generate JWT token
        JwtIssuer.IssuedToken token = jwtIssuer.issuePatientAccessToken(patient);
        
        // Verify token is valid
        assertNotNull(token, "Token should not be null");
        assertNotNull(token.token(), "Token string should not be null");
        
        // Parse JWT token
        SignedJWT jwt = SignedJWT.parse(token.token());
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        
        // Verify auth_provider is BOTH
        String authProvider = (String) claims.getClaim("auth_provider");
        assertEquals("BOTH", authProvider, "Linked account token should have BOTH auth_provider");
        
        // Verify all essential claims are present
        assertNotNull(claims.getClaim("email"), "Token should have email claim");
        assertNotNull(claims.getClaim("name"), "Token should have name claim");
        assertNotNull(claims.getClaim("roles"), "Token should have roles claim");
        assertNotNull(claims.getClaim("tenantProfileId"), "Token should have tenantProfileId claim");
        assertNotNull(claims.getClaim("globalPatientId"), "Token should have globalPatientId claim");
        
        // Verify ROLE_PATIENT is present
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.getClaim("roles");
        assertTrue(roles.contains("ROLE_PATIENT"), "Token should have ROLE_PATIENT");
    }

    // Helper methods
    
    private TenantEntity createTenant(Long tenantId) {
        TenantEntity tenant = new TenantEntity("test-clinic-" + tenantId, "Test Clinic " + tenantId);
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
    Arbitrary<String> patientEmails() {
        String[] domains = {"gmail.com", "yahoo.com", "outlook.com", "example.com", "test.com"};
        
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(15),
            Arbitraries.of(domains)
        ).as((username, domain) -> username.toLowerCase() + "@" + domain);
    }

    @Provide
    Arbitrary<String> patientNames() {
        String[] names = {"John", "Jane", "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", 
                         "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        
        return Arbitraries.of(names);
    }

    @Provide
    Arbitrary<Long> tenantIds() {
        return Arbitraries.longs().between(1L, 1000L);
    }

    @Provide
    Arbitrary<String> googleIds() {
        return Arbitraries.longs().between(100000000000000000L, 999999999999999999L)
            .map(String::valueOf);
    }
}
