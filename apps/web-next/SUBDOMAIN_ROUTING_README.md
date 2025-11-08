# Subdomain Routing Testing - Quick Start

This directory contains comprehensive testing resources for the web app's subdomain routing functionality.

## 📁 Test Files

| File | Purpose |
|------|---------|
| `test/subdomain-routing.test.js` | Manual test suite with test scenarios and helper functions |
| `SUBDOMAIN_ROUTING_TEST_GUIDE.md` | Detailed step-by-step testing instructions |
| `SUBDOMAIN_ROUTING_TEST_RESULTS.md` | Template for documenting test results |
| `test-subdomain-routing.sh` | Automated setup verification script |

## 🚀 Quick Start

### 1. Run Setup Verification

```bash
cd apps/web-next
./test-subdomain-routing.sh
```

This script will verify:
- ✅ Web-next is running on port 3001
- ✅ API is running on port 8080
- ✅ Hosts file is configured
- ✅ Subdomains resolve correctly
- ✅ Test tenants exist in database

### 2. Manual Browser Testing

1. **Open browser** and navigate to `http://tenant-a.localhost:3001`

2. **Check Cookie:**
   - Open DevTools (F12)
   - Go to Application > Cookies > tenant-a.localhost
   - Verify `tenantSlug` = `tenant-a`

3. **Check API Headers:**
   - Open DevTools > Network tab
   - Navigate to `/en/services`
   - Click on API request
   - Verify `x-tenant-slug: tenant-a` in Request Headers

4. **Test Tenant B:**
   - Navigate to `http://tenant-b.localhost:3001`
   - Verify cookie updated to `tenant-b`
   - Verify API headers show `x-tenant-slug: tenant-b`

### 3. Browser Console Testing

Open browser console and run:

```javascript
// Load test suite (if not auto-loaded)
const script = document.createElement('script');
script.src = '/test/subdomain-routing.test.js';
document.head.appendChild(script);

// Verify current tenant context
window.SubdomainRoutingTests.verifyTenantContext();

// Get test execution guide
window.SubdomainRoutingTests.generateTestExecutionGuide();

// Check API request headers
window.SubdomainRoutingTests.checkLastAPIRequestHeaders();
```

## 📋 Test Scenarios

The test suite covers 8 comprehensive scenarios:

1. ✅ **Tenant A Cookie Set** - Verify cookie is set to "tenant-a"
2. ✅ **Tenant A API Headers** - Verify API requests include X-Tenant-Slug: tenant-a
3. ✅ **Tenant B Cookie Set** - Verify cookie updates to "tenant-b"
4. ✅ **Tenant B API Headers** - Verify API requests include X-Tenant-Slug: tenant-b
5. ✅ **Tenant Switching** - Verify context switches properly between subdomains
6. ✅ **Tenant Data Isolation** - Verify each tenant sees only their own data
7. ✅ **Tenant Resolution Priority** - Test query param > subdomain > cookie > default
8. ✅ **Authenticated Requests** - Verify tenant header with auth token

## 🔧 Prerequisites Setup

### Hosts File Configuration

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

### Database Setup

```sql
-- Create test tenants
INSERT INTO tenants (slug, name, status, created_at, updated_at)
VALUES 
  ('tenant-a', 'Tenant A Clinic', 'ACTIVE', NOW(), NOW()),
  ('tenant-b', 'Tenant B Clinic', 'ACTIVE', NOW(), NOW())
ON CONFLICT (slug) DO NOTHING;
```

### Start Development Servers

```bash
# Terminal 1: API
cd apps/api
./gradlew bootRun

# Terminal 2: Web App
cd apps/web-next
pnpm dev
```

## 📊 Expected Results

All tests should pass with:
- ✅ Cookie `tenantSlug` set correctly for each subdomain
- ✅ API requests include `x-tenant-slug` header matching subdomain
- ✅ Tenant context switches properly between subdomains
- ✅ Each tenant displays only their own data
- ✅ No cross-tenant data leakage
- ✅ Tenant resolution priority works correctly
- ✅ Authenticated requests include both tenant header and auth token

## 🐛 Troubleshooting

### Subdomain doesn't resolve
```bash
# Verify hosts file
cat /etc/hosts | grep localhost

# Flush DNS cache
# macOS:
sudo dscacheutil -flushcache

# Linux:
sudo systemd-resolve --flush-caches

# Windows:
ipconfig /flushdns
```

### Cookie not set
- Check middleware is running
- Verify cookie settings in `middleware.ts`
- Try incognito/private window
- Check browser console for errors

### API header missing
- Verify `getTenantSlugClient()` in `lib/tenant.ts`
- Check `api.ts` includes tenant header logic
- Verify cookie is set correctly
- Check Network tab for actual requests

### Wrong tenant data displayed
- Verify API backend filters by `tenant_id`
- Check database has correct tenant data
- Verify tenant resolution in backend logs
- Check API response in Network tab

## 📝 Documentation

For detailed information, see:

- **[SUBDOMAIN_ROUTING_TEST_GUIDE.md](./SUBDOMAIN_ROUTING_TEST_GUIDE.md)** - Complete testing instructions
- **[SUBDOMAIN_ROUTING_TEST_RESULTS.md](./SUBDOMAIN_ROUTING_TEST_RESULTS.md)** - Results template
- **[test/subdomain-routing.test.js](./test/subdomain-routing.test.js)** - Test suite code

## 🎯 Success Criteria

Task 38 is complete when:
- [x] Test suite created with all scenarios
- [x] Test guide documentation written
- [x] Test results template created
- [x] Setup verification script created
- [ ] All manual tests executed and passed
- [ ] Test results documented
- [ ] Screenshots captured
- [ ] Any issues reported and resolved

## 📸 Required Screenshots

Capture and save:
1. DevTools > Application > Cookies showing `tenantSlug` for tenant-a
2. DevTools > Application > Cookies showing `tenantSlug` for tenant-b
3. DevTools > Network > Request Headers showing `x-tenant-slug: tenant-a`
4. DevTools > Network > Request Headers showing `x-tenant-slug: tenant-b`
5. Different services/doctors displayed on tenant-a vs tenant-b

## 🔗 Related Files

- `middleware.ts` - Tenant resolution middleware
- `lib/tenant.ts` - Tenant utility functions
- `lib/api.ts` - API client with tenant header injection
- `.kiro/specs/complete-multi-tenant-implementation/tasks.md` - Task 38

## ✅ Next Steps

After completing all tests:
1. Fill out `SUBDOMAIN_ROUTING_TEST_RESULTS.md`
2. Attach screenshots
3. Mark task 38 as complete in tasks.md
4. Proceed to task 39 (Performance testing)

---

**Need Help?**
- Check the troubleshooting section above
- Review the detailed test guide
- Check browser console for errors
- Verify all prerequisites are met
