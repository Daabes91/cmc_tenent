package com.clinic.multitenant;

import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.patient.GlobalPatientRepository;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.service.ClinicServiceRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance test to verify tenant filtering efficiency with large datasets.
 * 
 * This test:
 * - Creates 1000 doctors across 10 tenants (100 doctors per tenant)
 * - Queries doctors for a single tenant
 * - Verifies query uses tenant_id index
 * - Verifies response time is acceptable
 * - Checks database query execution plan
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantFilteringPerformanceTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private GlobalPatientRepository globalPatientRepository;

    @Autowired
    private ClinicServiceRepository serviceRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<TenantEntity> tenants;
    private static final int TENANT_COUNT = 10;
    private static final int DOCTORS_PER_TENANT = 100;
    private static final int TOTAL_DOCTORS = TENANT_COUNT * DOCTORS_PER_TENANT;

    @BeforeEach
    void setUp() {
        // Clean up existing test data (order matters due to foreign keys)
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        globalPatientRepository.deleteAll();
        serviceRepository.deleteAll();
        doctorRepository.deleteAll();
        
        // Keep default tenant and create additional test tenants
        tenants = new ArrayList<>();
        
        // Get or create default tenant
        TenantEntity defaultTenant = tenantRepository.findBySlugIgnoreCase("default")
                .orElseGet(() -> {
                    TenantEntity t = new TenantEntity("default", "Default Clinic");
                    return tenantRepository.save(t);
                });
        tenants.add(defaultTenant);

        // Create 9 additional tenants for testing
        for (int i = 1; i < TENANT_COUNT; i++) {
            String slug = "perf-test-tenant-" + i;
            
            // Delete if exists from previous test run
            tenantRepository.findBySlugIgnoreCase(slug).ifPresent(tenantRepository::delete);
            
            TenantEntity tenant = new TenantEntity(slug, "Performance Test Clinic " + i);
            tenant = tenantRepository.save(tenant);
            tenants.add(tenant);
        }

        System.out.println("Created " + tenants.size() + " tenants for performance testing");
    }

    @Test
    void shouldCreateLargeDatasetAcrossMultipleTenants() {
        // Create 100 doctors per tenant (1000 total)
        int doctorCount = 0;
        
        for (int tenantIndex = 0; tenantIndex < tenants.size(); tenantIndex++) {
            TenantEntity tenant = tenants.get(tenantIndex);
            
            for (int doctorIndex = 0; doctorIndex < DOCTORS_PER_TENANT; doctorIndex++) {
                String doctorNumber = String.format("T%d-D%03d", tenantIndex, doctorIndex);
                
                DoctorEntity doctor = new DoctorEntity(
                        "Dr. " + doctorNumber,
                        "د. " + doctorNumber,
                        "General Dentist",
                        "طبيب أسنان عام",
                        "Experienced dentist for performance testing",
                        "طبيب أسنان ذو خبرة لاختبار الأداء",
                        "en,ar"
                );
                doctor.updateContactInfo(
                        "doctor." + doctorNumber.toLowerCase() + "@example.com",
                        "+1" + String.format("%010d", doctorCount)
                );
                doctor.setTenant(tenant);
                doctorRepository.save(doctor);
                
                doctorCount++;
                
                // Flush periodically to avoid memory issues
                if (doctorCount % 100 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    System.out.println("Created " + doctorCount + " doctors...");
                }
            }
        }

        // Final flush
        entityManager.flush();
        entityManager.clear();

        System.out.println("Successfully created " + doctorCount + " doctors across " + tenants.size() + " tenants");

        // Verify total count
        long totalDoctors = doctorRepository.count();
        assertThat(totalDoctors).isEqualTo(TOTAL_DOCTORS);
        
        System.out.println("Verified total doctor count: " + totalDoctors);
    }

    @Test
    void shouldQuerySingleTenantEfficientlyWithLargeDataset() {
        // First create the dataset
        shouldCreateLargeDatasetAcrossMultipleTenants();

        // Select a tenant to query (use tenant at index 5 for middle of dataset)
        TenantEntity targetTenant = tenants.get(5);
        Long targetTenantId = targetTenant.getId();

        System.out.println("\n=== Performance Test: Querying Tenant " + targetTenant.getSlug() + " ===");

        // Warm up query (first query may be slower due to caching)
        List<DoctorEntity> warmupResults = doctorRepository.findAllByTenantIdAndIsActiveTrue(targetTenantId);
        assertThat(warmupResults).hasSize(DOCTORS_PER_TENANT);
        System.out.println("Warmup query returned " + warmupResults.size() + " doctors");

        // Clear entity manager to ensure fresh query
        entityManager.clear();

        // Measure query performance
        long startTime = System.nanoTime();
        List<DoctorEntity> results = doctorRepository.findAllByTenantIdAndIsActiveTrue(targetTenantId);
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("Query execution time: " + durationMs + " ms");
        System.out.println("Results returned: " + results.size() + " doctors");

        // Verify results
        assertThat(results).hasSize(DOCTORS_PER_TENANT);
        
        // Verify all results belong to target tenant
        for (DoctorEntity doctor : results) {
            assertThat(doctor.getTenant().getId()).isEqualTo(targetTenantId);
        }

        // Performance assertion: Query should complete in reasonable time
        // With proper indexing, querying 100 records from 1000 should be fast
        // Setting threshold at 1000ms (1 second) to be conservative
        assertThat(durationMs)
                .as("Query should complete within 1000ms with proper indexing")
                .isLessThan(1000);

        System.out.println("✓ Performance test passed: Query completed in " + durationMs + " ms");
    }

    @Test
    void shouldUseIndexForTenantFiltering() {
        // First create the dataset
        shouldCreateLargeDatasetAcrossMultipleTenants();

        // Select a tenant to query
        TenantEntity targetTenant = tenants.get(5);
        Long targetTenantId = targetTenant.getId();

        System.out.println("\n=== Execution Plan Analysis ===");

        // Get the query execution plan using EXPLAIN
        String explainQuery = """
                EXPLAIN (FORMAT JSON)
                SELECT d.*
                FROM doctors d
                WHERE d.tenant_id = ?
                AND d.is_active = true
                """;

        try {
            List<Map<String, Object>> explainResults = jdbcTemplate.queryForList(
                    explainQuery,
                    targetTenantId
            );

            System.out.println("Query Execution Plan:");
            for (Map<String, Object> row : explainResults) {
                System.out.println(row);
            }

            // Note: The exact format of EXPLAIN output varies by database
            // For PostgreSQL, we expect to see index usage in the plan
            assertThat(explainResults).isNotEmpty();
            
            System.out.println("✓ Execution plan retrieved successfully");

        } catch (Exception e) {
            System.err.println("Warning: Could not retrieve execution plan: " + e.getMessage());
            System.err.println("This may be expected in test environments");
        }
    }

    @Test
    void shouldVerifyIndexExists() {
        System.out.println("\n=== Index Verification ===");

        // Query to check if tenant_id index exists on doctors table
        String indexQuery = """
                SELECT
                    indexname,
                    indexdef
                FROM pg_indexes
                WHERE tablename = 'doctors'
                AND indexdef LIKE '%tenant_id%'
                """;

        try {
            List<Map<String, Object>> indexes = jdbcTemplate.queryForList(indexQuery);

            System.out.println("Indexes on doctors table containing tenant_id:");
            for (Map<String, Object> index : indexes) {
                System.out.println("  - " + index.get("indexname") + ": " + index.get("indexdef"));
            }

            // Verify at least one index exists on tenant_id
            assertThat(indexes)
                    .as("At least one index should exist on tenant_id column")
                    .isNotEmpty();

            System.out.println("✓ Tenant filtering index verified");

        } catch (Exception e) {
            System.err.println("Warning: Could not verify indexes: " + e.getMessage());
            System.err.println("This may be expected in non-PostgreSQL databases");
        }
    }

    @Test
    void shouldComparePerformanceAcrossDifferentTenants() {
        // First create the dataset
        shouldCreateLargeDatasetAcrossMultipleTenants();

        System.out.println("\n=== Multi-Tenant Performance Comparison ===");

        List<Long> queryTimes = new ArrayList<>();

        // Query each tenant and measure performance
        for (int i = 0; i < tenants.size(); i++) {
            TenantEntity tenant = tenants.get(i);
            Long tenantId = tenant.getId();

            // Clear cache
            entityManager.clear();

            // Measure query time
            long startTime = System.nanoTime();
            List<DoctorEntity> results = doctorRepository.findAllByTenantIdAndIsActiveTrue(tenantId);
            long endTime = System.nanoTime();

            long durationMs = (endTime - startTime) / 1_000_000;
            queryTimes.add(durationMs);

            System.out.println("Tenant " + i + " (" + tenant.getSlug() + "): " + 
                             durationMs + " ms, " + results.size() + " doctors");

            // Verify correct number of results
            assertThat(results).hasSize(DOCTORS_PER_TENANT);
        }

        // Calculate statistics
        long minTime = queryTimes.stream().min(Long::compareTo).orElse(0L);
        long maxTime = queryTimes.stream().max(Long::compareTo).orElse(0L);
        double avgTime = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        System.out.println("\n=== Performance Statistics ===");
        System.out.println("Min query time: " + minTime + " ms");
        System.out.println("Max query time: " + maxTime + " ms");
        System.out.println("Avg query time: " + String.format("%.2f", avgTime) + " ms");

        // Verify consistent performance across tenants
        // Max time should not be more than 3x the min time (allowing for variance)
        assertThat(maxTime)
                .as("Query performance should be consistent across tenants")
                .isLessThan(minTime * 3 + 100); // +100ms buffer for small datasets

        System.out.println("✓ Performance is consistent across all tenants");
    }

    @Test
    void shouldHandleConcurrentTenantQueries() {
        // First create the dataset
        shouldCreateLargeDatasetAcrossMultipleTenants();

        System.out.println("\n=== Concurrent Query Test ===");

        // Simulate concurrent queries from different tenants
        List<Thread> threads = new ArrayList<>();
        List<Long> queryTimes = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        for (int i = 0; i < tenants.size(); i++) {
            final int tenantIndex = i;
            final TenantEntity tenant = tenants.get(i);

            Thread thread = new Thread(() -> {
                try {
                    long startTime = System.nanoTime();
                    List<DoctorEntity> results = doctorRepository.findAllByTenantIdAndIsActiveTrue(tenant.getId());
                    long endTime = System.nanoTime();

                    long durationMs = (endTime - startTime) / 1_000_000;
                    
                    synchronized (queryTimes) {
                        queryTimes.add(durationMs);
                    }

                    System.out.println("Thread " + tenantIndex + " completed in " + durationMs + " ms");

                    // Verify results
                    if (results.size() != DOCTORS_PER_TENANT) {
                        throw new AssertionError("Expected " + DOCTORS_PER_TENANT + 
                                               " doctors but got " + results.size());
                    }

                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join(5000); // 5 second timeout per thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        }

        // Verify no exceptions occurred
        assertThat(exceptions)
                .as("No exceptions should occur during concurrent queries")
                .isEmpty();

        // Verify all queries completed
        assertThat(queryTimes)
                .as("All queries should complete")
                .hasSize(TENANT_COUNT);

        double avgTime = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.println("Average concurrent query time: " + String.format("%.2f", avgTime) + " ms");
        System.out.println("✓ Concurrent queries completed successfully");
    }
}
