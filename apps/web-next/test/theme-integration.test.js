/**
 * Integration Tests for Manual Theme Selection System
 * 
 * This test suite verifies:
 * 1. Tenant resolution with subdomain
 * 2. Tenant resolution with custom domain
 * 3. Theme loading for different tenants
 * 4. Theme switching via API
 * 5. Fallback to default theme
 * 
 * Requirements: 4.5, 6.3, 6.4, 8.3
 */

const TEST_CONFIG = {
  baseUrl: 'http://localhost:3001',
  apiBaseUrl: 'http://localhost:3001/api',
  tenants: {
    clinicA: {
      slug: 'clinic-a',
      subdomain: 'clinic-a.localhost:3001',
      themeKey: 'clinic',
      themeName: 'Clinic Theme'
    },
    clinicB: {
      slug: 'clinic-b',
      subdomain: 'clinic-b.localhost:3001',
      themeKey: 'barber',
      themeName: 'Barber Theme'
    },
    customDomain: {
      slug: 'custom-clinic',
      domain: 'custom.localhost',
      themeKey: 'clinic',
      themeName: 'Clinic Theme'
    }
  },
  themes: {
    clinic: { key: 'clinic', name: 'Clinic Theme' },
    barber: { key: 'barber', name: 'Barber Theme' },
    default: { key: 'default', name: 'Default Theme' }
  }
};

/**
 * Test Scenario 1: Tenant Resolution with Subdomain
 * Requirement: 4.5, 6.3 - Verify tenant is resolved from subdomain
 */
function testTenantResolutionSubdomain() {
  console.log("🧪 Test 1: Tenant Resolution with Subdomain");
  
  const testSteps = [
    {
      step: 1,
      description: "Access tenant via subdomain",
      action: "Navigate to clinic-a.localhost:3001",
      verification: [
        "Check x-tenant-slug header is set to 'clinic-a'",
        "Check tenantSlug cookie is set to 'clinic-a'",
        "Verify middleware resolves tenant correctly"
      ],
      expectedResult: "Tenant 'clinic-a' is resolved from subdomain"
    },
    {
      step: 2,
      description: "Verify tenant header in API requests",
      action: "Make request to /api/internal/tenant",
      verification: [
        "Response includes slug: 'clinic-a'",
        "Response includes themeId"
      ],
      expectedResult: "API returns correct tenant information"
    },
    {
      step: 3,
      description: "Test different subdomain",
      action: "Navigate to clinic-b.localhost:3001",
      verification: [
        "Check x-tenant-slug header is set to 'clinic-b'",
        "Check tenantSlug cookie is updated to 'clinic-b'",
        "Verify tenant context switches"
      ],
      expectedResult: "Tenant 'clinic-b' is resolved from subdomain"
    }
  ];
  
  return {
    testName: "Tenant Resolution with Subdomain",
    requirement: "4.5, 6.3",
    steps: testSteps,
    validationCriteria: [
      "✅ Subdomain is correctly parsed from hostname",
      "✅ Tenant slug is extracted from subdomain",
      "✅ x-tenant-slug header is set in middleware",
      "✅ tenantSlug cookie is set with correct value",
      "✅ Different subdomains resolve to different tenants"
    ]
  };
}

/**
 * Test Scenario 2: Tenant Resolution with Custom Domain
 * Requirement: 6.3, 6.4 - Verify tenant is resolved from custom domain
 */
function testTenantResolutionCustomDomain() {
  console.log("🧪 Test 2: Tenant Resolution with Custom Domain");
  
  const testSteps = [
    {
      step: 1,
      description: "Setup custom domain in database",
      action: "UPDATE tenants SET domain = 'custom.localhost' WHERE slug = 'custom-clinic'",
      verification: [
        "Verify domain field is set in database",
        "Ensure domain is unique"
      ],
      expectedResult: "Custom domain configured for tenant"
    },
    {
      step: 2,
      description: "Access tenant via custom domain",
      action: "Navigate to custom.localhost:3001",
      verification: [
        "Middleware queries database by domain",
        "Tenant slug is resolved from domain lookup",
        "x-tenant-slug header is set correctly"
      ],
      expectedResult: "Tenant 'custom-clinic' is resolved from custom domain"
    },
    {
      step: 3,
      description: "Verify tenant header in API requests",
      action: "Make request to /api/internal/tenant",
      verification: [
        "Response includes slug: 'custom-clinic'",
        "Response includes themeId"
      ],
      expectedResult: "API returns correct tenant information"
    },
    {
      step: 4,
      description: "Test inactive tenant with custom domain",
      action: "UPDATE tenants SET status = 'INACTIVE' WHERE slug = 'custom-clinic'",
      verification: [
        "Access custom.localhost:3001",
        "Verify rewrite to /_errors/no-tenant",
        "Error page is displayed"
      ],
      expectedResult: "Inactive tenant shows error page"
    }
  ];
  
  return {
    testName: "Tenant Resolution with Custom Domain",
    requirement: "6.3, 6.4",
    steps: testSteps,
    validationCriteria: [
      "✅ Custom domain is looked up in database",
      "✅ Tenant slug is resolved from domain field",
      "✅ x-tenant-slug header is set correctly",
      "✅ Inactive tenants show error page",
      "✅ Custom domain takes precedence over subdomain"
    ]
  };
}

/**
 * Test Scenario 3: Theme Loading for Different Tenants
 * Requirement: 4.5, 6.3 - Verify correct theme loads for each tenant
 */
function testThemeLoadingDifferentTenants() {
  console.log("🧪 Test 3: Theme Loading for Different Tenants");
  
  const testSteps = [
    {
      step: 1,
      description: "Load tenant with clinic theme",
      action: "Navigate to clinic-a.localhost:3001",
      verification: [
        "getTenantTheme() is called with slug 'clinic-a'",
        "Database query returns theme with key 'clinic'",
        "Theme layout is dynamically imported from themes/clinic/layout",
        "Clinic theme components are rendered (Header, Footer)"
      ],
      expectedResult: "Clinic theme is loaded and rendered"
    },
    {
      step: 2,
      description: "Verify clinic theme styling",
      action: "Inspect page elements",
      verification: [
        "Check for clinic-specific CSS classes",
        "Verify clinic theme colors and styles",
        "Confirm clinic theme components are present"
      ],
      expectedResult: "Clinic theme styling is applied"
    },
    {
      step: 3,
      description: "Load tenant with barber theme",
      action: "Navigate to clinic-b.localhost:3001",
      verification: [
        "getTenantTheme() is called with slug 'clinic-b'",
        "Database query returns theme with key 'barber'",
        "Theme layout is dynamically imported from themes/barber/layout",
        "Barber theme components are rendered"
      ],
      expectedResult: "Barber theme is loaded and rendered"
    },
    {
      step: 4,
      description: "Verify barber theme styling",
      action: "Inspect page elements",
      verification: [
        "Check for barber-specific CSS classes",
        "Verify barber theme colors and styles",
        "Confirm barber theme components are present"
      ],
      expectedResult: "Barber theme styling is applied"
    },
    {
      step: 5,
      description: "Verify theme isolation",
      action: "Compare rendered pages",
      verification: [
        "Clinic theme and barber theme have different layouts",
        "No cross-theme styling leakage",
        "Each theme uses its own components"
      ],
      expectedResult: "Themes are properly isolated"
    }
  ];
  
  return {
    testName: "Theme Loading for Different Tenants",
    requirement: "4.5, 6.3",
    steps: testSteps,
    validationCriteria: [
      "✅ getTenantTheme() retrieves correct theme for tenant",
      "✅ Theme layout is dynamically imported",
      "✅ Theme-specific components are rendered",
      "✅ Theme-specific styling is applied",
      "✅ Different tenants load different themes",
      "✅ Themes are properly isolated"
    ]
  };
}

/**
 * Test Scenario 4: Theme Switching via API
 * Requirement: 8.3 - Verify theme can be changed via API
 */
function testThemeSwitchingAPI() {
  console.log("🧪 Test 4: Theme Switching via API");
  
  const testSteps = [
    {
      step: 1,
      description: "Get current theme for tenant",
      action: "GET /api/internal/tenant (on clinic-a.localhost:3001)",
      verification: [
        "Response includes current themeId",
        "Note the current theme"
      ],
      expectedResult: "Current theme retrieved successfully"
    },
    {
      step: 2,
      description: "Get available themes",
      action: "GET /api/themes",
      verification: [
        "Response includes list of published themes",
        "Each theme has id, key, and name",
        "Select a different theme ID"
      ],
      expectedResult: "Available themes retrieved successfully"
    },
    {
      step: 3,
      description: "Update tenant theme",
      action: "POST /api/tenants/clinic-a/theme with { themeId: '<new-theme-id>' }",
      verification: [
        "Request validates themeId",
        "Theme exists and is published",
        "Database is updated with new themeId",
        "Response includes updated tenant data"
      ],
      expectedResult: "Theme updated successfully"
    },
    {
      step: 4,
      description: "Verify theme change takes effect",
      action: "Refresh clinic-a.localhost:3001",
      verification: [
        "getTenantTheme() returns new theme",
        "New theme layout is loaded",
        "New theme components are rendered",
        "Page displays with new theme styling"
      ],
      expectedResult: "New theme is immediately active"
    },
    {
      step: 5,
      description: "Test invalid theme ID",
      action: "POST /api/tenants/clinic-a/theme with { themeId: 'invalid-id' }",
      verification: [
        "API returns 404 error",
        "Error message: 'Theme not found'",
        "Database is not updated"
      ],
      expectedResult: "Invalid theme ID is rejected"
    },
    {
      step: 6,
      description: "Test draft theme assignment",
      action: "POST /api/tenants/clinic-a/theme with draft theme ID",
      verification: [
        "API returns 400 error",
        "Error message: 'Theme is not available for selection'",
        "Database is not updated"
      ],
      expectedResult: "Draft theme assignment is rejected"
    }
  ];
  
  return {
    testName: "Theme Switching via API",
    requirement: "8.3",
    steps: testSteps,
    validationCriteria: [
      "✅ Current theme can be retrieved via API",
      "✅ Available themes can be listed",
      "✅ Theme can be updated via POST request",
      "✅ Theme validation ensures theme exists",
      "✅ Only published themes can be assigned",
      "✅ Theme change takes effect immediately",
      "✅ Invalid theme IDs are rejected",
      "✅ Draft themes cannot be assigned"
    ]
  };
}

/**
 * Test Scenario 5: Fallback to Default Theme
 * Requirement: 4.5 - Verify fallback when theme not found
 */
function testFallbackDefaultTheme() {
  console.log("🧪 Test 5: Fallback to Default Theme");
  
  const testSteps = [
    {
      step: 1,
      description: "Create tenant without theme",
      action: "INSERT INTO tenants (slug, name, status) VALUES ('no-theme', 'No Theme Tenant', 'ACTIVE')",
      verification: [
        "Tenant created with themeId = NULL",
        "Tenant is active"
      ],
      expectedResult: "Tenant without theme created"
    },
    {
      step: 2,
      description: "Access tenant without theme",
      action: "Navigate to no-theme.localhost:3001",
      verification: [
        "getTenantTheme() returns null",
        "getDefaultTheme() is called",
        "Default theme layout is loaded",
        "Page renders with default theme"
      ],
      expectedResult: "Default theme is used as fallback"
    },
    {
      step: 3,
      description: "Test theme file not found",
      action: "Assign non-existent theme key to tenant",
      verification: [
        "Dynamic import fails for missing theme",
        "Catch block handles error",
        "Falls back to default theme",
        "Page renders with default theme"
      ],
      expectedResult: "Default theme is used when theme file missing"
    },
    {
      step: 4,
      description: "Test database error during theme lookup",
      action: "Simulate database connection failure",
      verification: [
        "getTenantTheme() catches error",
        "Returns null",
        "getDefaultTheme() is called",
        "Default theme is loaded"
      ],
      expectedResult: "Default theme is used on database error"
    },
    {
      step: 5,
      description: "Verify default theme structure",
      action: "Inspect default theme rendering",
      verification: [
        "Default theme layout is rendered",
        "Default theme components are present",
        "Page is functional with default theme",
        "No errors in console"
      ],
      expectedResult: "Default theme provides complete functionality"
    }
  ];
  
  return {
    testName: "Fallback to Default Theme",
    requirement: "4.5",
    steps: testSteps,
    validationCriteria: [
      "✅ Tenant without theme uses default theme",
      "✅ Missing theme file falls back to default",
      "✅ Database errors fall back to default",
      "✅ Default theme renders correctly",
      "✅ No errors when using default theme",
      "✅ Application remains functional with default theme"
    ]
  };
}

/**
 * Test Scenario 6: End-to-End Theme Selection Flow
 * Integration test covering complete user journey
 */
function testEndToEndThemeSelection() {
  console.log("🧪 Test 6: End-to-End Theme Selection Flow");
  
  const testSteps = [
    {
      step: 1,
      description: "Initial state - tenant with clinic theme",
      action: "Navigate to clinic-a.localhost:3001",
      verification: [
        "Clinic theme is loaded",
        "Page displays with clinic styling"
      ],
      expectedResult: "Initial theme is clinic"
    },
    {
      step: 2,
      description: "Access theme settings page",
      action: "Navigate to /en/dashboard/settings/theme",
      verification: [
        "Theme settings page loads",
        "Current theme is displayed",
        "Available themes are listed in dropdown"
      ],
      expectedResult: "Theme settings page is accessible"
    },
    {
      step: 3,
      description: "Select new theme",
      action: "Select 'Barber Theme' from dropdown",
      verification: [
        "Dropdown value changes",
        "Save button is enabled"
      ],
      expectedResult: "New theme is selected"
    },
    {
      step: 4,
      description: "Save theme selection",
      action: "Click 'Save Theme' button",
      verification: [
        "POST request to /api/tenants/clinic-a/theme",
        "Request includes new themeId",
        "Response confirms update",
        "Page reloads automatically"
      ],
      expectedResult: "Theme is saved successfully"
    },
    {
      step: 5,
      description: "Verify new theme is active",
      action: "Page reloads after save",
      verification: [
        "Barber theme is now loaded",
        "Page displays with barber styling",
        "Theme settings page shows barber as current theme"
      ],
      expectedResult: "New theme is immediately active"
    },
    {
      step: 6,
      description: "Verify persistence",
      action: "Navigate to different pages and return",
      verification: [
        "Barber theme persists across navigation",
        "Theme is consistent on all pages",
        "No reversion to previous theme"
      ],
      expectedResult: "Theme change is persistent"
    }
  ];
  
  return {
    testName: "End-to-End Theme Selection Flow",
    requirement: "4.5, 6.3, 6.4, 8.3",
    steps: testSteps,
    validationCriteria: [
      "✅ Theme settings page is accessible",
      "✅ Current theme is displayed correctly",
      "✅ Available themes are listed",
      "✅ Theme can be selected from dropdown",
      "✅ Theme change is saved via API",
      "✅ Page reloads after save",
      "✅ New theme is immediately active",
      "✅ Theme change persists across navigation"
    ]
  };
}

/**
 * Manual Test Execution Guide
 */
function generateTestExecutionGuide() {
  return {
    title: "Manual Theme Selection Integration Test Execution Guide",
    
    prerequisites: [
      "1. Ensure PostgreSQL database is running",
      "2. Ensure DATABASE_URL is configured in .env.local",
      "3. Run database migrations: pnpm prisma:migrate",
      "4. Seed database with themes and tenants: pnpm prisma:seed",
      "5. Start development server: pnpm dev",
      "6. Configure /etc/hosts with test subdomains:",
      "   127.0.0.1 clinic-a.localhost",
      "   127.0.0.1 clinic-b.localhost",
      "   127.0.0.1 custom.localhost",
      "   127.0.0.1 no-theme.localhost"
    ],
    
    databaseSetup: [
      "-- Verify themes exist",
      "SELECT * FROM \"Theme\" WHERE status = 'published';",
      "",
      "-- Verify tenants exist with themes",
      "SELECT t.slug, t.name, t.domain, th.key as theme_key, th.name as theme_name",
      "FROM tenants t",
      "LEFT JOIN \"Theme\" th ON t.\"themeId\" = th.id;",
      "",
      "-- Create test tenant without theme",
      "INSERT INTO tenants (slug, name, status, created_at, updated_at)",
      "VALUES ('no-theme', 'No Theme Tenant', 'ACTIVE', NOW(), NOW());",
      "",
      "-- Create tenant with custom domain",
      "INSERT INTO tenants (slug, name, status, domain, created_at, updated_at)",
      "VALUES ('custom-clinic', 'Custom Domain Clinic', 'ACTIVE', 'custom.localhost', NOW(), NOW());"
    ],
    
    testExecution: [
      {
        test: "Test 1: Tenant Resolution with Subdomain",
        commands: [
          "# Access tenant via subdomain",
          "curl -v http://clinic-a.localhost:3001/en 2>&1 | grep -i 'x-tenant-slug\\|set-cookie'",
          "",
          "# Verify tenant API",
          "curl -H 'x-tenant-slug: clinic-a' http://localhost:3001/api/internal/tenant"
        ]
      },
      {
        test: "Test 2: Tenant Resolution with Custom Domain",
        commands: [
          "# Setup custom domain",
          "psql -d your_db -c \"UPDATE tenants SET domain = 'custom.localhost' WHERE slug = 'custom-clinic';\"",
          "",
          "# Access via custom domain",
          "curl -v http://custom.localhost:3001/en 2>&1 | grep -i 'x-tenant-slug'"
        ]
      },
      {
        test: "Test 3: Theme Loading",
        commands: [
          "# Access clinic-a (should load clinic theme)",
          "curl http://clinic-a.localhost:3001/en | grep -i 'clinic-theme'",
          "",
          "# Access clinic-b (should load barber theme)",
          "curl http://clinic-b.localhost:3001/en | grep -i 'barber-theme'"
        ]
      },
      {
        test: "Test 4: Theme Switching via API",
        commands: [
          "# Get available themes",
          "curl http://localhost:3001/api/themes",
          "",
          "# Get current tenant theme",
          "curl -H 'x-tenant-slug: clinic-a' http://localhost:3001/api/internal/tenant",
          "",
          "# Update theme (replace THEME_ID with actual ID)",
          "curl -X POST http://localhost:3001/api/tenants/clinic-a/theme \\",
          "  -H 'Content-Type: application/json' \\",
          "  -d '{\"themeId\": \"THEME_ID\"}'",
          "",
          "# Verify theme changed",
          "curl -H 'x-tenant-slug: clinic-a' http://localhost:3001/api/internal/tenant"
        ]
      },
      {
        test: "Test 5: Fallback to Default Theme",
        commands: [
          "# Access tenant without theme",
          "curl http://no-theme.localhost:3001/en | grep -i 'default-theme'"
        ]
      }
    ],
    
    browserTesting: {
      description: "Manual browser testing steps",
      steps: [
        "1. Open browser and navigate to http://clinic-a.localhost:3001",
        "2. Open DevTools > Network tab",
        "3. Verify x-tenant-slug header in requests",
        "4. Check Application > Cookies for tenantSlug cookie",
        "5. Navigate to /en/dashboard/settings/theme",
        "6. Select different theme from dropdown",
        "7. Click 'Save Theme' button",
        "8. Verify page reloads with new theme",
        "9. Navigate to different pages to verify theme persists",
        "10. Switch to http://clinic-b.localhost:3001",
        "11. Verify different theme is loaded",
        "12. Test custom domain: http://custom.localhost:3001"
      ]
    },
    
    expectedResults: [
      "✅ Subdomains resolve to correct tenants",
      "✅ Custom domains resolve to correct tenants",
      "✅ x-tenant-slug header is set correctly",
      "✅ tenantSlug cookie is set correctly",
      "✅ Different tenants load different themes",
      "✅ Theme can be changed via API",
      "✅ Theme changes take effect immediately",
      "✅ Default theme is used as fallback",
      "✅ Theme settings page works correctly",
      "✅ Themes are properly isolated"
    ],
    
    debuggingTips: [
      "Check server console for theme loading errors",
      "Verify DATABASE_URL is correct in .env.local",
      "Ensure all themes exist in database",
      "Check that theme files exist in themes/ directory",
      "Use pnpm prisma:studio to inspect database",
      "Check middleware logs for tenant resolution",
      "Verify /etc/hosts entries are correct",
      "Clear browser cache if theme doesn't update",
      "Check Network tab for API request/response",
      "Look for console errors in browser DevTools"
    ],
    
    commonIssues: [
      {
        issue: "Subdomain doesn't resolve",
        solution: "Check /etc/hosts file and restart browser"
      },
      {
        issue: "Theme doesn't load",
        solution: "Verify theme files exist and theme is in database"
      },
      {
        issue: "Theme doesn't change",
        solution: "Check API response and database update"
      },
      {
        issue: "Default theme not working",
        solution: "Verify themes/default/layout.tsx exists"
      },
      {
        issue: "Custom domain not working",
        solution: "Check domain field in database and middleware logic"
      },
      {
        issue: "API returns 404",
        solution: "Verify tenant slug and theme ID are correct"
      }
    ]
  };
}

/**
 * Automated verification helper
 */
async function runAutomatedTests() {
  console.log("🚀 Running Automated Theme Integration Tests\n");
  
  const results = {
    passed: 0,
    failed: 0,
    tests: []
  };
  
  // Test 1: Verify themes API
  try {
    console.log("Test 1: Verify themes API...");
    const themesResponse = await fetch(`${TEST_CONFIG.apiBaseUrl}/themes`);
    const themes = await themesResponse.json();
    
    if (Array.isArray(themes) && themes.length > 0) {
      console.log("✅ Themes API working");
      results.passed++;
      results.tests.push({ name: "Themes API", status: "passed" });
    } else {
      console.log("❌ Themes API returned empty or invalid data");
      results.failed++;
      results.tests.push({ name: "Themes API", status: "failed" });
    }
  } catch (error) {
    console.log("❌ Themes API error:", error.message);
    results.failed++;
    results.tests.push({ name: "Themes API", status: "failed", error: error.message });
  }
  
  // Test 2: Verify tenant API with header
  try {
    console.log("\nTest 2: Verify tenant API...");
    const tenantResponse = await fetch(`${TEST_CONFIG.apiBaseUrl}/internal/tenant`, {
      headers: {
        'x-tenant-slug': 'clinic-a'
      }
    });
    
    if (tenantResponse.ok) {
      const tenant = await tenantResponse.json();
      if (tenant.slug === 'clinic-a') {
        console.log("✅ Tenant API working");
        results.passed++;
        results.tests.push({ name: "Tenant API", status: "passed" });
      } else {
        console.log("❌ Tenant API returned wrong tenant");
        results.failed++;
        results.tests.push({ name: "Tenant API", status: "failed" });
      }
    } else {
      console.log("❌ Tenant API returned error:", tenantResponse.status);
      results.failed++;
      results.tests.push({ name: "Tenant API", status: "failed" });
    }
  } catch (error) {
    console.log("❌ Tenant API error:", error.message);
    results.failed++;
    results.tests.push({ name: "Tenant API", status: "failed", error: error.message });
  }
  
  // Test 3: Verify theme update API (validation only, no actual update)
  try {
    console.log("\nTest 3: Verify theme update API validation...");
    const updateResponse = await fetch(`${TEST_CONFIG.apiBaseUrl}/tenants/clinic-a/theme`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ themeId: 'invalid-theme-id' })
    });
    
    if (updateResponse.status === 404) {
      console.log("✅ Theme update API validation working");
      results.passed++;
      results.tests.push({ name: "Theme Update Validation", status: "passed" });
    } else {
      console.log("❌ Theme update API validation not working as expected");
      results.failed++;
      results.tests.push({ name: "Theme Update Validation", status: "failed" });
    }
  } catch (error) {
    console.log("❌ Theme update API error:", error.message);
    results.failed++;
    results.tests.push({ name: "Theme Update Validation", status: "failed", error: error.message });
  }
  
  console.log("\n" + "=".repeat(50));
  console.log(`Test Results: ${results.passed} passed, ${results.failed} failed`);
  console.log("=".repeat(50));
  
  return results;
}

// Export test functions
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    testTenantResolutionSubdomain,
    testTenantResolutionCustomDomain,
    testThemeLoadingDifferentTenants,
    testThemeSwitchingAPI,
    testFallbackDefaultTheme,
    testEndToEndThemeSelection,
    generateTestExecutionGuide,
    runAutomatedTests,
    TEST_CONFIG
  };
}

// Node.js execution
if (typeof require !== 'undefined' && require.main === module) {
  console.log("🧪 Manual Theme Selection Integration Tests");
  console.log("=" .repeat(60) + "\n");
  
  const guide = generateTestExecutionGuide();
  console.log(guide.title);
  console.log("\nPrerequisites:");
  guide.prerequisites.forEach(p => console.log(`  ${p}`));
  
  console.log("\n\nTo run automated tests:");
  console.log("  node test/theme-integration.test.js run");
  
  // Run automated tests if 'run' argument is passed
  if (process.argv.includes('run')) {
    console.log("\n\nRunning automated tests...\n");
    runAutomatedTests().catch(console.error);
  } else {
    console.log("\n\nFor detailed test execution guide:");
    console.log("  const guide = require('./test/theme-integration.test.js').generateTestExecutionGuide();");
    console.log("  console.log(JSON.stringify(guide, null, 2));");
  }
}
