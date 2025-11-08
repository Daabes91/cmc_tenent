# Tenant Switcher Manual Testing Guide

This guide provides step-by-step instructions for manually testing the admin UI tenant switcher functionality.

## Prerequisites

1. Admin application running at `http://localhost:3001`
2. API server running at `http://localhost:8080`
3. At least two tenants configured in the database:
   - `default` (default tenant)
   - `clinic-b` (secondary tenant for testing)
4. Test data in both tenants (doctors, patients, appointments, etc.)

## Test Scenarios

### Test 1: Verify Current Tenant Display

**Objective:** Confirm that the current tenant is displayed in the UI

**Steps:**
1. Open browser and navigate to `http://localhost:3001`
2. Log in with admin credentials
3. Look for the tenant switcher in the top header (desktop view)
4. Verify you see a button with:
   - Network icon (🌐)
   - "Active Tenant" label
   - Current tenant slug in a code badge (e.g., `default`)

**Expected Result:**
- ✅ Tenant switcher is visible in the header
- ✅ Current tenant slug is displayed correctly
- ✅ Badge shows the tenant slug in a styled format

**Screenshot Location:** The tenant switcher should be in the top-right area of the header, before the theme toggle and language selector.

---

### Test 2: Open Tenant Switcher Popover

**Objective:** Verify the tenant switcher popover opens and displays correctly

**Steps:**
1. Hover over or click the tenant switcher button
2. Observe the popover that appears

**Expected Result:**
- ✅ Popover opens with a clean, styled interface
- ✅ Shows "Tenant Switcher" title
- ✅ Displays description text
- ✅ Contains an input field with current tenant slug
- ✅ Shows "Apply" and "Reset" buttons
- ✅ Displays hint text about tenant switching

---

### Test 3: Switch to Different Tenant

**Objective:** Verify switching to a different tenant works correctly

**Steps:**
1. Open the tenant switcher popover
2. Note the current tenant (e.g., `default`)
3. Clear the input field
4. Type a different tenant slug (e.g., `clinic-b`)
5. Click the "Apply" button
6. Wait for page reload

**Expected Result:**
- ✅ Page reloads automatically
- ✅ Tenant switcher now shows the new tenant slug (`clinic-b`)
- ✅ Cookie `tenantSlug` is updated to `clinic-b`
- ✅ All data in the UI reflects the new tenant

**Verification:**
- Open browser DevTools → Application → Cookies
- Check that `tenantSlug` cookie value is `clinic-b`

---

### Test 4: Verify Data Refresh After Tenant Switch

**Objective:** Confirm that data updates to show the new tenant's data

**Steps:**
1. Before switching: Navigate to `/doctors` page
2. Note the list of doctors displayed (Tenant A doctors)
3. Switch tenant using the tenant switcher (to `clinic-b`)
4. After page reload, observe the doctors list

**Expected Result:**
- ✅ Doctors list shows different doctors (Tenant B doctors)
- ✅ No doctors from Tenant A are visible
- ✅ Data is completely isolated between tenants

**Repeat for other pages:**
- `/patients` - Should show Tenant B patients only
- `/appointments` - Should show Tenant B appointments only
- `/services` - Should show Tenant B services only
- `/blogs` - Should show Tenant B blogs only

---

### Test 5: Verify API Requests Include Correct Header

**Objective:** Confirm that API requests include the correct `X-Tenant-Slug` header

**Steps:**
1. Open browser DevTools → Network tab
2. Switch to tenant `clinic-b`
3. After page reload, navigate to `/doctors`
4. Observe the API request to `/admin/doctors` in the Network tab
5. Click on the request and view the Request Headers

**Expected Result:**
- ✅ Request includes header: `X-Tenant-Slug: clinic-b`
- ✅ Response contains only Tenant B data
- ✅ All subsequent API requests include the same header

**Check multiple endpoints:**
- `/admin/doctors` → Should have `X-Tenant-Slug: clinic-b`
- `/admin/patients` → Should have `X-Tenant-Slug: clinic-b`
- `/admin/appointments` → Should have `X-Tenant-Slug: clinic-b`
- `/public/services` → Should have `X-Tenant-Slug: clinic-b`

---

### Test 6: Reset to Default Tenant

**Objective:** Verify the "Reset" button returns to the default tenant

**Steps:**
1. Switch to a non-default tenant (e.g., `clinic-b`)
2. Open the tenant switcher popover
3. Click the "Reset" button
4. Wait for page reload

**Expected Result:**
- ✅ Page reloads automatically
- ✅ Tenant switcher shows `default` tenant
- ✅ Cookie `tenantSlug` is reset to `default`
- ✅ Data shows default tenant's data

---

### Test 7: Tenant Slug Normalization

**Objective:** Verify that tenant slugs are normalized to lowercase

**Steps:**
1. Open tenant switcher popover
2. Enter tenant slug with uppercase letters: `CLINIC-B`
3. Click "Apply"
4. After reload, check the tenant switcher display

**Expected Result:**
- ✅ Tenant slug is normalized to lowercase: `clinic-b`
- ✅ Cookie stores lowercase value
- ✅ API requests use lowercase header value

---

### Test 8: Tenant Persistence Across Page Navigation

**Objective:** Verify tenant context persists when navigating between pages

**Steps:**
1. Switch to tenant `clinic-b`
2. Navigate to `/doctors`
3. Navigate to `/patients`
4. Navigate to `/appointments`
5. Navigate to `/dashboard`
6. Check tenant switcher on each page

**Expected Result:**
- ✅ Tenant remains `clinic-b` on all pages
- ✅ Cookie persists across navigation
- ✅ All API requests continue to use `X-Tenant-Slug: clinic-b`

---

### Test 9: Tenant Persistence After Browser Refresh

**Objective:** Verify tenant context persists after hard refresh

**Steps:**
1. Switch to tenant `clinic-b`
2. Press `Ctrl+Shift+R` (or `Cmd+Shift+R` on Mac) for hard refresh
3. Check tenant switcher after page loads

**Expected Result:**
- ✅ Tenant remains `clinic-b` after refresh
- ✅ Cookie persists through hard refresh
- ✅ Data continues to show Tenant B data

---

### Test 10: Invalid Tenant Handling

**Objective:** Verify behavior when switching to a non-existent tenant

**Steps:**
1. Open tenant switcher popover
2. Enter a non-existent tenant slug: `invalid-tenant-xyz`
3. Click "Apply"
4. Observe API responses in Network tab

**Expected Result:**
- ✅ Page reloads with the invalid tenant slug
- ✅ API requests include `X-Tenant-Slug: invalid-tenant-xyz`
- ✅ API returns appropriate errors (404 or empty data)
- ✅ UI handles the error gracefully (shows empty states)

---

## Test Results Checklist

Use this checklist to track your manual testing progress:

- [ ] Test 1: Current tenant display verified
- [ ] Test 2: Tenant switcher popover opens correctly
- [ ] Test 3: Successfully switched to different tenant
- [ ] Test 4: Data refreshed to show new tenant's data
- [ ] Test 5: API requests include correct X-Tenant-Slug header
- [ ] Test 6: Reset button returns to default tenant
- [ ] Test 7: Tenant slug normalized to lowercase
- [ ] Test 8: Tenant persists across page navigation
- [ ] Test 9: Tenant persists after browser refresh
- [ ] Test 10: Invalid tenant handled gracefully

---

## Browser DevTools Tips

### Viewing Cookies
1. Open DevTools (F12)
2. Go to Application tab
3. Expand Cookies in left sidebar
4. Click on `http://localhost:3001`
5. Find `tenantSlug` cookie

### Viewing Network Requests
1. Open DevTools (F12)
2. Go to Network tab
3. Filter by "Fetch/XHR"
4. Click on any request
5. View "Headers" tab
6. Look for `X-Tenant-Slug` in Request Headers

### Console Logging
You can also check the tenant slug in the browser console:
```javascript
// Check cookie value
document.cookie.split('; ').find(row => row.startsWith('tenantSlug='))

// Check if tenant slug is in localStorage or sessionStorage
localStorage.getItem('tenant:slug')
```

---

## Expected Behavior Summary

✅ **Current Tenant Display:**
- Tenant switcher shows current tenant slug in a badge
- Badge is visible in the header on desktop view

✅ **Tenant Switching:**
- User can enter new tenant slug in input field
- Clicking "Apply" updates the tenant and reloads the page
- Clicking "Reset" returns to default tenant

✅ **Data Refresh:**
- After switching, all data reflects the new tenant
- No cross-tenant data leakage
- Each tenant sees only their own data

✅ **API Headers:**
- All API requests include `X-Tenant-Slug` header
- Header value matches the current tenant slug
- Header is automatically added by `useAdminApi` composable

✅ **Persistence:**
- Tenant context stored in cookie
- Persists across page navigation
- Persists across browser refresh
- Cookie path is `/` (accessible to all pages)

---

## Troubleshooting

### Tenant switcher not visible
- Check if you're on desktop view (hidden on mobile)
- Verify you're logged in
- Check browser console for errors

### Tenant not switching
- Check browser console for errors
- Verify the tenant exists in the database
- Check if page reload is blocked by browser

### Data not updating after switch
- Clear browser cache and cookies
- Check Network tab for API errors
- Verify API is returning correct data for the tenant

### API requests missing X-Tenant-Slug header
- Check if `useTenantSlug` composable is working
- Verify `useAdminApi` is being used for API calls
- Check browser console for errors

---

## Requirement Verification

This manual testing guide verifies **Requirement 6.1** from the specification:

> "WHEN the System receives a request with tenant_id equals 1, THE System SHALL return only data where tenant_id equals 1"

By completing all tests in this guide, you confirm:
- ✅ Current tenant is displayed in UI
- ✅ Tenant can be switched using the switcher
- ✅ Data refreshes to show new tenant's data
- ✅ API requests include correct X-Tenant-Slug header
- ✅ Tenant isolation is complete and working correctly
