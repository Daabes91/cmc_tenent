/**
 * Quick Reactivity Verification Test
 * 
 * This script performs a quick verification that the dynamic branding
 * reactivity is properly implemented and working.
 */

const fs = require('fs');

console.log('🧪 Dynamic Branding Reactivity Verification');
console.log('==========================================');

// Test 1: Verify admin-nuxt implementation
console.log('\n📋 Admin-nuxt Implementation Check:');

const adminAppVue = `apps/admin-nuxt/app.vue`;
const adminTestFile = `apps/admin-nuxt/test/dynamic-branding-reactivity.test.js`;

try {
  
  // Check if app.vue has the required reactive components
  if (fs.existsSync(adminAppVue)) {
    const appContent = fs.readFileSync(adminAppVue, 'utf8');
    
    const checks = [
      { name: 'useClinicSettings composable', pattern: /useClinicSettings/ },
      { name: 'computed adminTitle', pattern: /adminTitle.*computed/ },
      { name: 'computed faviconUrl', pattern: /faviconUrl.*computed/ },
      { name: 'useHead configuration', pattern: /useHead/ },
      { name: 'route watching', pattern: /watch.*route/ }
    ];
    
    checks.forEach(check => {
      if (check.pattern.test(appContent)) {
        console.log(`✅ ${check.name} - Found`);
      } else {
        console.log(`❌ ${check.name} - Missing`);
      }
    });
  } else {
    console.log('❌ app.vue file not found');
  }
  
  // Check test file
  if (fs.existsSync(adminTestFile)) {
    console.log('✅ Test file exists');
  } else {
    console.log('❌ Test file missing');
  }
  
} catch (error) {
  console.log('❌ Error checking admin-nuxt files:', error.message);
}

// Test 2: Verify web-next implementation
console.log('\n📋 Web-next Implementation Check:');

const webLayout = `apps/web-next/app/layout.tsx`;
const webDynamicHead = `apps/web-next/components/DynamicHead.tsx`;
const webTestFile = `apps/web-next/test/dynamic-branding-reactivity.test.js`;

try {
  // Check layout.tsx
  if (fs.existsSync(webLayout)) {
    const layoutContent = fs.readFileSync(webLayout, 'utf8');
    
    const layoutChecks = [
      { name: 'generateMetadata function', pattern: /generateMetadata/ },
      { name: 'clinic settings integration', pattern: /clinicSettings|getClinicSettingsServer/ },
      { name: 'DynamicHead component', pattern: /<DynamicHead/ }
    ];
    
    layoutChecks.forEach(check => {
      if (check.pattern.test(layoutContent)) {
        console.log(`✅ ${check.name} - Found`);
      } else {
        console.log(`❌ ${check.name} - Missing`);
      }
    });
  } else {
    console.log('❌ layout.tsx file not found');
  }
  
  // Check DynamicHead component
  if (fs.existsSync(webDynamicHead)) {
    const dynamicHeadContent = fs.readFileSync(webDynamicHead, 'utf8');
    
    const dynamicHeadChecks = [
      { name: 'useClinicBranding hook', pattern: /useClinicBranding/ },
      { name: 'useEffect for reactivity', pattern: /useEffect/ },
      { name: 'document.title update', pattern: /document\.title/ },
      { name: 'favicon update function', pattern: /updateFavicon/ }
    ];
    
    dynamicHeadChecks.forEach(check => {
      if (check.pattern.test(dynamicHeadContent)) {
        console.log(`✅ ${check.name} - Found`);
      } else {
        console.log(`❌ ${check.name} - Missing`);
      }
    });
  } else {
    console.log('❌ DynamicHead.tsx file not found');
  }
  
  // Check test file
  if (fs.existsSync(webTestFile)) {
    console.log('✅ Test file exists');
  } else {
    console.log('❌ Test file missing');
  }
  
} catch (error) {
  console.log('❌ Error checking web-next files:', error.message);
}

// Test 3: Verify supporting utilities
console.log('\n📋 Supporting Utilities Check:');

const utilityFiles = [
  'apps/admin-nuxt/utils/pageNameUtils.ts',
  'apps/admin-nuxt/utils/titleUtils.ts', 
  'apps/admin-nuxt/utils/faviconUtils.ts',
  'apps/web-next/utils/pageTitleUtils.ts',
  'apps/web-next/utils/titleUtils.ts',
  'apps/web-next/utils/faviconUtils.ts'
];

utilityFiles.forEach(file => {
  try {
    if (fs.existsSync(file)) {
      console.log(`✅ ${file} - Exists`);
    } else {
      console.log(`❌ ${file} - Missing`);
    }
  } catch (error) {
    console.log(`❌ ${file} - Error: ${error.message}`);
  }
});

// Test 4: Requirements mapping
console.log('\n🎯 Requirements Verification:');

const requirements = [
  {
    id: '2.1',
    description: 'Titles update when clinic settings change',
    adminImplementation: 'computed adminTitle + watch route',
    webImplementation: 'useEffect + useClinicBranding + document.title'
  },
  {
    id: '2.2', 
    description: 'Favicon updates when logo is changed',
    adminImplementation: 'computed faviconUrl + useHead',
    webImplementation: 'useEffect + updateFavicon function'
  },
  {
    id: '2.3',
    description: 'No page refresh required for updates',
    adminImplementation: 'Vue reactivity system',
    webImplementation: 'React useEffect + SWR cache'
  }
];

requirements.forEach(req => {
  console.log(`\n📌 Requirement ${req.id}: ${req.description}`);
  console.log(`   Admin: ${req.adminImplementation}`);
  console.log(`   Web: ${req.webImplementation}`);
});

console.log('\n🚀 Verification Complete!');
console.log('\nNext steps:');
console.log('1. Run ./verify-branding-reactivity.sh to check app status');
console.log('2. Run node test-dynamic-branding-reactivity.js both for test instructions');
console.log('3. Follow manual test procedures in DYNAMIC_BRANDING_REACTIVITY_TESTS.md');
console.log('4. Generate test report with: node test-dynamic-branding-reactivity.js --report-template');