# Theme System Troubleshooting Guide

Comprehensive troubleshooting guide for the Manual Theme Selection system.

## Table of Contents

- [Quick Diagnostics](#quick-diagnostics)
- [Common Issues](#common-issues)
- [Error Messages](#error-messages)
- [Performance Issues](#performance-issues)
- [Development Issues](#development-issues)
- [Production Issues](#production-issues)
- [Database Issues](#database-issues)
- [Debugging Tools](#debugging-tools)

## Quick Diagnostics

Run these commands to quickly diagnose issues:

```bash
# Check if application is running
curl -I http://localhost:3001

# Check database connection
cd apps/web-next
npx prisma db execute --stdin <<< "SELECT 1;"

# List themes in database
npx prisma db execute --stdin <<< "SELECT * FROM \"Theme\";"

# List tenants and their themes
npx prisma db execute --stdin <<< "SELECT t.slug, t.themeId, th.key FROM \"Tenant\" t LEFT JOIN \"Theme\" th ON t.themeId = th.id;"

# Check theme files exist
ls -la themes/

# Verify environment variables
cat .env | grep -E "DATABASE_URL|APP_BASE_DOMAIN"
```

## Common Issues

### Issue: Theme Not Loading

**Symptoms:**
- Tenant sees default theme instead of selected theme
- No errors in console
- Theme selection appears correct in database

**Diagnosis:**

1. **Check theme file exists:**
   ```bash
   ls -la themes/[theme-key]/layout.tsx
   ```

2. **Verify theme key matches:**
   ```bash
   # Check database
   npx prisma studio
   # Compare Theme.key with directory name
   ```

3. **Check for TypeScript errors:**
   ```bash
   npm run build
   # Look for compilation errors in theme files
   ```

**Solutions:**

**Solution 1: Theme file missing**
```bash
# Create missing theme directory
mkdir -p themes/[theme-key]/components/styles

# Create layout.tsx
cat > themes/[theme-key]/layout.tsx <<'EOF'
import { ThemeLayoutProps } from '../types'

export default function ThemeLayout({ children }: ThemeLayoutProps) {
  return <div>{children}</div>
}
EOF
```

**Solution 2: Theme key mismatch**
```sql
-- Update theme key to match directory name
UPDATE "Theme" SET key = 'correct-key' WHERE id = 'theme-id';
```

**Solution 3: Clear Next.js cache**
```bash
rm -rf .next
npm run dev
```

---

### Issue: Tenant Not Resolved

**Symptoms:**
- "Tenant not found" error page
- 404 errors
- Middleware not setting x-tenant header

**Diagnosis:**

1. **Check tenant exists:**
   ```sql
   SELECT * FROM "Tenant" WHERE slug = 'tenant-slug';
   ```

2. **Verify subdomain format:**
   ```bash
   # Should be: tenant-slug.APP_BASE_DOMAIN
   echo $APP_BASE_DOMAIN
   ```

3. **Check middleware logs:**
   ```bash
   # Add logging to middleware.ts
   console.log('Host:', request.headers.get('host'))
   console.log('Tenant:', tenantSlug)
   ```

**Solutions:**

**Solution 1: Create missing tenant**
```sql
INSERT INTO "Tenant" (id, slug, status, "createdAt", "updatedAt")
VALUES (gen_random_uuid()::text, 'tenant-slug', 'active', NOW(), NOW());
```

**Solution 2: Fix APP_BASE_DOMAIN**
```bash
# Update .env
echo "APP_BASE_DOMAIN=localhost:3001" >> .env

# Restart application
npm run dev
```

**Solution 3: Configure DNS**
```bash
# Add to /etc/hosts (local development)
echo "127.0.0.1 tenant-slug.localhost" | sudo tee -a /etc/hosts
```

---

### Issue: Styles Not Applied

**Symptoms:**
- Theme loads but looks unstyled
- CSS missing or not loading
- Layout appears broken

**Diagnosis:**

1. **Check CSS import:**
   ```typescript
   // In themes/[theme-key]/layout.tsx
   import './styles/theme.css' // Should be present
   ```

2. **Verify CSS file exists:**
   ```bash
   ls -la themes/[theme-key]/styles/theme.css
   ```

3. **Check browser console:**
   - Open DevTools → Console
   - Look for CSS loading errors

4. **Inspect build output:**
   ```bash
   npm run build
   ls -la .next/static/css/
   ```

**Solutions:**

**Solution 1: Add CSS import**
```typescript
// themes/[theme-key]/layout.tsx
import './styles/theme.css' // Add this line
```

**Solution 2: Create CSS file**
```bash
mkdir -p themes/[theme-key]/styles
cat > themes/[theme-key]/styles/theme.css <<'EOF'
.theme-container {
  min-height: 100vh;
}
EOF
```

**Solution 3: Clear cache and rebuild**
```bash
rm -rf .next
npm run build
```

**Solution 4: Check CSS specificity**
```css
/* Use more specific selectors */
.my-theme .header {
  /* styles */
}

/* Or use !important (last resort) */
.my-theme .header {
  background: blue !important;
}
```

---

### Issue: Dynamic Import Errors

**Symptoms:**
- Error: "Cannot find module"
- Error: "Failed to load theme"
- Application crashes on theme load

**Diagnosis:**

1. **Check error message:**
   ```bash
   # Look for specific module path
   Error: Cannot find module '@/themes/clinic/layout'
   ```

2. **Verify file structure:**
   ```bash
   tree themes/
   ```

3. **Check default export:**
   ```typescript
   // themes/[theme-key]/layout.tsx must have:
   export default function Layout() { }
   ```

**Solutions:**

**Solution 1: Fix file path**
```typescript
// Ensure path matches directory structure
const ThemeLayout = await import(`@/themes/${themeKey}/layout`)
```

**Solution 2: Add default export**
```typescript
// themes/[theme-key]/layout.tsx
export default function ThemeLayout({ children }: ThemeLayoutProps) {
  return <div>{children}</div>
}
```

**Solution 3: Check TypeScript config**
```json
// tsconfig.json
{
  "compilerOptions": {
    "paths": {
      "@/*": ["./*"]
    }
  }
}
```

---

### Issue: Database Connection Errors

**Symptoms:**
- Error: "Can't reach database server"
- Error: "Connection timeout"
- Prisma client errors

**Diagnosis:**

1. **Test database connection:**
   ```bash
   psql $DATABASE_URL -c "SELECT 1;"
   ```

2. **Check DATABASE_URL format:**
   ```bash
   echo $DATABASE_URL
   # Should be: postgresql://user:pass@host:port/db
   ```

3. **Verify database is running:**
   ```bash
   # PostgreSQL
   pg_isready -h hostname -p 5432
   ```

**Solutions:**

**Solution 1: Fix DATABASE_URL**
```bash
# Correct format
DATABASE_URL="postgresql://user:password@localhost:5432/dbname?schema=public"

# With SSL (production)
DATABASE_URL="postgresql://user:password@host:5432/dbname?sslmode=require"
```

**Solution 2: Start database**
```bash
# Docker
docker-compose up -d postgres

# Local PostgreSQL
sudo systemctl start postgresql
```

**Solution 3: Regenerate Prisma client**
```bash
npx prisma generate
```

---

### Issue: Theme Switching Not Working

**Symptoms:**
- Theme selection saves but doesn't apply
- Old theme still shows after switching
- No errors in console

**Diagnosis:**

1. **Check database update:**
   ```sql
   SELECT slug, themeId FROM "Tenant" WHERE slug = 'tenant-slug';
   ```

2. **Verify theme exists:**
   ```sql
   SELECT * FROM "Theme" WHERE id = 'theme-id';
   ```

3. **Check browser cache:**
   - Open DevTools → Network
   - Disable cache
   - Hard refresh (Cmd+Shift+R / Ctrl+Shift+F5)

**Solutions:**

**Solution 1: Force page reload**
```typescript
// In theme settings page
await fetch(`/api/tenants/${slug}/theme`, {
  method: 'POST',
  body: JSON.stringify({ themeId })
})

// Force reload
window.location.reload()
```

**Solution 2: Clear application cache**
```bash
# Development
rm -rf .next

# Production (Vercel)
vercel --prod --force
```

**Solution 3: Check API response**
```bash
curl -X POST http://localhost:3001/api/tenants/clinic-a/theme \
  -H "Content-Type: application/json" \
  -d '{"themeId": "theme-id"}' \
  -v
```

---

## Error Messages

### "Cannot find module '@/themes/[key]/layout'"

**Cause:** Theme directory or layout file doesn't exist

**Fix:**
```bash
# Create theme structure
mkdir -p themes/[key]
cat > themes/[key]/layout.tsx <<'EOF'
import { ThemeLayoutProps } from '../types'
export default function Layout({ children }: ThemeLayoutProps) {
  return <div>{children}</div>
}
EOF
```

---

### "Prisma Client is not configured"

**Cause:** Prisma client not generated

**Fix:**
```bash
npx prisma generate
```

---

### "P2002: Unique constraint failed"

**Cause:** Trying to create duplicate theme key or tenant slug

**Fix:**
```sql
-- Check existing records
SELECT * FROM "Theme" WHERE key = 'duplicate-key';
SELECT * FROM "Tenant" WHERE slug = 'duplicate-slug';

-- Use different key/slug or update existing record
```

---

### "x-tenant header not found"

**Cause:** Middleware not running or not setting header

**Fix:**
```typescript
// Check middleware.ts matcher config
export const config = {
  matcher: [
    '/((?!_next|_vercel|favicon.ico|api/internal|assets).*)',
  ],
}

// Ensure middleware is exported
export default async function middleware(request: NextRequest) {
  // ...
}
```

---

### "Theme not found" (404)

**Cause:** Theme ID doesn't exist in database

**Fix:**
```sql
-- List available themes
SELECT id, key, name FROM "Theme" WHERE status = 'published';

-- Use valid theme ID
UPDATE "Tenant" SET "themeId" = 'valid-theme-id' WHERE slug = 'tenant-slug';
```

---

## Performance Issues

### Slow Theme Loading

**Symptoms:**
- Long page load times
- Slow theme switching
- High database query times

**Diagnosis:**

1. **Check database query performance:**
   ```sql
   EXPLAIN ANALYZE 
   SELECT t.*, th.* FROM "Tenant" t 
   LEFT JOIN "Theme" th ON t."themeId" = th.id 
   WHERE t.slug = 'tenant-slug';
   ```

2. **Monitor response times:**
   ```bash
   curl -w "@curl-format.txt" -o /dev/null -s http://localhost:3001
   ```

3. **Check bundle size:**
   ```bash
   npm run build
   # Look for large theme bundles
   ```

**Solutions:**

**Solution 1: Add database indexes**
```sql
CREATE INDEX IF NOT EXISTS "Tenant_themeId_idx" ON "Tenant"("themeId");
CREATE INDEX IF NOT EXISTS "Tenant_slug_idx" ON "Tenant"("slug");
CREATE INDEX IF NOT EXISTS "Theme_status_idx" ON "Theme"("status");
```

**Solution 2: Implement caching**
```typescript
// lib/theme.ts
const themeCache = new Map<string, TenantTheme>()

export async function getTenantTheme(slug: string) {
  if (themeCache.has(slug)) {
    return themeCache.get(slug)
  }
  
  const theme = await prisma.tenant.findUnique({
    where: { slug },
    include: { theme: true }
  })
  
  if (theme) {
    themeCache.set(slug, theme)
  }
  
  return theme
}
```

**Solution 3: Optimize theme bundle**
```typescript
// Use dynamic imports for heavy components
const HeavyComponent = dynamic(() => import('./HeavyComponent'), {
  loading: () => <div>Loading...</div>
})
```

---

### High Memory Usage

**Symptoms:**
- Application crashes
- Out of memory errors
- Slow performance over time

**Diagnosis:**

1. **Monitor memory:**
   ```bash
   # Node.js
   node --max-old-space-size=4096 server.js
   
   # PM2
   pm2 monit
   ```

2. **Check for memory leaks:**
   ```bash
   node --inspect server.js
   # Use Chrome DevTools Memory profiler
   ```

**Solutions:**

**Solution 1: Increase memory limit**
```bash
# Set Node.js memory limit
NODE_OPTIONS="--max-old-space-size=4096" npm start
```

**Solution 2: Clear caches periodically**
```typescript
// Clear theme cache every hour
setInterval(() => {
  themeCache.clear()
}, 3600000)
```

**Solution 3: Optimize Prisma client**
```typescript
// Use select to fetch only needed fields
const tenant = await prisma.tenant.findUnique({
  where: { slug },
  select: {
    slug: true,
    themeId: true,
    theme: {
      select: {
        key: true,
        name: true
      }
    }
  }
})
```

---

## Development Issues

### Hot Reload Not Working

**Cause:** Next.js not detecting theme file changes

**Fix:**
```bash
# Restart dev server
npm run dev

# Or clear cache
rm -rf .next
npm run dev
```

---

### TypeScript Errors in Theme Files

**Cause:** Missing type definitions or incorrect imports

**Fix:**
```typescript
// Ensure proper imports
import { ThemeLayoutProps } from '../types'
import { ReactNode } from 'react'

// Use correct types
export default function Layout({ 
  children, 
  locale, 
  tenantSlug 
}: ThemeLayoutProps) {
  return <div>{children}</div>
}
```

---

### Prisma Studio Not Opening

**Cause:** Port conflict or database connection issue

**Fix:**
```bash
# Try different port
npx prisma studio --port 5556

# Check database connection
npx prisma db execute --stdin <<< "SELECT 1;"
```

---

## Production Issues

### 500 Internal Server Error

**Diagnosis:**

1. **Check application logs:**
   ```bash
   # PM2
   pm2 logs web-next --lines 100
   
   # Docker
   docker logs web-next --tail 100
   
   # Vercel
   vercel logs
   ```

2. **Check database connectivity:**
   ```bash
   psql $DATABASE_URL -c "SELECT 1;"
   ```

**Solutions:**

- Review error logs for specific issues
- Verify environment variables are set
- Check database is accessible from production server
- Ensure all migrations have been run

---

### SSL/TLS Errors

**Cause:** Database requires SSL but not configured

**Fix:**
```bash
# Add SSL mode to DATABASE_URL
DATABASE_URL="postgresql://user:pass@host:5432/db?sslmode=require"
```

---

### CORS Errors

**Cause:** API requests from different origin

**Fix:**
```typescript
// next.config.ts
const nextConfig = {
  async headers() {
    return [
      {
        source: '/api/:path*',
        headers: [
          { key: 'Access-Control-Allow-Origin', value: '*' },
          { key: 'Access-Control-Allow-Methods', value: 'GET,POST,PUT,DELETE' },
        ],
      },
    ]
  },
}
```

---

## Database Issues

### Migration Failures

**Symptoms:**
- Migration fails to apply
- Schema out of sync
- Constraint violations

**Diagnosis:**
```bash
# Check migration status
npx prisma migrate status

# View migration history
npx prisma migrate history
```

**Solutions:**

**Solution 1: Reset database (development only)**
```bash
npx prisma migrate reset
```

**Solution 2: Resolve conflicts manually**
```bash
# Mark migration as applied
npx prisma migrate resolve --applied "migration-name"

# Or roll back
npx prisma migrate resolve --rolled-back "migration-name"
```

**Solution 3: Create new migration**
```bash
npx prisma migrate dev --name fix_schema
```

---

### Data Integrity Issues

**Symptoms:**
- Orphaned records
- Missing foreign key relationships
- Inconsistent data

**Diagnosis:**
```sql
-- Find tenants with invalid theme IDs
SELECT t.* FROM "Tenant" t 
LEFT JOIN "Theme" th ON t."themeId" = th.id 
WHERE t."themeId" IS NOT NULL AND th.id IS NULL;

-- Find themes with no tenants
SELECT th.* FROM "Theme" th 
LEFT JOIN "Tenant" t ON th.id = t."themeId" 
WHERE t.id IS NULL;
```

**Solutions:**

**Solution 1: Fix orphaned records**
```sql
-- Set invalid theme IDs to null
UPDATE "Tenant" SET "themeId" = NULL 
WHERE "themeId" NOT IN (SELECT id FROM "Theme");

-- Or assign default theme
UPDATE "Tenant" SET "themeId" = (
  SELECT id FROM "Theme" WHERE key = 'default'
) WHERE "themeId" IS NULL;
```

**Solution 2: Clean up unused themes**
```sql
-- Mark unused themes as draft
UPDATE "Theme" SET status = 'draft' 
WHERE id NOT IN (SELECT DISTINCT "themeId" FROM "Tenant" WHERE "themeId" IS NOT NULL);
```

---

## Debugging Tools

### Enable Debug Logging

```bash
# Prisma query logging
DEBUG=prisma:* npm run dev

# Next.js debug mode
NODE_OPTIONS='--inspect' npm run dev
```

### Browser DevTools

1. **Network Tab:**
   - Check API requests
   - Verify response codes
   - Inspect headers

2. **Console Tab:**
   - Look for JavaScript errors
   - Check for warnings

3. **Application Tab:**
   - Clear cache and cookies
   - Inspect local storage

### Database Inspection

```bash
# Open Prisma Studio
npx prisma studio

# Or use psql
psql $DATABASE_URL

# Useful queries
\dt                    # List tables
\d "Theme"            # Describe Theme table
\d "Tenant"           # Describe Tenant table
SELECT * FROM "Theme";
SELECT * FROM "Tenant";
```

### Testing Tools

```bash
# Test API endpoints
curl -v http://localhost:3001/api/themes

# Test with specific headers
curl -H "x-tenant: clinic-a" http://localhost:3001/api/internal/tenant

# Test POST requests
curl -X POST http://localhost:3001/api/tenants/clinic-a/theme \
  -H "Content-Type: application/json" \
  -d '{"themeId": "theme-id"}' \
  -v
```

---

## Getting Additional Help

If issues persist:

1. **Check documentation:**
   - [Main Documentation](THEME_SYSTEM.md)
   - [API Reference](THEME_API_REFERENCE.md)
   - [Deployment Guide](THEME_DEPLOYMENT.md)

2. **Review logs:**
   - Application logs
   - Database logs
   - Web server logs

3. **Test with default theme:**
   - Isolate theme-specific issues
   - Verify core functionality works

4. **Create minimal reproduction:**
   - Simplify to smallest failing case
   - Document steps to reproduce

5. **Check environment:**
   - Verify all environment variables
   - Ensure dependencies are installed
   - Confirm database is accessible
