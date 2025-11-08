# Quick Test Checklist - Subdomain Routing

Use this checklist for rapid testing of subdomain routing functionality.

## ⚙️ Setup (One-time)

### 1. Configure Hosts File
```bash
sudo nano /etc/hosts
```
Add:
```
127.0.0.1 tenant-a.localhost
127.0.0.1 tenant-b.localhost
```

### 2. Create Test Tenants
```sql
INSERT INTO tenants (slug, name, status, created_at, updated_at)
VALUES 
  ('tenant-a', 'Tenant A Clinic', 'ACTIVE', NOW(), NOW()),
  ('tenant-b', 'Tenant B Clinic', 'ACTIVE', NOW(), NOW())
ON CONFLICT (slug) DO NOTHING;
```

### 3. Start Servers
```bash
# Terminal 1: API
cd apps/api && ./gradlew bootRun

# Terminal 2: Web App
cd apps/web-next && pnpm dev
```

### 4. Run Verification
```bash
cd apps/web-next && ./test-subdomain-routing.sh
```

## ✅ Quick Tests

### Test A: Tenant A
1. [ ] Navigate to `http://tenant-a.localhost:3001`
2. [ ] Open DevTools > Application > Cookies
3. [ ] Verify: `tenantSlug` = `tenant-a` ✓
4. [ ] Open DevTools > Network tab
5. [ ] Navigate to `/en/services`
6. [ ] Click API request > Headers
7. [ ] Verify: `x-tenant-slug: tenant-a` ✓

### Test B: Tenant B
1. [ ] Navigate to `http://tenant-b.localhost:3001`
2. [ ] Open DevTools > Application > Cookies
3. [ ] Verify: `tenantSlug` = `tenant-b` ✓
4. [ ] Open DevTools > Network tab
5. [ ] Navigate to `/en/services`
6. [ ] Click API request > Headers
7. [ ] Verify: `x-tenant-slug: tenant-b` ✓

### Test C: Switching
1. [ ] Start at `tenant-a.localhost:3001`
2. [ ] Verify cookie = `tenant-a` ✓
3. [ ] Switch to `tenant-b.localhost:3001`
4. [ ] Verify cookie = `tenant-b` ✓
5. [ ] Switch back to `tenant-a.localhost:3001`
6. [ ] Verify cookie = `tenant-a` ✓

### Test D: Data Isolation
1. [ ] View services on `tenant-a.localhost:3001/en/services`
2. [ ] Note services displayed: _______________
3. [ ] View services on `tenant-b.localhost:3001/en/services`
4. [ ] Note services displayed: _______________
5. [ ] Verify different data ✓

## 🔍 Browser Console Quick Check

```javascript
// Paste in console
window.SubdomainRoutingTests.verifyTenantContext();
```

Expected output:
```
🔍 Tenant Context Verification:
   Hostname: tenant-a.localhost
   Expected Tenant: tenant-a
   Cookie Value: tenant-a
   Status: ✅ CORRECT
```

## 📸 Required Screenshots

- [ ] Cookies panel showing `tenantSlug=tenant-a`
- [ ] Cookies panel showing `tenantSlug=tenant-b`
- [ ] Network headers showing `x-tenant-slug: tenant-a`
- [ ] Network headers showing `x-tenant-slug: tenant-b`

## ✅ Pass Criteria

All tests pass when:
- ✅ Cookie set correctly for each subdomain
- ✅ API headers match subdomain
- ✅ Switching works both directions
- ✅ Each tenant shows different data

## 🐛 Common Issues

| Issue | Solution |
|-------|----------|
| Subdomain doesn't resolve | Check `/etc/hosts`, flush DNS cache |
| Cookie not set | Check middleware, try incognito |
| Wrong header | Check `api.ts`, verify cookie |
| Same data on both | Check backend tenant filtering |

## 📚 Full Documentation

- **Detailed Guide:** `SUBDOMAIN_ROUTING_TEST_GUIDE.md`
- **Results Template:** `SUBDOMAIN_ROUTING_TEST_RESULTS.md`
- **Test Suite:** `test/subdomain-routing.test.js`
- **Quick Start:** `SUBDOMAIN_ROUTING_README.md`

---

**Time Required:** ~15 minutes  
**Difficulty:** Easy  
**Prerequisites:** Hosts file, database, servers running
