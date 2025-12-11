package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.ecommerce.exception.EcommerceFeatureDisabledException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for EcommerceFeatureService.
 * 
 * This test verifies the basic functionality of the e-commerce feature flag system.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EcommerceFeatureServiceTest {

    @Autowired
    private EcommerceFeatureService ecommerceFeatureService;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    public void testEcommerceFeatureDisabledByDefault() {
        // Create a test tenant
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        tenant = tenantRepository.save(tenant);

        // E-commerce should be disabled by default
        assertFalse(ecommerceFeatureService.isEcommerceEnabled(tenant.getId()));
    }

    @Test
    public void testEnableEcommerce() {
        // Create a test tenant
        TenantEntity tenant = new TenantEntity("test-tenant-2", "Test Tenant 2");
        tenant = tenantRepository.save(tenant);

        // Enable e-commerce
        ecommerceFeatureService.enableEcommerce(tenant.getId());

        // E-commerce should now be enabled
        assertTrue(ecommerceFeatureService.isEcommerceEnabled(tenant.getId()));
    }

    @Test
    public void testDisableEcommerce() {
        // Create a test tenant
        TenantEntity tenant = new TenantEntity("test-tenant-3", "Test Tenant 3");
        tenant.setEcommerceEnabled(true);
        tenant = tenantRepository.save(tenant);

        // Disable e-commerce
        ecommerceFeatureService.disableEcommerce(tenant.getId());

        // E-commerce should now be disabled
        assertFalse(ecommerceFeatureService.isEcommerceEnabled(tenant.getId()));
    }

    @Test
    public void testValidateEcommerceEnabledThrowsException() {
        // Create a test tenant with e-commerce disabled
        TenantEntity tenant = new TenantEntity("test-tenant-4", "Test Tenant 4");
        TenantEntity savedTenant = tenantRepository.save(tenant);

        // Validation should throw exception
        assertThrows(EcommerceFeatureDisabledException.class, () -> {
            ecommerceFeatureService.validateEcommerceEnabled(savedTenant.getId());
        });
    }

    @Test
    public void testValidateEcommerceEnabledPasses() {
        // Create a test tenant with e-commerce enabled
        TenantEntity tenant = new TenantEntity("test-tenant-5", "Test Tenant 5");
        tenant.setEcommerceEnabled(true);
        TenantEntity savedTenant = tenantRepository.save(tenant);

        // Validation should pass without exception
        assertDoesNotThrow(() -> {
            ecommerceFeatureService.validateEcommerceEnabled(savedTenant.getId());
        });
    }

    @Test
    public void testNonExistentTenant() {
        // Non-existent tenant should return false
        assertFalse(ecommerceFeatureService.isEcommerceEnabled(99999L));
    }
}