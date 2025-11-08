# DevTools Visual Guide - Subdomain Routing Testing

This guide shows you exactly what to look for in Chrome/Firefox DevTools when testing subdomain routing.

## 🍪 Checking Cookies

### Step 1: Open DevTools
- **Chrome/Edge:** Press `F12` or `Cmd+Option+I` (Mac) / `Ctrl+Shift+I` (Windows)
- **Firefox:** Press `F12` or `Cmd+Option+I` (Mac) / `Ctrl+Shift+I` (Windows)
- **Safari:** Enable Developer menu first, then `Cmd+Option+I`

### Step 2: Navigate to Cookies Panel

**Chrome/Edge:**
```
DevTools > Application tab > Storage > Cookies > tenant-a.localhost
```

**Firefox:**
```
DevTools > Storage tab > Cookies > tenant-a.localhost
```

**Safari:**
```
DevTools > Storage tab > Cookies > tenant-a.localhost
```

### Step 3: What to Look For

You should see a table with cookies. Look for:

```
Name          | Value      | Domain              | Path | SameSite
------------- | ---------- | ------------------- | ---- | --------
tenantSlug    | tenant-a   | tenant-a.localhost  | /    | Lax
```

**✅ PASS Criteria:**
- Cookie name is exactly `tenantSlug`
- Cookie value matches subdomain (e.g., `tenant-a` for tenant-a.localhost)
- Path is `/`
- SameSite is `Lax`

**❌ FAIL Indicators:**
- Cookie doesn't exist
- Cookie value is wrong (e.g., `default` instead of `tenant-a`)
- Cookie value doesn't match subdomain

### Visual Example

```
┌─────────────────────────────────────────────────────────────┐
│ Application                                                  │
├─────────────────────────────────────────────────────────────┤
│ Storage                                                      │
│   ▼ Cookies                                                  │
│     ▶ http://localhost:3001                                  │
│     ▼ http://tenant-a.localhost:3001  ← Click here          │
│     ▶ http://tenant-b.localhost:3001                         │
│                                                              │
│ Cookies (1)                                                  │
│ ┌──────────┬──────────┬────────────────────┬──────┬────────┐│
│ │ Name     │ Value    │ Domain             │ Path │SameSite││
│ ├──────────┼──────────┼────────────────────┼──────┼────────┤│
│ │tenantSlug│tenant-a  │tenant-a.localhost  │ /    │ Lax    ││ ← Look here
│ └──────────┴──────────┴────────────────────┴──────┴────────┘│
└─────────────────────────────────────────────────────────────┘
```

## 📡 Checking API Request Headers

### Step 1: Open Network Tab

**All Browsers:**
```
DevTools > Network tab
```

### Step 2: Clear and Trigger Requests

1. Click the **Clear** button (🚫 icon) to clear existing requests
2. Navigate to a page that makes API calls:
   - `/en/services`
   - `/en/doctors`
   - `/en/blog`

### Step 3: Filter API Requests

In the filter box, type:
```
localhost:8080
```
or your API domain to show only API requests.

### Step 4: Inspect Request Headers

1. Click on any API request (e.g., `GET /public/services`)
2. Click the **Headers** tab
3. Scroll to **Request Headers** section

### Step 5: What to Look For

In the Request Headers section, you should see:

```
Request Headers
  Accept: application/json
  Authorization: Bearer eyJ... (if authenticated)
  Content-Type: application/json
  x-tenant-slug: tenant-a  ← Look for this!
```

**✅ PASS Criteria:**
- Header `x-tenant-slug` exists
- Header value matches subdomain (e.g., `tenant-a` for tenant-a.localhost)
- Header is present in ALL API requests

**❌ FAIL Indicators:**
- Header `x-tenant-slug` is missing
- Header value is wrong (e.g., `default` instead of `tenant-a`)
- Header value doesn't match subdomain

### Visual Example

```
┌─────────────────────────────────────────────────────────────┐
│ Network                                                      │
├─────────────────────────────────────────────────────────────┤
│ Filter: localhost:8080                                       │
│                                                              │
│ Name                    Status  Type    Size    Time         │
│ ─────────────────────────────────────────────────────────── │
│ ▶ services              200     xhr     2.1 KB  45 ms  ← Click│
│ ▶ doctors               200     xhr     3.4 KB  52 ms        │
│ ▶ settings              200     xhr     1.2 KB  38 ms        │
│                                                              │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Headers  Preview  Response  Timing                      │ │
│ ├─────────────────────────────────────────────────────────┤ │
│ │ General                                                 │ │
│ │   Request URL: http://localhost:8080/public/services   │ │
│ │   Request Method: GET                                   │ │
│ │   Status Code: 200 OK                                   │ │
│ │                                                         │ │
│ │ Request Headers                                         │ │
│ │   Accept: application/json                              │ │
│ │   Content-Type: application/json                        │ │
│ │   x-tenant-slug: tenant-a  ← Look for this!            │ │ ✅
│ │   User-Agent: Mozilla/5.0...                            │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🔄 Testing Tenant Switching

### Visual Flow

```
Step 1: tenant-a.localhost:3001
┌──────────────────────────┐
│ Cookie: tenant-a         │ ✅
│ Header: x-tenant-slug: tenant-a │ ✅
└──────────────────────────┘

        ↓ Navigate to tenant-b.localhost:3001

Step 2: tenant-b.localhost:3001
┌──────────────────────────┐
│ Cookie: tenant-b         │ ✅ (Updated!)
│ Header: x-tenant-slug: tenant-b │ ✅ (Updated!)
└──────────────────────────┘

        ↓ Navigate back to tenant-a.localhost:3001

Step 3: tenant-a.localhost:3001
┌──────────────────────────┐
│ Cookie: tenant-a         │ ✅ (Updated back!)
│ Header: x-tenant-slug: tenant-a │ ✅ (Updated back!)
└──────────────────────────┘
```

## 📊 Data Isolation Visual Check

### Tenant A Services
```
http://tenant-a.localhost:3001/en/services

┌─────────────────────────────────────┐
│ Services                            │
├─────────────────────────────────────┤
│ • Dental Cleaning - $100            │
│ • Root Canal - $500                 │
│ • Teeth Whitening - $200            │
└─────────────────────────────────────┘
```

### Tenant B Services (Different!)
```
http://tenant-b.localhost:3001/en/services

┌─────────────────────────────────────┐
│ Services                            │
├─────────────────────────────────────┤
│ • General Checkup - $75             │
│ • X-Ray - $150                      │
│ • Consultation - $50                │
└─────────────────────────────────────┘
```

**✅ PASS:** Different services displayed for each tenant  
**❌ FAIL:** Same services displayed for both tenants

## 🎯 Quick Visual Checklist

### For Tenant A (tenant-a.localhost:3001)

```
DevTools Checklist:
┌─────────────────────────────────────┐
│ ✅ Cookie Panel                     │
│    └─ tenantSlug = "tenant-a"       │
│                                     │
│ ✅ Network Tab                      │
│    └─ x-tenant-slug: tenant-a       │
│                                     │
│ ✅ Services Page                    │
│    └─ Shows tenant-a services       │
└─────────────────────────────────────┘
```

### For Tenant B (tenant-b.localhost:3001)

```
DevTools Checklist:
┌─────────────────────────────────────┐
│ ✅ Cookie Panel                     │
│    └─ tenantSlug = "tenant-b"       │
│                                     │
│ ✅ Network Tab                      │
│    └─ x-tenant-slug: tenant-b       │
│                                     │
│ ✅ Services Page                    │
│    └─ Shows tenant-b services       │
└─────────────────────────────────────┘
```

## 🔍 Browser Console Verification

### Quick Check Command

Open browser console (F12 > Console tab) and paste:

```javascript
// Check current tenant context
window.SubdomainRoutingTests.verifyTenantContext();
```

### Expected Output (Tenant A)

```
🔍 Tenant Context Verification:
   Hostname: tenant-a.localhost
   Expected Tenant: tenant-a
   Cookie Value: tenant-a
   Status: ✅ CORRECT
```

### Expected Output (Tenant B)

```
🔍 Tenant Context Verification:
   Hostname: tenant-b.localhost
   Expected Tenant: tenant-b
   Cookie Value: tenant-b
   Status: ✅ CORRECT
```

### Error Output (Mismatch)

```
🔍 Tenant Context Verification:
   Hostname: tenant-a.localhost
   Expected Tenant: tenant-a
   Cookie Value: default
   Status: ❌ MISMATCH
```

## 📸 Screenshot Locations

### Screenshot 1: Cookies Panel
**Location:** DevTools > Application > Cookies > tenant-a.localhost  
**What to capture:** The cookies table showing `tenantSlug` cookie

### Screenshot 2: Network Headers
**Location:** DevTools > Network > [API Request] > Headers  
**What to capture:** Request Headers section showing `x-tenant-slug`

### Screenshot 3: Services Page
**Location:** Browser window showing services page  
**What to capture:** Full page showing different services for each tenant

## 🎨 Color Coding for Results

When documenting results, use:
- 🟢 **Green** - Test passed, correct value
- 🔴 **Red** - Test failed, incorrect value
- 🟡 **Yellow** - Warning, needs attention
- ⚪ **White** - Not tested yet

Example:
```
Cookie Value: tenant-a 🟢
API Header: tenant-a 🟢
Data Isolation: Verified 🟢
```

## 💡 Pro Tips

1. **Use Incognito/Private Window** - Ensures clean state, no cached cookies
2. **Clear Network Log** - Click 🚫 before each test for clean results
3. **Filter by Domain** - Type API domain in Network filter to see only API calls
4. **Preserve Log** - Check "Preserve log" to keep requests across page navigations
5. **Disable Cache** - Check "Disable cache" in Network tab for fresh requests

## 🚨 Common Visual Issues

### Issue: Cookie Shows "default" Instead of Tenant Slug
```
❌ tenantSlug = "default"
✅ tenantSlug = "tenant-a"
```
**Fix:** Check middleware is running, verify subdomain in URL

### Issue: No x-tenant-slug Header
```
Request Headers:
  Accept: application/json
  Content-Type: application/json
  ❌ x-tenant-slug: (missing)
```
**Fix:** Check api.ts includes tenant header logic, verify cookie is set

### Issue: Wrong Tenant Header
```
URL: tenant-a.localhost:3001
❌ x-tenant-slug: tenant-b
✅ x-tenant-slug: tenant-a
```
**Fix:** Clear cookies, refresh page, verify middleware updates cookie

---

**Remember:** Visual verification is key! Always check both the cookie AND the API headers to ensure complete tenant isolation.
