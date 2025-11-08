# Web App Subdomain Routing Test Guide

This guide provides step-by-step instructions for testing subdomain routing functionality in the web-next application.

## Overview

The web app uses subdomain-based tenant routing where:
- Each tenant has a unique subdomain (e.g., `tenant-a.localhost`, `tenant-b.localhost`)
- The middleware automatically detects the subdomain and sets the tenant context
- A cookie (`tenantSlug`) stores the current tenant
- All API requests include the `X-Tenant-Slug` header

## Prerequisites

### 1. Start Development Servers

```bash
# Terminal 1: Start API backend
cd apps/api
./gradlew bootRun

# Terminal 2: Start web-next frontend
cd apps/web-next
pnpm dev
```

The web app should be running on `http://localhost:3001`

### 2. Configure Hosts File

Add subdomain entries to your hosts file:

**macOS/Linux:**
```bash
sudo nano /etc/hosts
```

**Windows:**
```
notepad C:\Windows\System32\drivers\etc\hosts
```
(Run as Administrator)

**Add these lines:**
```
127.0.0.1 tenant-a.localhost
127.0.0.1 tenant-b.localhost
```

Save and close the file.

### 3. Create Test Tenants in Database

Connect to your PostgreSQL database and run:

```sql
-- Create tenant-a
INSERT INTO tenants (slug, name, status, created_at, updated_at)
VALUES ('tenant-a', 'Tenant A Clinic', 'ACTIVE', NOW(), NOW())
ON CONFLICT (slug) DO NOTHING;

-- Create tenant-b
INSERT INTO tenants (slug, name, status, created_at, updated_at)
VALUES ('tenant-b', 'Tenant B Clinic', 'ACTIVE', NOW(), NOW())
ON CONFLICT (slug) DO NOTHING;
```

### 4. Add Test Data (Optional but Recommended)

Add some test services and doctors to each tenant so you can verify data isolation:

```sql
-- Get tenant IDs
SELECT id, slug FROM tenants WHERE slug IN ('tenant-a', 'tenant-b');

-- Add test service to tenant-a (replace <tenant_a_id> with actual ID)
INSERT INTO clinic_services (tenant_id, slug, name_en, name_ar, description_en, description_ar, price, currency, duration_minutes, is_active, created_at, updated_at)
VALUES (<tenant_a_id>, 'test-service-a', 'Test Service A', 'خدمة اختبار أ', 'Test service for tenant A', 'خدمة اختبار للمستأجر أ', 100.00, 'USD', 30, true, NOW(), NOW());

-- Add test service to tenant-b (replace <tenant_b_id> with actual ID)
INSERT INTO clinic_services (tenant_id, slug, name_en, name_ar, description_en, description_ar, price, currency, duration_minutes, is_active, created_at, updated_at)
VALUES (<tenant_b_id>, 'test-service-b', 'Test Service B', 'خدمة اختبار ب', 'Test service for tenant B', 'خدمة اختبار للمستأجر ب', 150.00, 'USD', 45, true, NOW(), NOW());
```

## Test Scenarios

### Test 1: Access tenant-a.localhost and Verify Cookie

**Steps:**

1. Open your browser (Chrome, Firefox, or Safari)
2. Navigate to: `http://tenant-a.localhost:3001`
3. Open DevTools (F12 or Cmd+Option+I on Mac)
4. Go to **Application** tab > **Cookies** > `tenant-a.localhost`
5. Look for a cookie named `tenantSlug`

**Expected Results:**
- ✅ Cookie `tenantSlug` exists
- ✅ Cookie value is `tenant-a`
- ✅ Cookie path is `/`
- ✅ Cookie SameSite is `Lax`

**Verification:**
```javascript
// Run in browser console
document.cookie.split(';').find(c => c.trim().startsWith('tenantSlug='))
// Should output: "tenantSlug=tenant-a"
```

### Test 2: Verify API Requests Include X-Tenant-Slug: tenant-a

**Steps:**

1. Stay on `http://tenant-a.localhost:3001`
2. Open DevTools > **Network** tab
3. Clear network log (🚫 icon)
4. Navigate to a page that makes API calls (e.g., `/en/services` or `/en/doctors`)
5. In the Network tab, filter by your API domain (e.g., `localhost:8080`)
6. Click on any API request (e.g., `GET /public/services`)
7. Go to the **Headers** tab
8. Scroll to **Request Headers** section

**Expected Results:**
- ✅ Header `x-tenant-slug: tenant-a` is present
- ✅ All API requests include this header
- ✅ Header value matches the subdomain

**Screenshot Location:**
Look for `x-tenant-slug` in the Request Headers section.

### Test 3: Access tenant-b.localhost and Verify Cookie Update

**Steps:**

1. Navigate to: `http://tenant-b.localhost:3001`
2. Open DevTools > **Application** tab > **Cookies** > `tenant-b.localhost`
3. Look for the `tenantSlug` cookie

**Expected Results:**
- ✅ Cookie `tenantSlug` exists
- ✅ Cookie value is now `tenant-b` (updated from `tenant-a`)
- ✅ Cookie path is `/`
- ✅ Cookie SameSite is `Lax`

**Verification:**
```javascript
// Run in browser console
document.cookie.split(';').find(c => c.trim().startsWith('tenantSlug='))
// Should output: "tenantSlug=tenant-b"
```

### Test 4: Verify API Requests Include X-Tenant-Slug: tenant-b

**Steps:**

1. Stay on `http://tenant-b.localhost:3001`
2. Open DevTools > **Network** tab
3. Clear network log
4. Navigate to `/en/services` or `/en/doctors`
5. Click on any API request
6. Check **Request Headers**

**Expected Results:**
- ✅ Header `x-tenant-slug: tenant-b` is present
- ✅ All API requests include this header
- ✅ Header value matches the subdomain

### Test 5: Tenant Switching

**Steps:**

1. Start at `http://tenant-a.localhost:3001`
2. Verify cookie = `tenant-a` and API headers = `x-tenant-slug: tenant-a`
3. Navigate to `http://tenant-b.localhost:3001`
4. Verify cookie updated to `tenant-b` and API headers = `x-tenant-slug: tenant-b`
5. Navigate back to `http://tenant-a.localhost:3001`
6. Verify cookie updated back to `tenant-a` and API headers = `x-tenant-slug: tenant-a`

**Expected Results:**
- ✅ Cookie updates automatically when switching subdomains
- ✅ API headers update to match current subdomain
- ✅ No manual refresh needed
- ✅ Switching works in both directions

### Test 6: Tenant Data Isolation

**Steps:**

1. Navigate to `http://tenant-a.localhost:3001/en/services`
2. Note the services displayed (should be tenant-a services)
3. Navigate to `http://tenant-b.localhost:3001/en/services`
4. Note the services displayed (should be tenant-b services)
5. Verify the services are different

**Expected Results:**
- ✅ Tenant A shows only tenant-a services
- ✅ Tenant B shows only tenant-b services
- ✅ No cross-tenant data leakage
- ✅ Each tenant has isolated data

### Test 7: Tenant Resolution Priority

Test the priority order: query param > subdomain > cookie > default

**Test 7a: Subdomain Resolution**
```
Navigate to: http://tenant-a.localhost:3001
Expected: tenant = tenant-a
```

**Test 7b: Query Parameter Override**
```
Navigate to: http://tenant-a.localhost:3001?tenant=tenant-b
Expected: tenant = tenant-b (query param overrides subdomain)
Cookie should be updated to tenant-b
```

**Test 7c: Cookie Fallback**
```
1. Navigate to tenant-a.localhost:3001 (sets cookie to tenant-a)
2. Navigate to localhost:3001 (no subdomain)
Expected: tenant = tenant-a (from cookie)
```

**Test 7d: Default Fallback**
```
1. Clear all cookies
2. Navigate to localhost:3001
Expected: tenant = default (fallback)
```

### Test 8: Authenticated Requests with Tenant Context

**Steps:**

1. Navigate to `http://tenant-a.localhost:3001/en/login`
2. Login with test credentials
3. Navigate to dashboard or profile page
4. Open DevTools > Network tab
5. Check authenticated API requests (e.g., `/patient/me`, `/appointments/my`)
6. Verify headers include both:
   - `Authorization: Bearer <token>`
   - `x-tenant-slug: tenant-a`
7. Switch to `http://tenant-b.localhost:3001`
8. Check API requests now include:
   - `Authorization: Bearer <token>` (same token)
   - `x-tenant-slug: tenant-b` (updated tenant)

**Expected Results:**
- ✅ Authenticated requests include both auth token and tenant header
- ✅ Tenant header updates when switching subdomains
- ✅ Auth token persists across tenant switches
- ✅ API returns correct tenant-scoped data

## Automated Verification

You can use the test helper functions in the browser console:

### Load Test Suite

1. Open browser console (F12)
2. Load the test file:
```javascript
// The test suite is automatically loaded if you include the script
// Or manually load it:
const script = document.createElement('script');
script.src = '/test/subdomain-routing.test.js';
document.head.appendChild(script);
```

### Run Verification

```javascript
// Check current tenant context
window.SubdomainRoutingTests.verifyTenantContext();

// Get test execution guide
window.SubdomainRoutingTests.generateTestExecutionGuide();

// Get instructions for checking API headers
window.SubdomainRoutingTests.checkLastAPIRequestHeaders();
```

## Troubleshooting

### Issue: Subdomain doesn't resolve

**Solution:**
- Verify `/etc/hosts` file includes subdomain entries
- Try `ping tenant-a.localhost` to verify DNS resolution
- Restart browser after modifying hosts file
- Clear DNS cache:
  - macOS: `sudo dscacheutil -flushcache`
  - Windows: `ipconfig /flushdns`
  - Linux: `sudo systemd-resolve --flush-caches`

### Issue: Cookie not set

**Solution:**
- Check middleware is running (look for middleware logs)
- Verify cookie settings in middleware.ts
- Check browser cookie settings (allow cookies)
- Try incognito/private window

### Issue: API header missing

**Solution:**
- Verify `getTenantSlugClient()` is working
- Check `api.ts` includes tenant header logic
- Verify cookie is set correctly
- Check browser console for errors

### Issue: Wrong tenant data displayed

**Solution:**
- Verify API backend is filtering by `tenant_id`
- Check database has correct tenant data
- Verify tenant resolution in backend logs
- Check API response in Network tab

### Issue: Tenant doesn't switch

**Solution:**
- Clear all cookies and try again
- Verify middleware updates cookie on navigation
- Check for JavaScript errors in console
- Verify subdomain is correct in URL bar

## Test Results Documentation

After completing all tests, document your results:

### Test Summary

| Test | Status | Notes |
|------|--------|-------|
| Tenant A Cookie Set | ✅/❌ | |
| Tenant A API Headers | ✅/❌ | |
| Tenant B Cookie Set | ✅/❌ | |
| Tenant B API Headers | ✅/❌ | |
| Tenant Switching | ✅/❌ | |
| Tenant Data Isolation | ✅/❌ | |
| Tenant Resolution Priority | ✅/❌ | |
| Authenticated Requests | ✅/❌ | |

### Screenshots

Capture screenshots of:
1. DevTools > Application > Cookies showing `tenantSlug` cookie
2. DevTools > Network > Request Headers showing `x-tenant-slug` header
3. Different data displayed on tenant-a vs tenant-b

### Issues Found

Document any issues discovered during testing:
- Issue description
- Steps to reproduce
- Expected vs actual behavior
- Screenshots/logs

## Success Criteria

All tests pass when:
- ✅ Cookie `tenantSlug` is set correctly for each subdomain
- ✅ API requests include `x-tenant-slug` header matching subdomain
- ✅ Tenant context switches properly between subdomains
- ✅ Each tenant displays only their own data
- ✅ No cross-tenant data leakage
- ✅ Tenant resolution priority works correctly
- ✅ Authenticated requests include both tenant header and auth token
- ✅ Cookie and header values are consistent

## Next Steps

After completing these tests:
1. Document results in `SUBDOMAIN_ROUTING_TEST_RESULTS.md`
2. Mark task 38 as complete in tasks.md
3. Proceed to task 39 (Performance testing) if all tests pass
4. Report any issues found to the development team
