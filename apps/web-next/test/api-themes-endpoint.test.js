/**
 * Manual Test for GET /api/themes endpoint
 * 
 * This test verifies that the /api/themes endpoint:
 * 1. Returns all published themes
 * 2. Returns themes with id, key, and name fields
 * 3. Handles database errors gracefully
 * 
 * Requirements: 9.1, 9.4
 */

const TEST_CONFIG = {
  baseUrl: 'http://localhost:3001',
  endpoint: '/api/themes',
  expectedFields: ['id', 'key', 'name'],
  expectedThemes: ['clinic', 'barber', 'default']
};

/**
 * Test Scenario 1: Fetch all published themes
 * Requirement: 9.1 - GET endpoint returns all published themes
 */
function testFetchPublishedThemes() {
  console.log("🧪 Testing GET /api/themes - Fetch Published Themes");
  
  const testSteps = [
    {
      step: 1,
      description: "Ensure database is seeded with themes",
      action: "Run: pnpm prisma:seed",
      expectedResult: "Database contains clinic, barber, and default themes"
    },
    {
      step: 2,
      description: "Start the development server",
      action: "Run: pnpm dev",
      expectedResult: "Server running on http://localhost:3001"
    },
    {
      step: 3,
      description: "Make GET request to /api/themes",
      action: "curl http://localhost:3001/api/themes",
      expectedResult: "Returns 200 OK with JSON array of themes"
    },
    {
      step: 4,
      description: "Verify response structure",
      action: "Check JSON response contains array of theme objects",
      expectedResult: "Each theme has id, key, and name fields"
    },
    {
      step: 5,
      description: "Verify all published themes are returned",
      action: "Check response includes clinic, barber, and default themes",
      expectedResult: "All three themes present in response"
    }
  ];
  
  return {
    testName: "Fetch Published Themes",
    requirement: "9.1",
    steps: testSteps,
    validationCriteria: [
      "Endpoint returns 200 OK status",
      "Response is valid JSON array",
      "Each theme has id, key, and name fields",
      "Only published themes are returned",
      "Response includes all expected themes"
    ]
  };
}

/**
 * Test Scenario 2: Verify response fields
 * Requirement: 9.1 - Returns themes with id, key, and name fields
 */
function testResponseFields() {
  console.log("🧪 Testing GET /api/themes - Response Fields");
  
  const testSteps = [
    {
      step: 1,
      description: "Make GET request to /api/themes",
      action: "curl http://localhost:3001/api/themes",
      expectedResult: "Returns JSON array"
    },
    {
      step: 2,
      description: "Verify each theme has 'id' field",
      action: "Check each object has 'id' property (string)",
      expectedResult: "All themes have id field"
    },
    {
      step: 3,
      description: "Verify each theme has 'key' field",
      action: "Check each object has 'key' property (string)",
      expectedResult: "All themes have key field (e.g., 'clinic', 'barber')"
    },
    {
      step: 4,
      description: "Verify each theme has 'name' field",
      action: "Check each object has 'name' property (string)",
      expectedResult: "All themes have name field (e.g., 'Clinic Theme')"
    },
    {
      step: 5,
      description: "Verify no extra fields are returned",
      action: "Check response only contains id, key, name (no createdAt, status, etc.)",
      expectedResult: "Only id, key, and name fields present"
    }
  ];
  
  return {
    testName: "Response Fields Validation",
    requirement: "9.1",
    steps: testSteps,
    validationCriteria: [
      "Each theme has 'id' field (string)",
      "Each theme has 'key' field (string)",
      "Each theme has 'name' field (string)",
      "No additional fields are exposed",
      "Field types are correct"
    ]
  };
}

/**
 * Test Scenario 3: Error handling
 * Requirement: 9.4 - Add error handling for database errors
 */
function testErrorHandling() {
  console.log("🧪 Testing GET /api/themes - Error Handling");
  
  const testSteps = [
    {
      step: 1,
      description: "Test with database unavailable",
      action: "Stop database or use invalid DATABASE_URL",
      expectedResult: "Returns 500 error with error message"
    },
    {
      step: 2,
      description: "Verify error response structure",
      action: "Check error response has 'error' and 'message' fields",
      expectedResult: "Error response is properly formatted JSON"
    },
    {
      step: 3,
      description: "Verify error is logged",
      action: "Check server console for error log",
      expectedResult: "Error is logged to console"
    },
    {
      step: 4,
      description: "Restore database connection",
      action: "Start database and verify endpoint works again",
      expectedResult: "Endpoint returns 200 OK with themes"
    }
  ];
  
  return {
    testName: "Error Handling",
    requirement: "9.4",
    steps: testSteps,
    validationCriteria: [
      "Database errors return 500 status code",
      "Error response includes error message",
      "Errors are logged to console",
      "Error messages don't expose sensitive information",
      "Endpoint recovers when database is restored"
    ]
  };
}

/**
 * Test Scenario 4: Only published themes returned
 * Requirement: 9.1 - Query published themes
 */
function testPublishedThemesOnly() {
  console.log("🧪 Testing GET /api/themes - Published Themes Only");
  
  const testSteps = [
    {
      step: 1,
      description: "Create a draft theme in database",
      action: "INSERT INTO \"Theme\" (id, key, name, status) VALUES (gen_random_uuid(), 'draft-theme', 'Draft Theme', 'draft')",
      expectedResult: "Draft theme created in database"
    },
    {
      step: 2,
      description: "Make GET request to /api/themes",
      action: "curl http://localhost:3001/api/themes",
      expectedResult: "Returns only published themes"
    },
    {
      step: 3,
      description: "Verify draft theme is not in response",
      action: "Check response does not include 'draft-theme'",
      expectedResult: "Draft theme is filtered out"
    },
    {
      step: 4,
      description: "Update draft theme to published",
      action: "UPDATE \"Theme\" SET status = 'published' WHERE key = 'draft-theme'",
      expectedResult: "Theme status updated to published"
    },
    {
      step: 5,
      description: "Verify theme now appears in response",
      action: "curl http://localhost:3001/api/themes",
      expectedResult: "Draft theme now included in response"
    }
  ];
  
  return {
    testName: "Published Themes Only",
    requirement: "9.1",
    steps: testSteps,
    validationCriteria: [
      "Only themes with status='published' are returned",
      "Draft themes are filtered out",
      "Updating theme status to published makes it visible",
      "Status filtering works correctly"
    ]
  };
}

/**
 * Test Scenario 5: Themes ordered alphabetically
 * Verifies themes are returned in alphabetical order by name
 */
function testThemesOrdering() {
  console.log("🧪 Testing GET /api/themes - Alphabetical Ordering");
  
  const testSteps = [
    {
      step: 1,
      description: "Make GET request to /api/themes",
      action: "curl http://localhost:3001/api/themes",
      expectedResult: "Returns themes array"
    },
    {
      step: 2,
      description: "Verify themes are ordered alphabetically by name",
      action: "Check array order: Barber Theme, Clinic Theme, Default Theme",
      expectedResult: "Themes sorted alphabetically by name field"
    },
    {
      step: 3,
      description: "Add new theme with name starting with 'A'",
      action: "Create theme with name 'Advanced Theme'",
      expectedResult: "New theme appears first in list"
    }
  ];
  
  return {
    testName: "Themes Alphabetical Ordering",
    requirement: "9.1",
    steps: testSteps,
    validationCriteria: [
      "Themes are sorted alphabetically by name",
      "Ordering is consistent across requests",
      "New themes are inserted in correct alphabetical position"
    ]
  };
}

/**
 * Manual Test Execution Guide
 */
function generateTestExecutionGuide() {
  return {
    title: "GET /api/themes Endpoint Test Execution Guide",
    
    prerequisites: [
      "1. Ensure PostgreSQL database is running",
      "2. Ensure DATABASE_URL is configured in .env.local",
      "3. Run database migrations: pnpm prisma:migrate",
      "4. Seed database with themes: pnpm prisma:seed",
      "5. Start development server: pnpm dev"
    ],
    
    quickTest: {
      description: "Quick verification using curl",
      commands: [
        "# Test endpoint",
        "curl http://localhost:3001/api/themes",
        "",
        "# Expected response:",
        "[",
        "  {",
        "    \"id\": \"clxxx...\",",
        "    \"key\": \"barber\",",
        "    \"name\": \"Barber Theme\"",
        "  },",
        "  {",
        "    \"id\": \"clxxx...\",",
        "    \"key\": \"clinic\",",
        "    \"name\": \"Clinic Theme\"",
        "  },",
        "  {",
        "    \"id\": \"clxxx...\",",
        "    \"key\": \"default\",",
        "    \"name\": \"Default Theme\"",
        "  }",
        "]"
      ]
    },
    
    browserTest: {
      description: "Test using browser",
      steps: [
        "1. Open browser and navigate to http://localhost:3001/api/themes",
        "2. Verify JSON response is displayed",
        "3. Check response contains array of themes",
        "4. Verify each theme has id, key, and name fields",
        "5. Confirm themes are sorted alphabetically by name"
      ]
    },
    
    postmanTest: {
      description: "Test using Postman or similar API client",
      steps: [
        "1. Create new GET request",
        "2. Set URL to http://localhost:3001/api/themes",
        "3. Send request",
        "4. Verify status code is 200",
        "5. Verify response is JSON array",
        "6. Check response schema matches expected structure"
      ]
    },
    
    testExecution: [
      {
        test: "Fetch Published Themes",
        command: "curl http://localhost:3001/api/themes",
        expectedStatus: 200,
        expectedResponse: "Array of theme objects with id, key, name"
      },
      {
        test: "Response Fields",
        command: "curl http://localhost:3001/api/themes | jq '.[0] | keys'",
        expectedStatus: 200,
        expectedResponse: "[\"id\", \"key\", \"name\"]"
      },
      {
        test: "Published Themes Only",
        setup: "Create draft theme in database",
        command: "curl http://localhost:3001/api/themes | jq 'map(.key)'",
        expectedResponse: "Should not include draft themes"
      },
      {
        test: "Alphabetical Ordering",
        command: "curl http://localhost:3001/api/themes | jq 'map(.name)'",
        expectedResponse: "[\"Barber Theme\", \"Clinic Theme\", \"Default Theme\"]"
      }
    ],
    
    expectedResults: [
      "✅ Endpoint returns 200 OK",
      "✅ Response is valid JSON array",
      "✅ Each theme has id, key, and name fields",
      "✅ Only published themes are returned",
      "✅ Themes are sorted alphabetically by name",
      "✅ Database errors return 500 with error message"
    ],
    
    debuggingTips: [
      "Check server console for error logs",
      "Verify DATABASE_URL is correct in .env.local",
      "Ensure database is running and accessible",
      "Run pnpm prisma:studio to inspect database",
      "Check that themes table has published themes",
      "Use curl -v for verbose output including headers",
      "Use jq to format and filter JSON responses"
    ],
    
    commonIssues: [
      {
        issue: "404 Not Found",
        solution: "Verify development server is running on port 3001"
      },
      {
        issue: "500 Internal Server Error",
        solution: "Check database connection and ensure migrations are run"
      },
      {
        issue: "Empty array returned",
        solution: "Run pnpm prisma:seed to create themes"
      },
      {
        issue: "Missing fields in response",
        solution: "Check Prisma select clause in route.ts"
      }
    ]
  };
}

/**
 * Automated verification helper (can be run in Node.js)
 */
async function verifyEndpoint() {
  try {
    const response = await fetch(`${TEST_CONFIG.baseUrl}${TEST_CONFIG.endpoint}`);
    
    const results = {
      status: response.status,
      statusText: response.statusText,
      isSuccess: response.ok,
      data: null,
      validation: {
        isArray: false,
        hasExpectedFields: false,
        hasExpectedThemes: false,
        isOrdered: false
      }
    };
    
    if (response.ok) {
      results.data = await response.json();
      
      // Validate response
      results.validation.isArray = Array.isArray(results.data);
      
      if (results.validation.isArray && results.data.length > 0) {
        // Check fields
        const firstTheme = results.data[0];
        results.validation.hasExpectedFields = TEST_CONFIG.expectedFields.every(
          field => field in firstTheme
        );
        
        // Check expected themes
        const themeKeys = results.data.map(t => t.key);
        results.validation.hasExpectedThemes = TEST_CONFIG.expectedThemes.every(
          key => themeKeys.includes(key)
        );
        
        // Check ordering
        const names = results.data.map(t => t.name);
        const sortedNames = [...names].sort();
        results.validation.isOrdered = JSON.stringify(names) === JSON.stringify(sortedNames);
      }
    }
    
    console.log("🔍 Endpoint Verification Results:");
    console.log(`   Status: ${results.status} ${results.statusText}`);
    console.log(`   Success: ${results.isSuccess ? '✅' : '❌'}`);
    console.log(`   Is Array: ${results.validation.isArray ? '✅' : '❌'}`);
    console.log(`   Has Expected Fields: ${results.validation.hasExpectedFields ? '✅' : '❌'}`);
    console.log(`   Has Expected Themes: ${results.validation.hasExpectedThemes ? '✅' : '❌'}`);
    console.log(`   Is Ordered: ${results.validation.isOrdered ? '✅' : '❌'}`);
    
    if (results.data) {
      console.log(`   Themes Count: ${results.data.length}`);
      console.log(`   Themes: ${results.data.map(t => t.key).join(', ')}`);
    }
    
    return results;
  } catch (error) {
    console.error("❌ Error verifying endpoint:", error.message);
    return { error: error.message };
  }
}

// Export test functions
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    testFetchPublishedThemes,
    testResponseFields,
    testErrorHandling,
    testPublishedThemesOnly,
    testThemesOrdering,
    generateTestExecutionGuide,
    verifyEndpoint,
    TEST_CONFIG
  };
}

// Node.js execution
if (typeof require !== 'undefined' && require.main === module) {
  console.log("🧪 GET /api/themes Endpoint Tests");
  console.log("=====================================\n");
  
  const guide = generateTestExecutionGuide();
  console.log(guide.title);
  console.log("\nPrerequisites:");
  guide.prerequisites.forEach(p => console.log(`  ${p}`));
  
  console.log("\n\nQuick Test:");
  console.log(guide.quickTest.description);
  guide.quickTest.commands.forEach(c => console.log(c));
  
  console.log("\n\nTo run automated verification:");
  console.log("  node test/api-themes-endpoint.test.js verify");
  
  // Run verification if 'verify' argument is passed
  if (process.argv.includes('verify')) {
    console.log("\n\nRunning automated verification...\n");
    verifyEndpoint();
  }
}

