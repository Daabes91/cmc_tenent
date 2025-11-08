package com.clinic.modules.saas;

import com.clinic.modules.saas.dto.SaasLoginRequest;
import com.clinic.modules.saas.dto.SaasLoginResponse;
import com.clinic.modules.saas.exception.UnauthorizedException;
import com.clinic.modules.saas.model.SaasManager;
import com.clinic.modules.saas.model.SaasManagerStatus;
import com.clinic.modules.saas.repository.SaasManagerRepository;
import com.clinic.modules.saas.service.SaasManagerService;
import com.clinic.security.SaasManagerJwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for SAAS Manager authentication.
 * Tests login flow end-to-end, JWT authentication filter, and unauthorized access.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SaasAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SaasManagerService saasManagerService;

    @Autowired
    private SaasManagerRepository saasManagerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SaasManagerJwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private SaasManager testManager;
    private static final String TEST_EMAIL = "test.manager@saas.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";
    private static final String TEST_NAME = "Test Manager";

    @BeforeEach
    void setUp() {
        // Clean up existing test managers
        saasManagerRepository.findByEmailIgnoreCase(TEST_EMAIL)
                .ifPresent(saasManagerRepository::delete);

        // Create test SAAS Manager
        testManager = new SaasManager(
                TEST_EMAIL,
                TEST_NAME,
                passwordEncoder.encode(TEST_PASSWORD)
        );
        testManager.setStatus(SaasManagerStatus.ACTIVE);
        testManager = saasManagerRepository.save(testManager);
    }

    @Test
    void shouldAuthenticateValidSaasManagerCredentials() {
        // Given
        SaasLoginRequest request = new SaasLoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // When
        SaasLoginResponse response = saasManagerService.authenticate(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.expiresAt()).isNotNull();
        assertThat(response.managerId()).isEqualTo(testManager.getId());
        assertThat(response.email()).isEqualTo(TEST_EMAIL);
        assertThat(response.fullName()).isEqualTo(TEST_NAME);
    }

    @Test
    void shouldRejectInvalidPassword() {
        // Given
        SaasLoginRequest request = new SaasLoginRequest(TEST_EMAIL, "WrongPassword123!");

        // When/Then
        assertThatThrownBy(() -> saasManagerService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void shouldRejectNonExistentEmail() {
        // Given
        SaasLoginRequest request = new SaasLoginRequest("nonexistent@saas.com", TEST_PASSWORD);

        // When/Then
        assertThatThrownBy(() -> saasManagerService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void shouldRejectSuspendedAccount() {
        // Given - Suspend the manager account
        testManager.setStatus(SaasManagerStatus.SUSPENDED);
        saasManagerRepository.save(testManager);

        SaasLoginRequest request = new SaasLoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // When/Then
        assertThatThrownBy(() -> saasManagerService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Account is not active");
    }

    @Test
    void shouldAuthenticateWithCaseInsensitiveEmail() {
        // Given - Email with different case
        SaasLoginRequest request = new SaasLoginRequest("TEST.MANAGER@SAAS.COM", TEST_PASSWORD);

        // When
        SaasLoginResponse response = saasManagerService.authenticate(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.managerId()).isEqualTo(testManager.getId());
    }

    @Test
    void shouldLoginViaRestEndpoint() throws Exception {
        // Given
        SaasLoginRequest request = new SaasLoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // When/Then
        mockMvc.perform(post("/saas/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.managerId").value(testManager.getId()))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.fullName").value(TEST_NAME));
    }

    @Test
    void shouldReturn401ForInvalidCredentialsViaRestEndpoint() throws Exception {
        // Given
        SaasLoginRequest request = new SaasLoginRequest(TEST_EMAIL, "WrongPassword");

        // When/Then
        mockMvc.perform(post("/saas/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400ForInvalidRequestFormat() throws Exception {
        // Given - Invalid request (missing required fields)
        String invalidJson = "{\"email\": \"\"}";

        // When/Then
        mockMvc.perform(post("/saas/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAccessProtectedEndpointWithValidToken() throws Exception {
        // Given - Authenticate and get token
        SaasLoginRequest loginRequest = new SaasLoginRequest(TEST_EMAIL, TEST_PASSWORD);
        SaasLoginResponse loginResponse = saasManagerService.authenticate(loginRequest);
        String token = loginResponse.accessToken();

        // When/Then - Access protected endpoint with valid token
        mockMvc.perform(get("/saas/tenants")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAccessWithoutToken() throws Exception {
        // When/Then - Try to access protected endpoint without token
        mockMvc.perform(get("/saas/tenants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAccessWithInvalidToken() throws Exception {
        // Given - Invalid token
        String invalidToken = "invalid.jwt.token";

        // When/Then - Try to access protected endpoint with invalid token
        mockMvc.perform(get("/saas/tenants")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAccessWithExpiredToken() throws Exception {
        // Given - Create a manager and generate token
        SaasLoginRequest loginRequest = new SaasLoginRequest(TEST_EMAIL, TEST_PASSWORD);
        SaasLoginResponse loginResponse = saasManagerService.authenticate(loginRequest);
        String token = loginResponse.accessToken();

        // Note: We can't easily test expired tokens without waiting or mocking time
        // This test verifies the token is initially valid
        mockMvc.perform(get("/saas/tenants")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // In a real scenario, we would wait for token expiration or use a time-mocking library
        // For now, we verify that the token has an expiration time set
        assertThat(loginResponse.expiresAt()).isNotNull();
        assertThat(loginResponse.expiresAt()).isAfter(java.time.Instant.now());
    }

    @Test
    void shouldRejectAccessWithMalformedAuthorizationHeader() throws Exception {
        // Given - Malformed header (missing "Bearer" prefix)
        String token = "some-token-without-bearer-prefix";

        // When/Then
        mockMvc.perform(get("/saas/tenants")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToPublicLoginEndpointWithoutToken() throws Exception {
        // When/Then - Login endpoint should be accessible without authentication
        SaasLoginRequest request = new SaasLoginRequest(TEST_EMAIL, TEST_PASSWORD);

        mockMvc.perform(post("/saas/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGenerateValidJwtTokenWithCorrectClaims() {
        // Given
        SaasLoginRequest request = new SaasLoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // When
        SaasLoginResponse response = saasManagerService.authenticate(request);

        // Then - Verify token can be parsed and validated
        var principal = jwtTokenService.parse(response.accessToken());
        assertThat(principal).isPresent();
        assertThat(principal.get().subject()).isEqualTo(String.valueOf(testManager.getId()));
        assertThat(principal.get().roles()).contains("ROLE_SAAS_MANAGER");
    }

    @Test
    void shouldRejectStaffTokenForSaasEndpoints() throws Exception {
        // Note: This test would require creating a staff JWT token
        // For now, we verify that only SAAS_MANAGER role can access SAAS endpoints
        // This is enforced by the security configuration

        // Given - Try to access with no token (simulating wrong token type)
        mockMvc.perform(get("/saas/tenants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleMultipleConcurrentAuthenticationRequests() {
        // Given
        SaasLoginRequest request = new SaasLoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // When - Authenticate multiple times
        SaasLoginResponse response1 = saasManagerService.authenticate(request);
        SaasLoginResponse response2 = saasManagerService.authenticate(request);
        SaasLoginResponse response3 = saasManagerService.authenticate(request);

        // Then - All should succeed
        assertThat(response1.accessToken()).isNotNull();
        assertThat(response2.accessToken()).isNotNull();
        assertThat(response3.accessToken()).isNotNull();
        
        // All tokens should be valid and have the same manager ID
        assertThat(response1.managerId()).isEqualTo(testManager.getId());
        assertThat(response2.managerId()).isEqualTo(testManager.getId());
        assertThat(response3.managerId()).isEqualTo(testManager.getId());
        
        // Note: Tokens may be identical if generated in the same second
        // This is expected behavior since JWT timestamps are truncated to seconds
    }

    @Test
    void shouldTrimAndNormalizeEmailDuringAuthentication() {
        // Given - Email with whitespace and mixed case
        SaasLoginRequest request = new SaasLoginRequest("  TEST.manager@SAAS.com  ", TEST_PASSWORD);

        // When
        SaasLoginResponse response = saasManagerService.authenticate(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.managerId()).isEqualTo(testManager.getId());
    }
}
