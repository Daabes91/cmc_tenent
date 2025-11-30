package com.clinic.modules.saas;

import com.clinic.modules.saas.dto.SaasLoginRequest;
import com.clinic.modules.saas.dto.SaasLoginResponse;
import com.clinic.modules.saas.dto.TenantCreateRequest;
import com.clinic.modules.saas.dto.TenantUpdateRequest;
import com.clinic.modules.saas.model.SaasManager;
import com.clinic.modules.saas.model.SaasManagerStatus;
import com.clinic.modules.saas.repository.SaasManagerRepository;
import com.clinic.modules.saas.service.SaasManagerService;
import com.clinic.modules.saas.service.TenantManagementService;
import com.clinic.modules.core.tenant.TenantRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Tenant CRUD API endpoints.
 * Tests all CRUD operations with authentication, validation, and error
 * handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TenantCrudApiTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private SaasManagerService saasManagerService;

        @Autowired
        private SaasManagerRepository saasManagerRepository;

        @Autowired
        private TenantManagementService tenantManagementService;

        @Autowired
        private TenantRepository tenantRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private ObjectMapper objectMapper;

        private String validToken;
        private static final String MANAGER_EMAIL = "api.test@saas.com";
        private static final String MANAGER_PASSWORD = "TestPassword123!";

        @BeforeEach
        void setUp() {
                // Clean up test data
                saasManagerRepository.findByEmailIgnoreCase(MANAGER_EMAIL)
                                .ifPresent(saasManagerRepository::delete);

                // Create test SAAS Manager
                SaasManager manager = new SaasManager(
                                MANAGER_EMAIL,
                                "API Test Manager",
                                passwordEncoder.encode(MANAGER_PASSWORD));
                manager.setStatus(SaasManagerStatus.ACTIVE);
                saasManagerRepository.save(manager);

                // Authenticate and get token
                SaasLoginRequest loginRequest = new SaasLoginRequest(MANAGER_EMAIL, MANAGER_PASSWORD);
                SaasLoginResponse loginResponse = saasManagerService.authenticate(loginRequest);
                validToken = loginResponse.accessToken();

                // Clean up test tenants
                tenantRepository.findAll().stream()
                                .filter(t -> !t.getSlug().equals("default"))
                                .forEach(tenantRepository::delete);
        }

        // ========== CREATE TENANT TESTS ==========

        @Test
        void shouldCreateTenantWithValidToken() throws Exception {
                // Given
                TenantCreateRequest request = new TenantCreateRequest(
                                "api-test-clinic",
                                "API Test Clinic",
                                "api-test.example.com");

                // When/Then
                mockMvc.perform(post("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").isNumber())
                                .andExpect(jsonPath("$.slug").value("api-test-clinic"))
                                .andExpect(jsonPath("$.name").value("API Test Clinic"))
                                .andExpect(jsonPath("$.customDomain").value("api-test.example.com"))
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                                .andExpect(jsonPath("$.adminCredentials.email")
                                                .value("admin@api-test-clinic.clinic.local"))
                                .andExpect(jsonPath("$.adminCredentials.password").isNotEmpty())
                                .andExpect(jsonPath("$.adminCredentials.password").value(hasLength(16)));
        }

        @Test
        void shouldReturn401WhenCreatingTenantWithoutAuthentication() throws Exception {
                // Given
                TenantCreateRequest request = new TenantCreateRequest(
                                "no-auth-clinic",
                                "No Auth Clinic",
                                null);

                // When/Then
                mockMvc.perform(post("/saas/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn409WhenCreatingTenantWithDuplicateSlug() throws Exception {
                // Given - Create first tenant
                TenantCreateRequest request1 = new TenantCreateRequest(
                                "duplicate-slug",
                                "First Clinic",
                                null);
                tenantManagementService.createTenant(request1);

                // When/Then - Try to create second tenant with same slug
                TenantCreateRequest request2 = new TenantCreateRequest(
                                "duplicate-slug",
                                "Second Clinic",
                                null);

                mockMvc.perform(post("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value(containsString("duplicate-slug")))
                                .andExpect(jsonPath("$.message").value(containsString("already exists")));
        }

        @Test
        void shouldReturn400WhenCreatingTenantWithInvalidSlug() throws Exception {
                // Given - Slug with uppercase letters (invalid)
                TenantCreateRequest request = new TenantCreateRequest(
                                "Invalid-Slug",
                                "Invalid Clinic",
                                null);

                // When/Then
                mockMvc.perform(post("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenCreatingTenantWithTooShortSlug() throws Exception {
                // Given - Slug too short (less than 3 characters)
                TenantCreateRequest request = new TenantCreateRequest(
                                "ab",
                                "Short Slug Clinic",
                                null);

                // When/Then
                mockMvc.perform(post("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenCreatingTenantWithBlankName() throws Exception {
                // Given - Blank name
                TenantCreateRequest request = new TenantCreateRequest(
                                "valid-slug",
                                "",
                                null);

                // When/Then
                mockMvc.perform(post("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenCreatingTenantWithInvalidCustomDomain() throws Exception {
                // Given - Invalid custom domain format
                TenantCreateRequest request = new TenantCreateRequest(
                                "valid-slug",
                                "Valid Name",
                                "not a valid domain");

                // When/Then
                mockMvc.perform(post("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // ========== LIST TENANTS TESTS ==========

        @Test
        void shouldListTenantsWithValidToken() throws Exception {
                // Given - Create some tenants
                tenantManagementService.createTenant(new TenantCreateRequest("list-test-1", "List Test 1", null));
                tenantManagementService.createTenant(new TenantCreateRequest("list-test-2", "List Test 2", null));

                // When/Then
                mockMvc.perform(get("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                                .andExpect(jsonPath("$.page").isNumber())
                                .andExpect(jsonPath("$.size").isNumber())
                                .andExpect(jsonPath("$.totalElements").isNumber())
                                .andExpect(jsonPath("$.totalPages").isNumber());
        }

        @Test
        void shouldReturn401WhenListingTenantsWithoutAuthentication() throws Exception {
                // When/Then
                mockMvc.perform(get("/saas/tenants"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldListTenantsWithPagination() throws Exception {
                // Given - Create multiple tenants
                for (int i = 1; i <= 5; i++) {
                        tenantManagementService.createTenant(
                                        new TenantCreateRequest("page-test-" + i, "Page Test " + i, null));
                }

                // When/Then - Request first page with size 2
                mockMvc.perform(get("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken)
                                .param("page", "0")
                                .param("size", "2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.page").value(0))
                                .andExpect(jsonPath("$.size").value(2));
        }

        @Test
        void shouldExcludeDeletedTenantsByDefault() throws Exception {
                // Given - Create and delete a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("to-delete", "To Delete", null));
                tenantManagementService.softDeleteTenant(created.id());

                // When/Then - List without includeDeleted parameter
                mockMvc.perform(get("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.slug == 'to-delete')]").doesNotExist());
        }

        @Test
        void shouldIncludeDeletedTenantsWhenRequested() throws Exception {
                // Given - Create and delete a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("deleted-tenant", "Deleted Tenant", null));
                tenantManagementService.softDeleteTenant(created.id());

                // When/Then - List with includeDeleted=true
                mockMvc.perform(get("/saas/tenants")
                                .header("Authorization", "Bearer " + validToken)
                                .param("includeDeleted", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.slug == 'deleted-tenant')]").exists())
                                .andExpect(jsonPath("$.content[?(@.slug == 'deleted-tenant')].deletedAt").isNotEmpty());
        }

        // ========== GET TENANT TESTS ==========

        @Test
        void shouldGetTenantByIdWithValidToken() throws Exception {
                // Given - Create a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("get-test", "Get Test Clinic", "get.example.com"));

                // When/Then
                mockMvc.perform(get("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(created.id()))
                                .andExpect(jsonPath("$.slug").value("get-test"))
                                .andExpect(jsonPath("$.name").value("Get Test Clinic"))
                                .andExpect(jsonPath("$.customDomain").value("get.example.com"))
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andExpect(jsonPath("$.deletedAt").isEmpty());
        }

        @Test
        void shouldReturn401WhenGettingTenantWithoutAuthentication() throws Exception {
                // When/Then
                mockMvc.perform(get("/saas/tenants/1"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn404WhenGettingNonExistentTenant() throws Exception {
                // When/Then
                mockMvc.perform(get("/saas/tenants/99999")
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value(containsString("not found")));
        }

        @Test
        void shouldReturn404WhenGettingDeletedTenantWithoutIncludeDeleted() throws Exception {
                // Given - Create and delete a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("deleted-get", "Deleted Get", null));
                tenantManagementService.softDeleteTenant(created.id());

                // When/Then
                mockMvc.perform(get("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldGetDeletedTenantWithIncludeDeleted() throws Exception {
                // Given - Create and delete a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("deleted-include", "Deleted Include", null));
                tenantManagementService.softDeleteTenant(created.id());

                // When/Then
                mockMvc.perform(get("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken)
                                .param("includeDeleted", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(created.id()))
                                .andExpect(jsonPath("$.deletedAt").isNotEmpty())
                                .andExpect(jsonPath("$.status").value("INACTIVE"));
        }

        // ========== UPDATE TENANT TESTS ==========

        @Test
        void shouldUpdateTenantWithValidToken() throws Exception {
                // Given - Create a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("update-test", "Original Name", "original.example.com"));

                // When/Then - Update tenant
                TenantUpdateRequest updateRequest = new TenantUpdateRequest(
                                "Updated Name",
                                "updated.example.com",
                                null);

                mockMvc.perform(put("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(created.id()))
                                .andExpect(jsonPath("$.slug").value("update-test")) // Slug should not change
                                .andExpect(jsonPath("$.name").value("Updated Name"))
                                .andExpect(jsonPath("$.customDomain").value("updated.example.com"));
        }

        @Test
        void shouldReturn401WhenUpdatingTenantWithoutAuthentication() throws Exception {
                // Given
                TenantUpdateRequest request = new TenantUpdateRequest("New Name", null, null);

                // When/Then
                mockMvc.perform(put("/saas/tenants/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentTenant() throws Exception {
                // Given
                TenantUpdateRequest request = new TenantUpdateRequest("New Name", null, null);

                // When/Then
                mockMvc.perform(put("/saas/tenants/99999")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenUpdatingDeletedTenant() throws Exception {
                // Given - Create and delete a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("deleted-update", "Deleted Update", null));
                tenantManagementService.softDeleteTenant(created.id());

                // When/Then
                TenantUpdateRequest request = new TenantUpdateRequest("New Name", null, null);

                mockMvc.perform(put("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn400WhenUpdatingWithInvalidCustomDomain() throws Exception {
                // Given - Create a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("invalid-domain-update", "Test", null));

                // When/Then - Update with invalid domain
                TenantUpdateRequest request = new TenantUpdateRequest(null, "invalid domain format", null);

                mockMvc.perform(put("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // ========== DELETE TENANT TESTS ==========

        @Test
        void shouldSoftDeleteTenantWithValidToken() throws Exception {
                // Given - Create a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("delete-test", "Delete Test", null));

                // When/Then - Delete tenant
                mockMvc.perform(delete("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isNoContent());

                // Verify tenant is soft-deleted
                mockMvc.perform(get("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isNotFound());

                // Verify tenant exists with includeDeleted
                mockMvc.perform(get("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken)
                                .param("includeDeleted", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedAt").isNotEmpty());
        }

        @Test
        void shouldReturn401WhenDeletingTenantWithoutAuthentication() throws Exception {
                // When/Then
                mockMvc.perform(delete("/saas/tenants/1"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn404WhenDeletingNonExistentTenant() throws Exception {
                // When/Then
                mockMvc.perform(delete("/saas/tenants/99999")
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenDeletingAlreadyDeletedTenant() throws Exception {
                // Given - Create and delete a tenant
                var created = tenantManagementService.createTenant(
                                new TenantCreateRequest("already-deleted", "Already Deleted", null));
                tenantManagementService.softDeleteTenant(created.id());

                // When/Then - Try to delete again
                mockMvc.perform(delete("/saas/tenants/" + created.id())
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isNotFound());
        }

        // ========== AUTHENTICATION AND AUTHORIZATION TESTS ==========

        @Test
        void shouldRejectAllEndpointsWithInvalidToken() throws Exception {
                String invalidToken = "invalid.jwt.token";

                mockMvc.perform(get("/saas/tenants")
                                .header("Authorization", "Bearer " + invalidToken))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(post("/saas/tenants")
                                .header("Authorization", "Bearer " + invalidToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(get("/saas/tenants/1")
                                .header("Authorization", "Bearer " + invalidToken))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(put("/saas/tenants/1")
                                .header("Authorization", "Bearer " + invalidToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(delete("/saas/tenants/1")
                                .header("Authorization", "Bearer " + invalidToken))
                                .andExpect(status().isUnauthorized());
        }
}
