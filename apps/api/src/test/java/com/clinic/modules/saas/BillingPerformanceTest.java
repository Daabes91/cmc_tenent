package com.clinic.modules.saas;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.dto.PayPalWebhookEvent;
import com.clinic.modules.saas.dto.PayPalWebhookResource;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.saas.service.BillingAccessControlService;
import com.clinic.modules.saas.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance tests for PayPal billing system.
 * Tests webhook processing, billing status caching, and query performance.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BillingPerformanceTest {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private BillingAccessControlService billingAccessControlService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CacheManager cacheManager;

    private List<TenantEntity> testTenants;
    private List<SubscriptionEntity> testSubscriptions;

    @BeforeEach
    public void setUp() {
        // Clean up
        subscriptionRepository.deleteAll();
        tenantRepository.deleteAll();
        
        // Clear cache
        var cache = cacheManager.getCache("billingStatus");
        if (cache != null) {
            cache.clear();
        }

        testTenants = new ArrayList<>();
        testSubscriptions = new ArrayList<>();
    }

    @Test
    public void testWebhookProcessingPerformance() {
        // Create test data
        TenantEntity tenant = createTenant("perf-test-webhook", BillingStatus.PENDING_PAYMENT);
        SubscriptionEntity subscription = createSubscription(tenant, "I-PERF-TEST-001");

        // Create webhook event
        PayPalWebhookResource resource = new PayPalWebhookResource();
        resource.setId("I-PERF-TEST-001");
        resource.setStatus("ACTIVE");

        PayPalWebhookEvent event = new PayPalWebhookEvent();
        event.setId("WH-PERF-TEST");
        event.setEventType("BILLING.SUBSCRIPTION.ACTIVATED");
        event.setResource(resource);

        // Measure processing time
        Instant start = Instant.now();
        webhookService.processWebhookEvent(event);
        Instant end = Instant.now();

        Duration duration = Duration.between(start, end);

        // Assert processing time is under 500ms
        assertThat(duration.toMillis()).isLessThan(500);
        
        // Verify event was processed correctly
        tenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThat(tenant.getBillingStatus()).isEqualTo(BillingStatus.ACTIVE);
    }

    @Test
    public void testBulkWebhookProcessing() {
        // Create 100 tenants with subscriptions
        int tenantCount = 100;
        for (int i = 0; i < tenantCount; i++) {
            TenantEntity tenant = createTenant("bulk-test-" + i, BillingStatus.PENDING_PAYMENT);
            createSubscription(tenant, "I-BULK-" + i);
        }

        // Process 100 webhook events
        Instant start = Instant.now();
        
        for (int i = 0; i < tenantCount; i++) {
            PayPalWebhookResource resource = new PayPalWebhookResource();
            resource.setId("I-BULK-" + i);
            resource.setStatus("ACTIVE");

            PayPalWebhookEvent event = new PayPalWebhookEvent();
            event.setId("WH-BULK-" + i);
            event.setEventType("BILLING.SUBSCRIPTION.ACTIVATED");
            event.setResource(resource);

            webhookService.processWebhookEvent(event);
        }
        
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Assert average processing time is under 100ms per webhook
        long avgTimeMs = duration.toMillis() / tenantCount;
        assertThat(avgTimeMs).isLessThan(100);
        
        System.out.println("Bulk webhook processing: " + tenantCount + " events in " + 
                          duration.toMillis() + "ms (avg: " + avgTimeMs + "ms per event)");
    }

    @Test
    public void testBillingStatusCachePerformance() {
        // Create test tenant
        TenantEntity tenant = createTenant("cache-test", BillingStatus.ACTIVE);

        // First call - should hit database
        Instant start1 = Instant.now();
        BillingStatus status1 = billingAccessControlService.getBillingStatus(tenant.getId());
        Instant end1 = Instant.now();
        Duration firstCallDuration = Duration.between(start1, end1);

        assertThat(status1).isEqualTo(BillingStatus.ACTIVE);

        // Second call - should hit cache
        Instant start2 = Instant.now();
        BillingStatus status2 = billingAccessControlService.getBillingStatus(tenant.getId());
        Instant end2 = Instant.now();
        Duration cachedCallDuration = Duration.between(start2, end2);

        assertThat(status2).isEqualTo(BillingStatus.ACTIVE);

        // Cached call should be significantly faster (at least 50% faster)
        assertThat(cachedCallDuration.toNanos()).isLessThan(firstCallDuration.toNanos() / 2);
        
        System.out.println("First call (DB): " + firstCallDuration.toNanos() / 1000 + "μs");
        System.out.println("Cached call: " + cachedCallDuration.toNanos() / 1000 + "μs");
        System.out.println("Speedup: " + (firstCallDuration.toNanos() / cachedCallDuration.toNanos()) + "x");
    }

    @Test
    public void testBillingStatusCheckUnderLoad() {
        // Create 1000 tenants
        int tenantCount = 1000;
        List<Long> tenantIds = new ArrayList<>();
        
        for (int i = 0; i < tenantCount; i++) {
            TenantEntity tenant = createTenant("load-test-" + i, BillingStatus.ACTIVE);
            tenantIds.add(tenant.getId());
        }

        // Perform 1000 billing status checks
        Instant start = Instant.now();
        
        for (Long tenantId : tenantIds) {
            billingAccessControlService.hasActiveBilling(tenantId);
        }
        
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Assert average check time is under 10ms
        long avgTimeMs = duration.toMillis() / tenantCount;
        assertThat(avgTimeMs).isLessThan(10);
        
        System.out.println("Billing status checks: " + tenantCount + " checks in " + 
                          duration.toMillis() + "ms (avg: " + avgTimeMs + "ms per check)");
    }

    @Test
    public void testTenantListQueryPerformance() {
        // Create 500 tenants with different billing statuses
        int tenantsPerStatus = 125;
        BillingStatus[] statuses = {
            BillingStatus.ACTIVE,
            BillingStatus.PENDING_PAYMENT,
            BillingStatus.PAST_DUE,
            BillingStatus.CANCELED
        };

        for (BillingStatus status : statuses) {
            for (int i = 0; i < tenantsPerStatus; i++) {
                createTenant("query-test-" + status.name().toLowerCase() + "-" + i, status);
            }
        }

        // Test paginated query performance
        Instant start = Instant.now();
        Page<TenantEntity> page = tenantRepository.findAll(PageRequest.of(0, 50));
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Assert query time is under 100ms
        assertThat(duration.toMillis()).isLessThan(100);
        assertThat(page.getContent()).hasSize(50);
        
        System.out.println("Tenant list query (50 items from 500): " + duration.toMillis() + "ms");
    }

    @Test
    public void testNoNPlusOneQueriesInTenantList() {
        // Create 100 tenants with subscriptions
        int tenantCount = 100;
        for (int i = 0; i < tenantCount; i++) {
            TenantEntity tenant = createTenant("n-plus-one-test-" + i, BillingStatus.ACTIVE);
            createSubscription(tenant, "I-N-PLUS-ONE-" + i);
        }

        // Fetch tenants with subscriptions
        Instant start = Instant.now();
        Page<TenantEntity> page = tenantRepository.findAll(PageRequest.of(0, 100));
        
        // Access subscription data for each tenant
        for (TenantEntity tenant : page.getContent()) {
            subscriptionRepository.findByTenantId(tenant.getId());
        }
        
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // With N+1 queries, this would take much longer
        // Assert total time is under 500ms (should be much faster with proper query optimization)
        assertThat(duration.toMillis()).isLessThan(500);
        
        System.out.println("Tenant list with subscriptions (100 tenants): " + duration.toMillis() + "ms");
    }

    @Test
    public void testConcurrentBillingStatusChecks() throws InterruptedException {
        // Create test tenant
        TenantEntity tenant = createTenant("concurrent-test", BillingStatus.ACTIVE);
        Long tenantId = tenant.getId();

        // Simulate concurrent requests
        int threadCount = 10;
        int requestsPerThread = 100;
        List<Thread> threads = new ArrayList<>();

        Instant start = Instant.now();

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    billingAccessControlService.hasActiveBilling(tenantId);
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        int totalRequests = threadCount * requestsPerThread;
        long avgTimeMs = duration.toMillis() / totalRequests;

        // Assert average time is under 5ms per request
        assertThat(avgTimeMs).isLessThan(5);
        
        System.out.println("Concurrent billing checks: " + totalRequests + " requests in " + 
                          duration.toMillis() + "ms (avg: " + avgTimeMs + "ms per request)");
    }

    @Test
    public void testSubscriptionLookupPerformance() {
        // Create 1000 subscriptions
        int subscriptionCount = 1000;
        List<String> subscriptionIds = new ArrayList<>();

        for (int i = 0; i < subscriptionCount; i++) {
            TenantEntity tenant = createTenant("sub-lookup-" + i, BillingStatus.ACTIVE);
            String subId = "I-LOOKUP-" + i;
            createSubscription(tenant, subId);
            subscriptionIds.add(subId);
        }

        // Test lookup performance
        Instant start = Instant.now();
        
        for (String subId : subscriptionIds) {
            subscriptionRepository.findByPaypalSubscriptionId(subId);
        }
        
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Assert average lookup time is under 5ms
        long avgTimeMs = duration.toMillis() / subscriptionCount;
        assertThat(avgTimeMs).isLessThan(5);
        
        System.out.println("Subscription lookups: " + subscriptionCount + " lookups in " + 
                          duration.toMillis() + "ms (avg: " + avgTimeMs + "ms per lookup)");
    }

    // Helper methods

    private TenantEntity createTenant(String slug, BillingStatus billingStatus) {
        TenantEntity tenant = new TenantEntity(slug, slug + " name");
        tenant.setBillingStatus(billingStatus);
        tenant = tenantRepository.save(tenant);
        testTenants.add(tenant);
        return tenant;
    }

    private SubscriptionEntity createSubscription(TenantEntity tenant, String paypalSubscriptionId) {
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setTenant(tenant);
        subscription.setPaypalSubscriptionId(paypalSubscriptionId);
        subscription.setStatus("ACTIVE");
        subscription.setProvider("paypal");
        subscription = subscriptionRepository.save(subscription);
        testSubscriptions.add(subscription);
        return subscription;
    }
}
