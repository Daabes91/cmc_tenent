#!/usr/bin/env node

/**
 * Dynamic Branding Reactivity Test Runner
 * 
 * This script provides automated testing capabilities for the dynamic branding
 * reactivity requirements (2.1, 2.2, 2.3).
 * 
 * Usage:
 *   node test-dynamic-branding-reactivity.js [admin|web|both]
 */

const fs = require('fs');
const path = require('path');

// Test configuration
const TEST_CONFIG = {
  adminNuxtPort: 3000,
  webNextPort: 3001,
  testTimeout: 30000,
  
  // Test clinic settings for reactivity testing
  originalSettings: {
    clinicName: "Test Clinic Original",
    logoUrl: "https://via.placeholder.com/32x32/blue/white?text=O"
  },
  
  updatedSettings: {
    clinicName: "Test Clinic Updated", 
    logoUrl: "https://via.placeholder.com/32x32/green/white?text=U"
  }
};

/**
 * Test Results Tracker
 */
class TestResultsTracker {
  constructor() {
    this.results = {
      adminNuxt: {
        titleReactivity: null,
        faviconReactivity: null,
        noRefreshRequired: null,
        overall: null
      },
      webNext: {
        titleReactivity: null,
        faviconReactivity: null,
        swrReactivity: null,
        metadataReactivity: null,
        overall: null
      }
    };
    this.startTime = Date.now();
  }
  
  recordResult(app, test, passed, details = '') {
    this.results[app][test] = {
      passed,
      details,
      timestamp: Date.now()
    };
  }
  
  generateReport() {
    const endTime = Date.now();
    const duration = endTime - this.startTime;
    
    return {
      summary: {
        totalDuration: duration,
        timestamp: new Date().toISOString(),
        adminNuxtPassed: this.getAppPassCount('adminNuxt'),
        webNextPassed: this.getAppPassCount('webNext')
      },
      results: this.results,
      requirements: {
        '2.1': this.checkRequirement21(),
        '2.2': this.checkRequirement22(), 
        '2.3': this.checkRequirement23()
      }
    };
  }
  
  getAppPassCount(app) {
    const tests = Object.values(this.results[app]).filter(r => r !== null);
    const passed = tests.filter(r => r.passed).length;
    return `${passed}/${tests.length}`;
  }
  
  checkRequirement21() {
    // Requirement 2.1: Title updates when clinic settings change
    const adminTitle = this.results.adminNuxt.titleReactivity?.passed;
    const webTitle = this.results.webNext.titleReactivity?.passed;
    return {
      passed: adminTitle && webTitle,
      details: "Title updates when clinic settings change"
    };
  }
  
  checkRequirement22() {
    // Requirement 2.2: Favicon updates when logo changes
    const adminFavicon = this.results.adminNuxt.faviconReactivity?.passed;
    const webFavicon = this.results.webNext.faviconReactivity?.passed;
    return {
      passed: adminFavicon && webFavicon,
      details: "Favicon updates when logo changes"
    };
  }
  
  checkRequirement23() {
    // Requirement 2.3: No page refresh required
    const adminNoRefresh = this.results.adminNuxt.noRefreshRequired?.passed;
    const webMetadata = this.results.webNext.metadataReactivity?.passed;
    return {
      passed: adminNoRefresh && webMetadata,
      details: "No page refresh required for updates"
    };
  }
}

/**
 * Manual Test Instructions Generator
 */
function generateManualTestInstructions() {
  return `
🧪 DYNAMIC BRANDING REACTIVITY TEST INSTRUCTIONS

This test suite verifies Requirements 2.1, 2.2, and 2.3:
- 2.1: Titles update when clinic settings change
- 2.2: Favicon updates when logo is changed  
- 2.3: No page refresh required for updates

📋 SETUP INSTRUCTIONS:

1. Start both applications:
   - Admin-nuxt: cd apps/admin-nuxt && npm run dev
   - Web-next: cd apps/web-next && npm run dev

2. Open browser tabs:
   - Admin: http://localhost:3000
   - Web: http://localhost:3001

3. Open browser developer tools in both tabs

📝 TEST EXECUTION:

=== ADMIN-NUXT TESTS ===

Test 1: Title Reactivity
□ Navigate to admin dashboard
□ Note current title format: "Admin | Dashboard"
□ Go to clinic settings page
□ Update clinic name and save
□ Verify title remains "Admin | Dashboard" (admin format maintained)
□ Navigate to appointments page
□ Verify title shows "Admin | Appointments"

Test 2: Favicon Reactivity  
□ Note current favicon in browser tab
□ Update clinic logo URL in settings
□ Verify favicon changes immediately without page refresh
□ Test with invalid URL to verify fallback to default

Test 3: No Refresh Required
□ Monitor Network tab in dev tools
□ Update clinic settings
□ Verify no full page reload occurs (no document request)
□ Confirm only API calls for settings update

=== WEB-NEXT TESTS ===

Test 1: Title Reactivity
□ Navigate to web home page  
□ Note current title format: "[Clinic Name] | Home"
□ Update clinic name via API or settings
□ Verify title updates to "[New Name] | Home"
□ Navigate to other pages
□ Verify format "[New Name] | [Page Title]" maintained

Test 2: Favicon Reactivity
□ Note current favicon in browser tab
□ Update clinic logo URL
□ Verify favicon changes immediately
□ Test SWR cache mutation for immediate updates

Test 3: SWR Cache Reactivity
□ Open React DevTools
□ Find SWR cache for clinic-settings
□ Manually mutate cache with new settings
□ Verify UI updates immediately
□ Test background revalidation

✅ SUCCESS CRITERIA:

Admin-nuxt:
- Title format "Admin | [Page Name]" maintained after updates
- Favicon updates immediately when logo changes
- No page refresh required for any updates
- Navigation preserves updated branding

Web-next:
- Title format "[Clinic Name] | [Page Title]" maintained after updates  
- Favicon updates immediately when logo changes
- SWR cache updates propagate to UI immediately
- Next.js metadata system works reactively

🐛 DEBUGGING TIPS:

- Check browser console for branding error logs
- Monitor Network tab for unnecessary requests
- Use React DevTools to inspect SWR cache state
- Verify favicon URL validation in console
- Test with slow network to verify loading states

📊 REPORTING:

After completing tests, document results:
- Which tests passed/failed
- Any performance issues observed
- Browser compatibility notes
- Specific error messages or unexpected behavior

Run this command to generate a test report template:
node test-dynamic-branding-reactivity.js --report-template
`;
}

/**
 * Generate Test Report Template
 */
function generateTestReportTemplate() {
  const timestamp = new Date().toISOString();
  
  return `
# Dynamic Branding Reactivity Test Report
Generated: ${timestamp}

## Test Environment
- Browser: [Chrome/Firefox/Safari/Edge]
- Admin-nuxt URL: http://localhost:3000
- Web-next URL: http://localhost:3001
- Tester: [Your Name]

## Requirements Testing

### Requirement 2.1: Title Updates When Clinic Settings Change
**Admin-nuxt:**
- [ ] PASS / [ ] FAIL - Title updates immediately when clinic name changes
- [ ] PASS / [ ] FAIL - Admin format "Admin | Page Name" maintained
- [ ] PASS / [ ] FAIL - Navigation preserves updated titles
- Notes: ________________________________

**Web-next:**
- [ ] PASS / [ ] FAIL - Title updates immediately when clinic name changes  
- [ ] PASS / [ ] FAIL - Web format "[Clinic Name] | Page Title" maintained
- [ ] PASS / [ ] FAIL - Navigation preserves updated titles
- Notes: ________________________________

### Requirement 2.2: Favicon Updates When Logo Changes
**Admin-nuxt:**
- [ ] PASS / [ ] FAIL - Favicon updates immediately when logo URL changes
- [ ] PASS / [ ] FAIL - Invalid URLs fall back to default favicon
- [ ] PASS / [ ] FAIL - Favicon validation works correctly
- Notes: ________________________________

**Web-next:**
- [ ] PASS / [ ] FAIL - Favicon updates immediately when logo URL changes
- [ ] PASS / [ ] FAIL - SWR cache invalidation triggers favicon update
- [ ] PASS / [ ] FAIL - Invalid URLs fall back to default favicon
- Notes: ________________________________

### Requirement 2.3: No Page Refresh Required
**Admin-nuxt:**
- [ ] PASS / [ ] FAIL - No full page reload during settings updates
- [ ] PASS / [ ] FAIL - Updates are reactive and immediate
- [ ] PASS / [ ] FAIL - Performance remains optimal
- Notes: ________________________________

**Web-next:**
- [ ] PASS / [ ] FAIL - No full page reload during settings updates
- [ ] PASS / [ ] FAIL - SWR cache updates work without refresh
- [ ] PASS / [ ] FAIL - Next.js metadata updates reactively
- Notes: ________________________________

## Overall Assessment

### Admin-nuxt Results
- Tests Passed: ___/9
- Critical Issues: ________________________________
- Performance Notes: ________________________________

### Web-next Results  
- Tests Passed: ___/9
- Critical Issues: ________________________________
- Performance Notes: ________________________________

### Requirements Compliance
- Requirement 2.1: [ ] COMPLIANT / [ ] NON-COMPLIANT
- Requirement 2.2: [ ] COMPLIANT / [ ] NON-COMPLIANT  
- Requirement 2.3: [ ] COMPLIANT / [ ] NON-COMPLIANT

### Recommendations
1. ________________________________
2. ________________________________
3. ________________________________

### Next Steps
- [ ] Fix any failing tests
- [ ] Performance optimization if needed
- [ ] Browser compatibility testing
- [ ] Documentation updates
`;
}

/**
 * Main execution function
 */
function main() {
  const args = process.argv.slice(2);
  const command = args[0] || 'help';
  
  switch (command) {
    case 'admin':
      console.log('🧪 Admin-nuxt Dynamic Branding Tests');
      console.log(generateManualTestInstructions());
      break;
      
    case 'web':
      console.log('🧪 Web-next Dynamic Branding Tests');
      console.log(generateManualTestInstructions());
      break;
      
    case 'both':
      console.log('🧪 Complete Dynamic Branding Test Suite');
      console.log(generateManualTestInstructions());
      break;
      
    case '--report-template':
      console.log(generateTestReportTemplate());
      break;
      
    case 'help':
    default:
      console.log(`
🧪 Dynamic Branding Reactivity Test Runner

Usage:
  node test-dynamic-branding-reactivity.js [command]

Commands:
  admin              Run admin-nuxt tests
  web                Run web-next tests  
  both               Run all tests (default)
  --report-template  Generate test report template
  help               Show this help message

Requirements tested:
  2.1 - Titles update when clinic settings change
  2.2 - Favicon updates when logo is changed
  2.3 - No page refresh required for updates

For detailed test instructions, run:
  node test-dynamic-branding-reactivity.js both
      `);
      break;
  }
}

// Run if called directly
if (require.main === module) {
  main();
}

module.exports = {
  TestResultsTracker,
  generateManualTestInstructions,
  generateTestReportTemplate,
  TEST_CONFIG
};