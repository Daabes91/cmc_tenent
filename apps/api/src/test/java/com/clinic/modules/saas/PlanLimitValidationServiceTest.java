package com.clinic.modules.saas;

import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.saas.config.PlanTierConfig;
import com.clinic.modules.saas.exception.PlanLimitExceededException;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.saas.service.PlanLimitValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanLimitValidationServiceTest {
    
    @Mock
    private SubscriptionRepository subscriptionRepository;
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private TenantService tenantService;
    
    private PlanTierConfig planTierConfig;
    private PlanLimitValidationService service;
    
    @BeforeEach
    void setUp() {
        planTierConfig = new PlanTierConfig();
        service = new PlanLimitValidationService(
                subscriptionRepository,
                patientRepository,
                planTierConfig,
                tenantService
        );
    }
    
    @Test
    void validatePatientLimit_shouldAllowWhenUnderLimit() {
        // Given
        Long tenantId = 1L;
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setPlanTier(PlanTier.BASIC);
        subscription.setStatus("ACTIVE");
        
        when(tenantService.requireTenant(tenantId)).thenReturn(tenant);
        when(subscriptionRepository.findByTenantId(tenantId))
                .thenReturn(Optional.of(subscription));
        when(patientRepository.countByTenantId(tenantId)).thenReturn(50L);
        
        // When/Then - should not throw
        service.validatePatientLimit(tenantId);
    }
    
    @Test
    void validatePatientLimit_shouldThrowWhenLimitExceeded() {
        // Given
        Long tenantId = 1L;
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setPlanTier(PlanTier.BASIC);
        subscription.setStatus("ACTIVE");
        
        when(tenantService.requireTenant(tenantId)).thenReturn(tenant);
        when(subscriptionRepository.findByTenantId(tenantId))
                .thenReturn(Optional.of(subscription));
        when(patientRepository.countByTenantId(tenantId)).thenReturn(100L); // At limit
        
        // When/Then
        assertThatThrownBy(() -> service.validatePatientLimit(tenantId))
                .isInstanceOf(PlanLimitExceededException.class)
                .hasMessageContaining("patients")
                .hasMessageContaining("100");
    }
    
    @Test
    void validatePatientLimit_shouldAllowUnlimitedForEnterprise() {
        // Given
        Long tenantId = 1L;
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setPlanTier(PlanTier.ENTERPRISE);
        subscription.setStatus("ACTIVE");
        
        when(tenantService.requireTenant(tenantId)).thenReturn(tenant);
        when(subscriptionRepository.findByTenantId(tenantId))
                .thenReturn(Optional.of(subscription));
        when(patientRepository.countByTenantId(tenantId)).thenReturn(10000L); // Way over basic limit
        
        // When/Then - should not throw
        service.validatePatientLimit(tenantId);
    }
    
    @Test
    void validatePatientLimit_shouldAllowWhenNoSubscription() {
        // Given
        Long tenantId = 1L;
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        
        when(tenantService.requireTenant(tenantId)).thenReturn(tenant);
        when(subscriptionRepository.findByTenantId(tenantId))
                .thenReturn(Optional.empty());
        
        // When/Then - should not throw (backwards compatibility)
        service.validatePatientLimit(tenantId);
    }
    
    @Test
    void getUsageStats_shouldReturnCorrectStats() {
        // Given
        Long tenantId = 1L;
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setPlanTier(PlanTier.PROFESSIONAL);
        subscription.setStatus("ACTIVE");
        
        when(tenantService.requireTenant(tenantId)).thenReturn(tenant);
        when(subscriptionRepository.findByTenantId(tenantId))
                .thenReturn(Optional.of(subscription));
        when(patientRepository.countByTenantId(tenantId)).thenReturn(250L);
        
        // When
        PlanLimitValidationService.PlanUsageStats stats = service.getUsageStats(tenantId);
        
        // Then
        assertThat(stats.getCurrentPatients()).isEqualTo(250);
        assertThat(stats.getMaxPatients()).isEqualTo(500);
        assertThat(stats.getPlanTier()).isEqualTo(PlanTier.PROFESSIONAL);
        assertThat(stats.isPatientLimitReached()).isFalse();
    }
    
    @Test
    void getUsageStats_shouldIndicateLimitReached() {
        // Given
        Long tenantId = 1L;
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setPlanTier(PlanTier.BASIC);
        subscription.setStatus("ACTIVE");
        
        when(tenantService.requireTenant(tenantId)).thenReturn(tenant);
        when(subscriptionRepository.findByTenantId(tenantId))
                .thenReturn(Optional.of(subscription));
        when(patientRepository.countByTenantId(tenantId)).thenReturn(100L);
        
        // When
        PlanLimitValidationService.PlanUsageStats stats = service.getUsageStats(tenantId);
        
        // Then
        assertThat(stats.getCurrentPatients()).isEqualTo(100);
        assertThat(stats.getMaxPatients()).isEqualTo(100);
        assertThat(stats.isPatientLimitReached()).isTrue();
    }
}
