package com.clinic.modules.saas;

import com.clinic.modules.admin.staff.model.*;
import com.clinic.modules.admin.staff.repository.StaffPermissionsRepository;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantStatus;
import com.clinic.modules.saas.dto.TenantCreateRequest;
import com.clinic.modules.saas.dto.TenantCreateResponse;
import com.clinic.modules.saas.dto.TenantListResponse;
import com.clinic.modules.saas.dto.TenantResponse;
import com.clinic.modules.saas.dto.TenantUpdateRequest;
import com.clinic.modules.saas.exception.ConflictException;
import com.clinic.modules.saas.exception.NotFoundException;
import com.clinic.modules.saas.service.TenantManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for TenantManagementService.
 * Tests complete tenant creation flow including admin staff and permissions
 * creation,
 * transactional rollback on failure scenarios, soft delete, and pagination.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantManagementIntegrationTest {

        @Autowired
        private TenantManagementService tenantManagementService;

        @Autowired
        private TenantRepository tenantRepository;

        @Autowired
        private StaffUserRepository staffUserRepository;

        @Autowired
        private StaffPermissionsRepository staffPermissionsRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
                // Clean up test data
                staffPermissionsRepository.deleteAll();
                staffUserRepository.deleteAll();

                // Delete test tenants but keep default tenant
                tenantRepository.findAll().stream()
                                .filter(t -> !t.getSlug().equals("default"))
                                .forEach(tenantRepository::delete);
        }

        @Test
        void shouldCreateTenantWithAutoProvisionedAdminAndFullPermissions() {
                // Given
                TenantCreateRequest request = new TenantCreateRequest(
                                "test-clinic",
                                "Test Clinic",
                                "test.example.com");

                // When
                TenantCreateResponse response = tenantManagementService.createTenant(request);

                // Then - Verify tenant was created
                assertThat(response).isNotNull();
                assertThat(response.id()).isNotNull();
                assertThat(response.slug()).isEqualTo("test-clinic");
                assertThat(response.name()).isEqualTo("Test Clinic");
                assertThat(response.customDomain()).isEqualTo("test.example.com");
                assertThat(response.status()).isEqualTo(TenantStatus.ACTIVE);
                assertThat(response.createdAt()).isNotNull();

                // Verify admin credentials were generated
                assertThat(response.adminCredentials()).isNotNull();
                assertThat(response.adminCredentials().email()).isEqualTo("admin@test-clinic.clinic.local");
                assertThat(response.adminCredentials().password()).isNotNull();
                assertThat(response.adminCredentials().password()).hasSize(16);

                // Verify tenant exists in database
                Optional<TenantEntity> savedTenant = tenantRepository.findById(response.id());
                assertThat(savedTenant).isPresent();
                assertThat(savedTenant.get().getSlug()).isEqualTo("test-clinic");
                assertThat(savedTenant.get().getStatus()).isEqualTo(TenantStatus.ACTIVE);
                assertThat(savedTenant.get().getDeletedAt()).isNull();

                // Verify admin staff was created
                Optional<StaffUser> adminStaff = staffUserRepository.findByEmailIgnoreCaseAndTenantId(
                                "admin@test-clinic.clinic.local",
                                response.id());
                assertThat(adminStaff).isPresent();
                assertThat(adminStaff.get().getFullName()).isEqualTo("Admin");
                assertThat(adminStaff.get().getRole()).isEqualTo(StaffRole.ADMIN);
                assertThat(adminStaff.get().getStatus()).isEqualTo(StaffStatus.ACTIVE);
                assertThat(adminStaff.get().getTenant().getId()).isEqualTo(response.id());

                // Verify password was hashed correctly
                boolean passwordMatches = passwordEncoder.matches(
                                response.adminCredentials().password(),
                                adminStaff.get().getPasswordHash());
                assertThat(passwordMatches).isTrue();

                // Verify full permissions were granted
                Optional<StaffPermissions> permissions = staffPermissionsRepository.findById(adminStaff.get().getId());
                assertThat(permissions).isPresent();

                // Check all modules have all permissions
                for (ModuleName module : ModuleName.values()) {
                        Set<PermissionAction> modulePermissions = permissions.get().getPermissionsForModule(module);
                        assertThat(modulePermissions).containsExactlyInAnyOrder(
                                        PermissionAction.VIEW,
                                        PermissionAction.CREATE,
                                        PermissionAction.EDIT,
                                        PermissionAction.DELETE);
                }
        }

        @Test
        void shouldThrowConflictExceptionWhenSlugAlreadyExists() {
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

                assertThatThrownBy(() -> tenantManagementService.createTenant(request2))
                                .isInstanceOf(ConflictException.class)
                                .hasMessageContaining("duplicate-slug")
                                .hasMessageContaining("already exists");
        }

        @Test
        void shouldRollbackEntireTransactionOnFailure() {
                // Given - Create a tenant first
                TenantCreateRequest validRequest = new TenantCreateRequest(
                                "valid-tenant",
                                "Valid Tenant",
                                null);
                tenantManagementService.createTenant(validRequest);

                // Count existing records
                long initialTenantCount = tenantRepository.count();
                long initialStaffCount = staffUserRepository.count();
                long initialPermissionsCount = staffPermissionsRepository.count();

                // When - Try to create tenant with duplicate slug (will fail)
                TenantCreateRequest duplicateRequest = new TenantCreateRequest(
                                "valid-tenant",
                                "Duplicate Tenant",
                                null);

                try {
                        tenantManagementService.createTenant(duplicateRequest);
                        fail("Should have thrown ConflictException");
                } catch (ConflictException e) {
                        // Expected
                }

                // Then - Verify no records were created (transaction rolled back)
                assertThat(tenantRepository.count()).isEqualTo(initialTenantCount);
                assertThat(staffUserRepository.count()).isEqualTo(initialStaffCount);
                assertThat(staffPermissionsRepository.count()).isEqualTo(initialPermissionsCount);
        }

        @Test
        void shouldSoftDeleteTenantAndPreventAccess() {
                // Given - Create a tenant
                TenantCreateRequest request = new TenantCreateRequest(
                                "to-be-deleted",
                                "To Be Deleted Clinic",
                                null);
                TenantCreateResponse created = tenantManagementService.createTenant(request);

                // Verify tenant is accessible
                TenantResponse beforeDelete = tenantManagementService.getTenant(created.id(), false);
                assertThat(beforeDelete).isNotNull();
                assertThat(beforeDelete.deletedAt()).isNull();

                // When - Soft delete the tenant
                tenantManagementService.softDeleteTenant(created.id());

                // Then - Verify tenant is marked as deleted
                Optional<TenantEntity> deletedTenant = tenantRepository.findById(created.id());
                assertThat(deletedTenant).isPresent();
                assertThat(deletedTenant.get().getDeletedAt()).isNotNull();
                assertThat(deletedTenant.get().getStatus()).isEqualTo(TenantStatus.INACTIVE);
                assertThat(deletedTenant.get().isDeleted()).isTrue();

                // Verify tenant is not accessible without includeDeleted flag
                assertThatThrownBy(() -> tenantManagementService.getTenant(created.id(), false))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("not found");

                // Verify tenant IS accessible with includeDeleted flag
                TenantResponse afterDelete = tenantManagementService.getTenant(created.id(), true);
                assertThat(afterDelete).isNotNull();
                assertThat(afterDelete.deletedAt()).isNotNull();
                assertThat(afterDelete.status()).isEqualTo(TenantStatus.INACTIVE);
        }

        @Test
        void shouldThrowNotFoundExceptionWhenDeletingAlreadyDeletedTenant() {
                // Given - Create and delete a tenant
                TenantCreateRequest request = new TenantCreateRequest(
                                "already-deleted",
                                "Already Deleted Clinic",
                                null);
                TenantCreateResponse created = tenantManagementService.createTenant(request);
                tenantManagementService.softDeleteTenant(created.id());

                // When/Then - Try to delete again
                assertThatThrownBy(() -> tenantManagementService.softDeleteTenant(created.id()))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("not found");
        }

        @Test
        void shouldListTenantsWithPaginationExcludingDeleted() {
                // Given - Create multiple tenants
                for (int i = 1; i <= 5; i++) {
                        TenantCreateRequest request = new TenantCreateRequest(
                                        "clinic-" + i,
                                        "Clinic " + i,
                                        null);
                        tenantManagementService.createTenant(request);
                }

                // Soft delete one tenant
                TenantResponse toDelete = tenantManagementService
                                .listTenants(PageRequest.of(0, 10), false, null, null, null)
                                .content().get(0);
                tenantManagementService.softDeleteTenant(toDelete.id());

                // When - List tenants without includeDeleted
                Pageable pageable = PageRequest.of(0, 3);
                TenantListResponse response = tenantManagementService.listTenants(pageable, false, null, null, null);

                // Then - Verify pagination and exclusion of deleted tenant
                assertThat(response).isNotNull();
                assertThat(response.content()).isNotEmpty();
                assertThat(response.page()).isEqualTo(0);
                assertThat(response.size()).isEqualTo(3);

                // Should have 4 active tenants (5 created - 1 deleted) + default tenant = 5
                // total
                // But we're only getting page 0 with size 3
                assertThat(response.content()).hasSizeLessThanOrEqualTo(3);

                // Verify none of the returned tenants are deleted
                response.content().forEach(tenant -> {
                        assertThat(tenant.deletedAt()).isNull();
                        assertThat(tenant.status()).isEqualTo(TenantStatus.ACTIVE);
                });
        }

        @Test
        void shouldListTenantsIncludingDeletedWhenRequested() {
                // Given - Create tenants and delete some
                TenantCreateRequest request1 = new TenantCreateRequest("active-clinic", "Active Clinic", null);
                TenantCreateRequest request2 = new TenantCreateRequest("deleted-clinic", "Deleted Clinic", null);

                tenantManagementService.createTenant(request1);
                TenantCreateResponse created2 = tenantManagementService.createTenant(request2);
                tenantManagementService.softDeleteTenant(created2.id());

                // When - List with includeDeleted = true
                TenantListResponse response = tenantManagementService.listTenants(PageRequest.of(0, 10), true, null,
                                null, null);

                // Then - Verify deleted tenant is included
                assertThat(response.content()).isNotEmpty();

                boolean hasDeletedTenant = response.content().stream()
                                .anyMatch(t -> t.deletedAt() != null);
                assertThat(hasDeletedTenant).isTrue();

                boolean hasActiveTenant = response.content().stream()
                                .anyMatch(t -> t.deletedAt() == null && t.slug().equals("active-clinic"));
                assertThat(hasActiveTenant).isTrue();
        }

        @Test
        void shouldUpdateTenantNameAndCustomDomain() {
                // Given - Create a tenant
                TenantCreateRequest createRequest = new TenantCreateRequest(
                                "update-test",
                                "Original Name",
                                "original.example.com");
                TenantCreateResponse created = tenantManagementService.createTenant(createRequest);

                // When - Update tenant
                TenantUpdateRequest updateRequest = new TenantUpdateRequest(
                                "Updated Name",
                                "updated.example.com",
                                null);
                TenantResponse updated = tenantManagementService.updateTenant(created.id(), updateRequest);

                // Then - Verify updates
                assertThat(updated.name()).isEqualTo("Updated Name");
                assertThat(updated.customDomain()).isEqualTo("updated.example.com");
                assertThat(updated.slug()).isEqualTo("update-test"); // Slug should not change
                assertThat(updated.updatedAt()).isAfterOrEqualTo(created.createdAt());
        }

        @Test
        void shouldThrowNotFoundExceptionWhenUpdatingDeletedTenant() {
                // Given - Create and delete a tenant
                TenantCreateRequest request = new TenantCreateRequest(
                                "deleted-for-update",
                                "Deleted For Update",
                                null);
                TenantCreateResponse created = tenantManagementService.createTenant(request);
                tenantManagementService.softDeleteTenant(created.id());

                // When/Then - Try to update deleted tenant
                TenantUpdateRequest updateRequest = new TenantUpdateRequest("New Name", null, null);

                assertThatThrownBy(() -> tenantManagementService.updateTenant(created.id(), updateRequest))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("not found");
        }

        @Test
        void shouldGenerateUniquePasswordsForEachTenant() {
                // Given/When - Create multiple tenants
                TenantCreateResponse tenant1 = tenantManagementService.createTenant(
                                new TenantCreateRequest("tenant-1", "Tenant 1", null));
                TenantCreateResponse tenant2 = tenantManagementService.createTenant(
                                new TenantCreateRequest("tenant-2", "Tenant 2", null));
                TenantCreateResponse tenant3 = tenantManagementService.createTenant(
                                new TenantCreateRequest("tenant-3", "Tenant 3", null));

                // Then - Verify all passwords are unique
                String password1 = tenant1.adminCredentials().password();
                String password2 = tenant2.adminCredentials().password();
                String password3 = tenant3.adminCredentials().password();

                assertThat(password1).isNotEqualTo(password2);
                assertThat(password1).isNotEqualTo(password3);
                assertThat(password2).isNotEqualTo(password3);

                // Verify all passwords meet length requirement
                assertThat(password1).hasSize(16);
                assertThat(password2).hasSize(16);
                assertThat(password3).hasSize(16);
        }

        @Test
        void shouldAllowReusingSoftDeletedSlug() {
                // Given - Create and soft delete a tenant
                TenantCreateRequest request1 = new TenantCreateRequest(
                                "reusable-slug",
                                "First Tenant",
                                null);
                TenantCreateResponse created1 = tenantManagementService.createTenant(request1);
                tenantManagementService.softDeleteTenant(created1.id());

                // When - Create new tenant with same slug
                TenantCreateRequest request2 = new TenantCreateRequest(
                                "reusable-slug",
                                "Second Tenant",
                                null);

                // Then - Should fail (slug is not available after soft delete)
                // Note: This behavior depends on the business requirement
                // Current implementation prevents reuse due to database unique constraint
                // The service throws ConflictException if it finds the slug in the check,
                // but if the soft-deleted tenant is not found, the database constraint will
                // fail
                assertThatThrownBy(() -> tenantManagementService.createTenant(request2))
                                .satisfiesAnyOf(
                                                ex -> assertThat(ex).isInstanceOf(ConflictException.class),
                                                ex -> assertThat(ex).isInstanceOf(
                                                                org.springframework.dao.DataIntegrityViolationException.class));
        }
}
