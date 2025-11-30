package com.clinic.modules.saas;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.service.BillingAccessControlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for billing access control.
 * Tests that billing status checks work correctly and use caching.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BillingAccessControlIntegrationTest {

    @Autowired
    private BillingAccessControlService billingAccessControlService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CacheManager cacheManager;

    private TenantEntity activeTenant;
    private TenantEntity pendingPaymentTenant;
    private TenantEntity pastDueTenant;
    private TenantEntity canceledTenant;

    @BeforeEach
    public void setUp() {
        // Clean up test data
        tenantRepository.deleteAll();
        
        // Clear cache before each test
        var cache = cacheManager.getCache("billingStatus");
        if (cache != null) {
            cache.clear();
        }

        // Create tenants with different billing statuses
        activeTenant = createTenant("active-clinic", "Active Clinic", BillingStatus.ACTIVE);
        pendingPaymentTenant = createTenant("pending-clinic", "Pending Clinic", BillingStatus.PENDING_PAYMENT);
        pastDueTenant = createTenant("pastdue-clinic", "Past Due Clinic", BillingStatus.PAST_DUE);
        canceledTenant = createTenant("canceled-clinic", "Canceled Clinic", BillingStatus.CANCELED);
    }

    @Test
    public void testHasActiveBillingWithActiveStatus() {
        // Act
        boolean hasActive = billingAccessControlService.hasActiveBilling(activeTenant.getId());

        // Assert
        assertThat(hasActive).isTrue();
    }

    @Test
    public void testHasActiveBillingWithPendingPayment() {
        // Act
        boolean hasActive = billingAccessControlService.hasActiveBilling(pendingPaymentTenant.getId());

        // Assert
        assertThat(hasActive).isFalse();
    }

    @Test
    public void testHasActiveBillingWithPastDue() {
        // Act
        boolean hasActive = billingAccessControlService.hasActiveBilling(pastDueTenant.getId());

        // Assert
        assertThat(hasActive).isFalse();
    }

    @Test
    public void testHasActiveBillingWithCanceled() {
        // Act
        boolean hasActive = billingAccessControlService.hasActiveBilling(canceledTenant.getId());

        // Assert
        assertThat(hasActive).isFalse();
    }

    @Test
    public void testGetBillingStatusForActiveTenant() {
        // Act
        BillingStatus status = billingAccessControlService.getBillingStatus(activeTenant.getId());

        // Assert
        assertThat(status).isEqualTo(BillingStatus.ACTIVE);
    }

    @Test
    public void testGetBillingStatusForPendingPaymentTenant() {
        // Act
        BillingStatus status = billingAccessControlService.getBillingStatus(pendingPaymentTenant.getId());

        // Assert
        assertThat(status).isEqualTo(BillingStatus.PENDING_PAYMENT);
    }

    @Test
    public void testGetBillingStatusForPastDueTenant() {
        // Act
        BillingStatus status = billingAccessControlService.getBillingStatus(pastDueTenant.getId());

        // Assert
        assertThat(status).isEqualTo(BillingStatus.PAST_DUE);
    }

    @Test
    public void testGetBillingStatusForCanceledTenant() {
        // Act
        BillingStatus status = billingAccessControlService.getBillingStatus(canceledTenant.getId());

        // Assert
        assertThat(status).isEqualTo(BillingStatus.CANCELED);
    }

    @Test
    public void testGetBillingStatusForNonExistentTenant() {
        // Act
        BillingStatus status = billingAccessControlService.getBillingStatus(99999L);

        // Assert - Should return PENDING_PAYMENT as default
        assertThat(status).isEqualTo(BillingStatus.PENDING_PAYMENT);
    }

    @Test
    public void testCanAccessTenantAdminWithActiveBilling() {
        // Act
        boolean canAccess = billingAccessControlService.canAccessTenantAdmin(1L, activeTenant.getId());

        // Assert
        assertThat(canAccess).isTrue();
    }

    @Test
    public void testCanAccessTenantAdminWithPendingPayment() {
        // Act
        boolean canAccess = billingAccessControlService.canAccessTenantAdmin(1L, pendingPaymentTenant.getId());

        // Assert
        assertThat(canAccess).isFalse();
    }

    @Test
    public void testCanAccessTenantAdminWithPastDue() {
        // Act
        boolean canAccess = billingAccessControlService.canAccessTenantAdmin(1L, pastDueTenant.getId());

        // Assert
        assertThat(canAccess).isFalse();
    }

    @Test
    public void testCanAccessTenantAdminWithCanceled() {
        // Act
        boolean canAccess = billingAccessControlService.canAccessTenantAdmin(1L, canceledTenant.getId());

        // Assert
        assertThat(canAccess).isFalse();
    }

    @Test
    public void testBillingStatusIsCached() {
        // First call - should hit database
        BillingStatus status1 = billingAccessControlService.getBillingStatus(activeTenant.getId());
        assertThat(status1).isEqualTo(BillingStatus.ACTIVE);

        // Change billing status in database
        activeTenant.setBillingStatus(BillingStatus.CANCELED);
        tenantRepository.saveAndFlush(activeTenant);

        // Second call - should return cached value (still ACTIVE)
        BillingStatus status2 = billingAccessControlService.getBillingStatus(activeTenant.getId());
        assertThat(status2).isEqualTo(BillingStatus.ACTIVE);

        // Clear cache
        var cache = cacheManager.getCache("billingStatus");
        if (cache != null) {
            cache.clear();
        }

        // Third call - should hit database and return new value
        BillingStatus status3 = billingAccessControlService.getBillingStatus(activeTenant.getId());
        assertThat(status3).isEqualTo(BillingStatus.CANCELED);
    }

    // Helper method

    private TenantEntity createTenant(String slug, String name, BillingStatus billingStatus) {
        TenantEntity tenant = new TenantEntity(slug, name);
        tenant.setBillingStatus(billingStatus);
        return tenantRepository.save(tenant);
    }
}
