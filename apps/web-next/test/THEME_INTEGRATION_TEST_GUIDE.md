# Manual Theme Selection Integration Test Guide

This guide provides comprehensive instructions for testing the manual theme selection system.

## Overview

The integration tests verify:
1. **Tenant Resolution with Subdomain** - Requirement 4.5, 6.3
2. **Tenant Resolution with Custom Domain** - Requirement 6.3, 6.4
3. **Theme Loading for Different Tenants** - Requirement 4.5, 6.3
4. **Theme Switching via API** - Requirement 8.3
5. **Fallback to Default Theme** - Requirement 4.5

## Prerequisites

### 1. Database Setup

Ensure PostgreSQL is running and configured:

```bash
# Check DATABASE_URL in .env.local
cat apps/web-next/.env.local | grep DATABASE_URL

# Run migrations
cd apps/web-next
pnpm prisma:migrate

# Seed database with themes and tenants
pnpm prisma:seed

# Verify themes exist
pnpm prisma:studio
# Navigate to Theme table and verify clinic, barber, default themes exist
```

### 2. Hosts File Configuration

Add test subdomains to your `/etc/hosts` file:

```bash
# macOS/Linux
sudo nano /etc/hosts

# Add these lines:
127.0.0.1 clinic-a.localhost
127.0.0.1 clinic-b.localhost
127.0.0.1 custom.localhost
127.0.0.1 no-theme.localhost
```

**Windows:**
```
# Edit as Administrator:
C:\Windows\System32\drivers\etc\hosts

# Add the same lines
```

### 3. Start Development Server

```bash
cd apps/web-next
pnpm dev
```

Server should be running on `http://localhost:3001`

## Test Execution

### Automated Tests

Run the automated test suite:

```bash
cd apps/web-next
node test/theme-integration.test.js run
```

**Expected Output:**
```
🚀 Running Automated Theme Integration Tests

Test 1: Verify themes API...
✅ Themes API working

Test 2: Verify tenant API...
✅ Tenant API working

Test 3: Verify theme update API validation...
✅ Theme update API validation working

==================================================
Test Results: 3 passed, 0 failed
==================================================
```

### Manual Browser Tests

#### Test 1: Tenant Resolution with Subdomain

1. Open browser and navigate to `http://clinic-a.localhost:3001`
2. Open DevTools (F12)
3. Go to **Application** tab > **Cookies** > `clinic-a.localhost`
4. Verify cookie `tenantSlug` = `clinic-a`
5. Go to **Network** tab
6. Refresh page and click on any request
7. Check **Request Headers** for `x-tenant-slug: clinic-a`
8. Navigate to `http://clinic-b.localhost:3001`
9. Verify cookie updates to `tenantSlug` = `clinic-b`
10. Verify header updates to `x-tenant-slug: clinic-b`

**Expected Results:**
- ✅ Cookie is set correctly for each subdomain
- ✅ Header is set correctly for each subdomain
- ✅ Cookie and header update when switching subdomains

#### Test 2: Tenant Resolution with Custom Domain

1. Setup custom domain in database:
```sql
UPDATE tenants 
SET domain = 'custom.localhost' 
WHERE slug = 'custom-clinic';
```

2. Navigate to `http://custom.localhost:3001`
3. Open DevTools > Application > Cookies
4. Verify `tenantSlug` cookie is set
5. Check Network tab for `x-tenant-slug` header
6. Verify tenant is resolved from custom domain

**Expected Results:**
- ✅ Custom domain resolves to correct tenant
- ✅ Cookie and header are set correctly
- ✅ Middleware queries database by domain

#### Test 3: Theme Loading for Different Tenants

1. Navigate to `http://clinic-a.localhost:3001`
2. Inspect page elements
3. Look for clinic-specific CSS classes (e.g., `.clinic-theme`)
4. Verify clinic theme header and footer components
5. Navigate to `http://clinic-b.localhost:3001`
6. Verify different theme is loaded (barber theme)
7. Look for barber-specific CSS classes (e.g., `.barber-theme`)
8. Compare the two pages - they should have different layouts

**Expected Results:**
- ✅ Clinic-a loads clinic theme
- ✅ Clinic-b loads barber theme
- ✅ Themes have different layouts and styling
- ✅ Theme-specific components are rendered

#### Test 4: Theme Switching via API

1. Navigate to `http://clinic-a.localhost:3001/en/dashboard/settings/theme`
2. Note the current theme displayed
3. Open DevTools > Network tab
4. Select a different theme from the dropdown
5. Click "Save Theme" button
6. Observe POST request to `/api/tenants/clinic-a/theme`
7. Verify response shows updated theme
8. Page should reload automatically
9. Verify new theme is now active

**Expected Results:**
- ✅ Theme settings page displays current theme
- ✅ Available themes are listed in dropdown
- ✅ Theme can be changed via dropdown
- ✅ API request updates theme in database
- ✅ Page reloads with new theme
- ✅ New theme persists across navigation

#### Test 5: Fallback to Default Theme

1. Create tenant without theme:
```sql
INSERT INTO tenants (slug, name, status, created_at, updated_at)
VALUES ('no-theme', 'No Theme Tenant', 'ACTIVE', NOW(), NOW());
```

2. Navigate to `http://no-theme.localhost:3001`
3. Verify page loads successfully
4. Inspect page elements
5. Look for default theme classes
6. Verify default theme components are rendered

**Expected Results:**
- ✅ Tenant without theme loads successfully
- ✅ Default theme is used as fallback
- ✅ Page is fully functional
- ✅ No errors in console

### API Testing with cURL

#### Get Available Themes
```bash
curl http://localhost:3001/api/themes | jq
```

**Expected Response:**
```json
[
  {
    "id": "clxxx...",
    "key": "barber",
    "name": "Barber Theme"
  },
  {
    "id": "clxxx...",
    "key": "clinic",
    "name": "Clinic Theme"
  },
  {
    "id": "clxxx...",
    "key": "default",
    "name": "Default Theme"
  }
]
```

#### Get Current Tenant Theme
```bash
curl -H "x-tenant-slug: clinic-a" http://localhost:3001/api/internal/tenant | jq
```

**Expected Response:**
```json
{
  "slug": "clinic-a",
  "themeId": "clxxx..."
}
```

#### Update Tenant Theme
```bash
# First, get a theme ID from /api/themes
THEME_ID="clxxx..."

# Update theme
curl -X POST http://localhost:3001/api/tenants/clinic-a/theme \
  -H "Content-Type: application/json" \
  -d "{\"themeId\": \"$THEME_ID\"}" | jq
```

**Expected Response:**
```json
{
  "id": "1",
  "slug": "clinic-a",
  "domain": null,
  "status": "ACTIVE",
  "themeId": "clxxx...",
  "theme": {
    "id": "clxxx...",
    "key": "barber",
    "name": "Barber Theme"
  },
  "updatedAt": "2024-01-01T00:00:00.000Z"
}
```

#### Test Invalid Theme ID
```bash
curl -X POST http://localhost:3001/api/tenants/clinic-a/theme \
  -H "Content-Type: application/json" \
  -d '{"themeId": "invalid-id"}' | jq
```

**Expected Response:**
```json
{
  "error": "Theme not found"
}
```

## Verification Checklist

### Tenant Resolution
- [ ] Subdomain resolution works (clinic-a.localhost → clinic-a)
- [ ] Custom domain resolution works (custom.localhost → custom-clinic)
- [ ] x-tenant-slug header is set correctly
- [ ] tenantSlug cookie is set correctly
- [ ] Tenant context switches when changing subdomains
- [ ] Inactive tenants show error page

### Theme Loading
- [ ] Clinic theme loads for clinic-a tenant
- [ ] Barber theme loads for clinic-b tenant
- [ ] Theme-specific layouts are rendered
- [ ] Theme-specific components are rendered
- [ ] Theme-specific styling is applied
- [ ] Themes are properly isolated (no cross-theme leakage)

### Theme Switching
- [ ] GET /api/themes returns published themes
- [ ] GET /api/internal/tenant returns current theme
- [ ] POST /api/tenants/[slug]/theme updates theme
- [ ] Theme validation ensures theme exists
- [ ] Only published themes can be assigned
- [ ] Invalid theme IDs are rejected
- [ ] Draft themes cannot be assigned
- [ ] Theme change takes effect immediately
- [ ] Theme settings page works correctly

### Fallback Behavior
- [ ] Tenant without theme uses default theme
- [ ] Missing theme file falls back to default
- [ ] Database errors fall back to default
- [ ] Default theme renders correctly
- [ ] No errors when using default theme

## Troubleshooting

### Issue: Subdomain doesn't resolve
**Solution:** 
- Check `/etc/hosts` file has correct entries
- Restart browser after editing hosts file
- Clear DNS cache: `sudo dscacheutil -flushcache` (macOS)

### Issue: Theme doesn't load
**Solution:**
- Verify theme files exist in `themes/` directory
- Check theme is in database: `pnpm prisma:studio`
- Check server console for errors
- Verify theme status is 'published'

### Issue: Theme doesn't change after API call
**Solution:**
- Check API response for errors
- Verify database was updated: `pnpm prisma:studio`
- Clear browser cache
- Check server console for errors
- Ensure page reloaded after save

### Issue: Default theme not working
**Solution:**
- Verify `themes/default/layout.tsx` exists
- Check default theme components exist
- Look for errors in server console

### Issue: Custom domain not working
**Solution:**
- Verify domain field in database
- Check middleware logic for domain lookup
- Ensure domain is unique in database
- Check `/etc/hosts` has entry for custom domain

### Issue: API returns 404
**Solution:**
- Verify tenant slug is correct
- Check theme ID is valid
- Ensure development server is running
- Check API route files exist

## Database Queries for Verification

### Check Themes
```sql
SELECT * FROM "Theme" WHERE status = 'published';
```

### Check Tenants with Themes
```sql
SELECT 
  t.slug, 
  t.name, 
  t.domain, 
  t.status,
  th.key as theme_key, 
  th.name as theme_name
FROM tenants t
LEFT JOIN "Theme" th ON t."themeId" = th.id
ORDER BY t.slug;
```

### Check Theme Assignments
```sql
SELECT 
  COUNT(*) as tenant_count,
  th.key as theme_key,
  th.name as theme_name
FROM tenants t
JOIN "Theme" th ON t."themeId" = th.id
GROUP BY th.key, th.name;
```

### Find Tenants Without Themes
```sql
SELECT slug, name, status
FROM tenants
WHERE "themeId" IS NULL;
```

## Test Results Documentation

After running all tests, document results:

```
Test Execution Date: [DATE]
Tester: [NAME]

Tenant Resolution Tests:
- Subdomain Resolution: [PASS/FAIL]
- Custom Domain Resolution: [PASS/FAIL]
- Header/Cookie Setting: [PASS/FAIL]

Theme Loading Tests:
- Clinic Theme Loading: [PASS/FAIL]
- Barber Theme Loading: [PASS/FAIL]
- Theme Isolation: [PASS/FAIL]

Theme Switching Tests:
- API Theme List: [PASS/FAIL]
- API Theme Update: [PASS/FAIL]
- Immediate Effect: [PASS/FAIL]
- Validation: [PASS/FAIL]

Fallback Tests:
- Default Theme Fallback: [PASS/FAIL]
- Error Handling: [PASS/FAIL]

Notes:
[Any issues or observations]
```

## Continuous Integration

For CI/CD pipelines, create a test script:

```bash
#!/bin/bash
# test-themes.sh

set -e

echo "Setting up test environment..."
pnpm prisma:migrate
pnpm prisma:seed

echo "Starting development server..."
pnpm dev &
SERVER_PID=$!

# Wait for server to start
sleep 5

echo "Running automated tests..."
node test/theme-integration.test.js run

# Cleanup
kill $SERVER_PID

echo "Tests completed successfully!"
```

## Additional Resources

- [Design Document](.kiro/specs/manual-theme-selection/design.md)
- [Requirements Document](.kiro/specs/manual-theme-selection/requirements.md)
- [Tasks Document](.kiro/specs/manual-theme-selection/tasks.md)
- [Prisma Schema](prisma/schema.prisma)
- [Middleware Implementation](middleware.ts)
- [Theme Service](lib/theme.ts)

## Support

If you encounter issues not covered in this guide:
1. Check server console logs
2. Check browser console logs
3. Verify database state with Prisma Studio
4. Review middleware logs
5. Check Network tab in DevTools for API requests/responses
