/**
 * Manual Test Suite for Web App Subdomain Routing
 * 
 * This file contains test scenarios to verify that:
 * 1. Subdomain routing correctly sets tenant cookies
 * 2. API requests include the correct X-Tenant-Slug header
 * 3. Tenant context switches properly between subdomains
 * 
 * Requirements: 6.1
 */

// Test Configuration
const TEST_CONFIG = {
  // Test tenants
  tenants: {
    tenantA: {
      slug: 'tenant-a',
      subdomain: 'tenant-a.localhost:3001',
      expectedCookie: 'tenant-a',
      expectedHeader: 'tenant-a'
    },
    tenantB: {
      slug: 'tenant-b',
      subdomain: 'tenant-b.localhost:3001',
      expectedCookie: 'tenant-b',
      expectedHeader: 'tenant-b'
    }
  },
  
  // Cookie configuration
  cookieName: 'tenantSlug',
  headerName: 'x-tenant-slug',
  
  // Test endpoints
  testEndpoints: [
    '/public/services',
    '/public/doctors',
    '/public/settings',
    '/public/blogs'
  ]
};

/**
 * Test Scenario 1: Access tenant-a.localhost and verify tenant cookie
 * Requirement: 6.1 - Verify tenant cookie set to "tenant-a"
 */
function testTenantACookieSet() {
  console.log("🧪 Testing Tenant A Cookie Set");
  
  const testSteps = [
    {
      step: 1,
      description: "Navigate to tenant-a.localhost:3001",
      action: "Open browser and navigate to http://tenant-a.localhost:3001",
      expectedResult: "Page loads successfully"
    },
    {
      step: 2,
      description: "Check tenant cookie is set",
      action: "Open DevTools > Application > Cookies > tenant-a.localhost",
      expectedResult: "Cookie 'tenantSlug' exists with value 'tenant-a'"
    },
    {
      step: 3,
      description: "Verify cookie attributes",
      action: "Inspect cookie properties",
      expectedResult: {
        name: "tenantSlug",
        value: "tenant-a",
        path: "/",
        sameSite: "Lax"
      }
    },
    {
      step: 4,
      description: "Navigate to different pages within tenant-a",
      action: "Navigate to /en/services, /en/doctors, /en/blog",
      expectedResult: "Cookie persists with value 'tenant-a' across all pages"
    }
  ];
  
  return {
    testName: "Tenant A Cookie Set",
    requirement: "6.1",
    steps: testSteps,
    validationCriteria: [
      "Cookie 'tenantSlug' is set on initial page load",
      "Cookie value is 'tenant-a'",
      "Cookie persists across page navigation",
      "Cookie attributes are correct (path: /, sameSite: Lax)"
    ]
  };
}

/**
 * Test Scenario 2: Verify API requests include X-Tenant-Slug: tenant-a
 * Requirement: 6.1 - Verify API requests include X-Tenant-Slug: tenant-a
 */
function testTenantAAPIHeaders() {
  console.log("🧪 Testing Tenant A API Headers");
  
  const testSteps = [
    {
      step: 1,
      description: "Navigate to tenant-a.localhost:3001",
      action: "Open browser and navigate to http://tenant-a.localhost:3001",
      expectedResult: "Page loads successfully"
    },
    {
      step: 2,
      description: "Open Network tab in DevTools",
      action: "Open DevTools > Network tab and filter for API calls",
      expectedResult: "Network tab is ready to capture requests"
    },
    {
      step: 3,
      description: "Trigger API requests",
      action: "Navigate to pages that fetch data (services, doctors, blog)",
      expectedResult: "API requests are visible in Network tab"
    },
    {
      step: 4,
      description: "Inspect request headers",
      action: "Click on API request > Headers tab > Request Headers",
      expectedResult: "Header 'x-tenant-slug: tenant-a' is present in all API requests"
    },
    {
      step: 5,
      description: "Test multiple API endpoints",
      action: "Check headers for /public/services, /public/doctors, /public/settings",
      expectedResult: "All API requests include 'x-tenant-slug: tenant-a'"
    }
  ];
  
  return {
    testName: "Tenant A API Headers",
    requirement: "6.1",
    steps: testSteps,
    validationCriteria: [
      "All API requests include X-Tenant-Slug header",
      "Header value is 'tenant-a'",
      "Header is present for all public endpoints",
      "Header is present for authenticated endpoints (if logged in)"
    ]
  };
}

/**
 * Test Scenario 3: Access tenant-b.localhost and verify tenant cookie
 * Requirement: 6.1 - Verify tenant cookie updated to "tenant-b"
 */
function testTenantBCookieSet() {
  console.log("🧪 Testing Tenant B Cookie Set");
  
  const testSteps = [
    {
      step: 1,
      description: "Navigate to tenant-b.localhost:3001",
      action: "Open browser and navigate to http://tenant-b.localhost:3001",
      expectedResult: "Page loads successfully"
    },
    {
      step: 2,
      description: "Check tenant cookie is updated",
      action: "Open DevTools > Application > Cookies > tenant-b.localhost",
      expectedResult: "Cookie 'tenantSlug' exists with value 'tenant-b'"
    },
    {
      step: 3,
      description: "Verify cookie attributes",
      action: "Inspect cookie properties",
      expectedResult: {
        name: "tenantSlug",
        value: "tenant-b",
        path: "/",
        sameSite: "Lax"
      }
    },
    {
      step: 4,
      description: "Navigate to different pages within tenant-b",
      action: "Navigate to /en/services, /en/doctors, /en/blog",
      expectedResult: "Cookie persists with value 'tenant-b' across all pages"
    }
  ];
  
  return {
    testName: "Tenant B Cookie Set",
    requirement: "6.1",
    steps: testSteps,
    validationCriteria: [
      "Cookie 'tenantSlug' is set on initial page load",
      "Cookie value is 'tenant-b'",
      "Cookie persists across page navigation",
      "Cookie attributes are correct (path: /, sameSite: Lax)"
    ]
  };
}

/**
 * Test Scenario 4: Verify API requests include X-Tenant-Slug: tenant-b
 * Requirement: 6.1 - Verify API requests include X-Tenant-Slug: tenant-b
 */
function testTenantBAPIHeaders() {
  console.log("🧪 Testing Tenant B API Headers");
  
  const testSteps = [
    {
      step: 1,
      description: "Navigate to tenant-b.localhost:3001",
      action: "Open browser and navigate to http://tenant-b.localhost:3001",
      expectedResult: "Page loads successfully"
    },
    {
      step: 2,
      description: "Open Network tab in DevTools",
      action: "Open DevTools > Network tab and filter for API calls",
      expectedResult: "Network tab is ready to capture requests"
    },
    {
      step: 3,
      description: "Trigger API requests",
      action: "Navigate to pages that fetch data (services, doctors, blog)",
      expectedResult: "API requests are visible in Network tab"
    },
    {
      step: 4,
      description: "Inspect request headers",
      action: "Click on API request > Headers tab > Request Headers",
      expectedResult: "Header 'x-tenant-slug: tenant-b' is present in all API requests"
    },
    {
      step: 5,
      description: "Test multiple API endpoints",
      action: "Check headers for /public/services, /public/doctors, /public/settings",
      expectedResult: "All API requests include 'x-tenant-slug: tenant-b'"
    }
  ];
  
  return {
    testName: "Tenant B API Headers",
    requirement: "6.1",
    steps: testSteps,
    validationCriteria: [
      "All API requests include X-Tenant-Slug header",
      "Header value is 'tenant-b'",
      "Header is present for all public endpoints",
      "Header is present for authenticated endpoints (if logged in)"
    ]
  };
}

/**
 * Test Scenario 5: Tenant switching between subdomains
 * Requirement: 6.1 - Verify tenant context switches properly
 */
function testTenantSwitching() {
  console.log("🧪 Testing Tenant Switching Between Subdomains");
  
  const testSteps = [
    {
      step: 1,
      description: "Start at tenant-a.localhost:3001",
      action: "Navigate to http://tenant-a.localhost:3001",
      expectedResult: "Cookie 'tenantSlug' = 'tenant-a'"
    },
    {
      step: 2,
      description: "Verify tenant-a API headers",
      action: "Check Network tab for API requests",
      expectedResult: "All requests include 'x-tenant-slug: tenant-a'"
    },
    {
      step: 3,
      description: "Switch to tenant-b.localhost:3001",
      action: "Navigate to http://tenant-b.localhost:3001",
      expectedResult: "Cookie 'tenantSlug' updated to 'tenant-b'"
    },
    {
      step: 4,
      description: "Verify tenant-b API headers",
      action: "Check Network tab for new API requests",
      expectedResult: "All requests now include 'x-tenant-slug: tenant-b'"
    },
    {
      step: 5,
      description: "Switch back to tenant-a",
      action: "Navigate back to http://tenant-a.localhost:3001",
      expectedResult: "Cookie 'tenantSlug' updated back to 'tenant-a'"
    },
    {
      step: 6,
      description: "Verify tenant-a API headers again",
      action: "Check Network tab for API requests",
      expectedResult: "All requests include 'x-tenant-slug: tenant-a' again"
    }
  ];
  
  return {
    testName: "Tenant Switching Between Subdomains",
    requirement: "6.1",
    steps: testSteps,
    validationCriteria: [
      "Cookie updates correctly when switching subdomains",
      "API headers update to match current subdomain",
      "No stale tenant data persists after switching",
      "Switching works in both directions (A→B and B→A)"
    ]
  };
}

/**
 * Test Scenario 6: Tenant isolation - data separation
 * Requirement: 6.1 - Verify each tenant sees only their own data
 */
function testTenantDataIsolation() {
  console.log("🧪 Testing Tenant Data Isolation");
  
  const testSteps = [
    {
      step: 1,
      description: "Access tenant-a and note available data",
      action: "Navigate to tenant-a.localhost:3001 and view services/doctors",
      expectedResult: "Tenant A's services and doctors are displayed"
    },
    {
      step: 2,
      description: "Record tenant-a data identifiers",
      action: "Note service names, doctor names, blog titles",
      expectedResult: "List of tenant-a specific data"
    },
    {
      step: 3,
      description: "Switch to tenant-b",
      action: "Navigate to tenant-b.localhost:3001",
      expectedResult: "Page loads with tenant-b context"
    },
    {
      step: 4,
      description: "Verify different data is displayed",
      action: "View services/doctors on tenant-b",
      expectedResult: "Tenant B's services and doctors are displayed (different from tenant-a)"
    },
    {
      step: 5,
      description: "Verify no cross-tenant data leakage",
      action: "Check that tenant-a data is not visible on tenant-b",
      expectedResult: "Only tenant-b data is visible, no tenant-a data"
    }
  ];
  
  return {
    testName: "Tenant Data Isolation",
    requirement: "6.1",
    steps: testSteps,
    validationCriteria: [
      "Each tenant displays only their own data",
      "No cross-tenant data leakage",
      "API responses are filtered by tenant",
      "Switching tenants shows different data sets"
    ]
  };
}

/**
 * Test Scenario 7: Middleware tenant resolution priority
 * Tests the priority order: query param > subdomain > cookie > default
 */
function testTenantResolutionPriority() {
  console.log("🧪 Testing Tenant Resolution Priority");
  
  const testSteps = [
    {
      step: 1,
      description: "Test subdomain resolution",
      action: "Navigate to tenant-a.localhost:3001",
      expectedResult: "Tenant resolved from subdomain: 'tenant-a'"
    },
    {
      step: 2,
      description: "Test query parameter override",
      action: "Navigate to tenant-a.localhost:3001?tenant=tenant-b",
      expectedResult: "Tenant resolved from query param: 'tenant-b', cookie updated"
    },
    {
      step: 3,
      description: "Test cookie fallback",
      action: "Navigate to localhost:3001 (no subdomain) with existing cookie",
      expectedResult: "Tenant resolved from cookie value"
    },
    {
      step: 4,
      description: "Test default fallback",
      action: "Clear cookies and navigate to localhost:3001",
      expectedResult: "Tenant resolved to default tenant"
    }
  ];
  
  return {
    testName: "Tenant Resolution Priority",
    requirement: "6.1",
    steps: testSteps,
    validationCriteria: [
      "Query parameter has highest priority",
      "Subdomain resolution works when no query param",
      "Cookie fallback works when no subdomain",
      "Default tenant used when all else fails"
    ]
  };
}

/**
 * Test Scenario 8: Authenticated requests with tenant context
 * Tests that authenticated API requests include tenant header
 */
function testAuthenticatedRequestsWithTenant() {
  console.log("🧪 Testing Authenticated Requests with Tenant Context");
  
  const testSteps = [
    {
      step: 1,
      description: "Login on tenant-a.localhost:3001",
      action: "Navigate to tenant-a.localhost:3001/en/login and login",
      expectedResult: "Successfully logged in, auth token stored"
    },
    {
      step: 2,
      description: "Make authenticated API requests",
      action: "Navigate to dashboard, view appointments, profile",
      expectedResult: "Authenticated endpoints return data"
    },
    {
      step: 3,
      description: "Verify tenant header in authenticated requests",
      action: "Check Network tab for /patient/me, /appointments/my requests",
      expectedResult: "All requests include 'x-tenant-slug: tenant-a' AND Authorization header"
    },
    {
      step: 4,
      description: "Switch to tenant-b while logged in",
      action: "Navigate to tenant-b.localhost:3001",
      expectedResult: "Tenant context switches to tenant-b"
    },
    {
      step: 5,
      description: "Verify tenant header updated for authenticated requests",
      action: "Check Network tab for API requests on tenant-b",
      expectedResult: "Requests include 'x-tenant-slug: tenant-b' with same auth token"
    }
  ];
  
  return {
    testName: "Authenticated Requests with Tenant Context",
    requirement: "6.1",
    steps: testSteps,
    validationCriteria: [
      "Authenticated requests include both tenant header and auth token",
      "Tenant header updates when switching subdomains",
      "Auth token persists across tenant switches",
      "API returns correct tenant-scoped data for authenticated user"
    ]
  };
}

/**
 * Manual Test Execution Guide
 */
function generateTestExecutionGuide() {
  return {
    title: "Web App Subdomain Routing Test Execution Guide",
    
    prerequisites: [
      "1. Ensure web-next development server is running on port 3001",
      "2. Ensure API backend is running and accessible",
      "3. Configure /etc/hosts to include:",
      "   127.0.0.1 tenant-a.localhost",
      "   127.0.0.1 tenant-b.localhost",
      "4. Create test tenants in database:",
      "   - Tenant A with slug 'tenant-a'",
      "   - Tenant B with slug 'tenant-b'",
      "5. Add test data (services, doctors) to each tenant",
      "6. Open browser with DevTools ready"
    ],
    
    hostsFileSetup: {
      macOS: "sudo nano /etc/hosts",
      linux: "sudo nano /etc/hosts",
      windows: "notepad C:\\Windows\\System32\\drivers\\etc\\hosts (as Administrator)",
      entries: [
        "127.0.0.1 tenant-a.localhost",
        "127.0.0.1 tenant-b.localhost"
      ]
    },
    
    databaseSetup: [
      "-- Create test tenants",
      "INSERT INTO tenants (slug, name, status, created_at, updated_at)",
      "VALUES ('tenant-a', 'Tenant A Clinic', 'ACTIVE', NOW(), NOW());",
      "",
      "INSERT INTO tenants (slug, name, status, created_at, updated_at)",
      "VALUES ('tenant-b', 'Tenant B Clinic', 'ACTIVE', NOW(), NOW());",
      "",
      "-- Add test data for each tenant (services, doctors, etc.)"
    ],
    
    testExecution: [
      {
        test: "Tenant A Cookie Set",
        steps: [
          "1. Navigate to http://tenant-a.localhost:3001",
          "2. Open DevTools > Application > Cookies",
          "3. Verify 'tenantSlug' cookie = 'tenant-a'",
          "4. Navigate to different pages and verify cookie persists"
        ]
      },
      {
        test: "Tenant A API Headers",
        steps: [
          "1. Stay on tenant-a.localhost:3001",
          "2. Open DevTools > Network tab",
          "3. Navigate to /en/services or /en/doctors",
          "4. Click on API requests and check Request Headers",
          "5. Verify 'x-tenant-slug: tenant-a' is present"
        ]
      },
      {
        test: "Tenant B Cookie Set",
        steps: [
          "1. Navigate to http://tenant-b.localhost:3001",
          "2. Open DevTools > Application > Cookies",
          "3. Verify 'tenantSlug' cookie = 'tenant-b'",
          "4. Navigate to different pages and verify cookie persists"
        ]
      },
      {
        test: "Tenant B API Headers",
        steps: [
          "1. Stay on tenant-b.localhost:3001",
          "2. Open DevTools > Network tab",
          "3. Navigate to /en/services or /en/doctors",
          "4. Click on API requests and check Request Headers",
          "5. Verify 'x-tenant-slug: tenant-b' is present"
        ]
      },
      {
        test: "Tenant Switching",
        steps: [
          "1. Start at tenant-a.localhost:3001",
          "2. Verify cookie and API headers for tenant-a",
          "3. Navigate to tenant-b.localhost:3001",
          "4. Verify cookie and API headers updated to tenant-b",
          "5. Navigate back to tenant-a.localhost:3001",
          "6. Verify cookie and API headers updated back to tenant-a"
        ]
      },
      {
        test: "Tenant Data Isolation",
        steps: [
          "1. View services/doctors on tenant-a.localhost:3001",
          "2. Note the specific data displayed",
          "3. Switch to tenant-b.localhost:3001",
          "4. Verify different services/doctors are displayed",
          "5. Confirm no tenant-a data is visible on tenant-b"
        ]
      },
      {
        test: "Tenant Resolution Priority",
        steps: [
          "1. Test subdomain: tenant-a.localhost:3001",
          "2. Test query param: tenant-a.localhost:3001?tenant=tenant-b",
          "3. Test cookie: localhost:3001 with existing cookie",
          "4. Test default: localhost:3001 with no cookie"
        ]
      },
      {
        test: "Authenticated Requests",
        steps: [
          "1. Login on tenant-a.localhost:3001",
          "2. Check authenticated API requests include tenant header",
          "3. Switch to tenant-b.localhost:3001",
          "4. Verify tenant header updates while auth token persists"
        ]
      }
    ],
    
    expectedResults: [
      "✅ Cookie 'tenantSlug' set correctly for each subdomain",
      "✅ API requests include 'x-tenant-slug' header matching subdomain",
      "✅ Tenant context switches properly between subdomains",
      "✅ Each tenant displays only their own data",
      "✅ No cross-tenant data leakage",
      "✅ Tenant resolution priority works correctly",
      "✅ Authenticated requests include both tenant header and auth token",
      "✅ Cookie and header values are consistent"
    ],
    
    debuggingTips: [
      "Use DevTools > Application > Cookies to inspect cookie values",
      "Use DevTools > Network tab to inspect request headers",
      "Filter Network tab by 'localhost:8080' to see only API requests",
      "Check console for any tenant resolution errors",
      "Verify /etc/hosts entries are correct if subdomains don't resolve",
      "Clear cookies between tests to ensure clean state",
      "Use incognito/private window for isolated testing"
    ],
    
    commonIssues: [
      {
        issue: "Subdomain doesn't resolve",
        solution: "Check /etc/hosts file includes subdomain entries"
      },
      {
        issue: "Cookie not set",
        solution: "Check middleware is running and cookie settings are correct"
      },
      {
        issue: "API header missing",
        solution: "Verify getTenantSlugClient() is working and api.ts includes header"
      },
      {
        issue: "Wrong tenant data displayed",
        solution: "Check API backend is filtering by tenant_id correctly"
      },
      {
        issue: "Tenant doesn't switch",
        solution: "Clear cookies and verify middleware updates cookie on navigation"
      }
    ]
  };
}

/**
 * Automated verification helper (can be run in browser console)
 */
function verifyTenantContext() {
  if (typeof window === 'undefined') {
    console.error("This function must be run in a browser context");
    return;
  }
  
  const results = {
    hostname: window.location.hostname,
    port: window.location.port,
    cookie: null,
    expectedTenant: null,
    isCorrect: false
  };
  
  // Get cookie value
  const cookieMatch = document.cookie
    .split(';')
    .map(c => c.trim())
    .find(c => c.startsWith('tenantSlug='));
  
  if (cookieMatch) {
    results.cookie = cookieMatch.split('=')[1];
  }
  
  // Determine expected tenant from hostname
  const hostname = window.location.hostname.toLowerCase();
  if (hostname.startsWith('tenant-a')) {
    results.expectedTenant = 'tenant-a';
  } else if (hostname.startsWith('tenant-b')) {
    results.expectedTenant = 'tenant-b';
  } else {
    results.expectedTenant = 'default';
  }
  
  // Check if cookie matches expected
  results.isCorrect = results.cookie === results.expectedTenant;
  
  console.log("🔍 Tenant Context Verification:");
  console.log(`   Hostname: ${results.hostname}`);
  console.log(`   Expected Tenant: ${results.expectedTenant}`);
  console.log(`   Cookie Value: ${results.cookie}`);
  console.log(`   Status: ${results.isCorrect ? '✅ CORRECT' : '❌ MISMATCH'}`);
  
  return results;
}

/**
 * Helper to check API request headers (must be called after API request)
 */
function checkLastAPIRequestHeaders() {
  console.log("📡 To check API request headers:");
  console.log("1. Open DevTools > Network tab");
  console.log("2. Filter by 'localhost:8080' or your API domain");
  console.log("3. Click on any API request");
  console.log("4. Go to 'Headers' tab");
  console.log("5. Look for 'x-tenant-slug' in Request Headers");
  console.log("6. Verify it matches your current subdomain");
}

// Export test functions for manual execution
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    testTenantACookieSet,
    testTenantAAPIHeaders,
    testTenantBCookieSet,
    testTenantBAPIHeaders,
    testTenantSwitching,
    testTenantDataIsolation,
    testTenantResolutionPriority,
    testAuthenticatedRequestsWithTenant,
    generateTestExecutionGuide,
    verifyTenantContext,
    checkLastAPIRequestHeaders,
    TEST_CONFIG
  };
}

// Browser console execution
if (typeof window !== 'undefined') {
  window.SubdomainRoutingTests = {
    testTenantACookieSet,
    testTenantAAPIHeaders,
    testTenantBCookieSet,
    testTenantBAPIHeaders,
    testTenantSwitching,
    testTenantDataIsolation,
    testTenantResolutionPriority,
    testAuthenticatedRequestsWithTenant,
    generateTestExecutionGuide,
    verifyTenantContext,
    checkLastAPIRequestHeaders,
    TEST_CONFIG
  };
  
  console.log("🧪 Subdomain Routing Tests loaded.");
  console.log("   Run window.SubdomainRoutingTests.generateTestExecutionGuide() for instructions.");
  console.log("   Run window.SubdomainRoutingTests.verifyTenantContext() to check current tenant context.");
}
